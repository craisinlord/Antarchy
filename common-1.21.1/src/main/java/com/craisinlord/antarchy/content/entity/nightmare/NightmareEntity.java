package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.config.AntarchySettings;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class NightmareEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTIVE_SPECIAL_ANIMATION = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO_TEXTURE = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TRANSITIONING = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ATTACK_ANIMATION_TICKS_KEY = "AttackAnimationTicks";
    private static final String ATTACK_HIT_APPLIED_KEY = "AttackHitApplied";
    private static final String ROAR_TICKS_KEY = "RoarTicks";
    private static final String ROAR_COOLDOWN_KEY = "RoarCooldown";
    private static final String INTRO_ROAR_USED_KEY = "IntroRoarUsed";
    private static final String TARGETLESS_TICKS_KEY = "TargetlessTicks";
    private static final String ANIMATION_STATE_KEY = "AnimationState";
    private static final String ACTIVE_SPECIAL_ANIMATION_KEY = "ActiveSpecialAnimation";
    private static final String PHASE_TWO_KEY = "PhaseTwo";
    private static final String PHASE_TWO_TEXTURE_KEY = "PhaseTwoTexture";
    private static final String PHASE_TRANSITIONING_KEY = "PhaseTransitioning";
    private static final String PHASE_TRANSITION_TICKS_KEY = "PhaseTransitionTicks";
    private static final String PENDING_PHASE_TRANSITION_KEY = "PendingPhaseTransition";
    private static final String PORTAL_COOLDOWN_KEY = "PortalCooldown";
    private static final String PORTAL_ATTACK_TICKS_KEY = "PortalAttackTicks";
    private static final String PORTAL_ATTACK_HIT_KEY = "PortalAttackHitApplied";
    private static final String PORTAL_TRAVEL_TICKS_KEY = "PortalTravelTicks";
    private static final String PORTAL_ENTRY_ID_KEY = "PortalEntryId";
    private static final String PORTAL_EXIT_ID_KEY = "PortalExitId";
    private static final String PORTAL_EXIT_X_KEY = "PortalExitX";
    private static final String PORTAL_EXIT_Y_KEY = "PortalExitY";
    private static final String PORTAL_EXIT_Z_KEY = "PortalExitZ";

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_FLY = 2;
    private static final int SPECIAL_NONE = 0;
    private static final int SPECIAL_ATTACK = 1;
    private static final int SPECIAL_ATTACK_P2 = 2;
    private static final int SPECIAL_FLY_ATTACK = 3;
    private static final int SPECIAL_FLY_ATTACK_P2 = 4;
    private static final int SPECIAL_ROAR = 5;
    private static final int SPECIAL_ROAR_P2 = 6;
    private static final int SPECIAL_PHASE_TWO = 7;
    private static final int SPECIAL_PORTAL_ENTER = 8;
    private static final int SPECIAL_PORTAL_EXIT = 9;
    private static final int SPECIAL_DEATH = 10;
    private static final int ATTACK_TOTAL_TICKS = 20;
    private static final int ATTACK_DAMAGE_TICK = 10;
    private static final int INTRO_ROAR_TICKS = 30;
    private static final int COMBAT_ROAR_TICKS = 30;
    private static final int DEATH_TICKS = 30;
    private static final int TARGET_RESET_TICKS = 60;
    private static final int MIN_AIRBORNE_TICKS_FOR_FLY_ANIM = 4;
    private static final int DREAD_TICKS = 160;
    private static final int WEAKNESS_TICKS = 100;
    private static final int BLOCK_BREAK_TICKS = 10;
    private static final int TAKEOFF_COMMIT_TICKS = 8;
    private static final int ROAR_FLIGHT_RECOVERY_TICKS = 16;
    private static final int FLIGHT_CEILING_CLEARANCE_BLOCKS = 4;
    private static final int PHASE_TWO_TRANSITION_TICKS = 80;
    private static final int PHASE_TWO_TEXTURE_SWAP_TICKS = 53;
    private static final int PORTAL_TOTAL_TICKS = 74;
    private static final int PORTAL_ENTER_TICK = 20;
    private static final int PORTAL_HIDE_TICK = 55;
    private static final int PORTAL_TELEPORT_TICK = 56;
    private static final int PORTAL_REAPPEAR_TICK = 57;
    private static final int PORTAL_DAMAGE_TICK = 61;
    private static final int PORTAL_TRAVEL_INVULNERABLE_TICKS = PORTAL_REAPPEAR_TICK - PORTAL_HIDE_TICK;
    private static final int PORTAL_LIFETIME_TICKS = 84;

    private static final double PATROL_SPEED = 0.34D;
    private static final double COMBAT_FLIGHT_SPEED = 0.725D;
    private static final double GROUND_APPROACH_SPEED = 0.82D;
    private static final double ATTACK_START_RANGE_SQR = 42.25D;
    private static final double ATTACK_HIT_FORWARD_OFFSET = 1.7D;
    private static final double ATTACK_HIT_HALF_WIDTH = 1.85D;
    private static final double ATTACK_HIT_MIN_Y_OFFSET = -0.35D;
    private static final double ATTACK_HIT_MAX_Y_OFFSET = 2.3D;
    private static final double ATTACK_COMMIT_HORIZONTAL_RANGE = 4.2D;
    private static final double ATTACK_COMMIT_HORIZONTAL_RANGE_SQR = ATTACK_COMMIT_HORIZONTAL_RANGE * ATTACK_COMMIT_HORIZONTAL_RANGE;
    private static final double FLIGHT_ENGAGE_RANGE_SQR = 42.25D;
    private static final double FLIGHT_DISENGAGE_RANGE_SQR = 20.25D;
    private static final double FLIGHT_ATTACK_RANGE_SQR = 49.0D;
    private static final double ROAR_RETRY_DISTANCE_SQR = 400.0D;
    private static final double PHASE_TWO_HEALTH_FRACTION = 0.5D;
    private static final double PORTAL_RANGE = 72.0D;
    private static final double PORTAL_MIN_RANGE_SQR = 49.0D;
    private static final double PORTAL_ENTRY_EXIT_MIN_RANGE_SQR = 225.0D;
    private static final double PORTAL_EXIT_OFFSET = 15.0D;
    private static final double PORTAL_EXIT_SIDE_OFFSET = 4.5D;
    private static final double PORTAL_EXIT_AIR_HEIGHT = 3.0D;
    private static final double FLYING_APPROACH_STRAFE_OFFSET = 2.1D;
    private static final double FLYING_APPROACH_HEIGHT = 0.0D;
    private static final double FLYING_APPROACH_HEIGHT_PHASE_TWO = 0.12D;
    private static final double FLYING_APPROACH_TOO_CLOSE_RANGE_SQR = 9.0D;
    private static final double FLYING_OVERHEAD_STALL_RANGE_SQR = 4.0D;
    private static final double FLYING_OVERHEAD_STALL_HEIGHT = 1.35D;
    private static final double FLYING_OVERHEAD_RELEASE_HEIGHT = 1.0D;
    private static final double LOW_PROFILE_FLIGHT_DISENGAGE_RANGE_SQR = 12.25D;
    private static final double LOW_PROFILE_FLIGHT_DISENGAGE_VERTICAL = 1.4D;
    private static final double LOW_PROFILE_FLIGHT_DISENGAGE_OVERHEAD = 1.35D;
    private static final double FLYING_OVERHEAD_DESCENT_BACK_OFFSET = 2.3D;
    private static final double FLYING_OVERHEAD_DESCENT_SIDE_OFFSET = 2.4D;
    private static final double FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT = 1.2D;
    private static final double FLYING_ATTACK_BODY_TARGET_HEIGHT = 0.65D;
    private static final double FLYING_ATTACK_MIN_TARGET_HEIGHT = 0.16D;
    private static final double LOW_PROFILE_TARGET_MAX_HEIGHT = 1.2D;
    private static final double LOW_PROFILE_OVERHEAD_DESCENT_BACK_OFFSET = 2.9D;
    private static final double LOW_PROFILE_OVERHEAD_DESCENT_SIDE_OFFSET = 3.0D;
    private static final double LOW_PROFILE_TOO_CLOSE_BACK_OFFSET = 3.2D;
    private static final double LOW_PROFILE_TOO_CLOSE_STRAFE_SCALE = 0.85D;
    private static final double ANIMATION_MOVEMENT_THRESHOLD_SQR = 0.01D;

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation PHASE_TWO_IDLE_ANIM = RawAnimation.begin().thenLoop("idle_P2");
    private static final RawAnimation PHASE_TWO_WALK_ANIM = RawAnimation.begin().thenLoop("walk_P2");
    private static final RawAnimation PHASE_TWO_FLY_ANIM = RawAnimation.begin().thenLoop("fly_P2");
    private static final String LOCOMOTION_CONTROLLER = "main_controller";
    private static final String SPECIAL_CONTROLLER = "special_controller";
    private static final String SPECIAL_ATTACK_TRIGGER = "attack";
    private static final String SPECIAL_ATTACK_P2_TRIGGER = "attack_P2";
    private static final String SPECIAL_FLY_ATTACK_TRIGGER = "attack2";
    private static final String SPECIAL_FLY_ATTACK_P2_TRIGGER = "attack2_P2";
    private static final String SPECIAL_ROAR_TRIGGER = "roar";
    private static final String SPECIAL_ROAR_P2_TRIGGER = "roar_P2";
    private static final String SPECIAL_PHASE_TWO_TRIGGER = "phase2";
    private static final String SPECIAL_PORTAL_ENTER_TRIGGER = "portal_enter";
    private static final String SPECIAL_PORTAL_EXIT_TRIGGER = "portal_out";
    private static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation ATTACK_P2_ANIM = RawAnimation.begin().thenPlay("attack_P2");
    private static final RawAnimation FLY_ATTACK_ANIM = RawAnimation.begin().thenPlay("attack2");
    private static final RawAnimation FLY_ATTACK_P2_ANIM = RawAnimation.begin().thenPlay("attack2_P2");
    private static final RawAnimation ROAR_ANIM = RawAnimation.begin().thenPlay("roar");
    private static final RawAnimation ROAR_P2_ANIM = RawAnimation.begin().thenPlay("roar_P2");
    private static final RawAnimation PHASE_TWO_TRANSITION_ANIM = RawAnimation.begin().thenPlay("phase2");
    private static final RawAnimation PORTAL_ENTER_ANIM = RawAnimation.begin().thenPlay("portal_enter");
    private static final RawAnimation PORTAL_EXIT_ANIM = RawAnimation.begin().thenPlay("portal_out");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int attackCooldown;
    private int attackAnimationTicks;
    private int roarTicks;
    private int roarCooldown;
    private int targetlessTicks;
    private int airborneTicks;
    private int landingCooldown;
    private int groundMoveTicks;
    private int wingFlapCooldown;
    private int blockBreakCooldown;
    private int phaseTransitionTicks;
    private int portalCooldown;
    private int portalAttackTicks;
    private int portalTravelTicks;
    private int takeoffCommitTicks;
    private boolean attackHitApplied;
    private boolean introRoarUsed;
    private boolean flyingToTarget;
    private boolean portalAttackHitApplied;
    private boolean pendingPhaseTransition;
    private int lastTriggeredSpecialAnimation = SPECIAL_NONE;
    private Vec3 portalExitPos;
    @Nullable
    private NightmarePortalEntity entryPortal;
    @Nullable
    private NightmarePortalEntity exitPortal;

    public NightmareEntity(EntityType<? extends NightmareEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 12, true);
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.nightmareHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.nightmareAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.nightmareMovementSpeed())
                .add(Attributes.FLYING_SPEED, 0.375D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType spawnReason, net.minecraft.world.entity.SpawnGroupData spawnData) {
        com.craisinlord.antarchy.content.entity.ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.nightmareHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    public static boolean canSpawn(EntityType<NightmareEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        boolean sturdy = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        boolean posEmpty = level.isEmptyBlock(pos);
        boolean aboveEmpty = level.isEmptyBlock(pos.above());
        return level.getDifficulty() != Difficulty.PEACEFUL && sturdy && posEmpty && aboveEmpty;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(ACTIVE_SPECIAL_ANIMATION, SPECIAL_NONE);
        builder.define(ROARING, false);
        builder.define(PHASE_TWO, false);
        builder.define(PHASE_TWO_TEXTURE, false);
        builder.define(PHASE_TRANSITIONING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new NightmarePhaseTransitionGoal());
        this.goalSelector.addGoal(2, new NightmarePortalGoal());
        this.goalSelector.addGoal(3, new NightmareRoarGoal());
        this.goalSelector.addGoal(4, new NightmareFlyToTargetGoal());
        this.goalSelector.addGoal(5, new NightmareMeleeGoal());
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new NightmareWanderGoal());
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::canTargetEntity));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::canTargetEntity));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, LOCOMOTION_CONTROLLER, 0, this::mainAnimController));
        controllers.add(new AnimationController<>(this, SPECIAL_CONTROLLER, 0, state -> PlayState.STOP)
                .triggerableAnim(SPECIAL_ATTACK_TRIGGER, ATTACK_ANIM)
                .triggerableAnim(SPECIAL_ATTACK_P2_TRIGGER, ATTACK_P2_ANIM)
                .triggerableAnim(SPECIAL_FLY_ATTACK_TRIGGER, FLY_ATTACK_ANIM)
                .triggerableAnim(SPECIAL_FLY_ATTACK_P2_TRIGGER, FLY_ATTACK_P2_ANIM)
                .triggerableAnim(SPECIAL_ROAR_TRIGGER, ROAR_ANIM)
                .triggerableAnim(SPECIAL_ROAR_P2_TRIGGER, ROAR_P2_ANIM)
                .triggerableAnim(SPECIAL_PHASE_TWO_TRIGGER, PHASE_TWO_TRANSITION_ANIM)
                .triggerableAnim(SPECIAL_PORTAL_ENTER_TRIGGER, PORTAL_ENTER_ANIM)
                .triggerableAnim(SPECIAL_PORTAL_EXIT_TRIGGER, PORTAL_EXIT_ANIM)
                .triggerableAnim("death", DEATH_ANIM));
    }

    private PlayState mainAnimController(AnimationState<NightmareEntity> state) {
        if (this.shouldBlockLocomotionAnimation()) {
            return PlayState.STOP;
        }
        int animState = this.getAnimationState();
        state.getController().setAnimationSpeed(animState == ANIM_FLY ? 0.45D : 1.0D);
        boolean phaseTwo = this.isPhaseTwo();
        return switch (animState) {
            case ANIM_WALK -> state.setAndContinue(phaseTwo ? PHASE_TWO_WALK_ANIM : WALK_ANIM);
            case ANIM_FLY -> state.setAndContinue(phaseTwo ? PHASE_TWO_FLY_ANIM : FLY_ANIM);
            default -> state.setAndContinue(phaseTwo ? PHASE_TWO_IDLE_ANIM : IDLE_ANIM);
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

        if (this.level().isClientSide) {
            this.syncSpecialAnimationTrigger();
            this.tickClientParticles();
            this.updateFlightRotation();
            if (this.isRoaring()) {
                com.craisinlord.antarchy.content.client.CameraShakeClientState.register(
                        this,
                        com.craisinlord.antarchy.content.client.CameraShakeClientState.NIGHTMARE_RANGE,
                        com.craisinlord.antarchy.content.client.CameraShakeClientState.NIGHTMARE_STRENGTH,
                        this::isRoaring
                );
            }
            return;
        }

        this.setNoGravity(!this.pendingPhaseTransition
                && !this.isPhaseTransitioning()
                && (this.flyingToTarget || this.portalAttackTicks > 0 || this.portalTravelTicks > 0)
                && !this.isTooCloseToCeiling());

        int prevAirborneTicks = this.airborneTicks;
        this.airborneTicks = this.onGround() ? 0 : this.airborneTicks + 1;
        if (this.onGround() && prevAirborneTicks > 5) {
            this.landingCooldown = 32;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;
        if (this.roarCooldown > 0) this.roarCooldown--;
        if (this.blockBreakCooldown > 0) this.blockBreakCooldown--;
        if (this.landingCooldown > 0) this.landingCooldown--;
        if (this.portalCooldown > 0) this.portalCooldown--;
        if (this.portalTravelTicks > 0) this.portalTravelTicks--;
        if (this.takeoffCommitTicks > 0) this.takeoffCommitTicks--;
        this.setInvisible(this.portalTravelTicks > 0);

        LivingEntity target = this.getTarget();
        if (!this.canTargetEntity(target)) {
            this.setTarget(null);
            target = null;
        }

        if (target == null) {
            this.targetlessTicks++;
            if (this.targetlessTicks >= TARGET_RESET_TICKS) {
                this.introRoarUsed = false;
            }
        } else {
            this.targetlessTicks = 0;
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
        }

        if (this.isDeadOrDying()) {
            this.clearNightmarePortals();
            this.updateFlightRotation();
            return;
        }

        if (this.pendingPhaseTransition) {
            this.tickPendingPhaseTransition(target);
        }

        if (this.isPhaseTransitioning()) {
            this.tickPhaseTransition(target);
        } else if (this.portalAttackTicks > 0) {
            this.tickPortalAttack(target);
        } else if (this.attackAnimationTicks > 0) {
            this.tickAttack(target);
        }

        this.updateAnimationState();
        this.syncSpecialAnimationTrigger();
        this.updateFlightRotation();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isNoGravity()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isPhaseTransitioning() || this.portalTravelTicks > 0 || this.portalAttackTicks > 0) {
            return false;
        }
        if (!this.isPhaseTwo() && amount > 0.0F) {
            float threshold = (float) (this.getMaxHealth() * PHASE_TWO_HEALTH_FRACTION);
            if (this.getHealth() > threshold && this.getHealth() - amount <= threshold) {
                float clampedAmount = this.getHealth() - threshold;
                boolean hurt = clampedAmount > 0.0F && super.hurt(source, clampedAmount);
                if (hurt || clampedAmount <= 0.0F) {
                    this.setHealth(threshold);
                    this.beginPhaseTransitionSequence();
                    return true;
                }
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) return false;
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
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.DROWN) || super.isInvulnerableTo(source);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(effectInstance);
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
        tag.putInt(ACTIVE_SPECIAL_ANIMATION_KEY, this.getActiveSpecialAnimation());
        tag.putBoolean(PHASE_TWO_KEY, this.isPhaseTwo());
        tag.putBoolean(PHASE_TWO_TEXTURE_KEY, this.hasPhaseTwoTexture());
        tag.putBoolean(PHASE_TRANSITIONING_KEY, this.isPhaseTransitioning());
        tag.putInt(PHASE_TRANSITION_TICKS_KEY, this.phaseTransitionTicks);
        tag.putBoolean(PENDING_PHASE_TRANSITION_KEY, this.pendingPhaseTransition);
        tag.putInt(PORTAL_COOLDOWN_KEY, this.portalCooldown);
        tag.putInt(PORTAL_ATTACK_TICKS_KEY, this.portalAttackTicks);
        tag.putBoolean(PORTAL_ATTACK_HIT_KEY, this.portalAttackHitApplied);
        tag.putInt(PORTAL_TRAVEL_TICKS_KEY, this.portalTravelTicks);
        if (this.portalExitPos != null) {
            tag.putDouble(PORTAL_EXIT_X_KEY, this.portalExitPos.x);
            tag.putDouble(PORTAL_EXIT_Y_KEY, this.portalExitPos.y);
            tag.putDouble(PORTAL_EXIT_Z_KEY, this.portalExitPos.z);
        }
        if (this.entryPortal != null) {
            tag.putUUID(PORTAL_ENTRY_ID_KEY, this.entryPortal.getUUID());
        }
        if (this.exitPortal != null) {
            tag.putUUID(PORTAL_EXIT_ID_KEY, this.exitPortal.getUUID());
        }
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
        this.portalCooldown = tag.getInt(PORTAL_COOLDOWN_KEY);
        this.portalAttackTicks = tag.getInt(PORTAL_ATTACK_TICKS_KEY);
        this.portalAttackHitApplied = tag.getBoolean(PORTAL_ATTACK_HIT_KEY);
        this.portalTravelTicks = tag.getInt(PORTAL_TRAVEL_TICKS_KEY);
        this.entityData.set(ROARING, tag.getBoolean("Roaring"));
        this.entityData.set(ANIMATION_STATE, tag.contains(ANIMATION_STATE_KEY) ? tag.getInt(ANIMATION_STATE_KEY) : ANIM_IDLE);
        this.entityData.set(ACTIVE_SPECIAL_ANIMATION, tag.contains(ACTIVE_SPECIAL_ANIMATION_KEY) ? tag.getInt(ACTIVE_SPECIAL_ANIMATION_KEY) : SPECIAL_NONE);
        this.setPhaseTwo(tag.getBoolean(PHASE_TWO_KEY));
        this.setPhaseTwoTexture(tag.contains(PHASE_TWO_TEXTURE_KEY) ? tag.getBoolean(PHASE_TWO_TEXTURE_KEY) : this.isPhaseTwo() && !this.isPhaseTransitioning());
        this.setPhaseTransitioning(tag.getBoolean(PHASE_TRANSITIONING_KEY));
        this.phaseTransitionTicks = tag.getInt(PHASE_TRANSITION_TICKS_KEY);
        this.pendingPhaseTransition = tag.getBoolean(PENDING_PHASE_TRANSITION_KEY);
        if (tag.contains(PORTAL_EXIT_X_KEY)) {
            this.portalExitPos = new Vec3(tag.getDouble(PORTAL_EXIT_X_KEY), tag.getDouble(PORTAL_EXIT_Y_KEY), tag.getDouble(PORTAL_EXIT_Z_KEY));
        } else {
            this.portalExitPos = null;
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.attackAnimationTicks = 0;
            this.attackCooldown = 0;
            this.roarTicks = 0;
            this.portalAttackTicks = 0;
            this.portalTravelTicks = 0;
            this.phaseTransitionTicks = 0;
            this.pendingPhaseTransition = false;
            this.flyingToTarget = false;
            this.clearActiveSpecialAnimation();
            this.setRoaring(false);
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(false);
            this.setInvisible(false);
            this.getNavigation().stop();
            this.clearNightmarePortals();
        }
        super.die(damageSource);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime == 1) {
            this.selectSpecialAnimation(SPECIAL_DEATH);
        }
        this.setNoGravity(false);
        if (!this.onGround()) {
            Vec3 fall = this.getDeltaMovement().multiply(0.6D, 0.0D, 0.6D);
            this.setDeltaMovement(fall.x, Math.min(fall.y, 0.0D) - 0.08D, fall.z);
            this.hasImpulse = true;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
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

    public int getActiveSpecialAnimation() {
        return this.entityData.get(ACTIVE_SPECIAL_ANIMATION);
    }

    public boolean isPhaseTwo() {
        return this.entityData.get(PHASE_TWO);
    }

    public boolean hasPhaseTwoTexture() {
        return this.entityData.get(PHASE_TWO_TEXTURE);
    }

    public boolean isPhaseTransitioning() {
        return this.entityData.get(PHASE_TRANSITIONING);
    }

    private void setRoaring(boolean roaring) {
        this.entityData.set(ROARING, roaring);
    }

    private void setAnimationState(int animationState) {
        if (this.entityData.get(ANIMATION_STATE) != animationState) {
            this.entityData.set(ANIMATION_STATE, animationState);
        }
    }

    private void setActiveSpecialAnimation(int specialAnimation) {
        if (this.entityData.get(ACTIVE_SPECIAL_ANIMATION) != specialAnimation) {
            this.entityData.set(ACTIVE_SPECIAL_ANIMATION, specialAnimation);
        }
    }

    private void setPhaseTwo(boolean phaseTwo) {
        this.entityData.set(PHASE_TWO, phaseTwo);
    }

    private void setPhaseTwoTexture(boolean phaseTwoTexture) {
        this.entityData.set(PHASE_TWO_TEXTURE, phaseTwoTexture);
    }

    private void setPhaseTransitioning(boolean phaseTransitioning) {
        this.entityData.set(PHASE_TRANSITIONING, phaseTransitioning);
    }

    private boolean shouldStartRoar(LivingEntity target) {
        if (!this.onGround() || this.roarCooldown > 0 || !this.hasLineOfSight(target)) return false;
        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr > ROAR_RETRY_DISTANCE_SQR || !this.isInFront(target.position(), -0.1D)) return false;
        return distanceSqr > ATTACK_START_RANGE_SQR || this.isPhaseTwo();
    }

    private void startRoar() {
        this.roarTicks = this.introRoarUsed ? COMBAT_ROAR_TICKS : INTRO_ROAR_TICKS;
        this.introRoarUsed = true;
        this.roarCooldown = 420 + this.random.nextInt(180);
        this.attackAnimationTicks = 0;
        this.attackHitApplied = false;
        this.setRoaring(true);
        this.selectSpecialAnimation(this.isPhaseTwo() ? SPECIAL_ROAR_P2 : SPECIAL_ROAR);
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
            this.flyingToTarget = false;
            this.takeoffCommitTicks = 0;
            this.landingCooldown = Math.max(this.landingCooldown, ROAR_FLIGHT_RECOVERY_TICKS);
            this.clearActiveSpecialAnimation();
            this.attackCooldown = Math.max(this.attackCooldown, 18);
        }
    }

    private void startAttack(LivingEntity target) {
        this.attackAnimationTicks = ATTACK_TOTAL_TICKS;
        this.attackHitApplied = false;
        this.attackCooldown = ATTACK_TOTAL_TICKS + 12;
        this.selectSpecialAnimation(this.selectAttackSpecialAnimation());
        this.getNavigation().stop();
        this.faceTowardTarget(target, 180.0F);
        Vec3 lungeTarget = this.shouldUseFlyingLocomotion()
                ? new Vec3(target.getX(), this.getFlyingAttackTargetY(target), target.getZ())
                : target.getEyePosition();
        Vec3 lunge = lungeTarget.subtract(this.getEyePosition());
        if (lunge.lengthSqr() > 1.0E-4D) {
            Vec3 normalized = lunge.normalize();
            double verticalBoost = this.shouldUseFlyingLocomotion() ? 0.34D : 0.18D;
            Vec3 current = this.getDeltaMovement();
            double upward = this.shouldUseFlyingLocomotion()
                    ? Math.max(normalized.y * verticalBoost, -0.08D)
                    : normalized.y * verticalBoost;
            this.setDeltaMovement(
                    normalized.x * 0.85D,
                    current.y * 0.18D + upward,
                    normalized.z * 0.85D
            );
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
            this.performAttackHit(target);
        }
        if (--this.attackAnimationTicks <= 0) {
            this.attackAnimationTicks = 0;
            this.clearActiveSpecialAnimation();
        }
    }

    private void beginPhaseTransitionSequence() {
        this.attackAnimationTicks = 0;
        this.portalAttackTicks = 0;
        this.portalTravelTicks = 0;
        this.attackHitApplied = false;
        this.portalAttackHitApplied = false;
        this.portalExitPos = null;
        this.pendingPhaseTransition = !this.onGround();
        this.flyingToTarget = false;
        this.clearNightmarePortals();
        this.getNavigation().stop();
        this.setRoaring(false);
        this.setInvisible(false);
        this.setNoGravity(false);
        this.roarCooldown = Math.max(this.roarCooldown, 80);
        this.portalCooldown = Math.max(this.portalCooldown, 140);
        if (this.pendingPhaseTransition) {
            Vec3 fall = this.getDeltaMovement();
            this.setDeltaMovement(fall.x * 0.25D, Math.min(fall.y, 0.0D) - 0.08D, fall.z * 0.25D);
            this.clearActiveSpecialAnimation();
            return;
        }
        this.startPhaseTransition();
    }

    private void startPhaseTransition() {
        this.setPhaseTwo(true);
        this.setPhaseTwoTexture(false);
        this.setPhaseTransitioning(true);
        this.pendingPhaseTransition = false;
        this.flyingToTarget = false;
        this.selectSpecialAnimation(SPECIAL_PHASE_TWO);
        this.phaseTransitionTicks = PHASE_TWO_TRANSITION_TICKS;
        this.attackAnimationTicks = 0;
        this.portalAttackTicks = 0;
        this.portalTravelTicks = 0;
        this.attackHitApplied = false;
        this.portalAttackHitApplied = false;
        this.portalExitPos = null;
        this.clearNightmarePortals();
        this.getNavigation().stop();
        this.setRoaring(false);
        this.setInvisible(false);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(false);
        this.roarCooldown = Math.max(this.roarCooldown, 80);
        this.portalCooldown = Math.max(this.portalCooldown, 140);
        this.playSound(AntarchySoundEvents.NIGHTMARE_ROAR.get(), 2.2F, 0.68F + this.random.nextFloat() * 0.06F);
    }

    private void tickPendingPhaseTransition(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setNoGravity(false);
        this.flyingToTarget = false;
        this.clearActiveSpecialAnimation();
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
        }
        if (!this.onGround()) {
            Vec3 fall = this.getDeltaMovement();
            this.setDeltaMovement(fall.x * 0.35D, Math.min(fall.y, 0.0D) - 0.08D, fall.z * 0.35D);
            this.hasImpulse = true;
            return;
        }
        this.setDeltaMovement(Vec3.ZERO);
        this.startPhaseTransition();
    }

    private void tickPhaseTransition(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setNoGravity(false);
        if (!this.onGround()) {
            Vec3 fall = this.getDeltaMovement();
            this.setDeltaMovement(fall.x * 0.2D, Math.min(fall.y, 0.0D) - 0.08D, fall.z * 0.2D);
            this.hasImpulse = true;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
        }
        if (this.phaseTransitionTicks % 8 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.9D, this.getZ(), 12, 1.0D, 0.7D, 1.0D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.9D, this.getZ(), 10, 0.8D, 0.5D, 0.8D, 0.02D);
        }
        if (!this.hasPhaseTwoTexture() && PHASE_TWO_TRANSITION_TICKS - this.phaseTransitionTicks >= PHASE_TWO_TEXTURE_SWAP_TICKS) {
            this.setPhaseTwoTexture(true);
        }
        if (--this.phaseTransitionTicks <= 0) {
            this.phaseTransitionTicks = 0;
            this.setPhaseTwoTexture(true);
            this.setPhaseTransitioning(false);
            this.clearActiveSpecialAnimation();
            this.attackCooldown = Math.max(this.attackCooldown, 18);
        }
    }

    private boolean canStartPortalAttack(LivingEntity target) {
        Vec3 entryPos = this.getPortalEntryPosition();
        if (!this.isPhaseTwo() || this.isPhaseTransitioning() || this.portalCooldown > 0 || this.attackAnimationTicks > 0 || this.roarTicks > 0) return false;
        if (!this.hasLineOfSight(target) || this.distanceToSqr(target) > PORTAL_RANGE * PORTAL_RANGE) return false;
        if (this.horizontalDistanceToSqr(target) < PORTAL_MIN_RANGE_SQR) return false;
        return this.portalAttackTicks <= 0 && this.findPortalExitPos(target, entryPos) != null;
    }

    private void startPortalAttack(LivingEntity target) {
        Vec3 entryPos = this.getPortalEntryPosition();
        Vec3 exitPos = this.findPortalExitPos(target, entryPos);
        if (exitPos == null || !(this.level() instanceof ServerLevel serverLevel)) return;
        this.clearNightmarePortals();
        this.portalExitPos = exitPos;
        this.portalAttackTicks = PORTAL_TOTAL_TICKS;
        this.portalTravelTicks = 0;
        this.portalAttackHitApplied = false;
        this.attackAnimationTicks = 0;
        this.attackHitApplied = false;
        this.attackCooldown = 24;
        this.portalCooldown = 180 + this.random.nextInt(100);
        this.setInvisible(false);
        this.clearActiveSpecialAnimation();
        this.getNavigation().stop();
        this.entryPortal = NightmarePortalEntity.spawnAt(serverLevel, entryPos, this, this.position(), PORTAL_LIFETIME_TICKS);
        this.exitPortal = NightmarePortalEntity.spawnAt(serverLevel, exitPos, this, this.position(), PORTAL_LIFETIME_TICKS);
        if (this.entryPortal != null && this.exitPortal != null) {
            this.entryPortal.linkTo(this.exitPortal);
            this.exitPortal.linkTo(this.entryPortal);
        }
    }

    private void tickPortalAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.55D));
        if (target != null) {
            this.getLookControl().setLookAt(target, 35.0F, 20.0F);
            this.faceTowardTarget(target, 12.0F);
        }
        int elapsed = PORTAL_TOTAL_TICKS - this.portalAttackTicks;
        if (elapsed == PORTAL_ENTER_TICK) {
            this.selectSpecialAnimation(SPECIAL_PORTAL_ENTER);
            this.flyingToTarget = false;
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = true;
        }
        if (elapsed >= PORTAL_ENTER_TICK && elapsed < PORTAL_REAPPEAR_TICK) {
            this.flyingToTarget = false;
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (elapsed == PORTAL_HIDE_TICK) {
            this.portalTravelTicks = Math.max(this.portalTravelTicks, PORTAL_TRAVEL_INVULNERABLE_TICKS);
            this.setInvisible(true);
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = true;
        }
        if (elapsed == PORTAL_TELEPORT_TICK && this.portalExitPos != null) {
            this.executePortalTeleport(target);
        }
        if (elapsed == PORTAL_REAPPEAR_TICK) {
            this.setInvisible(false);
            this.flyingToTarget = !this.onGround();
            this.selectSpecialAnimation(SPECIAL_PORTAL_EXIT);
            if (target != null) {
                this.faceTowardTarget(target, 180.0F);
            }
            this.resumePortalExitFlight(target);
        }
        if (elapsed > PORTAL_REAPPEAR_TICK && !this.onGround()) {
            this.resumePortalExitFlight(target);
        }
        if (!this.portalAttackHitApplied && elapsed >= PORTAL_DAMAGE_TICK) {
            this.portalAttackHitApplied = true;
            this.performPortalExitHit();
        }
        if (--this.portalAttackTicks <= 0) {
            this.portalAttackTicks = 0;
            this.portalExitPos = null;
            this.clearNightmarePortals();
            this.clearActiveSpecialAnimation();
            this.setInvisible(false);
        }
    }

    private void executePortalTeleport(@Nullable LivingEntity target) {
        if (this.portalExitPos == null) return;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 0.22F, 0.96F + this.random.nextFloat() * 0.08F);
        this.portalTravelTicks = PORTAL_TRAVEL_INVULNERABLE_TICKS;
        this.setPortalCooldown();
        this.setInvisible(true);
        this.teleportTo(this.portalExitPos.x, this.portalExitPos.y, this.portalExitPos.z);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.3F, 0.92F + this.random.nextFloat() * 0.08F);
        if (target != null) {
            this.faceTowardTarget(target, 180.0F);
        }
        this.setDeltaMovement(Vec3.ZERO);
        this.hasImpulse = true;
    }

    private void resumePortalExitFlight(@Nullable LivingEntity target) {
        if (this.onGround()) {
            this.flyingToTarget = false;
            return;
        }
        Vec3 destination = target != null
                ? this.getFlightApproachPosition(target)
                : this.position().add(this.getViewVector(1.0F).scale(3.0D));
        this.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, COMBAT_FLIGHT_SPEED);
        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 0.04D) {
            Vec3 launch = destination.subtract(this.position());
            if (launch.lengthSqr() < 1.0E-4D) {
                launch = this.getViewVector(1.0F);
            }
            if (launch.lengthSqr() > 1.0E-4D) {
                Vec3 normalized = launch.normalize();
                this.setDeltaMovement(normalized.x * 0.42D, Math.max(normalized.y * 0.22D, -0.02D), normalized.z * 0.42D);
                this.hasImpulse = true;
            }
        }
    }

    private void performAttackHit(@Nullable LivingEntity target) {
        Vec3 forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            float yawRadians = this.getYRot() * Mth.DEG_TO_RAD;
            forward = new Vec3(-Mth.sin(yawRadians), 0.0D, Mth.cos(yawRadians));
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 strikeOrigin = this.position()
                .add(0.0D, this.getBbHeight() * 0.55D, 0.0D)
                .add(forward.scale(0.4D));
        Vec3 strikeCenter = strikeOrigin
                .add(forward.scale(ATTACK_HIT_FORWARD_OFFSET));
        AABB hitBox = new AABB(
                Math.min(strikeOrigin.x, strikeCenter.x) - ATTACK_HIT_HALF_WIDTH,
                this.getBoundingBox().minY + ATTACK_HIT_MIN_Y_OFFSET,
                Math.min(strikeOrigin.z, strikeCenter.z) - ATTACK_HIT_HALF_WIDTH,
                Math.max(strikeOrigin.x, strikeCenter.x) + ATTACK_HIT_HALF_WIDTH,
                this.getBoundingBox().minY + ATTACK_HIT_MAX_Y_OFFSET,
                Math.max(strikeOrigin.z, strikeCenter.z) + ATTACK_HIT_HALF_WIDTH
        );
        List<LivingEntity> victims = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::canTargetEntity));
        if (target != null && this.canTargetEntity(target) && !victims.contains(target) && this.canConnectAttackTo(target, forward, hitBox)) {
            victims.add(0, target);
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        for (LivingEntity victim : victims) {
            if (!this.canConnectAttackTo(victim, forward, hitBox)) continue;
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

    private boolean canConnectAttackTo(LivingEntity victim, Vec3 forward, AABB hitBox) {
        if (!this.hasLineOfSight(victim)) {
            return false;
        }
        Vec3 victimCenter = victim.getBoundingBox().getCenter();
        if (!this.isInFront(victimCenter, -0.42D)) {
            return false;
        }
        if (victim.getBoundingBox().inflate(0.1D).intersects(hitBox)) {
            return true;
        }
        Vec3 toVictim = victimCenter.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        double forwardDistance = toVictim.dot(forward);
        if (forwardDistance < 0.15D || forwardDistance > ATTACK_HIT_FORWARD_OFFSET + 1.25D) {
            return false;
        }
        Vec3 lateral = toVictim.subtract(forward.scale(forwardDistance));
        if (lateral.lengthSqr() > (ATTACK_HIT_HALF_WIDTH + 0.55D) * (ATTACK_HIT_HALF_WIDTH + 0.55D)) {
            return false;
        }
        double minY = this.getBoundingBox().minY + ATTACK_HIT_MIN_Y_OFFSET;
        double maxY = this.getBoundingBox().minY + ATTACK_HIT_MAX_Y_OFFSET + 0.4D;
        return victim.getBoundingBox().maxY >= minY && victim.getBoundingBox().minY <= maxY;
    }

    private void performPortalExitHit() {
        if (this.portalExitPos == null) return;
        AABB hitBox = this.getBoundingBox().move(this.portalExitPos.subtract(this.position())).inflate(1.7D, 1.2D, 1.7D);
        float damage = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.15F);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::canTargetEntity);
        for (LivingEntity victim : victims) {
            if (victim.hurt(this.nightmareDamageSource(), damage)) {
                this.applyNightmareStrikeEffects(victim);
                this.knockAway(victim, 1.35D, 0.35D);
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.portalExitPos.x, this.portalExitPos.y + 1.0D, this.portalExitPos.z, 18, 0.9D, 0.45D, 0.9D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.portalExitPos.x, this.portalExitPos.y + 1.0D, this.portalExitPos.z, 14, 0.8D, 0.3D, 0.8D, 0.03D);
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
            serverLevel.sendParticles(AntarchyObjects.NIGHTMARE_FIRE_FLAME.get(), target.getX(), target.getY(0.8D), target.getZ(), 10, 0.28D, 0.22D, 0.28D, 0.01D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, target.getX(), target.getY(0.8D), target.getZ(), 6, 0.22D, 0.16D, 0.22D, 0.02D);
        }
    }

    private void knockAway(LivingEntity target, double horizontalStrength, double verticalStrength) {
        Vec3 direction = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) direction = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        if (direction.lengthSqr() < 1.0E-4D) direction = new Vec3(1.0D, 0.0D, 0.0D);
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

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType()) return false;
        if (entity.getType().is(AntarchyTags.Entities.NIGHTMARE_NO_ATTACK)) return false;
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }
        return entity instanceof Mob && entity.isAttackable();
    }

    private boolean canStartAttackOn(LivingEntity target) {
        if (!this.hasLineOfSight(target) || this.distanceToSqr(target) > ATTACK_START_RANGE_SQR) return false;
        double hdSqr = this.horizontalDistanceToSqr(target);
        double verticalDistance = Math.abs(target.getEyeY() - this.getEyeY());
        if (this.shouldUseFlyingLocomotion()) {
            double overheadGap = this.getY() - target.getY();
            return hdSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE_SQR
                    && verticalDistance <= 2.5D
                    && overheadGap <= FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT;
        }
        return hdSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE_SQR && verticalDistance <= 4.0D;
    }

    private boolean shouldUseFlightPressure(LivingEntity target) {
        if (target == null || this.isTooCloseToCeiling()) {
            return false;
        }
        boolean lowProfileTarget = this.isLowProfileFlightTarget(target);
        if (this.flyingToTarget && this.takeoffCommitTicks > 0) {
            return true;
        }
        double distanceSqr = this.distanceToSqr(target);
        double verticalDistance = Math.abs(target.getEyeY() - this.getEyeY());
        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double overheadGap = this.getY() - target.getY();
        if (lowProfileTarget
                && horizontalDistanceSqr <= LOW_PROFILE_FLIGHT_DISENGAGE_RANGE_SQR
                && verticalDistance <= LOW_PROFILE_FLIGHT_DISENGAGE_VERTICAL
                && overheadGap <= LOW_PROFILE_FLIGHT_DISENGAGE_OVERHEAD) {
            return false;
        }
        if (this.flyingToTarget) {
            if (horizontalDistanceSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE_SQR
                    && verticalDistance <= FLYING_OVERHEAD_RELEASE_HEIGHT
                    && overheadGap <= FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT) {
                return false;
            }
            return distanceSqr > ATTACK_COMMIT_HORIZONTAL_RANGE_SQR
                    || verticalDistance > 1.0D
                    || overheadGap > FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT;
        }
        if (distanceSqr >= FLIGHT_ENGAGE_RANGE_SQR) {
            return true;
        }
        return this.isPhaseTwo() && (distanceSqr >= FLIGHT_DISENGAGE_RANGE_SQR || verticalDistance > 1.5D);
    }

    private Vec3 getFlightApproachPosition(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        }
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 forward = toTarget.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        double strafe = this.tickCount % 32 < 16 ? FLYING_APPROACH_STRAFE_OFFSET : -FLYING_APPROACH_STRAFE_OFFSET;
        double height = this.isPhaseTwo() ? FLYING_APPROACH_HEIGHT_PHASE_TWO : FLYING_APPROACH_HEIGHT;
        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double overheadGap = this.getY() - target.getY();
        boolean lowProfileTarget = this.isLowProfileFlightTarget(target);
        double attackTargetY = this.getFlyingAttackTargetY(target);
        Vec3 anchor = target.position().subtract(forward.scale(1.8D)).add(right.scale(strafe));
        if (horizontalDistanceSqr <= FLYING_OVERHEAD_STALL_RANGE_SQR && overheadGap >= FLYING_OVERHEAD_STALL_HEIGHT) {
            double sideOffset = lowProfileTarget ? LOW_PROFILE_OVERHEAD_DESCENT_SIDE_OFFSET : FLYING_OVERHEAD_DESCENT_SIDE_OFFSET;
            double backOffset = lowProfileTarget ? LOW_PROFILE_OVERHEAD_DESCENT_BACK_OFFSET : FLYING_OVERHEAD_DESCENT_BACK_OFFSET;
            anchor = target.position()
                    .add(right.scale(strafe > 0.0D ? sideOffset : -sideOffset))
                    .subtract(forward.scale(backOffset));
            return new Vec3(anchor.x, attackTargetY, anchor.z);
        }
        if (horizontalDistanceSqr <= FLYING_APPROACH_TOO_CLOSE_RANGE_SQR) {
            double backOffset = lowProfileTarget ? LOW_PROFILE_TOO_CLOSE_BACK_OFFSET : 2.8D;
            double strafeScale = lowProfileTarget ? LOW_PROFILE_TOO_CLOSE_STRAFE_SCALE : 0.6D;
            anchor = target.position().subtract(forward.scale(backOffset)).add(right.scale(strafe * strafeScale));
        }
        return new Vec3(anchor.x, attackTargetY + height, anchor.z);
    }

    private Vec3 getFlightDescentPosition(LivingEntity target) {
        Vec3 toTarget = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 forward = toTarget.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        double side = this.tickCount % 20 < 10 ? 1.0D : -1.0D;
        boolean lowProfileTarget = this.isLowProfileFlightTarget(target);
        double sideOffset = lowProfileTarget ? 1.15D : 0.9D;
        double backOffset = lowProfileTarget ? 1.2D : 0.9D;
        Vec3 anchor = target.position()
                .subtract(forward.scale(backOffset))
                .add(right.scale(side * sideOffset));
        return new Vec3(anchor.x, this.getFlyingAttackTargetY(target), anchor.z);
    }

    private boolean isLowProfileFlightTarget(LivingEntity target) {
        return target.getBbHeight() <= LOW_PROFILE_TARGET_MAX_HEIGHT;
    }

    private double getFlyingAttackTargetY(LivingEntity target) {
        double targetHeight = Mth.clamp(target.getBbHeight() * 0.4D, FLYING_ATTACK_MIN_TARGET_HEIGHT, FLYING_ATTACK_BODY_TARGET_HEIGHT);
        return target.getY() + targetHeight;
    }

    private double horizontalDistanceToSqr(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        return dx * dx + dz * dz;
    }

    private boolean isInFront(Vec3 position, double minimumDot) {
        Vec3 forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        Vec3 toTarget = position.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D || toTarget.lengthSqr() < 1.0E-4D) return true;
        return forward.normalize().dot(toTarget.normalize()) >= minimumDot;
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

    private boolean isTooCloseToCeiling() {
        BlockPos current = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        for (int i = 1; i <= FLIGHT_CEILING_CLEARANCE_BLOCKS; i++) {
            if (!this.level().isEmptyBlock(current.above(i))) return true;
        }
        return false;
    }

    private Vec3 getPortalEntryPosition() {
        return this.position().add(this.getViewVector(1.0F).multiply(1.6D, 0.0D, 1.6D));
    }

    @Nullable
    private Vec3 findPortalExitPos(LivingEntity target, Vec3 entryPos) {
        Vec3 forward = target.getLookAngle().multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3[] candidates = new Vec3[] {
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).add(0.0D, PORTAL_EXIT_AIR_HEIGHT, 0.0D),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).add(right.scale(PORTAL_EXIT_SIDE_OFFSET)),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).subtract(right.scale(PORTAL_EXIT_SIDE_OFFSET)),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).add(right.scale(PORTAL_EXIT_SIDE_OFFSET)).add(0.0D, PORTAL_EXIT_AIR_HEIGHT, 0.0D),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).subtract(right.scale(PORTAL_EXIT_SIDE_OFFSET)).add(0.0D, PORTAL_EXIT_AIR_HEIGHT, 0.0D)
        };
        for (Vec3 candidate : candidates) {
            Vec3 adjusted = this.findValidPortalPosition(candidate);
            if (adjusted != null
                    && adjusted.distanceToSqr(this.position()) <= PORTAL_RANGE * PORTAL_RANGE
                    && adjusted.distanceToSqr(entryPos) >= PORTAL_ENTRY_EXIT_MIN_RANGE_SQR) {
                return adjusted;
            }
        }
        return null;
    }

    @Nullable
    private Vec3 findValidPortalPosition(Vec3 base) {
        EntityDimensions dimensions = this.getType().getDimensions();
        for (int yOffset = 4; yOffset >= -3; yOffset--) {
            Vec3 candidate = new Vec3(base.x, base.y + yOffset, base.z);
            AABB box = dimensions.makeBoundingBox(candidate);
            if (this.level().noCollision(this, box)) {
                return candidate;
            }
        }
        return null;
    }

    private void clearNightmarePortals() {
        if (this.entryPortal != null && this.entryPortal.isAlive()) {
            this.entryPortal.discard();
        }
        if (this.exitPortal != null && this.exitPortal.isAlive()) {
            this.exitPortal.discard();
        }
        this.entryPortal = null;
        this.exitPortal = null;
    }

    private void tickClientParticles() {
        if ((this.isRoaring() || this.attackAnimationTicks > 0 || this.portalAttackTicks > 0 || this.isPhaseTransitioning()) && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + 1.2D + this.random.nextDouble() * 1.1D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    0.0D, 0.03D, 0.0D
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

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.groundMoveTicks = 0;
            this.wingFlapCooldown = 0;
            return;
        }
        if (this.hasActiveSpecialAnimation()) {
            if (this.shouldBlockLocomotionAnimation()) {
                this.groundMoveTicks = 0;
                this.tickWingFlapSound();
                return;
            }
        }
        if (this.flyingToTarget) {
            this.groundMoveTicks = 0;
            this.setAnimationState(ANIM_FLY);
            this.tickWingFlapSound();
            return;
        }
        if (this.shouldUseFlyingLocomotion()) {
            this.groundMoveTicks = 0;
            this.setAnimationState(ANIM_FLY);
            this.tickWingFlapSound();
            return;
        }
        Vec3 horizontalVelocity = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        if (horizontalVelocity.lengthSqr() > ANIMATION_MOVEMENT_THRESHOLD_SQR) {
            this.groundMoveTicks = 6;
        }
        if (this.groundMoveTicks > 0) {
            this.groundMoveTicks--;
            this.setAnimationState(ANIM_WALK);
            this.wingFlapCooldown = 0;
            return;
        }
        this.setAnimationState(ANIM_IDLE);
        this.wingFlapCooldown = 0;
    }

    private void tickWingFlapSound() {
        if (!(this.level() instanceof ServerLevel)) return;
        if (this.onGround() || this.isRoaring() || this.isDeadOrDying()) {
            this.wingFlapCooldown = 0;
            return;
        }
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 0.01D) return;
        if (this.wingFlapCooldown > 0) {
            this.wingFlapCooldown--;
            return;
        }
        this.playSound(AntarchySoundEvents.NIGHTMARE_FLAP.get(), 1.25F, 0.85F + this.random.nextFloat() * 0.08F);
        this.wingFlapCooldown = 5 + this.random.nextInt(4);
    }

    private boolean hasActiveSpecialAnimation() {
        return this.getActiveSpecialAnimation() != SPECIAL_NONE;
    }

    private boolean shouldBlockLocomotionAnimation() {
        int specialAnimation = this.getActiveSpecialAnimation();
        if (specialAnimation == SPECIAL_NONE) {
            return false;
        }
        if (specialAnimation == SPECIAL_ROAR || specialAnimation == SPECIAL_ROAR_P2) {
            return false;
        }
        if (specialAnimation == SPECIAL_PORTAL_EXIT) {
            return false;
        }
        if (specialAnimation == SPECIAL_PHASE_TWO) {
            return true;
        }
        return true;
    }

    private boolean shouldUseFlyingLocomotion() {
        return this.flyingToTarget || this.isNoGravity() || !this.onGround() || this.airborneTicks >= MIN_AIRBORNE_TICKS_FOR_FLY_ANIM;
    }

    private int selectAttackSpecialAnimation() {
        boolean flyingAttack = this.shouldUseFlyingLocomotion();
        if (flyingAttack) {
            return this.isPhaseTwo() ? SPECIAL_FLY_ATTACK_P2 : SPECIAL_FLY_ATTACK;
        }
        return this.isPhaseTwo() ? SPECIAL_ATTACK_P2 : SPECIAL_ATTACK;
    }

    private void selectSpecialAnimation(int specialAnimation) {
        this.setActiveSpecialAnimation(specialAnimation);
    }

    private void clearActiveSpecialAnimation() {
        this.setActiveSpecialAnimation(SPECIAL_NONE);
    }

    private void syncSpecialAnimationTrigger() {
        int activeSpecialAnimation = this.getActiveSpecialAnimation();
        if (activeSpecialAnimation != SPECIAL_NONE && this.lastTriggeredSpecialAnimation != activeSpecialAnimation) {
            String triggerName = this.specialAnimationTrigger(activeSpecialAnimation);
            if (triggerName != null) {
                this.triggerAnim(SPECIAL_CONTROLLER, triggerName);
            }
            this.lastTriggeredSpecialAnimation = activeSpecialAnimation;
            return;
        }
        if (activeSpecialAnimation == SPECIAL_NONE) {
            this.lastTriggeredSpecialAnimation = SPECIAL_NONE;
        }
    }

    @Nullable
    private String specialAnimationTrigger(int specialAnimation) {
        return switch (specialAnimation) {
            case SPECIAL_ATTACK -> SPECIAL_ATTACK_TRIGGER;
            case SPECIAL_ATTACK_P2 -> SPECIAL_ATTACK_P2_TRIGGER;
            case SPECIAL_FLY_ATTACK -> SPECIAL_FLY_ATTACK_TRIGGER;
            case SPECIAL_FLY_ATTACK_P2 -> SPECIAL_FLY_ATTACK_P2_TRIGGER;
            case SPECIAL_ROAR -> SPECIAL_ROAR_TRIGGER;
            case SPECIAL_ROAR_P2 -> SPECIAL_ROAR_P2_TRIGGER;
            case SPECIAL_PHASE_TWO -> SPECIAL_PHASE_TWO_TRIGGER;
            case SPECIAL_PORTAL_ENTER -> SPECIAL_PORTAL_ENTER_TRIGGER;
            case SPECIAL_PORTAL_EXIT -> SPECIAL_PORTAL_EXIT_TRIGGER;
            case SPECIAL_DEATH -> "death";
            default -> null;
        };
    }

    private final class NightmarePhaseTransitionGoal extends Goal {
        NightmarePhaseTransitionGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return NightmareEntity.this.isPhaseTransitioning();
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.isPhaseTransitioning();
        }
    }

    private final class NightmarePortalGoal extends Goal {
        NightmarePortalGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target != null && NightmareEntity.this.canStartPortalAttack(target);
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.portalAttackTicks > 0;
        }

        @Override
        public void start() {
            LivingEntity target = NightmareEntity.this.getTarget();
            if (target != null) {
                NightmareEntity.this.startPortalAttack(target);
            }
        }
    }

    private final class NightmareRoarGoal extends Goal {
        NightmareRoarGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target instanceof Player && NightmareEntity.this.shouldStartRoar(target) && !NightmareEntity.this.isPhaseTransitioning() && NightmareEntity.this.portalAttackTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.isRoaring();
        }

        @Override
        public void start() {
            NightmareEntity.this.startRoar();
        }

        @Override
        public void tick() {
            NightmareEntity.this.tickRoar(NightmareEntity.this.getTarget());
        }

        @Override
        public void stop() {
            NightmareEntity.this.roarTicks = 0;
            NightmareEntity.this.setRoaring(false);
        }
    }

    private final class NightmareFlyToTargetGoal extends Goal {
        NightmareFlyToTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target != null
                    && NightmareEntity.this.landingCooldown <= 0
                    && NightmareEntity.this.shouldUseFlightPressure(target)
                    && !NightmareEntity.this.isPhaseTransitioning()
                    && !NightmareEntity.this.isRoaring()
                    && NightmareEntity.this.portalAttackTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return target != null
                    && NightmareEntity.this.shouldUseFlightPressure(target)
                    && !NightmareEntity.this.isRoaring()
                    && NightmareEntity.this.portalAttackTicks <= 0;
        }

        @Override
        public void start() {
            NightmareEntity.this.flyingToTarget = true;
            NightmareEntity.this.takeoffCommitTicks = TAKEOFF_COMMIT_TICKS;
        }

        @Override
        public void stop() {
            NightmareEntity.this.flyingToTarget = false;
            NightmareEntity.this.landingCooldown = 18;
            NightmareEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = NightmareEntity.this.getTarget();
            if (target == null) return;
            if (NightmareEntity.this.isTooCloseToCeiling()) {
                NightmareEntity.this.flyingToTarget = false;
                NightmareEntity.this.getNavigation().stop();
                return;
            }
            if (NightmareEntity.this.onGround() && NightmareEntity.this.distanceToSqr(target) >= FLIGHT_DISENGAGE_RANGE_SQR) {
                NightmareEntity.this.setDeltaMovement(NightmareEntity.this.getDeltaMovement().add(0.0D, 0.38D, 0.0D));
                NightmareEntity.this.hasImpulse = true;
            }
            double horizontalDistanceSqr = NightmareEntity.this.horizontalDistanceToSqr(target);
            double overheadGap = NightmareEntity.this.getY() - target.getY();
            if (horizontalDistanceSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE_SQR
                    && overheadGap > FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT) {
                Vec3 descentPos = NightmareEntity.this.getFlightDescentPosition(target);
                NightmareEntity.this.faceTowardTarget(target, 14.0F);
                NightmareEntity.this.getMoveControl().setWantedPosition(
                        descentPos.x,
                        descentPos.y,
                        descentPos.z,
                        COMBAT_FLIGHT_SPEED * 1.08D
                );
                return;
            }
            if (horizontalDistanceSqr <= FLYING_OVERHEAD_STALL_RANGE_SQR
                    && overheadGap >= FLYING_OVERHEAD_STALL_HEIGHT
                    && NightmareEntity.this.attackCooldown <= 0
                    && NightmareEntity.this.canStartAttackOn(target)) {
                NightmareEntity.this.startAttack(target);
                return;
            }
            if (NightmareEntity.this.attackCooldown <= 0 && NightmareEntity.this.canStartAttackOn(target)) {
                NightmareEntity.this.startAttack(target);
                return;
            }
            Vec3 approachPos = NightmareEntity.this.getFlightApproachPosition(target);
            NightmareEntity.this.faceTowardTarget(target, 10.0F);
            NightmareEntity.this.getMoveControl().setWantedPosition(approachPos.x, approachPos.y, approachPos.z, COMBAT_FLIGHT_SPEED);
        }
    }

    private final class NightmareMeleeGoal extends MeleeAttackGoal {
        NightmareMeleeGoal() {
            super(NightmareEntity.this, GROUND_APPROACH_SPEED, true);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return !NightmareEntity.this.isPhaseTransitioning()
                    && NightmareEntity.this.portalAttackTicks <= 0
                    && target != null
                    && !NightmareEntity.this.shouldUseFlightPressure(target)
                    && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return !NightmareEntity.this.isPhaseTransitioning()
                    && NightmareEntity.this.portalAttackTicks <= 0
                    && target != null
                    && !NightmareEntity.this.shouldUseFlightPressure(target)
                    && super.canContinueToUse();
        }

        @Override
        public void tick() {
            LivingEntity target = NightmareEntity.this.getTarget();
            if (target != null) {
                double hdSqr = NightmareEntity.this.horizontalDistanceToSqr(target);
                double vertDelta = NightmareEntity.this.getY() - target.getY();
                if (target.onGround() && hdSqr <= 25.0D && vertDelta > 1.6D) {
                    NightmareEntity.this.tryBreakBlocksToTarget(target);
                }
            }
            super.tick();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            if (NightmareEntity.this.shouldUseFlightPressure(target) && NightmareEntity.this.distanceToSqr(target) >= FLIGHT_ATTACK_RANGE_SQR) {
                return;
            }
            if (NightmareEntity.this.attackCooldown <= 0 && NightmareEntity.this.canStartAttackOn(target)) {
                NightmareEntity.this.startAttack(target);
            }
        }
    }

    private final class NightmareWanderGoal extends Goal {
        private double x;
        private double y;
        private double z;
        private int wanderTicks;

        NightmareWanderGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (NightmareEntity.this.getTarget() != null || NightmareEntity.this.isDeadOrDying() || NightmareEntity.this.isPhaseTransitioning() || NightmareEntity.this.portalAttackTicks > 0) return false;
            if (NightmareEntity.this.random.nextInt(40) != 0) return false;
            double angle = NightmareEntity.this.random.nextDouble() * Math.PI * 2;
            double dist = 4 + NightmareEntity.this.random.nextDouble() * 10;
            this.x = NightmareEntity.this.getX() + Math.cos(angle) * dist;
            this.z = NightmareEntity.this.getZ() + Math.sin(angle) * dist;
            this.y = NightmareEntity.this.getY();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wanderTicks > 0
                    && NightmareEntity.this.getTarget() == null
                    && !NightmareEntity.this.isDeadOrDying()
                    && NightmareEntity.this.distanceToSqr(this.x, this.y, this.z) > 2.25;
        }

        @Override
        public void start() {
            this.wanderTicks = 80 + NightmareEntity.this.random.nextInt(40);
        }

        @Override
        public void tick() {
            this.wanderTicks--;
            NightmareEntity.this.getMoveControl().setWantedPosition(this.x, this.y, this.z, PATROL_SPEED);
        }
    }
}
