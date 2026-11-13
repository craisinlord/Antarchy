package com.craisinlord.antarchy.content.entity.kraken;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.damage.AntarchyDamageTypes;
import com.craisinlord.antarchy.content.entity.MissileSquidEntity;
import com.craisinlord.antarchy.content.entity.OctopusBombEntity;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEcho;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.anthology.multipart.MultipartEntityOwner;
import com.craisinlord.anthology.multipart.MultipartLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
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

public class KrakenEntity extends Monster implements GeoEntity, MultipartEntityOwner, GlimmerEcho {
    private static final EntityDataAccessor<Boolean> GLIMMER_ECHO = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_STATE = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ROARING = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PHASE_TWO = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(KrakenEntity.class, EntityDataSerializers.INT);

    private static final String ATTACK_COOLDOWN_KEY = "AttackCooldown";
    private static final String PHASE_TRANSITION_TICKS_KEY = "PhaseTransitionTicks";
    private static final String PHASE_SUMMON_COOLDOWN_KEY = "PhaseSummonCooldown";
    private static final String LAST_ATTACK_STATE_KEY = "LastAttackState";
    private static final String AGGRO_TRIGGERED_KEY = "AggroTriggered";
    private static final String WATER_ANCHOR_KEY = "WaterAnchor";

    private static final int ATTACK_NONE = 0;
    private static final int ATTACK_GRAB = 1;
    private static final int ATTACK_SWING_LEFT = 2;
    private static final int ATTACK_SWING_RIGHT = 3;
    private static final int ATTACK_SLAM = 4;
    private static final int ATTACK_BREACH = 5;

    private static final int ANIM_NONE = 0;
    private static final int ANIM_CRY = 1;
    private static final int ANIM_GRAB = 2;
    private static final int ANIM_SWING_LEFT = 3;
    private static final int ANIM_SWING_RIGHT = 4;
    private static final int ANIM_SLAM = 5;
    private static final int ANIM_DEATH = 6;

    private static final int DEATH_ANIM_TICKS = 60;

    private static final int BASE_XP_REWARD = 60;
    private static final int GLIMMER_XP_REWARD = BASE_XP_REWARD * 5;
    private static final int MINION_CAP = 12;
    private static final double MINION_COUNT_RANGE = 64.0D;
    private static final int ATTACK_SELECTION_RETRY_TICKS = 5;
    private static final int GLIMMER_MINION_CAP_BONUS = 4;
    private static final int GLIMMER_EXTRA_MINIONS = 1;
    private static final int GLIMMER_RING_DELAY_TICKS = 15;
    private static final double GLIMMER_RING_RADIUS = 20.0D;
    private static final float GLIMMER_RING_DAMAGE_FRACTION = 0.5F;
    private static final double GLIMMER_STRIKE_HEIGHT = 20.0D;
    private static final int PATROL_SEARCH_RETRY_TICKS = 10;

    private static final int ROAR_TICKS = 60;
    private static final int PHASE_TRANSITION_TOTAL_TICKS = 60;
    private static final int PHASE_MINION_TICK = 40;
    private static final int COMBAT_GRACE_TICKS = 200;
    private static final int TARGET_SWITCH_HOLD_TICKS = 100;

    private static final double STRIKE_BAND_BELOW = 4.0D;
    private static final double STRIKE_BAND_ABOVE = 30.0D;
    private static final double GRAB_REACH = 9.0D;
    private static final double SWING_REACH = 11.0D;
    private static final double SLAM_RADIUS = 16.0D;
    private static final double SLAM_BAND_ABOVE = 22.0D;
    private static final double SWING_ACROSS_MIDLINE = -0.1D;
    private static final double SWING_REAR_LIMIT = -0.35D;

    private static final int SWING_TOTAL_TICKS = 40;
    private static final int SWING_LOCK_TICK = 35;
    private static final int SWING_STRIKE_TICK = 24;
    private static final int SLAM_TOTAL_TICKS = 40;
    private static final int SLAM_LOCK_TICK = 28;
    private static final int SLAM_STRIKE_TICK = 20;

    private static final int GRAB_TOTAL_TICKS = 64;
    private static final int GRAB_CLIP_DELAY = 4;
    private static final int GRAB_LOCK_ELAPSED = 8;
    private static final int GRAB_CAPTURE_ELAPSED = 12;
    private static final int GRAB_RELEASE_ELAPSED = 43;
    private static final double GRAB_CAPTURE_RADIUS = 3.0D;
    private static final double GRAB_CAPTURE_HEIGHT = 4.0D;
    private static final double GRAB_MAX_PULL_SPEED = 1.2D;
    private static final double GRAB_CORRECTION_DISTANCE_SQR = 64.0D;
    private static final float GRAB_BREAK_HEALTH_FRACTION = 0.04F;
    private static final int GRAB_COOLDOWN_TICKS = 100;
    private static final int GRAB_WHIFF_EXTRA_COOLDOWN = 20;

    private static final int BREACH_TOTAL_TICKS = 40;
    private static final int BREACH_COMMIT_ELAPSED = 14;
    private static final int BREACH_ACTIVE_TICKS = 12;
    private static final double BREACH_MAX_SPEED = 2.2D;
    private static final double BREACH_OVERSHOOT = 6.0D;
    private static final double BREACH_MIN_EDGE = 14.0D;
    private static final double BREACH_MAX_EDGE = 30.0D;
    private static final int BREACH_KITE_TICKS = 60;
    private static final int BREACH_COOLDOWN_TICKS = 300;
    private static final int BREACH_BLOCKED_RETRY_TICKS = 40;
    private static final int BREACH_RECOVERY_COOLDOWN = 30;
    private static final int BREACH_TERRAIN_EXTRA_COOLDOWN = 40;

    private static final float ORBIT_RATE_PHASE_ONE = 0.018F;
    private static final float ORBIT_RATE_PHASE_TWO = 0.026F;
    private static final float ORBIT_RATE_EASE = 0.0012F;
    private static final double ORBIT_RADIUS_PHASE_ONE = 14.0D;
    private static final double ORBIT_RADIUS_PHASE_TWO = 11.0D;
    private static final double ORBIT_WATER_BIAS = 0.06D;
    private static final int DESTINATION_REFRESH_TICKS = 6;
    private static final float COMBAT_TURN_RATE = 6.0F;
    private static final float PATROL_TURN_RATE = 3.0F;
    private static final float MAX_BODY_PITCH = 15.0F;

    private static final double WATERLINE_SUBMERGE = 18.0D;
    private static final double WATER_LEASH_RANGE = 64.0D;
    private static final double WATER_ACQUIRE_RANGE = 56.0D;
    private static final double PATROL_RETURN_RANGE = 32.0D;
    private static final int WATER_SAMPLE_INTERVAL = 40;
    private static final int FLUID_REFRESH_INTERVAL = 5;
    private static final int[] WATER_SAMPLE_RADII = {16, 32};
    private static final double FOLLOW_RANGE_LEEWAY = 1.25D;

    private static final int STUCK_CHECK_INTERVAL = 40;
    private static final int STUCK_LIFT_TICKS = 40;
    private static final int STUCK_GIVE_UP_CHECKS = 6;
    private static final double STUCK_MIN_DISTANCE_SQR = 36.0D;

    private static final int STRIKE_TELEGRAPH_TICKS = 25;
    private static final int STRIKE_TELEGRAPH_TICKS_PHASE_TWO = 20;
    private static final double STRIKE_RADIUS = 3.0D;
    private static final double STRIKE_REACH_ABOVE = 64.0D;
    private static final int MAX_MARKED_STRIKES = 6;
    private static final int STRIKE_NO_SKY_RETRY_TICKS = 20;
    private static final int STORM_REFRESH_TICKS = 100;
    private static final int STORM_DURATION_TICKS = 300;

    private static final double DAMAGE_RANGE_FALLOFF_SPAN = 45.0D;
    private static final double HITBOX_CREST_HEIGHT = 8.0D;
    private static final int RANGE_FEEDBACK_INTERVAL = 20;

