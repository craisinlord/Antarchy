package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.mixins.AbstractArrowAccessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PortalGunPortalEntity extends Entity implements GeoEntity {
    public enum PortalSide {
        BLUE,
        ORANGE
    }

    private static final EntityDataAccessor<Integer> SIDE = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> UP_AXIS = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CLOSING = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> LINKED_PORTAL = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> CHANNEL_RED = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHANNEL_GREEN = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHANNEL_BLUE = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("close");
    private static final int OPEN_TICKS = 10;
    private static final int CLOSE_TICKS = 10;
    private static final int MAX_LIFETIME_TICKS = 20 * 60 * 20;
    private static final int TELEPORT_COOLDOWN_TICKS = 40;
    private static final double PLAYER_PORTAL_INSIDE_SHIFT = 0.05D;
    private static final double HALF_WIDTH = 0.5D;
    private static final double HALF_HEIGHT = 1.0D;
    private static final double HALF_DEPTH = 0.35D;
    private static final double MIN_EXIT_MOMENTUM = 0.42D;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Integer> teleportCooldowns = new HashMap<>();
    private final Map<UUID, Integer> portalEntityDepths = new HashMap<>();
    private Set<UUID> lastScanEntities = new HashSet<>();
    private UUID ownerId;
    private UUID linkedPortalId;
    private int ageTicks;
    private int pairTime;
    private BlockPos supportOrigin = BlockPos.ZERO;
    private BlockPos masterPos = BlockPos.ZERO;
    private BlockPos basePos = BlockPos.ZERO;
    private BlockPos[] portalSpots = new BlockPos[] {BlockPos.ZERO, BlockPos.ZERO};
    private Set<BlockPos> compensatedSpots = Set.of();

    public PortalGunPortalEntity(EntityType<? extends PortalGunPortalEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIDE, PortalSide.BLUE.ordinal());
        builder.define(FACING, Direction.NORTH.get3DDataValue());
        builder.define(UP_AXIS, Direction.UP.get3DDataValue());
        builder.define(CLOSING, false);
        builder.define(LINKED_PORTAL, Optional.empty());
        builder.define(CHANNEL_RED, 58);
        builder.define(CHANNEL_GREEN, 166);
        builder.define(CHANNEL_BLUE, 255);
    }

    public void configure(UUID ownerId, PortalSide side, PortalGunPlacement placement) {
        this.ownerId = ownerId;
        this.entityData.set(SIDE, side.ordinal());
        this.entityData.set(FACING, placement.facing().get3DDataValue());
        this.entityData.set(UP_AXIS, placement.upAxis().get3DDataValue());
        int[] channelColor = generateChannelColor(ownerId, side);
        this.entityData.set(CHANNEL_RED, channelColor[0]);
        this.entityData.set(CHANNEL_GREEN, channelColor[1]);
        this.entityData.set(CHANNEL_BLUE, channelColor[2]);
        this.supportOrigin = placement.supportOrigin().immutable();
        this.masterPos = placement.masterPos().immutable();
        this.basePos = placement.basePos().immutable();
        this.portalSpots = new BlockPos[] {placement.portalSpots()[0].immutable(), placement.portalSpots()[1].immutable()};
        this.compensatedSpots = Set.copyOf(placement.compensatedSpots());
    }

    public void linkTo(PortalGunPortalEntity other) {
        this.linkedPortalId = other.getUUID();
        this.entityData.set(LINKED_PORTAL, Optional.of(other.getUUID()));
        this.pairTime = (int) this.level().getGameTime();
    }

    public void restorePair(UUID linkedPortalId, int pairTime) {
        this.linkedPortalId = linkedPortalId;
        this.entityData.set(LINKED_PORTAL, Optional.ofNullable(linkedPortalId));
        this.pairTime = pairTime;
    }

    public PortalSide getPortalSide() {
        int ordinal = this.entityData.get(SIDE);
        return ordinal == PortalSide.ORANGE.ordinal() ? PortalSide.ORANGE : PortalSide.BLUE;
    }

    public Direction getFacingDirection() {
        return Direction.from3DDataValue(this.entityData.get(FACING));
    }

    public Direction getUpAxis() {
        return Direction.from3DDataValue(this.entityData.get(UP_AXIS));
    }

    public UUID getLinkedPortalId() {
        return this.entityData.get(LINKED_PORTAL).orElse(this.linkedPortalId);
    }

    public BlockPos getMasterPos() {
        return this.masterPos;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    public int getChannelRed() {
        return this.entityData.get(CHANNEL_RED);
    }

    public int getChannelGreen() {
        return this.entityData.get(CHANNEL_GREEN);
    }

    public int getChannelBlue() {
        return this.entityData.get(CHANNEL_BLUE);
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

    public PortalGunPortalEntity getLinkedPortal() {
        UUID linkedId = this.getLinkedPortalId();
        if (linkedId == null) {
            return null;
        }
        List<Entity> entities = this.level().getEntities(this, new AABB(-3.0E7D, -3.0E7D, -3.0E7D, 3.0E7D, 3.0E7D, 3.0E7D), entity -> linkedId.equals(entity.getUUID()));
        if (entities.isEmpty()) {
            return null;
        }
        Entity entity = entities.getFirst();
        return entity instanceof PortalGunPortalEntity portal ? portal : null;
    }

    public Vec3 getNormalVec() {
        return Vec3.atLowerCornerOf(this.getFacingDirection().getNormal());
    }

    public Vec3 getUpVec() {
        return Vec3.atLowerCornerOf(this.getUpAxis().getNormal());
    }

    public Vec3 getWidthVec() {
        return this.getNormalVec().cross(this.getUpVec()).normalize();
    }

    public PortalGunWorldPortalShape getWorldPortalShape() {
        return new PortalGunWorldPortalShape(this.position(), this.getNormalVec().normalize(), this.getUpVec().normalize(), this.getWidthVec().normalize(), HALF_WIDTH, HALF_HEIGHT, HALF_DEPTH);
    }

    public AABB getPlane() {
        return this.getWorldPortalShape().getPlane();
    }

    public AABB getFlatPlane() {
        return this.getWorldPortalShape().getFlatPlane();
    }

    public AABB getScanRange() {
        return this.getWorldPortalShape().getScanRange();
    }

    public AABB getPortalInsides() {
        return this.getWorldPortalShape().getPortalInsides();
    }

    public AABB getPortalInsides(Entity entity) {
        if (!(entity instanceof Player)) {
            return this.getPortalInsides();
        }
        double shiftAmount = Math.min(PLAYER_PORTAL_INSIDE_SHIFT, Math.abs(this.getWorldPortalShape().localCoords(entity.position()).depth()));
        return this.getPortalInsides().move(this.getNormalVec().normalize().scale(shiftAmount));
    }

    public AABB getTeleportPlane(double offset) {
        return this.getWorldPortalShape().getTeleportPlane(offset);
    }

    public AABB getCollisionRemovalAabbForEntity(Entity entity) {
        double speed = entity.getDeltaMovement().length();
        double extent = Math.max(1.0D, Math.max(entity.getBbWidth(), entity.getBbHeight()) + speed);
        return this.getFlatPlane().expandTowards(this.getNormalVec().normalize().scale(extent));
    }

    public Vec3 teleportProbePosition(Entity entity) {
        if (entity instanceof Player player) {
            return player.getEyePosition();
        }
        if (entity instanceof LivingEntity living) {
            return entity.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
        }
        return entity.position();
    }

    public Vec3 previousTeleportProbePosition(Entity entity) {
        double prevX = entity.xo;
        double prevY = entity.yo;
        double prevZ = entity.zo;
        if (entity instanceof Player player) {
            return new Vec3(prevX, prevY + player.getEyeHeight(player.getPose()), prevZ);
        }
        if (entity instanceof LivingEntity living) {
            return new Vec3(prevX, prevY + living.getBbHeight() * 0.5D, prevZ);
        }
        return new Vec3(prevX, prevY, prevZ);
    }

    public Vec3 resolveCrossingProbe(Entity entity) {
        AABB currentBox = entity.getBoundingBox();
        AABB previousBox = currentBox.move(entity.xo - entity.getX(), entity.yo - entity.getY(), entity.zo - entity.getZ());
        for (Vec3 currentSample : portalSamples(entity, currentBox, false)) {
            Vec3 previousSample = matchPreviousSample(currentSample, currentBox, previousBox);
            if (this.crossesPortal(previousSample, currentSample)) {
                return currentSample;
            }
        }
        Vec3 previousProbe = this.previousTeleportProbePosition(entity);
        Vec3 currentProbe = this.teleportProbePosition(entity);
        if (this.crossesPortal(previousProbe, currentProbe)) {
            return currentProbe;
        }
        return this.overlapsPortalVolume(entity) ? this.portalVolumeProbe(entity) : null;
    }

    public boolean containsPoint(Vec3 position) {
        return this.getWorldPortalShape().contains(position, 0.0D);
    }

    public boolean intersectsEntityBounds(Entity entity) {
        AABB bounds = entity.getBoundingBox();
        for (Vec3 corner : new Vec3[] {
                new Vec3(bounds.minX, bounds.minY, bounds.minZ),
                new Vec3(bounds.minX, bounds.minY, bounds.maxZ),
                new Vec3(bounds.minX, bounds.maxY, bounds.minZ),
                new Vec3(bounds.minX, bounds.maxY, bounds.maxZ),
                new Vec3(bounds.maxX, bounds.minY, bounds.minZ),
                new Vec3(bounds.maxX, bounds.minY, bounds.maxZ),
                new Vec3(bounds.maxX, bounds.maxY, bounds.minZ),
                new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ)
        }) {
            if (this.getWorldPortalShape().contains(corner, 0.08D)) {
                return true;
            }
        }
        return this.getPortalInsides(entity).intersects(bounds);
    }

    public boolean crossesPortal(Vec3 previousProbePosition, Vec3 currentProbePosition) {
        return this.getWorldPortalShape().crosses(previousProbePosition, currentProbePosition);
    }

    public boolean shouldRenderFront(Vec3 cameraPos) {
        return this.getWorldPortalShape().shouldRenderFront(cameraPos);
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.tickTeleportCooldowns();
        if (this.ownerId == null || !this.isStillRegistered(serverLevel)) {
            this.discard();
            return;
        }
        if (!this.hasValidSurface()) {
            this.discard();
            return;
        }
        if (this.ageTicks >= MAX_LIFETIME_TICKS - CLOSE_TICKS) {
            this.entityData.set(CLOSING, true);
        }
        if (this.ageTicks >= MAX_LIFETIME_TICKS) {
            this.discard();
            return;
        }
        PortalGunPortalEntity linked = this.findLinkedPortal(serverLevel);
        if (linked == null || !linked.isAlive()) {
            if (this.ageTicks % 100 == 0) {
                Antarchy.LOGGER.info("Portal gun portal has no loaded linked portal portal={} owner={} side={} linked={}", this.getUUID(), this.ownerId, this.getPortalSide(), this.linkedPortalId);
            }
            return;
        }
        if (this.ageTicks % 80 == 0) {
            this.level().playSound(null, this.blockPosition(), sound("portal_ambient", SoundEvents.PORTAL_AMBIENT), SoundSource.BLOCKS, 0.18F, this.getPortalSide() == PortalSide.BLUE ? 1.05F : 0.92F);
        }
        this.tickTeleport(linked);
    }

    private boolean isStillRegistered(ServerLevel level) {
        return this.ownerId != null && PortalGunSavedData.isRegistered(level.getServer(), this.ownerId, this.getPortalSide(), this.getUUID());
    }

    private boolean hasValidSurface() {
        Direction facing = this.getFacingDirection();
        for (int i = 0; i < this.portalSpots.length; i++) {
            BlockPos portalSpot = this.portalSpots[i];
            BlockPos supportPos = portalSpot.relative(facing.getOpposite());
            BlockState supportState = this.level().getBlockState(supportPos);
            if (!supportState.isFaceSturdy(this.level(), supportPos, facing)) {
                return false;
            }
            BlockEntity blockEntity = this.level().getBlockEntity(portalSpot);
            if (i == 0) {
                if (!(blockEntity instanceof PortalGunPortalMasterBlockEntity master) || !master.matches(this.ownerId, this.getUUID(), this.getPortalSide())) {
                    return false;
                }
            } else {
                if (!(blockEntity instanceof PortalGunPortalBaseBlockEntity base) || !base.matches(this.ownerId, this.getUUID(), this.getPortalSide())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void tickTeleport(PortalGunPortalEntity linked) {
        List<Entity> entities = this.level().getEntities(this, this.getScanRange(), this::canTeleportEntity);
        this.updatePortalEntityScan(linked, entities);
        if (!entities.isEmpty() && this.ageTicks % 20 == 0) {
            Antarchy.LOGGER.info("Portal gun teleport scan portal={} side={} linked={} count={} scan={}", this.getUUID(), this.getPortalSide(), linked.getUUID(), entities.size(), this.getScanRange());
        }
        for (Entity entity : entities) {
            Vec3 crossingProbe = this.resolveCrossingProbe(entity);
            if (crossingProbe == null) {
                if (this.ageTicks % 40 == 0) {
                    PortalGunWorldPortalShape.PortalLocalCoords coords = this.getWorldPortalShape().localCoords(this.teleportProbePosition(entity));
                    PortalBounds bounds = this.portalBounds(entity.getBoundingBox());
                    Antarchy.LOGGER.info("Portal gun entity in scan did not cross portal={} entity={} entityType={} depth={} horizontal={} vertical={} boxDepthMin={} boxDepthMax={} boxHorizontalMin={} boxHorizontalMax={} boxVerticalMin={} boxVerticalMax={} movement={}", this.getUUID(), entity.getUUID(), EntityType.getKey(entity.getType()), coords.depth(), coords.horizontal(), coords.vertical(), bounds.minDepth(), bounds.maxDepth(), bounds.minHorizontal(), bounds.maxHorizontal(), bounds.minVertical(), bounds.maxVertical(), entity.getDeltaMovement());
                }
                continue;
            }
            Antarchy.LOGGER.info("Portal gun teleport crossing portal={} side={} linked={} entity={} entityType={} probe={}", this.getUUID(), this.getPortalSide(), linked.getUUID(), entity.getUUID(), EntityType.getKey(entity.getType()), crossingProbe);
            this.teleportEntity(entity, linked, crossingProbe);
        }
    }

    private void updatePortalEntityScan(PortalGunPortalEntity linked, List<Entity> entities) {
        Set<UUID> currentScan = new HashSet<>();
        for (Entity entity : entities) {
            UUID entityId = entity.getUUID();
            currentScan.add(entityId);
            PortalBounds bounds = this.portalBounds(entity.getBoundingBox());
            int depthBucket = (int) Math.round(bounds.minDepth() * 1000.0D);
            this.portalEntityDepths.put(entityId, depthBucket);
            if (!this.lastScanEntities.contains(entityId)) {
                Antarchy.LOGGER.info("Portal gun entity entered scan portal={} linked={} entity={} entityType={} minDepth={} maxDepth={} minHorizontal={} maxHorizontal={} minVertical={} maxVertical={} motion={}", this.getUUID(), linked.getUUID(), entityId, EntityType.getKey(entity.getType()), bounds.minDepth(), bounds.maxDepth(), bounds.minHorizontal(), bounds.maxHorizontal(), bounds.minVertical(), bounds.maxVertical(), entity.getDeltaMovement());
            }
        }
        for (UUID previous : this.lastScanEntities) {
            if (!currentScan.contains(previous)) {
                Integer depth = this.portalEntityDepths.remove(previous);
                Antarchy.LOGGER.info("Portal gun entity left scan portal={} linked={} entity={} lastDepth={}", this.getUUID(), linked.getUUID(), previous, depth);
            }
        }
        this.lastScanEntities = currentScan;
    }

    private boolean canTeleportEntity(Entity entity) {
        if (!entity.isAlive() || entity instanceof PortalGunPortalEntity || entity.isPassenger() || entity.isVehicle()) {
            return false;
        }
        return !this.teleportCooldowns.containsKey(entity.getUUID());
    }

    private boolean isInsidePortal(Vec3 position) {
        return this.getWorldPortalShape().contains(position, 0.0D);
    }

    private PortalLocalCoords localCoords(Vec3 position) {
        PortalGunWorldPortalShape.PortalLocalCoords coords = this.getWorldPortalShape().localCoords(position);
        return new PortalLocalCoords(coords.horizontal(), coords.vertical(), coords.depth());
    }

    private boolean overlapsPortalVolume(Entity entity) {
        PortalBounds bounds = this.portalBounds(entity.getBoundingBox());
        double edgePadding = entity instanceof Player ? 0.22D : 0.14D;
        double frontDepth = entity instanceof Player ? 0.5D : 0.36D;
        double backDepth = entity instanceof Player ? 0.28D : 0.22D;
        double normalVelocity = entity.getDeltaMovement().dot(this.getNormalVec().normalize());
        boolean movingIntoPortal = normalVelocity < -0.01D;
        boolean straddlingPlane = bounds.minDepth() <= 0.03D && bounds.maxDepth() >= -0.03D;
        boolean inFrame = bounds.maxHorizontal() >= -HALF_WIDTH - edgePadding
                && bounds.minHorizontal() <= HALF_WIDTH + edgePadding
                && bounds.maxVertical() >= -HALF_HEIGHT - edgePadding
                && bounds.minVertical() <= HALF_HEIGHT + edgePadding
                && bounds.maxDepth() >= -backDepth
                && bounds.minDepth() <= frontDepth;
        return inFrame && (movingIntoPortal || straddlingPlane);
    }

    private Vec3 portalVolumeProbe(Entity entity) {
        Vec3 probe = this.teleportProbePosition(entity);
        PortalGunWorldPortalShape shape = this.getWorldPortalShape();
        PortalGunWorldPortalShape.PortalLocalCoords coords = shape.localCoords(probe);
        double horizontal = clamp(coords.horizontal(), -HALF_WIDTH + 0.05D, HALF_WIDTH - 0.05D);
        double vertical = clamp(coords.vertical(), -HALF_HEIGHT + 0.05D, HALF_HEIGHT - 0.05D);
        return shape.center()
                .add(shape.right().scale(horizontal))
                .add(shape.up().scale(vertical))
                .subtract(shape.normal().scale(0.01D));
    }

    private PortalBounds portalBounds(AABB bounds) {
        PortalGunWorldPortalShape shape = this.getWorldPortalShape();
        double minHorizontal = Double.POSITIVE_INFINITY;
        double maxHorizontal = Double.NEGATIVE_INFINITY;
        double minVertical = Double.POSITIVE_INFINITY;
        double maxVertical = Double.NEGATIVE_INFINITY;
        double minDepth = Double.POSITIVE_INFINITY;
        double maxDepth = Double.NEGATIVE_INFINITY;
        for (Vec3 corner : boxCorners(bounds)) {
            PortalGunWorldPortalShape.PortalLocalCoords coords = shape.localCoords(corner);
            minHorizontal = Math.min(minHorizontal, coords.horizontal());
            maxHorizontal = Math.max(maxHorizontal, coords.horizontal());
            minVertical = Math.min(minVertical, coords.vertical());
            maxVertical = Math.max(maxVertical, coords.vertical());
            minDepth = Math.min(minDepth, coords.depth());
            maxDepth = Math.max(maxDepth, coords.depth());
        }
        return new PortalBounds(minHorizontal, maxHorizontal, minVertical, maxVertical, minDepth, maxDepth);
    }

    private static Vec3[] boxCorners(AABB bounds) {
        return new Vec3[] {
                new Vec3(bounds.minX, bounds.minY, bounds.minZ),
                new Vec3(bounds.minX, bounds.minY, bounds.maxZ),
                new Vec3(bounds.minX, bounds.maxY, bounds.minZ),
                new Vec3(bounds.minX, bounds.maxY, bounds.maxZ),
                new Vec3(bounds.maxX, bounds.minY, bounds.minZ),
                new Vec3(bounds.maxX, bounds.minY, bounds.maxZ),
                new Vec3(bounds.maxX, bounds.maxY, bounds.minZ),
                new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ)
        };
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private List<Vec3> portalSamples(Entity entity, AABB box, boolean previous) {
        Vec3 center = new Vec3((box.minX + box.maxX) * 0.5D, (box.minY + box.maxY) * 0.5D, (box.minZ + box.maxZ) * 0.5D);
        Vec3 topCenter = new Vec3(center.x, box.maxY, center.z);
        Vec3 bottomCenter = new Vec3(center.x, box.minY, center.z);
        if (entity instanceof Player player) {
            Vec3 eye = previous ? this.previousTeleportProbePosition(player) : this.teleportProbePosition(player);
            return List.of(center, eye, topCenter, bottomCenter);
        }
        if (entity instanceof LivingEntity) {
            return List.of(center, topCenter, bottomCenter);
        }
        return List.of(center);
    }

    private Vec3 matchPreviousSample(Vec3 currentSample, AABB currentBox, AABB previousBox) {
        double xLerp = axisRatio(currentSample.x, currentBox.minX, currentBox.maxX);
        double yLerp = axisRatio(currentSample.y, currentBox.minY, currentBox.maxY);
        double zLerp = axisRatio(currentSample.z, currentBox.minZ, currentBox.maxZ);
        return new Vec3(
                lerp(previousBox.minX, previousBox.maxX, xLerp),
                lerp(previousBox.minY, previousBox.maxY, yLerp),
                lerp(previousBox.minZ, previousBox.maxZ, zLerp)
        );
    }

    private static double axisRatio(double value, double min, double max) {
        double size = max - min;
        if (Math.abs(size) < 1.0E-6D) {
            return 0.5D;
        }
        return (value - min) / size;
    }

    private static double lerp(double min, double max, double delta) {
        return min + (max - min) * delta;
    }

    private void teleportEntity(Entity entity, PortalGunPortalEntity destination, Vec3 currentProbe) {
        Vec3 relativeProbe = currentProbe.subtract(this.position());
        Vec3 transformedProbe = PortalGunTransformUtil.transformPosition(this, destination, relativeProbe);
        Vec3 entityOffsetFromProbe = entity.position().subtract(currentProbe);
        Vec3 transformedEntityOffset = PortalGunTransformUtil.transformVector(this, destination, entityOffsetFromProbe);
        Vec3 transformedLook = PortalGunTransformUtil.transformVector(this, destination, entity.getLookAngle()).normalize();
        Vec3 entryMotion = this.portalEntryMomentum(entity, currentProbe);
        Vec3 transformedMotion = this.exitMomentum(entity, destination, PortalGunTransformUtil.transformVector(this, destination, entryMotion));
        double exitClearance = Math.max(0.35D, entity.getBbWidth() * 0.5D + 0.16D);
        Vec3 exitProbe = destination.position().add(transformedProbe).add(destination.getNormalVec().normalize().scale(exitClearance - transformedProbe.dot(destination.getNormalVec().normalize())));
        Vec3 requestedExitPos = exitProbe.add(transformedEntityOffset);
        Vec3 exitPos = this.resolveSafeExitPosition(entity, destination, requestedExitPos);
        float yaw = PortalGunTransformUtil.yawFromLook(transformedLook);
        float pitch = PortalGunTransformUtil.pitchFromLook(transformedLook);
        this.applyTeleportCooldown(entity, destination);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound("portal_enter", SoundEvents.PORTAL_TRAVEL), SoundSource.PLAYERS, 0.7F, 0.96F + this.random.nextFloat() * 0.08F);
        if (entity instanceof ServerPlayer player && destination.level() instanceof ServerLevel destinationLevel) {
            player.teleportTo(destinationLevel, exitPos.x, exitPos.y, exitPos.z, yaw, pitch);
        } else {
            entity.teleportTo(exitPos.x, exitPos.y, exitPos.z);
            entity.setYRot(yaw);
            entity.setXRot(pitch);
            entity.setYHeadRot(yaw);
            entity.setYBodyRot(yaw);
        }
        entity.setDeltaMovement(transformedMotion);
        entity.hasImpulse = true;
        entity.hurtMarked = true;
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
        entity.fallDistance = 0.0F;
        destination.level().playSound(null, exitPos.x, exitPos.y, exitPos.z, sound("portal_exit", SoundEvents.PORTAL_TRAVEL), SoundSource.PLAYERS, 0.7F, 0.96F + this.random.nextFloat() * 0.08F);
        this.handleSpecialEntityPostTeleport(entity);
        Antarchy.LOGGER.info("Portal gun teleported entity source={} destination={} entity={} entityType={} requestedExit={} exit={} inputVelocity={} outputVelocity={} yaw={} pitch={} sourceFacing={} sourceUp={} destinationFacing={} destinationUp={}", this.getUUID(), destination.getUUID(), entity.getUUID(), EntityType.getKey(entity.getType()), requestedExitPos, exitPos, entryMotion, transformedMotion, yaw, pitch, this.getFacingDirection(), this.getUpAxis(), destination.getFacingDirection(), destination.getUpAxis());
    }

    private Vec3 portalEntryMomentum(Entity entity, Vec3 currentProbe) {
        Vec3 velocity = entity.getDeltaMovement();
        Vec3 probeVelocity = currentProbe.subtract(this.previousTeleportProbePosition(entity));
        Vec3 positionVelocity = entity.position().subtract(entity.xo, entity.yo, entity.zo);
        Vec3 selected = velocity;
        if (probeVelocity.lengthSqr() > selected.lengthSqr()) {
            selected = probeVelocity;
        }
        if (positionVelocity.lengthSqr() > selected.lengthSqr()) {
            selected = positionVelocity;
        }
        return selected;
    }

    private Vec3 exitMomentum(Entity entity, PortalGunPortalEntity destination, Vec3 transformedMotion) {
        Vec3 normal = destination.getNormalVec().normalize();
        double outward = transformedMotion.dot(normal);
        if (outward >= MIN_EXIT_MOMENTUM) {
            return transformedMotion;
        }
        if (entity.getDeltaMovement().lengthSqr() <= 1.0E-5D && transformedMotion.lengthSqr() <= 1.0E-5D) {
            return normal.scale(MIN_EXIT_MOMENTUM);
        }
        return transformedMotion.add(normal.scale(MIN_EXIT_MOMENTUM - outward));
    }

    private Vec3 resolveSafeExitPosition(Entity entity, PortalGunPortalEntity destination, Vec3 requestedExitPos) {
        Vec3 normal = destination.getNormalVec().normalize();
        if (this.canPlaceEntityAt(entity, requestedExitPos)) {
            return requestedExitPos;
        }
        for (int i = 1; i <= 24; i++) {
            Vec3 candidate = requestedExitPos.add(normal.scale(i * 0.125D));
            if (this.canPlaceEntityAt(entity, candidate)) {
                Antarchy.LOGGER.info("Portal gun adjusted blocked exit entity={} destination={} requested={} adjusted={} distance={}", entity.getUUID(), destination.getUUID(), requestedExitPos, candidate, i * 0.125D);
                return candidate;
            }
        }
        Vec3 fallback = destination.position().add(normal.scale(Math.max(1.0D, entity.getBbWidth() + 0.5D)));
        Antarchy.LOGGER.info("Portal gun could not find clear exit entity={} destination={} requested={} fallback={}", entity.getUUID(), destination.getUUID(), requestedExitPos, fallback);
        return fallback;
    }

    private boolean canPlaceEntityAt(Entity entity, Vec3 position) {
        AABB movedBounds = entity.getBoundingBox().move(position.x - entity.getX(), position.y - entity.getY(), position.z - entity.getZ());
        return this.level().noCollision(entity, movedBounds);
    }

    private void tickTeleportCooldowns() {
        Iterator<Map.Entry<UUID, Integer>> iterator = this.teleportCooldowns.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int cooldown = entry.getValue() - 1;
            if (cooldown < 0) {
                iterator.remove();
            } else {
                entry.setValue(cooldown);
            }
        }
    }

    private void applyTeleportCooldown(Entity entity, PortalGunPortalEntity destination) {
        UUID entityId = entity.getUUID();
        this.teleportCooldowns.put(entityId, TELEPORT_COOLDOWN_TICKS);
        destination.teleportCooldowns.put(entityId, TELEPORT_COOLDOWN_TICKS);
    }

    private void handleSpecialEntityPostTeleport(Entity entity) {
        if (entity instanceof AbstractArrow arrow) {
            ((AbstractArrowAccessor) arrow).antarchy$setInGround(false);
            ((AbstractArrowAccessor) arrow).antarchy$setInGroundTime(0);
            ((AbstractArrowAccessor) arrow).antarchy$setShakeTime(0);
            arrow.setNoPhysics(false);
        }
    }

    private PortalGunPortalEntity findLinkedPortal(ServerLevel level) {
        if (this.linkedPortalId == null) {
            return null;
        }
        Entity entity = level.getEntity(this.linkedPortalId);
        return entity instanceof PortalGunPortalEntity portal ? portal : null;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getWorldPortalShape().getBoundsForCulling();
    }

    @Override
    protected AABB makeBoundingBox() {
        return this.getPortalInsides();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (this.ownerId != null) {
            tag.putUUID("OwnerId", this.ownerId);
        }
        if (this.linkedPortalId != null) {
            tag.putUUID("LinkedPortalId", this.linkedPortalId);
        }
        tag.putInt("AgeTicks", this.ageTicks);
        tag.putInt("PairTime", this.pairTime);
        tag.putInt("Side", this.getPortalSide().ordinal());
        tag.putInt("Facing", this.getFacingDirection().get3DDataValue());
        tag.putInt("UpAxis", this.getUpAxis().get3DDataValue());
        tag.putInt("ChannelRed", this.getChannelRed());
        tag.putInt("ChannelGreen", this.getChannelGreen());
        tag.putInt("ChannelBlue", this.getChannelBlue());
        tag.putInt("SupportX", this.supportOrigin.getX());
        tag.putInt("SupportY", this.supportOrigin.getY());
        tag.putInt("SupportZ", this.supportOrigin.getZ());
        tag.putInt("MasterX", this.masterPos.getX());
        tag.putInt("MasterY", this.masterPos.getY());
        tag.putInt("MasterZ", this.masterPos.getZ());
        tag.putInt("BaseX", this.basePos.getX());
        tag.putInt("BaseY", this.basePos.getY());
        tag.putInt("BaseZ", this.basePos.getZ());
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
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerId")) {
            this.ownerId = tag.getUUID("OwnerId");
        }
        if (tag.hasUUID("LinkedPortalId")) {
            this.linkedPortalId = tag.getUUID("LinkedPortalId");
            this.entityData.set(LINKED_PORTAL, Optional.of(this.linkedPortalId));
        }
        this.ageTicks = tag.getInt("AgeTicks");
        this.pairTime = tag.getInt("PairTime");
        this.entityData.set(SIDE, tag.getInt("Side"));
        this.entityData.set(FACING, tag.getInt("Facing"));
        this.entityData.set(UP_AXIS, tag.getInt("UpAxis"));
        if (tag.contains("ChannelRed")) {
            this.entityData.set(CHANNEL_RED, tag.getInt("ChannelRed"));
            this.entityData.set(CHANNEL_GREEN, tag.getInt("ChannelGreen"));
            this.entityData.set(CHANNEL_BLUE, tag.getInt("ChannelBlue"));
        } else if (this.ownerId != null) {
            int[] channelColor = generateChannelColor(this.ownerId, this.getPortalSide());
            this.entityData.set(CHANNEL_RED, channelColor[0]);
            this.entityData.set(CHANNEL_GREEN, channelColor[1]);
            this.entityData.set(CHANNEL_BLUE, channelColor[2]);
        }
        this.supportOrigin = new BlockPos(tag.getInt("SupportX"), tag.getInt("SupportY"), tag.getInt("SupportZ"));
        this.masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        this.basePos = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
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
    public void remove(Entity.RemovalReason reason) {
        Level level = this.level();
        UUID ownerId = this.ownerId;
        super.remove(reason);
        if (!level.isClientSide && ownerId != null && level instanceof ServerLevel serverLevel) {
            PortalGunSavedData.clearPortal(serverLevel.getServer(), ownerId, this.getPortalSide(), this.getUUID());
            this.clearPortalBlocks(serverLevel);
        }
    }

    private void clearPortalBlocks(ServerLevel level) {
        for (BlockPos portalSpot : this.portalSpots) {
            if (level.getBlockEntity(portalSpot) instanceof PortalGunPortalMasterBlockEntity master && this.getUUID().equals(master.getPortalId())) {
                level.removeBlock(portalSpot, false);
                continue;
            }
            if (level.getBlockEntity(portalSpot) instanceof PortalGunPortalBaseBlockEntity base && this.getUUID().equals(base.getPortalId())) {
                level.removeBlock(portalSpot, false);
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "portal_controller", 0, this::portalController));
    }

    private PlayState portalController(AnimationState<PortalGunPortalEntity> state) {
        state.getController().setAnimationSpeed(this.entityData.get(CLOSING) || this.ageTicks < OPEN_TICKS ? 4.0D : 1.0D);
        if (this.entityData.get(CLOSING)) {
            return state.setAndContinue(CLOSE_ANIM);
        }
        if (this.ageTicks < OPEN_TICKS) {
            return state.setAndContinue(OPEN_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static SoundEvent sound(String path, SoundEvent fallback) {
        return BuiltInRegistries.SOUND_EVENT.getOptional(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path)).orElse(fallback);
    }

    private static int[] generateChannelColor(UUID ownerId, PortalSide side) {
        int seed = ownerId == null ? side.ordinal() : ownerId.hashCode() ^ side.ordinal() * 0x45d9f3b;
        int wobbleA = Math.floorMod(seed, 54);
        int wobbleB = Math.floorMod(seed >> 8, 46);
        int wobbleC = Math.floorMod(seed >> 16, 38);
        if (side == PortalSide.BLUE) {
            return new int[] {42 + wobbleC, 128 + wobbleB, 218 + wobbleA / 3};
        }
        return new int[] {230 + wobbleA / 5, 108 + wobbleB, 28 + wobbleC};
    }

    private record PortalLocalCoords(double horizontal, double vertical, double depth) {
    }

    private record PortalBounds(double minHorizontal, double maxHorizontal, double minVertical, double maxVertical, double minDepth, double maxDepth) {
    }
}
