package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface PortalGunPortalCellAccess {
    Map<Direction, PortalGunPortalFaceRecord> portalGun$getFaceRecords();

    default PortalGunPortalFaceRecord portalGun$getFaceRecord(Direction face) {
        return this.portalGun$getFaceRecords().get(face);
    }

    default PortalGunPortalFaceRecord portalGun$getFaceRecord(Direction face, UUID portalId) {
        PortalGunPortalFaceRecord record = this.portalGun$getFaceRecord(face);
        return record != null && record.portalId().equals(portalId) ? record : null;
    }

    default boolean portalGun$putFaceRecord(PortalGunPortalFaceRecord record) {
        PortalGunPortalFaceRecord existing = this.portalGun$getFaceRecord(record.face());
        if (existing != null
                && (!existing.ownerId().equals(record.ownerId())
                || !java.util.Objects.equals(existing.channelName(), record.channelName())
                || existing.side() != record.side())) {
            return false;
        }
        this.portalGun$getFaceRecords().put(record.face(), record);
        if (this instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
        return true;
    }

    default PortalGunPortalFaceRecord portalGun$removeFaceRecord(Direction face, UUID portalId) {
        PortalGunPortalFaceRecord existing = this.portalGun$getFaceRecord(face, portalId);
        if (existing == null) {
            return null;
        }
        PortalGunPortalFaceRecord removed = this.portalGun$getFaceRecords().remove(face);
        if (this instanceof net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            blockEntity.setChanged();
        }
        return removed;
    }

    default void portalGun$updateFacePair(UUID portalId, UUID linkedPortalId, int pairTime) {
        for (PortalGunPortalFaceRecord record : java.util.List.copyOf(this.portalGun$getFaceRecords().values())) {
            if (record.portalId().equals(portalId)) {
                this.portalGun$putFaceRecord(record.withPair(linkedPortalId, pairTime));
            }
        }
    }

    default void portalGun$restoreMasterFaces(ServerLevel level, BlockPos pos) {
        for (PortalGunPortalFaceRecord record : java.util.List.copyOf(this.portalGun$getFaceRecords().values())) {
            if (record.ownerId() == null || record.portalId() == null) {
                continue;
            }
            if (!PortalGunSavedData.isRegistered(level, record.ownerId(), record.gunId(), record.channelName(), record.side(), record.portalId())) {
                this.portalGun$removeFaceRecord(record.face(), record.portalId());
                continue;
            }
            if (!record.master() || !record.masterPos().equals(pos)) {
                continue;
            }
            boolean complete = !record.portalSpots().isEmpty() && record.portalSpots().getFirst().equals(record.masterPos());
            for (int i = 0; complete && i < record.portalSpots().size(); i++) {
                BlockPos spot = record.portalSpots().get(i);
                BlockPos support = spot.relative(record.face().getOpposite());
                if (!level.hasChunkAt(spot) || !level.hasChunkAt(support)
                        || !level.getBlockState(support).isFaceSturdy(level, support, record.face())
                        || !(level.getBlockEntity(spot) instanceof PortalGunPortalCellAccess cell)) {
                    complete = false;
                    continue;
                }
                PortalGunPortalFaceRecord footprintRecord = cell.portalGun$getFaceRecord(record.face(), record.portalId());
                complete = footprintRecord != null && footprintRecord.master() == (i == 0);
            }
            complete = complete && PortalGunPlacement.isRectangularFootprint(
                    record.portalSpots().toArray(BlockPos[]::new),
                    record.face(),
                    record.upAxis(),
                    record.portalWidth(),
                    record.portalHeight()
            );
            if (complete) {
                PortalGunPortalMasterBlockEntity.restorePortalEntity(level, record);
            }
        }
    }

    default void portalGun$saveFaceRecords(CompoundTag tag) {
        ListTag faces = new ListTag();
        for (PortalGunPortalFaceRecord record : this.portalGun$getFaceRecords().values()) {
            faces.add(record.save());
        }
        tag.put("PortalFaces", faces);
    }

    default void portalGun$loadFaceRecords(CompoundTag tag) {
        Map<Direction, PortalGunPortalFaceRecord> records = this.portalGun$getFaceRecords();
        records.clear();
        ListTag faces = tag.getList("PortalFaces", Tag.TAG_COMPOUND);
        for (int i = 0; i < faces.size(); i++) {
            PortalGunPortalFaceRecord record = PortalGunPortalFaceRecord.load(faces.getCompound(i));
            if (record != null) {
                records.put(record.face(), record);
            }
        }
    }
}
