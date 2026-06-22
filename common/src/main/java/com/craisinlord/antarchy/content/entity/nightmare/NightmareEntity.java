package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
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

import java.util.EnumSet;
import java.util.Comparator;
import java.util.List;

public class NightmareEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ATTACK_ANIMATION_TICKS_KEY = "AttackAnimationTicks";
    private static final String ATTACK_HIT_APPLIED_KEY = "AttackHitApplied";
    private static final String ROAR_TICKS_KEY = "RoarTicks";
    private static final String ROAR_COOLDOWN_KEY = "RoarCooldown";
    private static final String INTRO_ROAR_USED_KEY = "IntroRoarUsed";
    private static final String TARGETLESS_TICKS_KEY = "TargetlessTicks";
    private static final String ANIMATION_STATE_KEY = "AnimationState";
    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_FLY = 2;
    private static final int ANIM_ATTACK = 3;
    private static final int ANIM_FLY_ATTACK = 4;
    private static final int ANIM_ROAR = 5;
    private static final int ANIM_DEATH = 6;
    private static final int ANIM_TAKEOFF = 7;
    private static final int ANIM_FLY_DAMAGED = 8;
    private static final int ATTACK_TOTAL_TICKS = 18;
    private static final int ATTACK_DAMAGE_TICK = 9;
    private static final int INTRO_ROAR_TICKS = 32;
    private static final int COMBAT_ROAR_TICKS = 24;
    private static final int DEATH_TICKS = 36;
    private static final int TARGET_RESET_TICKS = 60;
    private static final int DREAD_TICKS = 160;
    private static final int WEAKNESS_TICKS = 100;
    private static final double PATROL_SPEED = 0.34D;
    private static final double COMBAT_FLIGHT_SPEED = 0.58D;
    private static final double GROUND_APPROACH_SPEED = 0.82D;
    private static final int FLIGHT_MODE_COMMIT_TICKS = 80;
    private static final double ATTACK_START_RANGE_SQR = 42.25D;
    private static final double GROUND_APPROACH_RANGE_SQR = 16.0D * 16.0D;
    private static final double ATTACK_REACH_RADIUS = 2.65D;
    private static final double ATTACK_COMMIT_HORIZONTAL_RANGE = 4.2D;
    private static final double ATTACK_COMMIT_VERTICAL_RANGE = 2.8D;
    private static final double FLIGHT_REENGAGE_RANGE_SQR = 10.0D * 10.0D;
    private static final double FLIGHT_RETURN_RANGE_SQR = 20.0D * 20.0D;
    private static final double CLOSE_APPROACH_RANGE_SQR = 6.0D * 6.0D;
    private static final int FLIGHT_CEILING_CLEARANCE_BLOCKS = 4;
    private static final int BLOCK_BREAK_TICKS = 10;
    private static final double PATROL_AIR_MIN_HEIGHT = 1.15D;
    private static final double PATROL_AIR_HEIGHT_SPAN = 0.85D;
    private static final double COMBAT_FLIGHT_BASE_HEIGHT = 0.75D;
    private static final double COMBAT_FLIGHT_HEIGHT_SPAN = 0.70D;
    private static final double ROAR_RETRY_DISTANCE_SQR = 20.0D * 20.0D;
    private static final float AIR_PATROL_CHANCE = 0.2F;
    private static final long SPAWN_DEBUG_LOG_INTERVAL = 200L;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation TAKEOFF_ANIM = RawAnimation.begin().thenPlay("animation");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation FLY_DAMAGED_ANIM = RawAnimation.begin().thenLoop("fly_damaged");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation FLY_ATTACK_ANIM = RawAnimation.begin().thenPlay("fly_attack");
    private static final RawAnimation ROAR_ANIM = RawAnimation.begin().thenPlay("roar");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private Vec3 patrolTarget;
    private int patrolRetargetTicks;
    private int strafeRetargetTicks;
    private int attackCooldown;
    private int attackAnimationTicks;
    private int roarTicks;
    private int roarCooldown;
    private int targetlessTicks;
    private boolean attackHitApplied;
    private boolean introRoarUsed;
    private boolean groundCombatMode;
    private float orbitDirection = 1.0F;
    private double currentOrbitAngle = 0.0D;
    private int airborneWithTargetTicks = 0;
    private int airborneTicks = 0;
    private int ceilingAvoidanceTicks = 0;
    private int flightModeCommitTicks = 0;
    private int flightTargetRetargetTicks = 0;
    private int wingFlapCooldown = 0;
    private int blockBreakCooldown = 0;
    @Nullable
    private Vec3 cachedFlightTarget;
    @Nullable
    private Vec3 cachedFlightTargetAnchor;
    private static long lastSpawnDebugLogTick = Long.MIN_VALUE;

    public NightmareEntity(EntityType<? extends NightmareEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 12, true);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FLYING_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.ARMOR, 8.0D);
    }

    public static boolean canSpawn(EntityType<NightmareEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        boolean sturdy = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        boolean posEmpty = level.isEmptyBlock(pos);
        boolean aboveEmpty = level.isEmptyBlock(pos.above());
        boolean allowed = level.getDifficulty() != Difficulty.PEACEFUL && sturdy && posEmpty && aboveEmpty;
        return allowed;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(ROARING, false);
    }

    @Override
    protected void registerGoals() {
this.goalSelector.addGoal(4, new NightmareCombatMovementGoal());
        this.goalSelector.addGoal(5, new NightmareAirPatrolGoal());
this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canTargetEntity));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::canTargetEntity));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationState<NightmareEntity> state) {
        state.getController().setAnimationSpeed(this.getAnimationState() == ANIM_FLY ? 0.45D : 1.0D);

        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_TAKEOFF -> state.setAndContinue(TAKEOFF_ANIM);
            case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
            case ANIM_FLY_DAMAGED -> state.setAndContinue(FLY_DAMAGED_ANIM);
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_FLY_ATTACK -> state.setAndContinue(FLY_ATTACK_ANIM);
            case ANIM_ROAR -> state.setAndContinue(ROAR_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
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
    public void tick() {
        super.tick();
        this.setNoGravity(this.shouldUseFlight() && !this.isTooCloseToCeiling());

        if (this.level().isClientSide) {
            this.tickClientParticles();
            this.updateFlightRotation();
            return;
        }

        this.airborneTicks = this.onGround() ? 0 : this.airborneTicks + 1;

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.roarCooldown > 0) {
            this.roarCooldown--;
        }
        if (this.blockBreakCooldown > 0) {
            this.blockBreakCooldown--;
        }

        LivingEntity target = this.getTarget();
        if (!this.canTargetEntity(target)) {
            this.setTarget(null);
            target = null;
        }
        if (this.tickCount % 10 == 0) {
            LivingEntity bestTarget = this.findBestAttackTarget(target);
            if (bestTarget != target) {
                this.setTarget(bestTarget);
                target = bestTarget;
            }
        }

        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            this.updateFlightRotation();
            return;
        }

        if (target == null) {
            this.tickTargetless();
            this.updateAnimationState();
            this.updateFlightRotation();
            return;
        }

        this.targetlessTicks = 0;
        this.getLookControl().setLookAt(target, 35.0F, 20.0F);

        if (this.isRoaring()) {
            this.tickRoar(target);
            this.updateAnimationState();
            this.updateFlightRotation();
            return;
        }

        if (this.attackAnimationTicks > 0) {
            this.tickAttack(target);
            this.updateAnimationState();
            this.updateFlightRotation();
            return;
        }

        if (target instanceof Player && this.shouldStartRoar(target)) {
            this.startRoar();
            this.updateAnimationState();
            this.updateFlightRotation();
            return;
        }

        this.updateAnimationState();
        this.updateFlightRotation();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && (this.isNoGravity() || !this.onGround())) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.onGround() ? 0.88D : 0.91D));
            return;
        }

        super.travel(travelVector);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return false;
        }

        boolean hurt = this.level() instanceof ServerLevel serverLevel
                ? livingTarget.hurt(AntarchyDamageSources.nightmareMauling(serverLevel, this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE))
                : super.doHurtTarget(target);
        if (hurt) {
            this.playNightmareBiteSound();
            this.applyNightmareStrikeEffects(livingTarget);
        }
        return hurt;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void lavaHurt() {
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.canTargetEntity(target) && super.canAttack(target);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.NIGHTMARE_IDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.NIGHTMARE_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.25F;
    }

    @Override
    public float getVoicePitch() {
        return 0.75F + this.random.nextFloat() * 0.1F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ATTACK_COOLDOWN_KEY, this.attackCooldown);
        tag.putInt(ATTACK_ANIMATION_TICKS_KEY, this.attackAnimationTicks);
        tag.putBoolean(ATTACK_HIT_APPLIED_KEY, this.attackHitApplied);
        tag.putInt(ROAR_TICKS_KEY, this.roarTicks);
        tag.putInt(ROAR_COOLDOWN_KEY, this.roarCooldown);
        tag.putBoolean(INTRO_ROAR_USED_KEY, this.introRoarUsed);
        tag.putInt(TARGETLESS_TICKS_KEY, this.targetlessTicks);
        tag.putInt("AirborneTicks", this.airborneTicks);
        tag.putBoolean("Roaring", this.isRoaring());
        tag.putInt(ANIMATION_STATE_KEY, this.getAnimationState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackCooldown = tag.getInt(ATTACK_COOLDOWN_KEY);
        this.attackAnimationTicks = tag.getInt(ATTACK_ANIMATION_TICKS_KEY);
        this.attackHitApplied = tag.getBoolean(ATTACK_HIT_APPLIED_KEY);
        this.roarTicks = tag.getInt(ROAR_TICKS_KEY);
        this.roarCooldown = tag.getInt(ROAR_COOLDOWN_KEY);
        this.introRoarUsed = tag.getBoolean(INTRO_ROAR_USED_KEY);
        this.targetlessTicks = tag.getInt(TARGETLESS_TICKS_KEY);
        this.airborneTicks = tag.getInt("AirborneTicks");
        this.entityData.set(ROARING, tag.getBoolean("Roaring"));
        this.entityData.set(ANIMATION_STATE, tag.contains(ANIMATION_STATE_KEY) ? tag.getInt(ANIMATION_STATE_KEY) : ANIM_IDLE);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.attackAnimationTicks = 0;
            this.attackCooldown = 0;
            this.roarTicks = 0;
            this.setRoaring(false);
            this.setDeltaMovement(Vec3.ZERO);
            this.getNavigation().stop();
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;

        if (this.deathTime == 1) {
            this.setAnimationState(ANIM_DEATH);
        }

        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
            this.dropExperience(this);
        }
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void tickTargetless() {
        this.targetlessTicks++;
        if (this.targetlessTicks >= TARGET_RESET_TICKS) {
            this.introRoarUsed = false;
            this.currentOrbitAngle = this.random.nextDouble() * Math.PI * 2.0D;
        }

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks = 0;
            this.attackHitApplied = false;
        }
        if (this.isRoaring()) {
            this.roarTicks = 0;
            this.setRoaring(false);
        }

        this.groundCombatMode = false;
        this.ceilingAvoidanceTicks = 0;
        this.flightModeCommitTicks = 0;
        this.flightTargetRetargetTicks = 0;
        this.cachedFlightTarget = null;
        this.cachedFlightTargetAnchor = null;
    }

    private void tickPatrolMovement() {
        this.strafeRetargetTicks = 0;
        if (this.patrolRetargetTicks-- <= 0 || this.patrolTarget == null || this.position().distanceToSqr(this.patrolTarget) < 4.0D) {
            this.patrolRetargetTicks = 30 + this.random.nextInt(30);
            this.patrolTarget = this.findPatrolTarget();
        }

        if (this.patrolTarget != null) {
        this.getMoveControl().setWantedPosition(this.patrolTarget.x, this.patrolTarget.y, this.patrolTarget.z, PATROL_SPEED);
    }
    }

    private void tickCombatMovement(LivingEntity target) {
        this.patrolTarget = null;
        if (this.ceilingAvoidanceTicks > 0) {
            this.ceilingAvoidanceTicks--;
        }

        boolean shouldFly = this.resolveCombatFlightMode(target);
        double distanceSqr = this.distanceToSqr(target);
        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double verticalDistance = Math.abs(target.getY() - this.getY());
        this.airborneWithTargetTicks = this.onGround() ? 0 : this.airborneWithTargetTicks + 1;

        if (this.attackCooldown <= 0
                && this.canStartAttackOn(target)) {
            this.startAttack(target);
            return;
        }

        this.faceTowardTarget(target, shouldFly ? 12.0F : 18.0F);

        if (shouldFly && this.isTooCloseToCeiling()) {
            shouldFly = false;
            this.groundCombatMode = true;
            this.flightModeCommitTicks = FLIGHT_MODE_COMMIT_TICKS;
            this.ceilingAvoidanceTicks = 20;
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.15D, 0.0D));
        }

        if (!shouldFly) {
            this.groundCombatMode = true;
            this.flightTargetRetargetTicks = 0;
            this.cachedFlightTarget = null;
            this.cachedFlightTargetAnchor = null;
            if (this.isHoveringAboveTarget(target) && this.tryBreakBlocksToTarget(target)) {
                return;
            }
            Vec3 combatTarget = this.createGroundApproachTarget(target);
            this.getMoveControl().setWantedPosition(
                    combatTarget.x,
                    combatTarget.y,
                    combatTarget.z,
                    GROUND_APPROACH_SPEED
            );
            return;
        }

        this.groundCombatMode = false;
        this.flightModeCommitTicks = FLIGHT_MODE_COMMIT_TICKS;

        if (distanceSqr <= CLOSE_APPROACH_RANGE_SQR
                || horizontalDistanceSqr > ATTACK_COMMIT_HORIZONTAL_RANGE * ATTACK_COMMIT_HORIZONTAL_RANGE
                || verticalDistance > ATTACK_COMMIT_VERTICAL_RANGE) {
            Vec3 combatTarget = this.createStrikeFlightTarget(target);
            this.getMoveControl().setWantedPosition(
                    combatTarget.x,
                    combatTarget.y,
                    combatTarget.z,
                    COMBAT_FLIGHT_SPEED
            );
            return;
        }

        if (this.strafeRetargetTicks-- <= 0) {
            this.strafeRetargetTicks = 40 + this.random.nextInt(40);
        }

        Vec3 combatTarget = this.createCombatFlightTarget(target);
        this.getMoveControl().setWantedPosition(
                combatTarget.x,
                combatTarget.y,
                combatTarget.z,
                COMBAT_FLIGHT_SPEED
        );
    }

    private boolean shouldStartRoar(LivingEntity target) {
        if (!this.onGround() || this.roarCooldown > 0 || !this.hasLineOfSight(target)) {
            return false;
        }

        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr > ROAR_RETRY_DISTANCE_SQR || !this.isInFront(target.position(), -0.1D)) {
            return false;
        }

        boolean pressureWindow = distanceSqr > ATTACK_START_RANGE_SQR && distanceSqr <= GROUND_APPROACH_RANGE_SQR;
        boolean wounded = this.getHealth() <= this.getMaxHealth() * 0.5F;
        return pressureWindow || wounded;
    }

    private void startRoar() {
        this.roarTicks = this.introRoarUsed ? COMBAT_ROAR_TICKS : INTRO_ROAR_TICKS;
        this.introRoarUsed = true;
        this.roarCooldown = 240 + this.random.nextInt(140);
        this.attackAnimationTicks = 0;
        this.attackHitApplied = false;
        this.setRoaring(true);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.18D));
        this.playSound(AntarchySoundEvents.NIGHTMARE_ROAR.get(), 2.2F, 0.7F + this.random.nextFloat() * 0.06F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 2.0D, this.getZ(), 20, 1.1D, 0.8D, 1.1D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 2.0D, this.getZ(), 12, 0.9D, 0.6D, 0.9D, 0.02D);
        }
    }

    private void tickRoar(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));

        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
            this.faceTowardTarget(target, 14.0F);
        }

        if (--this.roarTicks <= 0) {
            this.roarTicks = 0;
            this.setRoaring(false);
            this.attackCooldown = Math.max(this.attackCooldown, 18);
        }
    }

    private void startAttack(LivingEntity target) {
        this.attackAnimationTicks = ATTACK_TOTAL_TICKS;
        this.attackHitApplied = false;
        this.attackCooldown = ATTACK_TOTAL_TICKS + 12;
        this.getNavigation().stop();

        Vec3 lunge = target.getEyePosition().subtract(this.getEyePosition());
        if (lunge.lengthSqr() > 1.0E-4D) {
            Vec3 normalized = lunge.normalize();
            this.setDeltaMovement(this.getDeltaMovement().scale(0.3D).add(normalized.x * 0.85D, normalized.y * 0.18D, normalized.z * 0.85D));
            this.hasImpulse = true;
        }
    }

    private void tickAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.93D));

        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
            this.faceTowardTarget(target, 20.0F);
        }

        int elapsed = ATTACK_TOTAL_TICKS - this.attackAnimationTicks;
        if (!this.attackHitApplied && elapsed >= ATTACK_DAMAGE_TICK) {
            this.attackHitApplied = true;
            this.playNightmareBiteSound();
            this.performAttackHit();
        }

        if (--this.attackAnimationTicks <= 0) {
            this.attackAnimationTicks = 0;
        }
    }

    private void performAttackHit() {
        AABB hitBox = this.getBoundingBox()
                .inflate(ATTACK_REACH_RADIUS, 1.5D, ATTACK_REACH_RADIUS)
                .expandTowards(this.getViewVector(1.0F).scale(1.9D));

        List<LivingEntity> victims = this.level().getEntitiesOfClass(
                LivingEntity.class,
                hitBox,
                this::canTargetEntity
        );

        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : victims) {
            if (!this.isInFront(victim.position(), -0.25D)) {
                continue;
            }

            if (victim.hurt(this.nightmareDamageSource(), damage)) {
                this.applyNightmareStrikeEffects(victim);
                this.knockAway(victim, 1.15D, 0.3D);
            }
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.6D, this.getZ(), 12, 0.9D, 0.35D, 0.9D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.6D, this.getZ(), 8, 0.7D, 0.25D, 0.7D, 0.03D);
        }
    }

    private void playNightmareBiteSound() {
        this.playSound(AntarchySoundEvents.NIGHTMARE_BITE.get(), 1.3F, 0.72F + this.random.nextFloat() * 0.08F);
    }

    private void applyNightmareStrikeEffects(LivingEntity target) {
        if (target instanceof Player) {
            target.addEffect(new MobEffectInstance(AntarchyObjects.DREAD.get(), DREAD_TICKS, 0));
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WEAKNESS_TICKS, 0));
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, target.getX(), target.getY(0.8D), target.getZ(), 10, 0.28D, 0.22D, 0.28D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.22D, 0.16D, 0.22D, 0.02D);
        }
    }

    private void knockAway(LivingEntity target, double horizontalStrength, double verticalStrength) {
        Vec3 direction = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 push = direction.normalize().scale(horizontalStrength);
        target.push(push.x, verticalStrength, push.z);
        target.hurtMarked = true;
    }

    private DamageSource nightmareDamageSource() {
        return this.level() instanceof ServerLevel serverLevel
                ? AntarchyDamageSources.nightmareMauling(serverLevel, this)
                : this.damageSources().mobAttack(this);
    }

    private boolean tryBreakBlocksToTarget(LivingEntity target) {
        int startY = Mth.floor(this.getY());
        int endY = Math.max(Mth.floor(target.getY()), startY - 16);
        int x = Mth.floor(this.getX());
        int z = Mth.floor(this.getZ());

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = startY; y >= endY; y--) {
            cursor.set(x, y, z);
            if (this.level().getBlockState(cursor).is(AntarchyTags.Blocks.NIGHTMARE_BREAKABLE)) {
                if (this.blockBreakCooldown <= 0) {
                    this.level().destroyBlock(cursor.immutable(), true, this);
                    this.blockBreakCooldown = BLOCK_BREAK_TICKS;
                }
                this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
                return true;
            }
        }

        return false;
    }

    @Nullable
    private LivingEntity findBestAttackTarget(@Nullable LivingEntity currentTarget) {
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        return this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(followRange, followRange * 0.5D, followRange),
                        this::canTargetEntity
                ).stream()
                .min(Comparator.comparingDouble(entity -> this.scoreAttackTarget(entity, currentTarget)))
                .orElse(null);
    }

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType()) {
            return false;
        }

        if (entity.getType().is(AntarchyTags.Entities.NIGHTMARE_NO_ATTACK)) {
            return false;
        }

        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }

        return entity instanceof Mob && entity.isAttackable();
    }

    private double scoreAttackTarget(LivingEntity candidate, @Nullable LivingEntity currentTarget) {
        double score = this.distanceToSqr(candidate);
        if (candidate instanceof Player) {
            score -= 10000.0D;
        }
        if (candidate == currentTarget) {
            score -= 18.0D;
        }
        if (this.hasLineOfSight(candidate)) {
            score -= 12.0D;
        } else {
            score += 16.0D;
        }
        if (this.isInFront(candidate.position(), -0.15D)) {
            score -= 4.0D;
        }
        return score;
    }

    private boolean shouldUseFlight() {
        LivingEntity target = this.getTarget();
        return (target != null && this.canTargetEntity(target) && this.shouldUseFlightForTarget(target))
                || (this.patrolTarget != null && this.patrolTarget.y > this.getY() + 1.5D);
    }

    private boolean shouldUseFlightForTarget(LivingEntity target) {
        if (this.ceilingAvoidanceTicks > 0) {
            return false;
        }

        if (!target.onGround() || target.getY() > this.getY() + 2.5D) {
            this.groundCombatMode = false;
            return true;
        }

        double distanceSqr = this.distanceToSqr(target);

        if (this.isHoveringAboveTarget(target)) {
            this.groundCombatMode = true;
            return false;
        }

        if (this.groundCombatMode) {
            if (distanceSqr <= FLIGHT_RETURN_RANGE_SQR) {
                return false;
            }

            this.groundCombatMode = false;
            return true;
        }

        if (this.onGround() && distanceSqr <= GROUND_APPROACH_RANGE_SQR) {
            this.groundCombatMode = true;
            return false;
        }

        if (distanceSqr > FLIGHT_REENGAGE_RANGE_SQR) {
            return true;
        }

        return !this.onGround() && distanceSqr > CLOSE_APPROACH_RANGE_SQR;
    }

    private boolean resolveCombatFlightMode(LivingEntity target) {
        boolean desiredFly = this.shouldUseFlightForTarget(target);
        boolean currentFly = !this.groundCombatMode;

        if (desiredFly == currentFly) {
            this.flightModeCommitTicks = FLIGHT_MODE_COMMIT_TICKS;
            return desiredFly;
        }

        if (this.flightModeCommitTicks > 0) {
            this.flightModeCommitTicks--;
            return currentFly;
        }

        this.groundCombatMode = !desiredFly;
        this.flightModeCommitTicks = FLIGHT_MODE_COMMIT_TICKS;
        return desiredFly;
    }

    private boolean isInFront(Vec3 position, double minimumDot) {
        Vec3 forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        Vec3 toTarget = position.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D || toTarget.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return forward.normalize().dot(toTarget.normalize()) >= minimumDot;
    }

    private boolean canStartAttackOn(LivingEntity target) {
        if (!this.hasLineOfSight(target) || this.distanceToSqr(target) > ATTACK_START_RANGE_SQR) {
            return false;
        }

        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double verticalDistance = Math.abs(target.getEyeY() - this.getEyeY());
        return horizontalDistanceSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE * ATTACK_COMMIT_HORIZONTAL_RANGE
                && verticalDistance <= 4.0D;
    }

    private double horizontalDistanceToSqr(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        return dx * dx + dz * dz;
    }

    private void faceTowardTarget(LivingEntity target, float maxTurnDegrees) {
        Vec3 toTarget = target.getEyePosition().subtract(this.getEyePosition());
        double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontal > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(toTarget.z, toTarget.x) * (180.0D / Math.PI)) - 90.0F;
            float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, maxTurnDegrees);
            this.setYRot(nextYaw);
            this.yBodyRot = nextYaw;
            this.yHeadRot = nextYaw;
        }
        if (toTarget.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(toTarget.y, horizontal) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, maxTurnDegrees * 0.7F));
        }
    }

    private Vec3 createCombatFlightTarget(LivingEntity target) {
        double distanceToTarget = this.distanceTo(target);
        if (distanceToTarget > 10.0D) {
            Vec3 direct = target.getEyePosition().add(0.0D, 0.5D, 0.0D);
            return this.getStableFlightTarget(direct, 8);
        }

        this.currentOrbitAngle += 0.022D * this.orbitDirection;
        double radius = 4.1D;
        double angle = this.currentOrbitAngle + this.getId() * 0.09D;
        double x = target.getX() + Math.cos(angle) * radius;
        double z = target.getZ() + Math.sin(angle) * radius;
        double y = Mth.clamp(
                target.getY() + COMBAT_FLIGHT_BASE_HEIGHT + Math.sin((this.tickCount + this.getId()) * 0.16D) * COMBAT_FLIGHT_HEIGHT_SPAN,
                target.getY() + 0.55D,
                target.getY() + 1.45D
        );
        return this.getStableFlightTarget(new Vec3(x, y, z), 8);
    }

    private Vec3 createStrikeFlightTarget(LivingEntity target) {
        Vec3 offset = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        if (offset.lengthSqr() < 1.0E-4D) {
            offset = this.getViewVector(1.0F).multiply(-1.0D, 0.0D, -1.0D);
        }
        if (offset.lengthSqr() < 1.0E-4D) {
            offset = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 desired = target.getEyePosition()
                .add(offset.normalize().scale(2.1D))
                .add(0.0D, 0.05D, 0.0D);
        return this.getStableFlightTarget(desired, 4);
    }

    private Vec3 createGroundApproachTarget(LivingEntity target) {
        return new Vec3(target.getX(), target.getY() + 0.85D, target.getZ());
    }

    private Vec3 findPatrolTarget() {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 18; attempt++) {
            int x = origin.getX() + this.random.nextInt(29) - 14;
            int z = origin.getZ() + this.random.nextInt(29) - 14;
            BlockPos terrainPos = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            boolean airPatrol = this.random.nextFloat() < AIR_PATROL_CHANCE;
            double y = airPatrol
                    ? terrainPos.getY() + PATROL_AIR_MIN_HEIGHT + this.random.nextDouble() * PATROL_AIR_HEIGHT_SPAN
                    : terrainPos.getY() + 1.0D;
            Vec3 desired = new Vec3(x + 0.5D, y, z + 0.5D);
            Vec3 openTarget = this.findNearestFlightTarget(desired);
            if (openTarget.distanceToSqr(desired) <= 16.0D) {
                return openTarget;
            }
        }

        BlockPos fallbackTerrain = this.level().getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.offset(this.random.nextInt(25) - 12, 0, this.random.nextInt(25) - 12)
        );
        return new Vec3(fallbackTerrain.getX() + 0.5D, fallbackTerrain.getY() + 1.0D, fallbackTerrain.getZ() + 0.5D);
    }

    private Vec3 findNearestFlightTarget(Vec3 desired) {
        BlockPos desiredPos = BlockPos.containing(desired);
        Vec3 best = null;
        double bestScore = Double.MAX_VALUE;
        for (int attempt = 0; attempt < 14; attempt++) {
            BlockPos candidate = desiredPos.offset(
                    this.random.nextInt(7) - 3,
                    this.random.nextInt(5) - 2,
                    this.random.nextInt(7) - 3
            );
            if (!this.isOpenFlightSpace(candidate) || !this.isCeilingBuffered(candidate)) {
                continue;
            }
            Vec3 candidateCenter = Vec3.atCenterOf(candidate);
            double score = candidateCenter.distanceToSqr(desired) + Math.max(0.0D, candidateCenter.y - desired.y) * 6.0D;
            if (score < bestScore) {
                best = candidateCenter;
                bestScore = score;
            }
        }
        if (best != null) {
            return best;
        }

        for (int down = 0; down <= 12; down++) {
            BlockPos lowered = desiredPos.below(down);
            if (this.isOpenFlightSpace(lowered) && this.isCeilingBuffered(lowered)) {
                return Vec3.atCenterOf(lowered);
            }
        }

        return desired;
    }

    private Vec3 getStableFlightTarget(Vec3 desired, int retargetTicks) {
        if (this.flightTargetRetargetTicks > 0
                && this.cachedFlightTarget != null
                && this.cachedFlightTargetAnchor != null
                && this.cachedFlightTargetAnchor.distanceToSqr(desired) <= 6.25D) {
            this.flightTargetRetargetTicks--;
            return this.cachedFlightTarget;
        }

        this.cachedFlightTarget = this.findNearestFlightTarget(desired);
        this.cachedFlightTargetAnchor = desired;
        this.flightTargetRetargetTicks = retargetTicks;
        return this.cachedFlightTarget;
    }

    private boolean isOpenFlightSpace(BlockPos pos) {
        return this.level().isEmptyBlock(pos)
                && this.level().isEmptyBlock(pos.above())
                && this.level().isEmptyBlock(pos.above(2));
    }

    private boolean isCeilingBuffered(BlockPos pos) {
        for (int i = 3; i <= FLIGHT_CEILING_CLEARANCE_BLOCKS; i++) {
            if (!this.level().isEmptyBlock(pos.above(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTooCloseToCeiling() {
        BlockPos current = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        for (int i = 1; i <= FLIGHT_CEILING_CLEARANCE_BLOCKS; i++) {
            if (!this.level().isEmptyBlock(current.above(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean isHoveringAboveTarget(LivingEntity target) {
        if (!target.onGround()) {
            return false;
        }

        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double verticalDelta = this.getY() - target.getY();
        return horizontalDistanceSqr <= 25.0D && verticalDelta > 1.6D;
    }

    private void tickClientParticles() {
        if ((this.isRoaring() || this.attackAnimationTicks > 0) && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + 1.2D + this.random.nextDouble() * 1.1D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    0.0D,
                    0.03D,
                    0.0D
            );
        }
    }

    private void updateFlightRotation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
            float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, 4.0F);
            this.setYRot(nextYaw);
            this.yBodyRot = Mth.approachDegrees(this.yBodyRot, nextYaw, 2.5F);
            this.yHeadRot = Mth.approachDegrees(this.yHeadRot, nextYaw, 3.5F);
        }

        if (!this.onGround() && velocity.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float) (-(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 3.0F));
        } else {
            this.setXRot(Mth.approachDegrees(this.getXRot(), 0.0F, 2.0F));
        }
    }

    private final class NightmareCombatMovementGoal extends Goal {
        private NightmareCombatMovementGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return NightmareEntity.this.getTarget() != null
                    && !NightmareEntity.this.isDeadOrDying()
                    && !NightmareEntity.this.isRoaring()
                    && NightmareEntity.this.attackAnimationTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            NightmareEntity.this.groundCombatMode = false;
        }

        @Override
        public void stop() {
            NightmareEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = NightmareEntity.this.getTarget();
            if (target == null) {
                return;
            }

            if (NightmareEntity.this.attackCooldown <= 0 && NightmareEntity.this.canStartAttackOn(target)) {
                NightmareEntity.this.startAttack(target);
                return;
            }

            NightmareEntity.this.tickCombatMovement(target);
        }
    }

    private final class NightmareAirPatrolGoal extends Goal {
        private NightmareAirPatrolGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return NightmareEntity.this.getTarget() == null
                    && !NightmareEntity.this.isDeadOrDying()
                    && !NightmareEntity.this.isRoaring()
                    && NightmareEntity.this.attackAnimationTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            NightmareEntity.this.patrolTarget = null;
            NightmareEntity.this.patrolRetargetTicks = 0;
            NightmareEntity.this.tickPatrolMovement();
        }

        @Override
        public void stop() {
            NightmareEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (NightmareEntity.this.getTarget() != null
                    || NightmareEntity.this.isRoaring()
                    || NightmareEntity.this.attackAnimationTicks > 0) {
                return;
            }

            NightmareEntity.this.tickPatrolMovement();
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            this.wingFlapCooldown = 0;
            return;
        }

        if (this.isRoaring()) {
            this.setAnimationState(ANIM_ROAR);
            this.wingFlapCooldown = 0;
            return;
        }

        if (this.attackAnimationTicks > 0) {
            this.setAnimationState(this.onGround() ? ANIM_ATTACK : ANIM_FLY_ATTACK);
            this.tickWingFlapSound();
            return;
        }

        if (!this.onGround() && this.airborneTicks > 0 && this.airborneTicks <= 10 && this.shouldUseFlight()) {
            this.setAnimationState(ANIM_TAKEOFF);
            this.wingFlapCooldown = 0;
            return;
        }

        if (!this.onGround() && this.hurtTime > 0) {
            this.setAnimationState(ANIM_FLY_DAMAGED);
            this.tickWingFlapSound();
            return;
        }

        if (!this.onGround()) {
            this.setAnimationState(ANIM_FLY);
            this.tickWingFlapSound();
            return;
        }

        Vec3 velocity = this.getDeltaMovement();
        if (this.onGround() && velocity.horizontalDistanceSqr() > 0.008D) {
            this.setAnimationState(ANIM_WALK);
            this.wingFlapCooldown = 0;
            return;
        }

        this.setAnimationState(ANIM_IDLE);
        this.wingFlapCooldown = 0;
    }

    private void tickWingFlapSound() {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }

        if (this.onGround() || this.isRoaring() || this.isDeadOrDying()) {
            this.wingFlapCooldown = 0;
            return;
        }

        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01D) {
            return;
        }

        if (this.wingFlapCooldown > 0) {
            this.wingFlapCooldown--;
            return;
        }

        this.playSound(AntarchySoundEvents.NIGHTMARE_FLAP.get(), 1.25F, 0.85F + this.random.nextFloat() * 0.08F);
        this.wingFlapCooldown = 5 + this.random.nextInt(4);
    }

}
