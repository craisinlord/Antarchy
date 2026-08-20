package com.craisinlord.antarchy.content.portalgun;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class PortalGunSavedData extends SavedData {
    private static final String ID = "antarchy_portal_gun_pairs";
    private static final String ENTRIES_KEY = "Entries";
    private static final String OWNER_KEY = "Owner";
    private static final String BLUE_KEY = "Blue";
    private static final String ORANGE_KEY = "Orange";
    private final Map<UUID, PairRecord> pairs = new HashMap<>();

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
            data.pairs.put(owner, new PairRecord(blue, orange));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map.Entry<UUID, PairRecord> entry : this.pairs.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID(OWNER_KEY, entry.getKey());
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

    public static Optional<UUID> getPortalId(MinecraftServer server, UUID owner, PortalGunPortalEntity.PortalSide side) {
        PairRecord record = get(server).pairs.get(owner);
        if (record == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(side == PortalGunPortalEntity.PortalSide.BLUE ? record.bluePortalId() : record.orangePortalId());
    }

    public static boolean isRegistered(MinecraftServer server, UUID owner, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        return getPortalId(server, owner, side).map(portalId::equals).orElse(false);
    }

    public static void setPortal(MinecraftServer server, UUID owner, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        PortalGunSavedData data = get(server);
        PairRecord current = data.pairs.getOrDefault(owner, new PairRecord(null, null));
        PairRecord updated = side == PortalGunPortalEntity.PortalSide.BLUE
                ? new PairRecord(portalId, current.orangePortalId())
                : new PairRecord(current.bluePortalId(), portalId);
        if (updated.bluePortalId() == null && updated.orangePortalId() == null) {
            data.pairs.remove(owner);
        } else {
            data.pairs.put(owner, updated);
        }
        data.setDirty();
    }

    public static void clearPortal(MinecraftServer server, UUID owner, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        PortalGunSavedData data = get(server);
        PairRecord current = data.pairs.get(owner);
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
            data.pairs.remove(owner);
        } else {
            data.pairs.put(owner, new PairRecord(blue, orange));
        }
        data.setDirty();
    }

    public static void clearAllPortals(MinecraftServer server, UUID owner) {
        PortalGunSavedData data = get(server);
        PairRecord current = data.pairs.remove(owner);
        data.setDirty();
        if (current == null) {
            return;
        }
        discardIfLoaded(server, current.bluePortalId());
        discardIfLoaded(server, current.orangePortalId());
    }

    private static void discardIfLoaded(MinecraftServer server, UUID portalId) {
        if (portalId == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(portalId) instanceof PortalGunPortalEntity portal) {
                portal.discard();
                return;
            }
        }
    }

    private record PairRecord(UUID bluePortalId, UUID orangePortalId) {
    }
}
