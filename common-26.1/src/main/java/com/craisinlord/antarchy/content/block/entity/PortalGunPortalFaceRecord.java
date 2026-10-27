package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunVariant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public record PortalGunPortalFaceRecord(
        UUID ownerId,
        UUID gunId,
        PortalGunVariant variant,
        UUID portalId,
        UUID linkedPortalId,
        PortalGunPortalEntity.PortalSide side,
        Direction face,
        Direction upAxis,
        BlockPos masterPos,
        BlockPos basePos,
        List<BlockPos> portalSpots,
        Set<BlockPos> compensatedSpots,
        int pairTime,
        int portalWidth,
        int portalHeight,
        boolean master
) {
    public PortalGunPortalFaceRecord {
        masterPos = masterPos.immutable();
        basePos = basePos.immutable();
        portalSpots = portalSpots.stream().map(BlockPos::immutable).toList();
        compensatedSpots = Set.copyOf(compensatedSpots);
    }

    public PortalGunPlacement placement() {
        return PortalGunPlacement.fromStored(this.face, this.upAxis, this.masterPos, this.basePos, this.portalSpots.toArray(BlockPos[]::new), this.compensatedSpots, this.portalWidth, this.portalHeight);
    }

    public PortalGunPortalFaceRecord withPair(UUID linkedPortalId, int pairTime) {
        return new PortalGunPortalFaceRecord(this.ownerId, this.gunId, this.variant, this.portalId, linkedPortalId, this.side, this.face, this.upAxis, this.masterPos, this.basePos, this.portalSpots, this.compensatedSpots, pairTime, this.portalWidth, this.portalHeight, this.master);
    }

    public PortalGunPortalFaceRecord withMaster(boolean master) {
        return new PortalGunPortalFaceRecord(this.ownerId, this.gunId, this.variant, this.portalId, this.linkedPortalId, this.side, this.face, this.upAxis, this.masterPos, this.basePos, this.portalSpots, this.compensatedSpots, this.pairTime, this.portalWidth, this.portalHeight, master);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.store("OwnerId", UUIDUtil.CODEC, this.ownerId);
        if (this.gunId != null) {
            tag.store("GunId", UUIDUtil.CODEC, this.gunId);
        }
        tag.putInt("Variant", this.variant.id());
        tag.store("PortalId", UUIDUtil.CODEC, this.portalId);
        if (this.linkedPortalId != null) {
            tag.store("LinkedPortalId", UUIDUtil.CODEC, this.linkedPortalId);
        }
        tag.putInt("Side", this.side.ordinal());
        tag.putInt("Face", this.face.get3DDataValue());
        tag.putInt("UpAxis", this.upAxis.get3DDataValue());
        putPos(tag, "Master", this.masterPos);
        putPos(tag, "Base", this.basePos);
        tag.putInt("PairTime", this.pairTime);
        tag.putInt("PortalWidth", this.portalWidth);
        tag.putInt("PortalHeight", this.portalHeight);
        tag.putBoolean("MasterRole", this.master);
        ListTag spots = new ListTag();
        for (BlockPos pos : this.portalSpots) {
            spots.add(savePos(pos));
        }
        tag.put("PortalSpots", spots);
        ListTag compensated = new ListTag();
        for (BlockPos pos : this.compensatedSpots) {
            compensated.add(savePos(pos));
        }
        tag.put("CompensatedSpots", compensated);
        return tag;
    }

    public static PortalGunPortalFaceRecord load(CompoundTag tag) {
        UUID ownerId = tag.read("OwnerId", UUIDUtil.CODEC).orElse(null);
        UUID portalId = tag.read("PortalId", UUIDUtil.CODEC).orElse(null);
        if (ownerId == null || portalId == null) {
            return null;
        }
        ListTag spotsTag = tag.getListOrEmpty("PortalSpots");
        List<BlockPos> spots = new java.util.ArrayList<>(spotsTag.size());
        for (int i = 0; i < spotsTag.size(); i++) {
            spots.add(loadPos(spotsTag.getCompoundOrEmpty(i)));
        }
        ListTag compensatedTag = tag.getListOrEmpty("CompensatedSpots");
        Set<BlockPos> compensated = new HashSet<>();
        for (int i = 0; i < compensatedTag.size(); i++) {
            compensated.add(loadPos(compensatedTag.getCompoundOrEmpty(i)));
        }
        PortalGunPortalEntity.PortalSide side = tag.getIntOr("Side", 0) == PortalGunPortalEntity.PortalSide.ORANGE.ordinal()
                ? PortalGunPortalEntity.PortalSide.ORANGE
                : PortalGunPortalEntity.PortalSide.BLUE;
        int width = net.minecraft.util.Mth.clamp(tag.getIntOr("PortalWidth", 0), 1, 16);
        int height = net.minecraft.util.Mth.clamp(tag.getIntOr("PortalHeight", 0), 2, 16);
        return new PortalGunPortalFaceRecord(
                ownerId,
                tag.read("GunId", UUIDUtil.CODEC).orElse(null),
                PortalGunVariant.byId(tag.getIntOr("Variant", 0)),
                portalId,
                tag.read("LinkedPortalId", UUIDUtil.CODEC).orElse(null),
                side,
                Direction.from3DDataValue(tag.getIntOr("Face", 0)),
                Direction.from3DDataValue(tag.getIntOr("UpAxis", 0)),
                getPos(tag, "Master"),
                getPos(tag, "Base"),
                spots,
                compensated,
                tag.getIntOr("PairTime", 0),
                width,
                height,
                tag.getBooleanOr("MasterRole", false)
        );
    }

    private static void putPos(CompoundTag tag, String key, BlockPos pos) {
        CompoundTag value = new CompoundTag();
        value.putInt("X", pos.getX());
        value.putInt("Y", pos.getY());
        value.putInt("Z", pos.getZ());
        tag.put(key, value);
    }

    private static BlockPos getPos(CompoundTag tag, String key) {
        CompoundTag value = tag.getCompoundOrEmpty(key);
        return new BlockPos(value.getIntOr("X", 0), value.getIntOr("Y", 0), value.getIntOr("Z", 0));
    }

    private static CompoundTag savePos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    private static BlockPos loadPos(CompoundTag tag) {
        return new BlockPos(tag.getIntOr("X", 0), tag.getIntOr("Y", 0), tag.getIntOr("Z", 0));
    }
}
