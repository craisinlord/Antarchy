package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.util.LegacySavedDataFiles;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PortalGunSavedData extends SavedData {
    private static final String ID = "antarchy_portal_gun_pairs_v2";
    private static final Codec<PortalGunSavedData> CODEC = CompoundTag.CODEC.xmap(PortalGunSavedData::load, PortalGunSavedData::save);
    private static final SavedDataType<PortalGunSavedData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(Antarchy.MODID, ID), PortalGunSavedData::create, CODEC, null);
    private static final String ENTRIES_KEY = "Entries";
    private static final String DIMENSION_KEY = "Dimension";
    private static final String GUN_KEY = "Gun";
    private static final String BLUE_KEY = "Blue";
    private static final String ORANGE_KEY = "Orange";
    private static final String OWNER_KEY = "Owner";
    private final Map<PairKey, PairRecord> pairs = new HashMap<>();

    public static PortalGunSavedData create() {
        return new PortalGunSavedData();
    }

    public static PortalGunSavedData load(CompoundTag tag) {
        PortalGunSavedData data = new PortalGunSavedData();
        for (net.minecraft.nbt.Tag rawEntry : tag.getListOrEmpty(ENTRIES_KEY)) {
            if (!(rawEntry instanceof CompoundTag entryTag)) {
                continue;
            }
            Identifier dimension = Identifier.tryParse(entryTag.getStringOr(DIMENSION_KEY, ""));
            UUID gun = entryTag.read(GUN_KEY, UUIDUtil.CODEC).orElse(null);
            if (dimension == null || gun == null) {
                continue;
            }
            UUID blue = entryTag.read(BLUE_KEY, UUIDUtil.CODEC).orElse(null);
            UUID orange = entryTag.read(ORANGE_KEY, UUIDUtil.CODEC).orElse(null);
            UUID owner = entryTag.read(OWNER_KEY, UUIDUtil.CODEC).orElse(null);
            data.pairs.put(new PairKey(dimension, gun), new PairRecord(blue, orange, owner));
        }
        return data;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for (Map.Entry<PairKey, PairRecord> entry : this.pairs.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString(DIMENSION_KEY, entry.getKey().dimension().toString());
            entryTag.store(GUN_KEY, UUIDUtil.CODEC, entry.getKey().gunId());
            if (entry.getValue().bluePortalId() != null) {
                entryTag.store(BLUE_KEY, UUIDUtil.CODEC, entry.getValue().bluePortalId());
            }
            if (entry.getValue().orangePortalId() != null) {
                entryTag.store(ORANGE_KEY, UUIDUtil.CODEC, entry.getValue().orangePortalId());
            }
            if (entry.getValue().ownerId() != null) {
                entryTag.store(OWNER_KEY, UUIDUtil.CODEC, entry.getValue().ownerId());
            }
            entries.add(entryTag);
        }
        tag.put(ENTRIES_KEY, entries);
        return tag;
    }

    private static PortalGunSavedData get(MinecraftServer server) {
        return LegacySavedDataFiles.computeIfAbsent(server, TYPE, ID);
    }

    public static Optional<UUID> getPortalId(MinecraftServer server, UUID gunId, PortalGunPortalEntity.PortalSide side, Identifier dimension) {
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
        return portalId != null && getPortalId(level.getServer(), gunId, side, level.dimension().identifier()).map(portalId::equals).orElse(false);
    }

    public static void setPortal(ServerLevel level, UUID gunId, UUID ownerId, PortalGunPortalEntity.PortalSide side, UUID portalId) {
        if (gunId == null) {
            return;
        }
        PortalGunSavedData data = get(level.getServer());
        PairKey key = new PairKey(level.dimension().identifier(), gunId);
        PairRecord current = data.pairs.getOrDefault(key, new PairRecord(null, null, null));
        UUID owner = ownerId != null ? ownerId : current.ownerId();
        data.put(key, side == PortalGunPortalEntity.PortalSide.BLUE
                ? new PairRecord(portalId, current.orangePortalId(), owner)
                : new PairRecord(current.bluePortalId(), portalId, owner));
    }

    public static void clearPortal(MinecraftServer server, UUID gunId, PortalGunPortalEntity.PortalSide side, UUID portalId, Identifier dimension) {
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
        data.put(key, new PairRecord(blue, orange, current.ownerId()));
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
                    ? new PairRecord(null, pair.orangePortalId(), pair.ownerId())
                    : new PairRecord(pair.bluePortalId(), null, pair.ownerId()));
        }
        for (UUID portalId : portalIds) {
            discardIfLoaded(server, portalId);
        }
    }

    public static int clearOwnedBy(MinecraftServer server, Set<UUID> ownerIds) {
        return clearMatching(server, record -> record.ownerId() != null && ownerIds.contains(record.ownerId()));
    }

    public static int clearMissingGuns(ServerPlayer player) {
        PortalGunSavedData data = get(player.level().getServer());
        if (data.pairs.isEmpty()) {
            return 0;
        }

        Set<UUID> heldGunIds = new HashSet<>();
        collectGunIds(player.getInventory(), heldGunIds);

        UUID ownerId = player.getUUID();
        Set<UUID> removedPortalIds = new HashSet<>();
        Set<UUID> missingGunIds = new HashSet<>();
        boolean removed = data.pairs.entrySet().removeIf(entry -> {
            PairRecord record = entry.getValue();
            if (!ownerId.equals(record.ownerId()) || heldGunIds.contains(entry.getKey().gunId())) {
                return false;
            }
            missingGunIds.add(entry.getKey().gunId());
            addPortalIds(removedPortalIds, record);
            return true;
        });
        if (removed) {
            data.setDirty();
        }
        for (UUID portalId : removedPortalIds) {
            discardIfLoaded(player.level().getServer(), portalId);
        }
        return missingGunIds.size();
    }

    private static void collectGunIds(net.minecraft.world.entity.player.Inventory inventory, Set<UUID> gunIds) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.getItem() instanceof PortalGunItem) {
                UUID gunId = PortalGunItem.getGunId(stack);
                if (gunId != null) {
                    gunIds.add(gunId);
                }
            }
        }
    }

    public static int clearEverything(MinecraftServer server) {
        return clearMatching(server, record -> true);
    }

    private static int clearMatching(MinecraftServer server, java.util.function.Predicate<PairRecord> filter) {
        PortalGunSavedData data = get(server);
        Set<UUID> portalIds = new HashSet<>();
        boolean removed = data.pairs.values().removeIf(record -> {
            if (!filter.test(record)) {
                return false;
            }
            addPortalIds(portalIds, record);
            return true;
        });
        if (removed) {
            data.setDirty();
        }
        for (UUID portalId : portalIds) {
            discardIfLoaded(server, portalId);
        }
        return portalIds.size();
    }

    public static PortalGunPortalEntity findLoadedPortal(ServerLevel level, UUID gunId, PortalGunPortalEntity.PortalSide side) {
        Optional<UUID> portalId = getPortalId(level.getServer(), gunId, side, level.dimension().identifier());
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

    private record PairKey(Identifier dimension, UUID gunId) {
    }

    private record PairRecord(UUID bluePortalId, UUID orangePortalId, UUID ownerId) {
    }
}
