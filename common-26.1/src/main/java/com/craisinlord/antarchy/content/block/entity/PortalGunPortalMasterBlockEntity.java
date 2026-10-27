package com.craisinlord.antarchy.content.block.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunPlacement;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.craisinlord.antarchy.content.portalgun.PortalGunVariant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PortalGunPortalMasterBlockEntity extends BlockEntity implements PortalGunPortalCellAccess {
    private final Map<Direction, PortalGunPortalFaceRecord> faceRecords = new EnumMap<>(Direction.class);
    private UUID ownerId;
    private UUID gunId;
    private PortalGunVariant variant = PortalGunVariant.DEFAULT;
    private UUID portalId;
    private PortalGunPortalEntity.PortalSide side = PortalGunPortalEntity.PortalSide.BLUE;
    private Direction facing = Direction.NORTH;
    private Direction upAxis = Direction.UP;
    private BlockPos basePos = BlockPos.ZERO;
    private BlockPos[] portalSpots = new BlockPos[] {BlockPos.ZERO, BlockPos.ZERO};
    private int portalWidth = 1;
    private int portalHeight = 2;
    private Set<BlockPos> compensatedSpots = Set.of();
    private UUID linkedPortalId;
    private int pairTime;

    public PortalGunPortalMasterBlockEntity(BlockPos pos, BlockState state, Supplier<? extends BlockEntityType<PortalGunPortalMasterBlockEntity>> typeSupplier) {
        super(typeSupplier.get(), pos, state);
    }

    public void configure(
            UUID ownerId,
            UUID gunId,
            PortalGunVariant variant,
            UUID portalId,
            PortalGunPortalEntity.PortalSide side,
            Direction facing,
            Direction upAxis,
            BlockPos basePos,
            BlockPos[] portalSpots,
            Set<BlockPos> compensatedSpots,
            int pairTime,
            int portalWidth,
            int portalHeight
    ) {
        this.ownerId = ownerId;
        this.gunId = gunId;
        this.variant = variant == null ? PortalGunVariant.DEFAULT : variant;
        this.portalId = portalId;
        this.side = side;
        this.facing = facing;
        this.upAxis = upAxis;
        this.basePos = basePos.immutable();
        this.portalSpots = java.util.Arrays.stream(portalSpots).map(BlockPos::immutable).toArray(BlockPos[]::new);
        this.portalWidth = portalWidth;
        this.portalHeight = portalHeight;
        this.compensatedSpots = new HashSet<>(compensatedSpots);
        this.pairTime = pairTime;
        this.portalGun$putFaceRecord(new PortalGunPortalFaceRecord(ownerId, gunId, this.variant, portalId, this.linkedPortalId, side, facing, upAxis, this.worldPosition, this.basePos, List.of(this.portalSpots), this.compensatedSpots, pairTime, portalWidth, portalHeight, true));
        this.setChanged();
    }

    @Override
    public Map<Direction, PortalGunPortalFaceRecord> portalGun$getFaceRecords() {
        return this.faceRecords;
    }

    public UUID getPortalId() {
        return this.portalId;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public UUID getGunId() {
        return this.gunId;
    }

    public PortalGunVariant getVariant() {
        return this.variant;
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
        return this.portalSpots.clone();
    }

    public boolean containsPortalSpot(BlockPos pos) {
        for (BlockPos portalSpot : this.portalSpots) {
            if (portalSpot.equals(pos)) {
                return true;
            }
        }
        return false;
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
        PortalGunPortalFaceRecord record = this.portalGun$getFaceRecord(this.facing, this.portalId);
        if (record != null) {
            this.portalGun$putFaceRecord(record.withPair(linkedPortalId, pairTime));
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
        Set<UUID> portalIds = new HashSet<>();
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

    public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, PortalGunPortalMasterBlockEntity blockEntity) {
        if (((level.getGameTime() + pos.asLong()) & 15L) != 0L) {
            return;
        }
        blockEntity.portalGun$restoreMasterFaces(level, pos);
        if (blockEntity.portalId != null
                && !(level.getEntity(blockEntity.portalId) instanceof PortalGunPortalEntity)
                && blockEntity.faceRecords.values().stream().anyMatch(record -> !record.portalId().equals(blockEntity.portalId))) {
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
        if (!PortalGunPlacement.isRectangularFootprint(this.portalSpots, this.facing, this.upAxis, this.portalWidth, this.portalHeight)) {
            return false;
        }
        List<BlockPos> supportPositions = new ArrayList<>(this.portalSpots.length);
        for (int i = 0; i < this.portalSpots.length; i++) {
            BlockPos portalSpot = this.portalSpots[i];
            if (portalSpot == null || BlockPos.ZERO.equals(portalSpot)) {
                return false;
            }
            if (i == 0) {
                if (!portalSpot.equals(pos)) {
                    return false;
                }
            } else if (!(level.getBlockEntity(portalSpot) instanceof PortalGunPortalBaseBlockEntity base)
                    || !base.matches(this.ownerId, this.portalId, this.side)
                    || !pos.equals(base.getMasterPos())) {
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
        if (!PortalGunSavedData.isRegistered(level, this.gunId, this.side, this.portalId)) {
            return null;
        }
        if (level.getEntity(this.portalId) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
            return portal;
        }
        PortalGunPortalFaceRecord record = this.portalGun$getFaceRecord(this.facing, this.portalId);
        if (record != null) {
            return restorePortalEntity(level, record);
        }
        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_portal"));
        if (!(rawType instanceof EntityType<?>)) {
            return null;
        }
        PortalGunPlacement placement = PortalGunPlacement.fromStored(this.facing, this.upAxis, this.worldPosition, this.basePos, this.portalSpots, this.compensatedSpots, this.portalWidth, this.portalHeight);
        @SuppressWarnings("unchecked")
        PortalGunPortalEntity restored = new PortalGunPortalEntity((EntityType<? extends PortalGunPortalEntity>) rawType, level);
        restored.setUUID(this.portalId);
        restored.configure(this.ownerId, this.gunId, this.variant, this.side, placement);
        restored.restorePair(this.linkedPortalId, this.pairTime);
        Vec3 center = placement.center();
        restored.snapTo(center.x, center.y, center.z, placement.yaw(), 0.0F);
        level.addFreshEntity(restored);
        PortalGunSavedData.setPortal(level, this.gunId, this.ownerId, this.side, restored.getUUID());
        return restored;
    }

    public static PortalGunPortalEntity restorePortalEntity(ServerLevel level, PortalGunPortalFaceRecord record) {
        if (record.ownerId() == null || record.portalId() == null
                || !PortalGunSavedData.isRegistered(level, record.gunId(), record.side(), record.portalId())) {
            return null;
        }
        if (level.getEntity(record.portalId()) instanceof PortalGunPortalEntity portal && !portal.isRemoved()) {
            return portal;
        }
        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_portal"));
        if (rawType == null) {
            return null;
        }
        PortalGunPlacement placement = record.placement();
        @SuppressWarnings("unchecked")
        PortalGunPortalEntity restored = new PortalGunPortalEntity((EntityType<? extends PortalGunPortalEntity>) rawType, level);
        restored.setUUID(record.portalId());
        restored.configure(record.ownerId(), record.gunId(), record.variant(), record.side(), placement);
        restored.restorePair(record.linkedPortalId(), record.pairTime());
        Vec3 center = placement.center();
        restored.snapTo(center.x, center.y, center.z, placement.yaw(), 0.0F);
        level.addFreshEntity(restored);
        PortalGunSavedData.setPortal(level, record.gunId(), record.ownerId(), record.side(), restored.getUUID());
        PortalGunPortalEntity linked = PortalGunSavedData.findLoadedPortal(level, record.gunId(), record.side() == PortalGunPortalEntity.PortalSide.BLUE ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE);
        if (linked != null && linked != restored && !linked.isRemoved()) {
            restored.linkTo(linked);
            linked.linkTo(restored);
        }
        return restored;
    }

    private void ensureLinkedPortal(ServerLevel level, PortalGunPortalEntity portal) {
        if (this.ownerId == null) {
            return;
        }
        PortalGunPortalEntity counterpart = PortalGunSavedData.findLoadedPortal(
                level,
                this.gunId,
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
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (this.ownerId != null) {
            tag.store("OwnerId", UUIDUtil.CODEC, this.ownerId);
        }
        if (this.gunId != null) {
            tag.store("GunId", UUIDUtil.CODEC, this.gunId);
        }
        tag.putInt("Variant", this.variant.id());
        if (this.portalId != null) {
            tag.store("PortalId", UUIDUtil.CODEC, this.portalId);
        }
        if (this.linkedPortalId != null) {
            tag.store("LinkedPortalId", UUIDUtil.CODEC, this.linkedPortalId);
        }
        tag.putInt("Side", this.side.ordinal());
        tag.putInt("Facing", this.facing.get3DDataValue());
        tag.putInt("UpAxis", this.upAxis.get3DDataValue());
        tag.putInt("BaseX", this.basePos.getX());
        tag.putInt("BaseY", this.basePos.getY());
        tag.putInt("BaseZ", this.basePos.getZ());
        tag.putInt("PairTime", this.pairTime);
        tag.putInt("PortalWidth", this.portalWidth);
        tag.putInt("PortalHeight", this.portalHeight);
        ValueOutput.TypedOutputList<CompoundTag> spots = tag.list("PortalSpots", CompoundTag.CODEC);
        for (BlockPos portalSpot : this.portalSpots) {
            CompoundTag spot = new CompoundTag();
            spot.putInt("X", portalSpot.getX());
            spot.putInt("Y", portalSpot.getY());
            spot.putInt("Z", portalSpot.getZ());
            spots.add(spot);
        }
        ValueOutput.TypedOutputList<CompoundTag> compensated = tag.list("CompensatedSpots", CompoundTag.CODEC);
        for (BlockPos compensatedSpot : this.compensatedSpots) {
            CompoundTag spot = new CompoundTag();
            spot.putInt("X", compensatedSpot.getX());
            spot.putInt("Y", compensatedSpot.getY());
            spot.putInt("Z", compensatedSpot.getZ());
            compensated.add(spot);
        }
        this.portalGun$saveFaceRecords(tag);
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.ownerId = tag.read("OwnerId", UUIDUtil.CODEC).orElse(null);
        this.gunId = tag.read("GunId", UUIDUtil.CODEC).orElse(null);
        this.variant = PortalGunVariant.byId(tag.getIntOr("Variant", 0));
        this.portalId = tag.read("PortalId", UUIDUtil.CODEC).orElse(null);
        this.linkedPortalId = tag.read("LinkedPortalId", UUIDUtil.CODEC).orElse(null);
        this.side = tag.getIntOr("Side", 0) == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        this.facing = Direction.from3DDataValue(tag.getIntOr("Facing", 0));
        this.upAxis = Direction.from3DDataValue(tag.getIntOr("UpAxis", 0));
        this.basePos = new BlockPos(tag.getIntOr("BaseX", 0), tag.getIntOr("BaseY", 0), tag.getIntOr("BaseZ", 0));
        this.pairTime = tag.getIntOr("PairTime", 0);
        List<CompoundTag> spots = tag.listOrEmpty("PortalSpots", CompoundTag.CODEC).stream().toList();
        if (!spots.isEmpty()) {
            this.portalSpots = new BlockPos[spots.size()];
        }
        for (int i = 0; i < spots.size(); i++) {
            CompoundTag spot = spots.get(i);
            this.portalSpots[i] = new BlockPos(spot.getIntOr("X", 0), spot.getIntOr("Y", 0), spot.getIntOr("Z", 0));
        }
        this.portalWidth = net.minecraft.util.Mth.clamp(tag.getIntOr("PortalWidth", 0), 1, 16);
        this.portalHeight = net.minecraft.util.Mth.clamp(tag.getIntOr("PortalHeight", 0), 2, 16);
        Set<BlockPos> compensatedSpots = new HashSet<>();
        for (CompoundTag spot : tag.listOrEmpty("CompensatedSpots", CompoundTag.CODEC)) {
            compensatedSpots.add(new BlockPos(spot.getIntOr("X", 0), spot.getIntOr("Y", 0), spot.getIntOr("Z", 0)));
        }
        this.compensatedSpots = compensatedSpots;
        this.portalGun$loadFaceRecords(tag);
        if (this.faceRecords.isEmpty() && this.ownerId != null && this.portalId != null && this.portalSpots.length > 0) {
            this.portalGun$putFaceRecord(new PortalGunPortalFaceRecord(this.ownerId, this.gunId, this.variant, this.portalId, this.linkedPortalId, this.side, this.facing, this.upAxis, this.worldPosition, this.basePos, List.of(this.portalSpots), this.compensatedSpots, this.pairTime, this.portalWidth, this.portalHeight, true));
        }
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
