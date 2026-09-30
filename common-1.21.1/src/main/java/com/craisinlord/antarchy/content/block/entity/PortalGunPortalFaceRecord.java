package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public record PortalGunPortalFaceRecord(
        UUID ownerId,
        String ownerIdentity,
        UUID gunId,
        String channelName,
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
        return new PortalGunPortalFaceRecord(this.ownerId, this.ownerIdentity, this.gunId, this.channelName, this.portalId, linkedPortalId, this.side, this.face, this.upAxis, this.masterPos, this.basePos, this.portalSpots, this.compensatedSpots, pairTime, this.portalWidth, this.portalHeight, this.master);
    }

    public PortalGunPortalFaceRecord withMaster(boolean master) {
        return new PortalGunPortalFaceRecord(this.ownerId, this.ownerIdentity, this.gunId, this.channelName, this.portalId, this.linkedPortalId, this.side, this.face, this.upAxis, this.masterPos, this.basePos, this.portalSpots, this.compensatedSpots, this.pairTime, this.portalWidth, this.portalHeight, master);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("OwnerId", this.ownerId);
        tag.putString("OwnerIdentity", this.ownerIdentity == null ? "" : this.ownerIdentity);
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
        tag.putString("ChannelName", this.channelName == null ? "" : this.channelName);
        tag.putUUID("PortalId", this.portalId);
        if (this.linkedPortalId != null) {
            tag.putUUID("LinkedPortalId", this.linkedPortalId);
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
        if (!tag.hasUUID("OwnerId") || !tag.hasUUID("PortalId")) {
            return null;
        }
        ListTag spotsTag = tag.getList("PortalSpots", Tag.TAG_COMPOUND);
        List<BlockPos> spots = new java.util.ArrayList<>(spotsTag.size());
        for (int i = 0; i < spotsTag.size(); i++) {
            spots.add(loadPos(spotsTag.getCompound(i)));
        }
        ListTag compensatedTag = tag.getList("CompensatedSpots", Tag.TAG_COMPOUND);
        Set<BlockPos> compensated = new HashSet<>();
        for (int i = 0; i < compensatedTag.size(); i++) {
            compensated.add(loadPos(compensatedTag.getCompound(i)));
        }
        PortalGunPortalEntity.PortalSide side = tag.getInt("Side") == PortalGunPortalEntity.PortalSide.ORANGE.ordinal()
                ? PortalGunPortalEntity.PortalSide.ORANGE
                : PortalGunPortalEntity.PortalSide.BLUE;
        int width = net.minecraft.util.Mth.clamp(tag.getInt("PortalWidth"), 1, 16);
        int height = net.minecraft.util.Mth.clamp(tag.getInt("PortalHeight"), 2, 16);
        return new PortalGunPortalFaceRecord(
                tag.getUUID("OwnerId"),
                tag.getString("OwnerIdentity"),
                tag.hasUUID("GunId") ? tag.getUUID("GunId") : null,
                tag.getString("ChannelName"),
                tag.getUUID("PortalId"),
                tag.hasUUID("LinkedPortalId") ? tag.getUUID("LinkedPortalId") : null,
                side,
                Direction.from3DDataValue(tag.getInt("Face")),
                Direction.from3DDataValue(tag.getInt("UpAxis")),
                getPos(tag, "Master"),
                getPos(tag, "Base"),
                spots,
                compensated,
                tag.getInt("PairTime"),
                width,
                height,
                tag.getBoolean("MasterRole")
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
        CompoundTag value = tag.getCompound(key);
        return new BlockPos(value.getInt("X"), value.getInt("Y"), value.getInt("Z"));
    }

    private static CompoundTag savePos(BlockPos pos) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("X", pos.getX());
        tag.putInt("Y", pos.getY());
        tag.putInt("Z", pos.getZ());
        return tag;
    }

    private static BlockPos loadPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
    }
}
