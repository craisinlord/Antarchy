package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.Antarchy;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
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
    private static final RawAnimation OPEN_ANIM = RawAnimation.begin().thenPlay("open");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation CLOSE_ANIM = RawAnimation.begin().thenPlay("close");
    private static final int OPEN_TICKS = 10;
    private static final int CLOSE_TICKS = 10;
    private static final int MAX_LIFETIME_TICKS = 20 * 60 * 20;
    private static final int TELEPORT_COOLDOWN_TICKS = 15;
    private static final double HALF_WIDTH = 0.5D;
    private static final double HALF_HEIGHT = 1.0D;
    private static final double HALF_DEPTH = 0.35D;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerId;
    private UUID linkedPortalId;
    private int ageTicks;
    private BlockPos supportOrigin = BlockPos.ZERO;
    private static final java.util.Map<UUID, Long> TELEPORT_COOLDOWNS = new java.util.HashMap<>();

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
    }

    public void configure(UUID ownerId, PortalSide side, Direction facing, Direction upAxis, BlockPos supportOrigin) {
        this.ownerId = ownerId;
        this.entityData.set(SIDE, side.ordinal());
        this.entityData.set(FACING, facing.get3DDataValue());
        this.entityData.set(UP_AXIS, upAxis.get3DDataValue());
        this.supportOrigin = supportOrigin.immutable();
    }

    public void linkTo(PortalGunPortalEntity other) {
        this.linkedPortalId = other.getUUID();
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

    public Vec3 getNormalVec() {
        return Vec3.atLowerCornerOf(this.getFacingDirection().getNormal());
    }

    public Vec3 getUpVec() {
        return Vec3.atLowerCornerOf(this.getUpAxis().getNormal());
    }

    public Vec3 getWidthVec() {
        return this.getNormalVec().cross(this.getUpVec()).normalize();
    }

    @Override
    public void tick() {
        super.tick();
        this.ageTicks++;
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
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
            return;
        }
        this.tickTeleport(linked);
    }

    private boolean isStillRegistered(ServerLevel level) {
        return this.ownerId != null && PortalGunSavedData.isRegistered(level.getServer(), this.ownerId, this.getPortalSide(), this.getUUID());
    }

    private boolean hasValidSurface() {
        Direction facing = this.getFacingDirection();
        Direction heightAxis = this.getUpAxis();
        for (int h = 0; h < 2; h++) {
            BlockPos supportPos = this.supportOrigin.relative(heightAxis, h);
            BlockPos airPos = supportPos.relative(facing);
            BlockState supportState = this.level().getBlockState(supportPos);
            BlockState airState = this.level().getBlockState(airPos);
            FluidState fluidState = this.level().getFluidState(airPos);
            if (!supportState.isFaceSturdy(this.level(), supportPos, facing)) {
                return false;
            }
            if (!airState.isAir() && !airState.canBeReplaced()) {
                return false;
            }
            if (!fluidState.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void tickTeleport(PortalGunPortalEntity linked) {
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(0.75D), this::canTeleportEntity);
        for (Entity entity : entities) {
            if (this.isInsidePortal(entity.position())) {
                this.teleportEntity(entity, linked);
            }
        }
    }

    private boolean canTeleportEntity(Entity entity) {
        if (!entity.isAlive() || entity instanceof PortalGunPortalEntity || entity.isPassenger() || entity.isVehicle()) {
            return false;
        }
        long gameTime = this.level().getGameTime();
        long cooldownUntil = TELEPORT_COOLDOWNS.getOrDefault(entity.getUUID(), 0L);
        if (cooldownUntil <= gameTime) {
            TELEPORT_COOLDOWNS.remove(entity.getUUID());
            return true;
        }
        return false;
    }

    private boolean isInsidePortal(Vec3 position) {
        Vec3 relative = position.subtract(this.position());
        double horizontal = relative.dot(this.getWidthVec());
        double vertical = relative.dot(this.getUpVec());
        double depth = Math.abs(relative.dot(this.getNormalVec()));
        return Math.abs(horizontal) <= HALF_WIDTH && Math.abs(vertical) <= HALF_HEIGHT && depth <= HALF_DEPTH;
    }

    private void teleportEntity(Entity entity, PortalGunPortalEntity destination) {
        Vec3 sourceNormal = this.getNormalVec();
        Vec3 sourceUp = this.getUpVec();
        Vec3 sourceWidth = this.getWidthVec();
        Vec3 destinationNormal = destination.getNormalVec();
        Vec3 destinationUp = destination.getUpVec();
        Vec3 destinationWidth = destination.getWidthVec();
        Vec3 motion = entity.getDeltaMovement();
        double widthComponent = motion.dot(sourceWidth);
        double upComponent = motion.dot(sourceUp);
        double forward = -motion.dot(sourceNormal);
        Vec3 transformedMotion = destinationWidth.scale(widthComponent).add(destinationUp.scale(upComponent)).add(destinationNormal.scale(forward));
        Vec3 exitPos = destination.position().add(destinationNormal.scale(1.0D));
        TELEPORT_COOLDOWNS.put(entity.getUUID(), this.level().getGameTime() + TELEPORT_COOLDOWN_TICKS);
        if (entity instanceof ServerPlayer player && destination.level() instanceof ServerLevel destinationLevel) {
            player.teleportTo(destinationLevel, exitPos.x, exitPos.y, exitPos.z, player.getYRot(), player.getXRot());
        } else {
            entity.teleportTo(exitPos.x, exitPos.y, exitPos.z);
        }
        entity.setDeltaMovement(transformedMotion);
        entity.hasImpulse = true;
        entity.fallDistance = 0.0F;
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
        return this.getBoundingBox().inflate(1.5D);
    }

    @Override
    protected AABB makeBoundingBox() {
        Vec3 widthVec = this.getWidthVec();
        Vec3 upVec = this.getUpVec();
        Vec3 normalVec = this.getNormalVec();
        double halfX = HALF_WIDTH * Math.abs(widthVec.x) + HALF_HEIGHT * Math.abs(upVec.x) + HALF_DEPTH * Math.abs(normalVec.x);
        double halfY = HALF_WIDTH * Math.abs(widthVec.y) + HALF_HEIGHT * Math.abs(upVec.y) + HALF_DEPTH * Math.abs(normalVec.y);
        double halfZ = HALF_WIDTH * Math.abs(widthVec.z) + HALF_HEIGHT * Math.abs(upVec.z) + HALF_DEPTH * Math.abs(normalVec.z);
        return new AABB(this.getX() - halfX, this.getY() - halfY, this.getZ() - halfZ, this.getX() + halfX, this.getY() + halfY, this.getZ() + halfZ);
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
        tag.putInt("Side", this.getPortalSide().ordinal());
        tag.putInt("Facing", this.getFacingDirection().get3DDataValue());
        tag.putInt("SupportX", this.supportOrigin.getX());
        tag.putInt("SupportY", this.supportOrigin.getY());
        tag.putInt("SupportZ", this.supportOrigin.getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("OwnerId")) {
            this.ownerId = tag.getUUID("OwnerId");
        }
        if (tag.hasUUID("LinkedPortalId")) {
            this.linkedPortalId = tag.getUUID("LinkedPortalId");
        }
        this.ageTicks = tag.getInt("AgeTicks");
        this.entityData.set(SIDE, tag.getInt("Side"));
        this.entityData.set(FACING, tag.getInt("Facing"));
        this.supportOrigin = new BlockPos(tag.getInt("SupportX"), tag.getInt("SupportY"), tag.getInt("SupportZ"));
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && this.ownerId != null && this.level() instanceof ServerLevel serverLevel) {
            PortalGunSavedData.clearPortal(serverLevel.getServer(), this.ownerId, this.getPortalSide(), this.getUUID());
        }
        super.remove(reason);
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
}
