package com.craisinlord.antarchy.content.antmail;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AntmailServerData extends SavedData {
    private static final String ID = "antarchy_antmail";
    private final Map<AntmailAddress, Endpoint> endpoints = new LinkedHashMap<>();
    private final AntmailMailboxStore mailboxes = new AntmailMailboxStore();
    private final List<PendingMessage> pending = new ArrayList<>();

    public static AntmailServerData create() {
        return new AntmailServerData();
    }

    public static AntmailServerData load(CompoundTag tag, HolderLookup.Provider registries) {
        AntmailServerData data = new AntmailServerData();
        ListTag endpointTags = tag.getList("Endpoints", 10);
        for (int i = 0; i < endpointTags.size(); i++) {
            try {
                CompoundTag endpoint = endpointTags.getCompound(i);
                AntmailAddress address = AntmailAddress.parse(endpoint.getString("Address"));
                data.endpoints.put(address, new Endpoint(endpoint.getString("Dimension"), endpoint.getLong("Position")));
            } catch (RuntimeException ignored) {
            }
        }
        AntmailServerData loaded = data;
        AntmailMailboxStore restored = AntmailMailboxStore.fromTag(tag.getCompound("Mailboxes"));
        for (AntmailMailbox mailbox : restored.mailboxes()) {
            loaded.mailboxes.put(mailbox);
        }
        ListTag pendingTags = tag.getList("Pending", 10);
        for (int i = 0; i < pendingTags.size(); i++) {
            try {
                CompoundTag pendingTag = pendingTags.getCompound(i);
                loaded.pending.add(new PendingMessage(AntmailAddress.parse(pendingTag.getString("Recipient")), AntmailMessage.fromTag(pendingTag.getCompound("Message"))));
            } catch (RuntimeException ignored) {
            }
        }
        return loaded;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag endpointTags = new ListTag();
        for (Map.Entry<AntmailAddress, Endpoint> entry : endpoints.entrySet()) {
            CompoundTag endpoint = new CompoundTag();
            endpoint.putString("Address", entry.getKey().fullAddress());
            endpoint.putString("Dimension", entry.getValue().dimension());
            endpoint.putLong("Position", entry.getValue().position());
            endpointTags.add(endpoint);
        }
        tag.put("Endpoints", endpointTags);
        tag.put("Mailboxes", mailboxes.toTag());
        ListTag pendingTags = new ListTag();
        for (PendingMessage message : pending) {
            CompoundTag pendingTag = new CompoundTag();
            pendingTag.putString("Recipient", message.recipient().fullAddress());
            pendingTag.put("Message", message.message().toTag());
            pendingTags.add(pendingTag);
        }
        tag.put("Pending", pendingTags);
        return tag;
    }

    private static AntmailServerData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(AntmailServerData::create, AntmailServerData::load, null), ID);
    }

    public static AntmailServerData access(MinecraftServer server) {
        return get(server);
    }

    public synchronized Registration register(MinecraftServer server, ServerLevel level, BlockPos position, String username) {
        final AntmailAddress address;
        try {
            address = AntmailAddress.ofUsername(username);
        } catch (IllegalArgumentException exception) {
            return Registration.invalid("invalid_username");
        }
        AntmailAddress previous = addressAt(level.dimension().location(), position);
        Endpoint existing = endpoints.get(address);
        if (existing != null && (!existing.dimension().equals(level.dimension().location().toString()) || existing.position() != position.asLong())) {
            return Registration.invalid("username_taken");
        }
        if (previous != null && !previous.equals(address)) {
            endpoints.remove(previous);
        }
        endpoints.put(address, new Endpoint(level.dimension().location().toString(), position.asLong()));
        get(server).drain(server, address);
        setDirty();
        return Registration.accepted(address);
    }

    public synchronized AntmailAddress addressAt(ResourceLocation dimension, BlockPos position) {
        for (Map.Entry<AntmailAddress, Endpoint> entry : endpoints.entrySet()) {
            if (entry.getValue().dimension().equals(dimension.toString()) && entry.getValue().position() == position.asLong()) return entry.getKey();
        }
        return null;
    }

    public synchronized boolean unregister(ResourceLocation dimension, BlockPos position) {
        AntmailAddress address = addressAt(dimension, position);
        if (address == null) return false;
        endpoints.remove(address);
        setDirty();
        return true;
    }

    public synchronized Endpoint endpoint(AntmailAddress address) {
        return endpoints.get(address);
    }

    public synchronized AntmailMailbox mailbox(AntmailAddress address) {
        return mailboxes.get(address);
    }

    public synchronized AntmailMailbox mailboxOrCreate(AntmailAddress address) {
        AntmailMailbox mailbox = mailboxes.getOrCreate(address);
        setDirty();
        return mailbox;
    }

    public synchronized AntmailDeliveryResult deliver(MinecraftServer server, AntmailMessage message) {
        if (endpoint(message.recipient()) == null) return AntmailDeliveryResult.failed(AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND, message, "address_not_found");
        AntmailMailbox destination = mailboxes.getOrCreate(message.recipient());
        if (destination.inbox().size() >= AntmailValidation.MAX_MAILBOX_MESSAGES) return AntmailDeliveryResult.failed(AntmailDeliveryResult.Status.MAILBOX_FULL, message, "mailbox_full");
        Endpoint target = endpoint(message.recipient());
        ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(target.dimension())));
        if (level == null || !level.hasChunkAt(BlockPos.of(target.position()))) {
            pending.add(new PendingMessage(message.recipient(), message));
            mailboxes.getOrCreate(message.sender()).addSent(message);
            setDirty();
            return AntmailDeliveryResult.queued(message);
        }
        destination.addIncoming(message);
        mailboxes.getOrCreate(message.sender()).addSent(message);
        setDirty();
        return AntmailDeliveryResult.delivered(message);
    }

    public synchronized void drain(MinecraftServer server, AntmailAddress address) {
        AntmailMailbox mailbox = mailboxes.getOrCreate(address);
        pending.removeIf(pendingMessage -> {
            if (!pendingMessage.recipient().equals(address) || mailbox.inbox().size() >= AntmailValidation.MAX_MAILBOX_MESSAGES) return false;
            mailbox.addIncoming(pendingMessage.message());
            return true;
        });
        setDirty();
    }

    public record Endpoint(String dimension, long position) {
    }

    public record Registration(boolean valid, AntmailAddress address, String reason) {
        static Registration accepted(AntmailAddress address) { return new Registration(true, address, ""); }
        static Registration invalid(String reason) { return new Registration(false, null, reason); }
    }

    private record PendingMessage(AntmailAddress recipient, AntmailMessage message) {
    }
}
