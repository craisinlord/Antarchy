package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PortalGunPortalBaseBlockEntity extends BlockEntity {
    private UUID ownerId;
    private UUID portalId;
    private PortalGunPortalEntity.PortalSide side = PortalGunPortalEntity.PortalSide.BLUE;
    private BlockPos masterPos = BlockPos.ZERO;
    private UUID linkedPortalId;
    private int pairTime;

    public PortalGunPortalBaseBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<PortalGunPortalBaseBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
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
        this.setChanged();
    }

    public boolean matches(UUID ownerId, UUID portalId, PortalGunPortalEntity.PortalSide side) {
        return ownerId != null && portalId != null && ownerId.equals(this.ownerId) && portalId.equals(this.portalId) && this.side == side;
    }

    public void onBroken() {
        if (!(this.level instanceof ServerLevel serverLevel) || this.portalId == null) {
            return;
        }
        if (serverLevel.getEntity(this.portalId) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
            portal.discard();
        }
    }

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, PortalGunPortalBaseBlockEntity blockEntity) {
        if (((level.getGameTime() + pos.asLong()) & 15L) != 0L) {
            return;
        }
        if (!(level.getBlockEntity(blockEntity.masterPos) instanceof PortalGunPortalMasterBlockEntity master)
                || !master.matches(blockEntity.ownerId, blockEntity.portalId, blockEntity.side)
                || !pos.equals(master.getBasePos())) {
            level.removeBlock(pos, false);
            return;
        }
        if (!(level.getEntity(blockEntity.portalId) instanceof PortalGunPortalEntity portal)
                || portal.isRemoved()
                || !pos.equals(portal.getBasePos())
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.ownerId != null) {
            tag.putUUID("OwnerId", this.ownerId);
        }
        if (this.portalId != null) {
            tag.putUUID("PortalId", this.portalId);
        }
        if (this.linkedPortalId != null) {
            tag.putUUID("LinkedPortalId", this.linkedPortalId);
        }
        tag.putInt("Side", this.side.ordinal());
        tag.putInt("MasterX", this.masterPos.getX());
        tag.putInt("MasterY", this.masterPos.getY());
        tag.putInt("MasterZ", this.masterPos.getZ());
        tag.putInt("PairTime", this.pairTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        this.portalId = tag.hasUUID("PortalId") ? tag.getUUID("PortalId") : null;
        this.linkedPortalId = tag.hasUUID("LinkedPortalId") ? tag.getUUID("LinkedPortalId") : null;
        this.side = tag.getInt("Side") == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        this.masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        this.pairTime = tag.getInt("PairTime");
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
