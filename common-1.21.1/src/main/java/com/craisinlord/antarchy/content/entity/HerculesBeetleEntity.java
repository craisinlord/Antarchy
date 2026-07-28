package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyGameRules;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.network.HerculesBeetleImpactShakeSync;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HerculesBeetleEntity extends TamableAnimal implements GeoEntity, FlyingAnimal {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(HerculesBeetleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SADDLED =
            SynchedEntityData.defineId(HerculesBeetleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING =
            SynchedEntityData.defineId(HerculesBeetleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> KNOCKED_DOWN =
            SynchedEntityData.defineId(HerculesBeetleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MOUNTED_CHARGE =
            SynchedEntityData.defineId(HerculesBeetleEntity.class, EntityDataSerializers.INT);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_FLY = 2;
    private static final int ANIM_CRY = 3;
    private static final int ANIM_ATTACK = 4;
    private static final int ANIM_FLY_ATTACK = 5;
    private static final int ANIM_CHARGE_START = 6;
    private static final int ANIM_CHARGE = 7;
    private static final int ANIM_KNOCKED_DOWN = 8;
    private static final int ANIM_KNOCKED_DOWN_IDLE = 9;
    private static final int ANIM_GET_UP = 10;
    private static final int ANIM_OPEN_WINGS = 11;
    private static final int ANIM_CLOSE_WINGS = 12;
    private static final int ANIM_DEATH = 13;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation CRY_ANIM = RawAnimation.begin().thenPlay("cry");
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation FLY_ATTACK_ANIM = RawAnimation.begin().thenPlay("fly_attack");
    private static final RawAnimation CHARGE_START_ANIM = RawAnimation.begin().thenPlay("charge_start");
    private static final RawAnimation CHARGE_ANIM = RawAnimation.begin().thenLoop("charge");
    private static final RawAnimation KNOCKED_DOWN_ANIM = RawAnimation.begin().thenPlay("knocked_down");
    private static final RawAnimation KNOCKED_DOWN_IDLE_ANIM = RawAnimation.begin().thenLoop("knocked_down_idle");
    private static final RawAnimation GET_UP_ANIM = RawAnimation.begin().thenPlay("get_up");
    private static final RawAnimation OPEN_WINGS_ANIM = RawAnimation.begin().thenPlay("open_wings");
    private static final RawAnimation CLOSE_WINGS_ANIM = RawAnimation.begin().thenPlay("close_wings");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private static final int CRY_TICKS = 30;
    private static final int ATTACK_TICKS = 10;
    private static final int ATTACK_HIT_TICK = 5;
    private static final int FLY_ATTACK_TICKS = 10;
    private static final int FLY_ATTACK_HIT_TICK = 5;
    private static final int OPEN_WINGS_TICKS = 10;
    private static final int CLOSE_WINGS_TICKS = 5;
    private static final int CHARGE_START_TICKS = 15;
    private static final int KNOCKED_DOWN_TICKS = 16;
    private static final int GET_UP_TICKS = 20;
    private static final int MAX_PLAYER_CHARGE_TICKS = 40;
    private static final int MIN_MOUNTED_CHARGE_TICKS = 40;
    private static final int MAX_CHARGE_TICKS = 200;
    private static final int CHARGE_COOLDOWN_TICKS = 200;
    private static final int REGULAR_ATTACK_COOLDOWN_TICKS = 18;
    private static final int CRY_COOLDOWN_TICKS = 120;
    private static final int COMBAT_RESET_TICKS = 60;
    private static final int IMPACT_SHAKE_TICKS = 25;
    private static final double FLIGHT_SPEED = 1.38D;
    private static final double GROUND_SPEED = 0.34D;
    private static final double CHARGE_SPEED = 1.5D;
    private static final double CHARGE_DISTANCE = 10.0D;
    private static final double ATTACK_RANGE = 4.5D;
    private static final double FLY_ATTACK_RANGE = 5.5D;
    private static final double CHARGE_TRIGGER_RANGE = 12.0D;
    private static final int MAX_BROKEN_BLOCKS = 32;
    private static final int MAX_SUFFOCATION_BLOCKS = 12;
    private static final EntityDimensions DEFAULT_DIMENSIONS = EntityDimensions.scalable(3.0F, 4.0F);
    private static final double MOUNTED_FLYING_SPEED_MULTIPLIER = 0.052D;
    private static final double MOUNTED_GROUND_BRAKE = 0.6D;
    private static final double MOUNTED_FLIGHT_LIFT = 0.36D;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this.getUUID(), Component.translatable("entity.antarchy.hercules_beetle"), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    private int actionTicks;
    private int actionHitTick;
    private int chargeTicksRemaining;
    private double chargeDistanceTravelled;
    private double chargeDistanceTarget;
    private int chargeCooldown;
    private int attackCooldown;
    private int cryCooldown;
    private int flightCooldown;
    private int targetlessTicks;
    private int groundedTicks;
    private int airborneTicks;
    private int mountedChargeTicks;
    private boolean attackDamageApplied;
    private boolean riderJumpPressed;
    private boolean mountedChargeActive;
    private boolean combatCryDone;
    private boolean pendingChargeStart;
    private boolean pendingMountedCharge;
    private boolean groundedMountedCharge;
    private int pendingChargeDuration;
    @Nullable
    private UUID attackTargetId;
    private Vec3 chargeDirection = Vec3.ZERO;

    public HerculesBeetleEntity(EntityType<? extends HerculesBeetleEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.LAVA, -1.0F);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.herculesBeetleHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.herculesBeetleAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, GROUND_SPEED)
                .add(Attributes.FLYING_SPEED, FLIGHT_SPEED)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 12.0D);
    }

    public static boolean canSpawn(EntityType<HerculesBeetleEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG || spawnReason == MobSpawnType.SPAWNER || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && level.isEmptyBlock(pos)
                && level.isEmptyBlock(pos.above());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(true);
        navigation.setCanOpenDoors(false);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HerculesCombatGoal());
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.75D) {
            @Override
            public boolean canUse() {
                return !HerculesBeetleEntity.this.isKnockedDown()
                        && !HerculesBeetleEntity.this.isVehicle()
                        && !HerculesBeetleEntity.this.isInBusyAnimation()
                        && super.canUse();
            }
        });
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, entity ->
                entity instanceof Player player
                        && !this.isTame()
                        && !this.isKnockedDown()
                        && !player.isCreative()
                        && !player.isSpectator()));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(SADDLED, false);
        builder.define(FLYING, false);
        builder.define(KNOCKED_DOWN, false);
        builder.define(MOUNTED_CHARGE, 0);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, this::mainAnimController));
        controllers.add(new AnimationController<>(this, "flying_charge_controller", 0, this::flyingChargeAnimController));
    }

    private PlayState mainAnimController(AnimationState<HerculesBeetleEntity> state) {
        if (this.isFlying() && (this.getAnimationState() == ANIM_CHARGE_START || this.getAnimationState() == ANIM_CHARGE)) {
            return state.setAndContinue(FLY_ANIM);
        }
        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
            case ANIM_CRY -> state.setAndContinue(CRY_ANIM);
            case ANIM_ATTACK -> state.setAndContinue(ATTACK_ANIM);
            case ANIM_FLY_ATTACK -> state.setAndContinue(FLY_ATTACK_ANIM);
            case ANIM_CHARGE_START -> state.setAndContinue(CHARGE_START_ANIM);
            case ANIM_CHARGE -> state.setAndContinue(CHARGE_ANIM);
            case ANIM_KNOCKED_DOWN -> state.setAndContinue(KNOCKED_DOWN_ANIM);
            case ANIM_KNOCKED_DOWN_IDLE -> state.setAndContinue(KNOCKED_DOWN_IDLE_ANIM);
            case ANIM_GET_UP -> state.setAndContinue(GET_UP_ANIM);
            case ANIM_OPEN_WINGS -> state.setAndContinue(OPEN_WINGS_ANIM);
            case ANIM_CLOSE_WINGS -> state.setAndContinue(CLOSE_WINGS_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    private PlayState flyingChargeAnimController(AnimationState<HerculesBeetleEntity> state) {
        if (!this.isFlying()) {
            state.resetCurrentAnimation();
            return PlayState.STOP;
        }

        return switch (this.getAnimationState()) {
            case ANIM_CHARGE_START -> state.setAndContinue(CHARGE_START_ANIM);
            case ANIM_CHARGE -> state.setAndContinue(CHARGE_ANIM);
            default -> {
                state.resetCurrentAnimation();
                yield PlayState.STOP;
            }
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        CavarynBurrowingMobBehavior.moveOutOfBlocks(this);
        super.tick();
        CavarynBurrowingMobBehavior.moveOutOfBlocks(this);

        if (this.level().isClientSide) {
            return;
        }

        this.syncConfiguredAttributes();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.chargeCooldown > 0) {
            this.chargeCooldown--;
        }
        if (this.cryCooldown > 0) {
            this.cryCooldown--;
        }

        if (this.getTarget() == null) {
            if (this.targetlessTicks++ >= COMBAT_RESET_TICKS) {
                this.combatCryDone = false;
            }
        } else {
            this.targetlessTicks = 0;
        }

        if (this.onGround()) {
            this.groundedTicks++;
            this.airborneTicks = 0;
        } else {
            this.groundedTicks = 0;
            this.airborneTicks++;
        }

        this.tickMountedCharge();

        if (this.isDeadOrDying()) {
            this.noPhysics = false;
            this.setAnimationState(ANIM_DEATH);
            this.bossEvent.setProgress(0.0F);
            return;
        }

        if (this.flightCooldown > 0) {
            this.flightCooldown--;
        }

        if (this.isFlying() && this.tickCount % 32 == 0) {
            this.playSound(AntarchySoundEvents.MANTIS_FLY_LOOP.get(), 0.5F, 0.85F + this.random.nextFloat() * 0.1F);
        }

        if (this.isKnockedDown()) {
            this.tickKnockedDown();
        } else if (this.chargeTicksRemaining > 0 || this.getAnimationState() == ANIM_CHARGE) {
            this.tickCharge();
        } else if (this.isInBusyAnimation()) {
            this.tickBusyAnimation();
        } else {
            this.tickFlightState();
            this.updateAmbientAnimation();
        }

        if (this.isVehicle()) {
            this.tickMountedMotion();
        }

        boolean airborneCharge = this.chargeTicksRemaining > 0 && !this.groundedMountedCharge;
        this.setNoGravity(this.isFlying() || airborneCharge);
        if (this.isFlying() || airborneCharge) {
            this.fallDistance = 0.0F;
        }

        if (this.shouldShowBossBar()) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        } else {
            this.bossEvent.removeAllPlayers();
        }
    }

    private void tickKnockedDown() {
        this.getNavigation().stop();
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, Math.min(motion.y, 0.0D), 0.0D);
        if (this.getAnimationState() == ANIM_KNOCKED_DOWN && this.actionTicks > 0) {
            this.actionTicks--;
            if (this.actionTicks <= 0) {
                this.setAnimationState(ANIM_KNOCKED_DOWN_IDLE);
            }
        } else if (this.getAnimationState() != ANIM_KNOCKED_DOWN_IDLE) {
            this.setAnimationState(ANIM_KNOCKED_DOWN_IDLE);
        }
    }

    private void tickBusyAnimation() {
        this.getNavigation().stop();
        if (this.getTarget() != null) {
            this.getLookControl().setLookAt(this.getTarget(), 25.0F, 20.0F);
        }

        int state = this.getAnimationState();
        if (state == ANIM_ATTACK || state == ANIM_FLY_ATTACK) {
            int elapsed = (state == ANIM_ATTACK ? ATTACK_TICKS : FLY_ATTACK_TICKS) - this.actionTicks;
            if (!this.attackDamageApplied && elapsed >= this.actionHitTick) {
                this.attackDamageApplied = true;
                this.performRegularAttack(state == ANIM_FLY_ATTACK);
            }
        }

        if (this.actionTicks > 0) {
            this.actionTicks--;
        }
        if (this.actionTicks > 0) {
            return;
        }

        switch (state) {
            case ANIM_OPEN_WINGS -> {
                this.setFlying(true);
                if (this.getControllingPassenger() instanceof Player) {
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(motion.x, Math.max(motion.y, MOUNTED_FLIGHT_LIFT), motion.z);
                    this.hasImpulse = true;
                }
                if (this.pendingChargeStart) {
                    this.pendingChargeStart = false;
                    this.beginChargeStart(this.chargeDirection, this.pendingChargeDuration, this.pendingMountedCharge);
                    return;
                }
            }
            case ANIM_CLOSE_WINGS -> this.setFlying(false);
            case ANIM_CHARGE_START -> this.beginChargeLoop();
            case ANIM_GET_UP -> this.setAnimationState(this.isFlying() ? ANIM_FLY : ANIM_IDLE);
            case ANIM_CRY, ANIM_ATTACK, ANIM_FLY_ATTACK -> {
            }
            default -> {
            }
        }

        this.updateAmbientAnimation();
    }

    private void tickCharge() {
        if (this.getAnimationState() != ANIM_CHARGE) {
            this.setAnimationState(ANIM_CHARGE);
        }
        if (!this.groundedMountedCharge) {
            this.setFlying(true);
            this.setNoGravity(true);
        }
        this.noPhysics = true;
        this.fallDistance = 0.0F;

        double remainingDistance = Math.max(0.0D, this.chargeDistanceTarget - this.chargeDistanceTravelled);
        if (remainingDistance <= 0.0D) {
            this.finishChargeImpact();
            return;
        }

        Vec3 move = this.chargeDirection.scale(Math.min(CHARGE_SPEED, remainingDistance));
        Vec3 nextPosition = this.position().add(move);
        if (this.blocksChargePath(nextPosition)) {
            this.finishChargeImpact();
            return;
        }

        this.breakBlocksForChargePath(nextPosition);
        this.setDeltaMovement(move);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.lookInDirection(this.chargeDirection);

        this.chargeDistanceTravelled += move.length();
        this.chargeTicksRemaining--;
        if (this.chargeDistanceTravelled >= this.chargeDistanceTarget || this.chargeTicksRemaining <= 0) {
            this.finishChargeImpact();
        }
    }

    private void tickFlightState() {
        if (!this.isVehicle() && this.isFlying() && this.onGround() && this.groundedTicks > 10 && this.getTarget() == null && !this.riderJumpPressed) {
            this.requestFlightStop();
        }
    }

    private void tickMountedMotion() {
        if (!(this.getControllingPassenger() instanceof Player rider) || this.isKnockedDown()) {
            return;
        }
        if (this.chargeTicksRemaining > 0 || this.getAnimationState() == ANIM_CHARGE_START || this.getAnimationState() == ANIM_OPEN_WINGS) {
            return;
        }

        this.setYRot(rider.getYHeadRot());
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();
        if (this.isFlying()) {
            this.setNoGravity(true);
            this.noPhysics = false;
            this.fallDistance = 0.0F;
            if (this.onGround() && this.getDeltaMovement().y <= 0.0D) {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(motion.x, MOUNTED_FLIGHT_LIFT, motion.z);
                this.hasImpulse = true;
            }
        } else {
            this.setNoGravity(false);
            this.noPhysics = false;
        }
    }

    private void updateAmbientAnimation() {
        if (this.isKnockedDown()) {
            this.setAnimationState(ANIM_KNOCKED_DOWN_IDLE);
            return;
        }
        if (this.isFlying()) {
            this.setAnimationState(ANIM_FLY);
            return;
        }
        if (!this.onGround()) {
            this.setAnimationState(ANIM_IDLE);
            return;
        }
        if (this.getControllingPassenger() instanceof Player rider) {
            boolean riderMoving = Math.abs(rider.zza) > 0.05F || Math.abs(rider.xxa) > 0.05F;
            if (riderMoving) {
                this.setAnimationState(ANIM_WALK);
                return;
            }
        }
        Vec3 motion = this.getDeltaMovement();
        boolean moving = motion.horizontalDistanceSqr() > 0.0025D;
        this.setAnimationState(moving ? ANIM_WALK : ANIM_IDLE);
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        if (!this.isFlying()) {
            return new Vec3(player.xxa * 0.22D, 0.0D, player.zza);
        }

        double vertical = 0.0D;
        if (this.riderJumpPressed) {
            vertical += 0.18D;
        }
        if (player.zza > 0.0F && player.getXRot() < -5.0F) {
            vertical += 0.16D;
        } else if (player.zza > 0.0F && player.getXRot() > 20.0F) {
            vertical -= 0.16D;
        }

        return new Vec3(player.xxa * 0.22D, vertical, player.zza);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        if (this.isFlying()) {
            return (float) (this.getAttributeValue(Attributes.FLYING_SPEED) * MOUNTED_FLYING_SPEED_MULTIPLIER);
        }
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    private void syncConfiguredAttributes() {
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(AntarchySettings.herculesBeetleHealth());
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(AntarchySettings.herculesBeetleAttackDamage());
        if (this.getHealth() > this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= 0.5F;
        }

        if (!this.level().isClientSide && !this.isTame() && !this.isKnockedDown() && !this.isDeadOrDying()) {
            float knockThreshold = Math.max(1.0F, this.getMaxHealth() * 0.05F);
            if (this.getHealth() - amount <= knockThreshold) {
                float allowedDamage = Math.max(0.0F, this.getHealth() - knockThreshold);
                boolean hurt = allowedDamage <= 0.0F || super.hurt(source, allowedDamage);
                if (hurt && this.isAlive()) {
                    this.enterKnockedDown();
                }
                return hurt;
            }
        }

        return super.hurt(source, amount);
    }

    private void enterKnockedDown() {
        if (this.isKnockedDown()) {
            return;
        }
        this.setKnockedDown(true);
        this.setFlying(false);
        this.setNoGravity(false);
        this.noPhysics = false;
        this.pendingChargeStart = false;
        this.groundedMountedCharge = false;
        this.chargeTicksRemaining = 0;
        this.attackCooldown = 0;
        this.getNavigation().stop();
        this.setTarget(null);
        this.setDeltaMovement(Vec3.ZERO);
        this.actionTicks = KNOCKED_DOWN_TICKS;
        this.setAnimationState(ANIM_KNOCKED_DOWN);
        this.playSound(AntarchySoundEvents.HERCULES_BEETLE_KNOCKED_DOWN.get(), 1.2F, 0.9F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (this.isKnockedDown() && this.isFood(stack)) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.setTame(true, true);
                    this.setOwnerUUID(player.getUUID());
                    if (player instanceof ServerPlayer serverPlayer) {
                        CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
                    }
                    this.setKnockedDown(false);
                    this.heal(this.getMaxHealth());
                    this.setTarget(null);
                    this.actionTicks = GET_UP_TICKS;
                    this.setAnimationState(ANIM_GET_UP);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                    this.bossEvent.removeAllPlayers();
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (this.isTame()) {
            if (!this.hasSaddle() && stack.is(Items.SADDLE) && this.isOwnedBy(player)) {
                if (!this.level().isClientSide) {
                    this.setSaddled(true);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                    this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.hasSaddle() && stack.isEmpty() && player.isShiftKeyDown() && this.isOwnedBy(player) && !player.isPassengerOfSameVehicle(this)) {
                if (!this.level().isClientSide) {
                    this.setSaddled(false);
                    this.spawnAtLocation(new ItemStack(Items.SADDLE));
                    this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 0.8F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.hasSaddle() && stack.isEmpty() && !player.isPassengerOfSameVehicle(this) && !this.isKnockedDown()) {
                if (!this.level().isClientSide) {
                    player.startRiding(this);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(AntarchyTags.Items.HERCULES_BEETLE_FOOD);
    }

    public boolean hasSaddle() {
        return this.entityData.get(SADDLED);
    }

    public void setSaddled(boolean saddled) {
        this.entityData.set(SADDLED, saddled);
    }

    private void setFlying(boolean flying) {
        if (this.entityData.get(FLYING) != flying) {
            this.entityData.set(FLYING, flying);
            this.refreshDimensions();
        }
    }

    private boolean isMountedFlightPoseActive() {
        return this.isFlying() && (!this.onGround() || this.getDeltaMovement().y > 0.08D);
    }

    private void startMountedFlight() {
        this.pendingChargeStart = false;
        this.setFlying(true);
        this.setNoGravity(true);
        this.noPhysics = false;
        this.fallDistance = 0.0F;
        this.groundedTicks = 0;
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, Math.max(motion.y, MOUNTED_FLIGHT_LIFT), motion.z);
        this.hasImpulse = true;
        this.setAnimationState(ANIM_FLY);
    }

    private void stopMountedFlight() {
        this.setFlying(false);
        this.setNoGravity(false);
        this.noPhysics = false;
        this.fallDistance = 0.0F;
        this.flightCooldown = 8;
        this.updateAmbientAnimation();
    }

    public boolean isKnockedDown() {
        return this.entityData.get(KNOCKED_DOWN);
    }

    private void setKnockedDown(boolean knockedDown) {
        this.entityData.set(KNOCKED_DOWN, knockedDown);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private boolean isInBusyAnimation() {
        int state = this.getAnimationState();
        return state == ANIM_CRY
                || state == ANIM_ATTACK
                || state == ANIM_FLY_ATTACK
                || state == ANIM_CHARGE_START
                || state == ANIM_CHARGE
                || state == ANIM_OPEN_WINGS
                || state == ANIM_CLOSE_WINGS
                || state == ANIM_GET_UP
                || state == ANIM_KNOCKED_DOWN;
    }

    private void requestFlightStart() {
        if (this.isFlying()
                || this.isKnockedDown()
                || this.getAnimationState() == ANIM_OPEN_WINGS
                || this.getAnimationState() == ANIM_CHARGE
                || this.getAnimationState() == ANIM_CHARGE_START
                || !this.onGround()
                || this.flightCooldown > 0) {
            return;
        }
        this.setAnimationState(ANIM_OPEN_WINGS);
        this.actionTicks = OPEN_WINGS_TICKS;
    }

    private void requestFlightStop() {
        if (!this.isFlying() || this.getAnimationState() == ANIM_CLOSE_WINGS || this.isInBusyAnimation()) {
            return;
        }
        this.setAnimationState(ANIM_CLOSE_WINGS);
        this.actionTicks = CLOSE_WINGS_TICKS;
        this.flightCooldown = 8;
    }

    private void startCry() {
        this.setAnimationState(ANIM_CRY);
        this.actionTicks = CRY_TICKS;
        this.cryCooldown = CRY_COOLDOWN_TICKS;
        this.combatCryDone = true;
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
        this.playSound(AntarchySoundEvents.HERCULES_BEETLE_CRY.get(), 1.5F, 0.7F);
    }

    private void startRegularAttack(boolean flyingAttack, @Nullable LivingEntity target) {
        this.setAnimationState(flyingAttack ? ANIM_FLY_ATTACK : ANIM_ATTACK);
        this.actionTicks = flyingAttack ? FLY_ATTACK_TICKS : ATTACK_TICKS;
        this.actionHitTick = flyingAttack ? FLY_ATTACK_HIT_TICK : ATTACK_HIT_TICK;
        this.attackDamageApplied = false;
        this.attackTargetId = target == null ? null : target.getUUID();
        this.attackCooldown = REGULAR_ATTACK_COOLDOWN_TICKS;
    }

    private void performRegularAttack(boolean flyingAttack) {
        LivingEntity target = this.attackTargetId == null ? null : this.findNearbyLivingEntity(this.attackTargetId);
        boolean hit = false;
        if (target != null && target.isAlive() && this.distanceToSqr(target) <= 49.0D) {
            hit = this.hurtAndLaunchTarget(target, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE), flyingAttack ? 1.0D : 0.7D);
        }
        if (!hit) {
            for (LivingEntity candidate : this.getFrontTargets(flyingAttack ? 6.0D : 5.0D, 2.5D)) {
                if (this.hurtAndLaunchTarget(candidate, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE), flyingAttack ? 1.0D : 0.7D)) {
                    break;
                }
            }
        }
        this.playSound(AntarchySoundEvents.HERCULES_BEETLE_ATTACK.get(), 1.2F, flyingAttack ? 1.1F : 0.9F);
    }

    private boolean hurtAndLaunchTarget(LivingEntity target, float damage, double horizontalKnockback) {
        if (!this.canHarm(target)) {
            return false;
        }
        boolean hurt = this.level() instanceof ServerLevel serverLevel
                ? target.hurt(AntarchyDamageSources.herculesBeetleObliteration(serverLevel, this), damage)
                : target.hurt(this.damageSources().mobAttack(this), damage);
        if (hurt) {
            Vec3 push = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (push.lengthSqr() < 1.0E-4D) {
                push = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            }
            push = push.normalize().scale(horizontalKnockback);
            target.setDeltaMovement(target.getDeltaMovement().add(push.x, 1.25D, push.z));
            target.hurtMarked = true;
        }
        return hurt;
    }

    private void startCharge(Vec3 direction, int durationTicks, boolean mountedCharge) {
        Vec3 horizontal = direction.multiply(1.0D, 0.0D, 1.0D);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            horizontal = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        this.chargeDirection = horizontal.normalize();
        this.pendingChargeDuration = Mth.clamp(durationTicks, MIN_MOUNTED_CHARGE_TICKS, MAX_CHARGE_TICKS);
        this.pendingMountedCharge = mountedCharge;

        if (!this.isFlying() && !mountedCharge) {
            this.pendingChargeStart = true;
            this.requestFlightStart();
            return;
        }
        this.beginChargeStart(this.chargeDirection, this.pendingChargeDuration, mountedCharge);
    }

    private void beginChargeStart(Vec3 direction, int durationTicks, boolean mountedCharge) {
        this.chargeDirection = direction.normalize();
        this.pendingChargeDuration = durationTicks;
        this.pendingMountedCharge = mountedCharge;
        this.setAnimationState(ANIM_CHARGE_START);
        this.actionTicks = CHARGE_START_TICKS;
        this.playSound(AntarchySoundEvents.HERCULES_BEETLE_CHARGE_START.get(), 1.3F, 0.7F);
    }

    private void beginChargeLoop() {
        this.setAnimationState(ANIM_CHARGE);
        this.chargeTicksRemaining = this.pendingChargeDuration;
        this.chargeDistanceTravelled = 0.0D;
        this.chargeDistanceTarget = CHARGE_DISTANCE;
        this.chargeCooldown = CHARGE_COOLDOWN_TICKS;
        this.pendingChargeDuration = 0;
        this.groundedMountedCharge = this.pendingMountedCharge && !this.isFlying();
        this.pendingMountedCharge = false;
        if (!this.groundedMountedCharge) {
            this.setFlying(true);
        }
    }

    private void finishChargeImpact() {
        this.groundedMountedCharge = false;
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.chargeTicksRemaining = 0;
            this.noPhysics = false;
            this.updateAmbientAnimation();
            return;
        }

        this.chargeTicksRemaining = 0;
        this.noPhysics = false;
        this.setDeltaMovement(Vec3.ZERO);

        AABB impactBox = this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D);
        float damage = (float) AntarchySettings.herculesBeetleChargeDamage();
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, impactBox, this::canHarm)) {
            if (victim == this) {
                continue;
            }
            if (victim.hurt(AntarchyDamageSources.herculesBeetleObliteration(serverLevel, this), damage)) {
                Vec3 push = victim.position().subtract(this.position());
                if (push.lengthSqr() < 1.0E-4D) {
                    push = this.chargeDirection;
                }
                push = push.normalize();
                victim.setDeltaMovement(victim.getDeltaMovement().add(push.x * 1.25D, 0.9D, push.z * 1.25D));
                victim.hurtMarked = true;
            }
        }

        this.breakBlocksForChargeImpact(serverLevel);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.6F, 0.75F);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.8D, this.getZ(), 4, 1.5D, 0.5D, 1.5D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.8D, this.getZ(), 20, 2.0D, 0.6D, 2.0D, 0.08D);
        this.broadcastImpactShake(serverLevel);

        if (this.onGround()) {
            this.requestFlightStop();
        } else {
            this.updateAmbientAnimation();
        }
    }

    private void breakBlocksForChargeImpact(ServerLevel level) {
        if (!this.isHerculesBeetleGriefingEnabled(level)) {
            return;
        }

        AABB impactBox = this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D);
        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(impactBox.minX, impactBox.minY, impactBox.minZ),
                BlockPos.containing(impactBox.maxX, impactBox.maxY, impactBox.maxZ)
        )) {
            if (broken >= MAX_BROKEN_BLOCKS) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (state.isAir()
                    || state.is(AntarchyTags.Blocks.HERCULES_BEETLE_CHARGE_IMMUNE_BLOCKS)
                    || blockEntity != null
                    || state.getDestroySpeed(level, pos) < 0.0F) {
                continue;
            }
            if (level.destroyBlock(pos, true, this)) {
                broken++;
            }
        }
    }

    private void breakBlocksForChargePath(Vec3 nextPosition) {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isHerculesBeetleGriefingEnabled(serverLevel)) {
            return;
        }

        AABB pathBox = this.getBoundingBox().expandTowards(nextPosition.subtract(this.position())).inflate(1.25D, 1.25D, 1.25D);
        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(pathBox.minX, pathBox.minY, pathBox.minZ),
                BlockPos.containing(pathBox.maxX, pathBox.maxY, pathBox.maxZ)
        )) {
            if (broken >= MAX_BROKEN_BLOCKS) {
                break;
            }
            BlockState state = serverLevel.getBlockState(pos);
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (state.isAir()
                    || state.is(AntarchyTags.Blocks.HERCULES_BEETLE_CHARGE_IMMUNE_BLOCKS)
                    || blockEntity != null
                    || state.getDestroySpeed(serverLevel, pos) < 0.0F) {
                continue;
            }
            if (serverLevel.destroyBlock(pos, true, this)) {
                broken++;
            }
        }
    }

    private void breakBlocksForSuffocation() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isHerculesBeetleGriefingEnabled(serverLevel)) {
            return;
        }

        AABB box = this.getBoundingBox().inflate(0.15D, 0.15D, 0.15D);
        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ)
        )) {
            if (broken >= MAX_SUFFOCATION_BLOCKS) {
                break;
            }
            BlockState state = serverLevel.getBlockState(pos);
            BlockEntity blockEntity = serverLevel.getBlockEntity(pos);
            if (state.isAir()
                    || state.is(AntarchyTags.Blocks.HERCULES_BEETLE_CHARGE_IMMUNE_BLOCKS)
                    || blockEntity != null
                    || state.getDestroySpeed(serverLevel, pos) < 0.0F) {
                continue;
            }
            if (serverLevel.destroyBlock(pos, true, this)) {
                broken++;
            }
        }
    }

    private boolean blocksChargePath(Vec3 nextPosition) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        AABB pathBox = this.getBoundingBox().expandTowards(nextPosition.subtract(this.position())).inflate(1.25D, 1.25D, 1.25D);
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(pathBox.minX, pathBox.minY, pathBox.minZ),
                BlockPos.containing(pathBox.maxX, pathBox.maxY, pathBox.maxZ)
        )) {
            BlockState state = serverLevel.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (state.is(AntarchyTags.Blocks.HERCULES_BEETLE_CHARGE_IMMUNE_BLOCKS) || serverLevel.getBlockEntity(pos) != null || state.getDestroySpeed(serverLevel, pos) < 0.0F) {
                return true;
            }
        }
        return false;
    }

    private boolean isHerculesBeetleGriefingEnabled(ServerLevel level) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && level.getGameRules().getBoolean(AntarchyGameRules.RULE_DO_HERCULES_BEETLE_GREIFING);
    }

    private void broadcastImpactShake(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(this) <= 48.0D * 48.0D) {
                HerculesBeetleImpactShakeSync.send(player, IMPACT_SHAKE_TICKS);
            }
        }
    }

    public void setRiderJumpPressed(boolean riderJumpPressed) {
        this.riderJumpPressed = riderJumpPressed;
    }

    public int getMountedCharge() {
        return this.entityData.get(MOUNTED_CHARGE);
    }

    public void handleMountedRegularAttack(ServerPlayer player) {
        if (!this.canPlayerControl(player) || this.attackCooldown > 0 || this.isKnockedDown() || this.isInBusyAnimation() || this.chargeTicksRemaining > 0) {
            return;
        }
        LivingEntity target = this.findFrontTarget(6.0D, 3.0D);
        this.startRegularAttack(this.isFlying(), target);
    }

    public void toggleMountedFlight(ServerPlayer player) {
        if (!this.canPlayerControl(player) || this.isKnockedDown() || this.chargeTicksRemaining > 0 || this.isInBusyAnimation()) {
            return;
        }
        if (this.isFlying()) {
            this.stopMountedFlight();
            return;
        }
        this.startMountedFlight();
    }

    public void startMountedCharge(ServerPlayer player) {
        if (!this.canPlayerControl(player) || this.chargeCooldown > 0 || this.isKnockedDown() || this.isInBusyAnimation() || this.chargeTicksRemaining > 0) {
            this.resetMountedChargeState();
            return;
        }
        this.mountedChargeActive = true;
    }

    public void releaseMountedCharge(ServerPlayer player) {
        if (!this.canPlayerControl(player)) {
            this.resetMountedChargeState();
            return;
        }
        if (!this.mountedChargeActive) {
            return;
        }

        int clamped = Mth.clamp(this.mountedChargeTicks, 0, MAX_PLAYER_CHARGE_TICKS);
        this.resetMountedChargeState();
        if (this.chargeCooldown > 0 || this.isKnockedDown() || this.isInBusyAnimation() || this.chargeTicksRemaining > 0 || clamped <= 0) {
            return;
        }

        int duration = MIN_MOUNTED_CHARGE_TICKS + Mth.floor((MAX_CHARGE_TICKS - MIN_MOUNTED_CHARGE_TICKS) * (clamped / (float) MAX_PLAYER_CHARGE_TICKS));
        this.startCharge(player.getLookAngle(), duration, true);
    }

    private boolean canPlayerControl(ServerPlayer player) {
        return this.isTame()
                && this.hasSaddle()
                && player.getVehicle() == this
                && this.getControllingPassenger() == player;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        if (!this.isTame() || !this.hasSaddle()) {
            return null;
        }
        Entity passenger = this.getFirstPassenger();
        return passenger instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction moveFunction) {
        if (this.hasPassenger(passenger)) {
            Vec3 forward = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
            boolean mountedFlightPose = this.isMountedFlightPoseActive();
            double yOffset = mountedFlightPose ? 5.3D : 3.0D;
            double forwardOffset = mountedFlightPose ? 2.2D : 1.25D;
            if (!this.isFlying()) {
                yOffset -= 0.25D;
                forwardOffset += 0.25D;
            }
            moveFunction.accept(
                    passenger,
                    this.getX() + forward.x * forwardOffset,
                    this.getY() + yOffset,
                    this.getZ() + forward.z * forwardOffset
            );
        }
    }

    @Override
    public boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!(this.getControllingPassenger() instanceof Player rider) || !this.isVehicle() || this.isKnockedDown()) {
            super.travel(travelVector);
            return;
        }

        this.setYRot(rider.getYHeadRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        Vec3 riddenInput = this.getRiddenInput(rider, travelVector);
        this.setSpeed(this.getRiddenSpeed(rider));

        if (!this.isFlying()) {
            super.travel(new Vec3(riddenInput.x, travelVector.y, riddenInput.z));

            if (Math.abs(rider.zza) < 0.05F && Math.abs(rider.xxa) < 0.05F && this.onGround()) {
                Vec3 dampedMotion = this.getDeltaMovement();
                this.setDeltaMovement(dampedMotion.x * MOUNTED_GROUND_BRAKE, dampedMotion.y, dampedMotion.z * MOUNTED_GROUND_BRAKE);
            }
        } else {
            Vec3 motion = new Vec3(0.0D, this.getDeltaMovement().y, 0.0D);
            this.moveRelative(this.getSpeed(), new Vec3(riddenInput.x, 0.0D, riddenInput.z));
            motion = motion.add(this.getDeltaMovement().x, riddenInput.y, this.getDeltaMovement().z);
            this.setDeltaMovement(motion);
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (this.attackCooldown > 0 || this.isKnockedDown()) {
            return false;
        }
        if (target instanceof LivingEntity livingTarget) {
            this.startRegularAttack(this.isFlying(), livingTarget);
            return true;
        }
        return super.doHurtTarget(target);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean isFlying() {
        return this.entityData.get(FLYING);
    }

    @Override
    public float maxUpStep() {
        return this.isFlying() ? 0.6F : 1.5F;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return DEFAULT_DIMENSIONS;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target instanceof Player player) {
            return !this.isTame() && !player.isCreative() && !player.isSpectator() && super.canAttack(target);
        }
        return super.canAttack(target);
    }

    private boolean canHarm(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target == this || target == this.getControllingPassenger()) {
            return false;
        }
        if (target instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && !this.isTame();
        }
        return target.canBeSeenAsEnemy();
    }

    private void lookInDirection(Vec3 direction) {
        if (direction.lengthSqr() <= 1.0E-4D) {
            return;
        }
        float yaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        this.setYHeadRot(yaw);
    }

    @Nullable
    private LivingEntity findNearbyLivingEntity(UUID uuid) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Entity entity = serverLevel.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    private List<LivingEntity> getFrontTargets(double range, double inflation) {
        AABB box = this.getBoundingBox().expandTowards(this.getLookAngle().scale(range)).inflate(inflation);
        Vec3 forward = this.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        return this.level().getEntitiesOfClass(LivingEntity.class, box, this::canHarm).stream()
                .filter(entity -> {
                    Vec3 toTarget = entity.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
                    if (forward.lengthSqr() < 1.0E-4D || toTarget.lengthSqr() < 1.0E-4D) {
                        return true;
                    }
                    return forward.normalize().dot(toTarget.normalize()) >= 0.15D;
                })
                .toList();
    }

    @Nullable
    private LivingEntity findFrontTarget(double range, double inflation) {
        List<LivingEntity> targets = this.getFrontTargets(range, inflation);
        return targets.isEmpty() ? null : targets.getFirst();
    }

    private boolean shouldShowBossBar() {
        return !this.isTame();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (this.shouldShowBossBar()) {
            this.bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Saddled", this.hasSaddle());
        tag.putBoolean("KnockedDown", this.isKnockedDown());
        tag.putBoolean("Flying", this.isFlying());
        tag.putInt("AnimationState", this.getAnimationState());
        tag.putInt("ChargeCooldown", this.chargeCooldown);
        tag.putInt("AttackCooldown", this.attackCooldown);
        tag.putBoolean("CombatCryDone", this.combatCryDone);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setSaddled(tag.getBoolean("Saddled"));
        this.setKnockedDown(tag.getBoolean("KnockedDown"));
        this.setFlying(tag.getBoolean("Flying"));
        this.setAnimationState(tag.contains("AnimationState") ? tag.getInt("AnimationState") : ANIM_IDLE);
        this.chargeCooldown = tag.getInt("ChargeCooldown");
        this.attackCooldown = tag.getInt("AttackCooldown");
        this.combatCryDone = tag.getBoolean("CombatCryDone");
        if (this.isKnockedDown() && this.getAnimationState() != ANIM_KNOCKED_DOWN_IDLE) {
            this.setAnimationState(ANIM_KNOCKED_DOWN_IDLE);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isKnockedDown() ? null : AntarchySoundEvents.HERCULES_BEETLE_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.HERCULES_BEETLE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.HERCULES_BEETLE_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.3F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.28F, 0.92F);
    }

    private void tickMountedCharge() {
        if (this.level().isClientSide) {
            return;
        }
        if (!(this.getControllingPassenger() instanceof ServerPlayer player) || !this.canPlayerControl(player)) {
            this.resetMountedChargeState();
            return;
        }
        if (!this.mountedChargeActive) {
            if (this.getMountedCharge() != 0) {
                this.entityData.set(MOUNTED_CHARGE, 0);
            }
            return;
        }
        if (this.chargeCooldown > 0 || this.isKnockedDown() || this.isInBusyAnimation() || this.chargeTicksRemaining > 0) {
            this.resetMountedChargeState();
            return;
        }

        this.mountedChargeTicks = Math.min(this.mountedChargeTicks + 1, MAX_PLAYER_CHARGE_TICKS);
        this.entityData.set(MOUNTED_CHARGE, Mth.floor((this.mountedChargeTicks / (float) MAX_PLAYER_CHARGE_TICKS) * 100.0F));
    }

    private void resetMountedChargeState() {
        this.mountedChargeActive = false;
        this.mountedChargeTicks = 0;
        if (this.getMountedCharge() != 0) {
            this.entityData.set(MOUNTED_CHARGE, 0);
        }
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    private final class HerculesCombatGoal extends Goal {
        HerculesCombatGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !HerculesBeetleEntity.this.isTame()
                    && !HerculesBeetleEntity.this.isKnockedDown()
                    && HerculesBeetleEntity.this.getTarget() != null
                    && HerculesBeetleEntity.this.canAttack(HerculesBeetleEntity.this.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = HerculesBeetleEntity.this.getTarget();
            return target != null
                    && target.isAlive()
                    && !HerculesBeetleEntity.this.isTame()
                    && !HerculesBeetleEntity.this.isKnockedDown()
                    && HerculesBeetleEntity.this.canAttack(target);
        }

        @Override
        public void tick() {
            LivingEntity target = HerculesBeetleEntity.this.getTarget();
            if (target == null) {
                return;
            }
            if (!HerculesBeetleEntity.this.canAttack(target)) {
                HerculesBeetleEntity.this.setTarget(null);
                HerculesBeetleEntity.this.getNavigation().stop();
                return;
            }

            HerculesBeetleEntity.this.getLookControl().setLookAt(target, 30.0F, 20.0F);

            if (!HerculesBeetleEntity.this.combatCryDone && HerculesBeetleEntity.this.cryCooldown <= 0 && !HerculesBeetleEntity.this.isInBusyAnimation()) {
                HerculesBeetleEntity.this.startCry();
                return;
            }

            if (HerculesBeetleEntity.this.isInBusyAnimation() || HerculesBeetleEntity.this.chargeTicksRemaining > 0) {
                return;
            }

            double distanceSqr = HerculesBeetleEntity.this.distanceToSqr(target);
            double verticalDelta = Math.abs(target.getY() - HerculesBeetleEntity.this.getY());

            if (!HerculesBeetleEntity.this.isFlying() && (verticalDelta > 5.0D || HerculesBeetleEntity.this.fallDistance > 4.0F)) {
                HerculesBeetleEntity.this.requestFlightStart();
            } else if (HerculesBeetleEntity.this.isFlying() && HerculesBeetleEntity.this.onGround() && distanceSqr < 25.0D && verticalDelta < 1.5D) {
                HerculesBeetleEntity.this.requestFlightStop();
            }

            if (HerculesBeetleEntity.this.chargeCooldown <= 0
                    && distanceSqr >= CHARGE_TRIGGER_RANGE * CHARGE_TRIGGER_RANGE
                    && distanceSqr <= 36.0D * 36.0D
                    && HerculesBeetleEntity.this.random.nextFloat() < 0.015F) {
                HerculesBeetleEntity.this.startCharge(target.position().subtract(HerculesBeetleEntity.this.position()), MAX_CHARGE_TICKS, false);
                return;
            }

            double attackRange = HerculesBeetleEntity.this.isFlying() ? FLY_ATTACK_RANGE : ATTACK_RANGE;
            if (HerculesBeetleEntity.this.attackCooldown <= 0 && distanceSqr <= attackRange * attackRange) {
                HerculesBeetleEntity.this.startRegularAttack(HerculesBeetleEntity.this.isFlying(), target);
                return;
            }

            double speed = HerculesBeetleEntity.this.isFlying() ? 1.2D : 1.0D;
            HerculesBeetleEntity.this.getNavigation().moveTo(target, speed);
        }
    }
}
