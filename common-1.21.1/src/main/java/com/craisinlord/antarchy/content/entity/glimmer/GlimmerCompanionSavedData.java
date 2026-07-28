package com.craisinlord.antarchy.content.entity.glimmer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Tracks each player's current Glimmer companion so taming a new one despawns the old,
 * even if the old companion is in a different loaded dimension.
 */
public class GlimmerCompanionSavedData extends SavedData {
    private static final String ID = "antarchy_glimmer_companions";
    private static final String ENTRIES_KEY = "Entries";
    private static final String OWNER_KEY = "Owner";
    private static final String COMPANION_KEY = "Companion";

    private final Map<UUID, UUID> companionByOwner = new HashMap<>();

    public static GlimmerCompanionSavedData create() {
        return new GlimmerCompanionSavedData();
    }

    public static GlimmerCompanionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        GlimmerCompanionSavedData data = new GlimmerCompanionSavedData();
        for (net.minecraft.nbt.Tag rawEntry : tag.getList(ENTRIES_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag entryTag = (CompoundTag) rawEntry;
            data.companionByOwner.put(entryTag.getUUID(OWNER_KEY), entryTag.getUUID(COMPANION_KEY));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<UUID, UUID> entry : this.companionByOwner.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(OWNER_KEY, entry.getKey());
            entryTag.putUUID(COMPANION_KEY, entry.getValue());
            entries.add(entryTag);
        }
        tag.put(ENTRIES_KEY, entries);
        return tag;
    }

    private static GlimmerCompanionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<GlimmerCompanionSavedData>(GlimmerCompanionSavedData::create, GlimmerCompanionSavedData::load, null),
                ID
        );
    }

    /**
     * Records {@code newCompanion} as {@code owner}'s current Glimmer. Any previous
     * companion found in a currently loaded dimension isn't removed - it just loses
     * its owner and becomes a wild, re-tameable Glimmer again.
     */
    public static void replaceCompanion(MinecraftServer server, UUID owner, UUID newCompanion) {
        GlimmerCompanionSavedData data = get(server);
        UUID previous = data.companionByOwner.put(owner, newCompanion);
        data.setDirty();

        if (previous == null || previous.equals(newCompanion)) {
            return;
        }

        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(previous);
            if (entity instanceof GlimmerEntity glimmer) {
                glimmer.setTame(false, true);
                glimmer.setOwnerUUID(null);
                return;
            }
        }
    }

    /**
     * Removes {@code owner}'s companion record if it currently points at {@code companion},
     * used when that Glimmer is captured into a bottle rather than replaced by a new one.
     */
    public static void clearCompanion(MinecraftServer server, UUID owner, UUID companion) {
        GlimmerCompanionSavedData data = get(server);
        if (companion.equals(data.companionByOwner.get(owner))) {
            data.companionByOwner.remove(owner);
            data.setDirty();
        }
    }
}
