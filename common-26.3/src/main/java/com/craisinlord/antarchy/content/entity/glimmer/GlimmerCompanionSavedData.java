package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.util.LegacySavedDataFiles;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class GlimmerCompanionSavedData extends SavedData {
    private static final String ID = "antarchy_glimmer_companions";
    private static final String ENTRIES_KEY = "Entries";
    private static final String OWNER_KEY = "Owner";
    private static final String COMPANION_KEY = "Companion";
    private static final Codec<GlimmerCompanionSavedData> CODEC = CompoundTag.CODEC.xmap(GlimmerCompanionSavedData::load, GlimmerCompanionSavedData::save);
    private static final SavedDataType<GlimmerCompanionSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(Antarchy.MODID, ID), GlimmerCompanionSavedData::create, CODEC, null);

    private final Map<UUID, UUID> companionByOwner = new HashMap<>();

    public static GlimmerCompanionSavedData create() {
        return new GlimmerCompanionSavedData();
    }

    public static GlimmerCompanionSavedData load(CompoundTag tag) {
        GlimmerCompanionSavedData data = new GlimmerCompanionSavedData();
        for (net.minecraft.nbt.Tag rawEntry : tag.getListOrEmpty(ENTRIES_KEY)) {
            if (!(rawEntry instanceof CompoundTag entryTag)) {
                continue;
            }
            Optional<UUID> owner = entryTag.read(OWNER_KEY, UUIDUtil.CODEC);
            Optional<UUID> companion = entryTag.read(COMPANION_KEY, UUIDUtil.CODEC);
            if (owner.isPresent() && companion.isPresent()) {
                data.companionByOwner.put(owner.get(), companion.get());
            }
        }
        return data;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for (Map.Entry<UUID, UUID> entry : this.companionByOwner.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.store(OWNER_KEY, UUIDUtil.CODEC, entry.getKey());
            entryTag.store(COMPANION_KEY, UUIDUtil.CODEC, entry.getValue());
            entries.add(entryTag);
        }
        tag.put(ENTRIES_KEY, entries);
        return tag;
    }

    private static GlimmerCompanionSavedData get(MinecraftServer server) {
        return LegacySavedDataFiles.computeIfAbsent(server, TYPE, ID);
    }

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
                glimmer.setOwnerReference(null);
                return;
            }
        }
    }

    public static void clearCompanion(MinecraftServer server, UUID owner, UUID companion) {
        GlimmerCompanionSavedData data = get(server);
        if (companion.equals(data.companionByOwner.get(owner))) {
            data.companionByOwner.remove(owner);
            data.setDirty();
        }
    }
}
