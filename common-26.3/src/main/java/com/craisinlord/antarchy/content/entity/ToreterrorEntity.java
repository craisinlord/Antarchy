package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEcho;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public class ToreterrorEntity extends Monster implements GeoEntity, GlimmerEcho {

    private static final EntityDataAccessor<Integer> ANIMATION_STATE = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_2 = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> JUMP_SHAKING = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GLIMMER_ECHO = SynchedEntityData.defineId(ToreterrorEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_SHOOT = 2;
    private static final int ANIM_JUMP_ATTACK = 3;
    private static final int ANIM_SPIN_START = 4;
    private static final int ANIM_SPIN_LOOP = 5;
    private static final int ANIM_SPIN_END = 6;
    private static final int ANIM_DEATH = 7;
    private static final int ANIM_RAPID_SHOOT = 8;
    private static final int ANIM_SWIPE_WINDUP = 9;

    private static final int ATTACK_RAPID_FIRE = 0;
    private static final int ATTACK_BOMBERS = 1;

    private static final int ACTION_NONE = -1;
    private static final int ACTION_SHOOT = 0;
    private static final int ACTION_JUMP = 1;
    private static final int ACTION_SPIN = 2;
    private static final int ACTION_SWIPE = 3;

    private static final int DEATH_TICKS = 60;
    private static final int SHOOT_TOTAL_TICKS = 75;
    private static final int RAPID_FIRE_INTERVAL = 4;
    private static final int RAPID_FIRE_END_TICK = 60;
    private static final int RAPID_FIRE_FIRST_TICK = 14;
    private static final int SHOOT_BOMBER_TICK = 12;
    private static final int SHOOT_BOMBER_SECOND_TICK = 32;
    private static final int JUMP_TOTAL_TICKS = 40;
    private static final int JUMP_LAUNCH_TICK = 10;
    private static final int JUMP_SHAKE_TICKS = 12;
    private static final int JUMP_FLIGHT_TICKS = 12;
    private static final int JUMP_FORCE_DOWN_GRACE_TICKS = 4;
    private static final int JUMP_MAX_AIR_TICKS = 80;
    private static final double JUMP_MAX_HORIZONTAL_RANGE = 22.0D;
    private static final double JUMP_MAX_ASCENT = 8.0D;
    private static final double JUMP_SELECT_MAX_ASCENT = 10.0D;
    private static final double GRAVITY_PER_TICK = 0.08D;
    private static final double VERTICAL_DRAG = 0.98D;
    private static final int SPIN_START_TOTAL_TICKS = 12;
    private static final int SPIN_LOOP_MIN_TICKS = 200;
    private static final int SPIN_LOOP_MAX_TICKS = 300;
    private static final int SPIN_END_TOTAL_TICKS = 10;
    private static final int SPIN_HIT_INTERVAL = 5;
    private static final int SPIN_STUCK_FAIL_LIMIT = 10;
    private static final int SWIPE_WINDUP_TICKS = 20;
    private static final int SWIPE_STRIKE_TICKS = 6;
    private static final int SWIPE_HIT_TICK = 22;
    private static final int SWIPE_TOTAL_TICKS = 36;
    private static final int PHASE_TRANSITION_TICKS = 50;
    private static final int PHASE_BLAST_TICK = 25;
    private static final int PHASE_SPIN_END_TICK = 35;
    private static final int PHASE_CLIP_END_TICK = 45;
    private static final int SHOOT_RECOVERY_TICKS = 16;
    private static final int JUMP_RECOVERY_TICKS = 24;
    private static final int SPIN_RECOVERY_TICKS = 28;
    private static final int SWIPE_RECOVERY_TICKS = 28;
    private static final int RANGE_FEEDBACK_INTERVAL_TICKS = 20;
    private static final int RETURN_DELAY_TICKS = 60;
    private static final int RESET_DELAY_TICKS = 400;
    private static final int TARGET_UNSEEN_MEMORY_TICKS = 300;
    private static final int CHASE_REPATH_TICKS = 8;
    private static final int CHASE_REPATH_FAIL_TICKS = 16;
    private static final int RETURN_REPATH_TICKS = 20;

    private static final double CLOSE_RANGE_SQR = 64.0D;
    private static final double JUMP_RANGE_SQR = 400.0D;
    private static final double RANGED_MAX_RANGE_SQR = 625.0D;
    private static final double SPIN_TRIGGER_RANGE_SQR = 225.0D;
    private static final double SPIN_LEASH_RANGE_SQR = 400.0D;
    private static final double SPIN_RADIUS = 2.5D;
    private static final double SPIN_MOVE_SPEED = 0.65D;
    private static final double SPIN_ANIMATION_SPEED = 0.49D;
    private static final double SPIN_START_ANIMATION_SPEED = 5.0D / SPIN_START_TOTAL_TICKS;
    private static final double SWIPE_WINDUP_ANIMATION_SPEED = 5.0D / SWIPE_WINDUP_TICKS;
    private static final double JUMP_SHOCKWAVE_RADIUS = 6.0D;
    private static final double JUMP_SHOCKWAVE_FULL_RADIUS = 3.0D;
    private static final double JUMP_SHOCKWAVE_HEIGHT = 2.0D;
    private static final float JUMP_SHOCKWAVE_MIN_DAMAGE_FRACTION = 0.5F;
    private static final double SWIPE_TRIGGER_RANGE_SQR = 36.0D;
    private static final double SWIPE_RADIUS = 5.0D;
    private static final double SWIPE_HEIGHT = 3.5D;
    private static final float SWIPE_DAMAGE_FRACTION = 0.85F;
    private static final double SWIPE_KNOCKBACK_MULTIPLIER = 1.5D;
    private static final double PHASE_BLAST_RADIUS = 7.0D;
    private static final double CHASE_STOP_RANGE_SQR = 20.25D;
    private static final double RETURN_ARRIVAL_RANGE_SQR = 81.0D;
    private static final double PARTICIPATION_RANGE = 48.0D;
    private static final double SPIN_MAX_VERTICAL_OFFSET = 3.0D;
    private static final double RANGE_FALLOFF_MAX_RATIO = 1.67D;
    private static final double PHASE_TWO_COOLDOWN_MULTIPLIER = 0.8D;
    private static final double PHASE_TWO_RECOVERY_MULTIPLIER = 0.7D;
    private static final int REPEAT_WEIGHT_DIVISOR = 3;

    private static final int BASE_XP_REWARD = 50;
    private static final int GLIMMER_XP_REWARD = 250;
    private static final int GLIMMER_RAPID_FIRE_INTERVAL = 3;
    private static final int GLIMMER_BOMBER_COUNT = 4;
    private static final int GLIMMER_BOMBER_THIRD_TICK = 52;
    private static final int GLIMMER_PULSE_DELAY_TICKS = 15;
    private static final double GLIMMER_PULSE_RADIUS = 9.0D;
    private static final float GLIMMER_PULSE_DAMAGE_FRACTION = 0.6F;
    private static final double GLIMMER_ABANDON_RANGE = 48.0D;
    private static final int GLIMMER_ABANDON_TICKS = 600;
    private static final ResourceKey<LootTable> GLIMMER_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(com.craisinlord.antarchy.Antarchy.MODID, "entities/toreterror_glimmer"));

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("Idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation SHOOT_ANIM = RawAnimation.begin().thenPlay("shoot").thenLoop("Idle");
    private static final RawAnimation RAPID_SHOOT_ANIM = RawAnimation.begin().thenLoop("shoot");
    private static final RawAnimation JUMP_ANIM = RawAnimation.begin().thenPlay("jump_attack").thenLoop("Idle");
    private static final RawAnimation SPIN_START_ANIM = RawAnimation.begin().thenPlayAndHold("spin_attack_start");
    private static final RawAnimation SWIPE_WINDUP_ANIM = RawAnimation.begin().thenPlayAndHold("spin_attack_start");
    private static final RawAnimation SPIN_LOOP_ANIM = RawAnimation.begin().thenLoop("spin_attack");
    private static final RawAnimation SPIN_END_ANIM = RawAnimation.begin().thenPlay("spin_attack_end");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent = new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this, Component.translatable("entity.antarchy.toreterror"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    private int phaseTransitionTicks;
    private int recoveryTicks;
    private int swipeTicks;
    private int swipeCooldown;
    private int previousAction = ACTION_NONE;
    private int lastRangeFeedbackTick = -RANGE_FEEDBACK_INTERVAL_TICKS;
    private boolean encounterActive;
    private int disengageTicks;
    @Nullable
    private BlockPos encounterHome;
    @Nullable
    private Vec3 jumpLandingPoint;
    private int jumpFlightTicks;
    private int jumpFlightTotalTicks;
    private int jumpAirTicks;
    private int shootAnimTicks;
    private int jumpAnimTicks;
    private int spinStartTicks;
    private int spinLoopTicks;
    private int spinEndTicks;
    private int spinLoopTotalTicks;
    private int shootCooldown;
    private int jumpCooldown;
    private int spinCooldown;
    private int spinRicochetCooldown;
    private boolean jumpLaunched;
    private boolean jumpAirborne;
    private boolean shockwaveApplied;
    private int jumpShakeTicks;
    private int shootAttackType;
    private boolean shootBomberFired;
    private boolean shootBomberFiredSecond;
    private boolean shootBomberFiredThird;
    private int glimmerPulseTicks;
    private int glimmerAbandonTicks;
    private int rapidFireNextTick;
    private Vec3 spinDirection = Vec3.ZERO;
    private int spinStrafeSign = 1;
    private int spinStuckTicks;
    private Vec3 spinProgressAnchor = Vec3.ZERO;
    private int spinProgressTicks;
    private final Map<UUID, Integer> spinHitCooldowns = new HashMap<>();

    public ToreterrorEntity(EntityType<? extends ToreterrorEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = BASE_XP_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.toreterrorHealth())
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.325D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 4.0D);
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
            EntitySpawnReason spawnReason, net.minecraft.world.entity.SpawnGroupData spawnData) {
        double health = AntarchySettings.toreterrorHealth();
        if (this.isGlimmerEcho()) {
            health *= AntarchySettings.glimmerToreterrorHealthMultiplier();
        }
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, health);
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    public static boolean canSpawn(EntityType<ToreterrorEntity> entityType, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, net.minecraft.util.RandomSource random) {
        boolean sturdy = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        return level.getDifficulty() != Difficulty.PEACEFUL && sturdy;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(PHASE_2, false);
        builder.define(JUMP_SHAKING, false);
        builder.define(GLIMMER_ECHO, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ToreterrorFloatGoal());
        this.goalSelector.addGoal(1, new ToreterrorAttackGoal());
        this.goalSelector.addGoal(3, new ToreterrorChaseGoal());
        this.goalSelector.addGoal(4, new ToreterrorReturnGoal());
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.5D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, ToreterrorEntity.class).setAlertOthers().setUnseenMemoryTicks(TARGET_UNSEEN_MEMORY_TICKS));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (entity, level) -> this.canTargetEntity(entity)).setUnseenMemoryTicks(TARGET_UNSEEN_MEMORY_TICKS));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<ToreterrorEntity>("main_controller", 0, this::mainAnimController));
    }

    private PlayState mainAnimController(AnimationTest<ToreterrorEntity> state) {
        int animState = this.getAnimationState();
        state.controller().setAnimationSpeed(1.0D);
        return switch (animState) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_SHOOT -> state.setAndContinue(SHOOT_ANIM);
            case ANIM_RAPID_SHOOT -> state.setAndContinue(RAPID_SHOOT_ANIM);
            case ANIM_JUMP_ATTACK -> state.setAndContinue(JUMP_ANIM);
            case ANIM_SPIN_START -> {
                state.controller().setAnimationSpeed(SPIN_START_ANIMATION_SPEED);
                yield state.setAndContinue(SPIN_START_ANIM);
            }
            case ANIM_SWIPE_WINDUP -> {
                state.controller().setAnimationSpeed(SWIPE_WINDUP_ANIMATION_SPEED);
                yield state.setAndContinue(SWIPE_WINDUP_ANIM);
            }
            case ANIM_SPIN_LOOP -> {
                state.controller().setAnimationSpeed(SPIN_ANIMATION_SPEED);
                yield state.setAndContinue(SPIN_LOOP_ANIM);
            }
            case ANIM_SPIN_END -> state.setAndContinue(SPIN_END_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            if (this.isJumpShaking()) {
                com.craisinlord.anthology.shake.client.CameraShake.register(
                        this,
                        48.0D,
                        1.0F,
                        this::isJumpShaking
                );
            }
            return;
        }

        if (this.shootCooldown > 0) this.shootCooldown--;
        if (this.jumpCooldown > 0) this.jumpCooldown--;
        if (this.spinCooldown > 0) this.spinCooldown--;
        if (this.swipeCooldown > 0) this.swipeCooldown--;
        if (this.spinRicochetCooldown > 0) this.spinRicochetCooldown--;
        if (this.jumpShakeTicks > 0) {
            this.jumpShakeTicks--;
            if (this.jumpShakeTicks <= 0) {
                this.entityData.set(JUMP_SHAKING, false);
            }
        }

        this.spinHitCooldowns.replaceAll((uuid, cd) -> cd - 1);
        this.spinHitCooldowns.entrySet().removeIf(e -> e.getValue() <= 0);

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.isGlimmerEcho()) {
            GlimmerEchoes.tickAmbient(this, 3, 4);
            if (this.tickGlimmerAbandonment()) {
                return;
            }
            this.tickGlimmerPulse();
        }

        this.tickEncounter();

        if (!this.isPhase2() && this.phaseTransitionTicks <= 0 && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.beginPhaseTransition();
        }

        if (this.phaseTransitionTicks > 0) {
            this.tickPhaseTransition();
        } else if (this.shootAnimTicks > 0) {
            this.tickShootAnimation();
        } else if (this.jumpAnimTicks > 0) {
            this.tickJumpAnimation();
        } else if (this.isSpinSequenceActive()) {
            this.tickSpinSequence();
        } else if (this.swipeTicks > 0) {
            this.tickSwipe();
        } else if (this.recoveryTicks > 0) {
            this.tickRecovery();
        } else {
            this.updateIdleAnimation();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        if (source.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) return false;
        if (source.getEntity() instanceof BomberEntity || source.getDirectEntity() instanceof BomberEntity) return false;
        boolean bypass = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (!bypass) {
            if (this.phaseTransitionTicks > 0) {
                this.playRangeResistFeedback();
                return false;
            }
            double fullRange = AntarchySettings.toreterrorDamageRange();
            if (source.getEntity() == null && fullRange > 0.0D && !this.hasParticipantWithin(fullRange)) {
                return false;
            }
            float rangeMultiplier = BossCombatUtil.damageRangeMultiplier(this, source, fullRange, fullRange * RANGE_FALLOFF_MAX_RATIO);
            if (rangeMultiplier < 1.0F) {
                this.playRangeResistFeedback();
                if (rangeMultiplier <= 0.0F) {
                    return false;
                }
                amount *= rangeMultiplier;
            }
        }
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            amount *= (float) AntarchySettings.toreterrorProjectileDamageMultiplier();
        }
        return super.hurtServer(serverLevel, source, amount);
    }

    @Override
    protected void actuallyHurt(ServerLevel serverLevel, DamageSource source, float amount) {
        float preHealth = this.getHealth();
        super.actuallyHurt(serverLevel, source, amount);
        float halfHealth = this.getMaxHealth() * 0.5F;
        if (!this.isPhase2() && preHealth >= halfHealth && this.getHealth() < halfHealth && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.setHealth(halfHealth);
        }
    }

    private boolean hasParticipantWithin(double range) {
        double rangeSqr = range * range;
        for (Player player : this.level().players()) {
            if (player.isAlive() && !player.isCreative() && !player.isSpectator() && this.distanceToSqr(player) <= rangeSqr) {
                return true;
            }
        }
        return false;
    }

    private void playRangeResistFeedback() {
        if (this.tickCount - this.lastRangeFeedbackTick < RANGE_FEEDBACK_INTERVAL_TICKS) {
            return;
        }
        this.lastRangeFeedbackTick = this.tickCount;
        this.playSound(SoundEvents.SHIELD_BLOCK.value(), 1.5F, 0.6F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT), this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(),
                    10, this.getBbWidth() * 0.4D, this.getBbHeight() * 0.3D, this.getBbWidth() * 0.4D, 0.1D);
        }
    }

    private void beginPhaseTransition() {
        this.entityData.set(PHASE_2, true);
        this.clearActions();
        this.recoveryTicks = 0;
        this.phaseTransitionTicks = PHASE_TRANSITION_TICKS;
        this.getNavigation().stop();
        this.playSound(AntarchySoundEvents.TORETERROR_HURT.get(), 2.5F, 0.6F);
    }

    private void tickPhaseTransition() {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
        int elapsed = PHASE_TRANSITION_TICKS - this.phaseTransitionTicks;
        if (elapsed < PHASE_BLAST_TICK) {
            this.setAnimationState(ANIM_SWIPE_WINDUP);
            if (elapsed % 5 == 0) {
                this.sendGroundRing(this.position(), PHASE_BLAST_RADIUS * (elapsed + 5) / (double) PHASE_BLAST_TICK);
            }
        } else if (elapsed < PHASE_SPIN_END_TICK) {
            this.setAnimationState(ANIM_SPIN_LOOP);
        } else if (elapsed < PHASE_CLIP_END_TICK) {
            this.setAnimationState(ANIM_SPIN_END);
        } else {
            this.setAnimationState(ANIM_IDLE);
        }
        if (elapsed == PHASE_BLAST_TICK) {
            this.applyPhaseBlast();
        }
        this.phaseTransitionTicks--;
    }

    private void applyPhaseBlast() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        AABB area = this.getBoundingBox().inflate(PHASE_BLAST_RADIUS, 3.0D, PHASE_BLAST_RADIUS);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area, this::canTargetEntity)) {
            if (!this.hasLineOfSight(victim)) continue;
            Vec3 knockDir = this.horizontalDirectionTo(victim.position());
            victim.push(knockDir.x * 1.2D, 0.45D, knockDir.z * 1.2D);
            victim.syncVelocity = true;
        }
        this.sendGroundRing(this.position(), PHASE_BLAST_RADIUS);
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.6F);
        this.playSound(AntarchySoundEvents.TORETERROR_JUMP_LAND.get(), 1.5F, 0.7F);
        serverLevel.sendParticles(GlimmerEchoes.particle(this, net.minecraft.core.particles.ParticleTypes.SPLASH), this.getX(), this.getY() + 0.5D, this.getZ(), 40, 2.0D, 0.5D, 2.0D, 0.2D);
    }

    private void sendGroundRing(Vec3 center, double radius) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (this.isGlimmerEcho()) {
            GlimmerEchoes.ring(serverLevel, center, radius, 32);
            return;
        }
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6.0D;
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, center.x + Math.cos(angle) * radius, center.y + 0.2D, center.z + Math.sin(angle) * radius, 2, 0.1D, 0.05D, 0.1D, 0.0D);
        }
    }

    private void clearActions() {
        this.shootAnimTicks = 0;
        this.jumpAnimTicks = 0;
        this.swipeTicks = 0;
        this.jumpLandingPoint = null;
        this.stopSpinSequence();
    }

    private void tickRecovery() {
        this.setAnimationState(ANIM_IDLE);
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, 1.0D, 0.7D));
        this.recoveryTicks--;
    }

    private void beginRecovery(int ticks) {
        this.recoveryTicks = this.isPhase2() ? Math.max(1, (int) Math.round(ticks * PHASE_TWO_RECOVERY_MULTIPLIER)) : ticks;
    }

    private void tickEncounter() {
        if (this.isGlimmerEcho()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && this.canTargetEntity(target)) {
            if (!this.encounterActive) {
                this.encounterActive = true;
                this.encounterHome = this.blockPosition();
            }
            this.disengageTicks = 0;
            return;
        }
        if (!this.encounterActive || this.isBusy()) {
            return;
        }
        if (this.hasParticipantWithin(PARTICIPATION_RANGE)) {
            this.disengageTicks = 0;
            return;
        }
        BlockPos home = this.encounterHome;
        boolean displaced = home != null && this.distanceToSqr(Vec3.atBottomCenterOf(home)) > RETURN_ARRIVAL_RANGE_SQR;
        if (!this.needsReset() && !displaced) {
            this.encounterActive = false;
            this.disengageTicks = 0;
            return;
        }
        this.disengageTicks++;
        if (this.disengageTicks >= RESET_DELAY_TICKS) {
            this.resetEncounter();
        }
    }

    private boolean needsReset() {
        return this.isPhase2() || this.getHealth() < this.getMaxHealth();
    }

    private boolean shouldReturnHome() {
        return this.encounterActive
                && this.encounterHome != null
                && this.disengageTicks >= RETURN_DELAY_TICKS
                && this.getTarget() == null
                && !this.isBusy()
                && this.distanceToSqr(Vec3.atBottomCenterOf(this.encounterHome)) > RETURN_ARRIVAL_RANGE_SQR;
    }

    private void resetEncounter() {
        this.clearActions();
        this.recoveryTicks = 0;
        this.phaseTransitionTicks = 0;
        this.entityData.set(PHASE_2, false);
        this.shootCooldown = 0;
        this.jumpCooldown = 0;
        this.spinCooldown = 0;
        this.swipeCooldown = 0;
        this.previousAction = ACTION_NONE;
        this.encounterActive = false;
        this.disengageTicks = 0;
        this.getNavigation().stop();
        this.setHealth(this.getMaxHealth());
        this.sendResetEffects();
        BlockPos home = this.encounterHome;
        if (home != null && this.distanceToSqr(Vec3.atBottomCenterOf(home)) > RETURN_ARRIVAL_RANGE_SQR && this.level().hasChunkAt(home)) {
            Vec3 destination = Vec3.atBottomCenterOf(home);
            AABB box = this.getBoundingBox().move(destination.subtract(this.position()));
            if (this.level().noCollision(this, box)) {
                this.snapTo(destination.x, destination.y, destination.z, this.getYRot(), this.getXRot());
                this.setDeltaMovement(Vec3.ZERO);
                this.sendResetEffects();
            }
        }
        this.encounterHome = null;
    }

    private void sendResetEffects() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0D, this.getZ(), 40, 1.2D, 1.0D, 1.2D, 0.1D);
        this.playSound(AntarchySoundEvents.TORETERROR_JUMP_LAND.get(), 1.0F, 0.6F);
    }

    private boolean isBusy() {
        return this.isInAttackAnimation() || this.recoveryTicks > 0 || this.phaseTransitionTicks > 0;
    }

    private int selectAction(LivingEntity target) {
        if (this.shootCooldown > 0 && this.jumpCooldown > 0 && this.spinCooldown > 0 && this.swipeCooldown > 0) {
            return ACTION_NONE;
        }
        double distSqr = this.distanceToSqr(target);
        if (distSqr > RANGED_MAX_RANGE_SQR || !this.hasLineOfSight(target)) {
            return ACTION_NONE;
        }
        boolean grounded = this.onGround();
        boolean close = distSqr <= CLOSE_RANGE_SQR;
        int shootWeight = this.shootCooldown <= 0 && !close && (grounded || this.isInWater()) ? this.weightFor(ACTION_SHOOT, 5) : 0;
        int jumpWeight = this.jumpCooldown <= 0 && grounded && distSqr <= JUMP_RANGE_SQR
                && target.getY() - this.getY() <= JUMP_SELECT_MAX_ASCENT ? this.weightFor(ACTION_JUMP, close ? 2 : 5) : 0;
        int spinWeight = this.spinCooldown <= 0 && grounded && distSqr <= SPIN_TRIGGER_RANGE_SQR
                && Math.abs(target.getY() - this.getY()) <= SPIN_MAX_VERTICAL_OFFSET ? this.weightFor(ACTION_SPIN, 3) : 0;
        int swipeWeight = this.swipeCooldown <= 0 && grounded && distSqr <= SWIPE_TRIGGER_RANGE_SQR ? this.weightFor(ACTION_SWIPE, 8) : 0;
        int total = shootWeight + jumpWeight + spinWeight + swipeWeight;
        if (total <= 0) {
            return ACTION_NONE;
        }
        int roll = this.random.nextInt(total);
        if (roll < shootWeight) {
            return ACTION_SHOOT;
        }
        roll -= shootWeight;
        if (roll < jumpWeight) {
            return ACTION_JUMP;
        }
        roll -= jumpWeight;
        return roll < spinWeight ? ACTION_SPIN : ACTION_SWIPE;
    }

    private int weightFor(int action, int weight) {
        return action == this.previousAction ? Math.max(1, weight / REPEAT_WEIGHT_DIVISOR) : weight;
    }

    private void beginAction(int action, LivingEntity target) {
        this.previousAction = action;
        this.getNavigation().stop();
        switch (action) {
            case ACTION_SHOOT -> this.beginShoot();
            case ACTION_JUMP -> this.beginJump(target);
            case ACTION_SPIN -> this.startSpinSequence(target);
            case ACTION_SWIPE -> this.beginSwipe();
            default -> {
            }
        }
    }

    private void beginShoot() {
        this.shootAnimTicks = SHOOT_TOTAL_TICKS;
        this.shootBomberFired = false;
        this.shootBomberFiredSecond = false;
        this.shootBomberFiredThird = false;
        this.rapidFireNextTick = RAPID_FIRE_FIRST_TICK;
        this.shootAttackType = this.random.nextBoolean() ? ATTACK_RAPID_FIRE : ATTACK_BOMBERS;
    }

    private void beginJump(LivingEntity target) {
        this.jumpAnimTicks = JUMP_TOTAL_TICKS;
        this.jumpLaunched = false;
        this.jumpAirborne = false;
        this.shockwaveApplied = false;
        this.jumpFlightTicks = 0;
        this.jumpAirTicks = 0;
        this.jumpLandingPoint = this.computeLandingPoint(target);
        this.sendGroundRing(this.jumpLandingPoint, JUMP_SHOCKWAVE_RADIUS);
        this.playSound(AntarchySoundEvents.TORETERROR_JUMP_UP.get(), 1.2F, 0.9F + this.random.nextFloat() * 0.15F);
    }

    private void beginSwipe() {
        this.swipeTicks = SWIPE_TOTAL_TICKS;
        this.playSound(AntarchySoundEvents.TORETERROR_SPIN.get(), 1.0F, 0.6F);
    }

    private void tickSwipe() {
        int elapsed = SWIPE_TOTAL_TICKS - this.swipeTicks;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
        if (elapsed < SWIPE_WINDUP_TICKS) {
            this.setAnimationState(ANIM_SWIPE_WINDUP);
            if (elapsed % 5 == 0) {
                this.sendGroundRing(this.position(), SWIPE_RADIUS);
            }
        } else if (elapsed < SWIPE_WINDUP_TICKS + SWIPE_STRIKE_TICKS) {
            this.setAnimationState(ANIM_SPIN_LOOP);
        } else {
            this.setAnimationState(ANIM_SPIN_END);
        }
        if (elapsed == SWIPE_HIT_TICK) {
            this.applySwipeDamage();
        }
        this.swipeTicks--;
        if (this.swipeTicks <= 0) {
            this.swipeCooldown = this.scaledCooldown(120 + this.random.nextInt(40));
            this.beginRecovery(SWIPE_RECOVERY_TICKS);
        }
    }

    private void applySwipeDamage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        AABB area = this.getBoundingBox().inflate(SWIPE_RADIUS, SWIPE_HEIGHT, SWIPE_RADIUS);
        float damage = (float) AntarchySettings.toreterrorJumpAttackDamage() * SWIPE_DAMAGE_FRACTION * this.glimmerEchoDamageMultiplier();
        double knockback = AntarchySettings.toreterrorSpinKnockback() * SWIPE_KNOCKBACK_MULTIPLIER;
        this.playSound(AntarchySoundEvents.TORETERROR_RICOCHET.get(), 1.4F, 0.7F);
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class, area, this::canTargetEntity)) {
            if (this.horizontalDistanceTo(victim) > SWIPE_RADIUS || !this.hasLineOfSight(victim)) continue;
            if (victim.hurtServer(serverLevel, AntarchyDamageSources.toreterrorSpin(serverLevel, this), damage)) {
                Vec3 knockDir = this.horizontalDirectionTo(victim.position());
                victim.push(knockDir.x * knockback, 0.35D, knockDir.z * knockback);
                victim.syncVelocity = true;
            }
        }
    }

    private double horizontalDistanceTo(LivingEntity entity) {
        double dx = entity.getX() - this.getX();
        double dz = entity.getZ() - this.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    public boolean isPhase2() {
        return this.entityData.get(PHASE_2);
    }

    public boolean isJumpShaking() {
        return this.entityData.get(JUMP_SHAKING);
    }

    @Override
    public boolean isGlimmerEcho() {
        return this.entityData.get(GLIMMER_ECHO);
    }

    @Override
    public void setGlimmerEcho(boolean glimmerEcho) {
        this.entityData.set(GLIMMER_ECHO, glimmerEcho);
        this.xpReward = glimmerEcho ? GLIMMER_XP_REWARD : BASE_XP_REWARD;
        this.bossEvent.setName(this.getTypeName());
        this.bossEvent.setColor(glimmerEcho ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.GREEN);
    }

    @Override
    public float glimmerEchoDamageMultiplier() {
        return this.isGlimmerEcho() ? (float) AntarchySettings.glimmerToreterrorDamageMultiplier() : 1.0F;
    }

    @Override
    protected Component getTypeName() {
        return this.isGlimmerEcho() ? Component.translatable("entity.antarchy.toreterror.glimmer") : super.getTypeName();
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource damageSource, boolean causedByPlayer) {
        if (!this.isGlimmerEcho()) {
            super.dropFromLootTable(level, damageSource, causedByPlayer);
            return;
        }
        this.dropFromLootTable(level, damageSource, causedByPlayer, GLIMMER_LOOT_TABLE);
    }

    private int scaledCooldown(int ticks) {
        double scaled = ticks;
        if (this.isPhase2()) {
            scaled *= PHASE_TWO_COOLDOWN_MULTIPLIER;
        }
        if (this.isGlimmerEcho()) {
            scaled *= AntarchySettings.glimmerToreterrorCooldownMultiplier();
        }
        return Math.max(10, (int) Math.round(scaled));
    }

    private boolean tickGlimmerAbandonment() {
        if (this.tickCount % 20 != 0) {
            return false;
        }
        if (GlimmerEchoes.hasPlayerWithin(this, GLIMMER_ABANDON_RANGE)) {
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

    private void tickGlimmerPulse() {
        if (this.glimmerPulseTicks <= 0) {
            return;
        }
        this.glimmerPulseTicks--;
        if (this.level() instanceof ServerLevel serverLevel && this.glimmerPulseTicks % 5 == 0) {
            double progress = 1.0D - this.glimmerPulseTicks / (double) GLIMMER_PULSE_DELAY_TICKS;
            GlimmerEchoes.ring(serverLevel, this.position(), 2.0D + GLIMMER_PULSE_RADIUS * progress, 32);
        }
        if (this.glimmerPulseTicks == 0) {
            this.applyGlimmerPulse();
        }
    }

    private void applyGlimmerPulse() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        AABB pulseArea = this.getBoundingBox().inflate(GLIMMER_PULSE_RADIUS, 1.5D, GLIMMER_PULSE_RADIUS);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, pulseArea,
                entity -> this.canTargetEntity(entity) && entity.onGround() && this.hasLineOfSight(entity));
        float damage = (float) AntarchySettings.toreterrorJumpAttackDamage() * GLIMMER_PULSE_DAMAGE_FRACTION * this.glimmerEchoDamageMultiplier();
        for (LivingEntity victim : victims) {
            if (victim.hurtServer(serverLevel, AntarchyDamageSources.toreterrorJump(serverLevel, this), damage)) {
                Vec3 knockDir = victim.position().subtract(this.position()).normalize();
                victim.push(knockDir.x * 1.2D, 0.45D, knockDir.z * 1.2D);
                victim.syncVelocity = true;
            }
        }
        GlimmerEchoes.ring(serverLevel, this.position(), GLIMMER_PULSE_RADIUS + 1.0D, 48);
        this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0F, 0.6F);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return AntarchySoundEvents.TORETERROR_IDLE.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        return AntarchySoundEvents.TORETERROR_HURT.get();
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return AntarchySoundEvents.TORETERROR_DEATH.get();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Phase2", this.isPhase2());
        output.putInt("SwipeCooldown", this.swipeCooldown);
        output.putBoolean("EncounterActive", this.encounterActive);
        if (this.encounterHome != null) {
            output.putLong("EncounterHome", this.encounterHome.asLong());
        }
        output.putInt("ShootCooldown", this.shootCooldown);
        output.putInt("JumpCooldown", this.jumpCooldown);
        output.putInt("SpinCooldown", this.spinCooldown);
        output.putBoolean(GlimmerEchoes.SAVE_KEY, this.isGlimmerEcho());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(ANIMATION_STATE, ANIM_IDLE);
        this.entityData.set(PHASE_2, input.getBooleanOr("Phase2", false) || this.getHealth() <= this.getMaxHealth() * 0.5F);
        this.swipeCooldown = input.getIntOr("SwipeCooldown", 0);
        this.encounterActive = input.getBooleanOr("EncounterActive", false);
        this.encounterHome = input.getLong("EncounterHome").map(BlockPos::of).orElse(null);
        this.shootCooldown = input.getIntOr("ShootCooldown", 0);
        this.jumpCooldown = input.getIntOr("JumpCooldown", 0);
        this.spinCooldown = input.getIntOr("SpinCooldown", 0);
        this.setGlimmerEcho(input.getBooleanOr(GlimmerEchoes.SAVE_KEY, false));
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.setAnimationState(ANIM_DEATH);
            this.clearActions();
            this.recoveryTicks = 0;
            this.phaseTransitionTicks = 0;
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
        }
    }

    public int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int state) {
        if (this.entityData.get(ANIMATION_STATE) != state) {
            this.entityData.set(ANIMATION_STATE, state);
        }
    }

    private void updateIdleAnimation() {
        Vec3 vel = this.getDeltaMovement();
        boolean moving = (vel.x * vel.x + vel.z * vel.z) > 0.0004D;
        this.setAnimationState(moving ? ANIM_WALK : ANIM_IDLE);
    }

    private boolean isInAttackAnimation() {
        return this.shootAnimTicks > 0 || this.jumpAnimTicks > 0 || this.swipeTicks > 0 || this.isSpinSequenceActive();
    }

    private boolean isSpinSequenceActive() {
        return this.spinStartTicks > 0 || this.spinLoopTicks > 0 || this.spinEndTicks > 0;
    }

    private void tickShootAnimation() {
        LivingEntity shootTarget = this.getTarget();
        if (shootTarget == null || !shootTarget.isAlive()) {
            this.shootAnimTicks = 0;
            this.shootCooldown = this.scaledCooldown(100 + this.random.nextInt(60));
            this.beginRecovery(SHOOT_RECOVERY_TICKS);
            return;
        }
        int elapsed = SHOOT_TOTAL_TICKS - this.shootAnimTicks;
        switch (this.shootAttackType) {
            case ATTACK_RAPID_FIRE -> {
                this.setAnimationState(ANIM_RAPID_SHOOT);
                if (elapsed >= this.rapidFireNextTick && elapsed < RAPID_FIRE_END_TICK) {
                    this.rapidFireNextTick += this.isGlimmerEcho() ? GLIMMER_RAPID_FIRE_INTERVAL : RAPID_FIRE_INTERVAL;
                    this.fireRapidBomb();
                }
            }
            default -> {
                this.setAnimationState(ANIM_SHOOT);
                if (!this.shootBomberFired && elapsed >= SHOOT_BOMBER_TICK) {
                    this.shootBomberFired = true;
                    this.fireBomberBurst();
                }
                if ((this.isPhase2() || this.isGlimmerEcho()) && !this.shootBomberFiredSecond && elapsed >= SHOOT_BOMBER_SECOND_TICK) {
                    this.shootBomberFiredSecond = true;
                    this.fireBomberBurst();
                }
                if (this.isPhase2() && this.isGlimmerEcho() && !this.shootBomberFiredThird && elapsed >= GLIMMER_BOMBER_THIRD_TICK) {
                    this.shootBomberFiredThird = true;
                    this.fireBomberBurst();
                }
            }
        }
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.85D));
        this.turnToward(this.horizontalDirectionTo(shootTarget.position()), 6.0F);
        this.shootAnimTicks--;
        if (this.shootAnimTicks <= 0) {
            this.shootCooldown = this.scaledCooldown(100 + this.random.nextInt(60));
            this.beginRecovery(SHOOT_RECOVERY_TICKS);
        }
    }

    private void tickJumpAnimation() {
        this.setAnimationState(ANIM_JUMP_ATTACK);
        int elapsed = JUMP_TOTAL_TICKS - this.jumpAnimTicks;
        if (!this.jumpLaunched) {
            this.getNavigation().stop();
            Vec3 landing = this.jumpLandingPoint;
            if (landing == null) {
                this.finishJump();
                return;
            }
            this.turnToward(this.horizontalDirectionTo(landing), 12.0F);
            if (elapsed % 5 == 0) {
                this.sendGroundRing(landing, JUMP_SHOCKWAVE_RADIUS);
            }
            if (elapsed >= JUMP_LAUNCH_TICK) {
                this.launchJump(landing);
            }
        } else if (!this.shockwaveApplied) {
            this.tickJumpFlight();
        }
        this.jumpAnimTicks--;
        if (this.jumpAnimTicks <= 0) {
            this.finishJump();
        }
    }

    private Vec3 computeLandingPoint(LivingEntity target) {
        Vec3 horizontal = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
        double distance = horizontal.length();
        if (distance > JUMP_MAX_HORIZONTAL_RANGE) {
            horizontal = horizontal.scale(JUMP_MAX_HORIZONTAL_RANGE / distance);
        }
        double rise = Math.min(target.getY() - this.getY(), JUMP_MAX_ASCENT);
        return new Vec3(this.getX() + horizontal.x, this.getY() + rise, this.getZ() + horizontal.z);
    }

    private void launchJump(Vec3 landing) {
        this.jumpLaunched = true;
        this.jumpFlightTotalTicks = JUMP_FLIGHT_TICKS;
        this.jumpFlightTicks = 0;
        this.jumpAirTicks = 0;
        this.setDeltaMovement((landing.x - this.getX()) / JUMP_FLIGHT_TICKS, launchVelocityFor(landing.y - this.getY(), JUMP_FLIGHT_TICKS), (landing.z - this.getZ()) / JUMP_FLIGHT_TICKS);
        this.needsSync = true;
        this.sendGroundRing(landing, JUMP_SHOCKWAVE_RADIUS);
    }

    private static double launchVelocityFor(double rise, int ticks) {
        double fall = 0.0D;
        double fallVelocity = 0.0D;
        double reach = 0.0D;
        double factor = 1.0D;
        for (int i = 0; i < ticks; i++) {
            fall += fallVelocity;
            fallVelocity = (fallVelocity - GRAVITY_PER_TICK) * VERTICAL_DRAG;
            reach += factor;
            factor *= VERTICAL_DRAG;
        }
        return (rise - fall) / reach;
    }

    private void tickJumpFlight() {
        this.jumpFlightTicks++;
        this.jumpAirTicks++;
        if (!this.onGround() && !this.isInWater()) {
            this.jumpAirborne = true;
        } else if (this.jumpAirborne) {
            this.landJump();
            return;
        }
        if (this.jumpAirTicks > JUMP_MAX_AIR_TICKS) {
            this.landJump();
            return;
        }
        Vec3 landing = this.jumpLandingPoint;
        Vec3 velocity = this.getDeltaMovement();
        int ticksLeft = this.jumpFlightTotalTicks - this.jumpFlightTicks;
        if (landing != null && ticksLeft > 0) {
            this.setDeltaMovement((landing.x - this.getX()) / ticksLeft, velocity.y, (landing.z - this.getZ()) / ticksLeft);
            this.needsSync = true;
            if (this.jumpFlightTicks % 5 == 0) {
                this.sendGroundRing(landing, JUMP_SHOCKWAVE_RADIUS);
            }
        } else if (this.jumpFlightTicks >= this.jumpFlightTotalTicks + JUMP_FORCE_DOWN_GRACE_TICKS) {
            this.setDeltaMovement(velocity.x * 0.5D, Math.min(velocity.y, -1.5D), velocity.z * 0.5D);
            this.needsSync = true;
        } else {
            this.setDeltaMovement(velocity.x * 0.5D, velocity.y, velocity.z * 0.5D);
        }
        if (this.jumpAnimTicks <= 2) {
            this.jumpAnimTicks = 3;
        }
    }

    private void landJump() {
        this.shockwaveApplied = true;
        this.jumpLandingPoint = null;
        this.playSound(AntarchySoundEvents.TORETERROR_JUMP_LAND.get(), 1.2F, 0.9F + this.random.nextFloat() * 0.15F);
        this.applyJumpShockwave();
        if (this.isGlimmerEcho()) {
            this.glimmerPulseTicks = GLIMMER_PULSE_DELAY_TICKS;
        }
        this.jumpShakeTicks = JUMP_SHAKE_TICKS;
        this.entityData.set(JUMP_SHAKING, true);
    }

    private void finishJump() {
        this.jumpAnimTicks = 0;
        this.jumpLandingPoint = null;
        this.jumpCooldown = this.scaledCooldown(140 + this.random.nextInt(80));
        this.beginRecovery(JUMP_RECOVERY_TICKS);
    }

    private void tickSpinSequence() {
        this.getNavigation().stop();
        if (this.spinStartTicks > 0) {
            this.tickSpinStart();
            return;
        }
        if (this.spinLoopTicks > 0) {
            this.tickSpinLoop();
            return;
        }
        this.tickSpinEnd();
    }

    private void tickSpinStart() {
        this.setAnimationState(ANIM_SPIN_START);
        this.setDeltaMovement(this.getDeltaMovement().scale(0.75D));
        LivingEntity target = this.getTarget();
        if (target != null) {
            Vec3 towardTarget = horizontalDirectionTo(target.position());
            if (towardTarget.lengthSqr() > 1.0E-4D) {
                this.spinDirection = towardTarget;
                this.lookInDirection(towardTarget);
            }
        }
        this.spinStartTicks--;
    }

    private void tickSpinLoop() {
        this.setAnimationState(ANIM_SPIN_LOOP);
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.beginSpinEnd();
            return;
        }

        this.updateSpinDirection(target);
        if (!this.moveDuringSpin(target)) {
            this.spinStuckTicks++;
            if (this.spinStuckTicks >= SPIN_STUCK_FAIL_LIMIT) {
                this.beginSpinEnd();
                return;
            }
        } else {
            this.spinStuckTicks = 0;
        }

        this.spinProgressTicks++;
        if (this.spinProgressTicks >= 20) {
            if (this.position().distanceToSqr(this.spinProgressAnchor) < 4.0D) {
                this.beginSpinEnd();
                return;
            }
            this.spinProgressAnchor = this.position();
            this.spinProgressTicks = 0;
        }

        int elapsed = this.spinLoopTotalTicks - this.spinLoopTicks;
        if (elapsed % SPIN_HIT_INTERVAL == 0) {
            this.applySpinDamage();
        }

        this.lookInDirection(this.spinDirection);
        this.spinLoopTicks--;
        if (this.spinLoopTicks <= 0) {
            this.beginSpinEnd();
        }
    }

    private void tickSpinEnd() {
        this.setAnimationState(ANIM_SPIN_END);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, 1.0D, 0.7D));
        this.spinEndTicks--;
        if (this.spinEndTicks <= 0) {
            this.stopSpinSequence();
            this.spinCooldown = this.scaledCooldown(340 + this.random.nextInt(80));
            this.spinHitCooldowns.clear();
            this.beginRecovery(SPIN_RECOVERY_TICKS);
        }
    }

    private void updateSpinDirection(LivingEntity target) {
        Vec3 towardTarget = horizontalDirectionTo(target.position());
        if (towardTarget.lengthSqr() <= 1.0E-4D) {
            return;
        }
        if (this.distanceToSqr(target) > SPIN_LEASH_RANGE_SQR) {
            this.spinDirection = towardTarget;
            return;
        }
        Vec3 forward = this.spinDirection.lengthSqr() > 1.0E-4D ? this.spinDirection.normalize() : towardTarget;
        Vec3 combined = forward.scale(0.99D).add(towardTarget.scale(0.01D));
        this.spinDirection = combined.lengthSqr() > 1.0E-4D ? combined.normalize() : towardTarget;
    }

    private boolean moveDuringSpin(LivingEntity target) {
        Vec3 direction = this.spinDirection.lengthSqr() > 1.0E-4D ? this.spinDirection.normalize() : Vec3.ZERO;
        if (direction == Vec3.ZERO) {
            return false;
        }

        if (this.trySpinMoves(direction, false)) {
            return true;
        }

        this.playSpinRicochetSound();
        return this.trySpinRicochetTowardTarget(target, direction);
    }

    private void playSpinRicochetSound() {
        if (this.spinRicochetCooldown > 0) {
            return;
        }
        this.spinRicochetCooldown = 6;
        this.playSound(AntarchySoundEvents.TORETERROR_RICOCHET.get(), 1.0F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    private boolean trySpinRicochetTowardTarget(LivingEntity target, Vec3 fallbackDirection) {
        this.spinStrafeSign *= -1;
        Vec3 towardTarget = horizontalDirectionTo(target.position());
        if (towardTarget.lengthSqr() <= 1.0E-4D) {
            towardTarget = fallbackDirection;
        }

        if (this.trySpinMoves(towardTarget, true)) {
            this.spinDirection = towardTarget.normalize();
            return true;
        }

        Vec3 slightRight = rotateHorizontal(towardTarget, 18.0D);
        if (this.trySpinMoves(slightRight, true)) {
            this.spinDirection = towardTarget.normalize();
            return true;
        }

        Vec3 slightLeft = rotateHorizontal(towardTarget, -18.0D);
        if (this.trySpinMoves(slightLeft, true)) {
            this.spinDirection = towardTarget.normalize();
            return true;
        }

        return false;
    }

    private boolean trySpinMoves(Vec3 baseDirection, boolean bounced) {
        Vec3[] attempts = new Vec3[]{
                baseDirection,
                rotateHorizontal(baseDirection, 28.0D * this.spinStrafeSign),
                rotateHorizontal(baseDirection, -28.0D * this.spinStrafeSign),
                rotateHorizontal(baseDirection, 52.0D * this.spinStrafeSign),
                rotateHorizontal(baseDirection, -52.0D * this.spinStrafeSign)
        };
        for (Vec3 attempt : attempts) {
            if (this.trySpinMove(attempt, 0.0D, 0.0D) || this.trySpinMove(attempt, 1.0D, 0.42D) || this.trySpinMove(attempt, -1.0D, -0.28D)) {
                this.spinDirection = attempt.normalize();
                if (bounced) {
                    this.spinStrafeSign *= -1;
                }
                return true;
            }
        }
        return false;
    }

    private boolean trySpinMove(Vec3 direction, double yOffset, double verticalVelocity) {
        Vec3 horizontalMove = new Vec3(direction.x * SPIN_MOVE_SPEED, 0.0D, direction.z * SPIN_MOVE_SPEED);
        AABB movedBox = this.getBoundingBox().move(horizontalMove.x, yOffset, horizontalMove.z);
        if (!this.level().noCollision(this, movedBox)) {
            return false;
        }
        if (!this.hasSpinSupport(movedBox)) {
            return false;
        }

        this.setDeltaMovement(horizontalMove.x, verticalVelocity, horizontalMove.z);
        this.needsSync = true;
        return true;
    }

    private boolean hasSpinSupport(AABB box) {
        double probeY = box.minY - 0.2D;
        double[][] probes = new double[][]{
                {box.getCenter().x, box.getCenter().z},
                {box.minX + 0.25D, box.minZ + 0.25D},
                {box.minX + 0.25D, box.maxZ - 0.25D},
                {box.maxX - 0.25D, box.minZ + 0.25D},
                {box.maxX - 0.25D, box.maxZ - 0.25D}
        };

        for (double[] probe : probes) {
            BlockPos supportPos = BlockPos.containing(probe[0], probeY, probe[1]);
            if (this.level().loadedAndEntityCanStandOnFace(supportPos, this, Direction.UP)) {
                return true;
            }
            BlockPos lowerSupportPos = supportPos.below();
            if (this.level().loadedAndEntityCanStandOnFace(lowerSupportPos, this, Direction.UP)) {
                return true;
            }
        }
        return false;
    }

    private Vec3 horizontalDirectionTo(Vec3 targetPos) {
        Vec3 diff = targetPos.subtract(this.position());
        Vec3 horizontal = new Vec3(diff.x, 0.0D, diff.z);
        return horizontal.lengthSqr() > 1.0E-4D ? horizontal.normalize() : Vec3.ZERO;
    }

    private void lookInDirection(Vec3 direction) {
        this.turnToward(direction, 30.0F);
    }

    private void turnToward(Vec3 direction, float maxDegrees) {
        if (direction.lengthSqr() <= 1.0E-4D) {
            return;
        }
        float desired = (float) (net.minecraft.util.Mth.atan2(direction.z, direction.x) * (180.0D / Math.PI)) - 90.0F;
        float yaw = net.minecraft.util.Mth.approachDegrees(this.getYRot(), desired, maxDegrees);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
    }

    private static Vec3 rotateHorizontal(Vec3 direction, double degrees) {
        if (direction.lengthSqr() <= 1.0E-4D) {
            return Vec3.ZERO;
        }
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = direction.x * cos - direction.z * sin;
        double z = direction.x * sin + direction.z * cos;
        return new Vec3(x, 0.0D, z).normalize();
    }

    private void beginSpinEnd() {
        this.spinStartTicks = 0;
        this.spinLoopTicks = 0;
        if (this.spinEndTicks <= 0) {
            this.spinEndTicks = SPIN_END_TOTAL_TICKS;
        }
    }

    private void stopSpinSequence() {
        this.spinStartTicks = 0;
        this.spinLoopTicks = 0;
        this.spinEndTicks = 0;
        this.spinLoopTotalTicks = 0;
        this.spinDirection = Vec3.ZERO;
        this.spinStrafeSign = 1;
        this.spinStuckTicks = 0;
        this.spinProgressAnchor = Vec3.ZERO;
        this.spinProgressTicks = 0;
    }

    private void startSpinSequence(LivingEntity target) {
        this.spinStartTicks = SPIN_START_TOTAL_TICKS;
        this.spinLoopTotalTicks = SPIN_LOOP_MIN_TICKS + this.random.nextInt(SPIN_LOOP_MAX_TICKS - SPIN_LOOP_MIN_TICKS + 1);
        this.spinLoopTicks = this.spinLoopTotalTicks;
        this.spinEndTicks = 0;
        this.spinStuckTicks = 0;
        this.spinProgressAnchor = this.position();
        this.spinProgressTicks = 0;
        this.spinStrafeSign = this.random.nextBoolean() ? 1 : -1;
        this.spinHitCooldowns.clear();
        this.spinDirection = target == null ? Vec3.ZERO : this.horizontalDirectionTo(target.position());
        this.playSound(AntarchySoundEvents.TORETERROR_SPIN.get(), 0.66F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    private Vec3 calcLaunchVelocity(Vec3 from, Vec3 to, double minSpeed) {
        Vec3 diff = to.subtract(from);
        double dh = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        double dy = diff.y;
        double gravity = AntarchySettings.waterBombGravity();
        double t = Math.max(8.0, dh / minSpeed);
        double vx = diff.x / t;
        double vz = diff.z / t;
        double vy = dy / t + 0.5 * gravity * t;
        return new Vec3(vx, vy, vz);
    }

    private void fireRapidBomb() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;
        this.playSound(AntarchySoundEvents.WATER_CANNON_FIRE.get(), 0.8F, 0.9F + this.random.nextFloat() * 0.2F);
        Vec3 from = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 to = new Vec3(target.getX(), target.getEyeY(), target.getZ());
        Vec3 baseVel = calcLaunchVelocity(from, to, 1.5);
        int count = this.isPhase2() ? 2 : 1;
        for (int i = 0; i < count; i++) {
            double spread = count > 1 ? (i - 0.5) * 0.15 : 0.0;
            Vec3 vel = new Vec3(baseVel.x + spread, baseVel.y, baseVel.z - spread);
            WaterBombEntity bomb = new WaterBombEntity(this.level(), this);
            bomb.setPos(from.x, from.y, from.z);
            bomb.setDeltaMovement(vel);
            bomb.needsSync = true;
            GlimmerEchoes.markSummon(this, bomb);
            serverLevel.addFreshEntity(bomb);
        }
    }

    private void fireBomberBurst() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        LivingEntity target = this.getTarget();
        if (target == null) return;
        this.playSound(AntarchySoundEvents.TORETERROR_BOMBER_FIRE.get(), 1.1F, 0.9F + this.random.nextFloat() * 0.15F);
        int count = this.isGlimmerEcho() ? GLIMMER_BOMBER_COUNT : 3;
        for (int i = 0; i < count; i++) {
            BomberEntity bomber = AntarchyObjects.BOMBER.get().create(this.level(), EntitySpawnReason.MOB_SUMMONED);
            if (bomber == null) continue;
            double spreadAngle = (i - (count - 1) / 2.0D) * 20.0D;
            double yaw = Math.toRadians(this.getYRot() + spreadAngle);
            double vx = -Math.sin(yaw) * 1.2D;
            double vy = 0.4D;
            double vz = Math.cos(yaw) * 1.2D;
            bomber.snapTo(this.getX() + vx * 0.5D, this.getEyeY(), this.getZ() + vz * 0.5D, this.getYRot(), 0);
            bomber.setDeltaMovement(vx, vy, vz);
            bomber.setTarget(target);
            GlimmerEchoes.markSummon(this, bomber);
            serverLevel.addFreshEntity(bomber);
            bomber.primeFuse();
        }
    }

    private void applyJumpShockwave() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        AABB shockArea = this.getBoundingBox().inflate(JUMP_SHOCKWAVE_RADIUS, JUMP_SHOCKWAVE_HEIGHT, JUMP_SHOCKWAVE_RADIUS);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, shockArea, this::canTargetEntity);
        float damage = (float) AntarchySettings.toreterrorJumpAttackDamage() * this.glimmerEchoDamageMultiplier();
        double knockback = AntarchySettings.toreterrorJumpAttackKnockback();
        for (LivingEntity victim : victims) {
            double distance = this.horizontalDistanceTo(victim);
            if (distance > JUMP_SHOCKWAVE_RADIUS || !this.hasLineOfSight(victim)) continue;
            double outer = (distance - JUMP_SHOCKWAVE_FULL_RADIUS) / (JUMP_SHOCKWAVE_RADIUS - JUMP_SHOCKWAVE_FULL_RADIUS);
            float falloff = 1.0F - (1.0F - JUMP_SHOCKWAVE_MIN_DAMAGE_FRACTION) * (float) net.minecraft.util.Mth.clamp(outer, 0.0D, 1.0D);
            if (victim.hurtServer(serverLevel, AntarchyDamageSources.toreterrorJump(serverLevel, this), damage * falloff)) {
                Vec3 knockDir = victim.position().subtract(this.position()).normalize().add(0, 0.4D, 0);
                victim.push(knockDir.x * knockback, 0.5D, knockDir.z * knockback);
                victim.syncVelocity = true;
            }
        }
        this.playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.5F, 0.75F);
        if (this.isGlimmerEcho()) {
            GlimmerEchoes.burst(serverLevel, this.position().add(0.0D, 0.5D, 0.0D), 2.0D, 43);
            return;
        }
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 3, 1.0D, 0.3D, 1.0D, 0.0D);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH, this.getX(), this.getY() + 0.5D, this.getZ(), 40, 2.0D, 0.5D, 2.0D, 0.2D);
    }

    private void applySpinDamage() {
        if (!(this.level() instanceof ServerLevel)) return;
        AABB spinArea = this.getBoundingBox().inflate(SPIN_RADIUS, SPIN_RADIUS, SPIN_RADIUS);
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, spinArea, this::canTargetEntity);
        float damage = (float) AntarchySettings.toreterrorSpinDamage() * this.glimmerEchoDamageMultiplier();
        double knockback = AntarchySettings.toreterrorSpinKnockback();
        for (LivingEntity victim : victims) {
            UUID id = victim.getUUID();
            if (this.spinHitCooldowns.getOrDefault(id, 0) > 0 || !this.hasLineOfSight(victim)) continue;
            if (victim.hurtServer((ServerLevel) this.level(), AntarchyDamageSources.toreterrorSpin((ServerLevel) this.level(), this), damage)) {
                this.spinHitCooldowns.put(id, 20);
                Vec3 knockDir = victim.position().subtract(this.position()).normalize();
                victim.push(knockDir.x * knockback, 0.35D, knockDir.z * knockback);
                victim.syncVelocity = true;
            }
        }
    }

    private boolean canTargetEntity(@Nullable LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity == this || entity.getType() == this.getType()) return false;
        if (entity instanceof Player player) {
            return !player.isCreative() && !player.isSpectator() && this.level().getDifficulty() != Difficulty.PEACEFUL;
        }
        return entity instanceof Mob && entity.isAttackable();
    }

    private final class ToreterrorAttackGoal extends Goal {
        private int pendingAction = ACTION_NONE;

        ToreterrorAttackGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (ToreterrorEntity.this.isBusy() || target == null || !ToreterrorEntity.this.canTargetEntity(target)) return false;
            this.pendingAction = ToreterrorEntity.this.selectAction(target);
            return this.pendingAction != ACTION_NONE;
        }

        @Override
        public void start() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (target != null) {
                ToreterrorEntity.this.beginAction(this.pendingAction, target);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return ToreterrorEntity.this.isBusy();
        }

        @Override
        public void tick() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (target != null) {
                ToreterrorEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }

    private final class ToreterrorChaseGoal extends Goal {
        private int repathTicks;

        ToreterrorChaseGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            return !ToreterrorEntity.this.isBusy() && ToreterrorEntity.this.canTargetEntity(ToreterrorEntity.this.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void start() {
            this.repathTicks = 0;
        }

        @Override
        public void tick() {
            LivingEntity target = ToreterrorEntity.this.getTarget();
            if (target == null) {
                return;
            }
            ToreterrorEntity.this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (ToreterrorEntity.this.distanceToSqr(target) <= CHASE_STOP_RANGE_SQR) {
                ToreterrorEntity.this.getNavigation().stop();
                return;
            }
            if (--this.repathTicks <= 0) {
                boolean moving = ToreterrorEntity.this.getNavigation().moveTo(target, 1.0D);
                this.repathTicks = moving ? CHASE_REPATH_TICKS : CHASE_REPATH_FAIL_TICKS;
            }
        }

        @Override
        public void stop() {
            ToreterrorEntity.this.getNavigation().stop();
        }
    }

    private final class ToreterrorReturnGoal extends Goal {
        private int repathTicks;

        ToreterrorReturnGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return ToreterrorEntity.this.shouldReturnHome();
        }

        @Override
        public boolean canContinueToUse() {
            return ToreterrorEntity.this.shouldReturnHome();
        }

        @Override
        public void start() {
            this.repathTicks = 0;
        }

        @Override
        public void tick() {
            BlockPos home = ToreterrorEntity.this.encounterHome;
            if (home != null && --this.repathTicks <= 0) {
                ToreterrorEntity.this.getNavigation().moveTo(home.getX() + 0.5D, home.getY(), home.getZ() + 0.5D, 0.8D);
                this.repathTicks = RETURN_REPATH_TICKS;
            }
        }

        @Override
        public void stop() {
            ToreterrorEntity.this.getNavigation().stop();
        }
    }

    private final class ToreterrorFloatGoal extends Goal {
        ToreterrorFloatGoal() {
            this.setFlags(EnumSet.of(Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return ToreterrorEntity.this.isInWater() && !ToreterrorEntity.this.onGround();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            Vec3 vel = ToreterrorEntity.this.getDeltaMovement();
            ToreterrorEntity.this.setDeltaMovement(vel.x, Math.min(vel.y + 0.08D, 0.2D), vel.z);
        }
    }
}
