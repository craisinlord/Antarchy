package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PortalGunPortalMasterBlockEntity extends BlockEntity {
    private UUID ownerId;
    private UUID portalId;
    private PortalGunPortalEntity.PortalSide side = PortalGunPortalEntity.PortalSide.BLUE;
    private Direction facing = Direction.NORTH;
    private Direction upAxis = Direction.UP;
    private BlockPos basePos = BlockPos.ZERO;
    private BlockPos[] portalSpots = new BlockPos[] {BlockPos.ZERO, BlockPos.ZERO};
    private Set<BlockPos> compensatedSpots = Set.of();
    private UUID linkedPortalId;
    private int pairTime;

    public PortalGunPortalMasterBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<PortalGunPortalMasterBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
    }

    public void configure(
            UUID ownerId,
            UUID portalId,
            PortalGunPortalEntity.PortalSide side,
            Direction facing,
            Direction upAxis,
            BlockPos basePos,
            BlockPos[] portalSpots,
            Set<BlockPos> compensatedSpots,
            int pairTime
    ) {
        this.ownerId = ownerId;
        this.portalId = portalId;
        this.side = side;
        this.facing = facing;
        this.upAxis = upAxis;
        this.basePos = basePos.immutable();
        this.portalSpots = new BlockPos[] {portalSpots[0].immutable(), portalSpots[1].immutable()};
        this.compensatedSpots = new HashSet<>(compensatedSpots);
        this.pairTime = pairTime;
        this.setChanged();
    }

    public UUID getPortalId() {
        return this.portalId;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public PortalGunPortalEntity.PortalSide getSide() {
        return this.side;
    }

    public Direction getFacing() {
        return this.facing;
    }

    public Direction getUpAxis() {
        return this.upAxis;
    }

    public BlockPos getBasePos() {
        return this.basePos;
    }

    public BlockPos[] getPortalSpots() {
        return new BlockPos[] {this.portalSpots[0], this.portalSpots[1]};
    }

    public Set<BlockPos> getCompensatedSpots() {
        return Set.copyOf(this.compensatedSpots);
    }

    public int getPairTime() {
        return this.pairTime;
    }

    public UUID getLinkedPortalId() {
        return this.linkedPortalId;
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

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, PortalGunPortalMasterBlockEntity blockEntity) {
        if (((level.getGameTime() + pos.asLong()) & 15L) != 0L) {
            return;
        }
        if (!blockEntity.hasValidFootprint(level, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        PortalGunPortalEntity portal = blockEntity.ensurePortalEntity(level);
        if (portal == null
                || portal.isRemoved()
                || !pos.equals(portal.getMasterPos())
                || blockEntity.ownerId == null
                || !blockEntity.ownerId.equals(portal.getOwnerId())
                || blockEntity.side != portal.getPortalSide()) {
            level.removeBlock(pos, false);
            return;
        }
        blockEntity.ensureLinkedPortal(level, portal);
        UUID linkedPortalId = portal.getLinkedPortalId();
        if ((linkedPortalId == null && blockEntity.linkedPortalId != null)
                || (linkedPortalId != null && !linkedPortalId.equals(blockEntity.linkedPortalId))
                || blockEntity.pairTime != portal.getPairTime()) {
            blockEntity.linkedPortalId = linkedPortalId;
            blockEntity.pairTime = portal.getPairTime();
            blockEntity.setChanged();
        }
    }

    private boolean hasValidFootprint(ServerLevel level, BlockPos pos) {
        if (this.ownerId == null || this.portalId == null || !pos.equals(this.worldPosition)) {
            return false;
        }
        if (!(level.getBlockEntity(this.basePos) instanceof PortalGunPortalBaseBlockEntity base)
                || !base.matches(this.ownerId, this.portalId, this.side)
                || !pos.equals(base.getMasterPos())) {
            return false;
        }
        List<BlockPos> supportPositions = new ArrayList<>(2);
        for (BlockPos portalSpot : this.portalSpots) {
            if (portalSpot == null || BlockPos.ZERO.equals(portalSpot)) {
                return false;
            }
            supportPositions.add(portalSpot.relative(this.facing.getOpposite()));
        }
        for (BlockPos supportPos : supportPositions) {
            BlockState supportState = level.getBlockState(supportPos);
            if (!supportState.isFaceSturdy(level, supportPos, this.facing)) {
                return false;
            }
        }
        return true;
    }

    private PortalGunPortalEntity ensurePortalEntity(ServerLevel level) {
        if (this.portalId == null || this.ownerId == null) {
            return null;
        }
        if (level.getEntity(this.portalId) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
            return portal;
        }
        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_portal"));
        if (!(rawType instanceof EntityType<?>)) {
            return null;
        }
        PortalGunPlacement placement = PortalGunPlacement.fromStored(this.facing, this.upAxis, this.worldPosition, this.basePos, this.compensatedSpots);
        @SuppressWarnings("unchecked")
        PortalGunPortalEntity restored = new PortalGunPortalEntity((EntityType<? extends PortalGunPortalEntity>) rawType, level);
        restored.setUUID(this.portalId);
        restored.configure(this.ownerId, this.side, placement);
        restored.restorePair(this.linkedPortalId, this.pairTime);
        Vec3 center = placement.center();
        restored.moveTo(center.x, center.y, center.z, placement.yaw(), 0.0F);
        level.addFreshEntity(restored);
        PortalGunSavedData.setPortal(level.getServer(), this.ownerId, this.side, restored.getUUID());
        return restored;
    }

    private void ensureLinkedPortal(ServerLevel level, PortalGunPortalEntity portal) {
        if (this.ownerId == null) {
            return;
        }
        PortalGunPortalEntity counterpart = PortalGunSavedData.findLoadedPortal(
                level.getServer(),
                this.ownerId,
                this.side == PortalGunPortalEntity.PortalSide.BLUE ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE
        );
        if (counterpart == null || counterpart == portal || counterpart.isRemoved()) {
            return;
        }
        if (!counterpart.getUUID().equals(portal.getLinkedPortalId()) || !portal.getUUID().equals(counterpart.getLinkedPortalId())) {
            portal.linkTo(counterpart);
            counterpart.linkTo(portal);
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
        tag.putInt("Facing", this.facing.get3DDataValue());
        tag.putInt("UpAxis", this.upAxis.get3DDataValue());
        tag.putInt("BaseX", this.basePos.getX());
        tag.putInt("BaseY", this.basePos.getY());
        tag.putInt("BaseZ", this.basePos.getZ());
        tag.putInt("PairTime", this.pairTime);
        ListTag spots = new ListTag();
        for (BlockPos portalSpot : this.portalSpots) {
            CompoundTag spot = new CompoundTag();
            spot.putInt("X", portalSpot.getX());
            spot.putInt("Y", portalSpot.getY());
            spot.putInt("Z", portalSpot.getZ());
            spots.add(spot);
        }
        tag.put("PortalSpots", spots);
        ListTag compensated = new ListTag();
        for (BlockPos compensatedSpot : this.compensatedSpots) {
            CompoundTag spot = new CompoundTag();
            spot.putInt("X", compensatedSpot.getX());
            spot.putInt("Y", compensatedSpot.getY());
            spot.putInt("Z", compensatedSpot.getZ());
            compensated.add(spot);
        }
        tag.put("CompensatedSpots", compensated);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        this.portalId = tag.hasUUID("PortalId") ? tag.getUUID("PortalId") : null;
        this.linkedPortalId = tag.hasUUID("LinkedPortalId") ? tag.getUUID("LinkedPortalId") : null;
        this.side = tag.getInt("Side") == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        this.facing = Direction.from3DDataValue(tag.getInt("Facing"));
        this.upAxis = Direction.from3DDataValue(tag.getInt("UpAxis"));
        this.basePos = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
        this.pairTime = tag.getInt("PairTime");
        ListTag spots = tag.getList("PortalSpots", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(2, spots.size()); i++) {
            CompoundTag spot = spots.getCompound(i);
            this.portalSpots[i] = new BlockPos(spot.getInt("X"), spot.getInt("Y"), spot.getInt("Z"));
        }
        ListTag compensated = tag.getList("CompensatedSpots", Tag.TAG_COMPOUND);
        Set<BlockPos> compensatedSpots = new HashSet<>();
        for (int i = 0; i < compensated.size(); i++) {
            CompoundTag spot = compensated.getCompound(i);
            compensatedSpots.add(new BlockPos(spot.getInt("X"), spot.getInt("Y"), spot.getInt("Z")));
        }
        this.compensatedSpots = compensatedSpots;
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