    private static final Map<ServerLevel, Set<UUID>> STORM_HOLDERS = new WeakHashMap<>();
    private static final double GLIMMER_ABANDON_RANGE = 80.0D;
    private static final int GLIMMER_ABANDON_TICKS = 600;
    private static final ResourceKey<LootTable> GLIMMER_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "entities/kraken_glimmer"));

    private static final ResourceLocation PHASE_TWO_SPEED_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_phase_two_speed");
    private static final ResourceLocation PHASE_TWO_FLYING_SPEED_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_phase_two_flying_speed");
    private static final ResourceLocation PHASE_TWO_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "kraken_phase_two_damage");
    private static final double PHASE_TWO_SPEED_BONUS = 0.15D;
    private static final double PHASE_TWO_DAMAGE_BONUS = 4.0D;
    private static final AttributeModifier PHASE_TWO_SPEED = new AttributeModifier(PHASE_TWO_SPEED_ID, PHASE_TWO_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier PHASE_TWO_FLYING_SPEED = new AttributeModifier(PHASE_TWO_FLYING_SPEED_ID, PHASE_TWO_SPEED_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final AttributeModifier PHASE_TWO_DAMAGE = new AttributeModifier(PHASE_TWO_DAMAGE_ID, PHASE_TWO_DAMAGE_BONUS, AttributeModifier.Operation.ADD_VALUE);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation CRY_ANIM = RawAnimation.begin().thenPlay("cry");
    private static final RawAnimation GRAB_ANIM = RawAnimation.begin().thenPlay("grab");
    private static final RawAnimation SWING_LEFT_ANIM = RawAnimation.begin().thenPlay("swing_left");
    private static final RawAnimation SWING_RIGHT_ANIM = RawAnimation.begin().thenPlay("swing_right");
    private static final RawAnimation SLAM_ANIM = RawAnimation.begin().thenPlay("attack");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    private static final Vec3 GRAB_ANCHOR_LOCAL = new Vec3(22.0D / 16.0D, 223.5D / 16.0D, -12.5D / 16.0D);

    private final ServerBossEvent bossEvent = new EntityLinkedServerBossEvent(this, Component.translatable("entity.antarchy.kraken"), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Nullable
    private Vec3 patrolTarget;
    private int patrolRetargetTicks;
    private int patrolSearchRetryTicks;
    private float orbitAngle;
    private float orbitRate;
    private boolean orbitInitialized;
    @Nullable
    private Vec3 combatDestination;
    private int destinationRefreshTicks;
    @Nullable
    private Vec3 progressDestination;
    private double progressDistanceSqr;
    private int progressCheckTicks;
    private int stuckChecks;
    private int stuckLiftTicks;
    @Nullable
    private BlockPos waterAnchor;
    private int noTargetTicks;
    private int lastTargetSwitchTick;
    private int rangeFeedbackTick;
    private boolean yawLocked;
    private boolean chainedSwing;
    private int grabCooldown;
    private boolean grabCaptured;
    private float grabBreakDamage;
    @Nullable
    private Vec3 grabPoint;
    private int lungeCooldown;
    private int kiteTicks;
    private boolean lungeBlocked;
    private Vec3 lungeVelocity = Vec3.ZERO;
    private final Set<Integer> lungeHitIds = new HashSet<>();
    private final List<MarkedStrike> markedStrikes = new ArrayList<>();
    private int attackCooldown;
    private int actionTicks;
    private int phaseTransitionTicks;
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
    private int glimmerRingTicks;
    private int glimmerAbandonTicks;

    @Nullable
    private Entity[] multipartParts;

    public KrakenEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new KrakenMoveControl(this);
        this.xpReward = BASE_XP_REWARD;
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.krakenHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.krakenAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.krakenMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.krakenFlyingSpeed())
                .add(Attributes.FOLLOW_RANGE, AntarchySettings.krakenFollowRange())
                .add(Attributes.KNOCKBACK_RESISTANCE, AntarchySettings.krakenKnockbackResistance())
                .add(Attributes.ARMOR, AntarchySettings.krakenArmor())
                .add(Attributes.ARMOR_TOUGHNESS, AntarchySettings.krakenArmorToughness());
    }

    public static boolean canSpawn(EntityType<KrakenEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    public MultipartLayout anthology$getMultipartLayout() {
        return KrakenMultipartLayout.INSTANCE;
    }

    @Override
    @Nullable
    public Entity[] anthology$getMultipartParts() {
        return this.multipartParts;
    }

    @Override
    public void anthology$setMultipartParts(@Nullable Entity[] parts) {
        this.multipartParts = parts;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_STATE, ATTACK_NONE);
        builder.define(ROARING, false);
        builder.define(PHASE_TWO, false);
        builder.define(ANIMATION_STATE, ANIM_NONE);
        builder.define(GLIMMER_ECHO, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return KrakenEntity.this.canSwitchTargetFromHurt() && super.canUse();
            }
        };
        hurtByTargetGoal.setAlertOthers(MissileSquidEntity.class);
        hurtByTargetGoal.setUnseenMemoryTicks(COMBAT_GRACE_TICKS);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        NearestAttackableTargetGoal<Player> playerGoal = new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isWithinAcquireLeash);
        playerGoal.setUnseenMemoryTicks(COMBAT_GRACE_TICKS);
        this.targetSelector.addGoal(2, playerGoal);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle_controller", 0, state -> {
            if (this.getAnimationState() == ANIM_DEATH) {
                return PlayState.STOP;
            }
            state.setAndContinue(IDLE_ANIM);
            return PlayState.CONTINUE;
        }).setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
        controllers.add(new AnimationController<>(this, "action_controller", 0, this::actionAnimController)
                .setSoundKeyframeHandler(new AutoPlayingSoundKeyframeHandler<>()));
    }

    private PlayState actionAnimController(AnimationState<KrakenEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_CRY -> state.setAndContinue(CRY_ANIM);
            case ANIM_GRAB -> state.setAndContinue(GRAB_ANIM);
            case ANIM_SWING_LEFT -> state.setAndContinue(SWING_LEFT_ANIM);
            case ANIM_SWING_RIGHT -> state.setAndContinue(SWING_RIGHT_ANIM);
            case ANIM_SLAM -> state.setAndContinue(SLAM_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> {
                state.getController().forceAnimationReset();
                yield PlayState.STOP;
            }
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
        double health = AntarchySettings.krakenHealth();
        double attackDamage = AntarchySettings.krakenAttackDamage();
        if (this.isGlimmerEcho()) {
            health *= AntarchySettings.glimmerKrakenHealthMultiplier();
            attackDamage *= AntarchySettings.glimmerKrakenDamageMultiplier();
        }
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(health);
        this.setHealth((float) health);
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(attackDamage);
        this.playSound(AntarchySoundEvents.KRAKEN_SUMMON.get(), 2.2F, 0.9F + this.random.nextFloat() * 0.08F);
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.remove(Entity.RemovalReason.DISCARDED);
            return;
        }

        super.tick();
        this.setAirSupply(this.getMaxAirSupply());
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            this.tickClientParticles();
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
            return;
        }

        if (this.isDeadOrDying()) {
            this.setDeltaMovement(Vec3.ZERO);
            this.bossEvent.setProgress(0.0F);
            this.updateAnimationState();
            return;
        }

        if (this.isGlimmerEcho()) {
            GlimmerEchoes.tickAmbient(this, 4, 8);
            if (this.tickGlimmerAbandonment()) {
                return;
            }
            this.tickGlimmerRing();
        }

        this.updateBossBarPlayers();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickWaterAnchor();

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }
        if (this.phaseSummonCooldown > 0) {
            this.phaseSummonCooldown--;
        }
        if (this.grabCooldown > 0) {
            this.grabCooldown--;
        }
        if (this.lungeCooldown > 0) {
            this.lungeCooldown--;
        }

        LivingEntity target = this.resolveCombatTarget();
        this.tickLightning(target);

        if (!this.isPhaseTwo() && this.phaseTransitionTicks <= 0 && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.startPhaseTransition();
        }

        if (this.phaseTransitionTicks > 0) {
            this.tickPhaseTransition(target);
            this.finishServerTick(target);
            return;
        }

        if (target == null) {
            this.noTargetTicks++;
            if (this.getAttackState() != ATTACK_NONE || this.isRoaring()) {
                this.cancelCurrentAction();
            }
            if (this.noTargetTicks >= COMBAT_GRACE_TICKS) {
                this.resetCombatState();
            }
            this.tickPatrolMovement();
            this.finishServerTick(null);
            return;
        }

        this.noTargetTicks = 0;
        this.getLookControl().setLookAt(target, 25.0F, 20.0F);
        this.tickStormOwnership(target);

        if (!this.aggroTriggered) {
            this.startAggroRoar();
        }

        if (this.isRoaring()) {
            this.tickRoarState(target);
            this.finishServerTick(target);
            return;
        }

        if (this.getAttackState() != ATTACK_NONE) {
            this.tickCurrentAttack(target);
            this.finishServerTick(target);
            return;
        }

        this.tickPhaseTwoPressure(target);
        this.tickPursuitMovement(target);

        if (this.attackCooldown <= 0) {
            int nextAttack = this.chooseNextAttack(target);
            if (nextAttack != ATTACK_NONE) {
                this.startAttack(nextAttack, target);
            } else {
                this.attackCooldown = ATTACK_SELECTION_RETRY_TICKS;
            }
        }

        this.finishServerTick(target);
    }

    private void finishServerTick(@Nullable LivingEntity target) {
        this.updateBodyRotation(target);
        this.updateAnimationState();
    }

    @Nullable
    private LivingEntity resolveCombatTarget() {
        LivingEntity target = this.getTarget();
        if (target != null && (!target.isAlive() || (this.tickCount % 10 == 0 && !this.isValidCombatTarget(target)))) {
            this.setTarget(null);
            target = null;
        }
        if (target == null && this.tickCount % 10 == 0) {
            target = this.findVisibleNearbyPlayer();
            if (target != null) {
                this.setTarget(target);
            }
        }
        return target;
    }

    private boolean isValidCombatTarget(LivingEntity target) {
        if (!target.isAlive() || target.level() != this.level() || !this.canAttack(target)) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        double followRange = this.getAttributeValue(Attributes.FOLLOW_RANGE) * FOLLOW_RANGE_LEEWAY;
        if (this.horizontalDistanceToSqr(target) > followRange * followRange) {
            return false;
        }
        return this.anchorDistanceSqr(target) <= WATER_LEASH_RANGE * WATER_LEASH_RANGE;
    }

    private boolean isWithinAcquireLeash(LivingEntity entity) {
        return this.anchorDistanceSqr(entity) <= WATER_ACQUIRE_RANGE * WATER_ACQUIRE_RANGE;
    }

    private double anchorDistanceSqr(Entity entity) {
        if (this.waterAnchor == null) {
            return 0.0D;
        }
        double dx = entity.getX() - (this.waterAnchor.getX() + 0.5D);
        double dz = entity.getZ() - (this.waterAnchor.getZ() + 0.5D);
        return dx * dx + dz * dz;
    }

    private boolean canSwitchTargetFromHurt() {
        LivingEntity attacker = this.getLastHurtByMob();
        if (attacker != null && this.anchorDistanceSqr(attacker) > WATER_LEASH_RANGE * WATER_LEASH_RANGE) {
            return false;
        }
        return this.getTarget() == null || this.tickCount - this.lastTargetSwitchTick >= TARGET_SWITCH_HOLD_TICKS;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != this.getTarget()) {
            this.lastTargetSwitchTick = this.tickCount;
            this.orbitInitialized = false;
            this.combatDestination = null;
            this.kiteTicks = 0;
        }
        super.setTarget(target);
    }

    @Nullable
    private Player findVisibleNearbyPlayer() {
        Player nearbyPlayer = null;
        double nearestHorizontalDistanceSqr = Double.MAX_VALUE;
        for (Player player : this.level().players()) {
            if (!player.isAlive() || !this.isWithinBossBarRange(player) || !this.isValidCombatTarget(player) || !this.isWithinAcquireLeash(player)) {
                continue;
            }
            double horizontalDistanceSqr = this.horizontalDistanceToSqr(player);
            if (horizontalDistanceSqr < nearestHorizontalDistanceSqr && this.hasLineOfSight(player)) {
                nearestHorizontalDistanceSqr = horizontalDistanceSqr;
                nearbyPlayer = player;
            }
        }
        return nearbyPlayer;
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

    // The 11.4 x 39 hitbox makes vanilla's per-tick block scans read ~5-6k blocks each (fluid
    // heights + getFlow on every water block, plus entityInside on every block). The Kraken drives
    // its own movement and ignores currents, so skip the push, refresh water state every few ticks,
    // and skip inside-block callbacks entirely.
    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected boolean updateInWaterStateAndDoFluidPushing() {
        if (!this.firstTick && (this.tickCount + this.getId()) % FLUID_REFRESH_INTERVAL != 0) {
            return this.isInWater();
        }
        return super.updateInWaterStateAndDoFluidPushing();
    }

    @Override
    protected void checkInsideBlocks() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        if (source.is(DamageTypeTags.IS_FIRE)
                || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.LIGHTNING_BOLT)
                || source.is(AntarchyDamageTypes.KRAKEN_LIGHTNING)
                || source.getEntity() instanceof MissileSquidEntity
                || source.getEntity() instanceof OctopusBombEntity) {
            return false;
        }
        boolean bypassesInvulnerability = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (this.phaseTransitionTicks > 0 && !bypassesInvulnerability) {
            return false;
        }

        if (!bypassesInvulnerability) {
            float rangeMultiplier = this.damageRangeMultiplier(source);
            if (rangeMultiplier < 1.0F) {
                this.playRangeFeedback(source);
            }
            if (rangeMultiplier <= 0.0F) {
                return false;
            }
            amount *= rangeMultiplier;
        }

        float preHealth = this.getHealth();
        boolean hurt = super.hurt(source, amount);
        if (hurt && this.grabbedTargetId >= 0) {
            this.grabBreakDamage += Math.max(0.0F, preHealth - this.getHealth());
            if (this.grabBreakDamage >= this.getMaxHealth() * GRAB_BREAK_HEALTH_FRACTION) {
                this.breakGrab();
            }
        }
        return hurt;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float preHealth = this.getHealth();
        super.actuallyHurt(source, amount);
        float halfHealth = this.getMaxHealth() * 0.5F;
        if (!this.isPhaseTwo() && preHealth > halfHealth && this.getHealth() < halfHealth && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.setHealth(halfHealth);
        }
    }

    private float damageRangeMultiplier(DamageSource source) {
        double fullRange = AntarchySettings.krakenDamageRange();
        if (fullRange <= 0.0D || !(source.getEntity() instanceof LivingEntity attacker)) {
            return 1.0F;
        }
        double distance = this.distanceToHitbox(attacker);
        if (distance <= fullRange) {
            return 1.0F;
        }
        return (float) Mth.clamp(1.0D - (distance - fullRange) / DAMAGE_RANGE_FALLOFF_SPAN, 0.0D, 1.0D);
    }

    private double distanceToHitbox(Entity entity) {
        AABB box = this.getBoundingBox();
        double dx = Math.max(Math.max(box.minX - entity.getX(), 0.0D), entity.getX() - box.maxX);
        double dy = Math.max(Math.max(box.minY - entity.getY(), 0.0D), entity.getY() - (box.maxY + HITBOX_CREST_HEIGHT));
        double dz = Math.max(Math.max(box.minZ - entity.getZ(), 0.0D), entity.getZ() - box.maxZ);
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void playRangeFeedback(DamageSource source) {
        if (this.tickCount - this.rangeFeedbackTick < RANGE_FEEDBACK_INTERVAL || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.rangeFeedbackTick = this.tickCount;
        Entity attacker = source.getEntity();
        if (attacker != null) {
            serverLevel.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.8F, 0.6F);
        }
        Entity direct = source.getDirectEntity();
        if (direct != null) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, direct.getX(), direct.getY(), direct.getZ(), 8, 0.3D, 0.3D, 0.3D, 0.05D);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
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
        tag.putInt(PHASE_TRANSITION_TICKS_KEY, this.phaseTransitionTicks);
        tag.putInt(PHASE_SUMMON_COOLDOWN_KEY, this.phaseSummonCooldown);
        tag.putInt(LAST_ATTACK_STATE_KEY, this.lastAttackState);
        tag.putBoolean(AGGRO_TRIGGERED_KEY, this.aggroTriggered);
        tag.putBoolean("PhaseTwo", this.isPhaseTwo());
        tag.putBoolean("SpawnedPhaseMinions", this.spawnedPhaseMinions);
        tag.putInt("LightningStrikeCooldown", this.lightningStrikeCooldown);
        tag.putInt("LightningAmbientCooldown", this.lightningAmbientCooldown);
        if (this.waterAnchor != null) {
            tag.putLong(WATER_ANCHOR_KEY, this.waterAnchor.asLong());
        }
        tag.putBoolean(GlimmerEchoes.SAVE_KEY, this.isGlimmerEcho());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setGlimmerEcho(tag.getBoolean(GlimmerEchoes.SAVE_KEY));
        this.attackCooldown = tag.getInt(ATTACK_COOLDOWN_KEY);
        this.actionTicks = 0;
        boolean transitionWasRunning = tag.getInt(PHASE_TRANSITION_TICKS_KEY) > 0;
        this.phaseTransitionTicks = 0;
        this.phaseSummonCooldown = tag.getInt(PHASE_SUMMON_COOLDOWN_KEY);
        this.lastAttackState = tag.contains(LAST_ATTACK_STATE_KEY) ? tag.getInt(LAST_ATTACK_STATE_KEY) : ATTACK_NONE;
        this.aggroTriggered = tag.getBoolean(AGGRO_TRIGGERED_KEY);
        this.grabbedTargetId = -1;
        this.yawLocked = false;
        this.spawnedPhaseMinions = tag.getBoolean("SpawnedPhaseMinions") || transitionWasRunning;
        this.waterAnchor = tag.contains(WATER_ANCHOR_KEY) ? BlockPos.of(tag.getLong(WATER_ANCHOR_KEY)) : null;
        this.entityData.set(ROARING, false);
        this.entityData.set(ATTACK_STATE, ATTACK_NONE);
        this.entityData.set(PHASE_TWO, tag.getBoolean("PhaseTwo") || transitionWasRunning);
        this.entityData.set(ANIMATION_STATE, ANIM_NONE);
        this.lightningStrikeCooldown = tag.getInt("LightningStrikeCooldown");
        this.lightningAmbientCooldown = tag.getInt("LightningAmbientCooldown");
        this.stormActive = false;
        if (this.isPhaseTwo()) {
            this.applyPhaseTwoBuffs();
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.clearKrakenStorm();
        this.markedStrikes.clear();
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    @Override
    public boolean isGlimmerEcho() {
        return this.entityData.get(GLIMMER_ECHO);
    }

    @Override
    public void setGlimmerEcho(boolean glimmerEcho) {
        this.entityData.set(GLIMMER_ECHO, glimmerEcho);
        this.xpReward = glimmerEcho ? GLIMMER_XP_REWARD : BASE_XP_REWARD;
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public float glimmerEchoDamageMultiplier() {
        return this.isGlimmerEcho() ? (float) AntarchySettings.glimmerKrakenDamageMultiplier() : 1.0F;
    }

    @Override
    protected Component getTypeName() {
        return this.isGlimmerEcho() ? Component.translatable("entity.antarchy.kraken.glimmer") : super.getTypeName();
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return this.isGlimmerEcho() ? GLIMMER_LOOT_TABLE : super.getDefaultLootTable();
    }

    private int scaledCooldown(int ticks) {
        if (!this.isGlimmerEcho()) {
            return ticks;
        }
        return Math.max(10, (int) Math.round(ticks * AntarchySettings.glimmerKrakenCooldownMultiplier()));
    }

    private boolean tickGlimmerAbandonment() {
        if (this.tickCount % 20 != 0) {
            return false;
        }
        LivingEntity target = this.getTarget();
        if ((target != null && target.isAlive()) || GlimmerEchoes.hasPlayerWithin(this, GLIMMER_ABANDON_RANGE)) {
            this.glimmerAbandonTicks = 0;
            return false;
        }
        this.glimmerAbandonTicks += 20;
        if (this.glimmerAbandonTicks < GLIMMER_ABANDON_TICKS) {
            return false;
        }
        GlimmerEchoes.fadeAway(this);
        return true;
    }

    private void tickGlimmerRing() {
        if (this.glimmerRingTicks <= 0) {
            return;
        }
        this.glimmerRingTicks--;
        LivingEntity target = this.getTarget();
        double centerY = target != null ? target.getY() : this.getY() + this.getBbHeight() * 0.3D;
        Vec3 center = new Vec3(this.getX(), centerY, this.getZ());
        if (this.level() instanceof ServerLevel serverLevel && this.glimmerRingTicks % 5 == 0) {
            double progress = 1.0D - this.glimmerRingTicks / (double) GLIMMER_RING_DELAY_TICKS;
            GlimmerEchoes.ring(serverLevel, center, 2.0D + GLIMMER_RING_RADIUS * progress, 40);
        }
        if (this.glimmerRingTicks == 0) {
            this.applyGlimmerRing(center);
        }
    }

    private void applyGlimmerRing(Vec3 center) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double rangeSqr = GLIMMER_RING_RADIUS * GLIMMER_RING_RADIUS;
        AABB ringArea = this.getBoundingBox().inflate(GLIMMER_RING_RADIUS);
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, ringArea,
                player -> player.isAlive() && !player.isSpectator() && this.isWithinStrikeBand(player, SLAM_BAND_ABOVE) && this.horizontalDistanceToSqr(player) <= rangeSqr);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * GLIMMER_RING_DAMAGE_FRACTION;
        for (Player player : players) {
            if (player.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), damage)) {
                this.knockAway(player, 1.2D, 0.45D);
            }
        }
        GlimmerEchoes.ring(serverLevel, center, GLIMMER_RING_RADIUS + 1.0D, 56);
        this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.5F, 0.6F);
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
        if (this.entityData.get(ANIMATION_STATE) != animationState) {
            this.entityData.set(ANIMATION_STATE, animationState);
        }
    }

    private void startAggroRoar() {
        this.aggroTriggered = true;
        this.actionTicks = ROAR_TICKS;
        this.lightningStrikeCooldown = Math.min(this.lightningStrikeCooldown, 30);
        this.setRoaring(true);
        this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 1.8F, 0.78F);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
        this.updateAnimationState();
    }

    private void tickRoarState(LivingEntity target) {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        this.getLookControl().setLookAt(target, 25.0F, 20.0F);

        if (--this.actionTicks <= 0) {
            this.actionTicks = 0;
            this.setRoaring(false);
            this.attackCooldown = this.scaledCooldown(18);
        }
    }

    private void startPhaseTransition() {
        this.cancelCurrentAction();
        this.phaseTransitionTicks = PHASE_TRANSITION_TOTAL_TICKS;
        this.spawnedPhaseMinions = false;
        this.phaseSummonCooldown = 0;
        this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 2.0F, 0.82F);
        this.getNavigation().stop();
        this.updateAnimationState();
    }

    private void tickPhaseTransition(@Nullable LivingEntity target) {
        this.getNavigation().stop();
        this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0.0D);
        if (target != null) {
            this.getLookControl().setLookAt(target, 25.0F, 20.0F);
        }

        if (!this.spawnedPhaseMinions && this.phaseTransitionTicks <= PHASE_MINION_TICK) {
            int room = this.minionRoom();
            if (room > 0) {
                this.spawnMinions(Math.min(room, 1 + this.random.nextInt(2)), target);
            }
            this.spawnedPhaseMinions = true;
        }

        if (this.phaseTransitionTicks % 8 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.SQUID_INK),this.getX(), this.getY() + 1.0D, this.getZ(), 12, 1.0D, 0.7D, 1.0D, 0.02D);
        }

        if (--this.phaseTransitionTicks <= 0) {
            this.phaseTransitionTicks = 0;
            this.setPhaseTwo(true);
            this.applyPhaseTwoBuffs();
            this.attackCooldown = this.scaledCooldown(25);
            this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 2.0F, 0.72F);
        }
    }

    private void tickCurrentAttack(LivingEntity target) {
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 25.0F);

        switch (this.getAttackState()) {
            case ATTACK_GRAB -> {
                this.dampMovement();
                this.tickGrabAttack(target);
            }
            case ATTACK_SWING_LEFT -> {
                this.dampMovement();
                this.tickSwingAttack(target, false);
            }
            case ATTACK_SWING_RIGHT -> {
                this.dampMovement();
                this.tickSwingAttack(target, true);
            }
            case ATTACK_SLAM -> {
                this.dampMovement();
                this.tickSlamAttack();
            }
            case ATTACK_BREACH -> this.tickBreachAttack(target);
            default -> this.finishAttack(target);
        }
    }

    private void dampMovement() {
        this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
    }

    private void startAttack(int attackState, LivingEntity target) {
        this.setAttackState(attackState);
        this.grabbedTargetId = -1;
        this.yawLocked = false;

        if (attackState == ATTACK_GRAB) {
            this.actionTicks = GRAB_TOTAL_TICKS;
            this.grabPoint = target.position();
            this.grabCaptured = false;
            this.grabBreakDamage = 0.0F;
            this.playSound(AntarchySoundEvents.KRAKEN_ATTACK.get(), 1.45F, 0.9F);
        } else if (attackState == ATTACK_SLAM) {
            this.actionTicks = SLAM_TOTAL_TICKS;
            this.playSound(AntarchySoundEvents.KRAKEN_ATTACK.get(), 1.55F, this.isPhaseTwo() ? 0.78F : 0.84F);
        } else if (attackState == ATTACK_SWING_LEFT || attackState == ATTACK_SWING_RIGHT) {
            this.actionTicks = SWING_TOTAL_TICKS;
            this.playSound(AntarchySoundEvents.KRAKEN_SPIN.get(), 1.3F, 0.95F);
        } else if (attackState == ATTACK_BREACH) {
            this.actionTicks = BREACH_TOTAL_TICKS;
            this.lungeBlocked = false;
            this.lungeVelocity = Vec3.ZERO;
            this.lungeHitIds.clear();
            this.playSound(AntarchySoundEvents.KRAKEN_FLYING_LOOP.get(), 2.5F, 0.7F);
        }
    }

    private void tickGrabAttack(LivingEntity target) {
        int elapsed = GRAB_TOTAL_TICKS - this.actionTicks;
        if (elapsed == GRAB_LOCK_ELAPSED) {
            this.yawLocked = true;
        }
        if (elapsed < GRAB_CAPTURE_ELAPSED && elapsed % 3 == 0) {
            this.sendGrabTelegraph();
        }
        if (elapsed == GRAB_CAPTURE_ELAPSED) {
            this.tryCaptureGrab(target);
        }

        LivingEntity grabbedTarget = this.getGrabbedTarget();
        if (grabbedTarget != null) {
            if (!this.canHoldGrabbedTarget(grabbedTarget)) {
                this.releaseGrab(false);
            } else if (elapsed >= GRAB_RELEASE_ELAPSED) {
                this.releaseGrab(true);
            } else {
                this.holdGrabbedTarget(grabbedTarget);
                if (elapsed > GRAB_CAPTURE_ELAPSED && elapsed % 10 == 0 && this.level() instanceof ServerLevel serverLevel) {
                    grabbedTarget.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), 4.0F * this.glimmerEchoDamageMultiplier());
                }
            }
        }

        if (--this.actionTicks <= 0) {
            this.finishAttack(target);
        }
    }

    private void sendGrabTelegraph() {
        if (this.grabPoint != null && this.level() instanceof ServerLevel serverLevel) {
            this.sendRing(serverLevel, this.grabPoint, GRAB_CAPTURE_RADIUS, GlimmerEchoes.particle(this, ParticleTypes.BUBBLE_POP));
        }
    }

    private void tryCaptureGrab(LivingEntity target) {
        if (this.grabPoint == null || !this.canBeGrabbed(target) || !this.hasClearStrike(target)) {
            return;
        }
        double dx = target.getX() - this.grabPoint.x;
        double dz = target.getZ() - this.grabPoint.z;
        if (dx * dx + dz * dz > GRAB_CAPTURE_RADIUS * GRAB_CAPTURE_RADIUS || Math.abs(target.getY() - this.grabPoint.y) > GRAB_CAPTURE_HEIGHT) {
            return;
        }
        this.grabbedTargetId = target.getId();
        this.grabCaptured = true;
        this.grabBreakDamage = 0.0F;
        target.stopRiding();
    }

    private boolean canBeGrabbed(LivingEntity target) {
        return target.isAlive() && target.getBbWidth() <= 2.5F && !(target instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    private boolean canHoldGrabbedTarget(LivingEntity target) {
        return target.level() == this.level() && this.canBeGrabbed(target);
    }

    private void releaseGrab(boolean throwTarget) {
        LivingEntity grabbedTarget = this.getGrabbedTarget();
        if (throwTarget && grabbedTarget != null && grabbedTarget.isAlive()) {
            this.knockAway(grabbedTarget, 2.0D, 1.8D);
        }
        if (this.grabbedTargetId >= 0) {
            this.grabCooldown = this.scaledCooldown(GRAB_COOLDOWN_TICKS);
        }
        this.grabbedTargetId = -1;
    }

    private void breakGrab() {
        this.releaseGrab(false);
        this.actionTicks = Math.min(this.actionTicks, 15);
        this.playSound(AntarchySoundEvents.KRAKEN_HURT.get(), 2.0F, 0.7F);
    }

    private void tickSwingAttack(LivingEntity target, boolean swingRight) {
        if (this.actionTicks == SWING_LOCK_TICK) {
            this.yawLocked = true;
        }
        if (this.actionTicks == SWING_STRIKE_TICK) {
            this.performSwing(swingRight);
        }

        if (--this.actionTicks <= 0) {
            this.finishAttack(target);
        }
    }

    private void tickSlamAttack() {
        if (this.actionTicks == SLAM_LOCK_TICK) {
            this.yawLocked = true;
        }
        if (this.actionTicks == SLAM_STRIKE_TICK) {
            this.performSlam();
        }

        if (--this.actionTicks <= 0) {
            this.finishAttack(this.getTarget());
        }
    }

    private void tickBreachAttack(LivingEntity target) {
        int elapsed = BREACH_TOTAL_TICKS - this.actionTicks;
        if (elapsed == BREACH_COMMIT_ELAPSED) {
            this.commitBreach(target);
        }
        boolean active = elapsed >= BREACH_COMMIT_ELAPSED && elapsed < BREACH_COMMIT_ELAPSED + BREACH_ACTIVE_TICKS && !this.lungeBlocked;
        if (active) {
            if (elapsed > BREACH_COMMIT_ELAPSED && this.horizontalCollision) {
                this.lungeBlocked = true;
                this.setDeltaMovement(Vec3.ZERO);
                this.playSound(AntarchySoundEvents.KRAKEN_HURT.get(), 2.0F, 0.6F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.SPLASH), this.getX(), this.getY() + 1.0D, this.getZ(), 40, 3.0D, 1.0D, 3.0D, 0.12D);
                }
            } else {
                this.setDeltaMovement(this.lungeVelocity);
                this.sweepBreachHits();
            }
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));
        }

        if (--this.actionTicks <= 0) {
            this.finishAttack(target);
        }
    }

    private void commitBreach(LivingEntity target) {
        Vec3 flat = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (flat.lengthSqr() < 1.0E-4D) {
            flat = this.bodyForward();
        }
        Vec3 direction = flat.normalize();
        double travel = this.edgeDistance(target) + BREACH_OVERSHOOT;
        double speed = Math.min(BREACH_MAX_SPEED, travel / BREACH_ACTIVE_TICKS);
        double verticalSpeed = Mth.clamp((this.combatHeight(target.getX(), target.getZ(), target) - this.getY()) / BREACH_ACTIVE_TICKS, -0.3D, 0.3D);
        this.lungeVelocity = new Vec3(direction.x * speed, verticalSpeed, direction.z * speed);
        this.yawLocked = true;
        this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 2.0F, 0.9F);
    }

    private void sweepBreachHits() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB sweep = this.getBoundingBox().expandTowards(this.lungeVelocity).inflate(1.5D, 1.0D, 1.5D);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.2F;
        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, sweep, this::isStrikeCandidate)) {
            if (!this.lungeHitIds.add(victim.getId())) {
                continue;
            }
            if (victim.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), damage)) {
                victim.push(this.lungeVelocity.x * 1.2D, 0.6D, this.lungeVelocity.z * 1.2D);
                victim.hurtMarked = true;
            }
        }
    }

    private void cancelCurrentAction() {
        this.releaseGrab(false);
        this.setAttackState(ATTACK_NONE);
        this.setRoaring(false);
        this.actionTicks = 0;
        this.yawLocked = false;
        this.chainedSwing = false;
    }

    private void finishAttack(@Nullable LivingEntity target) {
        int finished = this.getAttackState();
        this.releaseGrab(false);
        this.yawLocked = false;
        this.actionTicks = 0;
        this.lastAttackState = finished;

        if (this.isSwingAttack(finished) && !this.chainedSwing && this.isPhaseTwo() && target != null && this.random.nextBoolean()
                && this.edgeDistance(target) <= SWING_REACH + 2.0D && this.isWithinStrikeBand(target, STRIKE_BAND_ABOVE)) {
            this.chainedSwing = true;
            this.startAttack(finished == ATTACK_SWING_LEFT ? ATTACK_SWING_RIGHT : ATTACK_SWING_LEFT, target);
            return;
        }
        this.chainedSwing = false;

        this.setAttackState(ATTACK_NONE);
        int cooldown = this.isPhaseTwo() ? 12 : 18;
        if (finished == ATTACK_GRAB && !this.grabCaptured) {
            cooldown += GRAB_WHIFF_EXTRA_COOLDOWN;
        }
        if (finished == ATTACK_BREACH) {
            cooldown = BREACH_RECOVERY_COOLDOWN + (this.lungeBlocked ? BREACH_TERRAIN_EXTRA_COOLDOWN : 0);
            this.lungeCooldown = this.scaledCooldown(BREACH_COOLDOWN_TICKS);
            this.kiteTicks = 0;
        }
        this.attackCooldown = this.scaledCooldown(cooldown);
        this.updateAnimationState();
    }

    private void tickPursuitMovement(LivingEntity target) {
        this.patrolTarget = null;
        if (!this.orbitInitialized) {
            this.orbitInitialized = true;
            this.orbitAngle = (float) Mth.atan2(this.getZ() - target.getZ(), this.getX() - target.getX());
            this.orbitRate = 0.0F;
            this.strafeRetargetTicks = 100 + this.random.nextInt(100);
        }
        if (this.strafeRetargetTicks-- <= 0) {
            this.strafeRetargetTicks = 100 + this.random.nextInt(100);
            if (this.random.nextFloat() < 0.4F) {
                this.orbitDirection *= -1.0F;
            }
        }
        float wantedRate = this.orbitDirection * (this.isPhaseTwo() ? ORBIT_RATE_PHASE_TWO : ORBIT_RATE_PHASE_ONE);
        this.orbitRate += Mth.clamp(wantedRate - this.orbitRate, -ORBIT_RATE_EASE, ORBIT_RATE_EASE);
        this.orbitAngle += this.orbitRate;

        if (this.edgeDistance(target) > BREACH_MIN_EDGE + 2.0D && this.getSensing().hasLineOfSight(target)) {
            this.kiteTicks++;
        } else {
            this.kiteTicks = Math.max(0, this.kiteTicks - 2);
        }

        Vec3 destination;
        if (this.stuckLiftTicks > 0) {
            this.stuckLiftTicks--;
            destination = this.position().add(0.0D, 6.0D, 0.0D);
            this.combatDestination = null;
        } else {
            destination = this.resolveCombatDestination(target);
            this.trackMovementProgress(destination, target);
        }
        this.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, this.isPhaseTwo() ? 1.2D : 1.1D);
    }

    private void tickPatrolMovement() {
        if (this.stuckLiftTicks > 0) {
            this.stuckLiftTicks--;
            Vec3 lift = this.position().add(0.0D, 6.0D, 0.0D);
            this.getMoveControl().setWantedPosition(lift.x, lift.y, lift.z, 0.45D);
            return;
        }
        if (this.patrolRetargetTicks-- <= 0 || this.patrolTarget == null || this.position().distanceToSqr(this.patrolTarget) < 16.0D) {
            this.patrolRetargetTicks = 120 + this.random.nextInt(80);
            if (this.patrolSearchRetryTicks > 0) {
                this.patrolSearchRetryTicks--;
                this.patrolTarget = this.position();
            } else {
                this.patrolTarget = this.findPatrolTarget();
            }
        }

        if (this.patrolTarget != null) {
            Vec3 destination = this.patrolTarget;
            this.trackMovementProgress(destination, null);
            this.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 0.45D);
        }
    }

    private void trackMovementProgress(Vec3 destination, @Nullable LivingEntity target) {
        if (++this.progressCheckTicks < STUCK_CHECK_INTERVAL) {
            return;
        }
        this.progressCheckTicks = 0;
        double distanceSqr = this.position().distanceToSqr(destination);
        boolean sameGoal = this.progressDestination != null && this.progressDestination.distanceToSqr(destination) < 100.0D;
        boolean madeProgress = !sameGoal || Math.sqrt(this.progressDistanceSqr) - Math.sqrt(distanceSqr) >= 1.0D;
        this.progressDestination = destination;
        this.progressDistanceSqr = distanceSqr;
        if (madeProgress || distanceSqr <= STUCK_MIN_DISTANCE_SQR) {
            this.stuckChecks = Math.max(0, this.stuckChecks - 1);
            return;
        }
        this.stuckChecks++;
        this.stuckLiftTicks = STUCK_LIFT_TICKS;
        this.progressDestination = null;
        this.orbitDirection *= -1.0F;
        this.patrolTarget = null;
        if (this.stuckChecks >= STUCK_GIVE_UP_CHECKS) {
            this.stuckChecks = 0;
            if (target != null && !this.getSensing().hasLineOfSight(target)) {
                this.setTarget(null);
            }
        }
    }

    private void resetCombatState() {
        this.cancelCurrentAction();
        this.aggroTriggered = false;
        this.spawnedPhaseMinions = this.isPhaseTwo();
        this.kiteTicks = 0;
        this.markedStrikes.clear();
        this.clearKrakenStorm();
    }

    private double edgeDistance(Entity entity) {
        return Math.max(0.0D, Math.sqrt(this.horizontalDistanceToSqr(entity)) - this.getBbWidth() * 0.5D);
    }

    private boolean isWithinStrikeBand(Entity entity, double above) {
        double relativeY = entity.getY() - this.getY();
        return relativeY >= -STRIKE_BAND_BELOW && relativeY <= above;
    }

    private boolean isStrikeCandidate(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || entity instanceof MissileSquidEntity || entity instanceof OctopusBombEntity) {
            return false;
        }
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator();
        }
        return entity == this.getTarget();
    }

    private boolean hasClearStrike(LivingEntity entity) {
        Vec3 to = entity.getEyePosition();
        Vec3 flat = new Vec3(to.x - this.getX(), 0.0D, to.z - this.getZ());
        if (flat.lengthSqr() < 1.0E-4D) {
            return true;
        }
        Vec3 direction = flat.normalize();
        double halfWidth = this.getBbWidth() * 0.5D;
        double fromY = Mth.clamp(to.y, this.getY() + 1.0D, this.getY() + this.getBbHeight() - 1.0D);
        Vec3 from = new Vec3(this.getX() + direction.x * halfWidth, fromY, this.getZ() + direction.z * halfWidth);
        return this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private Vec3 bodyForward() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.sin(yaw), 0.0D, Mth.cos(yaw));
    }

    private Vec3 bodyRight() {
        float yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        return new Vec3(-Mth.cos(yaw), 0.0D, -Mth.sin(yaw));
    }

    private int chooseNextAttack(LivingEntity target) {
        double edge = this.edgeDistance(target);
        if (this.lungeCooldown <= 0 && this.kiteTicks >= BREACH_KITE_TICKS && edge >= BREACH_MIN_EDGE && edge <= BREACH_MAX_EDGE) {
            if (this.isBreachLaneClear(target)) {
                return ATTACK_BREACH;
            }
            this.lungeCooldown = BREACH_BLOCKED_RETRY_TICKS;
        }
        if (!this.isWithinStrikeBand(target, STRIKE_BAND_ABOVE) || !this.hasClearStrike(target)) {
            return ATTACK_NONE;
        }

        boolean canGrab = edge <= GRAB_REACH && this.grabCooldown <= 0 && this.canBeGrabbed(target);
        boolean canSwing = edge <= SWING_REACH;
        boolean canSlam = Math.sqrt(this.horizontalDistanceToSqr(target)) <= SLAM_RADIUS && this.isWithinStrikeBand(target, SLAM_BAND_ABOVE);

        int grabWeight = canGrab ? (this.lastAttackState == ATTACK_GRAB ? 1 : 4) : 0;
        int swingWeight = canSwing ? (this.isSwingAttack(this.lastAttackState) ? 1 : 5) : 0;
        int slamWeight = canSlam ? (this.lastAttackState == ATTACK_SLAM ? 1 : 3) : 0;
        int total = grabWeight + swingWeight + slamWeight;
        if (total == 0) {
            return ATTACK_NONE;
        }

        int roll = this.random.nextInt(total);
        if (roll < grabWeight) {
            return ATTACK_GRAB;
        }
        roll -= grabWeight;
        if (roll < slamWeight) {
            return ATTACK_SLAM;
        }
        return this.resolveSwingDirection(target);
    }

    private boolean isBreachLaneClear(LivingEntity target) {
        Vec3 flat = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (flat.lengthSqr() < 1.0E-4D) {
            return false;
        }
        Vec3 direction = flat.normalize();
        double travel = Math.min(this.edgeDistance(target) + BREACH_OVERSHOOT, BREACH_MAX_SPEED * BREACH_ACTIVE_TICKS);
        for (int sample = 1; sample <= 4; sample++) {
            Vec3 point = this.position().add(direction.scale(travel * sample / 4.0D));
            if (!this.isBodyClearAt(point.x, point.y, point.z)) {
                return false;
            }
        }
        return true;
    }

    private boolean isSwingAttack(int attackState) {
        return attackState == ATTACK_SWING_LEFT || attackState == ATTACK_SWING_RIGHT;
    }

    private int resolveSwingDirection(LivingEntity target) {
        Vec3 toTarget = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        if (toTarget.lengthSqr() < 1.0E-4D) {
            return this.random.nextBoolean() ? ATTACK_SWING_RIGHT : ATTACK_SWING_LEFT;
        }
        double side = this.bodyRight().dot(toTarget.normalize());
        if (Math.abs(side) < 0.2D) {
            return this.random.nextBoolean() ? ATTACK_SWING_RIGHT : ATTACK_SWING_LEFT;
        }
        return side > 0.0D ? ATTACK_SWING_RIGHT : ATTACK_SWING_LEFT;
    }

    private void performSwing(boolean swingRight) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 forward = this.bodyForward();
        Vec3 right = this.bodyRight();
        double sideSign = swingRight ? 1.0D : -1.0D;
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        Vec3 knockDir = right.scale(-sideSign);

        AABB area = this.getBoundingBox().inflate(SWING_REACH, STRIKE_BAND_BELOW, SWING_REACH);
        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::isStrikeCandidate)) {
            if (this.edgeDistance(victim) > SWING_REACH || !this.isWithinStrikeBand(victim, STRIKE_BAND_ABOVE)) {
                continue;
            }
            Vec3 toVictim = new Vec3(victim.getX() - this.getX(), 0.0D, victim.getZ() - this.getZ());
            if (toVictim.lengthSqr() > 1.0E-4D) {
                Vec3 normal = toVictim.normalize();
                if (right.dot(normal) * sideSign < SWING_ACROSS_MIDLINE || forward.dot(normal) < SWING_REAR_LIMIT) {
                    continue;
                }
            }
            if (!this.hasClearStrike(victim) || !victim.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), damage)) {
                continue;
            }
            victim.push(knockDir.x * 1.75D, 0.4D, knockDir.z * 1.75D);
            victim.hurtMarked = true;
        }

        double originDistance = this.getBbWidth() * 0.5D + SWING_REACH * 0.5D;
        Vec3 origin = this.position().add(right.scale(sideSign * originDistance));
        double originY = this.getTarget() != null ? this.getTarget().getY() : this.getY() + 0.8D;
        serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.SPLASH), origin.x, originY, origin.z, 24, 2.4D, 0.4D, 2.4D, 0.08D);
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.2F, 0.5F);
    }

    private void performSlam() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB area = this.getBoundingBox().inflate(SLAM_RADIUS, STRIKE_BAND_BELOW, SLAM_RADIUS);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.4F;
        double radiusSqr = SLAM_RADIUS * SLAM_RADIUS;

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::isStrikeCandidate)) {
            if (this.horizontalDistanceToSqr(victim) > radiusSqr || !this.isWithinStrikeBand(victim, SLAM_BAND_ABOVE) || !this.hasClearStrike(victim)) {
                continue;
            }
            if (!victim.hurt(AntarchyDamageSources.krakenMauling(serverLevel, this), damage)) {
                continue;
            }
            this.knockAway(victim, 3.2D, 0.75D);
        }

        LivingEntity target = this.getTarget();
        double effectY = target != null ? target.getY() : this.getY() + 0.8D;
        serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.SPLASH), this.getX(), effectY, this.getZ(), 40, 6.0D, 0.8D, 6.0D, 0.12D);
        serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.BUBBLE), this.getX(), effectY, this.getZ(), 30, 5.0D, 0.6D, 5.0D, 0.05D);
        this.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED, 3.0F, 0.5F);

        if (this.isGlimmerEcho()) {
            this.glimmerRingTicks = GLIMMER_RING_DELAY_TICKS;
        }
    }

    private void sendRing(ServerLevel serverLevel, Vec3 center, double radius, ParticleOptions particle) {
        for (int point = 0; point < 8; point++) {
            double angle = point * Mth.TWO_PI / 8.0D;
            serverLevel.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y + 0.2D, center.z + Math.sin(angle) * radius, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void holdGrabbedTarget(LivingEntity target) {
        Vec3 anchor = this.getGrabAnchorWorldPos();
        Vec3 toAnchor = anchor.subtract(target.position());
        Vec3 pull = toAnchor.scale(0.35D);
        if (pull.lengthSqr() > GRAB_MAX_PULL_SPEED * GRAB_MAX_PULL_SPEED) {
            pull = pull.normalize().scale(GRAB_MAX_PULL_SPEED);
        }
        target.stopRiding();
        target.setDeltaMovement(pull);
        target.resetFallDistance();
        if (toAnchor.lengthSqr() > GRAB_CORRECTION_DISTANCE_SQR && this.actionTicks % 10 == 0) {
            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.teleport(anchor.x, anchor.y, anchor.z, target.getYRot(), target.getXRot());
            } else {
                target.moveTo(anchor.x, anchor.y, anchor.z, target.getYRot(), target.getXRot());
            }
        }
        target.hurtMarked = true;
    }

    private Vec3 getGrabAnchorWorldPos() {
        Vec3 localAnchor = this.getGrabAnchorOffset();
        Vec3 rotatedAnchor = localToWorldOffset(localAnchor);
        return this.position().add(rotatedAnchor);
    }

    private Vec3 getGrabAnchorOffset() {
        if (this.getAttackState() != ATTACK_GRAB) {
            return new Vec3(this.getBbWidth() * 0.65D, this.getBbHeight() * 0.28D, 0.0D);
        }

        double progress = Mth.clamp((GRAB_TOTAL_TICKS - this.actionTicks) / (double) GRAB_RELEASE_ELAPSED, 0.0D, 1.0D);
        double forward = Mth.lerp(progress, this.getBbWidth() * 0.45D, GRAB_ANCHOR_LOCAL.z);
        double vertical = Mth.lerp(progress, this.getBbHeight() * 0.18D, GRAB_ANCHOR_LOCAL.y);
        double lateral = Mth.lerp(progress, this.getBbWidth() * 0.18D, GRAB_ANCHOR_LOCAL.x);
        return new Vec3(lateral, vertical, forward);
    }

    private Vec3 localToWorldOffset(Vec3 localOffset) {
        float bodyYawRadians = this.yBodyRot * Mth.DEG_TO_RAD;
        double cos = Math.cos(bodyYawRadians);
        double sin = Math.sin(bodyYawRadians);
        double worldX = localOffset.x * cos - localOffset.z * sin;
        double worldZ = localOffset.z * cos + localOffset.x * sin;
        return new Vec3(worldX, localOffset.y, worldZ);
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

    private void tickPhaseTwoPressure(LivingEntity target) {
        if (!this.isPhaseTwo() || this.phaseSummonCooldown > 0) {
            return;
        }
        int room = this.minionRoom();
        if (room <= 0) {
            this.phaseSummonCooldown = this.scaledCooldown(40);
            return;
        }
        int extraMinions = this.isGlimmerEcho() ? GLIMMER_EXTRA_MINIONS : 0;
        this.spawnMinions(Math.min(room, 1 + this.random.nextInt(2) + extraMinions), target);
        this.phaseSummonCooldown = this.scaledCooldown(90 + this.random.nextInt(50));
    }

    private int minionRoom() {
        int minionCap = this.isGlimmerEcho() ? MINION_CAP + GLIMMER_MINION_CAP_BONUS : MINION_CAP;
        return minionCap - this.countOwnedMinions();
    }

    private int countOwnedMinions() {
        AABB range = this.getBoundingBox().inflate(MINION_COUNT_RANGE);
        UUID owner = this.getUUID();
        return this.level().getEntitiesOfClass(OctopusBombEntity.class, range, bomb -> bomb.isAlive() && bomb.isOwnedByKraken(owner)).size()
                + this.level().getEntitiesOfClass(MissileSquidEntity.class, range, squid -> squid.isAlive() && squid.isOwnedByKraken(owner)).size();
    }

    private void spawnMinions(int desiredCount, @Nullable LivingEntity currentTarget) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int spawned = 0;

        for (int attempt = 0; attempt < desiredCount * 4 && spawned < desiredCount; attempt++) {
            Vec3 spawnPos = this.findBombSpawnPos(currentTarget);
            boolean spawnBomb = this.random.nextBoolean();

            if (spawnBomb) {
                OctopusBombEntity bomb = AntarchyObjects.OCTOPUS_BOMB.get().create(serverLevel);
                if (bomb == null) {
                    continue;
                }
                bomb.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, this.getYRot(), 0.0F);
                GlimmerEchoes.markSummon(this, bomb);
                bomb.setKrakenOwner(this.getUUID());
                bomb.applyConfiguredStats();
                serverLevel.addFreshEntity(bomb);
                bomb.launchAsProjectile(this, this.createBombLaunchVelocity(spawnPos, currentTarget));
                serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.BUBBLE), bomb.getX(), bomb.getY() + 0.4D, bomb.getZ(), 14, 0.3D, 0.25D, 0.3D, 0.03D);
            } else {
                MissileSquidEntity squid = AntarchyObjects.MISSILE_SQUID.get().create(serverLevel);
                if (squid == null) {
                    continue;
                }
                squid.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, this.getYRot(), 0.0F);
                GlimmerEchoes.markSummon(this, squid);
                squid.setKrakenOwner(this.getUUID());
                squid.setSpawnedByKraken(true);
                squid.setCanFly(true);
                serverLevel.addFreshEntity(squid);
                serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.BUBBLE), squid.getX(), squid.getY() + 0.4D, squid.getZ(), 14, 0.3D, 0.25D, 0.3D, 0.03D);
            }
            spawned++;
        }

        if (spawned > 0) {
            this.playSound(AntarchySoundEvents.KRAKEN_ROAR.get(), 1.8F, this.isPhaseTwo() ? 0.74F : 0.82F);
        }
    }

    private Vec3 findBombSpawnPos(@Nullable LivingEntity currentTarget) {
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

    private Vec3 createBombLaunchVelocity(Vec3 spawnPos, @Nullable LivingEntity currentTarget) {
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
        if (this.waterAnchor != null && this.anchorDistanceSqr(this) > PATROL_RETURN_RANGE * PATROL_RETURN_RANGE) {
            double x = this.waterAnchor.getX() + 0.5D;
            double z = this.waterAnchor.getZ() + 0.5D;
            return new Vec3(x, this.restingHeight(x, z), z);
        }

        BlockPos origin = this.blockPosition();
        Vec3 landFallback = null;
        for (int attempt = 0; attempt < 12; attempt++) {
            double x = origin.getX() + this.random.nextInt(57) - 28 + 0.5D;
            double z = origin.getZ() + this.random.nextInt(57) - 28 + 0.5D;
            if (!this.level().hasChunkAt(BlockPos.containing(x, this.getY(), z))) {
                continue;
            }
            double y = this.restingHeight(x, z);
            if (!this.isBodyClearAt(x, y, z)) {
                continue;
            }
            if (this.isWaterColumn(x, z)) {
                return new Vec3(x, y, z);
            }
            if (landFallback == null) {
                landFallback = new Vec3(x, y, z);
            }
        }

        if (landFallback != null) {
            return landFallback;
        }
        this.patrolSearchRetryTicks = PATROL_SEARCH_RETRY_TICKS;
        return this.position();
    }

    private void tickWaterAnchor() {
        if (this.waterAnchor == null) {
            this.waterAnchor = this.blockPosition();
        }
        if (this.tickCount % WATER_SAMPLE_INTERVAL != 0) {
            return;
        }
        if (this.isWaterColumn(this.getX(), this.getZ())) {
            this.waterAnchor = this.blockPosition();
            return;
        }
        for (int radius : WATER_SAMPLE_RADII) {
            for (int direction = 0; direction < 8; direction++) {
                double angle = direction * Mth.TWO_PI / 8.0D;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                if (this.isWaterColumn(x, z)) {
                    this.waterAnchor = BlockPos.containing(x, this.getY(), z);
                    return;
                }
            }
        }
    }

    private boolean isWaterColumn(double x, double z) {
        BlockPos column = BlockPos.containing(x, this.getY(), z);
        if (!this.level().hasChunkAt(column)) {
            return false;
        }
        int surface = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ());
        return this.level().getFluidState(new BlockPos(column.getX(), surface - 1, column.getZ())).is(FluidTags.WATER);
    }

    private double groundHeight(double x, double z) {
        BlockPos column = BlockPos.containing(x, this.getY(), z);
        if (!this.level().hasChunkAt(column)) {
            return this.getY();
        }
        if (this.isWaterColumn(x, z)) {
            return this.level().getHeight(Heightmap.Types.OCEAN_FLOOR, column.getX(), column.getZ()) + 1.0D;
        }
        return this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ()) + 1.0D;
    }

    private double restingHeight(double x, double z) {
        double ground = this.groundHeight(x, z);
        if (!this.isWaterColumn(x, z)) {
            return ground;
        }
        BlockPos column = BlockPos.containing(x, this.getY(), z);
        double surface = this.level().getHeight(Heightmap.Types.WORLD_SURFACE, column.getX(), column.getZ());
        return Math.max(ground, surface - WATERLINE_SUBMERGE);
    }

    private double combatHeight(double x, double z, LivingEntity target) {
        double ground = this.groundHeight(x, z);
        double y = Mth.clamp(this.restingHeight(x, z), target.getY() - STRIKE_BAND_ABOVE + 4.0D, target.getY() + 2.0D);
        return Math.max(ground, y);
    }

    private boolean isBodyClearAt(double x, double y, double z) {
        AABB box = this.getDimensions(this.getPose()).makeBoundingBox(x, y, z);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        if (!this.level().hasChunksAt(min, max)) {
            return false;
        }
        return !this.level().getBlockCollisions(this, box).iterator().hasNext();
    }

    private Vec3 resolveCombatDestination(LivingEntity target) {
        if (--this.destinationRefreshTicks > 0 && this.combatDestination != null) {
            return this.combatDestination;
        }
        this.destinationRefreshTicks = DESTINATION_REFRESH_TICKS;

        double radius = this.isPhaseTwo() ? ORBIT_RADIUS_PHASE_TWO : ORBIT_RADIUS_PHASE_ONE;
        double x = target.getX() + Math.cos(this.orbitAngle) * radius;
        double z = target.getZ() + Math.sin(this.orbitAngle) * radius;

        if (!this.isWaterColumn(x, z) && this.waterAnchor != null) {
            double anchorBearing = Mth.atan2(this.waterAnchor.getZ() + 0.5D - target.getZ(), this.waterAnchor.getX() + 0.5D - target.getX());
            double difference = Mth.wrapDegrees((anchorBearing - this.orbitAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
            this.orbitAngle += (float) Mth.clamp(difference, -ORBIT_WATER_BIAS, ORBIT_WATER_BIAS);
        }

        double bob = Math.sin(this.tickCount * 0.05D) * 0.8D;
        double y = this.combatHeight(x, z, target) + bob;
        for (double raise = 0.0D; raise <= 16.0D; raise += 4.0D) {
            if (this.isBodyClearAt(x, y + raise, z)) {
                this.combatDestination = new Vec3(x, y + raise, z);
                return this.combatDestination;
            }
        }
        if (this.combatDestination == null) {
            this.combatDestination = this.position();
        }
        return this.combatDestination;
    }

    private void tickLightning(@Nullable LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.stormActive && --this.lightningAmbientCooldown <= 0) {
            this.lightningAmbientCooldown = this.scaledCooldown(this.isPhaseTwo() ? 35 + this.random.nextInt(35) : 80 + this.random.nextInt(80));
            int ambientBolts = this.isPhaseTwo() ? 4 : 2;
            for (int i = 0; i < ambientBolts; i++) {
                Vec3 strikePos = this.getRandomStormStrikePos(serverLevel);
                if (serverLevel.hasChunkAt(BlockPos.containing(strikePos))) {
                    this.spawnVisualLightning(serverLevel, strikePos);
                }
            }
        }

        this.tickMarkedStrikes(serverLevel);

        if (target != null) {
            if (--this.lightningStrikeCooldown <= 0) {
                if (serverLevel.canSeeSky(target.blockPosition().above())) {
                    this.lightningStrikeCooldown = this.scaledCooldown(this.isPhaseTwo() ? 40 + this.random.nextInt(30) : 60 + this.random.nextInt(80));
                    this.markStrikes(serverLevel, target);
                } else {
                    this.lightningStrikeCooldown = STRIKE_NO_SKY_RETRY_TICKS;
                }
            }
        } else {
            this.lightningStrikeCooldown = Math.max(this.lightningStrikeCooldown, this.scaledCooldown(this.isPhaseTwo() ? 30 : 60));
        }
    }

    private void markStrikes(ServerLevel serverLevel, LivingEntity target) {
        int telegraph = this.isPhaseTwo() ? STRIKE_TELEGRAPH_TICKS_PHASE_TWO : STRIKE_TELEGRAPH_TICKS;
        this.addMarkedStrike(serverLevel, target.getX(), target.getZ(), telegraph);
        if (!this.isPhaseTwo()) {
            return;
        }
        Vec3 lead = target.getDeltaMovement().multiply(telegraph, 0.0D, telegraph);
        this.addMarkedStrike(serverLevel, target.getX() + lead.x, target.getZ() + lead.z, telegraph);
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        double distance = 6.0D + this.random.nextDouble() * 4.0D;
        this.addMarkedStrike(serverLevel, target.getX() + Math.cos(angle) * distance, target.getZ() + Math.sin(angle) * distance, telegraph);
        this.playSound(SoundEvents.RESPAWN_ANCHOR_CHARGE, 2.0F, 1.2F);
    }

    private void addMarkedStrike(ServerLevel serverLevel, double x, double z, int telegraph) {
        BlockPos column = BlockPos.containing(x, this.getY(), z);
        if (this.markedStrikes.size() >= MAX_MARKED_STRIKES || !serverLevel.hasChunkAt(column)) {
            return;
        }
        int y = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ());
        Vec3 position = new Vec3(x, y, z);
        this.markedStrikes.add(new MarkedStrike(position, telegraph));
        serverLevel.playSound(null, x, y, z, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.5F, 1.6F);
    }

    private void tickMarkedStrikes(ServerLevel serverLevel) {
        Iterator<MarkedStrike> strikes = this.markedStrikes.iterator();
        while (strikes.hasNext()) {
            MarkedStrike strike = strikes.next();
            if (strike.ticks % 5 == 0) {
                if (this.isGlimmerEcho()) {
                    GlimmerEchoes.ring(serverLevel, strike.position, STRIKE_RADIUS, 16);
                } else {
                    this.sendRing(serverLevel, strike.position, STRIKE_RADIUS, ParticleTypes.ELECTRIC_SPARK);
                }
            }
            if (--strike.ticks <= 0) {
                this.resolveMarkedStrike(serverLevel, strike.position);
                strikes.remove();
            }
        }
    }

    private void resolveMarkedStrike(ServerLevel serverLevel, Vec3 position) {
        this.spawnVisualLightning(serverLevel, position);
        AABB column = new AABB(position.x - STRIKE_RADIUS, position.y - 2.0D, position.z - STRIKE_RADIUS,
                position.x + STRIKE_RADIUS, position.y + STRIKE_REACH_ABOVE, position.z + STRIKE_RADIUS);
        double radiusSqr = STRIKE_RADIUS * STRIKE_RADIUS;
        float damage = (this.isPhaseTwo() ? 12.0F : AntarchySettings.krakenLightningDamagePhaseOne()) * this.glimmerEchoDamageMultiplier();
        for (LivingEntity victim : serverLevel.getEntitiesOfClass(LivingEntity.class, column, this::isStrikeCandidate)) {
            double dx = victim.getX() - position.x;
            double dz = victim.getZ() - position.z;
            if (dx * dx + dz * dz <= radiusSqr && serverLevel.canSeeSky(victim.blockPosition().above())) {
                victim.hurt(AntarchyDamageSources.krakenLightning(serverLevel, this), damage);
            }
        }
    }

    private void tickStormOwnership(LivingEntity target) {
        if (target instanceof Player && (!this.stormActive || this.tickCount % STORM_REFRESH_TICKS == 0)) {
            this.setKrakenStormActive(true);
        }
    }

    private void setKrakenStormActive(boolean active) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (active) {
            this.stormActive = true;
            STORM_HOLDERS.computeIfAbsent(serverLevel, level -> new HashSet<>()).add(this.getUUID());
            serverLevel.setWeatherParameters(0, STORM_DURATION_TICKS, true, true);
            return;
        }

        if (this.stormActive) {
            this.stormActive = false;
            Set<UUID> holders = STORM_HOLDERS.get(serverLevel);
            if (holders != null) {
                holders.remove(this.getUUID());
                holders.removeIf(holder -> serverLevel.getEntity(holder) == null);
                if (holders.isEmpty()) {
                    STORM_HOLDERS.remove(serverLevel);
                }
            }
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

        double bossBarRange = this.getBossBarRange();
        AABB bossBarArea = this.getBoundingBox().inflate(bossBarRange, 192.0D, bossBarRange);
        List<ServerPlayer> nearbyPlayers = new ArrayList<>();
        for (ServerPlayer player : serverLevel.players()) {
            boolean inRange = bossBarArea.intersects(player.getBoundingBox()) && this.isWithinBossBarRange(player);
            if (player.isAlive() && !player.isSpectator() && (inRange || player == this.getTarget())) {
                nearbyPlayers.add(player);
            }
        }

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
    }

    private double getBossBarRange() {
        return Math.max(40.0D, AntarchySettings.krakenBossBarRange());
    }

    private boolean isWithinBossBarRange(Entity entity) {
        double bossBarRange = this.getBossBarRange();
        return this.horizontalDistanceToSqr(entity) <= bossBarRange * bossBarRange;
    }

    private double horizontalDistanceToSqr(Entity entity) {
        double dx = this.getX() - entity.getX();
        double dz = this.getZ() - entity.getZ();
        return dx * dx + dz * dz;
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
            attackDamage.addPermanentModifier(this.isGlimmerEcho()
                    ? new AttributeModifier(PHASE_TWO_DAMAGE_ID, PHASE_TWO_DAMAGE_BONUS * this.glimmerEchoDamageMultiplier(), AttributeModifier.Operation.ADD_VALUE)
                    : PHASE_TWO_DAMAGE);
        }
    }

    private Vec3 getRandomStormStrikePos(ServerLevel serverLevel) {
        double lx = this.getX() + (this.random.nextDouble() - 0.5D) * 56.0D;
        double lz = this.getZ() + (this.random.nextDouble() - 0.5D) * 56.0D;
        int ly = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(lx), Mth.floor(lz));
        return new Vec3(lx, ly, lz);
    }

    private void spawnVisualLightning(ServerLevel serverLevel, Vec3 strikePos) {
        if (this.isGlimmerEcho()) {
            this.spawnGlimmerStrike(serverLevel, strikePos);
            return;
        }

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt == null) {
            return;
        }

        bolt.moveTo(strikePos.x, strikePos.y, strikePos.z);
        bolt.setVisualOnly(true);
        serverLevel.addFreshEntity(bolt);
    }

    private void spawnGlimmerStrike(ServerLevel serverLevel, Vec3 strikePos) {
        GlimmerEchoes.column(serverLevel, strikePos, GLIMMER_STRIKE_HEIGHT, 60);
        GlimmerEchoes.burst(serverLevel, strikePos.add(0.0D, GLIMMER_STRIKE_HEIGHT, 0.0D), 1.5D, 20);
        GlimmerEchoes.burst(serverLevel, strikePos.add(0.0D, 0.5D, 0.0D), 1.2D, 36);
        serverLevel.playSound(null, strikePos.x, strikePos.y, strikePos.z,
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 4.0F, 1.3F + this.random.nextFloat() * 0.2F);
        serverLevel.playSound(null, strikePos.x, strikePos.y, strikePos.z,
                SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.HOSTILE, 1.5F, 0.7F);
    }

    private void tickClientParticles() {
        if (this.isRoaring() || this.getAnimationState() == ANIM_CRY || this.getAttackState() != ATTACK_NONE) {
            if (this.tickCount % 3 == 0) {
                this.level().addParticle(
                        GlimmerEchoes.particle(this, ParticleTypes.BUBBLE),
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

    private void updateBodyRotation(@Nullable LivingEntity target) {
        float yaw = this.getYRot();
        if (!this.yawLocked) {
            if (target != null) {
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                if (dx * dx + dz * dz > 1.0E-4D) {
                    float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
                    yaw = Mth.approachDegrees(yaw, targetYaw, COMBAT_TURN_RATE);
                }
            } else {
                Vec3 velocity = this.getDeltaMovement();
                if (velocity.horizontalDistanceSqr() > 1.0E-3D) {
                    float targetYaw = (float) (Mth.atan2(velocity.z, velocity.x) * (180.0D / Math.PI)) - 90.0F;
                    yaw = Mth.approachDegrees(yaw, targetYaw, PATROL_TURN_RATE);
                }
            }
        }
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;

        Vec3 velocity = this.getDeltaMovement();
        float targetPitch = 0.0F;
        if (velocity.lengthSqr() > 1.0E-3D) {
            targetPitch = Mth.clamp((float) (-(Mth.atan2(velocity.y, velocity.horizontalDistance()) * (180.0D / Math.PI))), -MAX_BODY_PITCH, MAX_BODY_PITCH);
        }
        this.setXRot(Mth.approachDegrees(this.getXRot(), targetPitch, 2.0F));
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.setAnimationState(ANIM_DEATH);
            this.actionTicks = 0;
            this.attackCooldown = 0;
            this.phaseTransitionTicks = 0;
            this.yawLocked = false;
            this.grabbedTargetId = -1;
            this.setAttackState(ATTACK_NONE);
            this.setRoaring(false);
            this.setTarget(null);
            this.markedStrikes.clear();
            this.clearKrakenStorm();
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
        }
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.isRoaring()) {
            this.setAnimationState(ANIM_CRY);
            return;
        }

        if (this.phaseTransitionTicks > 0) {
            this.setAnimationState(ANIM_CRY);
            return;
        }

        switch (this.getAttackState()) {
            case ATTACK_GRAB -> {
                this.setAnimationState(GRAB_TOTAL_TICKS - this.actionTicks >= GRAB_CLIP_DELAY ? ANIM_GRAB : ANIM_NONE);
                return;
            }
            case ATTACK_BREACH -> {
                this.setAnimationState(ANIM_SLAM);
                return;
            }
            case ATTACK_SWING_LEFT -> {
                this.setAnimationState(ANIM_SWING_LEFT);
                return;
            }
            case ATTACK_SWING_RIGHT -> {
                this.setAnimationState(ANIM_SWING_RIGHT);
                return;
            }
            case ATTACK_SLAM -> {
                this.setAnimationState(ANIM_SLAM);
                return;
            }
            default -> {
            }
        }

        this.setAnimationState(ANIM_NONE);
    }

    private static final class MarkedStrike {
        private final Vec3 position;
        private int ticks;

        private MarkedStrike(Vec3 position, int ticks) {
            this.position = position;
            this.ticks = ticks;
        }
    }
}
