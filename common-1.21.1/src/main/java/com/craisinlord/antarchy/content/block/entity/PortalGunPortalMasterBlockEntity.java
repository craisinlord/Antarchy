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
        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_portal"));
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
        restored.moveTo(center.x, center.y, center.z, placement.yaw(), 0.0F);
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
        EntityType<?> rawType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "portal_gun_portal"));
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
        restored.moveTo(center.x, center.y, center.z, placement.yaw(), 0.0F);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.ownerId != null) {
            tag.putUUID("OwnerId", this.ownerId);
        }
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
        tag.putInt("Variant", this.variant.id());
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
        tag.putInt("PortalWidth", this.portalWidth);
        tag.putInt("PortalHeight", this.portalHeight);
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
        this.portalGun$saveFaceRecords(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.ownerId = tag.hasUUID("OwnerId") ? tag.getUUID("OwnerId") : null;
        this.gunId = tag.hasUUID("GunId") ? tag.getUUID("GunId") : null;
        this.variant = PortalGunVariant.byId(tag.getInt("Variant"));
        this.portalId = tag.hasUUID("PortalId") ? tag.getUUID("PortalId") : null;
        this.linkedPortalId = tag.hasUUID("LinkedPortalId") ? tag.getUUID("LinkedPortalId") : null;
        this.side = tag.getInt("Side") == PortalGunPortalEntity.PortalSide.ORANGE.ordinal() ? PortalGunPortalEntity.PortalSide.ORANGE : PortalGunPortalEntity.PortalSide.BLUE;
        this.facing = Direction.from3DDataValue(tag.getInt("Facing"));
        this.upAxis = Direction.from3DDataValue(tag.getInt("UpAxis"));
        this.basePos = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
        this.pairTime = tag.getInt("PairTime");
        ListTag spots = tag.getList("PortalSpots", Tag.TAG_COMPOUND);
        if (!spots.isEmpty()) {
            this.portalSpots = new BlockPos[spots.size()];
        }
        for (int i = 0; i < spots.size(); i++) {
            CompoundTag spot = spots.getCompound(i);
            this.portalSpots[i] = new BlockPos(spot.getInt("X"), spot.getInt("Y"), spot.getInt("Z"));
        }
        this.portalWidth = net.minecraft.util.Mth.clamp(tag.getInt("PortalWidth"), 1, 16);
        this.portalHeight = net.minecraft.util.Mth.clamp(tag.getInt("PortalHeight"), 2, 16);
        ListTag compensated = tag.getList("CompensatedSpots", Tag.TAG_COMPOUND);
        Set<BlockPos> compensatedSpots = new HashSet<>();
        for (int i = 0; i < compensated.size(); i++) {
            CompoundTag spot = compensated.getCompound(i);
            compensatedSpots.add(new BlockPos(spot.getInt("X"), spot.getInt("Y"), spot.getInt("Z")));
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
