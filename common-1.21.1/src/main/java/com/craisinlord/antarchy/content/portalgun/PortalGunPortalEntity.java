package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalCellAccess;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalFaceRecord;
import com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity;
import com.craisinlord.antarchy.mixins.AbstractArrowAccessor;
import com.craisinlord.antarchy.mixins.FallingBlockEntityAccessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.item.FallingBlockEntity;
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
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> GUN_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> CHANNEL_NAME = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> UP_AXIS = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> LINKED_PORTAL = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> LINKED_PORTAL_ENTITY_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHANNEL_RED = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHANNEL_GREEN = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHANNEL_BLUE = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PORTAL_WIDTH = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PORTAL_HEIGHT = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OPENING_ANIMATION_ID = SynchedEntityData.defineId(PortalGunPortalEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final int OPEN_TICKS = 5;
    public static final int TELEPORT_COOLDOWN_TICKS = 3;
    private static final String DEBUG_TRAVERSAL_PROPERTY = "antarchy.portalGun.debugTraversal";
    private static final double PORTAL_INSIDE_SHIFT = 0.05D;
    private static final Set<String> LOGGED_CROSSING_PATHS = new HashSet<>();
    private static final double HALF_DEPTH = 0.35D;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Map<UUID, Integer> teleportCooldowns = new HashMap<>();
    private UUID ownerId;
    private String ownerIdentity;
    private UUID gunId;
    private String channelName;
    private UUID linkedPortalId;
    private int ageTicks;
    private int lastOpeningAnimationId;
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
        builder.define(OWNER_ID, Optional.empty());
        builder.define(GUN_ID, Optional.empty());
        builder.define(CHANNEL_NAME, "");
        builder.define(FACING, Direction.NORTH.get3DDataValue());
        builder.define(UP_AXIS, Direction.UP.get3DDataValue());
        builder.define(LINKED_PORTAL, Optional.empty());
        builder.define(LINKED_PORTAL_ENTITY_ID, -1);
        builder.define(CHANNEL_RED, 58);
        builder.define(CHANNEL_GREEN, 166);
        builder.define(CHANNEL_BLUE, 255);
        builder.define(PORTAL_WIDTH, 1);
        builder.define(PORTAL_HEIGHT, 2);
        builder.define(OPENING_ANIMATION_ID, 0);
    }

    public void configure(UUID ownerId, UUID gunId, String ownerIdentity, String channelName, PortalSide side, PortalGunPlacement placement) {
        this.ownerId = ownerId;
        this.ownerIdentity = ownerIdentity;
        this.gunId = gunId;
        this.channelName = channelName;
        this.entityData.set(OWNER_ID, Optional.ofNullable(ownerId));
        this.entityData.set(GUN_ID, Optional.ofNullable(gunId));
        this.entityData.set(CHANNEL_NAME, channelName == null ? "" : channelName);
        this.entityData.set(SIDE, side.ordinal());
        this.entityData.set(FACING, placement.facing().get3DDataValue());
        this.entityData.set(UP_AXIS, placement.upAxis().get3DDataValue());
        this.entityData.set(PORTAL_WIDTH, placement.width());
        this.entityData.set(PORTAL_HEIGHT, placement.height());
        int[] channelColor = generateChannelColor(ownerIdentity, channelName, side);
        this.entityData.set(CHANNEL_RED, channelColor[0]);
        this.entityData.set(CHANNEL_GREEN, channelColor[1]);
        this.entityData.set(CHANNEL_BLUE, channelColor[2]);
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

    public String getChannelName() {
        String synchronizedName = this.entityData.get(CHANNEL_NAME);
        return synchronizedName.isEmpty() ? this.channelName : synchronizedName;
    }

    public void adoptChannelIdentity(UUID gunId, String channelName) {
        if (this.gunId == null && gunId != null) {
            this.gunId = gunId;
            this.entityData.set(GUN_ID, Optional.of(gunId));
        }
        if (channelName != null && !channelName.isEmpty() && !channelName.equals(this.channelName)) {
            this.channelName = channelName;
            this.entityData.set(CHANNEL_NAME, channelName);
        }
        if (this.level() instanceof ServerLevel serverLevel && serverLevel.hasChunkAt(this.masterPos)
                && serverLevel.getBlockEntity(this.masterPos) instanceof PortalGunPortalMasterBlockEntity master
                && this.getUUID().equals(master.getPortalId())) {
            master.adoptChannelIdentity(gunId, channelName);
        }
    }

    public void adoptGunId(UUID gunId) {
        if (this.gunId != null || gunId == null) {
            return;
        }
        this.gunId = gunId;
        this.entityData.set(GUN_ID, Optional.of(gunId));
        if (this.level() instanceof ServerLevel serverLevel && serverLevel.hasChunkAt(this.masterPos)
                && serverLevel.getBlockEntity(this.masterPos) instanceof com.craisinlord.antarchy.content.block.entity.PortalGunPortalMasterBlockEntity master
                && this.getUUID().equals(master.getPortalId())) {
            master.adoptGunId(gunId);
        }
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
        Entity linkedEntity;
        if (this.level() instanceof ServerLevel serverLevel) {
            linkedEntity = serverLevel.getEntity(linkedId);
        } else {
            int linkedEntityId = this.entityData.get(LINKED_PORTAL_ENTITY_ID);
            if (linkedEntityId < 0) {
                return null;
            }
            linkedEntity = this.level().getEntity(linkedEntityId);
        }
        return linkedEntity instanceof PortalGunPortalEntity portal && !portal.isRemoved() ? portal : null;
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
        return new PortalGunWorldPortalShape(this.position(), this.getNormalVec().normalize(), this.getUpVec().normalize(), this.getWidthVec().normalize(), this.getPortalWidth() / 2.0D, this.getPortalHeight() / 2.0D, HALF_DEPTH);
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

    public PortalCrossing resolveCrossing(Entity entity, AABB previousBox, AABB currentBox) {
        Vec3 previousProbe = this.teleportProbePosition(entity, previousBox);
        Vec3 currentProbe = this.teleportProbePosition(entity, currentBox);
        Vec3 normal = this.getNormalVec().normalize();
        if (entity instanceof Player && Math.abs(normal.y) < 0.5D) {
            PortalGunWorldPortalShape shape = this.getWorldPortalShape();
            double previousDepth = shape.localCoords(previousProbe).depth();
            double currentDepth = shape.localCoords(currentProbe).depth();
            double halfBodyDepth = entity.getBbWidth() * 0.5D;
            if (previousDepth <= 0.0D || currentDepth >= previousDepth - 1.0E-6D
                    || currentDepth - halfBodyDepth > 0.02D || currentDepth < -shape.halfDepth()) {
                return null;
            }
            Vec3 center = currentBox.getCenter();
            PortalGunWorldPortalShape.PortalLocalCoords localCenter = shape.localCoords(center);
            double halfX = (currentBox.maxX - currentBox.minX) * 0.5D;
            double halfY = (currentBox.maxY - currentBox.minY) * 0.5D;
            double halfZ = (currentBox.maxZ - currentBox.minZ) * 0.5D;
            double widthExtent = halfX * Math.abs(shape.right().x) + halfY * Math.abs(shape.right().y) + halfZ * Math.abs(shape.right().z);
            double heightExtent = halfX * Math.abs(shape.up().x) + halfY * Math.abs(shape.up().y) + halfZ * Math.abs(shape.up().z);
            if (Math.abs(localCenter.horizontal()) + widthExtent > shape.halfWidth() + 1.0E-3D
                    || Math.abs(localCenter.vertical()) + heightExtent > shape.halfHeight() + 1.0E-3D) {
                return null;
            }
            return new PortalCrossing(currentProbe, Math.max(0.05D, currentDepth + 0.05D));
        }
        return this.crossesPortal(previousProbe, currentProbe) ? new PortalCrossing(currentProbe, 0.0D) : null;
    }

    public Vec3 resolveCrossingProbe(Entity entity) {
        AABB currentBox = entity.getBoundingBox();
        AABB previousBox = currentBox.move(entity.xo - entity.getX(), entity.yo - entity.getY(), entity.zo - entity.getZ());
        PortalCrossing crossing = this.resolveCrossing(entity, previousBox, currentBox);
        return crossing == null ? null : crossing.probe();
    }

    public PortalTransitTransform previewTransit(Entity entity, PortalGunPortalEntity destination, PortalCrossing crossing, Vec3 movement) {
        Vec3 sourcePlaneCenter = this.position().add(this.getNormalVec().normalize().scale(crossing.normalOffset()));
        Vec3 destinationPlaneCenter = destination.position().add(destination.getNormalVec().normalize().scale(crossing.normalOffset()));
        Vec3 relativeProbe = crossing.probe().subtract(sourcePlaneCenter);
        Vec3 transformedProbe = PortalGunTransformUtil.transformPosition(this, destination, relativeProbe);
        Vec3 entityOffset = PortalGunTransformUtil.transformVector(this, destination, entity.position().subtract(crossing.probe()));
        Vec3 requestedPosition = destinationPlaneCenter.add(transformedProbe).add(entityOffset);
        Vec3 transformedMovement = PortalGunTransformUtil.transformVector(this, destination, movement);
        Vec3 exitPosition = putEntityWithinExitBounds(entity, requestedPosition, legacyExitBounds(destination, entity, destination.getNormalVec().normalize()));
        return new PortalTransitTransform(requestedPosition, exitPosition, transformedMovement);
    }

    public static void teleportEntityAfterMovement(Entity entity, AABB previousBox, Vec3 resolvedMovement) {
        if (entity.level().isClientSide || !entity.isAlive() || entity.isPassenger() || entity instanceof PortalGunPortalEntity) {
            return;
        }
        AABB currentBox = entity.getBoundingBox();
        if (previousBox.getCenter().distanceToSqr(currentBox.getCenter()) <= 1.0E-10D) {
            return;
        }
        AABB sweptBounds = previousBox.minmax(currentBox).inflate(1.0D);
        for (PortalGunPortalEntity portal : findPortalsNearBounds(entity.level(), sweptBounds)) {
            if (!(portal.level() instanceof ServerLevel serverLevel) || !portal.canTeleportEntity(entity)) {
                continue;
            }
            PortalGunPortalEntity linked = portal.findLinkedPortal(serverLevel);
            if (linked == null || !linked.isAlive()) {
                continue;
            }
            PortalCrossing crossing = portal.resolveCrossing(entity, previousBox, currentBox);
            if (crossing != null) {
                if (entity instanceof Player && Boolean.getBoolean(DEBUG_TRAVERSAL_PROPERTY)) {
                    Antarchy.LOGGER.info("Portal gun traversal accepted path=movement portal={} player={} probe={} offset={} linked={}",
                            portal.getUUID(), entity.getUUID(), crossing.probe(), crossing.normalOffset(), linked.getUUID());
                }
                logCrossingPath(entity, "movement");
                portal.teleportEntity(entity, linked, crossing.probe(), resolvedMovement, crossing.normalOffset());
                return;
            }
        }
    }

    public static List<PortalGunPortalEntity> findPortalsNearBounds(Level level, AABB bounds) {
        List<PortalGunPortalEntity> portals = new java.util.ArrayList<>();
        AABB searchBounds = bounds.inflate(10.0D);
        for (PortalGunPortalEntity portal : level.getEntitiesOfClass(PortalGunPortalEntity.class, searchBounds, PortalGunPortalEntity::isAlive)) {
            if (portal.getWorldPortalShape().getBoundsForCulling().intersects(bounds)) {
                portals.add(portal);
            }
        }
        return portals;
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
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.ownerId != null) {
            PortalGunSavedData.getChannelNameForPortal(serverLevel.getServer(), this.ownerId, serverLevel.dimension().location(), this.getUUID())
                    .ifPresent(channelName -> this.adoptChannelIdentity(this.gunId, channelName));
            if (this.gunId == null) {
                PortalGunSavedData.getGunIdForPortal(serverLevel.getServer(), this.ownerId, serverLevel.dimension().location(), this.getUUID())
                        .ifPresent(gunId -> this.adoptChannelIdentity(gunId, this.channelName));
            }
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

    private boolean isStillRegistered(ServerLevel level) {
        return this.ownerId != null && PortalGunSavedData.isRegistered(level, this.ownerId, this.gunId, this.channelName, this.getPortalSide(), this.getUUID());
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
        List<Entity> entities = this.level().getEntities(this, this.getScanRange(), this::canTeleportEntity);
        for (Entity entity : entities) {
            AABB currentBox = entity.getBoundingBox();
            AABB previousBox = currentBox.move(entity.xo - entity.getX(), entity.yo - entity.getY(), entity.zo - entity.getZ());
            PortalCrossing crossing = this.resolveCrossing(entity, previousBox, currentBox);
            if (crossing == null) {
                if (entity instanceof Player && Boolean.getBoolean(DEBUG_TRAVERSAL_PROPERTY) && this.ageTicks % 20 == 0) {
                    Vec3 probe = this.teleportProbePosition(entity, currentBox);
                    double depth = this.getWorldPortalShape().localCoords(probe).depth();
                    if (depth < 1.5D) {
                        Antarchy.LOGGER.info("Portal gun traversal waiting portal={} player={} depth={} position={} linked={}",
                                this.getUUID(), entity.getUUID(), depth, entity.position(), linked.getUUID());
                    }
                }
                continue;
            }
            if (entity instanceof Player && Boolean.getBoolean(DEBUG_TRAVERSAL_PROPERTY)) {
                Antarchy.LOGGER.info("Portal gun traversal accepted portal={} player={} probe={} offset={} linked={}",
                        this.getUUID(), entity.getUUID(), crossing.probe(), crossing.normalOffset(), linked.getUUID());
            }
            logCrossingPath(entity, "portal_tick");
            Vec3 resolvedMovement = entity.position().subtract(entity.xo, entity.yo, entity.zo);
            this.teleportEntity(entity, linked, crossing.probe(), resolvedMovement, crossing.normalOffset());
        }
    }

    private static void logCrossingPath(Entity entity, String path) {
        String entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if (LOGGED_CROSSING_PATHS.add(entityType + ":" + path)) {
            Antarchy.LOGGER.info("Portal gun entity crossing path entityType={} path={}", entityType, path);
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

    private static Vec3 clampPortalMotion(Vec3 motion) {
        return new Vec3(clampPortalMotionComponent(motion.x), clampPortalMotionComponent(motion.y), clampPortalMotionComponent(motion.z));
    }

    private static double clampPortalMotionComponent(double motion) {
        return Math.abs(motion) > 0.99D ? motion / (Math.abs(motion) + 0.001D) : motion;
    }

    private void teleportEntity(Entity entity, PortalGunPortalEntity destination, Vec3 currentProbe, Vec3 entryMotion, double normalOffset) {
        List<EntityTeleportState> group = new java.util.ArrayList<>();
        collectTeleportGroup(entity, null, group);
        Vec3 rootOriginalPosition = entity.position();
        PortalTransitTransform transitTransform = this.previewTransit(entity, destination, new PortalCrossing(currentProbe, normalOffset), entryMotion);
        Vec3 exitNormal = destination.getNormalVec().normalize();
        Vec3 requestedExitPos = transitTransform.requestedPosition();
        Vec3 exitPos = transitTransform.exitPosition();
        Vec3 transformedMotion = transitTransform.movement();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sound("portal_enter"), SoundSource.PLAYERS, 0.7F, 0.96F + this.random.nextFloat() * 0.08F);
        for (EntityTeleportState state : group) {
            Entity member = state.entity();
            member.stopRiding();
        }
        for (EntityTeleportState state : group) {
            Entity member = state.entity();
            Vec3 memberOffset = PortalGunTransformUtil.transformVector(this, destination, state.position().subtract(rootOriginalPosition));
            Vec3 memberExitPos = putEntityWithinExitBounds(member, requestedExitPos.add(memberOffset), legacyExitBounds(destination, member, exitNormal));
            Vec3 memberLook = PortalGunTransformUtil.transformVector(this, destination, state.look()).normalize();
            Vec3 memberMotion = member == entity
                    ? transformedMotion
                    : PortalGunTransformUtil.transformVector(this, destination, state.motion());
            memberMotion = clampPortalMotion(memberMotion);
            float yaw = PortalGunTransformUtil.yawFromLook(memberLook);
            float pitch = PortalGunTransformUtil.pitchFromLook(memberLook);
            if (member instanceof ServerPlayer player && destination.level() instanceof ServerLevel destinationLevel) {
                player.teleportTo(destinationLevel, memberExitPos.x, memberExitPos.y, memberExitPos.z, yaw, pitch);
            } else {
                member.teleportTo(memberExitPos.x, memberExitPos.y, memberExitPos.z);
                member.setYRot(yaw);
                member.setXRot(pitch);
                member.setYHeadRot(yaw);
                member.setYBodyRot(yaw);
            }
            member.setDeltaMovement(memberMotion);
            member.hasImpulse = true;
            member.hurtMarked = true;
            if (member instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
            member.xo = member.getX();
            member.yo = member.getY();
            member.zo = member.getZ();
            member.yRotO = yaw;
            member.xRotO = pitch;
            member.setOnGround(false);
            member.horizontalCollision = false;
            member.verticalCollision = false;
            member.verticalCollisionBelow = false;
            float verticalMotion = (float) memberMotion.y;
            member.fallDistance = 0.1F * (verticalMotion / -0.1F * verticalMotion / -0.1F);
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
                }
            }
        }
        destination.level().playSound(null, exitPos.x, exitPos.y, exitPos.z, sound("portal_exit"), SoundSource.PLAYERS, 0.7F, 0.96F + this.random.nextFloat() * 0.08F);
    }

    private void collectTeleportGroup(Entity entity, Entity vehicle, List<EntityTeleportState> group) {
        group.add(new EntityTeleportState(entity, vehicle, entity.position(), entity.getLookAngle(), entity.getDeltaMovement()));
        for (Entity passenger : entity.getPassengers()) {
            collectTeleportGroup(passenger, entity, group);
        }
    }

    private static Vec3 putEntityWithinExitBounds(Entity entity, Vec3 position, AABB exitBounds) {
        AABB entityBounds = entity.getBoundingBox().move(position.x - entity.getX(), position.y - entity.getY(), position.z - entity.getZ());
        double x = position.x;
        double y = position.y;
        double z = position.z;
        if (entityBounds.maxX > exitBounds.maxX) {
            x += exitBounds.maxX - entityBounds.maxX;
        }
        if (entityBounds.minX < exitBounds.minX) {
            x += exitBounds.minX - entityBounds.minX;
        }
        if (y + entity.getEyeHeight() > exitBounds.maxY) {
            y += exitBounds.maxY - y - entity.getEyeHeight();
        }
        if (y < exitBounds.minY) {
            y += exitBounds.minY - y + 0.001D;
        }
        if (entityBounds.maxZ > exitBounds.maxZ) {
            z += exitBounds.maxZ - entityBounds.maxZ;
        }
        if (entityBounds.minZ < exitBounds.minZ) {
            z += exitBounds.minZ - entityBounds.minZ;
        }
        return new Vec3(x, y, z);
    }

    private static AABB legacyExitBounds(PortalGunPortalEntity destination, Entity entity, Vec3 exitNormal) {
        double maxDimension = Math.max(entity.getBbWidth(), entity.getBbHeight());
        return destination.getScanRange().expandTowards(exitNormal.scale(-maxDimension));
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
        if (this.ownerIdentity != null) {
            tag.putString("OwnerIdentity", this.ownerIdentity);
        }
        if (this.gunId != null) {
            tag.putUUID("GunId", this.gunId);
        }
        if (this.channelName != null) {
            tag.putString("ChannelName", this.channelName);
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
        this.ownerIdentity = tag.contains("OwnerIdentity") ? tag.getString("OwnerIdentity") : String.valueOf(this.ownerId);
        this.gunId = tag.hasUUID("GunId") ? tag.getUUID("GunId") : null;
        this.channelName = tag.contains("ChannelName") ? tag.getString("ChannelName") : this.gunId == null ? "" : "Random Channel #" + this.gunId.hashCode();
        this.entityData.set(OWNER_ID, Optional.ofNullable(this.ownerId));
        this.entityData.set(GUN_ID, Optional.ofNullable(this.gunId));
        this.entityData.set(CHANNEL_NAME, this.channelName);
        if (tag.hasUUID("LinkedPortalId")) {
            this.linkedPortalId = tag.getUUID("LinkedPortalId");
            this.entityData.set(LINKED_PORTAL, Optional.of(this.linkedPortalId));
        }
        this.ageTicks = tag.getInt("AgeTicks");
        this.pairTime = tag.getInt("PairTime");
        this.entityData.set(SIDE, tag.getInt("Side"));
        this.entityData.set(FACING, tag.getInt("Facing"));
        this.entityData.set(UP_AXIS, tag.getInt("UpAxis"));
        if (this.ownerId != null && ("Global".equals(this.ownerIdentity) || !tag.contains("ChannelRed"))) {
            int[] channelColor = generateChannelColor(this.ownerIdentity, this.channelName, this.getPortalSide());
            this.entityData.set(CHANNEL_RED, channelColor[0]);
            this.entityData.set(CHANNEL_GREEN, channelColor[1]);
            this.entityData.set(CHANNEL_BLUE, channelColor[2]);
        } else if (tag.contains("ChannelRed")) {
            this.entityData.set(CHANNEL_RED, tag.getInt("ChannelRed"));
            this.entityData.set(CHANNEL_GREEN, tag.getInt("ChannelGreen"));
            this.entityData.set(CHANNEL_BLUE, tag.getInt("ChannelBlue"));
        }
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
            PortalGunSavedData.clearPortal(serverLevel.getServer(), ownerId, this.gunId, this.channelName, this.getPortalSide(), this.getUUID(), serverLevel.dimension().location());
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

    private static int[] generateChannelColor(String ownerIdentity, String channelName, PortalSide side) {
        if ("Global".equals(ownerIdentity)) {
            int[] globalColors = switch (channelName) {
                case "Chell" -> new int[] {361215, 16756742};
                case "Atlas" -> new int[] {5482192, 4064209};
                case "P-body" -> new int[] {16373344, 8394260};
                default -> null;
            };
            if (globalColors != null) {
                int color = globalColors[side == PortalSide.BLUE ? 0 : 1];
                return new int[] {color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF};
            }
        }
        String owner = String.valueOf(ownerIdentity);
        String identity = owner + "_" + String.valueOf(channelName);
        Random random = new Random();
        random.setSeed((long) Math.abs(identity.hashCode() * owner.hashCode()));
        int firstColor = Math.round(1.6777215E7F * random.nextFloat());
        float[] hsb = Color.RGBtoHSB(firstColor >> 16 & 0xFF, firstColor >> 8 & 0xFF, firstColor & 0xFF, null);
        hsb[2] = 0.65F + 0.25F * hsb[2];
        int portalAColor = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        float oppositeHue = hsb[0] + 0.5F;
        if (oppositeHue > 1.0F) {
            oppositeHue -= 1.0F;
        }
        int portalBColor = Color.HSBtoRGB(oppositeHue, hsb[1], hsb[2]);
        int color = side == PortalSide.BLUE ? portalAColor : portalBColor;
        return new int[] {color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF};
    }

    public record PortalCrossing(Vec3 probe, double normalOffset) {
    }

    public record PortalTransitTransform(Vec3 requestedPosition, Vec3 exitPosition, Vec3 movement) {
    }

    private record EntityTeleportState(Entity entity, Entity vehicle, Vec3 position, Vec3 look, Vec3 motion) {
    }
}
