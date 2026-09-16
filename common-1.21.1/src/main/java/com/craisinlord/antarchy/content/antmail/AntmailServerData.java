package com.craisinlord.antarchy.content.antmail;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.craisinlord.antarchy.content.block.entity.ComputerBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AntmailServerData extends SavedData {
    private static final String ID = "antarchy_antmail";
    private static final int MAX_PENDING_MESSAGES = AntmailValidation.MAX_MAILBOX_MESSAGES;
    private final Map<AntmailAddress, Endpoint> endpoints = new LinkedHashMap<>();
    private final AntmailMailboxStore mailboxes = new AntmailMailboxStore();
    private final List<PendingMessage> pending = new ArrayList<>();
    private final Map<AntmailAddress, java.util.Set<ResourceLocation>> triggered = new LinkedHashMap<>();
    private final Map<AntmailAddress, Long> mailboxVersions = new LinkedHashMap<>();
    private final Map<UUID, AntmailAddress> lastUsedAddresses = new LinkedHashMap<>();
    private long randomMailDay = Long.MIN_VALUE;
    private int retryTicks;

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
        ListTag triggeredTags = tag.getList("Triggered", 10);
        for (int i = 0; i < triggeredTags.size(); i++) {
            CompoundTag triggeredTag = triggeredTags.getCompound(i);
            try {
                AntmailAddress address = AntmailAddress.parse(triggeredTag.getString("Address"));
                java.util.Set<ResourceLocation> ids = new java.util.LinkedHashSet<>();
                ListTag idsTag = triggeredTag.getList("Ids", 8);
                for (int j = 0; j < idsTag.size(); j++) ids.add(ResourceLocation.parse(idsTag.getString(j)));
                data.triggered.put(address, ids);
            } catch (RuntimeException ignored) {
            }
        }
        ListTag lastUsedTags = tag.getList("LastUsedAddresses", 10);
        for (int i = 0; i < lastUsedTags.size(); i++) {
            try {
                CompoundTag lastUsedTag = lastUsedTags.getCompound(i);
                data.lastUsedAddresses.put(UUID.fromString(lastUsedTag.getString("Player")), AntmailAddress.parse(lastUsedTag.getString("Address")));
            } catch (RuntimeException ignored) {
            }
        }
        data.randomMailDay = tag.contains("RandomMailDay") ? tag.getLong("RandomMailDay") : Long.MIN_VALUE;
        ListTag versionTags = tag.getList("MailboxVersions", 10);
        for (int i = 0; i < versionTags.size(); i++) {
            try {
                CompoundTag versionTag = versionTags.getCompound(i);
                data.mailboxVersions.put(AntmailAddress.parse(versionTag.getString("Address")), Math.max(1L, versionTag.getLong("Version")));
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
        ListTag triggeredTags = new ListTag();
        for (Map.Entry<AntmailAddress, java.util.Set<ResourceLocation>> entry : triggered.entrySet()) {
            CompoundTag triggeredTag = new CompoundTag();
            triggeredTag.putString("Address", entry.getKey().fullAddress());
            ListTag idsTag = new ListTag();
            for (ResourceLocation id : entry.getValue()) idsTag.add(net.minecraft.nbt.StringTag.valueOf(id.toString()));
            triggeredTag.put("Ids", idsTag);
            triggeredTags.add(triggeredTag);
        }
        tag.put("Triggered", triggeredTags);
        ListTag lastUsedTags = new ListTag();
        for (Map.Entry<UUID, AntmailAddress> entry : lastUsedAddresses.entrySet()) {
            CompoundTag lastUsedTag = new CompoundTag();
            lastUsedTag.putString("Player", entry.getKey().toString());
            lastUsedTag.putString("Address", entry.getValue().fullAddress());
            lastUsedTags.add(lastUsedTag);
        }
        tag.put("LastUsedAddresses", lastUsedTags);
        tag.putLong("RandomMailDay", randomMailDay);
        ListTag versionTags = new ListTag();
        for (Map.Entry<AntmailAddress, Long> entry : mailboxVersions.entrySet()) {
            CompoundTag versionTag = new CompoundTag();
            versionTag.putString("Address", entry.getKey().fullAddress());
            versionTag.putLong("Version", entry.getValue());
            versionTags.add(versionTag);
        }
        tag.put("MailboxVersions", versionTags);
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

    public synchronized List<AntmailAddress> registeredAddresses() {
        return List.copyOf(endpoints.keySet());
    }

    public synchronized AntmailAddress addressFor(ServerPlayer player) {
        AntmailAddress address = lastUsedAddresses.get(player.getUUID());
        return address != null && endpoints.containsKey(address) ? address : null;
    }

    public synchronized AntmailAddress lastUsedAddress(ServerPlayer player) {
        AntmailAddress address = lastUsedAddresses.get(player.getUUID());
        return address != null && endpoints.containsKey(address) ? address : null;
    }

    public synchronized void setLastUsedAddress(ServerPlayer player, AntmailAddress address) {
        if (address == null) return;
        lastUsedAddresses.put(player.getUUID(), address);
        setDirty();
    }

    public synchronized boolean claimTrigger(AntmailAddress address, ResourceLocation triggerId) {
        java.util.Set<ResourceLocation> ids = triggered.computeIfAbsent(address, ignored -> new java.util.LinkedHashSet<>());
        if (!ids.add(triggerId)) return false;
        setDirty();
        return true;
    }

    public synchronized boolean beginRandomMailDay(long day) {
        if (randomMailDay == day) return false;
        randomMailDay = day;
        setDirty();
        return true;
    }

    public synchronized AntmailMailbox mailbox(AntmailAddress address) {
        return mailboxes.get(address);
    }

    public synchronized AntmailMailbox mailboxOrCreate(AntmailAddress address) {
        AntmailMailbox mailbox = mailboxes.getOrCreate(address);
        setDirty();
        return mailbox;
    }

    public synchronized long mailboxVersion(AntmailAddress address) {
        return mailboxVersions.getOrDefault(address, 1L);
    }

    public synchronized void touchMailbox(AntmailAddress address) {
        if (address == null) return;
        mailboxVersions.put(address, mailboxVersion(address) + 1L);
        setDirty();
    }

    public synchronized void drainAddress(MinecraftServer server, ServerLevel level, BlockPos position) {
        AntmailAddress address = addressAt(level.dimension().location(), position);
        if (address != null) drain(server, address);
    }

    public synchronized void drainAvailable(MinecraftServer server) {
        if (pending.isEmpty() || ++retryTicks < 20) return;
        retryTicks = 0;
        List<AntmailAddress> available = new ArrayList<>();
        for (Map.Entry<AntmailAddress, Endpoint> entry : endpoints.entrySet()) {
            Endpoint endpoint = entry.getValue();
            ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(endpoint.dimension())));
            if (level != null && level.hasChunkAt(BlockPos.of(endpoint.position()))) available.add(entry.getKey());
        }
        for (AntmailAddress address : available) drain(server, address);
    }

    public synchronized AntmailDeliveryResult deliver(MinecraftServer server, AntmailMessage message) {
        if (message == null) return AntmailDeliveryResult.failed(AntmailDeliveryResult.Status.MESSAGE_INVALID, null, "missing_message");
        if (endpoint(message.recipient()) == null) return failedAndRecorded(message, AntmailDeliveryResult.Status.ADDRESS_NOT_FOUND, "address_not_found");
        AntmailMailbox destination = mailboxes.getOrCreate(message.recipient());
        if (destination.inbox().size() >= AntmailValidation.MAX_MAILBOX_MESSAGES) return failedAndRecorded(message, AntmailDeliveryResult.Status.MAILBOX_FULL, "mailbox_full");
        Endpoint target = endpoint(message.recipient());
        ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, ResourceLocation.parse(target.dimension())));
        if (level == null || !level.hasChunkAt(BlockPos.of(target.position()))) {
            if (pending.stream().anyMatch(item -> item.message().id().equals(message.id()))) {
                return AntmailDeliveryResult.failed(AntmailDeliveryResult.Status.DUPLICATE, message, "already_pending");
            }
            if (pending.size() >= MAX_PENDING_MESSAGES) {
                return failedAndRecorded(message, AntmailDeliveryResult.Status.FAILED, "pending_queue_full");
            }
            message.setDeliveryStatus("QUEUED");
            pending.add(new PendingMessage(message.recipient(), message));
            mailboxes.getOrCreate(message.sender()).addSent(message);
            touchMailbox(message.recipient());
            touchMailbox(message.sender());
            setDirty();
            return AntmailDeliveryResult.queued(message);
        }
        message.setDeliveryStatus("DELIVERED");
        destination.addIncoming(message);
        mailboxes.getOrCreate(message.sender()).addSent(message);
        touchMailbox(message.recipient());
        touchMailbox(message.sender());
        setDirty();
        return AntmailDeliveryResult.delivered(message);
    }

    private AntmailDeliveryResult failedAndRecorded(AntmailMessage message, AntmailDeliveryResult.Status status, String detail) {
        message.setDeliveryStatus("FAILED // " + detail.toUpperCase());
        mailboxes.getOrCreate(message.sender()).addSent(message);
        touchMailbox(message.sender());
        setDirty();
        return AntmailDeliveryResult.failed(status, message, detail);
    }

    public synchronized void drain(MinecraftServer server, AntmailAddress address) {
        AntmailMailbox mailbox = mailboxes.getOrCreate(address);
        final boolean[] delivered = {false};
        pending.removeIf(pendingMessage -> {
            if (!pendingMessage.recipient().equals(address) || mailbox.inbox().size() >= AntmailValidation.MAX_MAILBOX_MESSAGES) return false;
            if (!mailbox.addIncoming(pendingMessage.message())) return false;
            pendingMessage.message().setDeliveryStatus("DELIVERED");
            delivered[0] = true;
            return true;
        });
        if (delivered[0]) touchMailbox(address);
        setDirty();
    }

    public synchronized AntmailDeliveryResult retry(MinecraftServer server, AntmailAddress sender, UUID messageId) {
        AntmailMessage message = mailboxes.getOrCreate(sender).findSent(messageId);
        if (message == null) return null;
        pending.removeIf(item -> item.message().id().equals(messageId));
        return deliver(server, message);
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
