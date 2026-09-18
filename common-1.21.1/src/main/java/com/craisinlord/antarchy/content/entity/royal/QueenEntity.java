package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.entity.ManticoreEntity;
import com.craisinlord.antarchy.content.entity.nightmare.NightmareBiteEntity;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityTransition;
import com.craisinlord.antarchy.content.time.TimeDilationApi;
import com.craisinlord.antarchy.content.time.TimeDilationFieldEntity;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackLane;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalEffectController;
import com.craisinlord.antarchy.content.entity.portal.DimensionalTearEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class QueenEntity extends RoyalBossEntity {
    static final double TIME_FIELD_RADIUS_SCALE = 3.0D;
    private static final String SUMMON_COOLDOWN_KEY = "ManticoreSummonCooldownTicks";
    private static final int FAILED_SUMMON_RETRY_TICKS = 20;
    private static final int POSITION_ATTEMPTS_PER_MANTICORE = 8;

    private static final ResourceLocation ACCEL_SPEED_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "queen_royal_acceleration_speed");
    private static final ResourceLocation ACCEL_FLY_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "queen_royal_acceleration_fly");
    private static final DustParticleOptions ACCEL_DUST = new DustParticleOptions(new org.joml.Vector3f(1.0F, 0.15F, 0.15F), 2.0F);

    private static final int GRAVITY_STOMP_COOLDOWN = 180;
    private static final int MOMENTUM_LOCK_COOLDOWN = 420;
    private static final int MOMENTUM_LOCK_DURATION = 90;
    private static final double MOMENTUM_LOCK_RADIUS = 26.0D;
    private static final int CRUSHING_GRAVITY_COOLDOWN = 360;
    private static final int CRUSHING_GRAVITY_DURATION = 120;
    private static final double CRUSHING_GRAVITY_RADIUS = 18.0D;
    private static final double CRUSHING_GRAVITY_STRENGTH = 0.12D;
    private static final int ACCELERATION_COOLDOWN = 500;
    private static final int QUEEN_CHRONOSPHERE_COOLDOWN = 700;
    private static final int QUEEN_CHRONOSPHERE_DURATION = 200;
    private static final double QUEEN_CHRONOSPHERE_RADIUS = 20.0D;
    private static final int ACCELERATION_DURATION = 140;
    private static final int BLACK_HOLE_COOLDOWN = 340;
    private static final double QUEEN_BITE_COOLDOWN_MULTIPLIER = 1.5D;
    private static final int GRAVITY_STOMP_TELEGRAPH_TICKS = 22;
    private static final int BLACK_HOLE_TELEGRAPH_TICKS = 24;
    private static final int MOMENTUM_LOCK_TELEGRAPH_TICKS = 40;
    private static final int CRUSHING_GRAVITY_TELEGRAPH_TICKS = 28;
    private static final int ACCELERATION_TELEGRAPH_TICKS = 32;
    private static final int LANDING_TELEGRAPH_TICKS = 30;
    private static final int LANDING_IMPACT_TICKS = 18;
    private static final int GROUND_COMBAT_TICKS = 160;
    private static final int TAKEOFF_TICKS = 30;
    private static final int LANDING_COOLDOWN_MIN = 300;
    private static final int LANDING_COOLDOWN_VARIANCE = 120;
    private static final int BLACK_HOLE_FOLLOW_UP_DELAY = 22;
    private static final int CRUSHING_LANDING_DELAY = 40;
    private static final int MOMENTUM_VOLLEY_DELAY = MOMENTUM_LOCK_DURATION + 5;
    private static final int ACCELERATION_VOLLEY_DELAY = 18;
    private static final int ACCELERATION_LANDING_DELAY = 55;
    private static final int DIRECTED_LANDING_RETRY_TICKS = 60;

    private int manticoreSummonCooldownTicks;
    private int gravityStompCooldownTicks;
    private int momentumLockCooldownTicks;
    private int crushingGravityCooldownTicks;
    private int accelerationCooldownTicks;
    private int queenChronosphereCooldownTicks;
    private int blackHoleCooldownTicks;
    private int gravityStompTelegraphTicks;
    private int blackHoleTelegraphTicks;
    private int momentumLockTelegraphTicks;
    private int crushingGravityTelegraphTicks;
    private int accelerationTelegraphTicks;
    private int queenChronosphereTelegraphTicks;
    @Nullable
    private Vec3 pendingBlackHoleAnchor;
    private int queenLandingCooldownTicks = 300;
    private int queenLandingStageTicks;
    private QueenLandingStage queenLandingStage = QueenLandingStage.AERIAL;
    @Nullable
    private Vec3 queenLandingWaypoint;
    @Nullable
    private Phase pendingPhaseSignature;
    private QueenFollowUp queuedQueenFollowUp = QueenFollowUp.NONE;
    private int queuedQueenFollowUpTicks;
    private int queuedQueenLandingRetryTicks;
    private int directedFireballVolleyTicks;
    @Nullable
    private String lastQueenMajorAttack;
    private final int[] dreamFireballCooldownTicks = new int[3];
    private int idleWanderCooldownTicks;
    private boolean manticoreAnimationPending;
    private final RoyalEffectController royalEffects = new RoyalEffectController();
    private final Map<UUID, Vec3> frozenVelocities = new HashMap<>();
    private long naturalTrailSiteId = Long.MIN_VALUE;
    @Nullable
    private BlockPos naturalTrailHome;

    private enum QueenLandingStage {
        AERIAL,
        TELEGRAPH,
        DESCENT,
        IMPACT,
        GROUND_COMBAT,
        TAKEOFF
    }

    private enum QueenFollowUp {
        NONE,
        FIREBALL_VOLLEY,
        LANDING,
        ACCELERATION_VOLLEY
    }

    public QueenEntity(EntityType<? extends QueenEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.queenHealth(), AntarchySettings.queenAttackDamage());
    }

    @Override
    protected String geoName() {
        return "queen";
    }

    @Override
    protected boolean isFlyingBoss() {
        return true;
    }

    @Override
    protected double groundCombatBias() {
        return 0.72D;
    }

    @Override
    protected boolean managesOwnCombatLocomotion() {
        return true;
    }

    @Override
    protected boolean blocksRoyalAttacksForMovement() {
        return this.queenLandingStage == QueenLandingStage.TELEGRAPH
                || this.queenLandingStage == QueenLandingStage.DESCENT
                || this.queenLandingStage == QueenLandingStage.IMPACT
                || this.queenLandingStage == QueenLandingStage.TAKEOFF
                || this.isQueenAbilityTelegraphActive();
    }

    @Override
    protected boolean blocksRoyalBeamScheduling() {
        return this.directedFireballVolleyTicks > 0;
    }

    @Override
    protected void onRoyalPhaseTransitionStarted(Phase previousPhase, Phase nextPhase) {
        this.resetQueenLandingSequence();
        this.clearQueenAbilityTelegraphs();
        this.clearQueenAttackDirector();
        this.pendingPhaseSignature = null;
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), nextPhase == Phase.THREE ? 0.66F : 0.82F);
        if (this.level() instanceof ServerLevel level) {
            Component title = Component.translatable(nextPhase == Phase.TWO
                            ? "boss.antarchy.queen.phase_two" : "boss.antarchy.queen.phase_three")
                    .withStyle(style -> style.withColor(nextPhase == Phase.TWO
                            ? ChatFormatting.DARK_PURPLE : ChatFormatting.RED).withBold(true));
            for (ServerPlayer player : this.royalEncounterPlayers(level)) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 45, 10));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
            }
        }
    }

    @Override
    protected void tickRoyalPhaseTransition(Phase nextPhase, int elapsedTicks, int remainingTicks) {
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.7D));
        if (this.level() instanceof ServerLevel level && elapsedTicks % 3 == 0) {
            level.sendParticles(nextPhase == Phase.THREE ? ACCEL_DUST : ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                    20, this.getBbWidth() * 0.25D, this.getBbHeight() * 0.25D,
                    this.getBbWidth() * 0.25D, 0.08D);
        }
    }

    @Override
    protected void onRoyalPhaseTransitionCompleted(Phase nextPhase) {
        if (nextPhase == Phase.TWO) {
            this.momentumLockCooldownTicks = 0;
            this.pendingPhaseSignature = Phase.TWO;
            this.queenLandingCooldownTicks = Math.min(this.queenLandingCooldownTicks, 100);
        } else if (nextPhase == Phase.THREE) {
            this.accelerationCooldownTicks = 0;
            this.pendingPhaseSignature = Phase.THREE;
            this.queenLandingCooldownTicks = 0;
        }
    }

    @Override
    protected double biteApproachSpeed() {
        return 1.55D;
    }

    @Override
    protected double combatStandoffDistance() {
        return Math.min(super.combatStandoffDistance(), 14.0D);
    }

    @Override
    protected int maxConcurrentHeadAttacks(Phase phase) {
        return switch (phase) {
            case ONE, TWO -> 1;
            case THREE -> 2;
        };
    }

    @Override
    protected int biteCooldownTicks(Phase phase) {
        return Math.max(10, (int) Math.ceil(super.biteCooldownTicks(phase) * QUEEN_BITE_COOLDOWN_MULTIPLIER));
    }

    protected void registerRoyalTargetGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, KingEntity.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false, this::canAttack));
    }

    @Override
    protected int selectBeamVolleyLimit(Phase phase) {
        return switch (phase) {
            case ONE -> AntarchySettings.queenBeamPhaseOneCap();
            case TWO -> AntarchySettings.queenBeamPhaseTwoCap();
            case THREE -> this.random.nextDouble() < AntarchySettings.queenBeamPhaseThreeMaxVolleyChance()
                    ? AntarchySettings.queenBeamPhaseThreeCap()
                    : Math.min(2, AntarchySettings.queenBeamPhaseThreeCap());
        };
    }

    @Override
    protected boolean shouldDistributeHeadTargets(LivingEntity primaryTarget) {
        return !(primaryTarget instanceof KingEntity);
    }

    public boolean canAttack(LivingEntity target) {
        return !target.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK)
                && super.canAttack(target);
    }

    public boolean canDamageWithRoyalAttack(LivingEntity target) {
        return target != this
                && target.isAlive()
                && (!(target instanceof Player player) || !player.isCreative() && !player.isSpectator())
                && !target.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK)
                && !this.isAlliedTo(target);
    }

    @Override
    protected SoundEvent royalFlyLoopSound() {
        return AntarchySoundEvents.QUEEN_FLY_LOOP.get();
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.PURPLE;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.QUEEN_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.QUEEN_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.QUEEN_DEATH.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.QUEEN_BITE.get();
    }

    @Override
    protected SoundEvent royalBeamShootSound() { return AntarchySoundEvents.QUEEN_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound() { return AntarchySoundEvents.QUEEN_BEAM_START.get(); }

    @Override
    protected SoundEvent royalBeamLoopSound() { return AntarchySoundEvents.QUEEN_BEAM_LOOP.get(); }

    @Override
    protected SoundEvent royalBeamEndSound() { return AntarchySoundEvents.QUEEN_BEAM_END.get(); }

    @Override
    protected RoyalBeamSettings royalBeamSettings() {
        return new RoyalBeamSettings(AntarchySettings.queenBeamRange(), AntarchySettings.queenBeamTracking(), 7.5D,
                AntarchySettings.queenBeamDurationTicks(), AntarchySettings.queenBeamTravelTicks(), AntarchySettings.queenBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.queenBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.queenBeamTerrainRadius(), 4.0F, AntarchySettings.queenBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.DESTROY;
    }

    @Override
    protected RoyalBeamElement royalBeamElement(@Nullable RoyalHead head) {
        if (head == null) {
            return RoyalBeamElement.QUEEN_BLACK;
        }
        return switch (head.slot()) {
            case LEFT -> RoyalBeamElement.QUEEN_PURPLE;
            case CENTER -> RoyalBeamElement.QUEEN_BLACK;
            case RIGHT -> RoyalBeamElement.QUEEN_RED;
        };
    }

    public static boolean isFieldImmune(Entity entity) {
        return entity.getType().is(AntarchyTags.Entities.QUEEN_DOES_NOT_ATTACK);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isDeadOrDying()) {
            return;
        }
        if (this.isRoyalPhaseTransitionActive()) {
            this.royalEffects.tick();
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) {
            this.clearQueenAttackDirector();
            this.resetQueenLandingSequence();
            this.tickIdleWander();
        } else {
            this.tickQueenAttackDirector(target);
            this.tickQueenLandingSequence(target);
        }
        this.tickQueenAbilityTelegraphs();
        if (this.blocksRoyalAttacksForMovement()) {
            this.royalEffects.tick();
            return;
        }
        this.tickManticoreSummon();
        this.tickPendingManticoreAnimation();
        this.tickQueenAbilities();
        if (this.isQueenAbilityTelegraphActive()) {
            this.royalEffects.tick();
            return;
        }
        this.tickDreamFireballs();
        this.tickDirectedFireballVolley();
        this.tickFinalAcceleration();
        this.royalEffects.tick();
    }

    private void tickQueenLandingSequence(LivingEntity target) {
        if (this.queenLandingStage == QueenLandingStage.AERIAL && this.isRoyalFlying()
                && !this.isQueenAbilityTelegraphActive()
                && this.queenLandingCooldownTicks-- <= 0 && target.onGround()) {
            this.beginQueenLanding(target);
        }

        switch (this.queenLandingStage) {
            case TELEGRAPH -> {
                this.getNavigation().stop();
                this.setDeltaMovement(this.getDeltaMovement().scale(0.72D));
                this.tickQueenLandingIndicator();
                if (--this.queenLandingStageTicks <= 0) {
                    this.queenLandingStage = QueenLandingStage.DESCENT;
                    this.playRoyalSound(AntarchySoundEvents.QUEEN_FLY_LOOP.get(), 0.72F);
                }
            }
            case DESCENT -> {
                Vec3 landing = this.queenLandingWaypoint;
                if (landing == null) {
                    this.resetQueenLandingSequence();
                    return;
                }
                this.setRoyalFlying(true);
                this.getMoveControl().setWantedPosition(landing.x, landing.y, landing.z, 1.15D);
                this.tickQueenLandingIndicator();
                boolean reachedLanding = this.onGround() || this.position().distanceToSqr(landing) <= 2.25D;
                if (reachedLanding) {
                    this.setRoyalFlying(false);
                    this.queenLandingStage = QueenLandingStage.IMPACT;
                    this.queenLandingStageTicks = LANDING_IMPACT_TICKS;
                    this.performGravityStomp((ServerLevel) this.level(), target);
                }
            }
            case IMPACT -> {
                this.getNavigation().stop();
                this.setDeltaMovement(Vec3.ZERO);
                if (--this.queenLandingStageTicks <= 0) {
                    this.queenLandingStage = QueenLandingStage.GROUND_COMBAT;
                    this.queenLandingStageTicks = GROUND_COMBAT_TICKS;
                    this.queenLandingWaypoint = null;
                }
            }
            case GROUND_COMBAT -> {
                if (--this.queenLandingStageTicks <= 0) {
                    this.beginQueenTakeoff();
                }
            }
            case TAKEOFF -> {
                if (--this.queenLandingStageTicks <= 0) {
                    this.queenLandingStage = QueenLandingStage.AERIAL;
                    this.queenLandingCooldownTicks = LANDING_COOLDOWN_MIN
                            + this.random.nextInt(LANDING_COOLDOWN_VARIANCE + 1);
                }
            }
            case AERIAL -> {
            }
        }
    }

    private void beginQueenLanding(LivingEntity target) {
        this.queenLandingWaypoint = this.findQueenLandingPoint(target);
        if (this.queenLandingWaypoint == null) {
            this.queenLandingCooldownTicks = 100;
            return;
        }
        this.queenLandingStage = QueenLandingStage.TELEGRAPH;
        this.queenLandingStageTicks = LANDING_TELEGRAPH_TICKS;
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 1.08F);
    }

    @Nullable
    private Vec3 findQueenLandingPoint(LivingEntity target) {
        Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        double baseAngle = away.lengthSqr() > 1.0E-4D
                ? Math.atan2(away.z, away.x) : this.random.nextDouble() * Mth.TWO_PI;
        boolean inverted = AntarchyGravityApi.isGravityInverted(this);
        double[] radii = {10.0D, 14.0D, 18.0D, 7.0D};
        for (int angleIndex = 0; angleIndex < 12; angleIndex++) {
            double angle = baseAngle + angleIndex * Mth.TWO_PI / 12.0D;
            for (double radius : radii) {
                double x = target.getX() + Math.cos(angle) * radius;
                double z = target.getZ() + Math.sin(angle) * radius;
                double y = inverted
                        ? target.position().y - 0.1D
                        : this.groundYBelow(x, z) + 0.1D;
                Vec3 candidate = new Vec3(x, y, z);
                AABB box = this.getBoundingBox().move(
                        candidate.x - this.getX(), candidate.y - this.getY(), candidate.z - this.getZ());
                if (this.level().noCollision(this, box)) {
                    return candidate;
                }
            }
        }
        double fallbackY = inverted
                ? target.position().y - 0.1D
                : this.groundYBelow(target.getX(), target.getZ()) + 0.1D;
        Vec3 fallback = new Vec3(target.getX(), fallbackY, target.getZ());
        AABB fallbackBox = this.getBoundingBox().move(
                fallback.x - this.getX(), fallback.y - this.getY(), fallback.z - this.getZ());
        if (this.level().noCollision(this, fallbackBox)) {
            return fallback;
        }
        return null;
    }

    private void tickQueenLandingIndicator() {
        if (!(this.level() instanceof ServerLevel level) || this.queenLandingWaypoint == null
                || this.tickCount % 2 != 0) {
            return;
        }
        double progress = this.queenLandingStage == QueenLandingStage.TELEGRAPH
                ? 1.0D - this.queenLandingStageTicks / (double) LANDING_TELEGRAPH_TICKS : 1.0D;
        double radius = Mth.lerp(progress, 4.0D, 18.0D);
        Vec3 indicator = this.queenLandingWaypoint.add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D, 0.2D, 0.0D, AntarchyGravityApi.getGravityDirection(this)));
        for (int index = 0; index < 40; index++) {
            double angle = Mth.TWO_PI * index / 40.0D;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.queenLandingWaypoint.x + Math.cos(angle) * radius,
                    indicator.y,
                    this.queenLandingWaypoint.z + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void beginQueenTakeoff() {
        this.queenLandingStage = QueenLandingStage.TAKEOFF;
        this.queenLandingStageTicks = TAKEOFF_TICKS;
        this.triggerAnim("wing_action", "wing_gust");
        this.playRoyalSound(AntarchySoundEvents.QUEEN_FLY_LOOP.get(), 0.9F);
        if (this.level() instanceof ServerLevel level) {
            this.spawnQueenWingPressure(level);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    this.getX(), this.getY() + this.getBbHeight() * 0.4D, this.getZ(),
                    60, 7.0D, 1.0D, 7.0D, 0.12D);
        }
        this.setRoyalFlying(true);
        this.addGravityAwareImpulse(this, AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D, 0.42D, 0.0D, AntarchyGravityApi.getGravityDirection(this)));
        this.hasImpulse = true;
    }

    private void resetQueenLandingSequence() {
        if (this.queenLandingStage != QueenLandingStage.AERIAL) {
            this.setRoyalFlying(true);
        }
        this.queenLandingStage = QueenLandingStage.AERIAL;
        this.queenLandingStageTicks = 0;
        this.queenLandingWaypoint = null;
        if (this.queenLandingCooldownTicks <= 0) {
            this.queenLandingCooldownTicks = 100;
        }
    }

    private void tickDreamFireballs() {
        if (this.isRoyalRecoveryActive()) return;
        if (!(this.level() instanceof ServerLevel level)) return;
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) return;
        for (RoyalHead head : new RoyalHead[] {this.royalHead(RoyalHead.Slot.LEFT), this.royalHead(RoyalHead.Slot.CENTER), this.royalHead(RoyalHead.Slot.RIGHT)}) {
            int index = head.slot().ordinal();
            if (this.dreamFireballCooldownTicks[index] > 0) {
                this.dreamFireballCooldownTicks[index]--;
                continue;
            }
            double minimumRange = this.directedFireballVolleyTicks > 0 ? 6.0D : 12.0D;
            double maximumRange = this.directedFireballVolleyTicks > 0 ? 64.0D : 52.0D;
            if (!head.readyToAttack() || this.distanceTo(target) < minimumRange
                    || this.distanceTo(target) > maximumRange) continue;
            int cooldown = this.cooldown(180);
            RoyalHead.Slot slot = head.slot();
            LivingEntity fireballTarget = target;
            if (this.directedFireballVolleyTicks > 0) {
                LivingEntity assignedTarget = head.target(level);
                if (assignedTarget != null && this.canAttack(assignedTarget)) {
                    fireballTarget = assignedTarget;
                }
            }
            final LivingEntity committedTarget = fireballTarget;
            if (!this.beginRoyalAttack("dream_fireball_" + slot.name(), this.headLane(head), cooldown, 12, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            head.startShoot();
                            QueenEntity.this.triggerAnim(slot.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            Vec3 origin = QueenEntity.this.headAnchor(head);
                            Vec3 direction = committedTarget.getEyePosition().subtract(origin).normalize();
                            RoyalElementalProjectileEntity projectile = RoyalElementalProjectileEntity.create(
                                    level, QueenEntity.this, RoyalBeamElement.DREAM_FIRE, origin, direction);
                            level.addFreshEntity(projectile);
                            QueenEntity.this.playRoyalSound(AntarchySoundEvents.QUEEN_BEAM_SHOOT.get(), 0.9F);
                        }
                        @Override public void onComplete() {
                            head.stopShoot();
                        }
                    })) {
                this.dreamFireballCooldownTicks[index] = cooldown;
            }
        }
    }

    private void tickQueenAttackDirector(LivingEntity target) {
        if (this.queuedQueenFollowUp == QueenFollowUp.NONE) {
            return;
        }
        if (this.queuedQueenFollowUpTicks-- > 0) {
            return;
        }
        QueenFollowUp followUp = this.queuedQueenFollowUp;
        switch (followUp) {
            case FIREBALL_VOLLEY -> {
                this.clearQueuedQueenFollowUp();
                this.primeDirectedFireballVolley();
            }
            case LANDING -> {
                if (this.queenLandingStage == QueenLandingStage.AERIAL
                        && this.isRoyalFlying() && target.onGround()) {
                    this.clearQueuedQueenFollowUp();
                    this.queenLandingCooldownTicks = 0;
                } else if (--this.queuedQueenLandingRetryTicks <= 0) {
                    this.clearQueuedQueenFollowUp();
                } else {
                    this.queuedQueenFollowUpTicks = 0;
                }
            }
            case ACCELERATION_VOLLEY -> {
                this.clearQueuedQueenFollowUp();
                this.primeDirectedFireballVolley();
                this.queueQueenFollowUp(QueenFollowUp.LANDING, ACCELERATION_LANDING_DELAY);
            }
            case NONE -> {
            }
        }
    }

    private void queueQueenFollowUp(QueenFollowUp followUp, int delayTicks) {
        this.queuedQueenFollowUp = followUp;
        this.queuedQueenFollowUpTicks = Math.max(0, delayTicks);
        this.queuedQueenLandingRetryTicks = followUp == QueenFollowUp.LANDING
                ? DIRECTED_LANDING_RETRY_TICKS : 0;
    }

    private void clearQueuedQueenFollowUp() {
        this.queuedQueenFollowUp = QueenFollowUp.NONE;
        this.queuedQueenFollowUpTicks = 0;
        this.queuedQueenLandingRetryTicks = 0;
    }

    private void clearQueenAttackDirector() {
        this.clearQueuedQueenFollowUp();
        this.directedFireballVolleyTicks = 0;
    }

    private void primeDirectedFireballVolley() {
        this.directedFireballVolleyTicks = 60;
        this.stopRoyalBeamsForDirectedAttack();
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            int index = slot.ordinal();
            this.dreamFireballCooldownTicks[index] = 0;
            this.attackScheduler.resetCooldown("dream_fireball_" + slot.name());
        }
    }

    private void tickDirectedFireballVolley() {
        if (this.directedFireballVolleyTicks <= 0 || this.isRoyalRecoveryActive()) {
            return;
        }
        boolean allCommitted = true;
        for (int cooldown : this.dreamFireballCooldownTicks) {
            if (cooldown <= 0) {
                allCommitted = false;
                break;
            }
        }
        if (allCommitted || --this.directedFireballVolleyTicks <= 0) {
            this.directedFireballVolleyTicks = 0;
        }
    }

    private void tickIdleWander() {
        if (this.idleWanderCooldownTicks-- > 0) {
            return;
        }
        this.idleWanderCooldownTicks = 80 + this.random.nextInt(80);
        if (this.naturalTrailHome != null) {
            double distanceSquared = this.distanceToSqr(
                    this.naturalTrailHome.getX() + 0.5D,
                    this.naturalTrailHome.getY() + 0.5D,
                    this.naturalTrailHome.getZ() + 0.5D
            );
            if (distanceSquared > 48.0D * 48.0D) {
                this.getMoveControl().setWantedPosition(
                        this.naturalTrailHome.getX() + 0.5D,
                        this.naturalTrailHome.getY() + 0.5D,
                        this.naturalTrailHome.getZ() + 0.5D,
                        1.0D
                );
                return;
            }
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double radius = 6.0D + this.random.nextDouble() * 22.0D;
            double wx = this.naturalTrailHome.getX() + 0.5D + Math.cos(angle) * radius;
            double wz = this.naturalTrailHome.getZ() + 0.5D + Math.sin(angle) * radius;
            double wy = this.naturalTrailHome.getY() - 2.0D - this.random.nextDouble() * 7.0D;
            this.getMoveControl().setWantedPosition(wx, wy, wz, 0.7D);
            return;
        }
        double angle = this.random.nextDouble() * Mth.TWO_PI;
        double radius = 6.0D + this.random.nextDouble() * 14.0D;
        double wx = this.getX() + Math.cos(angle) * radius;
        double wz = this.getZ() + Math.sin(angle) * radius;
        this.getMoveControl().setWantedPosition(wx, this.groundYBelow(wx, wz), wz, 0.7D);
    }

    @Nullable
    public static QueenEntity spawnFromUndersideTrail(ServerLevel level, BlockPos spawnPos, BlockPos homePos, long siteId, float yaw) {
        if (!ThoraxisUndersideManager.isThoraxis(level) || spawnPos.getY() >= 0) {
            return null;
        }
        AABB siteBounds = new AABB(homePos).inflate(160.0D);
        java.util.List<QueenEntity> existing = level.getEntitiesOfClass(QueenEntity.class, siteBounds,
                queen -> queen.naturalTrailSiteId == siteId && queen.isAlive());
        if (!existing.isEmpty()) {
            return existing.getFirst();
        }
        if (!level.hasChunksAt(spawnPos.offset(-16, -2, -16), spawnPos.offset(16, 18, 16))) {
            return null;
        }
        QueenEntity queen = AntarchyObjects.QUEEN.get().create(level);
        if (queen == null) {
            return null;
        }
        queen.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        if (!level.noCollision(queen, queen.getBoundingBox())) {
            return null;
        }
        queen.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.CHUNK_GENERATION, null);
        queen.naturalTrailSiteId = siteId;
        queen.naturalTrailHome = homePos.immutable();
        queen.setPersistenceRequired();
        queen.setNoGravity(false);
        ThoraxisUndersideManager.applyUndersideInversion(queen);
        AntarchyGravityApi.setAirborneGravityDirection(queen, AntarchyGravityDirection.UP, true, new AntarchyGravityTransition(12));
        queen.resetFallDistance();
        return level.addFreshEntity(queen) ? queen : null;
    }

    private int cooldown(int base) {
        return this.royalEffects.active("acceleration") ? Math.max(20, base / 2) : base;
    }

    private void tickManticoreSummon() {
        if (this.isRoyalRecoveryActive()) return;
        if (this.manticoreSummonCooldownTicks > 0) {
            this.manticoreSummonCooldownTicks--;
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target)) {
            return;
        }

        int currentCount = ManticoreEntity.countSummonedBy(serverLevel, this.getUUID());
        int cap = AntarchySettings.queenManticoreCap();
        if (currentCount >= cap) {
            this.manticoreSummonCooldownTicks = FAILED_SUMMON_RETRY_TICKS;
            return;
        }

        int summonCount = Math.min(AntarchySettings.queenManticoreSummonCount(), cap - currentCount);
        int spawned = this.spawnManticoreTears(serverLevel, target, summonCount);

        this.manticoreSummonCooldownTicks = spawned > 0
                ? this.cooldown(Math.max(20, AntarchySettings.queenManticoreSummonCooldownTicks()))
                : FAILED_SUMMON_RETRY_TICKS;
        if (spawned > 0) {
            this.startRoyalRecovery(45);
            this.manticoreAnimationPending = true;
            this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.8F + this.random.nextFloat() * 0.12F);
            serverLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 2.0D, this.getZ(),
                    36, 3.0D, 2.0D, 3.0D, 0.15D);
        }
    }

    private void tickPendingManticoreAnimation() {
        if (!this.manticoreAnimationPending
                || !this.attackScheduler.ready("manticore_summon_animation", RoyalAttackLane.BODY)) {
            return;
        }
        if (this.beginRoyalAttack("manticore_summon_animation", RoyalAttackLane.BODY, 1, 0, 1,
                this.animationRecovery(59, 0, 1, 0),
                new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override public void onStart() {
                        QueenEntity.this.manticoreAnimationPending = false;
                        QueenEntity.this.triggerAnim("body_action", "minion_spawn");
                    }
                })) {
            this.manticoreAnimationPending = false;
        }
    }

    private int spawnManticoreTears(ServerLevel level, LivingEntity target, int count) {
        int pairs = Math.max(1, Math.min(3, (count + 1) / 2));
        int created = 0;
        int remaining = count;
        Vec3 towardTarget = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
        if (towardTarget.lengthSqr() < 1.0E-4D) {
            towardTarget = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
        }
        towardTarget = towardTarget.normalize();
        Vec3 behindTarget = target.position().add(towardTarget.scale(8.0D));
        Vec3 lateral = new Vec3(-towardTarget.z, 0.0D, towardTarget.x);
        float tearYaw = (float) Math.toDegrees(Math.atan2(-towardTarget.x, towardTarget.z));
        double vertical = AntarchyGravityApi.isGravityInverted(this) ? -2.0D : 2.0D;
        for (int i = 0; i < pairs; i++) {
            double forwardScatter = 3.0D + this.random.nextDouble() * 9.0D;
            double lateralScatter = (this.random.nextDouble() * 2.0D - 1.0D) * 11.0D;
            double heightScatter = (this.random.nextDouble() * 2.0D - 1.0D) * 3.0D;
            Vec3 pairCenter = behindTarget
                    .add(towardTarget.scale(forwardScatter))
                    .add(lateral.scale(lateralScatter))
                    .add(0.0D, vertical + heightScatter, 0.0D);
            double pairAngle = this.random.nextDouble() * Mth.TWO_PI;
            Vec3 pairDirection = towardTarget.scale(Math.cos(pairAngle))
                    .add(lateral.scale(Math.sin(pairAngle)));
            double pairSeparation = 3.5D + this.random.nextDouble() * 3.0D;
            Vec3 firstPos = pairCenter.add(pairDirection.scale(pairSeparation));
            Vec3 secondPos = pairCenter.add(pairDirection.scale(-pairSeparation));
            int firstCount = Math.min(3, (remaining + 1) / 2);
            int secondCount = Math.min(3, remaining / 2);
            DimensionalTearEntity first = DimensionalTearEntity.createQueenManticoreTear(level, firstPos,
                    tearYaw, 240, this.getUUID(), firstCount);
            DimensionalTearEntity second = DimensionalTearEntity.createQueenManticoreTear(level, secondPos,
                    tearYaw, 240, this.getUUID(), secondCount);
            first.linkTo(second);
            second.linkTo(first);
            level.addFreshEntity(first);
            level.addFreshEntity(second);
            created++;
            remaining -= firstCount + secondCount;
        }
        return created;
    }

    private void tickQueenAbilities() {
        if (this.isRoyalRecoveryActive()) return;
        if (this.gravityStompCooldownTicks > 0) this.gravityStompCooldownTicks--;
        if (this.momentumLockCooldownTicks > 0) this.momentumLockCooldownTicks--;
        if (this.crushingGravityCooldownTicks > 0) this.crushingGravityCooldownTicks--;
        if (this.accelerationCooldownTicks > 0) this.accelerationCooldownTicks--;
        if (this.queenChronosphereCooldownTicks > 0) this.queenChronosphereCooldownTicks--;
        if (this.blackHoleCooldownTicks > 0) this.blackHoleCooldownTicks--;

        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.queuedQueenFollowUp != QueenFollowUp.NONE || this.directedFireballVolleyTicks > 0) {
            return;
        }

        Phase phase = this.phase();
        boolean majorBusy = this.royalEffects.active("momentum_lock")
                || this.royalEffects.active("crushing_gravity")
                || this.royalEffects.active("acceleration");
        String[] candidates = switch (phase) {
            case ONE -> new String[] {"gravity_stomp", "black_hole"};
            case TWO -> new String[] {"gravity_stomp", "black_hole", "momentum_lock", "crushing_gravity"};
            case THREE -> new String[] {"gravity_stomp", "black_hole", "momentum_lock", "crushing_gravity", "acceleration", "queen_chronosphere"};
        };
        String forcedAttack = this.pendingPhaseSignature == Phase.TWO ? "momentum_lock"
                : this.pendingPhaseSignature == Phase.THREE ? "acceleration" : null;
        String selected = forcedAttack != null
                && this.queenAttackReady(forcedAttack, phase, majorBusy)
                ? forcedAttack : this.attackScheduler.chooseWeighted(candidates, id -> {
                    if (!this.queenAttackReady(id, phase, majorBusy)
                            || id.equals(this.lastQueenMajorAttack)) return 0;
                    return this.queenAttackWeight(id, phase);
                }, this.random);
        if (selected == null) return;
        int cooldown = switch (selected) {
            case "gravity_stomp" -> GRAVITY_STOMP_COOLDOWN;
            case "black_hole" -> BLACK_HOLE_COOLDOWN;
            case "momentum_lock" -> MOMENTUM_LOCK_COOLDOWN;
            case "crushing_gravity" -> CRUSHING_GRAVITY_COOLDOWN;
            case "queen_chronosphere" -> QUEEN_CHRONOSPHERE_COOLDOWN;
            default -> ACCELERATION_COOLDOWN;
        };
        RoyalAttackLane lane = "black_hole".equals(selected) ? RoyalAttackLane.CENTER_HEAD : RoyalAttackLane.HAZARD;
        int actionWindup = this.queenAbilityWindup(selected);
        int actionAnimationTicks = "momentum_lock".equals(selected)
                || "crushing_gravity".equals(selected) || "acceleration".equals(selected)
                || "queen_chronosphere".equals(selected) ? 55 : 25;
        int actionRecovery = this.animationRecovery(actionAnimationTicks, actionWindup, 1, 14);
        if (!this.beginRoyalAttack(selected, lane, this.cooldown(cooldown),
                actionWindup, 1, actionRecovery, new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override public void onStart() {
                        if ("gravity_stomp".equals(selected)) {
                            QueenEntity.this.triggerAnim("body_action", "stomp");
                        } else if ("momentum_lock".equals(selected)
                                || "crushing_gravity".equals(selected)
                                || "acceleration".equals(selected)
                                || "queen_chronosphere".equals(selected)) {
                            QueenEntity.this.triggerAnim("wing_action", "wing_gust");
                        } else if ("black_hole".equals(selected)) {
                            QueenEntity.this.royalHead(RoyalHead.Slot.CENTER).startShoot();
                            QueenEntity.this.triggerAnim(RoyalHead.Slot.CENTER.controllerName(), "shoot");
                        }
                        QueenEntity.this.startQueenAbilityTelegraph(selected, target);
                    }
                    @Override public void onActive(int elapsedTicks) {
                        QueenEntity.this.finishQueenAbilityTelegraph(selected);
                        if ("momentum_lock".equals(selected) || "crushing_gravity".equals(selected)
                                || "acceleration".equals(selected)) {
                            QueenEntity.this.spawnQueenWingPressure(level);
                        }
                        QueenEntity.this.performSelectedQueenAttack(selected, cooldown, level, target);
                    }
                    @Override public void onComplete() {
                        if ("black_hole".equals(selected)) {
                            QueenEntity.this.royalHead(RoyalHead.Slot.CENTER).stopShoot();
                        }
                    }
                })) return;
        this.lastQueenMajorAttack = selected;
        if (selected.equals(forcedAttack)) {
            this.pendingPhaseSignature = null;
        }
    }

    private void performSelectedQueenAttack(String selected, int cooldown,
                                            ServerLevel level, LivingEntity target) {
        switch (selected) {
            case "gravity_stomp" -> {
                this.gravityStompCooldownTicks = cooldown;
                this.performGravityStomp(level, target, false);
                this.startRoyalRecovery(18);
            }
            case "black_hole" -> {
                this.blackHoleCooldownTicks = cooldown;
                this.castBlackHole(level, target);
                this.startRoyalRecovery(16);
                if (this.phase() == Phase.ONE) {
                    this.queueQueenFollowUp(QueenFollowUp.FIREBALL_VOLLEY, BLACK_HOLE_FOLLOW_UP_DELAY);
                }
            }
            case "momentum_lock" -> {
                this.momentumLockCooldownTicks = cooldown;
                this.startMomentumLock(level);
                this.queueQueenFollowUp(QueenFollowUp.FIREBALL_VOLLEY, MOMENTUM_VOLLEY_DELAY);
            }
            case "crushing_gravity" -> {
                this.crushingGravityCooldownTicks = cooldown;
                this.royalEffects.start("crushing_gravity", CRUSHING_GRAVITY_DURATION,
                        () -> {}, this::tickCrushingGravity, () -> this.startRoyalRecovery(35));
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.6F);
                if (this.phase() == Phase.TWO) {
                    this.queueQueenFollowUp(QueenFollowUp.LANDING, CRUSHING_LANDING_DELAY);
                }
            }
            case "acceleration" -> {
                this.accelerationCooldownTicks = cooldown;
                this.startRoyalAcceleration();
                this.queueQueenFollowUp(QueenFollowUp.ACCELERATION_VOLLEY, ACCELERATION_VOLLEY_DELAY);
            }
            case "queen_chronosphere" -> {
                this.queenChronosphereCooldownTicks = cooldown;
                TimeDilationFieldEntity field = TimeDilationApi.createField(level, this.position(),
                        QUEEN_CHRONOSPHERE_RADIUS, 0.08D, QUEEN_CHRONOSPHERE_DURATION, this.getUUID());
                field.configureQueenChronosphere(this);
                this.startRoyalRecovery(35);
            }
        }
    }

    private void spawnQueenWingPressure(ServerLevel level) {
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this);
        for (int ring = 1; ring <= 3; ring++) {
            double radius = ring * 3.2D;
            int samples = 14 + ring * 4;
            for (int sample = 0; sample < samples; sample++) {
                double angle = Mth.TWO_PI * sample / samples;
                double localX = Math.cos(angle) * radius;
                double localZ = Math.sin(angle) * radius;
                Vec3 point = this.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                        localX, this.getBbHeight() * 0.45D, localZ, gravity));
                Vec3 velocity = AntarchyGravityRotationUtil.vecPlayerToWorld(
                        Math.cos(angle) * 0.2D, 0.025D, Math.sin(angle) * 0.2D, gravity);
                level.sendParticles(ACCEL_DUST, point.x, point.y, point.z,
                        0, velocity.x, velocity.y, velocity.z, 1.0D);
            }
        }
    }

    private boolean queenAttackReady(String id, Phase phase, boolean majorBusy) {
        if (majorBusy) return false;
        return switch (id) {
            case "gravity_stomp" -> this.gravityStompCooldownTicks <= 0
                    && !this.isRoyalFlying() && this.queenLandingStage == QueenLandingStage.GROUND_COMBAT;
            case "black_hole" -> this.blackHoleCooldownTicks <= 0
                    && this.royalHead(RoyalHead.Slot.CENTER).readyToAttack()
                    && !this.attackScheduler.laneBusy(RoyalAttackLane.CENTER_HEAD);
            case "momentum_lock" -> !this.royalEffects.active("momentum_lock") && this.momentumLockCooldownTicks <= 0;
            case "crushing_gravity" -> phase != Phase.ONE && !this.royalEffects.active("crushing_gravity") && this.crushingGravityCooldownTicks <= 0;
            case "acceleration" -> phase == Phase.THREE && !this.royalEffects.active("acceleration") && this.accelerationCooldownTicks <= 0;
            case "queen_chronosphere" -> phase == Phase.THREE && this.queenChronosphereCooldownTicks <= 0;
            default -> false;
        };
    }

    private int queenAttackWeight(String id, Phase phase) {
        return switch (phase) {
            case ONE -> "black_hole".equals(id) ? 6 : 3;
            case TWO -> switch (id) {
                case "momentum_lock", "crushing_gravity" -> 6;
                case "gravity_stomp" -> 3;
                case "black_hole" -> 2;
                default -> 0;
            };
            case THREE -> switch (id) {
                case "acceleration" -> 7;
                case "queen_chronosphere" -> 6;
                case "black_hole", "crushing_gravity" -> 5;
                case "gravity_stomp" -> 4;
                case "momentum_lock" -> 2;
                default -> 0;
            };
        };
    }

    private void tickFinalAcceleration() {
        if (this.phase() != Phase.THREE) {
            return;
        }
        if (!this.royalEffects.active("acceleration") && this.accelerationCooldownTicks <= 0) {
            this.pendingPhaseSignature = Phase.THREE;
        }
    }

    private void performGravityStomp(ServerLevel level, LivingEntity target) {
        this.performGravityStomp(level, target, true);
    }

    private void performGravityStomp(ServerLevel level, LivingEntity target, boolean triggerAnimation) {
        if (triggerAnimation) {
            this.triggerAnim("body_action", "stomp");
        }
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.7F);
        DamageSource source = this.damageSources().mobAttack(this);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(18.0D), this::canDamageWithRoyalAttack)) {
            double distance = Math.max(1.0D, living.distanceTo(this));
            living.hurt(source, this.scaleRoyalDamage(12.0D * Math.max(0.25D, 1.0D - distance / 24.0D)));
            Vec3 push = living.position().subtract(this.position()).normalize().scale(1.3D);
            Vec3 vertical = AntarchyGravityRotationUtil.vecPlayerToWorld(
                    0.0D, 0.65D, 0.0D, AntarchyGravityApi.getGravityDirection(living));
            this.addGravityAwareImpulse(living, new Vec3(push.x, 0.0D, push.z).add(vertical));
            living.hasImpulse = true;
        }
        level.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 1.0D, this.getZ(),
                18, 5.0D, 0.6D, 5.0D, 0.08D);
    }

    private void castBlackHole(ServerLevel level, LivingEntity target) {
        Vec3 anchor = this.pendingBlackHoleAnchor != null
                ? this.pendingBlackHoleAnchor : this.selectBlackHoleAnchor(target);
        this.pendingBlackHoleAnchor = null;
        Vec3 mouth = this.beamAnchor(this.royalHead(RoyalHead.Slot.CENTER));
        RoyalBlackHoleEntity hole = RoyalBlackHoleEntity.createProjectile(level, mouth, anchor, this.getUUID());
        level.addFreshEntity(hole);
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 1.0F);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, mouth.x, mouth.y, mouth.z,
                30, 0.6D, 0.6D, 0.6D, 0.25D);
    }

    private Vec3 selectBlackHoleAnchor(LivingEntity target) {
        double vertical = 2.0D + this.random.nextDouble() * 2.0D;
        if (AntarchyGravityApi.isGravityInverted(this)) {
            vertical = -vertical;
        }
        return target.position().add(
                (this.random.nextDouble() - 0.5D) * 4.0D,
                vertical,
                (this.random.nextDouble() - 0.5D) * 4.0D);
    }

    private int queenAbilityWindup(String ability) {
        return switch (ability) {
            case "gravity_stomp" -> GRAVITY_STOMP_TELEGRAPH_TICKS;
            case "black_hole" -> BLACK_HOLE_TELEGRAPH_TICKS;
            case "momentum_lock" -> MOMENTUM_LOCK_TELEGRAPH_TICKS;
            case "crushing_gravity" -> CRUSHING_GRAVITY_TELEGRAPH_TICKS;
            case "acceleration" -> ACCELERATION_TELEGRAPH_TICKS;
            case "queen_chronosphere" -> 40;
            default -> 10;
        };
    }

    private void startQueenAbilityTelegraph(String ability, LivingEntity target) {
        switch (ability) {
            case "gravity_stomp" -> {
                this.gravityStompTelegraphTicks = GRAVITY_STOMP_TELEGRAPH_TICKS;
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.82F);
            }
            case "black_hole" -> {
                this.pendingBlackHoleAnchor = this.selectBlackHoleAnchor(target);
                this.blackHoleTelegraphTicks = BLACK_HOLE_TELEGRAPH_TICKS;
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.48F);
            }
            case "momentum_lock" -> {
                this.momentumLockTelegraphTicks = MOMENTUM_LOCK_TELEGRAPH_TICKS;
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.58F);
            }
            case "crushing_gravity" -> {
                this.crushingGravityTelegraphTicks = CRUSHING_GRAVITY_TELEGRAPH_TICKS;
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.42F);
            }
            case "acceleration" -> {
                this.accelerationTelegraphTicks = ACCELERATION_TELEGRAPH_TICKS;
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 1.22F);
            }
            case "queen_chronosphere" -> {
                this.queenChronosphereTelegraphTicks = 40;
                this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.85F);
            }
        }
    }

    private void finishQueenAbilityTelegraph(String ability) {
        switch (ability) {
            case "gravity_stomp" -> this.gravityStompTelegraphTicks = 0;
            case "black_hole" -> this.blackHoleTelegraphTicks = 0;
            case "momentum_lock" -> this.momentumLockTelegraphTicks = 0;
            case "crushing_gravity" -> this.crushingGravityTelegraphTicks = 0;
            case "acceleration" -> this.accelerationTelegraphTicks = 0;
            case "queen_chronosphere" -> this.queenChronosphereTelegraphTicks = 0;
        }
    }

    private boolean isQueenAbilityTelegraphActive() {
        return this.gravityStompTelegraphTicks > 0 || this.blackHoleTelegraphTicks > 0
                || this.momentumLockTelegraphTicks > 0 || this.crushingGravityTelegraphTicks > 0
                || this.accelerationTelegraphTicks > 0 || this.queenChronosphereTelegraphTicks > 0;
    }

    private void clearQueenAbilityTelegraphs() {
        this.gravityStompTelegraphTicks = 0;
        this.blackHoleTelegraphTicks = 0;
        this.momentumLockTelegraphTicks = 0;
        this.crushingGravityTelegraphTicks = 0;
        this.accelerationTelegraphTicks = 0;
        this.queenChronosphereTelegraphTicks = 0;
        this.pendingBlackHoleAnchor = null;
    }

    private void tickQueenAbilityTelegraphs() {
        if (!this.isQueenAbilityTelegraphActive() || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.tickGravityStompTelegraph(level);
        this.tickBlackHoleTelegraph(level);
        this.tickMomentumLockTelegraph(level);
        this.tickCrushingGravityTelegraph(level);
        this.tickAccelerationTelegraph(level);
        this.tickQueenChronosphereTelegraph(level);
    }

    private void tickGravityStompTelegraph(ServerLevel level) {
        if (this.gravityStompTelegraphTicks <= 0) return;
        int elapsed = GRAVITY_STOMP_TELEGRAPH_TICKS - this.gravityStompTelegraphTicks;
        if (this.tickCount % 2 == 0) {
            double radius = Mth.lerp(elapsed / (double) GRAVITY_STOMP_TELEGRAPH_TICKS, 3.0D, 18.0D);
            Vec3 surfaceCenter = this.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                    0.0D, 0.2D, 0.0D, AntarchyGravityApi.getGravityDirection(this)));
            this.spawnQueenTelegraphSpokes(level, surfaceCenter, radius);
        }
        this.playQueenTelegraphBeat(level, this.gravityStompTelegraphTicks,
                GRAVITY_STOMP_TELEGRAPH_TICKS, 0.72F);
        this.gravityStompTelegraphTicks--;
    }

    private void tickBlackHoleTelegraph(ServerLevel level) {
        if (this.blackHoleTelegraphTicks <= 0 || this.pendingBlackHoleAnchor == null) return;
        int elapsed = BLACK_HOLE_TELEGRAPH_TICKS - this.blackHoleTelegraphTicks;
        double progress = elapsed / (double) BLACK_HOLE_TELEGRAPH_TICKS;
        double fieldRadius = Math.max(4.0D, AntarchySettings.queenBlackHoleRadius());
        if (this.tickCount % 2 == 0) {
            this.spawnQueenTelegraphRing(level, this.pendingBlackHoleAnchor, fieldRadius, 40, ParticleTypes.WITCH);
            this.spawnBlackHoleFunnel(level, this.pendingBlackHoleAnchor, fieldRadius, progress);
            level.sendParticles(AntarchyObjects.LUCID_BOLT_IMPACT_LARGE.get(),
                    this.pendingBlackHoleAnchor.x, this.pendingBlackHoleAnchor.y, this.pendingBlackHoleAnchor.z,
                    2, 0.12D, 0.12D, 0.12D, 0.01D);
        }
        this.playQueenTelegraphBeat(level, this.blackHoleTelegraphTicks,
                BLACK_HOLE_TELEGRAPH_TICKS, 0.48F);
        this.blackHoleTelegraphTicks--;
    }

    private void tickMomentumLockTelegraph(ServerLevel level) {
        if (this.momentumLockTelegraphTicks <= 0) return;
        int elapsed = MOMENTUM_LOCK_TELEGRAPH_TICKS - this.momentumLockTelegraphTicks;
        if (this.tickCount % 2 == 0) {
            this.spawnMomentumLockCage(level, elapsed / (double) MOMENTUM_LOCK_TELEGRAPH_TICKS);
        }
        if (this.momentumLockTelegraphTicks <= 14) {
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(MOMENTUM_LOCK_RADIUS), this::canDamageWithRoyalAttack)) {
                this.spawnMomentumTargetCage(level, living);
                if (this.momentumLockTelegraphTicks == 12) {
                    NightmareBiteEntity.spawnTelegraphAt(level, living, true);
                }
            }
        }
        if (this.momentumLockTelegraphTicks == 40 || this.momentumLockTelegraphTicks == 20
                || this.momentumLockTelegraphTicks == 10 || this.momentumLockTelegraphTicks == 4) {
            float pitch = 0.65F + elapsed / (float) MOMENTUM_LOCK_TELEGRAPH_TICKS;
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.HOSTILE, 2.0F, pitch);
        }
        this.momentumLockTelegraphTicks--;
    }

    private void tickCrushingGravityTelegraph(ServerLevel level) {
        if (this.crushingGravityTelegraphTicks <= 0) return;
        int elapsed = CRUSHING_GRAVITY_TELEGRAPH_TICKS - this.crushingGravityTelegraphTicks;
        double progress = elapsed / (double) CRUSHING_GRAVITY_TELEGRAPH_TICKS;
        if (this.tickCount % 2 == 0) {
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(CRUSHING_GRAVITY_RADIUS), this::canDamageWithRoyalAttack)) {
                double downSign = AntarchyGravityApi.isGravityInverted(living) ? 1.0D : -1.0D;
                this.spawnCrushingGravityArrow(level, living, downSign, progress);
            }
        }
        if (elapsed % 7 == 0) {
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_AMBIENT,
                    SoundSource.HOSTILE, 1.8F, 0.55F + (float) progress * 0.35F);
        }
        this.crushingGravityTelegraphTicks--;
    }

    private void tickAccelerationTelegraph(ServerLevel level) {
        if (this.accelerationTelegraphTicks <= 0) return;
        int elapsed = ACCELERATION_TELEGRAPH_TICKS - this.accelerationTelegraphTicks;
        double progress = elapsed / (double) ACCELERATION_TELEGRAPH_TICKS;
        this.spawnAccelerationHelix(level, progress);
        this.playQueenTelegraphBeat(level, this.accelerationTelegraphTicks,
                ACCELERATION_TELEGRAPH_TICKS, 1.0F);
        this.accelerationTelegraphTicks--;
    }

    private void tickQueenChronosphereTelegraph(ServerLevel level) {
        if (this.queenChronosphereTelegraphTicks <= 0) return;
        int elapsed = 40 - this.queenChronosphereTelegraphTicks;
        Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        if (this.tickCount % 2 == 0) {
            double radius = Mth.lerp(elapsed / 40.0D, 3.0D, QUEEN_CHRONOSPHERE_RADIUS);
            this.spawnQueenTelegraphRing(level, center, radius, 96, ParticleTypes.REVERSE_PORTAL);
        }
        this.playQueenTelegraphBeat(level, this.queenChronosphereTelegraphTicks, 40, 0.5F);
        this.queenChronosphereTelegraphTicks--;
    }

    private void spawnQueenTelegraphSpokes(ServerLevel level, Vec3 center, double radius) {
        int spokes = 10;
        int segments = 6;
        for (int spoke = 0; spoke < spokes; spoke++) {
            double angle = Mth.TWO_PI * spoke / spokes;
            for (int segment = 1; segment <= segments; segment++) {
                double distance = radius * segment / segments;
                level.sendParticles(ParticleTypes.CRIT,
                        center.x + Math.cos(angle) * distance, center.y,
                        center.z + Math.sin(angle) * distance,
                        1, 0.08D, 0.02D, 0.08D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y, center.z,
                3, 0.7D, 0.05D, 0.7D, 0.015D);
    }

    private void spawnBlackHoleFunnel(ServerLevel level, Vec3 center, double fieldRadius, double progress) {
        double rotation = this.tickCount * 0.35D;
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this);
        for (int tier = 0; tier < 4; tier++) {
            double tierProgress = tier / 3.0D;
            double radius = Mth.lerp(progress, fieldRadius * (0.25D + tierProgress * 0.5D), 0.7D);
            double localY = -1.5D + tierProgress * 4.5D;
            for (int point = 0; point < 10; point++) {
                double angle = rotation + tier * 0.65D + Mth.TWO_PI * point / 10.0D;
                Vec3 particlePos = center.add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                        Math.cos(angle) * radius, localY, Math.sin(angle) * radius, gravity));
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void spawnMomentumLockCage(ServerLevel level, double progress) {
        double halfHeight = Mth.lerp(progress, 1.5D, 6.0D);
        double centerY = this.getY(0.45D);
        for (int pillar = 0; pillar < 8; pillar++) {
            double angle = Mth.TWO_PI * pillar / 8.0D;
            double x = this.getX() + Math.cos(angle) * MOMENTUM_LOCK_RADIUS;
            double z = this.getZ() + Math.sin(angle) * MOMENTUM_LOCK_RADIUS;
            for (int segment = 0; segment < 7; segment++) {
                double y = centerY - halfHeight + segment * halfHeight / 3.0D;
                level.sendParticles(ParticleTypes.END_ROD, x, y, z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void spawnMomentumTargetCage(ServerLevel level, LivingEntity living) {
        double centerY = living.getY(0.5D);
        double halfHeight = living.getBbHeight() * 0.7D + 0.6D;
        for (int corner = 0; corner < 4; corner++) {
            double angle = Math.PI / 4.0D + Math.PI / 2.0D * corner;
            double x = living.getX() + Math.cos(angle) * 0.9D;
            double z = living.getZ() + Math.sin(angle) * 0.9D;
            for (int segment = -1; segment <= 1; segment++) {
                level.sendParticles(ParticleTypes.END_ROD, x,
                        centerY + segment * halfHeight, z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private void spawnCrushingGravityArrow(ServerLevel level, LivingEntity living,
                                            double downSign, double progress) {
        double centerY = living.getY(0.5D);
        double shaftStartY = centerY - downSign * Mth.lerp(progress, 7.0D, 4.5D);
        for (int segment = 0; segment < 6; segment++) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, living.getX(),
                    shaftStartY + downSign * segment * 0.75D, living.getZ(),
                    1, 0.04D, 0.02D, 0.04D, 0.0D);
        }
        double tipY = shaftStartY + downSign * 4.5D;
        double headY = tipY - downSign * 0.85D;
        level.sendParticles(ParticleTypes.WITCH, living.getX() + 0.7D, headY, living.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.WITCH, living.getX() - 0.7D, headY, living.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.WITCH, living.getX(), headY, living.getZ() + 0.7D,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.WITCH, living.getX(), headY, living.getZ() - 0.7D,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(AntarchyObjects.LUCID_BOLT_IMPACT_SMALL.get(),
                living.getX(), tipY, living.getZ(), 1, 0.05D, 0.05D, 0.05D, 0.0D);
    }

    private void spawnAccelerationHelix(ServerLevel level, double progress) {
        double rotation = this.tickCount * Mth.lerp(progress, 0.24D, 0.62D);
        double radius = Mth.lerp(progress, 2.8D, 0.75D);
        AntarchyGravityDirection gravity = AntarchyGravityApi.getGravityDirection(this);
        for (int strand = 0; strand < 2; strand++) {
            for (int segment = 0; segment < 9; segment++) {
                double heightProgress = segment / 8.0D;
                double angle = rotation + strand * Math.PI + heightProgress * Mth.TWO_PI * 1.5D;
                double localY = 0.4D + heightProgress * (this.getBbHeight() + 2.0D);
                Vec3 particlePos = this.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                        Math.cos(angle) * radius, localY, Math.sin(angle) * radius, gravity));
                level.sendParticles(AntarchyObjects.LUCID_BOLT_IMPACT_SMALL.get(),
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.02D, 0.02D, 0.02D, 0.0D);
                if (segment % 2 == 0) {
                    level.sendParticles(ACCEL_DUST, particlePos.x, particlePos.y, particlePos.z,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    private <T extends net.minecraft.core.particles.ParticleOptions> void spawnQueenTelegraphRing(
            ServerLevel level, Vec3 center, double radius, int count, T particle) {
        for (int index = 0; index < count; index++) {
            double angle = Mth.TWO_PI * index / count;
            level.sendParticles(particle,
                    center.x + Math.cos(angle) * radius, center.y,
                    center.z + Math.sin(angle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void playQueenTelegraphBeat(ServerLevel level, int remainingTicks, int duration, float basePitch) {
        if (remainingTicks == duration || remainingTicks == duration / 2
                || remainingTicks == 8 || remainingTicks == 3) {
            float progress = 1.0F - remainingTicks / (float) duration;
            level.playSound(null, this.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.HOSTILE, 2.0F, basePitch + progress * 0.7F);
        }
    }

    private void startMomentumLock(ServerLevel level) {
        this.momentumLockCooldownTicks = MOMENTUM_LOCK_COOLDOWN;
        this.royalEffects.start("momentum_lock", MOMENTUM_LOCK_DURATION, () -> {
            this.frozenVelocities.clear();
            for (Entity entity : level.getEntitiesOfClass(Entity.class,
                    this.getBoundingBox().inflate(MOMENTUM_LOCK_RADIUS))) {
                if (entity == this || isFieldImmune(entity) || entity instanceof Player player && player.isCreative()) {
                    continue;
                }
                this.frozenVelocities.put(entity.getUUID(), entity.getDeltaMovement());
            }
        }, this::tickMomentumLockEffect, () -> {
            for (Map.Entry<UUID, Vec3> entry : this.frozenVelocities.entrySet()) {
                Entity entity = level.getEntity(entry.getKey());
                if (entity != null && entity.isAlive()) {
                    entity.setDeltaMovement(entry.getValue());
                    entity.hasImpulse = true;
                }
            }
            this.frozenVelocities.clear();
            this.startRoyalRecovery(35);
        });
        this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 0.5F);
    }

    private void tickMomentumLockEffect() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        for (Iterator<Map.Entry<UUID, Vec3>> iterator = this.frozenVelocities.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<UUID, Vec3> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive()) {
                iterator.remove();
                continue;
            }
            entity.setDeltaMovement(Vec3.ZERO);
            entity.fallDistance = 0.0F;
            entity.hasImpulse = true;
            if (this.tickCount % 6 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(),
                        2, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        }
    }

    private void tickCrushingGravity() {
        if (!this.royalEffects.active("crushing_gravity")) {
            return;
        }
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        for (Entity entity : level.getEntitiesOfClass(Entity.class,
                this.getBoundingBox().inflate(CRUSHING_GRAVITY_RADIUS))) {
            if (entity == this || isFieldImmune(entity) || entity instanceof Player player && player.isCreative()) {
                continue;
            }
            double downSign = AntarchyGravityApi.isGravityInverted(entity) ? 1.0D : -1.0D;
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, downSign * CRUSHING_GRAVITY_STRENGTH, 0.0D));
            entity.hasImpulse = true;
        }
        if (this.tickCount % 8 == 0) {
            level.sendParticles(ACCEL_DUST, this.getX(), this.getY() + 1.0D, this.getZ(), 20,
                    CRUSHING_GRAVITY_RADIUS * 0.5D, 1.0D, CRUSHING_GRAVITY_RADIUS * 0.5D, 0.0D);
        }
    }

    private void startRoyalAcceleration() {
        this.accelerationCooldownTicks = ACCELERATION_COOLDOWN;
        this.royalEffects.start("acceleration", ACCELERATION_DURATION, () -> {
            this.setRoyalAccelerated(true);
            this.playRoyalSound(AntarchySoundEvents.QUEEN_ROAR.get(), 1.4F);
            applyAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
            applyAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
        }, this::tickRoyalAccelerationEffect, () -> {
            this.setRoyalAccelerated(false);
            removeAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
            removeAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
            this.startRoyalRecovery(30);
        });
    }

    private static void applyAccelModifier(AttributeInstance attribute, ResourceLocation id) {
        if (attribute == null || attribute.getModifier(id) != null) {
            return;
        }
        attribute.addTransientModifier(new AttributeModifier(id, 0.9D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removeAccelModifier(AttributeInstance attribute, ResourceLocation id) {
        if (attribute != null) {
            attribute.removeModifier(id);
        }
    }

    private void tickRoyalAccelerationEffect() {
        if (!this.isRoyalAccelerated()) {
            return;
        }
        if (this.level() instanceof ServerLevel level && this.tickCount % 2 == 0) {
            level.sendParticles(ACCEL_DUST,
                    this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    this.getY() + this.random.nextDouble() * this.getBbHeight(),
                    this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth(),
                    6, 0.4D, 0.6D, 0.4D, 0.02D);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel level) {
            DimensionalTearEntity.discardQueenOwnedTears(level, this.getUUID());
        }
        this.royalEffects.clear();
        this.frozenVelocities.clear();
        removeAccelModifier(this.getAttribute(Attributes.MOVEMENT_SPEED), ACCEL_SPEED_ID);
        removeAccelModifier(this.getAttribute(Attributes.FLYING_SPEED), ACCEL_FLY_ID);
        super.die(damageSource);
    }

    private boolean spawnManticore(ServerLevel serverLevel, LivingEntity target) {
        double minimumDistance = Math.max(4.0D, this.getBbWidth() * 0.55D);
        double maximumDistance = Math.max(minimumDistance + 1.0D, AntarchySettings.queenManticoreSummonRange());
        for (int attempt = 0; attempt < POSITION_ATTEMPTS_PER_MANTICORE; attempt++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double distance = Mth.lerp(this.random.nextDouble(), minimumDistance, maximumDistance);
            double x = this.getX() + Math.cos(angle) * distance;
            double y = this.getY() + (this.random.nextDouble() - 0.5D) * 2.0D;
            double z = this.getZ() + Math.sin(angle) * distance;
            ManticoreEntity manticore = AntarchyObjects.MANTICORE.get().create(serverLevel);
            if (manticore == null) {
                return false;
            }
            manticore.moveTo(x, y, z, this.random.nextFloat() * 360.0F, 0.0F);
            BlockPos spawnPos = manticore.blockPosition();
            if (!serverLevel.noCollision(manticore)
                    || !serverLevel.isEmptyBlock(spawnPos)
                    || !ManticoreEntity.canSpawn(AntarchyObjects.MANTICORE.get(), serverLevel, MobSpawnType.MOB_SUMMONED, spawnPos, this.random)) {
                manticore.discard();
                continue;
            }
            manticore.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
            manticore.markQueenSummoned(this.getUUID());
            manticore.setTarget(target);
            serverLevel.addFreshEntity(manticore);
            return true;
        }
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(SUMMON_COOLDOWN_KEY, this.manticoreSummonCooldownTicks);
        tag.putInt("GravityStompCooldownTicks", this.gravityStompCooldownTicks);
        tag.putInt("MomentumLockCooldownTicks", this.momentumLockCooldownTicks);
        tag.putInt("CrushingGravityCooldownTicks", this.crushingGravityCooldownTicks);
        tag.putInt("AccelerationCooldownTicks", this.accelerationCooldownTicks);
        tag.putInt("QueenChronosphereCooldownTicks", this.queenChronosphereCooldownTicks);
        tag.putInt("BlackHoleCooldownTicks", this.blackHoleCooldownTicks);
        tag.putInt("QueenLandingCooldownTicks", this.queenLandingCooldownTicks);
        tag.putString("QueuedQueenFollowUp", this.queuedQueenFollowUp.name());
        tag.putInt("QueuedQueenFollowUpTicks", this.queuedQueenFollowUpTicks);
        tag.putInt("QueuedQueenLandingRetryTicks", this.queuedQueenLandingRetryTicks);
        tag.putInt("DirectedFireballVolleyTicks", this.directedFireballVolleyTicks);
        if (this.lastQueenMajorAttack != null) {
            tag.putString("LastQueenMajorAttack", this.lastQueenMajorAttack);
        }
        if (this.naturalTrailHome != null) {
            tag.putLong("NaturalTrailSiteId", this.naturalTrailSiteId);
            tag.putLong("NaturalTrailHome", this.naturalTrailHome.asLong());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.manticoreSummonCooldownTicks = Math.max(0, tag.getInt(SUMMON_COOLDOWN_KEY));
        this.gravityStompCooldownTicks = Math.max(0, tag.getInt("GravityStompCooldownTicks"));
        this.momentumLockCooldownTicks = Math.max(0, tag.getInt("MomentumLockCooldownTicks"));
        this.crushingGravityCooldownTicks = Math.max(0, tag.getInt("CrushingGravityCooldownTicks"));
        this.accelerationCooldownTicks = Math.max(0, tag.getInt("AccelerationCooldownTicks"));
        this.queenChronosphereCooldownTicks = Math.max(0, tag.getInt("QueenChronosphereCooldownTicks"));
        this.blackHoleCooldownTicks = Math.max(0, tag.getInt("BlackHoleCooldownTicks"));
        this.queenLandingCooldownTicks = tag.contains("QueenLandingCooldownTicks")
                ? Math.max(0, tag.getInt("QueenLandingCooldownTicks")) : 300;
        if (tag.contains("QueuedQueenFollowUp")) {
            try {
                this.queuedQueenFollowUp = QueenFollowUp.valueOf(tag.getString("QueuedQueenFollowUp"));
            } catch (IllegalArgumentException ignored) {
                this.queuedQueenFollowUp = QueenFollowUp.NONE;
            }
        }
        this.queuedQueenFollowUpTicks = Math.max(0, tag.getInt("QueuedQueenFollowUpTicks"));
        this.queuedQueenLandingRetryTicks = Math.max(0, tag.getInt("QueuedQueenLandingRetryTicks"));
        if (this.queuedQueenFollowUp == QueenFollowUp.LANDING && this.queuedQueenLandingRetryTicks == 0) {
            this.queuedQueenLandingRetryTicks = DIRECTED_LANDING_RETRY_TICKS;
        }
        this.directedFireballVolleyTicks = Math.max(0, tag.getInt("DirectedFireballVolleyTicks"));
        this.lastQueenMajorAttack = tag.contains("LastQueenMajorAttack")
                ? tag.getString("LastQueenMajorAttack") : null;
        if (tag.contains("NaturalTrailHome")) {
            this.naturalTrailSiteId = tag.getLong("NaturalTrailSiteId");
            this.naturalTrailHome = BlockPos.of(tag.getLong("NaturalTrailHome"));
        }
    }
}
