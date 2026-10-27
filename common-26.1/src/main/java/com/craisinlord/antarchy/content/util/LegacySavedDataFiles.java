package com.craisinlord.antarchy.content.util;

import com.craisinlord.antarchy.Antarchy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.SavedDataStorage;

public final class LegacySavedDataFiles {
    private LegacySavedDataFiles() {
    }

    public static <T extends SavedData> T computeIfAbsent(MinecraftServer server, SavedDataType<T> type, String legacyId) {
        return computeIfAbsent(server, server.overworld().getDataStorage(), server.getWorldPath(LevelResource.DATA), type, legacyId);
    }

    public static <T extends SavedData> T computeIfAbsent(ServerLevel level, SavedDataType<T> type, String legacyId) {
        MinecraftServer server = level.getServer();
        Path legacyFolder = level.dimension() == Level.OVERWORLD
                ? server.getWorldPath(LevelResource.DATA)
                : DimensionType.getStorageFolder(level.dimension(), server.getWorldPath(LevelResource.ROOT)).resolve("data");
        return computeIfAbsent(server, level.getDataStorage(), legacyFolder, type, legacyId);
    }

    private static <T extends SavedData> T computeIfAbsent(MinecraftServer server, SavedDataStorage storage, Path legacyFolder, SavedDataType<T> type, String legacyId) {
        T existing = storage.get(type);
        if (existing != null) {
            return existing;
        }
        T migrated = readLegacy(server, legacyFolder, type, legacyId);
        if (migrated != null) {
            storage.set(type, migrated);
            return migrated;
        }
        return storage.computeIfAbsent(type);
    }

    private static <T extends SavedData> T readLegacy(MinecraftServer server, Path legacyFolder, SavedDataType<T> type, String legacyId) {
        Path file = legacyFolder.resolve(legacyId + ".dat");
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            RegistryOps<Tag> ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            T data = type.codec()
                    .parse(ops, root.getCompoundOrEmpty("data"))
                    .resultOrPartial(error -> Antarchy.LOGGER.error("Failed to migrate legacy saved data {}: {}", file, error))
                    .orElse(null);
            if (data != null) {
                Antarchy.LOGGER.info("Migrated legacy saved data {} to {}", file, type.id());
            }
            return data;
        } catch (IOException exception) {
            Antarchy.LOGGER.error("Failed to read legacy saved data {}", file, exception);
            return null;
        }
    }
}
