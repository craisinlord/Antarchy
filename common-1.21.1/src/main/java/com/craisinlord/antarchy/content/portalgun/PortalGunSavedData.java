package com.craisinlord.antarchy.content.portalgun;

import java.util.HashMap;
import java.util.HashSet;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;

public final class PortalGunSavedData extends SavedData {
    private static final String ID = "antarchy_portal_gun_pairs";
    private static final String ENTRIES_KEY = "Entries";
    private static final String OWNER_KEY = "Owner";
    private static final String DIMENSION_KEY = "Dimension";
    private static final String GUN_KEY = "Gun";
    private static final String BLUE_KEY = "Blue";
    private static final String ORANGE_KEY = "Orange";
    private static final String CHANNELS_KEY = "Channels";
    private final Map<PairKey, PairRecord> pairs = new HashMap<>();
    private final Map<UUID, PairRecord> legacyPairs = new HashMap<>();
    private final Map<OwnerChannelKey, UUID> channelGunIds = new HashMap<>();

    public static PortalGunSavedData create() {
        return new PortalGunSavedData();
    }

    public static PortalGunSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PortalGunSavedData data = new PortalGunSavedData();
        for (net.minecraft.nbt.Tag rawEntry : tag.getList(ENTRIES_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag entryTag = (CompoundTag) rawEntry;
            UUID owner = entryTag.getUUID(OWNER_KEY);
            UUID blue = entryTag.hasUUID(BLUE_KEY) ? entryTag.getUUID(BLUE_KEY) : null;
            UUID orange = entryTag.hasUUID(ORANGE_KEY) ? entryTag.getUUID(ORANGE_KEY) : null;
            PairRecord record = new PairRecord(blue, orange);
            ResourceLocation dimension = entryTag.contains(DIMENSION_KEY) ? ResourceLocation.tryParse(entryTag.getString(DIMENSION_KEY)) : null;
            if (dimension == null) {
                data.legacyPairs.put(owner, record);
            } else {
                UUID gunId = entryTag.hasUUID(GUN_KEY) ? entryTag.getUUID(GUN_KEY) : null;
                data.pairs.put(new PairKey(owner, dimension, gunId), record);
            }
        }
        for (net.minecraft.nbt.Tag rawChannel : tag.getList(CHANNELS_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag channelTag = (CompoundTag) rawChannel;
            if (channelTag.hasUUID(OWNER_KEY) && channelTag.hasUUID(GUN_KEY) && channelTag.contains("Channel")) {
                data.channelGunIds.put(new OwnerChannelKey(channelTag.getUUID(OWNER_KEY), channelTag.getString("Channel")), channelTag.getUUID(GUN_KEY));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<PairKey, PairRecord> entry : this.pairs.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(OWNER_KEY, entry.getKey().owner());
            entryTag.putString(DIMENSION_KEY, entry.getKey().dimension().toString());
            if (entry.getKey().gunId() != null) {
                entryTag.putUUID(GUN_KEY, entry.getKey().gunId());
            }
            writePair(entryTag, entry.getValue());
            entries.add(entryTag);
        }
        for (Map.Entry<UUID, PairRecord> entry : this.legacyPairs.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(OWNER_KEY, entry.getKey());
            writePair(entryTag, entry.getValue());
            entries.add(entryTag);
        }
        ListTag channels = new ListTag();
        for (Map.Entry<OwnerChannelKey, UUID> entry : this.channelGunIds.entrySet()) {
            CompoundTag channelTag = new CompoundTag();
            channelTag.putUUID(OWNER_KEY, entry.getKey().owner());
            channelTag.putString("Channel", entry.getKey().channelName());
            channelTag.putUUID(GUN_KEY, entry.getValue());
            channels.add(channelTag);
        }
        tag.put(ENTRIES_KEY, entries);
        tag.put(CHANNELS_KEY, channels);
        return tag;
    }

    private static void writePair(CompoundTag tag, PairRecord record) {
        if (record.bluePortalId() != null) {
            tag.putUUID(BLUE_KEY, record.bluePortalId());
        }
        if (record.orangePortalId() != null) {
            tag.putUUID(ORANGE_KEY, record.orangePortalId());
        }
    }

    private static PortalGunSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PortalGunSavedData::create, PortalGunSavedData::load, null),
                ID
        );
    }

    public static Optional<UUID> getPortalId(MinecraftServer server, UUID owner, UUID gunId, String channelName, PortalGunPortalEntity.PortalSide side, ResourceLocation dimension) {
        PortalGunSavedData data = get(server);
        UUID resolvedGunId = data.resolveChannelGunId(owner, channelName, gunId);
        data.migrateLegacyPair(server, owner);
        data.adoptLegacyPairs(server, owner, resolvedGunId, channelName);
        PairRecord record = data.pairs.get(new PairKey(owner, dimension, resolvedGunId));
        if (record == null) {
            return Optional.empty();
        }
        data.synchronizeLoadedPortal(server, owner, dimension, record.bluePortalId(), resolvedGunId, channelName);
        data.synchronizeLoadedPortal(server, owner, dimension, record.orangePortalId(), resolvedGunId, channelName);
        return Optional.ofNullable(side == PortalGunPortalEntity.PortalSide.BLUE ? record.bluePortalId() : record.orangePortalId());
    }

    public static Optional<UUID> getPortalId(MinecraftServer server, UUID owner, UUID gunId, PortalGunPortalEntity.PortalSide side, ResourceLocation dimension) {
        return getPortalId(server, owner, gunId, String.valueOf(gunId), side, dimension);
    }

    public static Optional<UUID> getGunIdForPortal(MinecraftServer server, UUID owner, ResourceLocation dimension, UUID portalId) {
        PortalGunSavedData data = get(server);
        data.migrateLegacyPair(server, owner);
        for (Map.Entry<PairKey, PairRecord> entry : data.pairs.entrySet()) {
            PairKey key = entry.getKey();
            if (key.owner().equals(owner) && key.dimension().equals(dimension) && key.gunId() != null
                    && (portalId.equals(entry.getValue().bluePortalId()) || portalId.equals(entry.getValue().orangePortalId()))) {
                return Optional.of(key.gunId());
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getChannelNameForPortal(MinecraftServer server, UUID owner, ResourceLocation dimension, UUID portalId) {
        PortalGunSavedData data = get(server);
        for (Map.Entry<PairKey, PairRecord> entry : data.pairs.entrySet()) {
            PairKey key = entry.getKey();
            if (!key.owner().equals(owner) || !key.dimension().equals(dimension)
                    || (!portalId.equals(entry.getValue().bluePortalId()) && !portalId.equals(entry.getValue().orangePortalId()))) {
                continue;
            }
            return data.channelGunIds.entrySet().stream()
                    .filter(channel -> channel.getKey().owner().equals(owner) && channel.getValue().equals(key.gunId()))
                    .map(channel -> channel.getKey().channelName())
                    .findFirst();
        }
        return Optional.empty();
    }

    public static boolean isRegistered(ServerLevel level, UUID owner, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        return isRegistered(level, owner, gunId, String.valueOf(gunId), side, portalId);
    }

    public static boolean isRegistered(ServerLevel level, UUID owner, UUID gunId, String channelName, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        return getPortalId(level.getServer(), owner, gunId, channelName, side, level.dimension().location()).map(portalId::equals).orElse(false);
    }

    public static void setPortal(ServerLevel level, UUID owner, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        setPortal(level, owner, gunId, String.valueOf(gunId), side, portalId);
    }

    public static void setPortal(ServerLevel level, UUID owner, UUID gunId, String channelName, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        PortalGunSavedData data = get(level.getServer());
        ResourceLocation dimension = level.dimension().location();
        UUID resolvedGunId = data.resolveChannelGunId(owner, channelName, gunId);
        data.migrateLegacyPair(level.getServer(), owner);
        data.adoptLegacyPairs(level.getServer(), owner, resolvedGunId, channelName);
        PairKey key = new PairKey(owner, dimension, resolvedGunId);
        PairRecord current = data.pairs.getOrDefault(key, new PairRecord(null, null));
        PairRecord updated = side == PortalGunPortalEntity.PortalSide.BLUE
                ? new PairRecord(portalId, current.orangePortalId())
                : new PairRecord(current.bluePortalId(), portalId);
        if (updated.bluePortalId() == null && updated.orangePortalId() == null) {
            data.pairs.remove(key);
        } else {
            data.pairs.put(key, updated);
        }
        data.setDirty();
    }

    public static void clearPortal(MinecraftServer server, UUID owner, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId, ResourceLocation dimension) {
        clearPortal(server, owner, gunId, String.valueOf(gunId), side, portalId, dimension);
    }

    public static void clearPortal(MinecraftServer server, UUID owner, UUID gunId, String channelName, PortalGunPortalEntity.PortalSide side, UUID portalId, ResourceLocation dimension) {
        PortalGunSavedData data = get(server);
        UUID resolvedGunId = data.resolveChannelGunId(owner, channelName, gunId);
        data.migrateLegacyPair(server, owner);
        data.adoptLegacyPairs(server, owner, resolvedGunId, channelName);
        PairKey key = new PairKey(owner, dimension, resolvedGunId);
        PairRecord current = data.pairs.get(key);
        if (current == null) {
            return;
        }
        UUID blue = current.bluePortalId();
        UUID orange = current.orangePortalId();
        if (side == PortalGunPortalEntity.PortalSide.BLUE && portalId.equals(blue)) {
            blue = null;
        }
        if (side == PortalGunPortalEntity.PortalSide.ORANGE && portalId.equals(orange)) {
            orange = null;
        }
        if (blue == null && orange == null) {
            data.pairs.remove(key);
        } else {
            data.pairs.put(key, new PairRecord(blue, orange));
        }
        data.setDirty();
    }

    public static void clearAllPortals(ServerLevel level, UUID owner, UUID gunId) {
        clearAllPortals(level, owner, gunId, String.valueOf(gunId));
    }

    public static void clearAllPortals(ServerLevel level, UUID owner, UUID gunId, String channelName) {
        MinecraftServer server = level.getServer();
        PortalGunSavedData data = get(server);
        UUID resolvedGunId = data.resolveChannelGunId(owner, channelName, gunId);
        data.migrateLegacyPair(server, owner);
        data.adoptLegacyPairs(server, owner, resolvedGunId, channelName);
        Set<UUID> portalIds = new HashSet<>();
        PairRecord legacy = gunId == null ? data.legacyPairs.remove(owner) : null;
        if (legacy != null) {
            addPortalIds(portalIds, legacy);
        }
        data.pairs.entrySet().removeIf(entry -> {
            if (!entry.getKey().owner().equals(owner) || !entry.getKey().dimension().equals(level.dimension().location())
                    || !Objects.equals(entry.getKey().gunId(), resolvedGunId)) {
                return false;
            }
            addPortalIds(portalIds, entry.getValue());
            return true;
        });
        data.setDirty();
        for (UUID portalId : portalIds) {
            discardIfLoaded(server, portalId);
        }
    }

    public static void clearPortalSide(ServerLevel level, UUID owner, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        clearPortalSide(level, owner, gunId, String.valueOf(gunId), side);
    }

    public static void clearPortalSide(ServerLevel level, UUID owner, UUID gunId, String channelName, PortalGunPortalEntity.PortalSide side) {
        MinecraftServer server = level.getServer();
        PortalGunSavedData data = get(server);
        UUID resolvedGunId = data.resolveChannelGunId(owner, channelName, gunId);
        data.migrateLegacyPair(server, owner);
        data.adoptLegacyPairs(server, owner, resolvedGunId, channelName);
        Set<UUID> portalIds = new HashSet<>();
        for (Map.Entry<PairKey, PairRecord> entry : new HashMap<>(data.pairs).entrySet()) {
            PairKey key = entry.getKey();
            if (!key.owner().equals(owner) || !key.dimension().equals(level.dimension().location()) || !Objects.equals(key.gunId(), resolvedGunId)) {
                continue;
            }
            PairRecord pair = entry.getValue();
            UUID removed = side == PortalGunPortalEntity.PortalSide.BLUE ? pair.bluePortalId() : pair.orangePortalId();
            if (removed == null) {
                continue;
            }
            portalIds.add(removed);
            PairRecord remaining = side == PortalGunPortalEntity.PortalSide.BLUE
                    ? new PairRecord(null, pair.orangePortalId())
                    : new PairRecord(pair.bluePortalId(), null);
            if (remaining.bluePortalId() == null && remaining.orangePortalId() == null) {
                data.pairs.remove(key);
            } else {
                data.pairs.put(key, remaining);
            }
        }
        data.setDirty();
        for (UUID portalId : portalIds) {
            discardIfLoaded(server, portalId);
        }
    }

    public static PortalGunPortalEntity findLoadedPortal(ServerLevel level, UUID owner, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        Optional<UUID> portalId = getPortalId(level.getServer(), owner, gunId, side, level.dimension().location());
        if (portalId.isEmpty()) {
            return null;
        }
        Entity entity = level.getEntity(portalId.get());
        return entity instanceof PortalGunPortalEntity portal && !portal.isRemoved() ? portal : null;
    }

    public static PortalGunPortalEntity findLoadedPortal(ServerLevel level, UUID owner, UUID gunId, String channelName, PortalGunPortalEntity.PortalSide side) {
        Optional<UUID> portalId = getPortalId(level.getServer(), owner, gunId, channelName, side, level.dimension().location());
        if (portalId.isEmpty()) {
            return null;
        }
        Entity entity = level.getEntity(portalId.get());
        return entity instanceof PortalGunPortalEntity portal && !portal.isRemoved() ? portal : null;
    }

    private UUID resolveChannelGunId(UUID owner, String channelName, UUID gunId) {
        if (channelName == null || channelName.isEmpty()) {
            return gunId;
        }
        OwnerChannelKey key = new OwnerChannelKey(owner, channelName);
        UUID existing = this.channelGunIds.get(key);
        if (existing != null) {
            return existing;
        }
        UUID channelGunId = UUID.nameUUIDFromBytes((owner + "\u0000" + channelName).getBytes(StandardCharsets.UTF_8));
        this.channelGunIds.put(key, channelGunId);
        if (gunId != null && !gunId.equals(channelGunId)) {
            this.migrateGunPairs(owner, gunId, channelGunId);
        }
        this.setDirty();
        return channelGunId;
    }

    private void migrateGunPairs(UUID owner, UUID previousGunId, UUID channelGunId) {
        for (Map.Entry<PairKey, PairRecord> entry : new HashMap<>(this.pairs).entrySet()) {
            PairKey previousKey = entry.getKey();
            if (!previousKey.owner().equals(owner) || !previousGunId.equals(previousKey.gunId())) {
                continue;
            }
            PairKey channelKey = new PairKey(owner, previousKey.dimension(), channelGunId);
            PairRecord existing = this.pairs.get(channelKey);
            PairRecord migrated = mergePairs(existing, entry.getValue());
            this.pairs.remove(previousKey);
            this.pairs.put(channelKey, migrated);
        }
    }

    private static PairRecord mergePairs(PairRecord preferred, PairRecord fallback) {
        if (preferred == null) {
            return fallback;
        }
        return new PairRecord(
                preferred.bluePortalId() == null ? fallback.bluePortalId() : preferred.bluePortalId(),
                preferred.orangePortalId() == null ? fallback.orangePortalId() : preferred.orangePortalId()
        );
    }

    private void migrateLegacyPair(MinecraftServer server, UUID owner) {
        PairRecord legacy = this.legacyPairs.get(owner);
        if (legacy == null) {
            return;
        }
        ServerLevel blueLevel = findPortalLevel(server, legacy.bluePortalId());
        ServerLevel orangeLevel = findPortalLevel(server, legacy.orangePortalId());
        UUID legacyBlue = legacy.bluePortalId();
        UUID legacyOrange = legacy.orangePortalId();
        if (blueLevel != null) {
            Entity blueEntity = blueLevel.getEntity(legacyBlue);
            UUID blueGunId = blueEntity instanceof PortalGunPortalEntity portal ? portal.getGunId() : null;
            PairKey key = new PairKey(owner, blueLevel.dimension().location(), blueGunId);
            this.pairs.put(key, withPortal(this.pairs.get(key), PortalGunPortalEntity.PortalSide.BLUE, legacyBlue));
            legacyBlue = null;
        }
        if (orangeLevel != null) {
            Entity orangeEntity = orangeLevel.getEntity(legacyOrange);
            UUID orangeGunId = orangeEntity instanceof PortalGunPortalEntity portal ? portal.getGunId() : null;
            PairKey key = new PairKey(owner, orangeLevel.dimension().location(), orangeGunId);
            this.pairs.put(key, withPortal(this.pairs.get(key), PortalGunPortalEntity.PortalSide.ORANGE, legacyOrange));
            legacyOrange = null;
        }
        if (!Objects.equals(legacy.bluePortalId(), legacyBlue) || !Objects.equals(legacy.orangePortalId(), legacyOrange)) {
            if (legacyBlue == null && legacyOrange == null) {
                this.legacyPairs.remove(owner);
            } else {
                this.legacyPairs.put(owner, new PairRecord(legacyBlue, legacyOrange));
            }
            this.setDirty();
        }
    }

    private void adoptLegacyPairs(MinecraftServer server, UUID owner, UUID gunId, String channelName) {
        if (gunId == null) {
            return;
        }
        boolean changed = false;
        for (Map.Entry<PairKey, PairRecord> entry : new HashMap<>(this.pairs).entrySet()) {
            PairKey legacyKey = entry.getKey();
            if (!legacyKey.owner().equals(owner) || legacyKey.gunId() != null) {
                continue;
            }
            PairKey ownedKey = new PairKey(owner, legacyKey.dimension(), gunId);
            if (this.pairs.containsKey(ownedKey)) {
                continue;
            }
            this.pairs.remove(legacyKey);
            this.pairs.put(ownedKey, entry.getValue());
            synchronizeLoadedPortal(server, owner, legacyKey.dimension(), entry.getValue().bluePortalId(), gunId, channelName);
            synchronizeLoadedPortal(server, owner, legacyKey.dimension(), entry.getValue().orangePortalId(), gunId, channelName);
            changed = true;
        }
        if (changed) {
            this.setDirty();
        }
    }

    private static void synchronizeLoadedPortal(MinecraftServer server, UUID owner, ResourceLocation dimension, UUID portalId, UUID gunId, String channelName) {
        if (portalId == null) {
            return;
        }
        ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimension));
        if (level == null || !(level.getEntity(portalId) instanceof PortalGunPortalEntity portal) || !owner.equals(portal.getOwnerId())) {
            return;
        }
        portal.adoptChannelIdentity(gunId, channelName);
    }

    private static PairRecord withPortal(PairRecord current, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        PairRecord pair = current == null ? new PairRecord(null, null) : current;
        return side == PortalGunPortalEntity.PortalSide.BLUE
                ? new PairRecord(pair.bluePortalId() == null ? portalId : pair.bluePortalId(), pair.orangePortalId())
                : new PairRecord(pair.bluePortalId(), pair.orangePortalId() == null ? portalId : pair.orangePortalId());
    }

    private static ServerLevel findPortalLevel(MinecraftServer server, UUID portalId) {
        if (portalId == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(portalId) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
                return level;
            }
        }
        return null;
    }

    private static void addPortalIds(Set<UUID> portalIds, PairRecord pair) {
        if (pair.bluePortalId() != null) {
            portalIds.add(pair.bluePortalId());
        }
        if (pair.orangePortalId() != null) {
            portalIds.add(pair.orangePortalId());
        }
    }

    private static void discardIfLoaded(MinecraftServer server, UUID portalId) {
        ServerLevel level = findPortalLevel(server, portalId);
        if (level != null && level.getEntity(portalId) instanceof PortalGunPortalEntity portal) {
            portal.discard();
        }
    }

    private record PairKey(UUID owner, ResourceLocation dimension, UUID gunId) {
    }

    private record OwnerChannelKey(UUID owner, String channelName) {
    }

    private record PairRecord(UUID bluePortalId, UUID orangePortalId) {
    }
}
