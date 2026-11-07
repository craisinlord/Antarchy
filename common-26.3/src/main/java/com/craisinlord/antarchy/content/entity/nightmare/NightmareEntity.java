package com.craisinlord.antarchy.content.entity.nightmare;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.entity.ConfiguredMobSpawnUtil;
import com.craisinlord.antarchy.content.entity.NearestFirstTargetGoal;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import com.craisinlord.anthology.shake.client.CameraShake;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.util.GeckoLibUtil;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NightmareEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTIVE_SPECIAL_ANIMATION = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO_TEXTURE = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TRANSITIONING = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> BODY_PITCH = SynchedEntityData.defineId(NightmareEntity.class, EntityDataSerializers.FLOAT);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String ROAR_COOLDOWN_KEY = "RoarCooldown";
    private static final String INTRO_ROAR_USED_KEY = "IntroRoarUsed";
    private static final String TARGETLESS_TICKS_KEY = "TargetlessTicks";
    private static final String PHASE_TWO_KEY = "PhaseTwo";
    private static final String PHASE_TWO_TEXTURE_KEY = "PhaseTwoTexture";
    private static final String PHASE_TRANSITIONING_KEY = "PhaseTransitioning";
    private static final String PHASE_TRANSITION_TICKS_KEY = "PhaseTransitionTicks";
    private static final String PENDING_PHASE_TRANSITION_KEY = "PendingPhaseTransition";
    private static final String PORTAL_COOLDOWN_KEY = "PortalCooldown";

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
    private static final int ATTACK_COMMIT_TICK = 6;
    private static final int ATTACK_DAMAGE_TICK = 11;
    private static final int ATTACK_RECOVERY_MISS_TICKS = 14;
    private static final int ATTACK_RECOVERY_HIT_TICKS = 6;
    private static final int ROAR_PULSE_REMAINING_TICKS = 18;
    private static final double ROAR_PULSE_RADIUS = 16.0D;
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
    private static final int PENDING_PHASE_TIMEOUT_TICKS = 100;
    private static final int TARGET_MEMORY_TICKS = 240;
    private static final int TARGET_HOLD_TICKS = 60;
    private static final int FLIGHT_GOAL_LOST_TICKS = 20;
    private static final int FLIGHT_STUCK_TICKS = 50;
    private static final int FORCED_GROUND_TICKS = 100;
    private static final int LAST_SEEN_PURSUIT_TICKS = 20;
    private static final int UNREACHABLE_TICKS = 60;
    private static final int UNREACHABLE_REPEAT_DELAY_TICKS = 40;
    private static final double UNREACHABLE_RANGE_SQR = 64.0D;
    private static final int PORTAL_UNSEEN_TICKS = 40;
    private static final int WING_BEAT_TICKS = 44;
    private static final int SWOOP_NONE = 0;
    private static final int SWOOP_WINDUP = 1;
    private static final int SWOOP_DIVE = 2;
    private static final int SWOOP_WINDUP_TICKS = 14;
    private static final int SWOOP_MAX_DIVE_TICKS = 40;
    private static final int SWOOP_RECOVERY_TICKS = 24;
    private static final int SWOOP_CRASH_RECOVERY_TICKS = 40;
    private static final int SWOOP_CLIP_TICKS = 20;
    private static final int SWOOP_FAILED_RETRY_TICKS = 40;
    private static final int SWOOP_REPEAT_CHANCE_DIVISOR = 4;
    private static final double SWOOP_SPEED = 1.1D;
    private static final double SWOOP_OVERSHOOT_SPEED = 1.0D;
    private static final double SWOOP_OVERSHOOT = 7.0D;
    private static final double SWOOP_MIN_RANGE_SQR = 64.0D;
    private static final double SWOOP_MAX_RANGE_SQR = 400.0D;
    private static final double SWOOP_MAX_VERTICAL = 10.0D;
    private static final double SWOOP_CLIP_LEAD_BLOCKS = 13.0D;
    private static final double SWOOP_COMMIT_ARC_DOT = 0.6D;
    private static final float SWOOP_DAMAGE_MULTIPLIER = 1.2F;
    private static final float AMBUSH_DAMAGE_MULTIPLIER = 1.15F;
    private static final double AMBUSH_MAX_RANGE_SQR = 144.0D;
    private static final double ATTACK_ARC_DOT = 0.35D;
    private static final double ATTACK_START_ARC_DOT = 0.3D;
    private static final double ATTACK_MAX_OFFSET_ABOVE = 4.2D;
    private static final double GROUND_ATTACK_MAX_OFFSET_BELOW = 2.0D;
    private static final float ATTACK_START_TURN_DEGREES = 30.0F;
    private static final float ATTACK_WINDUP_TURN_DEGREES = 12.0F;
    private static final float FLIGHT_TURN_DEGREES = 7.0F;
    private static final float GROUND_TURN_DEGREES = 12.0F;
    private static final float GROUND_FACE_TURN_DEGREES = 8.0F;
    private static final float HEAD_YAW_DEGREES = 20.0F;
    private static final float HEAD_PITCH_DEGREES = 15.0F;
    private static final float MAX_BODY_PITCH_DEGREES = 25.0F;
    private static final double GROUND_FACE_RANGE_SQR = 64.0D;
    private static final double FLIGHT_ARRIVAL_SPEED_SCALE = 0.22D;
    private static final double FLIGHT_ACCEL_SCALE = 1.5D;
    private static final int PHASE_TWO_TRANSITION_TICKS = 80;
    private static final int PHASE_TWO_TEXTURE_SWAP_TICKS = 53;
    private static final int PORTAL_TOTAL_TICKS = 79;
    private static final int PORTAL_EXIT_OPEN_TICK = 34;
    private static final int PORTAL_ENTER_TICK = 20;
    private static final int PORTAL_HIDE_TICK = 55;
    private static final int PORTAL_TELEPORT_TICK = 56;
    private static final int PORTAL_REAPPEAR_TICK = 57;
    private static final int PORTAL_TRAVEL_INVULNERABLE_TICKS = PORTAL_REAPPEAR_TICK - PORTAL_HIDE_TICK;
    private static final int PORTAL_LIFETIME_TICKS = 89;

    private static final double PATROL_SPEED = 0.34D;
    private static final double COMBAT_FLIGHT_SPEED = 0.725D;
    private static final double GROUND_APPROACH_SPEED = 0.82D;
    private static final double ATTACK_START_RANGE_SQR = 42.25D;
    private static final double ATTACK_LUNGE_SPEED = 0.55D;
    private static final double AMBUSH_LUNGE_SPEED = 0.75D;
    private static final double ATTACK_HIT_FORWARD_OFFSET = 1.7D;
    private static final double ATTACK_HIT_HALF_WIDTH = 1.85D;
    private static final double ATTACK_HIT_MIN_Y_OFFSET = -0.35D;
    private static final double ATTACK_HIT_MAX_Y_OFFSET = 4.4D;
    private static final double ATTACK_COMMIT_HORIZONTAL_RANGE = 4.2D;
    private static final double ATTACK_COMMIT_HORIZONTAL_RANGE_SQR = ATTACK_COMMIT_HORIZONTAL_RANGE * ATTACK_COMMIT_HORIZONTAL_RANGE;
    private static final double FLIGHT_ENGAGE_RANGE_SQR = 42.25D;
    private static final double FLIGHT_DISENGAGE_RANGE_SQR = 30.25D;
    private static final double FLIGHT_ATTACK_RANGE_SQR = 49.0D;
    private static final double ROAR_RETRY_DISTANCE_SQR = 400.0D;
    private static final double PHASE_TWO_HEALTH_FRACTION = 0.5D;
    private static final int PORTAL_SEARCH_RETRY_TICKS = 10;
    private static final double PORTAL_RANGE = 72.0D;
    private static final double PORTAL_MIN_RANGE_SQR = 81.0D;
    private static final double PORTAL_ENTRY_EXIT_MIN_RANGE_SQR = 64.0D;
    private static final double PORTAL_EXIT_OFFSET = 6.0D;
    private static final double PORTAL_EXIT_SIDE_OFFSET = 4.0D;
    private static final double PORTAL_EXIT_AIR_HEIGHT = 2.5D;
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

    private static final int[] PORTAL_EXIT_HEIGHT_ORDER = {0, 1, -1, 2, -2, 3, 4, -3};

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation PHASE_TWO_IDLE_ANIM = RawAnimation.begin().thenLoop("idle_P2");
    private static final RawAnimation PHASE_TWO_WALK_ANIM = RawAnimation.begin().thenLoop("walk_P2");
    private static final RawAnimation PHASE_TWO_FLY_ANIM = RawAnimation.begin().thenLoop("fly_P2");
    private static final String LOCOMOTION_CONTROLLER = "main_controller";
    private static final int LOCOMOTION_TRANSITION_TICKS = 5;
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
    private int attackRecoveryTicks;
    private boolean attackConnected;
    private boolean ambushAttack;
    private boolean lastAttackWasSwoop;
    private int swoopPhase = SWOOP_NONE;
    private int swoopTicks;
    private int swoopCooldown;
    private int swoopClipTicks;
    private double swoopTravelled;
    private double swoopStrikeDistance;
    private boolean swoopClipStarted;
    private boolean swoopRepeatAllowed;
    @Nullable
    private Vec3 swoopDirection;
    @Nullable
    private Vec3 swoopOvershootDirection;
    private final Set<UUID> swoopHits = new HashSet<>();
    private int pendingPhaseTicks;
    private int unseenTargetTicks;
    private int unreachableTicks;
    private boolean unreachablePressure;
    @Nullable
    private Vec3 lastSeenTargetPos;
    private int targetHoldTicks;
    private int forcedGroundTicks;
    private int flightGoalTick;
    private int flightStuckTicks;
    private double flightBestDistance = Double.MAX_VALUE;
    private int approachSide = 1;
    private int ceilingCheckTick = -1;
    private boolean ceilingCheckResult;
    private float clientBodyPitch;
    private float clientBodyPitchO;
    private int roarTicks;
    private boolean roarPulsePending;
    private int roarCooldown;
    private int targetlessTicks;
    private int airborneTicks;
    private int landingCooldown;
    private int groundMoveTicks;
    private int wingFlapCooldown;
    private int blockBreakCooldown;
    private int phaseTransitionTicks;
    private int portalCooldown;
    private int portalSearchRetryTicks;
    private int portalAttackTicks;
    private int portalTravelTicks;
    private int takeoffCommitTicks;
    private boolean attackHitApplied;
    private boolean introRoarUsed;
    private boolean flyingToTarget;
    private boolean pendingPhaseTransition;
    private int lastTriggeredSpecialAnimation = SPECIAL_NONE;
    private Vec3 portalExitPos;
    @Nullable
    private NightmarePortalEntity entryPortal;
    @Nullable
    private NightmarePortalEntity exitPortal;

    public NightmareEntity(EntityType<? extends NightmareEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new NightmareMoveControl();
        this.xpReward = 25;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.nightmareHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.nightmareAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.nightmareMovementSpeed())
                .add(Attributes.FLYING_SPEED, 0.375D)
                .add(Attributes.STEP_HEIGHT, 1.1D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.ARMOR, AntarchySettings.nightmareMobArmor())
                .add(Attributes.ARMOR_TOUGHNESS, AntarchySettings.nightmareMobArmorToughness());
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason spawnReason, SpawnGroupData spawnData) {
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.nightmareHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    public static boolean canSpawn(EntityType<NightmareEntity> entityType, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == EntitySpawnReason.TRIAL_SPAWNER) {
            return level.getDifficulty() != Difficulty.PEACEFUL;
        }
        boolean posEmpty = level.isEmptyBlock(pos);
        boolean floorSpawn = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) && level.isEmptyBlock(pos.above());
        boolean ceilingSpawn = level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN) && level.isEmptyBlock(pos.below());
        return level.getDifficulty() != Difficulty.PEACEFUL && posEmpty && (floorSpawn || ceilingSpawn);
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
        builder.define(BODY_PITCH, 0.0F);
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
        this.targetSelector.addGoal(1, new NightmareHurtByTargetGoal().setAlertOthers().setUnseenMemoryTicks(TARGET_MEMORY_TICKS));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (entity, level) -> this.canTargetEntity(entity)).setUnseenMemoryTicks(TARGET_MEMORY_TICKS));
        this.targetSelector.addGoal(3, new NearestFirstTargetGoal<>(this, Mob.class, 10, true, false, (entity, level) -> this.canTargetEntity(entity)).setUnseenMemoryTicks(TARGET_MEMORY_TICKS));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<NightmareEntity>(LOCOMOTION_CONTROLLER, LOCOMOTION_TRANSITION_TICKS, this::mainAnimController));
        controllers.add(new AnimationController<NightmareEntity>(SPECIAL_CONTROLLER, 0, state -> PlayState.STOP)
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

    private PlayState mainAnimController(AnimationTest<NightmareEntity> state) {
        if (this.shouldBlockLocomotionAnimation()) {
            state.controller().reset();
            return PlayState.STOP;
        }
        int animState = this.getAnimationState();
        state.controller().setAnimationSpeed(animState == ANIM_FLY ? 0.45D : 1.0D);
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
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.getNodeEvaluator().setCanPassDoors(true);
        return navigation;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.syncSpecialAnimationTrigger();
            this.tickClientParticles();
            this.clientBodyPitchO = this.clientBodyPitch;
            this.clientBodyPitch = Mth.approachDegrees(this.clientBodyPitch, this.entityData.get(BODY_PITCH), 2.5F);
            if (this.isRoaring()) {
                CameraShake.register(
                        this,
                        32.0D,
                        1.35F,
                        this::isRoaring
                );
            }
            return;
        }

        if (this.isAttackBusy()) {
            this.flightGoalTick = this.tickCount;
        }
        if (this.flyingToTarget
                && this.portalAttackTicks <= 0
                && this.swoopPhase == SWOOP_NONE
                && this.tickCount - this.flightGoalTick > FLIGHT_GOAL_LOST_TICKS) {
            this.flyingToTarget = false;
        }

        this.setNoGravity(!this.pendingPhaseTransition
                && !this.isPhaseTransitioning()
                && ((this.flyingToTarget && !this.isTooCloseToCeiling())
                        || this.portalAttackTicks > 0
                        || this.portalTravelTicks > 0
                        || this.swoopPhase != SWOOP_NONE));

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
        if (this.portalSearchRetryTicks > 0) this.portalSearchRetryTicks--;
        if (this.portalTravelTicks > 0) this.portalTravelTicks--;
        if (this.takeoffCommitTicks > 0) this.takeoffCommitTicks--;
        if (this.targetHoldTicks > 0) this.targetHoldTicks--;
        if (this.forcedGroundTicks > 0) this.forcedGroundTicks--;
        if (this.swoopCooldown > 0) this.swoopCooldown--;
        if (this.attackRecoveryTicks > 0) this.attackRecoveryTicks--;
        if (this.swoopClipTicks > 0 && --this.swoopClipTicks == 0 && this.attackAnimationTicks <= 0) {
            this.clearActiveSpecialAnimation();
        }
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
            this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
        }
        this.tickTargetMemory(target);
        this.tickUnreachableTarget(target);

        if (this.isDeadOrDying()) {
            this.clearNightmarePortals();
            this.setBodyPitch(0.0F);
            return;
        }

        if (this.pendingPhaseTransition) {
            this.tickPendingPhaseTransition(target);
        }

        if (this.isPhaseTransitioning()) {
            this.tickPhaseTransition(target);
        } else if (this.portalAttackTicks > 0) {
            this.tickPortalAttack(target);
        } else if (this.swoopPhase != SWOOP_NONE) {
            this.tickSwoop(target);
        } else if (this.attackAnimationTicks > 0) {
            this.tickAttack(target);
        }

        this.updateAnimationState();
        this.syncSpecialAnimationTrigger();
        this.updateFacing(target);
        this.updateBodyPitch();
    }

    private void tickUnreachableTarget(@Nullable LivingEntity target) {
        if (!(target instanceof Player)) {
            this.unreachableTicks = 0;
            this.unreachablePressure = false;
            return;
        }
        if (this.isAttackBusy() || this.isCombatLocked() || this.isRoaring() || this.portalAttackTicks > 0 || this.attackCooldown > 0) {
            return;
        }
        if (this.horizontalDistanceToSqr(target) > UNREACHABLE_RANGE_SQR || this.canStartAttackOn(target)) {
            this.unreachableTicks = 0;
            return;
        }
        if (++this.unreachableTicks >= UNREACHABLE_TICKS) {
            this.unreachableTicks = 0;
            this.unreachablePressure = true;
            this.roarCooldown = 0;
            this.forcedGroundTicks = Math.max(this.forcedGroundTicks, FORCED_GROUND_TICKS);
            this.flyingToTarget = false;
        }
    }

    private void tickTargetMemory(@Nullable LivingEntity target) {
        if (target == null) {
            this.unseenTargetTicks = 0;
            this.lastSeenTargetPos = null;
            return;
        }
        if (this.getSensing().hasLineOfSight(target)) {
            this.unseenTargetTicks = 0;
            this.lastSeenTargetPos = target.position();
        } else {
            this.unseenTargetTicks++;
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && target != this.getTarget()) {
            this.targetHoldTicks = TARGET_HOLD_TICKS;
        }
        super.setTarget(target);
    }

    public float getBodyPitch(float partialTick) {
        return Mth.lerp(partialTick, this.clientBodyPitchO, this.clientBodyPitch);
    }

    private void setBodyPitch(float pitch) {
        if (Math.abs(this.entityData.get(BODY_PITCH) - pitch) >= 1.5F || (pitch == 0.0F && this.entityData.get(BODY_PITCH) != 0.0F)) {
            this.entityData.set(BODY_PITCH, pitch);
        }
    }

    private void updateBodyPitch() {
        if (!this.isNoGravity()) {
            this.setBodyPitch(0.0F);
            return;
        }
        Vec3 velocity = this.toLocal(this.getDeltaMovement());
        if (velocity.lengthSqr() < 0.01D) {
            this.setBodyPitch(0.0F);
            return;
        }
        float pitch = (float) (Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI));
        this.setBodyPitch(Mth.clamp(pitch, -MAX_BODY_PITCH_DEGREES, MAX_BODY_PITCH_DEGREES));
    }

    private boolean isAttackBusy() {
        return this.attackAnimationTicks > 0 || this.attackRecoveryTicks > 0 || this.swoopPhase != SWOOP_NONE;
    }

    private boolean isCombatLocked() {
        return this.isPhaseTransitioning() || this.pendingPhaseTransition;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isNoGravity()) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        boolean bypassesProtection = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (!bypassesProtection && (this.isCombatLocked() || this.portalTravelTicks > 0)) {
            return false;
        }
        if (!bypassesProtection && !this.isPhaseTwo() && amount > 0.0F) {
            float threshold = (float) (this.getMaxHealth() * PHASE_TWO_HEALTH_FRACTION);
            float mitigated = this.getDamageAfterMagicAbsorb(source, this.getDamageAfterArmorAbsorb(source, amount));
            if (this.getHealth() > threshold && this.getHealth() - mitigated <= threshold) {
                float clampedAmount = this.getHealth() - threshold;
                boolean hurt = clampedAmount > 0.0F && super.hurtServer(level, source, clampedAmount);
                if (hurt || clampedAmount <= 0.0F) {
                    this.setHealth(threshold);
                    this.beginPhaseTransitionSequence();
                    return true;
                }
                return false;
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target) {
        if (!(target instanceof LivingEntity livingTarget)) {
            return false;
        }
        boolean hurt = livingTarget.hurtServer(serverLevel, AntarchyDamageSources.nightmareMauling(serverLevel, this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (hurt) {
            this.playNightmareBiteSound();
            this.applyNightmareStrikeEffects(livingTarget);
        }
        return hurt;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
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
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return source.is(DamageTypes.DROWN) || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() == MobEffects.WITHER) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
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
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(ATTACK_COOLDOWN_KEY, this.attackCooldown);
        output.putInt(ROAR_COOLDOWN_KEY, this.roarCooldown);
        output.putBoolean(INTRO_ROAR_USED_KEY, this.introRoarUsed);
        output.putInt(TARGETLESS_TICKS_KEY, this.targetlessTicks);
        output.putInt("AirborneTicks", this.airborneTicks);
        output.putBoolean(PHASE_TWO_KEY, this.isPhaseTwo());
        output.putBoolean(PHASE_TWO_TEXTURE_KEY, this.hasPhaseTwoTexture());
        output.putBoolean(PHASE_TRANSITIONING_KEY, this.isPhaseTransitioning());
        output.putInt(PHASE_TRANSITION_TICKS_KEY, this.phaseTransitionTicks);
        output.putBoolean(PENDING_PHASE_TRANSITION_KEY, this.pendingPhaseTransition);
        output.putInt(PORTAL_COOLDOWN_KEY, this.portalCooldown);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.attackCooldown = input.getIntOr(ATTACK_COOLDOWN_KEY, 0);
        this.roarCooldown = input.getIntOr(ROAR_COOLDOWN_KEY, 0);
        this.introRoarUsed = input.getBooleanOr(INTRO_ROAR_USED_KEY, false);
        this.targetlessTicks = input.getIntOr(TARGETLESS_TICKS_KEY, 0);
        this.airborneTicks = input.getIntOr("AirborneTicks", 0);
        this.portalCooldown = input.getIntOr(PORTAL_COOLDOWN_KEY, 0);
        this.setPhaseTwo(input.getBooleanOr(PHASE_TWO_KEY, false));
        this.setPhaseTwoTexture(input.read(PHASE_TWO_TEXTURE_KEY, Codec.BOOL).orElse(this.isPhaseTwo() && !this.isPhaseTransitioning()));
        this.setPhaseTransitioning(input.getBooleanOr(PHASE_TRANSITIONING_KEY, false));
        this.phaseTransitionTicks = input.getIntOr(PHASE_TRANSITION_TICKS_KEY, 0);
        this.pendingPhaseTransition = input.getBooleanOr(PENDING_PHASE_TRANSITION_KEY, false);
        this.setActiveSpecialAnimation(this.isPhaseTransitioning() ? SPECIAL_PHASE_TWO : SPECIAL_NONE);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.attackAnimationTicks = 0;
            this.attackRecoveryTicks = 0;
            this.attackCooldown = 0;
            this.roarTicks = 0;
            this.cancelSwoop();
            this.portalAttackTicks = 0;
            this.portalTravelTicks = 0;
            this.phaseTransitionTicks = 0;
            this.setPhaseTransitioning(false);
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
        if (this.deathTime == 1 && !this.level().isClientSide()) {
            this.selectSpecialAnimation(SPECIAL_DEATH);
        }
        this.setNoGravity(false);
        if (!this.onGround()) {
            Vec3 fall = this.getDeltaMovement().multiply(0.6D, 0.0D, 0.6D);
            this.setDeltaMovement(fall.x, Math.min(fall.y, 0.0D) - 0.08D, fall.z);
            this.needsSync = true;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        if (this.deathTime >= DEATH_TICKS && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    public boolean isRoaring() {
        return this.entityData.get(ROARING);
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    public void setArchivePreviewFlying() {
        this.setAnimationState(ANIM_FLY);
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
        if (!this.onGround() || this.roarCooldown > 0) {
            return false;
        }
        if (this.unreachablePressure) {
            return this.hasLineOfSight(target);
        }
        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr > ROAR_RETRY_DISTANCE_SQR || !this.isInFront(target.position(), -0.1D)) {
            return false;
        }
        return (distanceSqr > ATTACK_START_RANGE_SQR || this.isPhaseTwo()) && this.hasLineOfSight(target);
    }

    private void startRoar() {
        this.roarPulsePending = this.introRoarUsed || this.unreachablePressure;
        this.unreachablePressure = false;
        this.unreachableTicks = -UNREACHABLE_REPEAT_DELAY_TICKS;
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
            this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
            this.faceTowardTarget(target, 14.0F);
        }
        if (this.roarPulsePending && this.roarTicks <= ROAR_PULSE_REMAINING_TICKS) {
            this.roarPulsePending = false;
            this.performRoarPulse();
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
        this.startAttack(target, false);
    }

    private void startAttack(LivingEntity target, boolean ambush) {
        this.attackAnimationTicks = ATTACK_TOTAL_TICKS;
        this.attackRecoveryTicks = 0;
        this.attackHitApplied = false;
        this.attackConnected = false;
        this.ambushAttack = ambush;
        this.lastAttackWasSwoop = false;
        this.attackCooldown = ATTACK_TOTAL_TICKS + ATTACK_RECOVERY_MISS_TICKS + 4;
        this.selectSpecialAnimation(this.selectAttackSpecialAnimation());
        this.getNavigation().stop();
        this.faceTowardTarget(target, ambush ? 180.0F : ATTACK_START_TURN_DEGREES);
    }

    private void commitAttackLunge(@Nullable LivingEntity target) {
        Vec3 localForward = this.toLocalPlane(this.getViewVector(1.0F));
        if (localForward.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 forward = localForward.normalize();
        boolean flying = this.shouldUseFlyingLocomotion();
        double vertical = 0.0D;
        if (target != null) {
            Vec3 lungeTarget = flying ? this.getFlyingAttackTargetPos(target) : target.getEyePosition();
            Vec3 lunge = this.toLocal(lungeTarget.subtract(this.getEyePosition()));
            if (lunge.lengthSqr() > 1.0E-4D) {
                vertical = lunge.normalize().y * (flying ? 0.34D : 0.18D);
            }
        }
        Vec3 current = this.toLocal(this.getDeltaMovement());
        double upward = flying ? Math.max(vertical, -0.08D) : vertical;
        double speed = this.ambushAttack ? AMBUSH_LUNGE_SPEED : ATTACK_LUNGE_SPEED;
        this.setDeltaMovement(this.toWorld(new Vec3(forward.x * speed, current.y * 0.18D + upward, forward.z * speed)));
        this.needsSync = true;
    }

    private void tickAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.93D));
        int elapsed = ATTACK_TOTAL_TICKS - this.attackAnimationTicks;
        if (target != null) {
            this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
            if (elapsed < ATTACK_COMMIT_TICK) {
                this.faceTowardTarget(target, ATTACK_WINDUP_TURN_DEGREES);
            }
        }
        if (elapsed == ATTACK_COMMIT_TICK) {
            this.commitAttackLunge(target);
        }
        if (!this.attackHitApplied && elapsed >= ATTACK_DAMAGE_TICK) {
            this.attackHitApplied = true;
            this.playNightmareBiteSound();
            this.attackConnected = this.performAttackHit(target);
        }
        if (--this.attackAnimationTicks <= 0) {
            this.attackAnimationTicks = 0;
            this.attackRecoveryTicks = this.attackConnected ? ATTACK_RECOVERY_HIT_TICKS : ATTACK_RECOVERY_MISS_TICKS;
            this.ambushAttack = false;
            this.clearActiveSpecialAnimation();
        }
    }

    private boolean canStartSwoop(LivingEntity target) {
        if (this.attackCooldown > 0 || this.swoopCooldown > 0 || !this.isNoGravity() || this.unseenTargetTicks > 0) {
            return false;
        }
        if (this.lastAttackWasSwoop && !this.swoopRepeatAllowed) {
            return false;
        }
        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        return horizontalDistanceSqr >= SWOOP_MIN_RANGE_SQR
                && horizontalDistanceSqr <= SWOOP_MAX_RANGE_SQR
                && Math.abs(this.verticalOffsetTo(target)) <= SWOOP_MAX_VERTICAL
                && !this.isTooCloseToCeiling();
    }

    private void startSwoop() {
        this.swoopPhase = SWOOP_WINDUP;
        this.swoopTicks = SWOOP_WINDUP_TICKS;
        this.swoopHits.clear();
        this.getNavigation().stop();
        this.playSound(AntarchySoundEvents.NIGHTMARE_ROAR.get(), 1.2F, 1.15F + this.random.nextFloat() * 0.1F);
    }

    private void tickSwoop(@Nullable LivingEntity target) {
        if (this.swoopPhase == SWOOP_WINDUP) {
            this.tickSwoopWindup(target);
        } else {
            this.tickSwoopDive();
        }
    }

    private void tickSwoopWindup(@Nullable LivingEntity target) {
        if (target == null) {
            this.finishSwoop(SWOOP_FAILED_RETRY_TICKS, false);
            return;
        }
        this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
        this.faceTowardTarget(target, FLIGHT_TURN_DEGREES);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7D).add(this.toWorld(new Vec3(0.0D, 0.03D, 0.0D))));
        if (this.swoopTicks % 3 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(AntarchyObjects.NIGHTMARE_FIRE_FLAME.get(), this.getX(), this.getY() + 2.0D, this.getZ(), 6, 1.0D, 0.5D, 1.0D, 0.01D);
        }
        if (--this.swoopTicks > 0) {
            return;
        }
        Vec3 strike = this.getFlyingAttackTargetPos(target);
        Vec3 toStrike = strike.subtract(this.position());
        Vec3 localForward = this.toLocalPlane(toStrike);
        if (toStrike.lengthSqr() < 1.0E-4D || localForward.lengthSqr() < 1.0E-4D || !this.isInFront(target.position(), SWOOP_COMMIT_ARC_DOT)) {
            this.finishSwoop(SWOOP_FAILED_RETRY_TICKS, false);
            return;
        }
        Vec3 horizontal = localForward.normalize();
        Vec3 overshootDirection = this.toWorld(new Vec3(horizontal.x, 0.2D, horizontal.z).normalize());
        Vec3 overshootEnd = strike.add(overshootDirection.scale(SWOOP_OVERSHOOT));
        if (!this.isSwoopLaneClear(this.position(), strike) || !this.isSwoopLaneClear(strike, overshootEnd)) {
            this.finishSwoop(SWOOP_FAILED_RETRY_TICKS, false);
            return;
        }
        this.swoopPhase = SWOOP_DIVE;
        this.swoopTicks = SWOOP_MAX_DIVE_TICKS;
        this.swoopTravelled = 0.0D;
        this.swoopStrikeDistance = toStrike.length();
        this.swoopDirection = toStrike.normalize();
        this.swoopOvershootDirection = overshootDirection;
        this.swoopClipStarted = false;
        this.lastAttackWasSwoop = true;
        this.playSound(AntarchySoundEvents.NIGHTMARE_FLAP.get(), 1.6F, 0.7F);
    }

    private void tickSwoopDive() {
        if (this.swoopDirection == null || this.swoopOvershootDirection == null) {
            this.finishSwoop(SWOOP_FAILED_RETRY_TICKS, false);
            return;
        }
        boolean crashed = this.swoopTravelled > 0.0D && (this.horizontalCollision || this.verticalCollision);
        boolean pastStrike = this.swoopTravelled >= this.swoopStrikeDistance;
        if (crashed || --this.swoopTicks <= 0 || this.swoopTravelled >= this.swoopStrikeDistance + SWOOP_OVERSHOOT) {
            this.finishSwoop(crashed ? SWOOP_CRASH_RECOVERY_TICKS : SWOOP_RECOVERY_TICKS, crashed);
            return;
        }
        if (!this.swoopClipStarted && this.swoopStrikeDistance - this.swoopTravelled <= SWOOP_CLIP_LEAD_BLOCKS) {
            this.swoopClipStarted = true;
            this.swoopClipTicks = SWOOP_CLIP_TICKS;
            this.selectSpecialAnimation(this.isPhaseTwo() ? SPECIAL_FLY_ATTACK_P2 : SPECIAL_FLY_ATTACK);
        }
        double speed = pastStrike ? SWOOP_OVERSHOOT_SPEED : SWOOP_SPEED;
        Vec3 velocity = (pastStrike ? this.swoopOvershootDirection : this.swoopDirection).scale(speed);
        this.setDeltaMovement(velocity);
        this.needsSync = true;
        this.swoopTravelled += speed;
        this.applySwoopContact(velocity);
    }

    private void applySwoopContact(Vec3 velocity) {
        AABB sweep = this.getBoundingBox().expandTowards(velocity).inflate(0.4D);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * SWOOP_DAMAGE_MULTIPLIER;
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, sweep, this::canTargetEntity)) {
            if (!this.hasLineOfSight(victim) || !this.swoopHits.add(victim.getUUID())) {
                continue;
            }
            if (this.level() instanceof ServerLevel serverLevel && victim.hurtServer(serverLevel, this.nightmareDamageSource(), damage)) {
                this.playNightmareBiteSound();
                this.applyNightmareStrikeEffects(victim);
                this.knockAway(victim, 1.35D, 0.35D);
            }
        }
    }

    private boolean isSwoopLaneClear(Vec3 from, Vec3 to) {
        EntityDimensions dimensions = this.getType().getDimensions();
        double length = from.distanceTo(to);
        int samples = Math.max(1, Mth.ceil(length / 2.0D));
        for (int i = 1; i <= samples; i++) {
            Vec3 sample = from.lerp(to, i / (double) samples);
            AABB box = dimensions.makeBoundingBox(sample);
            if (!this.level().hasChunksAt(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))
                    || !this.level().noCollision(this, box)) {
                return false;
            }
        }
        return true;
    }

    private void finishSwoop(int recoveryTicks, boolean crashed) {
        boolean failed = this.swoopPhase == SWOOP_WINDUP;
        this.swoopPhase = SWOOP_NONE;
        this.swoopTicks = 0;
        this.swoopDirection = null;
        this.swoopOvershootDirection = null;
        this.swoopHits.clear();
        if (failed) {
            this.swoopCooldown = Math.max(this.swoopCooldown, recoveryTicks);
            return;
        }
        this.attackRecoveryTicks = recoveryTicks;
        this.attackCooldown = Math.max(this.attackCooldown, recoveryTicks + 10);
        this.swoopRepeatAllowed = this.random.nextInt(SWOOP_REPEAT_CHANCE_DIVISOR) == 0;
        int baseCooldown = 140 + this.random.nextInt(80);
        this.swoopCooldown = this.isPhaseTwo() ? baseCooldown * 3 / 4 : baseCooldown;
        this.setDeltaMovement(this.getDeltaMovement().scale(crashed ? 0.1D : 0.5D));
        if (crashed) {
            this.playSound(SoundEvents.GENERIC_SMALL_FALL, 1.5F, 0.6F);
        }
    }

    private void cancelSwoop() {
        this.swoopPhase = SWOOP_NONE;
        this.swoopTicks = 0;
        this.swoopClipTicks = 0;
        this.swoopDirection = null;
        this.swoopOvershootDirection = null;
        this.swoopHits.clear();
    }

    private void performRoarPulse() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(ROAR_PULSE_RADIUS);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, area, this::canTargetEntity)) {
            if (this.distanceToSqr(player) > ROAR_PULSE_RADIUS * ROAR_PULSE_RADIUS || !this.hasLineOfSight(player)) {
                continue;
            }
            player.addEffect(new MobEffectInstance(AntarchyObjects.DREAD.get(), DREAD_TICKS, 0));
            NightmareBiteEntity.spawnTelegraphAt(serverLevel, player, this.isPhaseTwo());
        }
        serverLevel.sendParticles(AntarchyObjects.NIGHTMARE_FIRE_FLAME.get(), this.getX(), this.getY() + 1.5D, this.getZ(), 36, ROAR_PULSE_RADIUS * 0.25D, 0.4D, ROAR_PULSE_RADIUS * 0.25D, 0.08D);
    }

    private void beginPhaseTransitionSequence() {
        this.attackAnimationTicks = 0;
        this.attackRecoveryTicks = 0;
        this.ambushAttack = false;
        this.pendingPhaseTicks = 0;
        this.cancelSwoop();
        this.portalAttackTicks = 0;
        this.portalTravelTicks = 0;
        this.attackHitApplied = false;
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
        this.pendingPhaseTicks = 0;
        this.flyingToTarget = false;
        this.selectSpecialAnimation(SPECIAL_PHASE_TWO);
        this.phaseTransitionTicks = PHASE_TWO_TRANSITION_TICKS;
        this.attackAnimationTicks = 0;
        this.portalAttackTicks = 0;
        this.portalTravelTicks = 0;
        this.attackHitApplied = false;
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
            this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
        }
        if (!this.onGround() && ++this.pendingPhaseTicks < PENDING_PHASE_TIMEOUT_TICKS) {
            Vec3 fall = this.getDeltaMovement();
            this.setDeltaMovement(fall.x * 0.35D, Math.min(fall.y, 0.0D) - 0.08D, fall.z * 0.35D);
            this.needsSync = true;
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
            this.needsSync = true;
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        if (target != null) {
            this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
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
        if (!this.isPhaseTwo() || this.isCombatLocked() || this.portalCooldown > 0 || this.portalSearchRetryTicks > 0 || this.isAttackBusy() || this.roarTicks > 0) {
            return false;
        }
        if (this.distanceToSqr(target) > PORTAL_RANGE * PORTAL_RANGE) {
            return false;
        }
        if (this.portalAttackTicks > 0) {
            return false;
        }
        boolean seen = this.unseenTargetTicks == 0;
        if (seen && this.horizontalDistanceToSqr(target) < PORTAL_MIN_RANGE_SQR) {
            return false;
        }
        if (!seen && this.unseenTargetTicks < PORTAL_UNSEEN_TICKS) {
            return false;
        }
        if (this.findPortalExitPos(target, this.getPortalEntryPosition()) == null) {
            this.portalSearchRetryTicks = PORTAL_SEARCH_RETRY_TICKS;
            return false;
        }
        return true;
    }

    private void startPortalAttack(LivingEntity target) {
        Vec3 entryPos = this.getPortalEntryPosition();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.clearNightmarePortals();
        this.portalExitPos = null;
        this.portalAttackTicks = PORTAL_TOTAL_TICKS;
        this.portalTravelTicks = 0;
        this.attackAnimationTicks = 0;
        this.attackHitApplied = false;
        this.attackCooldown = 24;
        this.portalCooldown = 180 + this.random.nextInt(100);
        this.setInvisible(false);
        this.clearActiveSpecialAnimation();
        this.getNavigation().stop();
        this.entryPortal = NightmarePortalEntity.spawnAt(serverLevel, entryPos, this, this.position(), PORTAL_LIFETIME_TICKS);
    }

    private void openPortalExit(@Nullable LivingEntity target) {
        Vec3 exitPos = target != null ? this.findPortalExitPos(target, this.getPortalEntryPosition()) : null;
        if (exitPos == null || !(this.level() instanceof ServerLevel serverLevel)) {
            this.cancelPortalAttack();
            return;
        }
        this.portalExitPos = exitPos;
        this.exitPortal = NightmarePortalEntity.spawnAt(serverLevel, exitPos, this, this.position(), PORTAL_LIFETIME_TICKS - PORTAL_EXIT_OPEN_TICK);
        serverLevel.playSound(null, exitPos.x, exitPos.y, exitPos.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 0.6F, 1.1F);
        if (this.entryPortal != null) {
            this.entryPortal.linkTo(this.exitPortal);
            this.exitPortal.linkTo(this.entryPortal);
        }
    }

    private void cancelPortalAttack() {
        this.portalAttackTicks = 0;
        this.portalTravelTicks = 0;
        this.portalExitPos = null;
        this.portalCooldown = Math.max(this.portalCooldown, 40);
        if (this.entryPortal != null) {
            this.entryPortal.closeNow();
        }
        if (this.exitPortal != null) {
            this.exitPortal.closeNow();
        }
        this.entryPortal = null;
        this.exitPortal = null;
        this.flyingToTarget = false;
        this.clearActiveSpecialAnimation();
        this.setInvisible(false);
    }

    private void tickPortalAttack(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.55D));
        if (target != null) {
            this.getLookControl().setLookAt(target, HEAD_YAW_DEGREES, HEAD_PITCH_DEGREES);
            this.faceTowardTarget(target, 12.0F);
        }
        int elapsed = PORTAL_TOTAL_TICKS - this.portalAttackTicks;
        if (elapsed == PORTAL_ENTER_TICK) {
            this.selectSpecialAnimation(SPECIAL_PORTAL_ENTER);
            this.flyingToTarget = false;
            this.setDeltaMovement(Vec3.ZERO);
            this.needsSync = true;
        }
        if (elapsed == PORTAL_EXIT_OPEN_TICK) {
            this.openPortalExit(target);
            if (this.portalAttackTicks <= 0) {
                return;
            }
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
            this.needsSync = true;
        }
        if (elapsed == PORTAL_TELEPORT_TICK && this.portalExitPos != null) {
            if (!this.level().noCollision(this, this.getType().getDimensions().makeBoundingBox(this.portalExitPos))) {
                this.cancelPortalAttack();
                return;
            }
            this.executePortalTeleport(target);
        }
        if (elapsed == PORTAL_REAPPEAR_TICK) {
            this.setInvisible(false);
            this.selectSpecialAnimation(SPECIAL_PORTAL_EXIT);
            if (target != null) {
                this.faceTowardTarget(target, 180.0F);
            }
        }
        if (--this.portalAttackTicks <= 0) {
            this.finishPortalAttack(target);
        }
    }

    private void finishPortalAttack(@Nullable LivingEntity target) {
        this.portalAttackTicks = 0;
        this.portalExitPos = null;
        this.entryPortal = null;
        this.exitPortal = null;
        this.setInvisible(false);
        this.clearActiveSpecialAnimation();
        boolean canAmbush = target != null
                && this.distanceToSqr(target) <= AMBUSH_MAX_RANGE_SQR
                && this.hasLineOfSight(target);
        this.flyingToTarget = target != null && !this.onGround() && this.forcedGroundTicks <= 0;
        if (this.flyingToTarget) {
            this.landingCooldown = 0;
            this.flightGoalTick = this.tickCount;
            this.takeoffCommitTicks = TAKEOFF_COMMIT_TICKS;
        }
        if (canAmbush) {
            this.startAttack(target, true);
        }
    }

    private void executePortalTeleport(@Nullable LivingEntity target) {
        if (this.portalExitPos == null) {
            return;
        }
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
        this.needsSync = true;
    }

    private boolean performAttackHit(@Nullable LivingEntity target) {
        Vec3 forward = this.getPlaneForward(this.getViewVector(1.0F));
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.toWorld(new Vec3(-Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), 0.0D, Mth.cos(this.getYRot() * Mth.DEG_TO_RAD)));
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.toWorld(new Vec3(0.0D, 0.0D, 1.0D));
        }
        forward = forward.normalize();
        Vec3 strikeOrigin = this.position()
                .add(this.toWorld(new Vec3(0.0D, this.getBbHeight() * 0.55D, 0.0D)))
                .add(forward.scale(0.4D));
        Vec3 strikeCenter = strikeOrigin
                .add(forward.scale(ATTACK_HIT_FORWARD_OFFSET));
        Vec3 strikeMin = this.position().add(this.toWorld(new Vec3(0.0D, ATTACK_HIT_MIN_Y_OFFSET, 0.0D)));
        Vec3 strikeMax = this.position().add(this.toWorld(new Vec3(0.0D, ATTACK_HIT_MAX_Y_OFFSET, 0.0D)));
        AABB hitBox = new AABB(
                Math.min(strikeOrigin.x, strikeCenter.x) - ATTACK_HIT_HALF_WIDTH,
                Math.min(strikeMin.y, strikeMax.y),
                Math.min(strikeOrigin.z, strikeCenter.z) - ATTACK_HIT_HALF_WIDTH,
                Math.max(strikeOrigin.x, strikeCenter.x) + ATTACK_HIT_HALF_WIDTH,
                Math.max(strikeMin.y, strikeMax.y),
                Math.max(strikeOrigin.z, strikeCenter.z) + ATTACK_HIT_HALF_WIDTH
        );
        List<LivingEntity> victims = new ArrayList<>(this.level().getEntitiesOfClass(LivingEntity.class, hitBox, this::canTargetEntity));
        if (target != null && this.canTargetEntity(target) && !victims.contains(target) && this.canConnectAttackTo(target, forward, hitBox)) {
            victims.add(0, target);
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * (this.ambushAttack ? AMBUSH_DAMAGE_MULTIPLIER : 1.0F);
        double knockback = this.ambushAttack ? 1.35D : 1.15D;
        boolean connected = false;
        for (LivingEntity victim : victims) {
            if (!this.canConnectAttackTo(victim, forward, hitBox)) {
                continue;
            }
            if (this.level() instanceof ServerLevel serverLevel && victim.hurtServer(serverLevel, this.nightmareDamageSource(), damage)) {
                connected = true;
                this.applyNightmareStrikeEffects(victim);
                this.knockAway(victim, knockback, 0.3D);
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(AntarchyObjects.NIGHTMARE_FIRE_FLAME.get(), this.getX(), this.getY() + 1.6D, this.getZ(), 12, 0.9D, 0.35D, 0.9D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 1.6D, this.getZ(), 8, 0.7D, 0.25D, 0.7D, 0.03D);
        }
        return connected;
    }

    private boolean canConnectAttackTo(LivingEntity victim, Vec3 forward, AABB hitBox) {
        if (!this.hasLineOfSight(victim)) {
            return false;
        }
        Vec3 victimCenter = victim.getBoundingBox().getCenter();
        if (!this.isInFront(victimCenter, ATTACK_ARC_DOT)) {
            return false;
        }
        if (victim.getBoundingBox().inflate(0.1D).intersects(hitBox)) {
            return true;
        }
        Vec3 localForward = this.toLocalPlane(forward);
        Vec3 toVictim = this.toLocalPlane(victimCenter.subtract(this.position()));
        double forwardDistance = toVictim.dot(localForward.normalize());
        if (forwardDistance < 0.15D || forwardDistance > ATTACK_HIT_FORWARD_OFFSET + 1.25D) {
            return false;
        }
        Vec3 lateral = toVictim.subtract(localForward.normalize().scale(forwardDistance));
        if (lateral.lengthSqr() > (ATTACK_HIT_HALF_WIDTH + 0.55D) * (ATTACK_HIT_HALF_WIDTH + 0.55D)) {
            return false;
        }
        Vec3 minOffset = this.toWorld(new Vec3(0.0D, ATTACK_HIT_MIN_Y_OFFSET, 0.0D));
        Vec3 maxOffset = this.toWorld(new Vec3(0.0D, ATTACK_HIT_MAX_Y_OFFSET + 0.4D, 0.0D));
        double minY = Math.min(this.position().y + minOffset.y, this.position().y + maxOffset.y);
        double maxY = Math.max(this.position().y + minOffset.y, this.position().y + maxOffset.y);
        return victim.getBoundingBox().maxY >= minY && victim.getBoundingBox().minY <= maxY;
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
        GravityDirection direction = this.gravityDirection();
        Vec3 localDirection = this.toLocalPlane(target.position().subtract(this.position()));
        if (localDirection.lengthSqr() < 1.0E-4D) localDirection = this.toLocalPlane(this.getViewVector(1.0F));
        if (localDirection.lengthSqr() < 1.0E-4D) localDirection = new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 localPush = localDirection.normalize().scale(horizontalStrength);
        Vec3 worldPush = GravityRotationUtil.vecPlayerToWorld(localPush.x, verticalStrength, localPush.z, direction);
        target.push(worldPush.x, worldPush.y, worldPush.z);
        target.syncVelocity = true;
    }

    private GravityDirection gravityDirection() {
        return GravityApi.getGravityDirection(this);
    }

    private Vec3 toLocal(Vec3 worldVector) {
        return GravityRotationUtil.vecWorldToPlayer(worldVector, this.gravityDirection());
    }

    private Vec3 toWorld(Vec3 localVector) {
        return GravityRotationUtil.vecPlayerToWorld(localVector, this.gravityDirection());
    }

    private Vec3 toLocalPlane(Vec3 worldVector) {
        Vec3 local = this.toLocal(worldVector);
        return new Vec3(local.x, 0.0D, local.z);
    }

    private Vec3 localDeltaTo(Vec3 worldPosition) {
        return this.toLocal(worldPosition.subtract(this.position()));
    }

    private double verticalOffsetTo(LivingEntity target) {
        return this.localDeltaTo(target.position()).y;
    }

    private double verticalDistanceTo(LivingEntity target) {
        return Math.abs(this.verticalOffsetTo(target));
    }

    private double overheadGapTo(LivingEntity target) {
        return -this.localDeltaTo(target.position()).y;
    }

    private Vec3 getPlaneForward(Vec3 worldVector) {
        Vec3 local = this.toLocalPlane(worldVector);
        if (local.lengthSqr() < 1.0E-4D) {
            return Vec3.ZERO;
        }
        return this.toWorld(local.normalize());
    }

    private Vec3 getLocalRight(Vec3 worldForward) {
        Vec3 localForward = this.toLocalPlane(worldForward);
        if (localForward.lengthSqr() < 1.0E-4D) {
            return Vec3.ZERO;
        }
        Vec3 normalized = localForward.normalize();
        return this.toWorld(new Vec3(-normalized.z, 0.0D, normalized.x));
    }

    private Vec3 getFlyingAttackTargetPos(LivingEntity target) {
        double targetHeight = Mth.clamp(target.getBbHeight() * 0.4D, FLYING_ATTACK_MIN_TARGET_HEIGHT, FLYING_ATTACK_BODY_TARGET_HEIGHT);
        return target.position().add(this.toWorld(new Vec3(0.0D, targetHeight, 0.0D)));
    }

    private DamageSource nightmareDamageSource() {
        return this.level() instanceof ServerLevel serverLevel
                ? AntarchyDamageSources.nightmareMauling(serverLevel, this)
                : this.damageSources().mobAttack(this);
    }

    private void tryBreakBlocksToTarget() {
        if (this.blockBreakCooldown > 0 || !(this.level() instanceof ServerLevel griefLevel) || !griefLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
        }
        Direction gravityDown = GravityRotationUtil.getGravityDownDirection(this);
        Direction sideA = gravityDown.getAxis() == Direction.Axis.X ? Direction.NORTH : Direction.EAST;
        Direction sideB = gravityDown.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.UP;
        BlockPos center = this.blockPosition().relative(gravityDown);
        int broken = 0;
        for (int a = -1; a <= 1 && broken < 3; a++) {
            for (int b = -1; b <= 1 && broken < 3; b++) {
                BlockPos pos = center.relative(sideA, a).relative(sideB, b);
                if (this.level().getBlockState(pos).is(AntarchyTags.Blocks.NIGHTMARE_BREAKABLE)
                        && this.level().getBlockEntity(pos) == null
                        && this.level().destroyBlock(pos, true, this)) {
                    broken++;
                }
            }
        }
        if (broken > 0) {
            this.blockBreakCooldown = BLOCK_BREAK_TICKS;
        }
    }

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType() || entity.level() != this.level()) {
            return false;
        }
        if (entity.is(AntarchyTags.Entities.NIGHTMARE_NO_ATTACK)) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }
        return entity instanceof Mob && entity.isAttackable();
    }

    private boolean canStartAttackOn(LivingEntity target) {
        if (this.distanceToSqr(target) > ATTACK_START_RANGE_SQR) {
            return false;
        }
        if (this.horizontalDistanceToSqr(target) > ATTACK_COMMIT_HORIZONTAL_RANGE_SQR) {
            return false;
        }
        if (!this.isInFront(target.position(), ATTACK_START_ARC_DOT)) {
            return false;
        }
        double verticalOffset = this.verticalOffsetTo(target);
        if (verticalOffset > ATTACK_MAX_OFFSET_ABOVE) {
            return false;
        }
        if (this.shouldUseFlyingLocomotion()) {
            return this.overheadGapTo(target) <= FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT && this.hasLineOfSight(target);
        }
        return verticalOffset >= -GROUND_ATTACK_MAX_OFFSET_BELOW && this.hasLineOfSight(target);
    }

    private boolean shouldUseFlightPressure(LivingEntity target) {
        if (target == null || this.forcedGroundTicks > 0 || this.isTooCloseToCeiling()) {
            return false;
        }
        boolean lowProfileTarget = this.isLowProfileFlightTarget(target);
        if (this.flyingToTarget && this.takeoffCommitTicks > 0) {
            return true;
        }
        double distanceSqr = this.distanceToSqr(target);
        double verticalDistance = this.verticalDistanceTo(target);
        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double overheadGap = this.overheadGapTo(target);
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
        if (distanceSqr >= FLIGHT_ENGAGE_RANGE_SQR || this.verticalOffsetTo(target) > ATTACK_MAX_OFFSET_ABOVE) {
            return true;
        }
        return this.isPhaseTwo() && (distanceSqr >= FLIGHT_DISENGAGE_RANGE_SQR || verticalDistance > 1.5D);
    }

    private Vec3 getFlightApproachPosition(LivingEntity target) {
        Vec3 forward = this.getPlaneForward(target.position().subtract(this.position()));
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.getPlaneForward(target.getLookAngle());
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.getPlaneForward(this.getViewVector(1.0F));
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.toWorld(new Vec3(0.0D, 0.0D, 1.0D));
        }
        Vec3 right = this.getLocalRight(forward);
        double strafe = this.approachSide * FLYING_APPROACH_STRAFE_OFFSET;
        double height = this.isPhaseTwo() ? FLYING_APPROACH_HEIGHT_PHASE_TWO : FLYING_APPROACH_HEIGHT;
        double horizontalDistanceSqr = this.horizontalDistanceToSqr(target);
        double overheadGap = this.overheadGapTo(target);
        boolean lowProfileTarget = this.isLowProfileFlightTarget(target);
        Vec3 attackTarget = this.getFlyingAttackTargetPos(target);
        Vec3 anchor = target.position().subtract(forward.scale(1.8D)).add(right.scale(strafe));
        if (horizontalDistanceSqr <= FLYING_OVERHEAD_STALL_RANGE_SQR && overheadGap >= FLYING_OVERHEAD_STALL_HEIGHT) {
            double sideOffset = lowProfileTarget ? LOW_PROFILE_OVERHEAD_DESCENT_SIDE_OFFSET : FLYING_OVERHEAD_DESCENT_SIDE_OFFSET;
            double backOffset = lowProfileTarget ? LOW_PROFILE_OVERHEAD_DESCENT_BACK_OFFSET : FLYING_OVERHEAD_DESCENT_BACK_OFFSET;
            anchor = target.position()
                    .add(right.scale(strafe > 0.0D ? sideOffset : -sideOffset))
                    .subtract(forward.scale(backOffset));
            return new Vec3(anchor.x, attackTarget.y, anchor.z);
        }
        if (horizontalDistanceSqr <= FLYING_APPROACH_TOO_CLOSE_RANGE_SQR) {
            double backOffset = lowProfileTarget ? LOW_PROFILE_TOO_CLOSE_BACK_OFFSET : 2.8D;
            double strafeScale = lowProfileTarget ? LOW_PROFILE_TOO_CLOSE_STRAFE_SCALE : 0.6D;
            anchor = target.position().subtract(forward.scale(backOffset)).add(right.scale(strafe * strafeScale));
        }
        Vec3 elevated = attackTarget.add(this.toWorld(new Vec3(0.0D, height, 0.0D)));
        return new Vec3(anchor.x, elevated.y, anchor.z);
    }

    private Vec3 getFlightDescentPosition(LivingEntity target) {
        Vec3 forward = this.getPlaneForward(target.position().subtract(this.position()));
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.getPlaneForward(this.getViewVector(1.0F));
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.toWorld(new Vec3(0.0D, 0.0D, 1.0D));
        }
        Vec3 right = this.getLocalRight(forward);
        double side = this.approachSide;
        boolean lowProfileTarget = this.isLowProfileFlightTarget(target);
        double sideOffset = lowProfileTarget ? 1.15D : 0.9D;
        double backOffset = lowProfileTarget ? 1.2D : 0.9D;
        Vec3 anchor = target.position()
                .subtract(forward.scale(backOffset))
                .add(right.scale(side * sideOffset));
        return new Vec3(anchor.x, this.getFlyingAttackTargetPos(target).y, anchor.z);
    }

    private boolean isLowProfileFlightTarget(LivingEntity target) {
        return target.getBbHeight() <= LOW_PROFILE_TARGET_MAX_HEIGHT;
    }

    private double horizontalDistanceToSqr(LivingEntity target) {
        Vec3 delta = this.localDeltaTo(target.position());
        double dx = delta.x;
        double dz = delta.z;
        return dx * dx + dz * dz;
    }

    private boolean isInFront(Vec3 position, double minimumDot) {
        Vec3 forward = this.toLocalPlane(this.getViewVector(1.0F));
        Vec3 toTarget = this.toLocalPlane(position.subtract(this.position()));
        if (forward.lengthSqr() < 1.0E-4D || toTarget.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return forward.normalize().dot(toTarget.normalize()) >= minimumDot;
    }

    private void faceTowardTarget(LivingEntity target, float maxTurnDegrees) {
        Vec3 toTarget = this.toLocal(target.getEyePosition().subtract(this.getEyePosition()));
        double horizontal = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontal > 1.0E-4D) {
            float targetYaw = (float) (Mth.atan2(toTarget.z, toTarget.x) * (180.0D / Math.PI)) - 90.0F;
            float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, maxTurnDegrees);
            this.setYRot(nextYaw);
            this.yBodyRot = nextYaw;
        }
    }

    private boolean isTooCloseToCeiling() {
        if (this.ceilingCheckTick != this.tickCount) {
            this.ceilingCheckTick = this.tickCount;
            AABB headroom = this.getBoundingBox().expandTowards(this.toWorld(new Vec3(0.0D, 1.0D, 0.0D)));
            this.ceilingCheckResult = !this.level().noCollision(this, headroom);
        }
        return this.ceilingCheckResult;
    }

    private Vec3 getPortalEntryPosition() {
        Vec3 forward = this.getPlaneForward(this.getViewVector(1.0F));
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.toWorld(new Vec3(0.0D, 0.0D, 1.0D));
        }
        return this.position().add(forward.scale(1.6D));
    }

    @Nullable
    private Vec3 findPortalExitPos(LivingEntity target, Vec3 entryPos) {
        Vec3 forward = this.getPlaneForward(target.getLookAngle());
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.getPlaneForward(this.getViewVector(1.0F));
        }
        if (forward.lengthSqr() < 1.0E-4D) {
            forward = this.toWorld(new Vec3(0.0D, 0.0D, 1.0D));
        }
        forward = forward.normalize();
        Vec3 right = this.getLocalRight(forward);
        Vec3 airOffset = this.toWorld(new Vec3(0.0D, PORTAL_EXIT_AIR_HEIGHT, 0.0D));
        Vec3[] candidates = new Vec3[] {
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).add(airOffset),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).add(right.scale(PORTAL_EXIT_SIDE_OFFSET)),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).subtract(right.scale(PORTAL_EXIT_SIDE_OFFSET)),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).add(right.scale(PORTAL_EXIT_SIDE_OFFSET)).add(airOffset),
                target.position().subtract(forward.scale(PORTAL_EXIT_OFFSET)).subtract(right.scale(PORTAL_EXIT_SIDE_OFFSET)).add(airOffset)
        };
        for (Vec3 candidate : candidates) {
            Vec3 adjusted = this.findValidPortalPosition(candidate);
            if (adjusted != null
                    && adjusted.distanceToSqr(this.position()) <= PORTAL_RANGE * PORTAL_RANGE
                    && adjusted.distanceToSqr(entryPos) >= PORTAL_ENTRY_EXIT_MIN_RANGE_SQR
                    && this.hasClearSightFrom(adjusted, target)) {
                return adjusted;
            }
        }
        return null;
    }

    @Nullable
    private Vec3 findValidPortalPosition(Vec3 base) {
        EntityDimensions dimensions = this.getType().getDimensions();
        for (int yOffset : PORTAL_EXIT_HEIGHT_ORDER) {
            Vec3 candidate = base.add(this.toWorld(new Vec3(0.0D, yOffset, 0.0D)));
            AABB box = dimensions.makeBoundingBox(candidate);
            if (this.level().noCollision(this, box)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean hasClearSightFrom(Vec3 position, LivingEntity target) {
        Vec3 eye = position.add(this.toWorld(new Vec3(0.0D, this.getEyeHeight(), 0.0D)));
        return this.level().clip(new ClipContext(eye, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
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
        int special = this.getActiveSpecialAnimation();
        if ((this.isRoaring() || (special != SPECIAL_NONE && special != SPECIAL_DEATH) || this.isPhaseTransitioning()) && this.tickCount % 3 == 0) {
            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + 1.2D + this.random.nextDouble() * 1.1D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    0.0D, 0.03D, 0.0D
            );
        }
    }

    private void updateFacing(@Nullable LivingEntity target) {
        if (this.isCombatLocked() || this.portalAttackTicks > 0 || this.attackAnimationTicks > 0 || this.isRoaring() || this.swoopPhase != SWOOP_NONE) {
            return;
        }
        boolean flying = this.isNoGravity();
        Vec3 direction = null;
        if (target != null && (flying || (this.getNavigation().isDone() && this.horizontalDistanceToSqr(target) <= GROUND_FACE_RANGE_SQR))) {
            direction = this.localDeltaTo(target.position());
        } else if (flying) {
            direction = this.toLocal(this.getDeltaMovement());
        }
        if (direction == null || direction.horizontalDistanceSqr() < 1.0E-4D) {
            return;
        }
        float targetYaw = (float) (Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
        float nextYaw = Mth.approachDegrees(this.getYRot(), targetYaw, flying ? FLIGHT_TURN_DEGREES : GROUND_FACE_TURN_DEGREES);
        this.setYRot(nextYaw);
        if (flying) {
            this.yBodyRot = nextYaw;
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
        Vec3 horizontalVelocity = this.toLocalPlane(this.getDeltaMovement());
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
        this.wingFlapCooldown = WING_BEAT_TICKS;
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
        return this.flyingToTarget || this.isNoGravity() || this.airborneTicks >= MIN_AIRBORNE_TICKS_FOR_FLY_ANIM;
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
            return NightmareEntity.this.isCombatLocked();
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.isCombatLocked();
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
            return target instanceof Player && NightmareEntity.this.shouldStartRoar(target) && !NightmareEntity.this.isCombatLocked() && !NightmareEntity.this.isAttackBusy() && NightmareEntity.this.portalAttackTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return NightmareEntity.this.isRoaring();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
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
                    && !NightmareEntity.this.isCombatLocked()
                    && !NightmareEntity.this.isAttackBusy()
                    && !NightmareEntity.this.isRoaring()
                    && NightmareEntity.this.portalAttackTicks <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            if (NightmareEntity.this.isAttackBusy() && !NightmareEntity.this.isCombatLocked()) {
                return true;
            }
            LivingEntity target = NightmareEntity.this.getTarget();
            return target != null
                    && NightmareEntity.this.shouldUseFlightPressure(target)
                    && !NightmareEntity.this.isCombatLocked()
                    && !NightmareEntity.this.isRoaring()
                    && NightmareEntity.this.portalAttackTicks <= 0;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            NightmareEntity.this.flyingToTarget = true;
            NightmareEntity.this.takeoffCommitTicks = TAKEOFF_COMMIT_TICKS;
            NightmareEntity.this.flightGoalTick = NightmareEntity.this.tickCount;
            NightmareEntity.this.flightStuckTicks = 0;
            NightmareEntity.this.flightBestDistance = Double.MAX_VALUE;
            NightmareEntity.this.approachSide = NightmareEntity.this.random.nextBoolean() ? 1 : -1;
        }

        @Override
        public void stop() {
            NightmareEntity.this.flyingToTarget = false;
            NightmareEntity.this.landingCooldown = 18;
            NightmareEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            NightmareEntity nightmare = NightmareEntity.this;
            nightmare.flightGoalTick = nightmare.tickCount;
            LivingEntity target = nightmare.getTarget();
            if (target == null || nightmare.isAttackBusy()) {
                return;
            }
            if (nightmare.isTooCloseToCeiling()) {
                nightmare.flyingToTarget = false;
                nightmare.getNavigation().stop();
                return;
            }
            if (nightmare.onGround() && nightmare.distanceToSqr(target) >= FLIGHT_DISENGAGE_RANGE_SQR) {
                nightmare.setDeltaMovement(nightmare.getDeltaMovement().add(nightmare.toWorld(new Vec3(0.0D, 0.38D, 0.0D))));
                nightmare.needsSync = true;
            }
            double horizontalDistanceSqr = nightmare.horizontalDistanceToSqr(target);
            double overheadGap = nightmare.overheadGapTo(target);
            if (this.detectStuck(target, horizontalDistanceSqr, overheadGap)) {
                return;
            }
            if (nightmare.canStartSwoop(target)) {
                nightmare.startSwoop();
                return;
            }
            if (horizontalDistanceSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE_SQR
                    && overheadGap > FLYING_ATTACK_MAX_OVERHEAD_COMMIT_HEIGHT) {
                Vec3 descentPos = nightmare.getFlightDescentPosition(target);
                nightmare.getMoveControl().setWantedPosition(descentPos.x, descentPos.y, descentPos.z, COMBAT_FLIGHT_SPEED * 1.08D);
                return;
            }
            if (nightmare.attackCooldown <= 0 && nightmare.canStartAttackOn(target)) {
                nightmare.startAttack(target);
                return;
            }
            if (nightmare.unseenTargetTicks > LAST_SEEN_PURSUIT_TICKS
                    && nightmare.lastSeenTargetPos != null
                    && nightmare.distanceToSqr(nightmare.lastSeenTargetPos) > 16.0D) {
                Vec3 lastSeen = nightmare.lastSeenTargetPos;
                nightmare.getMoveControl().setWantedPosition(lastSeen.x, lastSeen.y + 1.0D, lastSeen.z, COMBAT_FLIGHT_SPEED);
                return;
            }
            Vec3 approachPos = nightmare.getFlightApproachPosition(target);
            nightmare.getMoveControl().setWantedPosition(approachPos.x, approachPos.y, approachPos.z, COMBAT_FLIGHT_SPEED);
        }

        private boolean detectStuck(LivingEntity target, double horizontalDistanceSqr, double overheadGap) {
            NightmareEntity nightmare = NightmareEntity.this;
            double distance = Math.sqrt(horizontalDistanceSqr) + Math.abs(overheadGap);
            if (distance < nightmare.flightBestDistance - 0.5D) {
                nightmare.flightBestDistance = distance;
                nightmare.flightStuckTicks = 0;
                return false;
            }
            if (nightmare.horizontalCollision || nightmare.verticalCollision || horizontalDistanceSqr <= ATTACK_COMMIT_HORIZONTAL_RANGE_SQR) {
                nightmare.flightStuckTicks++;
            }
            if (nightmare.flightStuckTicks < FLIGHT_STUCK_TICKS) {
                return false;
            }
            nightmare.flightStuckTicks = 0;
            nightmare.flightBestDistance = Double.MAX_VALUE;
            nightmare.approachSide = -nightmare.approachSide;
            nightmare.forcedGroundTicks = FORCED_GROUND_TICKS;
            nightmare.flyingToTarget = false;
            nightmare.portalCooldown = Math.min(nightmare.portalCooldown, 20);
            nightmare.portalSearchRetryTicks = 0;
            nightmare.roarCooldown = Math.min(nightmare.roarCooldown, 40);
            return true;
        }
    }

    private final class NightmareMeleeGoal extends MeleeAttackGoal {
        NightmareMeleeGoal() {
            super(NightmareEntity.this, GROUND_APPROACH_SPEED, true);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return !NightmareEntity.this.isCombatLocked()
                    && NightmareEntity.this.portalAttackTicks <= 0
                    && target != null
                    && target.isAlive()
                    && (NightmareEntity.this.landingCooldown > 0 || !NightmareEntity.this.shouldUseFlightPressure(target));
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = NightmareEntity.this.getTarget();
            return !NightmareEntity.this.isCombatLocked()
                    && NightmareEntity.this.portalAttackTicks <= 0
                    && target != null
                    && (NightmareEntity.this.landingCooldown > 0 || !NightmareEntity.this.shouldUseFlightPressure(target))
                    && super.canContinueToUse();
        }

        @Override
        public void tick() {
            NightmareEntity nightmare = NightmareEntity.this;
            if (nightmare.isAttackBusy()) {
                nightmare.getNavigation().stop();
                return;
            }
            LivingEntity target = nightmare.getTarget();
            if (target == null) {
                return;
            }
            double hdSqr = nightmare.horizontalDistanceToSqr(target);
            if (target.onGround() && hdSqr <= 25.0D && nightmare.overheadGapTo(target) > 1.6D) {
                nightmare.tryBreakBlocksToTarget();
            }
            super.tick();
            if (nightmare.getNavigation().isDone() && (hdSqr > ATTACK_COMMIT_HORIZONTAL_RANGE_SQR || nightmare.unreachablePressure)) {
                nightmare.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), GROUND_APPROACH_SPEED);
            }
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
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (NightmareEntity.this.getTarget() != null || NightmareEntity.this.isDeadOrDying() || NightmareEntity.this.isCombatLocked() || NightmareEntity.this.portalAttackTicks > 0) {
                return false;
            }
            if (NightmareEntity.this.random.nextInt(40) != 0) {
                return false;
            }
            double angle = NightmareEntity.this.random.nextDouble() * Math.PI * 2;
            double dist = 4 + NightmareEntity.this.random.nextDouble() * 10;
            Vec3 localOffset = new Vec3(Math.cos(angle) * dist, 0.0D, Math.sin(angle) * dist);
            Vec3 worldOffset = NightmareEntity.this.toWorld(localOffset);
            this.x = NightmareEntity.this.getX() + worldOffset.x;
            this.y = NightmareEntity.this.getY() + worldOffset.y;
            this.z = NightmareEntity.this.getZ() + worldOffset.z;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.wanderTicks > 0
                    && NightmareEntity.this.getTarget() == null
                    && !NightmareEntity.this.isDeadOrDying()
                    && !NightmareEntity.this.getNavigation().isDone()
                    && NightmareEntity.this.distanceToSqr(this.x, this.y, this.z) > 2.25D;
        }

        @Override
        public void start() {
            this.wanderTicks = 160 + NightmareEntity.this.random.nextInt(80);
            NightmareEntity.this.getNavigation().moveTo(this.x, this.y, this.z, PATROL_SPEED);
        }

        @Override
        public void stop() {
            NightmareEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.wanderTicks--;
        }
    }

    private final class NightmareHurtByTargetGoal extends HurtByTargetGoal {
        NightmareHurtByTargetGoal() {
            super(NightmareEntity.this);
        }

        @Override
        public boolean canUse() {
            LivingEntity current = NightmareEntity.this.getTarget();
            if (current != null && NightmareEntity.this.canTargetEntity(current)
                    && (NightmareEntity.this.targetHoldTicks > 0 || NightmareEntity.this.isAttackBusy())) {
                return false;
            }
            return super.canUse();
        }
    }

    private final class NightmareMoveControl extends MoveControl {
        NightmareMoveControl() {
            super(NightmareEntity.this);
        }

        @Override
        public void tick() {
            NightmareEntity nightmare = NightmareEntity.this;
            if (nightmare.isNoGravity()) {
                this.tickFlight(nightmare);
            } else {
                this.tickGround(nightmare);
            }
        }

        private void tickFlight(NightmareEntity nightmare) {
            nightmare.setXxa(0.0F);
            nightmare.setYya(0.0F);
            nightmare.setZza(0.0F);
            if (this.operation != Operation.MOVE_TO) {
                return;
            }
            this.operation = Operation.WAIT;
            Vec3 toWanted = new Vec3(this.wantedX - nightmare.getX(), this.wantedY - nightmare.getY(), this.wantedZ - nightmare.getZ());
            double distance = toWanted.length();
            if (distance < 1.0E-3D || this.speedModifier <= 0.0D) {
                return;
            }
            double thrust = this.speedModifier * nightmare.getAttributeValue(Attributes.FLYING_SPEED);
            double acceleration = thrust * thrust;
            double topSpeed = acceleration / 0.09D * 1.1D;
            Vec3 desired = toWanted.scale(Math.min(topSpeed, distance * FLIGHT_ARRIVAL_SPEED_SCALE) / distance);
            Vec3 steering = desired.subtract(nightmare.getDeltaMovement());
            double maxSteering = acceleration * FLIGHT_ACCEL_SCALE;
            if (steering.lengthSqr() > maxSteering * maxSteering) {
                steering = steering.normalize().scale(maxSteering);
            }
            nightmare.setDeltaMovement(nightmare.getDeltaMovement().add(steering));
        }

        private void tickGround(NightmareEntity nightmare) {
            if (this.operation != Operation.MOVE_TO) {
                nightmare.setZza(0.0F);
                return;
            }
            this.operation = Operation.WAIT;
            Vec3 local = nightmare.localDeltaTo(new Vec3(this.wantedX, this.wantedY, this.wantedZ));
            double horizontalSqr = local.x * local.x + local.z * local.z;
            if (horizontalSqr < 0.0625D) {
                nightmare.setZza(0.0F);
                return;
            }
            float targetYaw = (float) (Mth.atan2(local.z, local.x) * (180.0D / Math.PI)) - 90.0F;
            nightmare.setYRot(this.rotlerp(nightmare.getYRot(), targetYaw, GROUND_TURN_DEGREES));
            nightmare.setSpeed((float) (this.speedModifier * nightmare.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            double reach = Math.max(1.0D, nightmare.getBbWidth());
            if (nightmare.onGround() && local.y > nightmare.maxUpStep() && horizontalSqr < reach * reach) {
                nightmare.getJumpControl().jump();
            }
        }
    }
}
