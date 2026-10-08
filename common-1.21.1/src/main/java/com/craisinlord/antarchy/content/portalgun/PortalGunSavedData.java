package com.craisinlord.antarchy.content.portalgun;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private static final String ID = "antarchy_portal_gun_pairs_v2";
    private static final String ENTRIES_KEY = "Entries";
    private static final String DIMENSION_KEY = "Dimension";
    private static final String GUN_KEY = "Gun";
    private static final String BLUE_KEY = "Blue";
    private static final String ORANGE_KEY = "Orange";
    private final Map<PairKey, PairRecord> pairs = new HashMap<>();

    public static PortalGunSavedData create() {
        return new PortalGunSavedData();
    }

    public static PortalGunSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        PortalGunSavedData data = new PortalGunSavedData();
        for (net.minecraft.nbt.Tag rawEntry : tag.getList(ENTRIES_KEY, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag entryTag = (CompoundTag) rawEntry;
            ResourceLocation dimension = ResourceLocation.tryParse(entryTag.getString(DIMENSION_KEY));
            if (dimension == null || !entryTag.hasUUID(GUN_KEY)) {
                continue;
            }
            UUID blue = entryTag.hasUUID(BLUE_KEY) ? entryTag.getUUID(BLUE_KEY) : null;
            UUID orange = entryTag.hasUUID(ORANGE_KEY) ? entryTag.getUUID(ORANGE_KEY) : null;
            data.pairs.put(new PairKey(dimension, entryTag.getUUID(GUN_KEY)), new PairRecord(blue, orange));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<PairKey, PairRecord> entry : this.pairs.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(DIMENSION_KEY, entry.getKey().dimension().toString());
            entryTag.putUUID(GUN_KEY, entry.getKey().gunId());
            if (entry.getValue().bluePortalId() != null) {
                entryTag.putUUID(BLUE_KEY, entry.getValue().bluePortalId());
            }
            if (entry.getValue().orangePortalId() != null) {
                entryTag.putUUID(ORANGE_KEY, entry.getValue().orangePortalId());
            }
            entries.add(entryTag);
        }
        tag.put(ENTRIES_KEY, entries);
        return tag;
    }

    private static PortalGunSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PortalGunSavedData::create, PortalGunSavedData::load, null),
                ID
        );
    }

    public static Optional<UUID> getPortalId(MinecraftServer server, UUID gunId, PortalGunPortalEntity.PortalSide side, ResourceLocation dimension) {
        if (gunId == null) {
            return Optional.empty();
        }
        PairRecord record = get(server).pairs.get(new PairKey(dimension, gunId));
        if (record == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(side == PortalGunPortalEntity.PortalSide.BLUE ? record.bluePortalId() : record.orangePortalId());
    }

    public static boolean isRegistered(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        return portalId != null && getPortalId(level.getServer(), gunId, side, level.dimension().location()).map(portalId::equals).orElse(false);
    }

    public static void setPortal(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        if (gunId == null) {
            return;
        }
        PortalGunSavedData data = get(level.getServer());
        PairKey key = new PairKey(level.dimension().location(), gunId);
        PairRecord current = data.pairs.getOrDefault(key, new PairRecord(null, null));
        data.put(key, side == PortalGunPortalEntity.PortalSide.BLUE
                ? new PairRecord(portalId, current.orangePortalId())
                : new PairRecord(current.bluePortalId(), portalId));
    }

    public static void clearPortal(MinecraftServer server, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId, ResourceLocation dimension) {
        if (gunId == null) {
            return;
        }
        PortalGunSavedData data = get(server);
        PairKey key = new PairKey(dimension, gunId);
        PairRecord current = data.pairs.get(key);
        if (current == null) {
            return;
        }
        UUID blue = side == PortalGunPortalEntity.PortalSide.BLUE && portalId.equals(current.bluePortalId()) ? null : current.bluePortalId();
        UUID orange = side == PortalGunPortalEntity.PortalSide.ORANGE && portalId.equals(current.orangePortalId()) ? null : current.orangePortalId();
        data.put(key, new PairRecord(blue, orange));
    }

    public static void clearAllPortals(ServerLevel level, UUID gunId) {
        if (gunId == null) {
            return;
        }
        MinecraftServer server = level.getServer();
        PortalGunSavedData data = get(server);
        Set<UUID> portalIds = new HashSet<>();
        data.pairs.entrySet().removeIf(entry -> {
            if (!entry.getKey().gunId().equals(gunId)) {
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

    public static void clearPortalSide(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        if (gunId == null) {
            return;
        }
        MinecraftServer server = level.getServer();
        PortalGunSavedData data = get(server);
        Set<UUID> portalIds = new HashSet<>();
        for (Map.Entry<PairKey, PairRecord> entry : new HashMap<>(data.pairs).entrySet()) {
            if (!entry.getKey().gunId().equals(gunId)) {
                continue;
            }
            PairRecord pair = entry.getValue();
            UUID removed = side == PortalGunPortalEntity.PortalSide.BLUE ? pair.bluePortalId() : pair.orangePortalId();
            if (removed == null) {
                continue;
            }
            portalIds.add(removed);
            data.put(entry.getKey(), side == PortalGunPortalEntity.PortalSide.BLUE
                    ? new PairRecord(null, pair.orangePortalId())
                    : new PairRecord(pair.bluePortalId(), null));
        }
        for (UUID portalId : portalIds) {
            discardIfLoaded(server, portalId);
        }
    }

    public static PortalGunPortalEntity findLoadedPortal(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        Optional<UUID> portalId = getPortalId(level.getServer(), gunId, side, level.dimension().location());
        if (portalId.isEmpty()) {
            return null;
        }
        Entity entity = level.getEntity(portalId.get());
        return entity instanceof PortalGunPortalEntity portal && !portal.isRemoved() ? portal : null;
    }

    private void put(PairKey key, PairRecord record) {
        if (record.bluePortalId() == null && record.orangePortalId() == null) {
            this.pairs.remove(key);
        } else {
            this.pairs.put(key, record);
        }
        this.setDirty();
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
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(portalId) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
                portal.discard();
                return;
            }
        }
    }

    private record PairKey(ResourceLocation dimension, UUID gunId) {
    }

    private record PairRecord(UUID bluePortalId, UUID orangePortalId) {
    }
}
