package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PortalGunPortalBaseBlockEntity extends BlockEntity implements PortalGunPortalCellAccess {
    private final Map<net.minecraft.core.Direction, PortalGunPortalFaceRecord> faceRecords = new EnumMap<>(net.minecraft.core.Direction.class);
    private UUID ownerId;
    private UUID portalId;
    private PortalGunPortalEntity.PortalSide side = PortalGunPortalEntity.PortalSide.BLUE;
    private BlockPos masterPos = BlockPos.ZERO;
    private UUID linkedPortalId;
    private int pairTime;

    public PortalGunPortalBaseBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<PortalGunPortalBaseBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
    }

    @Override
    public Map<net.minecraft.core.Direction, PortalGunPortalFaceRecord> portalGun$getFaceRecords() {
        return this.faceRecords;
    }

    public void configure(UUID ownerId, UUID portalId, PortalGunPortalEntity.PortalSide side, BlockPos masterPos) {
        this.ownerId = ownerId;
        this.portalId = portalId;
        this.side = side;
        this.masterPos = masterPos.immutable();
        this.setChanged();
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public UUID getPortalId() {
        return this.portalId;
    }

    public PortalGunPortalEntity.PortalSide getSide() {
        return this.side;
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    public UUID getLinkedPortalId() {
        return this.linkedPortalId;
    }

    public int getPairTime() {
        return this.pairTime;
    }

    public void updatePair(UUID linkedPortalId, int pairTime) {
        this.linkedPortalId = linkedPortalId;
        this.pairTime = pairTime;
        for (PortalGunPortalFaceRecord record : java.util.List.copyOf(this.faceRecords.values())) {
            if (record.portalId().equals(this.portalId)) {
                this.portalGun$putFaceRecord(record.withPair(linkedPortalId, pairTime));
            }
        }
        this.setChanged();
    }

    public boolean matches(UUID ownerId, UUID portalId, PortalGunPortalEntity.PortalSide side) {
        return ownerId != null && portalId != null && ownerId.equals(this.ownerId) && portalId.equals(this.portalId) && this.side == side;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        this.onBroken();
    }

    public void onBroken() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        java.util.Set<UUID> portalIds = new java.util.HashSet<>();
        if (this.portalId != null) {
            portalIds.add(this.portalId);
        }
        for (PortalGunPortalFaceRecord record : this.faceRecords.values()) {
            portalIds.add(record.portalId());
        }
        for (UUID id : portalIds) {
            if (serverLevel.getEntity(id) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
                portal.discard();
            }
        }
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, PortalGunPortalBaseBlockEntity blockEntity) {
        if (((level.getGameTime() + pos.asLong()) & 15L) != 0L) {
            return;
        }
        if (level.getBlockEntity(blockEntity.masterPos) instanceof PortalGunPortalMasterBlockEntity master
                && master.matches(blockEntity.ownerId, blockEntity.portalId, blockEntity.side)) {
            PortalGunPortalFaceRecord masterRecord = master.portalGun$getFaceRecord(master.getFacing(), blockEntity.portalId);
            if (masterRecord != null && blockEntity.portalGun$getFaceRecord(masterRecord.face(), masterRecord.portalId()) == null) {
                blockEntity.portalGun$putFaceRecord(masterRecord.withMaster(false));
            }
        }
        blockEntity.portalGun$restoreMasterFaces(level, pos);
        if (blockEntity.portalId != null
                && !(level.getEntity(blockEntity.portalId) instanceof PortalGunPortalEntity)
                && blockEntity.faceRecords.values().stream().anyMatch(record -> !record.portalId().equals(blockEntity.portalId))) {
            return;
        }
        if (!(level.getBlockEntity(blockEntity.masterPos) instanceof PortalGunPortalMasterBlockEntity master)
                || !master.matches(blockEntity.ownerId, blockEntity.portalId, blockEntity.side)
                || !master.containsPortalSpot(pos)) {
            level.removeBlock(pos, false);
            return;
        }
        if (!(level.getEntity(blockEntity.portalId) instanceof PortalGunPortalEntity portal)
                || portal.isRemoved()
                || !portal.containsPortalSpot(pos)
                || blockEntity.ownerId == null
                || !blockEntity.ownerId.equals(portal.getOwnerId())
                || blockEntity.side != portal.getPortalSide()) {
            level.removeBlock(pos, false);
            return;
        }
        UUID linkedPortalId = portal.getLinkedPortalId();
        if ((linkedPortalId == null && blockEntity.linkedPortalId != null)
                || (linkedPortalId != null && !linkedPortalId.equals(blockEntity.linkedPortalId))
                || blockEntity.pairTime != portal.getPairTime()) {
            blockEntity.linkedPortalId = linkedPortalId;
            blockEntity.pairTime = portal.getPairTime();
            blockEntity.setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (this.ownerId != null) {
            tag.store("OwnerId", UUIDUtil.CODEC, this.ownerId);
        }
        if (this.portalId != null) {
            tag.store("PortalId", UUIDUtil.CODEC, this.portalId);
        }
        if (this.linkedPortalId != null) {
            tag.store("LinkedPortalId", UUIDUtil.CODEC, this.linkedPortalId);
        }
        tag.putInt("Side", this.side.ordinal());
        tag.putInt("MasterX", this.masterPos.getX());
        tag.putInt("MasterY", this.masterPos.getY());
        tag.putInt("MasterZ", this.masterPos.getZ());
        tag.putInt("PairTime", this.pairTime);
        this.portalGun$saveFaceRecords(tag);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.ownerId = tag.read("OwnerId", UUIDUtil.CODEC).orElse(null);
        this.portalId = tag.read("PortalId", UUIDUtil.CODEC).orElse(null);
        this.linkedPortalId = tag.read("LinkedPortalId", UUIDUtil.CODEC).orElse(null);
        this.side = tag.getIntOr("Side", 0) == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        this.masterPos = new BlockPos(tag.getIntOr("MasterX", 0), tag.getIntOr("MasterY", 0), tag.getIntOr("MasterZ", 0));
        this.pairTime = tag.getIntOr("PairTime", 0);
        this.portalGun$loadFaceRecords(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
