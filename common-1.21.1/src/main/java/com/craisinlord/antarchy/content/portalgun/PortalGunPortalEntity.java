package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalCellAccess;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalFaceRecord;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.entity.BlockSuction;
import com.craisinlord.antarchy.mixins.AbstractArrowAccessor;
import com.craisinlord.antarchy.mixins.FallingBlockEntityAccessor;
import com.craisinlord.antarchy.mixins.ServerGamePacketListenerTeleportAccessor;
import com.craisinlord.antarchy.mixins.gravity.ChunkMapAccessor;
import com.craisinlord.antarchy.mixins.gravity.ServerEntityAccessor;
import com.craisinlord.antarchy.mixins.gravity.TrackedEntityAccessor;
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
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
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
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> GUN_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> UP_AXIS = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> LINKED_PORTAL = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> LINKED_PORTAL_ENTITY_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PORTAL_WIDTH = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PORTAL_HEIGHT = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OPENING_ANIMATION_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MOON = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.BOOLEAN);
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final int OPEN_TICKS = 5;
    public static final int TELEPORT_COOLDOWN_TICKS = 3;
    private static final String DEBUG_TRAVERSAL_PROPERTY = "antarchy.portalGun.debugTraversal";
    private static final double PORTAL_INSIDE_SHIFT = 0.05D;
    private static final double HALF_DEPTH = 0.35D;
    private static final double MAX_CLIENT_TRANSIT_START_ERROR_SQR = 1.0D;
    private static final double MAX_CLIENT_TRANSIT_MOVEMENT_SQR = 100.0D;
    private static final double PROJECTILE_PLANE_TOLERANCE = 0.1D;
    private static final double MOON_RANGE = 14.0D;
    private static final double MOON_SPREAD = 0.9D;
    private static final double MOON_PULL_STRENGTH = 0.15D;
    private static final double MOON_MAX_SPEED = 1.8D;
    private static final double MOON_MOUTH_DEPTH = 0.9D;
    private static final double MOON_HOLD_DEPTH = 0.65D;
    private static final int MOON_BLOCK_INTERVAL = 1;
    private static final int MOON_BLOCKS_PER_PULL = 4;
    private static final int MOON_BLOCK_RAYS = 16;
    private static final double MOON_RAY_STEP = 0.4D;
    private static final double MOON_BLOCK_SPEED = 0.6D;
    private static final int MOON_VACUUM_INTERVAL = 10;
    private static final float MOON_VACUUM_DAMAGE = 2.0F;
    private static final int MOON_WIND_INTERVAL = 40;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Integer> teleportCooldowns = new HashMap<>();
    private UUID ownerId;
    private UUID gunId;
    private UUID linkedPortalId;
    private int ageTicks;
    private int lastOpeningAnimationId;
    private int pairTime;
    private int moonTicks;
    private int moonSuckedBlocks;
    private BlockPos supportOrigin = BlockPos.ZERO;
    private BlockPos masterPos = BlockPos.ZERO;
    private BlockPos basePos = BlockPos.ZERO;
    private BlockPos[] portalSpots = new BlockPos[] {BlockPos.ZERO, BlockPos.ZERO};
    private Set<BlockPos> compensatedSpots = Set.of();
    private PortalGunWorldPortalShape cachedShape;
    private Vec3 cachedShapePosition;
    private int cachedShapeFacing;
    private int cachedShapeUpAxis;
    private int cachedShapeWidth;
    private int cachedShapeHeight;

    public PortalGunPortalEntity(EntityType<? extends PortalGunPortalEntity> entityType, Level level) {
        super(entityType, level);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.noCulling = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIDE, PortalSide.BLUE.ordinal());
        builder.define(OWNER_ID, Optional.empty());
        builder.define(GUN_ID, Optional.empty());
        builder.define(FACING, Direction.NORTH.get3DDataValue());
        builder.define(UP_AXIS, Direction.UP.get3DDataValue());
        builder.define(LINKED_PORTAL, Optional.empty());
        builder.define(LINKED_PORTAL_ENTITY_ID, -1);
        builder.define(VARIANT, PortalGunVariant.DEFAULT.id());
        builder.define(PORTAL_WIDTH, 1);
        builder.define(PORTAL_HEIGHT, 2);
        builder.define(OPENING_ANIMATION_ID, 0);
        builder.define(MOON, false);
    }

    public void configure(UUID ownerId, UUID gunId, PortalGunVariant variant, PortalSide side, PortalGunPlacement placement) {
        this.ownerId = ownerId;
        this.gunId = gunId;
        this.entityData.set(OWNER_ID, Optional.ofNullable(ownerId));
        this.entityData.set(GUN_ID, Optional.ofNullable(gunId));
        this.entityData.set(VARIANT, (variant == null ? PortalGunVariant.DEFAULT : variant).id());
        this.entityData.set(SIDE, side.ordinal());
        this.entityData.set(FACING, placement.facing().get3DDataValue());
        this.entityData.set(UP_AXIS, placement.upAxis().get3DDataValue());
        this.entityData.set(PORTAL_WIDTH, placement.width());
        this.entityData.set(PORTAL_HEIGHT, placement.height());
        this.supportOrigin = placement.supportOrigin().immutable();
        this.masterPos = placement.masterPos().immutable();
        this.basePos = placement.basePos().immutable();
        this.portalSpots = java.util.Arrays.stream(placement.portalSpots()).map(BlockPos::immutable).toArray(BlockPos[]::new);
        this.compensatedSpots = Set.copyOf(placement.compensatedSpots());
    }

    public void linkTo(PortalGunPortalEntity other) {
        this.linkedPortalId = other.getUUID();
        this.entityData.set(LINKED_PORTAL, Optional.of(other.getUUID()));
        this.entityData.set(LINKED_PORTAL_ENTITY_ID, other.getId());
        this.pairTime = (int) this.level().getGameTime();
    }

    public void restorePair(UUID linkedPortalId, int pairTime) {
        this.linkedPortalId = linkedPortalId;
        this.entityData.set(LINKED_PORTAL, Optional.ofNullable(linkedPortalId));
        this.entityData.set(LINKED_PORTAL_ENTITY_ID, -1);
        this.pairTime = pairTime;
    }

    public void openToMoon() {
        this.linkedPortalId = null;
        this.entityData.set(LINKED_PORTAL, Optional.empty());
        this.entityData.set(LINKED_PORTAL_ENTITY_ID, -1);
        this.entityData.set(MOON, true);
        this.moonTicks = 0;
        this.moonSuckedBlocks = 0;
    }

    public boolean isMoonPortal() {
        return this.entityData.get(MOON);
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
        return this.entityData.get(OWNER_ID).orElse(this.ownerId);
    }

    public UUID getGunId() {
        return this.entityData.get(GUN_ID).orElse(this.gunId);
    }

    public PortalGunVariant getVariant() {
        return PortalGunVariant.byId(this.entityData.get(VARIANT));
    }

    public int getPortalColor() {
        return this.getVariant().color(this.getPortalSide());
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

    public int getPortalWidth() { return this.entityData.get(PORTAL_WIDTH); }

    public int getPortalHeight() { return this.entityData.get(PORTAL_HEIGHT); }

    public void restartOpeningAnimation() {
        int animationId = this.entityData.get(OPENING_ANIMATION_ID) + 1;
        this.entityData.set(OPENING_ANIMATION_ID, animationId);
        this.lastOpeningAnimationId = animationId;
        this.ageTicks = 0;
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
        if (this.level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(linkedId) instanceof PortalGunPortalEntity portal && !portal.isRemoved() ? portal : null;
        }
        return PortalGunPortalRegistry.find(this.level(), linkedId);
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
        Vec3 position = this.position();
        int facing = this.entityData.get(FACING);
        int upAxis = this.entityData.get(UP_AXIS);
        int width = this.getPortalWidth();
        int height = this.getPortalHeight();
        PortalGunWorldPortalShape shape = this.cachedShape;
        if (shape == null || !position.equals(this.cachedShapePosition) || facing != this.cachedShapeFacing
                || upAxis != this.cachedShapeUpAxis || width != this.cachedShapeWidth || height != this.cachedShapeHeight) {
            shape = new PortalGunWorldPortalShape(position, this.getNormalVec().normalize(), this.getUpVec().normalize(), this.getWidthVec().normalize(), width / 2.0D, height / 2.0D, HALF_DEPTH);
            this.cachedShape = shape;
            this.cachedShapePosition = position;
            this.cachedShapeFacing = facing;
            this.cachedShapeUpAxis = upAxis;
            this.cachedShapeWidth = width;
            this.cachedShapeHeight = height;
        }
        return shape;
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
        double shiftAmount = Math.min(PORTAL_INSIDE_SHIFT, Math.abs(this.getWorldPortalShape().localCoords(entity.position()).depth()));
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
        return entity.position().add(0.0D, this.getTeleportProbeHeight(entity), 0.0D);
    }

    public Vec3 teleportProbePosition(Entity entity, AABB bounds) {
        double x = (bounds.minX + bounds.maxX) * 0.5D;
        double y = bounds.minY;
        double z = (bounds.minZ + bounds.maxZ) * 0.5D;
        y += this.getTeleportProbeHeight(entity);
        return new Vec3(x, y, z);
    }

    private double getTeleportProbeHeight(Entity entity) {
        return entity instanceof LivingEntity living ? living.getEyeHeight(living.getPose()) : entity.getEyeHeight();
    }

    public PortalCrossing resolveCrossing(Entity entity, AABB previousBox, AABB currentBox, Vec3 attemptedMovement) {
        PortalGunWorldPortalShape shape = this.getWorldPortalShape();
        if (!PortalGunTransitMath.crossesWithBody(shape, previousBox, currentBox, attemptedMovement)) {
            return null;
        }
        return new PortalCrossing(this.teleportProbePosition(entity, currentBox));
    }

    public PortalTransit computeTransit(PortalGunPortalEntity destination, AABB box, Vec3 velocity, Vec3 look) {
        PortalGunWorldPortalShape source = this.getWorldPortalShape();
        PortalGunWorldPortalShape target = destination.getWorldPortalShape();
        AABB exitBox = PortalGunTransitMath.exitBox(source, target, box);
        Vec3 exitVelocity = PortalGunTransitMath.exitVelocity(source, target, velocity,
                AntarchySettings.portalGunMinFloorExitSpeed(), AntarchySettings.portalGunMaxExitSpeed());
        Vec3 exitLook = PortalGunTransitMath.transformVector(source, target, look).normalize();
        return new PortalTransit(exitBox, PortalGunTransitMath.feetPosition(exitBox), exitVelocity, exitLook);
    }

    public PortalTransit computeTransit(Entity entity, PortalGunPortalEntity destination, AABB box, Vec3 velocity, Vec3 look) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return computeTransit(destination, box, velocity, look);
        }
        PortalGunWorldPortalShape source = this.getWorldPortalShape();
        PortalGunWorldPortalShape target = destination.getWorldPortalShape();
        AABB exitBox = PortalGunTransitMath.exitBox(source, target, box);
        Vec3 worldVelocity = PortalGunTransitMath.entityVelocityToWorld(entity, velocity);
        Vec3 worldExitVelocity = PortalGunTransitMath.exitVelocity(source, target, worldVelocity,
                AntarchySettings.portalGunMinFloorExitSpeed(), AntarchySettings.portalGunMaxExitSpeed());
        Vec3 exitVelocity = PortalGunTransitMath.entityVelocityFromWorld(entity, worldExitVelocity);
        Vec3 worldLook = PortalGunTransitMath.entityVelocityToWorld(entity, look);
        Vec3 worldExitLook = PortalGunTransitMath.transformVector(source, target, worldLook).normalize();
        Vec3 exitLook = PortalGunTransitMath.entityVelocityFromWorld(entity, worldExitLook);
        return new PortalTransit(exitBox, PortalGunTransitMath.entityPosition(exitBox, entity), exitVelocity, exitLook);
    }

    public static void teleportEntityAfterMovement(Entity entity, AABB previousBox, Vec3 velocity) {
        if (entity instanceof Player) {
            return;
        }
        teleportAfterMovement(entity, previousBox, entity.getBoundingBox(), velocity);
    }

    public static void teleportPlayerAfterMovementPacket(ServerPlayer player, AABB previousBox, Vec3 claimedMovement) {
        Vec3 transitVelocity = PortalGunTransitMath.entityVelocityFromWorld(player, claimedMovement);
        teleportAfterMovement(player, previousBox, previousBox.move(claimedMovement), transitVelocity, claimedMovement);
    }

    private static void teleportAfterMovement(Entity entity, AABB previousBox, AABB currentBox, Vec3 velocity) {
        teleportAfterMovement(entity, previousBox, currentBox, velocity,
                PortalGunTransitMath.entityVelocityToWorld(entity, velocity));
    }

    private static void teleportAfterMovement(Entity entity, AABB previousBox, AABB currentBox, Vec3 velocity, Vec3 attemptedMovement) {
        if (entity.level().isClientSide || !entity.isAlive() || entity.isPassenger() || entity instanceof PortalGunPortalEntity) {
            return;
        }
        if (velocity.lengthSqr() <= 1.0E-12D && previousBox.getCenter().distanceToSqr(currentBox.getCenter()) <= 1.0E-10D) {
            return;
        }
        AABB sweptBounds = previousBox.minmax(currentBox).expandTowards(attemptedMovement).inflate(1.0D);
        for (PortalGunPortalEntity portal : findPortalsNearBounds(entity.level(), sweptBounds)) {
            if (!(portal.level() instanceof ServerLevel serverLevel) || !portal.canTeleportEntity(entity)) {
                continue;
            }
            PortalGunPortalEntity linked = portal.findLinkedPortal(serverLevel);
            if (linked == null || !linked.isAlive()) {
                continue;
            }
            PortalCrossing crossing = portal.resolveCrossing(entity, previousBox, currentBox, attemptedMovement);
            if (crossing != null) {
                if (entity instanceof Player && Boolean.getBoolean(DEBUG_TRAVERSAL_PROPERTY)) {
                    Antarchy.LOGGER.info("Portal gun traversal accepted path=server portal={} player={} probe={} linked={}",
                            portal.getUUID(), entity.getUUID(), crossing.probe(), linked.getUUID());
                }
                portal.teleportEntity(entity, linked, currentBox, velocity, false);
                return;
            }
        }
    }

    public static boolean handleClientTransit(ServerPlayer player, int portalId, Vec3 start, Vec3 end, Vec3 velocity) {
        if (!player.isAlive() || player.isPassenger() || player.isSpectator()) {
            return false;
        }
        if (((ServerGamePacketListenerTeleportAccessor) player.connection).antarchy$getAwaitingPositionFromClient() != null) {
            return true;
        }
        if (!isFinite(start) || !isFinite(end) || !isFinite(velocity)
                || start.distanceToSqr(player.position()) > MAX_CLIENT_TRANSIT_START_ERROR_SQR
                || end.distanceToSqr(start) > MAX_CLIENT_TRANSIT_MOVEMENT_SQR
                || velocity.lengthSqr() > MAX_CLIENT_TRANSIT_MOVEMENT_SQR) {
            return false;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)
                || !(serverLevel.getEntity(portalId) instanceof PortalGunPortalEntity portal)
                || !portal.isAlive()) {
            return false;
        }
        PortalGunPortalEntity linked = portal.findLinkedPortal(serverLevel);
        if (linked == null || !linked.isAlive()) {
            return false;
        }
        AABB startBox = player.getBoundingBox().move(start.subtract(player.position()));
        AABB endBox = startBox.move(end.subtract(start));
        Vec3 crossingVelocity = PortalGunTransitMath.entityVelocityToWorld(player, velocity);
        if (portal.resolveCrossing(player, startBox, endBox, crossingVelocity) == null) {
            return false;
        }
        if (Boolean.getBoolean(DEBUG_TRAVERSAL_PROPERTY)) {
            Antarchy.LOGGER.info("Portal gun traversal accepted path=client portal={} player={} end={} velocity={} linked={}",
                    portal.getUUID(), player.getUUID(), end, velocity, linked.getUUID());
        }
        portal.teleportEntity(player, linked, endBox, velocity, true);
        return true;
    }

    public static boolean passProjectileThroughPortal(Projectile projectile, BlockHitResult hit) {
        if (projectile instanceof PortalGunProjectileEntity || projectile.isRemoved() || projectile.isPassenger()) {
            return false;
        }
        Vec3 location = hit.getLocation();
        Vec3 velocity = projectile.getDeltaMovement();
        for (PortalGunPortalEntity portal : findPortalsNearBounds(projectile.level(), new AABB(location, location).inflate(0.5D))) {
            if (portal.getFacingDirection() != hit.getDirection() || portal.isTeleportCoolingDown(projectile)) {
                continue;
            }
            PortalGunWorldPortalShape shape = portal.getWorldPortalShape();
            PortalGunWorldPortalShape.PortalLocalCoords coords = shape.localCoords(location);
            if (Math.abs(coords.depth()) > PROJECTILE_PLANE_TOLERANCE
                    || Math.abs(coords.horizontal()) > shape.halfWidth()
                    || Math.abs(coords.vertical()) > shape.halfHeight()
                    || velocity.dot(shape.normal()) >= 0.0D) {
                continue;
            }
            PortalGunPortalEntity linked = projectile.level() instanceof ServerLevel serverLevel ? portal.findLinkedPortal(serverLevel) : portal.getLinkedPortal();
            if (linked == null || !linked.isAlive()) {
                continue;
            }
            AABB crossingBox = projectile.getBoundingBox().move(location.subtract(projectile.position()));
            if (projectile.level().isClientSide) {
                PortalTransit transit = portal.computeTransit(projectile, linked, crossingBox, velocity, projectile.getLookAngle());
                Vec3 exit = transit.position();
                projectile.setPos(exit.x, exit.y, exit.z);
                projectile.xo = exit.x;
                projectile.yo = exit.y;
                projectile.zo = exit.z;
                projectile.setDeltaMovement(transit.velocity());
                orientAlongVelocity(projectile, transit.velocity());
                projectile.setPortalCooldown(TELEPORT_COOLDOWN_TICKS);
            } else {
                portal.teleportEntity(projectile, linked, crossingBox, velocity, false);
            }
            return true;
        }
        return false;
    }

    private static void orientAlongVelocity(Entity entity, Vec3 velocity) {
        if (velocity.lengthSqr() < 1.0E-12D) {
            return;
        }
        float yaw = (float) Math.toDegrees(Math.atan2(velocity.x, velocity.z));
        float pitch = (float) Math.toDegrees(Math.atan2(velocity.y, velocity.horizontalDistance()));
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yRotO = yaw;
        entity.xRotO = pitch;
    }

    private static boolean isFinite(Vec3 vector) {
        return Double.isFinite(vector.x) && Double.isFinite(vector.y) && Double.isFinite(vector.z);
    }

    public static List<PortalGunPortalEntity> findPortalsNearBounds(Level level, AABB bounds) {
        List<PortalGunPortalEntity> portals = null;
        for (PortalGunPortalEntity portal : PortalGunPortalRegistry.all(level)) {
            if (portal.isAlive() && portal.getWorldPortalShape().getBoundsForCulling().intersects(bounds)) {
                if (portals == null) {
                    portals = new java.util.ArrayList<>(2);
                }
                portals.add(portal);
            }
        }
        return portals == null ? List.of() : portals;
    }

    @Override
    public void setLevelCallback(net.minecraft.world.level.entity.EntityInLevelCallback callback) {
        super.setLevelCallback(callback);
        if (callback == net.minecraft.world.level.entity.EntityInLevelCallback.NULL) {
            PortalGunPortalRegistry.untrack(this);
        } else {
            PortalGunPortalRegistry.track(this);
        }
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

    public float getPortalVisualScale(float partialTick) {
        return Mth.clamp((this.ageTicks - 1.0F + partialTick) / OPEN_TICKS, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isRemoved()) {
            PortalGunPortalRegistry.track(this);
        }
        int openingAnimationId = this.entityData.get(OPENING_ANIMATION_ID);
        if (openingAnimationId != this.lastOpeningAnimationId) {
            this.lastOpeningAnimationId = openingAnimationId;
            this.ageTicks = 0;
        } else {
            this.ageTicks++;
        }
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        if (this.level().isClientSide) {
            if (this.isMoonPortal()) {
                this.spawnMoonParticles();
            }
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.tickTeleportCooldowns();
        if (this.gunId == null || !this.isStillRegistered(serverLevel)) {
            this.discard();
            return;
        }
        if (!this.hasValidSurface()) {
            this.discard();
            return;
        }
        if (this.isMoonPortal()) {
            this.tickMoon(serverLevel);
            return;
        }
        PortalGunPortalEntity linked = this.findLinkedPortal(serverLevel);
        if (linked == null || !linked.isAlive()) {
            if (Boolean.getBoolean(DEBUG_TRAVERSAL_PROPERTY) && this.ageTicks % 40 == 0) {
                Antarchy.LOGGER.info("Portal gun traversal unavailable portal={} side={} linked={} loaded={}",
                        this.getUUID(), this.getPortalSide(), this.linkedPortalId, linked != null);
            }
            if (this.ageTicks % 100 == 0) {
                Antarchy.LOGGER.debug("Portal gun portal has no loaded linked portal portal={} owner={} side={} linked={}", this.getUUID(), this.ownerId, this.getPortalSide(), this.linkedPortalId);
            }
            return;
        }
        if (this.ageTicks % 80 == 0) {
            this.level().playSound(null, this.blockPosition(), sound("portal_ambient"), SoundSource.BLOCKS, 0.18F, this.getPortalSide() == PortalSide.BLUE ? 1.05F : 0.92F);
        }
        this.tickTeleport(linked);
    }

    private void tickMoon(ServerLevel level) {
        this.moonTicks++;
        if (this.moonTicks >= AntarchySettings.portalGunMoonPortalSeconds() * 20) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), sound("portal_fizzle"), SoundSource.BLOCKS, 1.0F, 0.6F);
            this.discard();
            return;
        }
        if (this.moonTicks % MOON_WIND_INTERVAL == 1) {
            level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 1.4F, 0.55F);
        }
        PortalGunWorldPortalShape shape = this.getWorldPortalShape();
        Player owner = this.getOwnerId() == null ? null : level.getPlayerByUUID(this.getOwnerId());
        this.pullMoonEntities(level, shape, owner);
        if (AntarchySettings.portalGunMoonPullsBlocks() && this.moonTicks % MOON_BLOCK_INTERVAL == 0) {
            this.pullMoonBlocks(level, shape, owner);
        }
    }

    private static double moonLateralLimit(PortalGunWorldPortalShape shape, double depth) {
        return Math.max(shape.halfWidth(), shape.halfHeight()) + 0.5D + Math.max(0.0D, depth) * MOON_SPREAD;
    }

    private void pullMoonEntities(ServerLevel level, PortalGunWorldPortalShape shape, Player owner) {
        Vec3 center = shape.center();
        AABB area = new AABB(center, center.add(shape.normal().scale(MOON_RANGE))).inflate(moonLateralLimit(shape, MOON_RANGE));
        Vec3 target = center.add(shape.normal().scale(0.3D));
        boolean vacuumTick = this.moonTicks % MOON_VACUUM_INTERVAL == 0;
        for (Entity entity : level.getEntities(this, area, this::canMoonAffect)) {
            Vec3 position = entity.getBoundingBox().getCenter();
            PortalGunWorldPortalShape.PortalLocalCoords local = shape.localCoords(position);
            double depth = local.depth();
            double lateral = Math.sqrt(local.horizontal() * local.horizontal() + local.vertical() * local.vertical());
            if (depth < -0.5D || depth > MOON_RANGE || lateral > moonLateralLimit(shape, depth)) {
                continue;
            }
            boolean atMouth = depth <= MOON_MOUTH_DEPTH + entity.getBbWidth() * 0.5D
                    && Math.abs(local.horizontal()) <= shape.halfWidth() + 0.25D
                    && Math.abs(local.vertical()) <= shape.halfHeight() + 0.25D;
            if (atMouth) {
                if (!this.swallowIntoSpace(level, entity, owner)) {
                    this.holdInVacuum(level, entity, shape, owner, vacuumTick);
                }
                continue;
            }
            double falloff = 1.0D - depth / MOON_RANGE;
            double strength = MOON_PULL_STRENGTH * (0.35D + falloff * falloff * 1.6D);
            Vec3 worldMotion = PortalGunTransitMath.entityVelocityToWorld(entity, entity.getDeltaMovement())
                    .scale(0.92D)
                    .add(target.subtract(position).normalize().scale(strength));
            if (worldMotion.length() > MOON_MAX_SPEED) {
                worldMotion = worldMotion.normalize().scale(MOON_MAX_SPEED);
            }
            applyMoonMotion(entity, worldMotion);
        }
    }

    private boolean canMoonAffect(Entity entity) {
        if (!entity.isAlive() || entity.isPassenger() || entity.isSpectator()
                || entity instanceof PortalGunPortalEntity || entity instanceof PortalGunProjectileEntity) {
            return false;
        }
        if (entity instanceof Player player && player.isCreative()) {
            return false;
        }
        return entity instanceof LivingEntity || entity instanceof ItemEntity || entity instanceof ExperienceOrb
                || entity instanceof FallingBlockEntity || entity instanceof Projectile
                || entity instanceof VehicleEntity || entity instanceof PrimedTnt;
    }

    private boolean swallowIntoSpace(ServerLevel level, Entity entity, Player owner) {
        if (entity instanceof Player || entity instanceof VehicleEntity) {
            return false;
        }
        if (entity instanceof LivingEntity living) {
            living.hurt(AntarchyDamageSources.moonVacuum(level, owner), Float.MAX_VALUE);
            return !living.isAlive();
        }
        entity.discard();
        return true;
    }

    private void holdInVacuum(ServerLevel level, Entity entity, PortalGunWorldPortalShape shape, Player owner, boolean vacuumTick) {
        Vec3 hold = shape.center().add(shape.normal().scale(MOON_HOLD_DEPTH + entity.getBbWidth() * 0.5D));
        applyMoonMotion(entity, hold.subtract(entity.getBoundingBox().getCenter()).scale(0.45D));
        if (vacuumTick && entity instanceof LivingEntity living) {
            living.hurt(AntarchyDamageSources.moonVacuum(level, owner == entity ? null : owner), MOON_VACUUM_DAMAGE);
        }
    }

    private static void applyMoonMotion(Entity entity, Vec3 worldMotion) {
        entity.setDeltaMovement(PortalGunTransitMath.entityVelocityFromWorld(entity, worldMotion));
        entity.hasImpulse = true;
        entity.fallDistance = 0.0F;
        if (entity instanceof LivingEntity) {
            entity.hurtMarked = true;
        }
    }

    private void pullMoonBlocks(ServerLevel level, PortalGunWorldPortalShape shape, Player owner) {
        int cap = AntarchySettings.portalGunMoonBlockCap();
        int pulled = 0;
        for (int ray = 0; ray < MOON_BLOCK_RAYS && pulled < MOON_BLOCKS_PER_PULL && this.moonSuckedBlocks < cap; ray++) {
            double limit = moonLateralLimit(shape, MOON_RANGE);
            double horizontal = (this.random.nextDouble() * 2.0D - 1.0D) * limit;
            double vertical = (this.random.nextDouble() * 2.0D - 1.0D) * limit;
            if (horizontal * horizontal + vertical * vertical > limit * limit) {
                continue;
            }
            Vec3 start = shape.center()
                    .add(shape.right().scale((this.random.nextDouble() * 2.0D - 1.0D) * shape.halfWidth()))
                    .add(shape.up().scale((this.random.nextDouble() * 2.0D - 1.0D) * shape.halfHeight()))
                    .add(shape.normal().scale(0.5D));
            Vec3 end = shape.center()
                    .add(shape.right().scale(horizontal))
                    .add(shape.up().scale(vertical))
                    .add(shape.normal().scale(MOON_RANGE));
            BlockPos hit = this.firstBlockAlong(level, shape, start, end);
            if (hit == null) {
                continue;
            }
            BlockState state = loadedState(level, hit);
            if (state == null || !BlockSuction.canLift(level, hit, state, true)
                    || owner != null && !level.mayInteract(owner, hit)) {
                continue;
            }
            FallingBlockEntity falling = BlockSuction.lift(level, hit, state, shape.center(), MOON_BLOCK_SPEED);
            if (falling == null) {
                continue;
            }
            falling.setNoGravity(true);
            pulled++;
            this.moonSuckedBlocks++;
        }
    }

    private BlockPos firstBlockAlong(ServerLevel level, PortalGunWorldPortalShape shape, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(delta.length() / MOON_RAY_STEP));
        BlockPos previous = null;
        for (int step = 0; step <= steps; step++) {
            BlockPos pos = BlockPos.containing(start.add(delta.scale((double) step / steps)));
            if (pos.equals(previous)) {
                continue;
            }
            previous = pos;
            if (shape.localCoords(Vec3.atCenterOf(pos)).depth() < 0.5D) {
                continue;
            }
            if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
                return null;
            }
            BlockState state = loadedState(level, pos);
            if (state == null) {
                return null;
            }
            if (!state.isAir()) {
                return pos;
            }
        }
        return null;
    }

    private static BlockState loadedState(ServerLevel level, BlockPos pos) {
        net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunkSource()
                .getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        return chunk == null ? null : chunk.getBlockState(pos);
    }

    private void spawnMoonParticles() {
        PortalGunWorldPortalShape shape = this.getWorldPortalShape();
        Vec3 center = shape.center();
        for (int i = 0; i < 4; i++) {
            double depth = 1.0D + this.random.nextDouble() * MOON_RANGE * 0.6D;
            double limit = moonLateralLimit(shape, depth);
            Vec3 start = center
                    .add(shape.right().scale((this.random.nextDouble() * 2.0D - 1.0D) * limit))
                    .add(shape.up().scale((this.random.nextDouble() * 2.0D - 1.0D) * limit))
                    .add(shape.normal().scale(depth));
            Vec3 velocity = center.subtract(start).normalize().scale(0.35D + this.random.nextDouble() * 0.3D);
            this.level().addParticle(ParticleTypes.CLOUD, start.x, start.y, start.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private boolean isStillRegistered(ServerLevel level) {
        return PortalGunSavedData.isRegistered(level, this.gunId, this.getPortalSide(), this.getUUID());
    }

    private boolean hasValidSurface() {
        Direction facing = this.getFacingDirection();
        if (!PortalGunPlacement.isRectangularFootprint(this.portalSpots, this.getFacingDirection(), this.getUpAxis(), this.getPortalWidth(), this.getPortalHeight())) {
            return false;
        }
        for (int i = 0; i < this.portalSpots.length; i++) {
            BlockPos portalSpot = this.portalSpots[i];
            BlockPos supportPos = portalSpot.relative(facing.getOpposite());
            BlockState supportState = this.level().getBlockState(supportPos);
            if (!supportState.isFaceSturdy(this.level(), supportPos, facing)) {
                return false;
            }
            BlockEntity blockEntity = this.level().getBlockEntity(portalSpot);
              if (!(blockEntity instanceof PortalGunPortalCellAccess cell)) {
                  return false;
              }
              PortalGunPortalFaceRecord record = cell.portalGun$getFaceRecord(facing, this.getUUID());
              if (record != null) {
                  if (!this.ownerId.equals(record.ownerId()) || record.side() != this.getPortalSide() || record.master() != (i == 0)) {
                      return false;
                  }
              } else if (i == 0) {
                  if (!(blockEntity instanceof PortalGunPortalMasterBlockEntity master) || !master.matches(this.ownerId, this.getUUID(), this.getPortalSide())) {
                      return false;
                  }
              } else if (!(blockEntity instanceof PortalGunPortalBaseBlockEntity base) || !base.matches(this.ownerId, this.getUUID(), this.getPortalSide())) {
                  return false;
              }
        }
        return true;
    }

    private void tickTeleport(PortalGunPortalEntity linked) {
        List<Entity> entities = this.level().getEntities(this, this.getScanRange(), entity -> !(entity instanceof Player) && this.canTeleportEntity(entity));
        for (Entity entity : entities) {
            AABB currentBox = entity.getBoundingBox();
            AABB previousBox = currentBox.move(entity.xo - entity.getX(), entity.yo - entity.getY(), entity.zo - entity.getZ());
            PortalCrossing crossing = this.resolveCrossing(entity, previousBox, currentBox, entity.getDeltaMovement());
            if (crossing == null) {
                continue;
            }
            this.teleportEntity(entity, linked, currentBox, entity.getDeltaMovement(), false);
        }
    }

    private boolean canTeleportEntity(Entity entity) {
        if (!entity.isAlive() || entity instanceof PortalGunPortalEntity || entity.isPassenger()) {
            return false;
        }
        return !this.isTeleportCoolingDown(entity);
    }

    public boolean isTeleportCoolingDown(Entity entity) {
        return this.teleportCooldowns.containsKey(entity.getUUID()) || entity.getPortalCooldown() > 0;
    }

    private void teleportEntity(Entity entity, PortalGunPortalEntity destination, AABB crossingBox, Vec3 velocity, boolean clientAuthoritative) {
        List<EntityTeleportState> group = new java.util.ArrayList<>();
        collectTeleportGroup(entity, null, group);
        Vec3 rootOriginalPosition = entity.position();
        PortalTransit transit = this.computeTransit(entity, destination, crossingBox, velocity, entity.getLookAngle());
        Vec3 exitPos = transit.position();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound("portal_enter"), SoundSource.PLAYERS, 0.7F, 0.96F + this.random.nextFloat() * 0.08F);
        for (EntityTeleportState state : group) {
            state.entity().stopRiding();
        }
        for (EntityTeleportState state : group) {
            Entity member = state.entity();
            boolean root = member == entity;
            Vec3 memberExitPos = root
                    ? exitPos
                    : exitPos.add(PortalGunTransformUtil.transformVector(this, destination, state.position().subtract(rootOriginalPosition)));
            Vec3 memberLook;
            if (root) {
                memberLook = transit.look();
            } else if (AntarchyGravityApi.isGravityInverted(member)) {
                Vec3 worldLook = PortalGunTransitMath.entityVelocityToWorld(member, state.look());
                memberLook = PortalGunTransitMath.entityVelocityFromWorld(member,
                        PortalGunTransformUtil.transformVector(this, destination, worldLook).normalize());
            } else {
                memberLook = PortalGunTransformUtil.transformVector(this, destination, state.look()).normalize();
            }
            Vec3 memberMotion;
            if (root) {
                memberMotion = transit.velocity();
            } else if (AntarchyGravityApi.isGravityInverted(member)) {
                Vec3 worldMotion = PortalGunTransitMath.entityVelocityToWorld(member, state.motion());
                memberMotion = PortalGunTransitMath.entityVelocityFromWorld(member,
                        PortalGunTransformUtil.transformVector(this, destination, worldMotion));
            } else {
                memberMotion = PortalGunTransformUtil.transformVector(this, destination, state.motion());
            }
            float yaw = PortalGunTransformUtil.yawFromLook(memberLook);
            float pitch = PortalGunTransformUtil.pitchFromLook(memberLook);
            float fallDistance = PortalGunTransitMath.exitFallDistance(member.fallDistance,
                    AntarchyGravityApi.isGravityInverted(member) ? PortalGunTransitMath.entityVelocityToWorld(member, memberMotion) : memberMotion,
                    member);
            if (member instanceof ServerPlayer player && root && clientAuthoritative) {
                player.moveTo(memberExitPos.x, memberExitPos.y, memberExitPos.z, yaw, pitch);
                player.setYHeadRot(yaw);
                player.setDeltaMovement(memberMotion);
                player.connection.resetPosition();
                player.serverLevel().getChunkSource().move(player);
            } else if (member instanceof ServerPlayer player && destination.level() instanceof ServerLevel destinationLevel) {
                player.teleportTo(destinationLevel, memberExitPos.x, memberExitPos.y, memberExitPos.z, yaw, pitch);
                player.setDeltaMovement(memberMotion);
                player.hasImpulse = true;
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            } else {
                member.teleportTo(memberExitPos.x, memberExitPos.y, memberExitPos.z);
                member.setYRot(yaw);
                member.setXRot(pitch);
                member.setYHeadRot(yaw);
                member.setYBodyRot(yaw);
                member.setDeltaMovement(memberMotion);
                member.hasImpulse = true;
                member.hurtMarked = true;
            }
            member.xo = member.getX();
            member.yo = member.getY();
            member.zo = member.getZ();
            member.yRotO = yaw;
            member.xRotO = pitch;
            if (member instanceof Projectile) {
                orientAlongVelocity(member, memberMotion);
            }
            member.setOnGround(false);
            member.horizontalCollision = false;
            member.verticalCollision = false;
            member.verticalCollisionBelow = false;
            member.fallDistance = fallDistance;
            this.applyTeleportCooldown(member, destination);
            this.handleSpecialEntityPostTeleport(member);
        }
        for (EntityTeleportState state : group) {
            if (state.vehicle() != null && state.entity().isAlive()) {
                state.entity().startRiding(state.vehicle(), true);
            }
        }
        if (destination.level() instanceof ServerLevel destinationLevel) {
            for (EntityTeleportState state : group) {
                Entity member = state.entity();
                if (!(member instanceof ServerPlayer)) {
                    destinationLevel.getChunkSource().broadcastAndSend(member, new ClientboundTeleportEntityPacket(member));
                    destinationLevel.getChunkSource().broadcastAndSend(member, new ClientboundSetEntityMotionPacket(member));
                    syncTrackedPosition(destinationLevel, member);
                }
            }
        }
        destination.level().playSound(null, exitPos.x, exitPos.y, exitPos.z, sound("portal_exit"), SoundSource.PLAYERS, 0.7F, 0.96F + this.random.nextFloat() * 0.08F);
    }

    private static void syncTrackedPosition(ServerLevel level, Entity entity) {
        Object tracked = ((ChunkMapAccessor) level.getChunkSource().chunkMap).antarchy$getEntityMap().get(entity.getId());
        if (tracked instanceof TrackedEntityAccessor trackedEntity) {
            ServerEntityAccessor serverEntity = (ServerEntityAccessor) trackedEntity.antarchy$getServerEntity();
            serverEntity.antarchy$getPositionCodec().setBase(entity.trackingPosition());
            serverEntity.antarchy$setLastSentMovement(entity.getDeltaMovement());
        }
    }

    private void collectTeleportGroup(Entity entity, Entity vehicle, List<EntityTeleportState> group) {
        group.add(new EntityTeleportState(entity, vehicle, entity.position(), entity.getLookAngle(), entity.getDeltaMovement()));
        for (Entity passenger : entity.getPassengers()) {
            collectTeleportGroup(passenger, entity, group);
        }
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
        entity.setPortalCooldown(TELEPORT_COOLDOWN_TICKS);
    }

    private void handleSpecialEntityPostTeleport(Entity entity) {
        if (entity instanceof FallingBlockEntity) {
            ((FallingBlockEntityAccessor) entity).antarchy$setTime(2);
        }
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
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
        if (this.linkedPortalId != null) {
            tag.putUUID("LinkedPortalId", this.linkedPortalId);
        }
        tag.putInt("AgeTicks", this.ageTicks);
        tag.putInt("PairTime", this.pairTime);
        if (this.isMoonPortal()) {
            tag.putBoolean("Moon", true);
            tag.putInt("MoonTicks", this.moonTicks);
            tag.putInt("MoonSuckedBlocks", this.moonSuckedBlocks);
        }
        tag.putInt("Side", this.getPortalSide().ordinal());
        tag.putInt("Facing", this.getFacingDirection().get3DDataValue());
        tag.putInt("UpAxis", this.getUpAxis().get3DDataValue());
        tag.putInt("Variant", this.getVariant().id());
        tag.putInt("PortalWidth", this.getPortalWidth());
        tag.putInt("PortalHeight", this.getPortalHeight());
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
        this.gunId = tag.hasUUID("GunId") ? tag.getUUID("GunId") : null;
        this.entityData.set(OWNER_ID, Optional.ofNullable(this.ownerId));
        this.entityData.set(GUN_ID, Optional.ofNullable(this.gunId));
        if (tag.hasUUID("LinkedPortalId")) {
            this.linkedPortalId = tag.getUUID("LinkedPortalId");
            this.entityData.set(LINKED_PORTAL, Optional.of(this.linkedPortalId));
        }
        this.ageTicks = tag.getInt("AgeTicks");
        this.pairTime = tag.getInt("PairTime");
        this.entityData.set(MOON, tag.getBoolean("Moon"));
        this.moonTicks = tag.getInt("MoonTicks");
        this.moonSuckedBlocks = tag.getInt("MoonSuckedBlocks");
        this.entityData.set(SIDE, tag.getInt("Side"));
        this.entityData.set(FACING, tag.getInt("Facing"));
        this.entityData.set(UP_AXIS, tag.getInt("UpAxis"));
        this.entityData.set(VARIANT, PortalGunVariant.byId(tag.getInt("Variant")).id());
        this.supportOrigin = new BlockPos(tag.getInt("SupportX"), tag.getInt("SupportY"), tag.getInt("SupportZ"));
        this.masterPos = new BlockPos(tag.getInt("MasterX"), tag.getInt("MasterY"), tag.getInt("MasterZ"));
        this.basePos = new BlockPos(tag.getInt("BaseX"), tag.getInt("BaseY"), tag.getInt("BaseZ"));
        ListTag spots = tag.getList("PortalSpots", Tag.TAG_COMPOUND);
        if (!spots.isEmpty()) {
            this.portalSpots = new BlockPos[spots.size()];
        }
        for (int i = 0; i < spots.size(); i++) {
            CompoundTag spot = spots.getCompound(i);
            this.portalSpots[i] = new BlockPos(spot.getInt("X"), spot.getInt("Y"), spot.getInt("Z"));
        }
        this.entityData.set(PORTAL_WIDTH, Mth.clamp(tag.getInt("PortalWidth"), 1, 16));
        this.entityData.set(PORTAL_HEIGHT, Mth.clamp(tag.getInt("PortalHeight"), 2, 16));
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
        boolean destroyPortal = reason.shouldDestroy();
        if (destroyPortal && !level.isClientSide && ownerId != null && level instanceof ServerLevel serverLevel) {
            PortalGunSavedData.clearPortal(serverLevel.getServer(), this.gunId, this.getPortalSide(), this.getUUID(), serverLevel.dimension().location());
        }
        super.remove(reason);
        if (destroyPortal && !level.isClientSide && ownerId != null && level instanceof ServerLevel serverLevel) {
            this.clearPortalBlocks(serverLevel);
        }
    }

    private void clearPortalBlocks(ServerLevel level) {
        for (BlockPos portalSpot : this.portalSpots) {
            if (level.getBlockEntity(portalSpot) instanceof PortalGunPortalCellAccess cell) {
                PortalGunPortalFaceRecord removed = cell.portalGun$removeFaceRecord(this.getFacingDirection(), this.getUUID());
                if (!cell.portalGun$getFaceRecords().isEmpty()) {
                    continue;
                }
                if (cell instanceof PortalGunPortalMasterBlockEntity master && this.getUUID().equals(master.getPortalId())) {
                    level.removeBlock(portalSpot, false);
                } else if (cell instanceof PortalGunPortalBaseBlockEntity base && this.getUUID().equals(base.getPortalId())) {
                    level.removeBlock(portalSpot, false);
                } else if (removed != null) {
                    level.removeBlock(portalSpot, false);
                }
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
        state.getController().setAnimationSpeed(this.ageTicks < OPEN_TICKS ? 4.0D : 1.0D);
        if (this.ageTicks < OPEN_TICKS) {
            return state.setAndContinue(OPEN_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static SoundEvent sound(String path) {
        ResourceLocation soundId = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path);
        return BuiltInRegistries.SOUND_EVENT.getOptional(soundId)
                .orElseThrow(() -> new IllegalStateException("Missing portal gun sound event: " + soundId));
    }

    public record PortalCrossing(Vec3 probe) {
    }

    public record PortalTransit(AABB exitBox, Vec3 position, Vec3 velocity, Vec3 look) {
    }

    private record EntityTeleportState(Entity entity, Entity vehicle, Vec3 position, Vec3 look, Vec3 motion) {
    }
}
