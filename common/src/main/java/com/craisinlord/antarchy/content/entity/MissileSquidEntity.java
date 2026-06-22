package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.entity.kraken.KrakenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

public class MissileSquidEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(MissileSquidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> CAN_FLY = SynchedEntityData.defineId(MissileSquidEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String BITE_COOLDOWN_KEY = "BiteCooldown";
    private static final String CAN_FLY_KEY = "CanFly";
    private static final String LAUNCHED_FROM_SQUIDZOOKA_KEY = "LaunchedFromSquidzooka";
    private static final String LAUNCH_OWNER_KEY = "LaunchOwner";
    private static final String ARMED_AFTER_LANDING_KEY = "ArmedAfterLanding";
    private static final String ACTION_ANIM_TICKS_KEY = "ActionAnimTicks";
    private static final double AGGRO_SPEED = 0.5D;
    private static final double PATROL_SPEED = 0.28D;
    private static final double ATTACH_RANGE_SQR = 2.25D;
    private static final int LATCH_DAMAGE_INTERVAL = 15;
    private static final float LATCH_DAMAGE = 2.0F;
    private static final int ANIM_IDLE = 0;
    private static final int ANIM_SWIM = 1;
    private static final int ANIM_ATTACK = 2;
    private static final int ATTACK_ANIM_TICKS = 10;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");

    @Nullable
    private Vec3 patrolTarget;
    private int patrolRetargetTicks;
    private int strafeRetargetTicks;
    private int biteCooldown;
    private int actionAnimationTicks;
    private float orbitDirection = 1.0F;
    private int attachedTargetId = -1;
    private boolean launchedFromSquidzooka;
    private boolean armedAfterLanding;
    @Nullable
    private UUID launchOwnerUuid;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public MissileSquidEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 80, 8, 0.06F, 0.12F, true);
        this.xpReward = 6;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.84D)
                .add(Attributes.FLYING_SPEED, 0.84D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    public static boolean canSpawn(EntityType<MissileSquidEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && pos.getY() <= level.getSeaLevel() - 10
                && level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(CAN_FLY, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
        hurtByTargetGoal.setAlertOthers(MissileSquidEntity.class);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<MissileSquidEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_SWIM -> state.setAndContinue(SWIM_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (CAN_FLY.equals(key)) {
            this.moveControl = this.canFly()
                    ? new FlyingMoveControl(this, 20, true)
                    : new SmoothSwimmingMoveControl(this, 80, 8, 0.06F, 0.12F, true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity attachedTarget = this.getAttachedTarget();
        this.setNoGravity(attachedTarget != null || (!this.launchedFromSquidzooka && (this.isInWater() || this.canFly())));

        if (attachedTarget != null) {
            if (!this.level().isClientSide) {
                this.tickAnimationTimers();
                this.tickAttachedTarget(attachedTarget);
                this.updateAnimationState();
            } else {
                this.positionOnAttachedTarget(attachedTarget);
                this.updateAttachedRotation(attachedTarget);
            }
            return;
        }

        if (this.launchedFromSquidzooka) {
            if (!this.level().isClientSide) {
                this.tickAnimationTimers();
            }
            this.tickLaunchedSquid();
            this.updateSwimRotation();
            if (!this.level().isClientSide) {
                this.updateAnimationState();
            }
            return;
        }

        if (this.level().isClientSide) {
            this.tickClientParticles();
            this.updateSwimRotation();
            return;
        }

        this.tickAnimationTimers();

        if (this.biteCooldown > 0) {
            this.biteCooldown--;
        }

        LivingEntity target = this.getTarget();
        if ((target == null || !target.isAlive() || !this.canTargetEntity(target)) && this.tickCount % 10 == 0) {
            LivingEntity nearbyTarget = this.findNearestAttackTarget();
            if (nearbyTarget != null) {
                this.setTarget(nearbyTarget);
                target = nearbyTarget;
            } else {
                this.setTarget(null);
                target = null;
            }
        }

        if (target != null && target.isAlive()) {
            this.tickAggroMovement(target);
        } else {
            this.tickPatrolMovement();
        }

        this.updateSwimRotation();
        this.updateAnimationState();
    }

    @Override
    public void baseTick() {
        int airSupply = this.getAirSupply();
        super.baseTick();
        this.handleWaterAnimalAirSupply(airSupply);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.getAttachedTarget() != null) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (this.launchedFromSquidzooka) {
            return;
        }

        if (this.isEffectiveAi() && (this.isInWater() || this.canFly())) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.isInWater() ? 0.92D : 0.91D));
            return;
        }

        super.travel(travelVector);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return target instanceof LivingEntity livingTarget && this.attachToTarget(livingTarget);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.MISSILE_SQUID_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.MISSILE_SQUID_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.MISSILE_SQUID_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(BITE_COOLDOWN_KEY, this.biteCooldown);
        tag.putBoolean(CAN_FLY_KEY, this.canFly());
        tag.putBoolean(LAUNCHED_FROM_SQUIDZOOKA_KEY, this.launchedFromSquidzooka);
        tag.putBoolean(ARMED_AFTER_LANDING_KEY, this.armedAfterLanding);
        tag.putInt(ACTION_ANIM_TICKS_KEY, this.actionAnimationTicks);
        if (this.launchOwnerUuid != null) {
            tag.putUUID(LAUNCH_OWNER_KEY, this.launchOwnerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.biteCooldown = tag.getInt(BITE_COOLDOWN_KEY);
        this.setCanFly(tag.getBoolean(CAN_FLY_KEY));
        this.launchedFromSquidzooka = tag.getBoolean(LAUNCHED_FROM_SQUIDZOOKA_KEY);
        this.armedAfterLanding = tag.getBoolean(ARMED_AFTER_LANDING_KEY);
        this.actionAnimationTicks = Math.max(0, tag.getInt(ACTION_ANIM_TICKS_KEY));
        this.launchOwnerUuid = tag.hasUUID(LAUNCH_OWNER_KEY) ? tag.getUUID(LAUNCH_OWNER_KEY) : null;
        this.updateAnimationState();
    }

    private void tickAggroMovement(LivingEntity target) {
        this.patrolTarget = null;
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (!this.isInWater() && !this.canFly()) {
            this.tickGroundAggroMovement(target);
            return;
        }

        if (this.strafeRetargetTicks-- <= 0) {
            this.strafeRetargetTicks = 6 + this.random.nextInt(8);
            if (this.random.nextFloat() < 0.35F) {
                this.orbitDirection *= -1.0F;
            }
        }

        Vec3 attackPoint = this.createAttachApproachPoint(target);
        this.getMoveControl().setWantedPosition(attackPoint.x, attackPoint.y, attackPoint.z, AGGRO_SPEED);

        if (this.biteCooldown <= 0 && this.distanceToSqr(attackPoint) <= ATTACH_RANGE_SQR && this.hasLineOfSight(target)) {
            this.doHurtTarget(target);
        }
    }

    private void tickPatrolMovement() {
        this.strafeRetargetTicks = 0;

        if (this.patrolRetargetTicks-- <= 0 || this.patrolTarget == null || this.position().distanceToSqr(this.patrolTarget) < 3.0D) {
            this.patrolRetargetTicks = 24 + this.random.nextInt(18);
            this.patrolTarget = this.findPatrolTarget();
        }

        if (this.patrolTarget != null) {
            this.getMoveControl().setWantedPosition(this.patrolTarget.x, this.patrolTarget.y, this.patrolTarget.z, PATROL_SPEED);
        }
    }

    private Vec3 createAttachApproachPoint(LivingEntity target) {
        return target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
    }

    private Vec3 findPatrolTarget() {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos candidate = origin.offset(this.random.nextInt(17) - 8, this.random.nextInt(7) - 3, this.random.nextInt(17) - 8);
            boolean candidateOpen = this.level().isEmptyBlock(candidate) || this.level().getFluidState(candidate).is(FluidTags.WATER);
            boolean aboveOpen = this.level().isEmptyBlock(candidate.above()) || this.level().getFluidState(candidate.above()).is(FluidTags.WATER);
            if (this.canFly()) {
                if (!candidateOpen || !aboveOpen) {
                    continue;
                }
            } else if (!this.level().getFluidState(candidate).is(FluidTags.WATER) || !this.level().getFluidState(candidate.above()).is(FluidTags.WATER)) {
                continue;
            }

            return Vec3.atCenterOf(candidate);
        }

        return this.position();
    }

    public void launchFromSquidzooka(LivingEntity owner, float velocity) {
        Vec3 direction = Vec3.directionFromRotation(new Vec2(owner.getXRot(), owner.getYRot()));
        this.launchAsProjectile(owner, direction.scale(velocity).add(owner.getDeltaMovement()));
    }

    public void launchAsProjectile(@Nullable LivingEntity owner, Vec3 velocity) {
        this.attachedTargetId = -1;
        this.launchedFromSquidzooka = true;
        this.armedAfterLanding = false;
        this.launchOwnerUuid = owner != null ? owner.getUUID() : null;
        this.setTarget(null);
        this.setNoGravity(false);
        this.setDeltaMovement(velocity);
        this.hasImpulse = true;
        this.updateAnimationState();
    }

    private void tickLaunchedSquid() {
        if (this.level().isClientSide) {
            return;
        }

        Vec3 start = this.position();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 end = start.add(velocity);

        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                this.level(),
                this,
                start,
                end,
                this.getBoundingBox().expandTowards(velocity).inflate(0.35D),
                this::canLaunchHit
        );

        if (entityHit != null) {
            Vec3 location = entityHit.getLocation();
            this.moveTo(location.x, location.y, location.z, this.getYRot(), this.getXRot());
            if (entityHit.getEntity() instanceof LivingEntity livingEntity) {
                this.attachToTarget(livingEntity);
            } else {
                this.armAfterLanding(null);
            }
            return;
        }

        this.move(MoverType.SELF, velocity);
        Vec3 movedVelocity = this.getDeltaMovement();
        if (!this.isNoGravity()) {
            movedVelocity = movedVelocity.add(0.0D, -0.08D, 0.0D);
        }

        this.setDeltaMovement(movedVelocity.scale(this.isInWater() ? 0.9D : 0.98D));
        this.hasImpulse = true;

        if (this.onGround() || this.horizontalCollision || this.verticalCollision) {
            this.armAfterLanding(this.findLandingTarget());
        }
    }

    private boolean canLaunchHit(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity) || entity == this) {
            return false;
        }

        return this.canTargetEntity(livingEntity) && entity.isPickable();
    }

    @Nullable
    private LivingEntity findLandingTarget() {
        List<LivingEntity> nearbyTargets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(this.blockPosition()).inflate(2.5D),
                this::canLaunchHit
        );
        return nearbyTargets.isEmpty() ? null : nearbyTargets.get(0);
    }

    private void armAfterLanding(@Nullable LivingEntity target) {
        this.attachedTargetId = -1;
        this.launchedFromSquidzooka = false;
        this.armedAfterLanding = true;
        this.fallDistance = 0.0F;
        if (target != null && this.canTargetEntity(target)) {
            this.setTarget(target);
        }
        this.updateAnimationState();
    }

    @Nullable
    private LivingEntity findNearestAttackTarget() {
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        Player nearestPlayer = this.level().getNearestPlayer(this, followRange);
        return nearestPlayer != null && this.canTargetEntity(nearestPlayer) ? nearestPlayer : null;
    }

    private boolean canTargetEntity(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity.getType() == this.getType() || entity instanceof KrakenEntity) {
            return false;
        }

        if (this.launchOwnerUuid != null && this.launchOwnerUuid.equals(entity.getUUID())) {
            return false;
        }

        if (entity instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
            return this.level().getDifficulty() != Difficulty.PEACEFUL;
        }

        return true;
    }

    private void tickGroundAggroMovement(LivingEntity target) {
        Vec3 horizontal = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (horizontal.lengthSqr() > 1.0E-4D && this.onGround()) {
            Vec3 lunge = horizontal.normalize().scale(0.16D).add(0.0D, 0.24D, 0.0D);
            this.setDeltaMovement(this.getDeltaMovement().add(lunge));
            this.hasImpulse = true;
        }

        if (this.biteCooldown <= 0 && this.distanceToSqr(target) <= ATTACH_RANGE_SQR && this.hasLineOfSight(target)) {
            this.doHurtTarget(target);
        }
    }

    private void tickClientParticles() {
        if ((this.getTarget() != null || this.biteCooldown > 0 || this.getAttachedTarget() != null) && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.BUBBLE,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + 0.3D + this.random.nextDouble() * 0.4D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }

    private void updateSwimRotation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 10.0F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }

        if (velocity.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 8.0F));
        }
    }

    private void handleWaterAnimalAirSupply(int previousAirSupply) {
        if (!this.isAlive()) {
            return;
        }

        if (this.isInWaterOrBubble()) {
            this.setAirSupply(this.getMaxAirSupply());
            return;
        }

        this.setAirSupply(previousAirSupply - 1);
        if (this.getAirSupply() == -20) {
            this.setAirSupply(0);
            this.hurt(this.damageSources().drown(), 2.0F);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.canTargetEntity(target) && super.canAttack(target);
    }


    @Nullable
    private LivingEntity getAttachedTarget() {
        if (this.attachedTargetId < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(this.attachedTargetId);
        return entity instanceof LivingEntity livingEntity && this.canTargetEntity(livingEntity) ? livingEntity : null;
    }

    private boolean attachToTarget(LivingEntity target) {
        if (!this.canTargetEntity(target) || this.hasAttachedSquid(target)) {
            return false;
        }

        this.attachedTargetId = target.getId();
        this.launchedFromSquidzooka = false;
        this.armedAfterLanding = true;
        this.setTarget(target);
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
        this.biteCooldown = LATCH_DAMAGE_INTERVAL;
        this.playActionAnimation(ANIM_ATTACK, ATTACK_ANIM_TICKS);
        this.positionOnAttachedTarget(target);
        this.updateAttachedRotation(target);
        this.playSound(AntarchySoundEvents.MISSILE_SQUID_ATTACK.get(), 0.8F, 1.15F + this.random.nextFloat() * 0.15F);
        return true;
    }

    private boolean hasAttachedSquid(LivingEntity target) {
        return !this.level().getEntitiesOfClass(
                MissileSquidEntity.class,
                target.getBoundingBox().inflate(1.5D),
                squid -> squid != this && squid.attachedTargetId == target.getId()
        ).isEmpty();
    }

    private void detachFromTarget() {
        this.attachedTargetId = -1;
        this.setTarget(null);
    }

    private void tickAttachedTarget(LivingEntity target) {
        if (!target.isAlive() || !this.canTargetEntity(target)) {
            this.detachFromTarget();
            return;
        }

        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setDeltaMovement(Vec3.ZERO);
        this.positionOnAttachedTarget(target);
        this.updateAttachedRotation(target);

        if (this.biteCooldown > 0) {
            this.biteCooldown--;
        }

        if (this.biteCooldown <= 0) {
            Vec3 previousMovement = target.getDeltaMovement();
            boolean hurt = target.hurt(this.damageSources().mobAttack(this), LATCH_DAMAGE);
            target.setDeltaMovement(previousMovement);
            if (hurt) {
                this.playActionAnimation(ANIM_ATTACK, ATTACK_ANIM_TICKS);
            }
            this.biteCooldown = LATCH_DAMAGE_INTERVAL;
        }
    }

    private void positionOnAttachedTarget(LivingEntity target) {
        Vec3 anchor = this.createLatchAnchor(target);
        this.moveTo(anchor.x, anchor.y, anchor.z, this.getYRot(), this.getXRot());
        this.resetFallDistance();
    }

    private Vec3 createLatchAnchor(LivingEntity target) {
        return target.position().add(
                0.0D,
                target.getBbHeight() + 0.15D,
                0.0D
        );
    }

    private void updateAttachedRotation(LivingEntity target) {
        Vec3 direction = target.position().subtract(this.position());
        if (direction.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float)(Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(targetYaw);
            this.yBodyRot = targetYaw;
            this.yHeadRot = targetYaw;
        }
        this.setXRot(0.0F);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public boolean canFly() {
        return this.entityData.get(CAN_FLY);
    }

    public void setCanFly(boolean canFly) {
        this.entityData.set(CAN_FLY, canFly);
        this.moveControl = canFly
                ? new FlyingMoveControl(this, 20, true)
                : new SmoothSwimmingMoveControl(this, 80, 8, 0.06F, 0.12F, true);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void playActionAnimation(int animationState, int ticks) {
        this.setAnimationState(animationState);
        this.actionAnimationTicks = ticks;
    }

    private void tickAnimationTimers() {
        if (this.actionAnimationTicks > 0) {
            this.actionAnimationTicks--;
        }
    }

    private void updateAnimationState() {
        if (this.getAttachedTarget() != null || this.actionAnimationTicks > 0) {
            this.setAnimationState(ANIM_ATTACK);
            return;
        }

        Vec3 velocity = this.getDeltaMovement();
        if (this.launchedFromSquidzooka
                || this.getTarget() != null
                || velocity.lengthSqr() > 1.0E-3D
                || (this.isInWater() && velocity.horizontalDistanceSqr() > 1.0E-4D)) {
            this.setAnimationState(ANIM_SWIM);
            return;
        }

        this.setAnimationState(ANIM_IDLE);
    }
}
