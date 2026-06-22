package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;

import com.craisinlord.antarchy.content.damage.AntarchyDamageTypes;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
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
import software.bernie.geckolib.animation.keyframe.event.builtin.AutoPlayingSoundKeyframeHandler;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KrakenEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ATTACK_STATE = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.INT);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ACTION_TICKS_KEY = "ActionTicks";
    private static final String PHASE_TRANSITION_TICKS_KEY = "PhaseTransitionTicks";
    private static final String NEXT_ATTACK_INDEX_KEY = "NextAttackIndex";
    private static final String SLAM_COOLDOWN_KEY = "SlamCooldown";
    private static final String PHASE_SUMMON_COOLDOWN_KEY = "PhaseSummonCooldown";
    private static final String LAST_ATTACK_STATE_KEY = "LastAttackState";
    private static final String AGGRO_TRIGGERED_KEY = "AggroTriggered";
    private static final String GRAB_TARGET_KEY = "GrabTarget";
    private static final double BOSS_BAR_RANGE = 40.0D;
    private static final int ATTACK_NONE = 0;
    private static final int ATTACK_GRAB = 1;
    private static final int ATTACK_SWIPE = 2;
    private static final int ATTACK_SLAM = 3;
    private static final int ANIM_IDLE = 0;
    private static final int ANIM_IDLE_TWO = 1;
    private static final int ANIM_SWIM = 2;
    private static final int ANIM_SWIPE = 3;
    private static final int ANIM_GRAB = 4;
    private static final int ANIM_DEATH = 5;
    private static final int DEATH_ANIM_TICKS = 60;
    private static final ResourceLocation PHASE_TWO_SPEED_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_phase_two_speed");
    private static final ResourceLocation PHASE_TWO_FLYING_SPEED_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_phase_two_flying_speed");
    private static final ResourceLocation PHASE_TWO_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_phase_two_damage");
    private static final double PHASE_TWO_SPEED_BONUS = 0.15D;
    private static final double PHASE_TWO_DAMAGE_BONUS = 4.0D;
    private static final AttributeModifier PHASE_TWO_SPEED = new AttributeModifier(PHASE_TWO_SPEED_ID, PHASE_TWO_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier PHASE_TWO_FLYING_SPEED = new AttributeModifier(PHASE_TWO_FLYING_SPEED_ID, PHASE_TWO_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier PHASE_TWO_DAMAGE = new AttributeModifier(PHASE_TWO_DAMAGE_ID, PHASE_TWO_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation SWIPE_ANIM = RawAnimation.begin().thenPlay("Swipe");
    private static final RawAnimation GRAB_ANIM = RawAnimation.begin().thenPlay("Grab");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("Death");

    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.literal("The Kraken"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private Vec3 patrolTarget;
    private int patrolRetargetTicks;
    private int attackCooldown;
    private int actionTicks;
    private int phaseTransitionTicks;
    private int nextAttackIndex;
    private int slamCooldown;
    private int phaseSummonCooldown;
    private int lastAttackState;
    private boolean aggroTriggered;
    private int grabbedTargetId = -1;
    private boolean spawnedPhaseMinions;
    private int lightningStrikeCooldown;
    private int lightningAmbientCooldown;
    private int strafeRetargetTicks;
    private float orbitDirection = 1.0F;
    private boolean stormActive;
    public KrakenEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.xpReward = 60;
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 750.0D)
                .add(Attributes.ATTACK_DAMAGE, 45.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D);
    }

    public static boolean canSpawn(EntityType<KrakenEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_STATE, ATTACK_NONE);
        builder.define(ROARING, false);
        builder.define(PHASE_TWO, false);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
        hurtByTargetGoal.setAlertOthers(MissileSquidEntity.class);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
    }

    private PlayState mainAnimController(AnimationState<KrakenEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_GRAB -> state.setAndContinue(GRAB_ANIM);
            case ANIM_SWIPE -> state.setAndContinue(SWIPE_ANIM);
            case ANIM_SWIM -> state.setAndContinue(SWIM_ANIM);
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnData) {
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(com.craisinlord.antarchy.config.AntarchySettings.krakenHealth());
        this.setHealth((float) com.craisinlord.antarchy.config.AntarchySettings.krakenHealth());
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(com.craisinlord.antarchy.config.AntarchySettings.krakenAttackDamage());
        this.playSound(AntarchySoundEvents.KRAKEN_SUMMON.get(), 2.2F, 0.9F + this.random.nextFloat() * 0.08F);
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    public void tick() {
        super.tick();
        this.setAirSupply(this.getMaxAirSupply());
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.tickClientParticles();
            this.updateSwimRotation();
            return;
        }

        this.updateBossBarPlayers();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickLightning();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }
        if (this.phaseSummonCooldown > 0) {
            this.phaseSummonCooldown--;
        }

        LivingEntity target = this.getTarget();
        if ((target == null || !target.isAlive()) && this.tickCount % 10 == 0) {
            Player nearbyPlayer = this.level().getNearestPlayer(this, BOSS_BAR_RANGE);
            if (nearbyPlayer != null && this.canAttack(nearbyPlayer)) {
                this.setTarget(nearbyPlayer);
                target = nearbyPlayer;
            }
        }

        if (target == null || !target.isAlive()) {
            this.resetCombatState();
            this.tickPatrolMovement();
            this.updateSwimRotation();
            this.updateAnimationState();
            return;
        }

        this.getLookControl().setLookAt(target, 25.0F, 20.0F);

        if (!this.aggroTriggered) {
            this.startAggroRoar();
        }

        if (!this.isPhaseTwo() && this.phaseTransitionTicks <= 0 && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.startPhaseTransition();
        }

        if (this.isRoaring()) {
            this.tickRoarState(target);
            this.updateSwimRotation();
            this.updateAnimationState();
            return;
        }

        if (this.phaseTransitionTicks > 0) {
            this.tickPhaseTransition(target);
            this.updateSwimRotation();
            this.updateAnimationState();
            return;
        }

        if (this.getAttackState() != ATTACK_NONE) {
            this.tickCurrentAttack(target);
            this.updateSwimRotation();
            this.updateAnimationState();
            return;
        }

        this.tickPhaseTwoPressure(target);
        this.tickPursuitMovement(target);

        if (this.attackCooldown <= 0) {
            int nextAttack = this.chooseNextAttack(target);
            if (nextAttack != ATTACK_NONE) {
                this.startAttack(nextAttack);
            }
        }

        this.updateSwimRotation();
        this.updateAnimationState();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(this.isInWater() ? 0.92D : 0.91D));
            return;
        }

        super.travel(travelVector);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(AntarchyDamageTypes.KRAKEN_LIGHTNING)
                || source.getEntity() instanceof MissileSquidEntity) {
            return false;
        }

        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.KRAKEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.KRAKEN_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.5F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(ATTACK_COOLDOWN_KEY, this.attackCooldown);
        tag.putInt(ACTION_TICKS_KEY, this.actionTicks);
        tag.putInt(PHASE_TRANSITION_TICKS_KEY, this.phaseTransitionTicks);
        tag.putInt(NEXT_ATTACK_INDEX_KEY, this.nextAttackIndex);
        tag.putInt(SLAM_COOLDOWN_KEY, this.slamCooldown);
        tag.putInt(PHASE_SUMMON_COOLDOWN_KEY, this.phaseSummonCooldown);
        tag.putInt(LAST_ATTACK_STATE_KEY, this.lastAttackState);
        tag.putBoolean(AGGRO_TRIGGERED_KEY, this.aggroTriggered);
        if (this.grabbedTargetId >= 0) {
            tag.putInt(GRAB_TARGET_KEY, this.grabbedTargetId);
        }
        tag.putBoolean("PhaseTwo", this.isPhaseTwo());
        tag.putBoolean("Roaring", this.isRoaring());
        tag.putInt("AttackState", this.getAttackState());
        tag.putInt("AnimationState", this.getAnimationState());
        tag.putBoolean("SpawnedPhaseMinions", this.spawnedPhaseMinions);
        tag.putInt("LightningStrikeCooldown", this.lightningStrikeCooldown);
        tag.putInt("LightningAmbientCooldown", this.lightningAmbientCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.attackCooldown = tag.getInt(ATTACK_COOLDOWN_KEY);
        this.actionTicks = tag.getInt(ACTION_TICKS_KEY);
        this.phaseTransitionTicks = tag.getInt(PHASE_TRANSITION_TICKS_KEY);
        this.nextAttackIndex = tag.getInt(NEXT_ATTACK_INDEX_KEY);
        this.slamCooldown = tag.getInt(SLAM_COOLDOWN_KEY);
        this.phaseSummonCooldown = tag.getInt(PHASE_SUMMON_COOLDOWN_KEY);
        this.lastAttackState = tag.contains(LAST_ATTACK_STATE_KEY) ? tag.getInt(LAST_ATTACK_STATE_KEY) : ATTACK_NONE;
        this.aggroTriggered = tag.getBoolean(AGGRO_TRIGGERED_KEY);
        this.grabbedTargetId = tag.contains(GRAB_TARGET_KEY) ? tag.getInt(GRAB_TARGET_KEY) : -1;
        this.spawnedPhaseMinions = tag.getBoolean("SpawnedPhaseMinions");
        this.entityData.set(ROARING, tag.getBoolean("Roaring"));
        this.entityData.set(ATTACK_STATE, tag.getInt("AttackState"));
        this.entityData.set(PHASE_TWO, tag.getBoolean("PhaseTwo"));
        this.entityData.set(ANIMATION_STATE, tag.contains("AnimationState") ? tag.getInt("AnimationState") : ANIM_IDLE);
        this.lightningStrikeCooldown = tag.getInt("LightningStrikeCooldown");
        this.lightningAmbientCooldown = tag.getInt("LightningAmbientCooldown");
        this.stormActive = false;
        if (this.isPhaseTwo()) {
            this.applyPhaseTwoBuffs();
        }
        this.updateAnimationState();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.clearKrakenStorm();
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public boolean isPhaseTwo() {
        return this.entityData.get(PHASE_TWO);
    }

    public int getAttackState() {
        return this.entityData.get(ATTACK_STATE);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    private void setPhaseTwo(boolean phaseTwo) {
        this.entityData.set(PHASE_TWO, phaseTwo);
    }

    private void setAttackState(int attackState) {
        this.entityData.set(ATTACK_STATE, attackState);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void startAggroRoar() {
        this.aggroTriggered = true;
        this.actionTicks = 30;
        this.setRoaring(true);
        this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 1.8F, 0.78F);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
        this.updateAnimationState();
    }

    private void tickRoarState(LivingEntity target) {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
        this.getLookControl().setLookAt(target, 25.0F, 20.0F);

        if (--this.actionTicks <= 0) {
            this.actionTicks = 0;
            this.setRoaring(false);
            this.attackCooldown = 18;
        }
    }

    private void startPhaseTransition() {
        this.phaseTransitionTicks = 50;
        this.actionTicks = 0;
        this.setAttackState(ATTACK_NONE);
        this.setRoaring(false);
        this.grabbedTargetId = -1;
        this.spawnedPhaseMinions = false;
        this.phaseSummonCooldown = 0;
        this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 2.0F, 0.82F);
        this.getNavigation().stop();
        this.updateAnimationState();
    }

    private void tickPhaseTransition(LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.45D));
        this.getLookControl().setLookAt(target, 25.0F, 20.0F);

        if (!this.spawnedPhaseMinions && this.phaseTransitionTicks <= 40) {
            this.spawnPhaseMinions(1 + this.random.nextInt(2), true, target);
            this.spawnedPhaseMinions = true;
        }

        if (this.phaseTransitionTicks % 8 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, this.getX(), this.getY() + 1.0D, this.getZ(), 12, 1.0D, 0.7D, 1.0D, 0.02D);
        }

        if (--this.phaseTransitionTicks <= 0) {
            this.phaseTransitionTicks = 0;
            this.setPhaseTwo(true);
            this.applyPhaseTwoBuffs();
            this.attackCooldown = 25;
            this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 2.0F, 0.72F);
        }
    }

    private void tickCurrentAttack(LivingEntity target) {
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 25.0F);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));

        switch (this.getAttackState()) {
            case ATTACK_GRAB -> this.tickGrabAttack(target);
            case ATTACK_SWIPE -> this.tickSwipeAttack();
            case ATTACK_SLAM -> this.tickSlamAttack();
            default -> this.finishAttack();
        }
    }

    private void startAttack(int attackState) {
        this.setAttackState(attackState);
        this.grabbedTargetId = -1;

        if (attackState == ATTACK_GRAB) {
            this.actionTicks = 55;
            this.playSound(AntarchySoundEvents.KRAKEN_ATTACK.get(), 1.45F, 0.9F);
        } else if (attackState == ATTACK_SWIPE) {
            this.actionTicks = 40;
            this.playSound(AntarchySoundEvents.KRAKEN_SPIN.get(), 1.3F, 0.95F);
        } else if (attackState == ATTACK_SLAM) {
            this.actionTicks = 34;
            this.playSound(AntarchySoundEvents.KRAKEN_ATTACK.get(), 1.5F, 0.82F);
        }
    }

    private void tickGrabAttack(LivingEntity target) {
        if (this.actionTicks == 48 && this.distanceToSqr(target) <= 42.25D) {
            this.grabbedTargetId = target.getId();
        }

        LivingEntity grabbedTarget = this.getGrabbedTarget();
        if (grabbedTarget != null && grabbedTarget.isAlive()) {
            this.holdGrabbedTarget(grabbedTarget);
            if (this.actionTicks <= 45 && this.actionTicks > 10 && this.actionTicks % 10 == 0) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    grabbedTarget.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), 4.0F);
                }
            }
        }

        if (--this.actionTicks <= 0) {
            if (grabbedTarget != null && grabbedTarget.isAlive()) {
                this.knockAway(grabbedTarget, 2.8D, 4.2D);
            }
            this.grabbedTargetId = -1;
            this.finishAttack();
        }
    }

    private void tickSwipeAttack() {
        if (this.actionTicks == 10) {
            this.performSwipe();
        }

        if (--this.actionTicks <= 0) {
            this.finishAttack();
        }
    }

    private void tickSlamAttack() {
        if (this.actionTicks == 8) {
            this.performTentacleSlam();
        }

        if (--this.actionTicks <= 0) {
            this.finishAttack();
        }
    }

    private void finishAttack() {
        int finishedAttack = this.getAttackState();
        this.setAttackState(ATTACK_NONE);
        this.actionTicks = 0;
        this.attackCooldown = this.isPhaseTwo() ? 12 : 18;
        this.grabbedTargetId = -1;
        this.lastAttackState = finishedAttack;
        if (finishedAttack == ATTACK_SLAM) {
            this.slamCooldown = this.isPhaseTwo() ? 110 : 150;
        }
        this.updateAnimationState();
    }

    private void tickPursuitMovement(LivingEntity target) {
        this.patrolTarget = null;
        if (this.strafeRetargetTicks-- <= 0) {
            this.strafeRetargetTicks = 10 + this.random.nextInt(12);
            if (this.random.nextFloat() < 0.4F) {
                this.orbitDirection *= -1.0F;
            }
        }

        Vec3 attackPoint = this.createCombatFlightTarget(target);
        this.getMoveControl().setWantedPosition(attackPoint.x, attackPoint.y, attackPoint.z, this.isPhaseTwo() ? 1.2D : 1.1D);
    }

    private void tickPatrolMovement() {
        if (this.patrolRetargetTicks-- <= 0 || this.patrolTarget == null || this.position().distanceToSqr(this.patrolTarget) < 6.0D) {
            this.patrolRetargetTicks = 28 + this.random.nextInt(24);
            this.patrolTarget = this.findPatrolTarget();
        }

        if (this.patrolTarget != null) {
            this.getMoveControl().setWantedPosition(this.patrolTarget.x, this.patrolTarget.y, this.patrolTarget.z, 0.45D);
        }
    }

    private void resetCombatState() {
        this.setAttackState(ATTACK_NONE);
        this.setRoaring(false);
        this.actionTicks = 0;
        this.phaseTransitionTicks = 0;
        this.grabbedTargetId = -1;
        this.aggroTriggered = false;
        this.spawnedPhaseMinions = this.isPhaseTwo();
        this.updateAnimationState();
    }

    private int chooseNextAttack(LivingEntity target) {
        int[] attackRotation = new int[] {ATTACK_GRAB, ATTACK_SWIPE, ATTACK_SLAM};
        int[] weightedAttacks = new int[attackRotation.length];
        int[] weights = new int[attackRotation.length];
        int eligibleCount = 0;
        int totalWeight = 0;

        for (int attack : attackRotation) {
            if (!this.canUseAttack(attack, target)) {
                continue;
            }

            int weight = this.getAttackWeight(attack);
            if (attack == this.lastAttackState) {
                weight = Math.max(1, weight / 3);
            }
            if (weight <= 0) {
                continue;
            }

            weightedAttacks[eligibleCount] = attack;
            weights[eligibleCount] = weight;
            totalWeight += weight;
            eligibleCount++;
        }

        if (eligibleCount == 0) {
            return ATTACK_NONE;
        }

        int roll = this.random.nextInt(totalWeight);
        for (int i = 0; i < eligibleCount; i++) {
            roll -= weights[i];
            if (roll < 0) {
                return weightedAttacks[i];
            }
        }

        return weightedAttacks[eligibleCount - 1];
    }

    private int getAttackWeight(int attackState) {
        return switch (attackState) {
            case ATTACK_GRAB -> 4;
            case ATTACK_SWIPE -> 5;
            case ATTACK_SLAM -> this.isPhaseTwo() ? 1 : 0;
            default -> 0;
        };
    }

    private void tickPhaseTwoPressure(LivingEntity target) {
        if (!this.isPhaseTwo() || this.phaseSummonCooldown > 0) {
            return;
        }
        if (this.countNearbyMissileSquids() >= 12) {
            this.phaseSummonCooldown = 40;
            return;
        }
        this.spawnPhaseMinions(1 + this.random.nextInt(2), true, target);
        this.phaseSummonCooldown = 90 + this.random.nextInt(50);
    }

    private int countNearbyMissileSquids() {
        return this.level().getEntitiesOfClass(
                MissileSquidEntity.class,
                this.getBoundingBox().inflate(28.0D),
                squid -> squid.isAlive()
        ).size();
    }

    private boolean canUseAttack(int attackState, LivingEntity target) {
        double distanceToTarget = this.distanceToSqr(target);
        return switch (attackState) {
            case ATTACK_GRAB -> distanceToTarget <= 64.0D;
            case ATTACK_SWIPE -> distanceToTarget <= 81.0D && this.isTargetInFront(target, 0.1D);
            case ATTACK_SLAM -> this.slamCooldown <= 0 && distanceToTarget <= 144.0D;
            default -> false;
        };
    }

    private boolean isTargetInFront(LivingEntity target, double minimumDot) {
        Vec3 forward = this.getViewVector(1.0F);
        Vec3 toTarget = target.position().subtract(this.position()).normalize();
        return forward.dot(toTarget) >= minimumDot;
    }

    private void performSwipe() {
        AABB hitBox = this.getBoundingBox().inflate(7.0D, 3.0D, 7.0D);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, hitBox, player -> player.isAlive() && this.isTargetInFront(player, -0.15D));
        for (Player player : players) {
            this.hurtAndKnockback(player, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE), 1.75D, 0.45D);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.8D, this.getZ(), 24, 1.2D, 0.4D, 1.2D, 0.08D);
        }
    }

    private void performTentacleSlam() {
        AABB hitBox = this.getBoundingBox().inflate(8.5D, 2.5D, 8.5D);
        List<Player> players = this.level().getEntitiesOfClass(Player.class, hitBox, LivingEntity::isAlive);
        float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.75F;
        for (Player player : players) {
            this.hurtAndKnockback(player, damage, 1.35D, 0.62D);
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5D, this.getZ(), 36, 1.8D, 0.5D, 1.8D, 0.12D);
        }
    }

    private void holdGrabbedTarget(LivingEntity target) {
        Vec3 anchor = this.position()
                .add(this.getViewVector(1.0F).normalize().scale(this.getBbWidth() * 0.65D))
                .add(0.0D, this.getBbHeight() * 0.28D, 0.0D);
        target.setDeltaMovement(anchor.subtract(target.position()).scale(0.35D));
        target.resetFallDistance();
        if (target instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(anchor.x, anchor.y, anchor.z, target.getYRot(), target.getXRot());
        } else {
            target.moveTo(anchor.x, anchor.y, anchor.z, target.getYRot(), target.getXRot());
        }
        target.hurtMarked = true;
    }

    private void hurtAndKnockback(LivingEntity target, float damage, double horizontalStrength, double verticalStrength) {
        if (!(this.level() instanceof ServerLevel serverLevel) || !target.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), damage)) {
            return;
        }

        this.knockAway(target, horizontalStrength, verticalStrength);
    }

    private void knockAway(LivingEntity target, double horizontalStrength, double verticalStrength) {
        Vec3 direction = target.position().subtract(this.position());
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = new Vec3(1.0D, 0.0D, 0.0D);
        }

        horizontal = horizontal.normalize().scale(horizontalStrength);
        target.push(horizontal.x, verticalStrength, horizontal.z);
        target.hurtMarked = true;
    }

    @Nullable
    private LivingEntity getGrabbedTarget() {
        if (this.grabbedTargetId < 0) {
            return null;
        }

        Entity entity = this.level().getEntity(this.grabbedTargetId);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private void spawnPhaseMinions(int desiredCount, boolean launched, @Nullable LivingEntity currentTarget) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int spawned = 0;

        for (int attempt = 0; attempt < desiredCount * 4 && spawned < desiredCount; attempt++) {
            Vec3 spawnPos = this.findPhaseMinionSpawnPos(currentTarget);
            MissileSquidEntity missileSquid = AntarchyObjects.MISSILE_SQUID.get().create(serverLevel);
            if (missileSquid == null) {
                continue;
            }

            missileSquid.setCanFly(true);
            missileSquid.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, this.getYRot(), 0.0F);
            serverLevel.addFreshEntity(missileSquid);
            if (launched) {
                missileSquid.launchAsProjectile(this, this.createPhaseMinionLaunchVelocity(spawnPos, currentTarget));
            } else if (currentTarget != null) {
                missileSquid.setTarget(currentTarget);
            }
            serverLevel.sendParticles(ParticleTypes.BUBBLE, missileSquid.getX(), missileSquid.getY() + 0.4D, missileSquid.getZ(), 14, 0.3D, 0.25D, 0.3D, 0.03D);
            spawned++;
        }

        if (spawned > 0) {
            this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 1.8F, this.isPhaseTwo() ? 0.74F : 0.82F);
        }
    }

    private Vec3 findPhaseMinionSpawnPos(@Nullable LivingEntity currentTarget) {
        Vec3 anchor = currentTarget != null ? currentTarget.position() : this.position();
        for (int attempt = 0; attempt < 18; attempt++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double radius = 7.0D + this.random.nextDouble() * 8.0D;
            double height = 4.0D + this.random.nextDouble() * (this.isPhaseTwo() ? 6.0D : 4.0D);
            BlockPos candidate = BlockPos.containing(
                    anchor.x + Math.cos(angle) * radius,
                    anchor.y + height,
                    anchor.z + Math.sin(angle) * radius
            );
            if (this.isOpenFlightSpace(candidate)) {
                return Vec3.atCenterOf(candidate);
            }
        }

        return this.position().add(0.0D, this.getBbHeight() * 0.6D, 0.0D);
    }

    private Vec3 createPhaseMinionLaunchVelocity(Vec3 spawnPos, @Nullable LivingEntity currentTarget) {
        Vec3 aimPoint = currentTarget != null
                ? currentTarget.getEyePosition()
                : this.position().add(this.getViewVector(1.0F).scale(10.0D));
        Vec3 direction = aimPoint.subtract(spawnPos);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = this.getViewVector(1.0F);
        }

        Vec3 spread = new Vec3(
                (this.random.nextDouble() - 0.5D) * 0.18D,
                (this.random.nextDouble() - 0.5D) * 0.12D,
                (this.random.nextDouble() - 0.5D) * 0.18D
        );
        return direction.normalize().scale(this.isPhaseTwo() ? 1.45D : 1.2D).add(spread);
    }

    private boolean isOpenFlightSpace(BlockPos candidate) {
        return (this.level().isEmptyBlock(candidate) || this.level().getFluidState(candidate).is(FluidTags.WATER))
                && (this.level().isEmptyBlock(candidate.above()) || this.level().getFluidState(candidate.above()).is(FluidTags.WATER));
    }

    private Vec3 findPatrolTarget() {
        BlockPos origin = this.blockPosition();
        for (int attempt = 0; attempt < 20; attempt++) {
            BlockPos candidate = origin.offset(
                    this.random.nextInt(29) - 14,
                    this.random.nextInt(9) - 4,
                    this.random.nextInt(29) - 14
            );
            if (!this.level().isEmptyBlock(candidate) && !this.level().getFluidState(candidate).is(FluidTags.WATER)) {
                continue;
            }
            if (!this.level().isEmptyBlock(candidate.above()) && !this.level().getFluidState(candidate.above()).is(FluidTags.WATER)) {
                continue;
            }

            double clampedY = Mth.clamp(candidate.getY() + 0.5D, this.getY() - 5.0D, this.getY() + 5.0D);
            return new Vec3(candidate.getX() + 0.5D, clampedY, candidate.getZ() + 0.5D);
        }

        return this.position().add(0.0D, 1.5D, 0.0D);
    }

    private Vec3 createCombatFlightTarget(LivingEntity target) {
        double radius = this.isPhaseTwo() ? 7.0D : 9.0D;
        double angle = this.tickCount * 0.16D * this.orbitDirection + this.getId() * 0.11D;
        double x = target.getX() + Math.cos(angle) * radius;
        double z = target.getZ() + Math.sin(angle) * radius;
        double y = Mth.clamp(
                target.getY() + 2.0D + Math.sin((this.tickCount + this.getId()) * 0.18D) * 1.0D,
                target.getY() - 2.0D,
                target.getY() + 5.0D
        );
        return this.findNearestFlightTarget(new Vec3(x, y, z));
    }

    private Vec3 findNearestFlightTarget(Vec3 desired) {
        BlockPos desiredPos = BlockPos.containing(desired);
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos candidate = desiredPos.offset(
                    this.random.nextInt(7) - 3,
                    this.random.nextInt(5) - 2,
                    this.random.nextInt(7) - 3
            );
            if (!this.level().isEmptyBlock(candidate) && !this.level().getFluidState(candidate).is(FluidTags.WATER)) {
                continue;
            }
            if (!this.level().isEmptyBlock(candidate.above()) && !this.level().getFluidState(candidate.above()).is(FluidTags.WATER)) {
                continue;
            }
            return Vec3.atCenterOf(candidate);
        }

        boolean desiredOpen = this.level().isEmptyBlock(desiredPos) || this.level().getFluidState(desiredPos).is(FluidTags.WATER);
        boolean desiredOpenAbove = this.level().isEmptyBlock(desiredPos.above()) || this.level().getFluidState(desiredPos.above()).is(FluidTags.WATER);
        return desiredOpen && desiredOpenAbove ? desired : this.position().add(0.0D, 1.5D, 0.0D);
    }

    private void tickLightning() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.stormActive && --this.lightningAmbientCooldown <= 0) {
            this.lightningAmbientCooldown = this.isPhaseTwo() ? 35 + this.random.nextInt(35) : 80 + this.random.nextInt(80);
            int ambientBolts = this.isPhaseTwo() ? 4 : 2;
            for (int i = 0; i < ambientBolts; i++) {
                this.spawnVisualLightning(serverLevel, this.getRandomStormStrikePos(serverLevel));
            }
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            if (--this.lightningStrikeCooldown <= 0) {
                this.lightningStrikeCooldown = this.isPhaseTwo() ? 26 + this.random.nextInt(20) : 60 + this.random.nextInt(80);
                int strikeBolts = this.isPhaseTwo() ? 2 : 1;
                for (int i = 0; i < strikeBolts; i++) {
                    this.spawnVisualLightning(serverLevel, this.getTargetStormStrikePos(target));
                }
                target.hurt(AntarchyDamageSources.krakenLightning(serverLevel, this), this.isPhaseTwo() ? 12.0F : 8.0F);
            }
        } else {
            this.lightningStrikeCooldown = this.isPhaseTwo() ? 30 : 60;
        }
    }

    private void setKrakenStormActive(boolean active) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (active) {
            this.stormActive = true;
            serverLevel.setWeatherParameters(0, 200, true, true);
            return;
        }

        if (this.stormActive) {
            this.stormActive = false;
            serverLevel.setWeatherParameters(6000, 0, false, false);
        }
    }

    private void clearKrakenStorm() {
        this.setKrakenStormActive(false);
    }

    private void updateBossBarPlayers() {
        if (this.tickCount % 20 != 0) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB bossBarArea = this.getBoundingBox().inflate(BOSS_BAR_RANGE, 16.0D, BOSS_BAR_RANGE);
        List<ServerPlayer> nearbyPlayers = serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                bossBarArea,
                player -> player.isAlive() && this.distanceToSqr(player) <= BOSS_BAR_RANGE * BOSS_BAR_RANGE
        );

        for (ServerPlayer player : List.copyOf(this.bossEvent.getPlayers())) {
            if (!nearbyPlayers.contains(player)) {
                this.bossEvent.removePlayer(player);
            }
        }

        for (ServerPlayer player : nearbyPlayers) {
            if (!this.bossEvent.getPlayers().contains(player)) {
                this.bossEvent.addPlayer(player);
            }
        }

        this.setKrakenStormActive(!nearbyPlayers.isEmpty());
    }

    private void applyPhaseTwoBuffs() {
        AttributeInstance movementSpeed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null && !movementSpeed.hasModifier(PHASE_TWO_SPEED_ID)) {
            movementSpeed.addPermanentModifier(PHASE_TWO_SPEED);
        }

        AttributeInstance flyingSpeed = this.getAttribute(Attributes.FLYING_SPEED);
        if (flyingSpeed != null && !flyingSpeed.hasModifier(PHASE_TWO_FLYING_SPEED_ID)) {
            flyingSpeed.addPermanentModifier(PHASE_TWO_FLYING_SPEED);
        }

        AttributeInstance attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage != null && !attackDamage.hasModifier(PHASE_TWO_DAMAGE_ID)) {
            attackDamage.addPermanentModifier(PHASE_TWO_DAMAGE);
        }
    }

    private Vec3 getRandomStormStrikePos(ServerLevel serverLevel) {
        double lx = this.getX() + (this.random.nextDouble() - 0.5D) * 56.0D;
        double lz = this.getZ() + (this.random.nextDouble() - 0.5D) * 56.0D;
        int ly = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(lx), Mth.floor(lz));
        return new Vec3(lx, ly, lz);
    }

    private Vec3 getTargetStormStrikePos(LivingEntity target) {
        double spread = this.isPhaseTwo() ? 4.0D : 1.5D;
        return new Vec3(
                target.getX() + (this.random.nextDouble() - 0.5D) * spread,
                target.getY(),
                target.getZ() + (this.random.nextDouble() - 0.5D) * spread
        );
    }

    private void spawnVisualLightning(ServerLevel serverLevel, Vec3 strikePos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt == null) {
            return;
        }

        bolt.moveTo(strikePos.x, strikePos.y, strikePos.z);
        bolt.setVisualOnly(true);
        serverLevel.addFreshEntity(bolt);
    }

    private void tickClientParticles() {
        if (this.isRoaring() || this.phaseTransitionTicks > 0 || this.getAttackState() != ATTACK_NONE) {
            if (this.tickCount % 3 == 0) {
                this.level().addParticle(
                        ParticleTypes.BUBBLE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                        this.getY() + 0.8D + this.random.nextDouble() * 0.6D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                        0.0D,
                        0.02D,
                        0.0D
                );
            }
        }
    }

    private void updateSwimRotation() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.horizontalDistanceSqr() > 1.0E-4D) {
            float targetYaw = (float)(Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
            this.setYRot(Mth.approachDegrees(this.getYRot(), targetYaw, 6.0F));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }

        if (velocity.lengthSqr() > 1.0E-4D) {
            float targetPitch = (float)(-(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI)));
            this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 4.0F));
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.actionTicks = 0;
            this.attackCooldown = 0;
            this.phaseTransitionTicks = 0;
            this.setAttackState(ATTACK_NONE);
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

        if (this.deathTime >= DEATH_ANIM_TICKS) {
            this.remove(RemovalReason.KILLED);
            this.dropExperience(this);
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.phaseTransitionTicks > 0 || this.isRoaring()) {
            this.setAnimationState(ANIM_IDLE);
            return;
        }

        switch (this.getAttackState()) {
            case ATTACK_GRAB -> {
                this.setAnimationState(ANIM_GRAB);
                return;
            }
            case ATTACK_SWIPE, ATTACK_SLAM -> {
                this.setAnimationState(ANIM_SWIPE);
                return;
            }
            default -> {
            }
        }

        Vec3 velocity = this.getDeltaMovement();
        double horizontalSpeedSqr = velocity.horizontalDistanceSqr();
        double verticalSpeedSqr = velocity.y * velocity.y;
        if (horizontalSpeedSqr > 0.03D && horizontalSpeedSqr > verticalSpeedSqr * 2.5D) {
            this.setAnimationState(ANIM_SWIM);
            return;
        }

        this.setAnimationState(this.isPhaseTwo() ? ANIM_IDLE_TWO : ANIM_IDLE);
    }
}
