package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.content.item.GlimmerBottleItem;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public class GlimmerCompanionSavedData extends SavedData {
    private static final String ID = "antarchy_glimmer_companions";
    private static final String ENTRIES_KEY = "Entries";
    private static final String OWNER_KEY = "Owner";
    private static final String COMPANION_KEY = "Companion";
    private static final String VARIANT_KEY = "Variant";
    private static final String RETIRED_KEY = "Retired";
    private static final double FOLLOW_RADIUS = 16.0D;

    public enum Standing {
        CURRENT,
        SUPERSEDED,
        RETIRED
    }

    public record Companion(UUID id, @Nullable GlimmerVariant variant) {
    }

    private final Map<UUID, Companion> companionByOwner = new HashMap<>();
    private final Set<UUID> retired = new HashSet<>();

    public static GlimmerCompanionSavedData create() {
        return new GlimmerCompanionSavedData();
    }

    public static GlimmerCompanionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        GlimmerCompanionSavedData data = new GlimmerCompanionSavedData();
        for (Tag rawEntry : tag.getList(ENTRIES_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag entryTag = (CompoundTag) rawEntry;
            GlimmerVariant variant = entryTag.contains(VARIANT_KEY) ? GlimmerVariant.byName(entryTag.getString(VARIANT_KEY)) : null;
            data.companionByOwner.put(entryTag.getUUID(OWNER_KEY), new Companion(entryTag.getUUID(COMPANION_KEY), variant));
        }
        for (Tag rawEntry : tag.getList(RETIRED_KEY, Tag.TAG_COMPOUND)) {
            data.retired.add(((CompoundTag) rawEntry).getUUID(COMPANION_KEY));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<UUID, Companion> entry : this.companionByOwner.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(OWNER_KEY, entry.getKey());
            entryTag.putUUID(COMPANION_KEY, entry.getValue().id());
            if (entry.getValue().variant() != null) {
                entryTag.putString(VARIANT_KEY, entry.getValue().variant().name());
            }
            entries.add(entryTag);
        }
        tag.put(ENTRIES_KEY, entries);

        ListTag retiredEntries = new ListTag();
        for (UUID id : this.retired) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(COMPANION_KEY, id);
            retiredEntries.add(entryTag);
        }
        tag.put(RETIRED_KEY, retiredEntries);
        return tag;
    }

    private static GlimmerCompanionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<GlimmerCompanionSavedData>(GlimmerCompanionSavedData::create, GlimmerCompanionSavedData::load, null),
                ID
        );
    }

    public static boolean hasCompanion(MinecraftServer server, UUID owner) {
        return get(server).companionByOwner.containsKey(owner);
    }

    public static void setCompanion(MinecraftServer server, UUID owner, UUID companion, GlimmerVariant variant) {
        GlimmerCompanionSavedData data = get(server);
        data.companionByOwner.put(owner, new Companion(companion, variant));
        data.setDirty();
    }

    public static void clearCompanion(MinecraftServer server, UUID owner, UUID companion) {
        GlimmerCompanionSavedData data = get(server);
        Companion current = data.companionByOwner.get(owner);
        if (current != null && companion.equals(current.id())) {
            data.companionByOwner.remove(owner);
            data.setDirty();
        }
    }

    public static ItemStack retireCompanion(MinecraftServer server, UUID owner) {
        GlimmerCompanionSavedData data = get(server);
        Companion current = data.companionByOwner.remove(owner);
        if (current == null) {
            return ItemStack.EMPTY;
        }
        data.setDirty();

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(current.id());
            if (entity instanceof GlimmerEntity glimmer) {
                ItemStack bottle = glimmer.toBottle();
                glimmer.discard();
                return bottle;
            }
        }

        if (current.variant() == null) {
            return ItemStack.EMPTY;
        }
        data.retired.add(current.id());
        return GlimmerBottleItem.create(current.variant());
    }

    public static Standing reconcile(MinecraftServer server, UUID owner, UUID companion, GlimmerVariant variant) {
        GlimmerCompanionSavedData data = get(server);
        if (data.retired.remove(companion)) {
            data.setDirty();
            return Standing.RETIRED;
        }

        Companion current = data.companionByOwner.get(owner);
        if (current == null) {
            data.companionByOwner.put(owner, new Companion(companion, variant));
            data.setDirty();
            return Standing.CURRENT;
        }
        if (!companion.equals(current.id())) {
            return Standing.SUPERSEDED;
        }
        if (current.variant() != variant) {
            data.companionByOwner.put(owner, new Companion(companion, variant));
            data.setDirty();
        }
        return Standing.CURRENT;
    }

    @Nullable
    public static GlimmerEntity findNearbyFollower(ServerPlayer player) {
        Companion current = get(player.server).companionByOwner.get(player.getUUID());
        if (current == null) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(current.id());
        if (entity instanceof GlimmerEntity glimmer
                && glimmer.isAlive()
                && !glimmer.isOrderedToSit()
                && glimmer.distanceToSqr(player) <= FOLLOW_RADIUS * FOLLOW_RADIUS) {
            return glimmer;
        }
        return null;
    }
}
