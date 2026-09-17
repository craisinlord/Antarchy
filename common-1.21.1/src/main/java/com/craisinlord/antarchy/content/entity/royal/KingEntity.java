package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.network.KingJudgmentFlashSync;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.menu.RoyalJudgmentMenu;
import com.craisinlord.antarchy.content.menu.RoyalJudgmentState;
import com.craisinlord.antarchy.content.entity.royal.decree.CloseQuartersDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.ComeNoCloserDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.DoNotRunDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.FightMeCowardDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.HandsOffTheCrownDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.KneelDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.NoRespiteDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.RoyalDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.ShowNoMercyDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.SkyIsMineDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.StandTallDecree;
import com.craisinlord.antarchy.content.entity.royal.decree.StandYourGroundDecree;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackLane;
import java.util.EnumMap;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class KingEntity extends RoyalBossEntity {
    private static final DustParticleOptions KING_GOLD_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.12F), 1.25F);
    private static final DustParticleOptions KING_WHITE_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.35F);
    public enum Behavior {
        AIRBORNE, SPRINTING, RETREATING, HUGGING, RANGED, KEEPING_FAR
    }

    private static final int STOMP_COOLDOWN_TICKS = 220;
    private static final int STOMP_WINDUP_TICKS = 18;
    private static final double STOMP_RADIUS = 15.0D;
    private static final int WING_GUST_COOLDOWN_TICKS = 320;
    private static final int WING_GUST_WINDUP_TICKS = 14;
    private static final double WING_GUST_RADIUS = 22.0D;
    private static final int ROYAL_CHARGE_WINDUP_TICKS = 24;
    private static final int ROYAL_CHARGE_ACTIVE_TICKS = 18;
    private static final int ROYAL_CHARGE_RECOVERY_TICKS = 18;
    private static final double ROYAL_CHARGE_SPEED = 2.35D;
    private static final double ROYAL_CHARGE_MIN_RANGE = 10.0D;
    private static final double ROYAL_CHARGE_MAX_RANGE = 50.0D;
    private static final int CHAIN_LIGHTNING_INTERVAL = 12;
    private static final int CHAIN_LIGHTNING_JUMPS = 4;
    private static final double CHAIN_LIGHTNING_JUMP_RANGE = 9.0D;
    private static final int ICE_FREEZE_PER_TICK = 7;
    private static final int BEHAVIOR_SCORE_CAP = 12;
    /** Keep the tree-bound patrol safely inside the authored tree footprint. */
    private static final double TREE_ORBIT_RADIUS = 128.0D;
    private static final double TREE_ORBIT_ANGLE_STEP = 0.014D;
    private static final int TREE_ORBIT_PATH_SAMPLES = 16;
    private static final double COMBAT_ORBIT_ANGLE_STEP = 0.62D;
    private static final double KING_DAMAGE_MULTIPLIER = 1.5D;
    private static final double KING_FOLLOW_RANGE = 216.0D;
    private static final double COME_NO_CLOSER_RADIUS = 16.0D;
    private static final int COME_NO_CLOSER_PARTICLE_INTERVAL = 4;
    private static final int COME_NO_CLOSER_PARTICLE_COUNT = 40;
    private static final double CLOSE_QUARTERS_RADIUS = 16.0D;
    private static final int CLOSE_QUARTERS_APPROACH_TICKS = 120;
    private static final int CLOSE_QUARTERS_EXIT_GRACE_TICKS = 40;
    private static final int DECREE_RESPONSE_GRACE_TICKS = 30;
    private static final int PROJECTILE_PRESSURE_MAX = 60;
    private static final int PROJECTILE_PRESSURE_PER_HIT = 10;
    private static final int GROUND_ASSAULT_TICKS = 200;
    private static final int GROUND_ASSAULT_COOLDOWN_MIN = 600;
    private static final int GROUND_ASSAULT_COOLDOWN_VARIANCE = 300;
    private static final int LANDING_TELEGRAPH_TICKS = 24;
    private static final int LANDING_IMPACT_TICKS = 18;
    private static final int TAKEOFF_TICKS = 30;
    private static final int COMBAT_WAYPOINT_MIN_TICKS = 45;
    private static final int COMBAT_WAYPOINT_VARIANCE = 35;
    private static final int GROUND_COMBAT_WAYPOINT_MIN_TICKS = 18;
    private static final int GROUND_COMBAT_WAYPOINT_VARIANCE = 12;

    @Nullable
    private Vec3 patrolCenter;
    @Nullable
    private Vec3 treePatrolCenter;
    @Nullable
    private Vec3 treePatrolWaypoint;
    private double treePatrolMinimumY;
    private double treePatrolMaximumY;
    private double treePatrolAngle;
    private boolean treePatrolBound;
    private int patrolCooldownTicks;
    @Nullable
    private Vec3 treePatrolProgressPosition;
    private int treePatrolProgressTicks;
    private int treePatrolFailures;
    private int decreeCooldownTicks;
    private int activeDecreeTicks;
    private int decreeResponseGraceTicks;
    private int stompCooldownTicks;
    private int wingGustCooldownTicks;
    private int fireballCooldownTicks = 80;
    private int iceballCooldownTicks = 120;
    private int iceSpikeCooldownTicks = 160;
    private int royalChargeOpeningDelayTicks = 35;
    private boolean royalChargeActive;
    private Vec3 royalChargeDirection = Vec3.ZERO;
    private final Set<UUID> royalChargeHitTargets = new HashSet<>();
    private boolean decreeRetreatPressure;
    @Nullable
    private String lastDecreeKey;
    private final Set<UUID> closeQuartersEntered = new HashSet<>();
    private final Map<UUID, Integer> closeQuartersOutsideTicks = new HashMap<>();
    private final Set<UUID> decreeRecipients = new HashSet<>();
    private int projectilePressure;
    private int groundAssaultCooldownTicks = 400;
    private KingLandingStage kingLandingStage = KingLandingStage.AERIAL;
    private int kingLandingStageTicks;
    @Nullable
    private Vec3 kingLandingWaypoint;
    @Nullable
    private Vec3 kingCombatWaypoint;
    @Nullable
    private Vec3 kingCombatProgressPosition;
    private int kingCombatWaypointTicks;
    private int kingCombatProgressTicks;
    private int kingGroundCombatWaypointTicks;
    @Nullable
    private Vec3 kingGroundCombatWaypoint;
    private int kingStrafeDirection = 1;
    private int royalBoundaryGraceTicks;
    @Nullable
    private ServerPlayer exileTarget;
    private int exileTicks;
    private boolean exileWingAnimationPending;
    @Nullable
    private ServerPlayer titheTarget;
    private int titheTicks;
    @Nullable
    private ServerPlayer targetShotPlayer;
    @Nullable
    private ServerPlayer targetShotVictim;
    private int targetShotTicks;
    @Nullable
    private ServerPlayer sealedTarget;
    private int sealedTicks;
    private long sealUntil;
    private final Map<UUID, Long> judgmentCooldowns = new HashMap<>();
    private final Map<UUID, Long> lastPlayerDamageTime = new HashMap<>();
    private final Map<UUID, Integer> iceBuildup = new HashMap<>();
    private final EnumMap<Behavior, Integer> behaviorScores = new EnumMap<>(Behavior.class);
    @Nullable
    private RoyalDecree activeDecree;
    private static final List<RoyalDecree> DECREES = List.of(
            new SkyIsMineDecree(), new DoNotRunDecree(), new KneelDecree(), new CloseQuartersDecree(),
            new ComeNoCloserDecree(), new FightMeCowardDecree(), new NoRespiteDecree(),
            new StandTallDecree(), new StandYourGroundDecree(),
            new HandsOffTheCrownDecree(), new ShowNoMercyDecree());

    private enum KingLandingStage {
        AERIAL,
        TELEGRAPH,
        DESCENT,
        IMPACT,
        GROUND_COMBAT,
        TAKEOFF
    }

    public KingEntity(EntityType<? extends KingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseAttributes(AntarchySettings.kingHealth(),
                        AntarchySettings.kingAttackDamage() * KING_DAMAGE_MULTIPLIER, AntarchySettings.kingArmor())
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, KING_FOLLOW_RANGE);
    }

    @Override
    public float scaleRoyalDamage(double damage) {
        return super.scaleRoyalDamage(damage) * (float) KING_DAMAGE_MULTIPLIER;
    }

    @Override
    protected int maxConcurrentHeadAttacks(Phase phase) {
        return phase.maxConcurrentHeadAttacks();
    }

    @Override
    protected boolean blocksRoyalAttacksForMovement() {
        return this.royalChargeActive
                || this.kingLandingStage == KingLandingStage.TELEGRAPH
                || this.kingLandingStage == KingLandingStage.DESCENT
                || this.kingLandingStage == KingLandingStage.IMPACT
                || this.kingLandingStage == KingLandingStage.TAKEOFF;
    }

    @Override
    protected boolean blocksRoyalBeamScheduling() {
        if (this.royalChargeActive) {
            return true;
        }
        LivingEntity target = this.getTarget();
        if (target == null || this.activeDecree != null || this.royalChargeOpeningDelayTicks > 0
                || this.kingLandingStage != KingLandingStage.AERIAL
                || !this.attackScheduler.ready("royal_charge", RoyalAttackLane.MOVEMENT)) {
            return false;
        }
        double distance = this.distanceTo(target);
        return distance >= ROYAL_CHARGE_MIN_RANGE && distance <= ROYAL_CHARGE_MAX_RANGE;
    }

    @Override
    protected void onRoyalPhaseTransitionStarted(Phase previousPhase, Phase nextPhase) {
        this.attackScheduler.cancel(RoyalAttackLane.MOVEMENT);
        this.royalChargeActive = false;
        this.royalChargeDirection = Vec3.ZERO;
        this.royalChargeHitTargets.clear();
        if (this.activeDecree != null) {
            this.endDecree(null);
        }
        this.resetKingCombatMovement();
        this.setRoyalFlying(true);
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.25D, 0.0D));
        this.getNavigation().stop();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.35D));
        this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), nextPhase == Phase.THREE ? 0.72F : 0.88F);
        if (this.level() instanceof ServerLevel level) {
            Component title = Component.translatable(nextPhase == Phase.TWO
                            ? "boss.antarchy.king.phase_two" : "boss.antarchy.king.phase_three")
                    .withStyle(style -> style.withColor(nextPhase == Phase.TWO
                            ? ChatFormatting.GOLD : ChatFormatting.YELLOW).withBold(true));
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
            level.sendParticles(nextPhase == Phase.THREE ? KING_GOLD_DUST : ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY() + this.getBbHeight() * 0.55D, this.getZ(),
                    18, this.getBbWidth() * 0.25D, this.getBbHeight() * 0.25D,
                    this.getBbWidth() * 0.25D, 0.08D);
        }
    }

    @Override
    protected void onRoyalPhaseTransitionCompleted(Phase nextPhase) {
        this.decreeCooldownTicks = 0;
        this.groundAssaultCooldownTicks = nextPhase == Phase.THREE ? 0 : Math.min(this.groundAssaultCooldownTicks, 100);
        if (nextPhase == Phase.THREE) {
            this.fireballCooldownTicks = 0;
            this.iceballCooldownTicks = 0;
            this.iceSpikeCooldownTicks = 0;
        }
    }

    @Override
    protected boolean managesOwnCombatLocomotion() {
        return true;
    }

    public void setTreePatrolHome(Vec3 center, double minimumY, double maximumY, double angle) {
        this.treePatrolCenter = center;
        this.treePatrolMinimumY = minimumY;
        this.treePatrolMaximumY = maximumY;
        this.treePatrolAngle = angle;
        this.treePatrolWaypoint = null;
        this.treePatrolProgressPosition = null;
        this.treePatrolProgressTicks = 0;
        this.treePatrolFailures = 0;
        this.treePatrolBound = true;
    }

    public boolean isTreePatrolFor(Vec3 center) {
        return this.treePatrolBound && this.treePatrolCenter != null
                && this.treePatrolCenter.distanceToSqr(center) < 1.0D;
    }

    @Override
    protected boolean shouldClearObstruction() {
        return this.getTarget() != null;
    }

    @Override
    protected String geoName() {
        return "king";
    }

    @Override
    protected boolean isFlyingBoss() {
        return true;
    }

    @Override
    protected BossEvent.BossBarColor bossBarColor() {
        return BossEvent.BossBarColor.YELLOW;
    }

    @Override
    protected SoundEvent royalIdleSound() {
        return AntarchySoundEvents.KING_IDLE.get();
    }

    @Override
    protected SoundEvent royalHurtSound() {
        return AntarchySoundEvents.KING_HURT.get();
    }

    @Override
    protected SoundEvent royalDeathSound() {
        return AntarchySoundEvents.KING_DEATH.get();
    }

    @Override
    protected SoundEvent royalFlyLoopSound() {
        return AntarchySoundEvents.KING_FLY_LOOP.get();
    }

    @Override
    protected SoundEvent royalBiteSound() {
        return AntarchySoundEvents.KING_BITE.get();
    }

    @Override
    protected SoundEvent royalBeamShootSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamStartSound(RoyalHead head) {
        return head.slot() == RoyalHead.Slot.LEFT
                ? AntarchySoundEvents.KING_FIREBALL_SHOOT.get()
                : AntarchySoundEvents.KING_BEAM_SHOOT.get();
    }

    @Override
    protected float royalBeamStartSoundVolume(RoyalHead head) {
        return head.slot() == RoyalHead.Slot.LEFT
                ? 0.18F : (float) AntarchySettings.royalBossSoundVolume();
    }

    @Override
    protected SoundEvent royalBeamLoopSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected SoundEvent royalBeamEndSound() { return AntarchySoundEvents.KING_BEAM_SHOOT.get(); }

    @Override
    protected RoyalBeamSettings royalBeamSettings() {
        return new RoyalBeamSettings(AntarchySettings.kingBeamRange(), AntarchySettings.kingBeamTracking(), 7.5D,
                AntarchySettings.kingBeamDurationTicks(), AntarchySettings.kingBeamTravelTicks(), AntarchySettings.kingBeamCooldownTicks(), 6.0F, 6.0F,
                (float) AntarchySettings.kingBeamDamage(), 1.0F, 3, 100.0D,
                (float) AntarchySettings.kingBeamTerrainRadius(), 4.0F, AntarchySettings.kingBeamTerrainCap(),
                1.0F, 0.08F, 15.0F, true, true);
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode() {
        return RoyalBeamTerrainMode.BUILD_ICE;
    }

    @Override
    protected double royalBeamMinimumRange() {
        return AntarchySettings.kingBeamMinimumRange();
    }

    @Override
    protected RoyalBeamTerrainMode royalBeamTerrainMode(@Nullable RoyalHead head) {
        return head != null && head.slot() == RoyalHead.Slot.RIGHT ? RoyalBeamTerrainMode.BUILD_ICE : RoyalBeamTerrainMode.NONE;
    }

    @Override
    protected RoyalBeamElement royalBeamElement(@Nullable RoyalHead head) {
        if (head == null) {
            return RoyalBeamElement.GENERIC;
        }
        return switch (head.slot()) {
            case LEFT -> RoyalBeamElement.FIRE;
            case CENTER -> RoyalBeamElement.LIGHTNING;
            case RIGHT -> RoyalBeamElement.ICE;
        };
    }

    @Override
    protected void tickRoyalBeamEffects(RoyalHead head, Vec3 start, Vec3 end) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        var particle = switch (head.slot()) {
            case LEFT -> null;
            case CENTER -> ParticleTypes.ELECTRIC_SPARK;
            case RIGHT -> ParticleTypes.SNOWFLAKE;
        };
        Vec3 direction = end.subtract(start);
        double distance = direction.length();
        if (particle != null && distance > 0.001D) {
            direction = direction.scale(1.0D / distance);
            for (double walked = 0.0D; walked <= distance; walked += 2.5D) {
                Vec3 point = start.add(direction.scale(walked));
                level.sendParticles(particle, point.x, point.y, point.z, 2, 0.12D, 0.12D, 0.12D, 0.01D);
            }
        }
        LivingEntity target = head.target(this.level());
        switch (head.slot()) {
            case LEFT -> {
                if (target != null && target.isAlive()) {
                    target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), 40));
                }
            }
            case CENTER -> {
                if (this.tickCount % CHAIN_LIGHTNING_INTERVAL == 0) {
                    this.chainLightning(level, end);
                }
            }
            case RIGHT -> {
                if (target != null && target.isAlive()) {
                    this.applyIceFreezePressure(target);
                }
            }
        }
    }

    private void chainLightning(ServerLevel level, Vec3 origin) {
        java.util.Set<UUID> struck = new java.util.HashSet<>();
        Vec3 from = origin;
        float damage = this.scaleRoyalDamage(AntarchySettings.kingBeamDamage() * 0.6D);
        for (int jump = 0; jump < CHAIN_LIGHTNING_JUMPS; jump++) {
            LivingEntity next = null;
            double bestScore = Double.MAX_VALUE;
            for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class,
                    new AABB(from, from).inflate(CHAIN_LIGHTNING_JUMP_RANGE),
                    e -> e.isAlive() && !(e instanceof RoyalBossEntity) && e.getType() != this.getType())) {
                if (struck.contains(candidate.getUUID())) {
                    continue;
                }
                double score = candidate.position().distanceToSqr(from) - (isConductive(candidate) ? 16.0D : 0.0D);
                if (score < bestScore) {
                    bestScore = score;
                    next = candidate;
                }
            }
            if (next == null) {
                break;
            }
            struck.add(next.getUUID());
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(next.getX(), next.getY(), next.getZ());
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            next.hurt(this.damageSources().lightningBolt(), damage);
            from = next.position().add(0.0D, next.getBbHeight() * 0.5D, 0.0D);
            if (isConductive(next)) {
                jump--;
            }
        }
    }

    private static boolean isConductive(LivingEntity entity) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof ArmorItem armor) {
                var material = armor.getMaterial();
                if (material == ArmorMaterials.IRON || material == ArmorMaterials.NETHERITE || material == ArmorMaterials.CHAIN) {
                    return true;
                }
            }
        }
        return false;
    }

    private void applyIceFreezePressure(LivingEntity target) {
        int buildup = this.iceBuildup.merge(target.getUUID(), ICE_FREEZE_PER_TICK, Integer::sum);
        target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + ICE_FREEZE_PER_TICK * 2));
        if (buildup >= 60) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 3, false, true));
            this.iceBuildup.put(target.getUUID(), 20);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.tickRoyalBoundary((ServerLevel) this.level());
        this.tickComeNoCloserIndicator();
        this.tickRoyalPunishment();
        this.tickPendingExileAnimation();
        long gameTime = this.level().getGameTime();
        this.judgmentCooldowns.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
        this.lastPlayerDamageTime.entrySet().removeIf(entry -> gameTime - entry.getValue() > 600L);
        ServerLevel serverLevel = (ServerLevel) this.level();
        this.iceBuildup.entrySet().removeIf(entry -> serverLevel.getEntity(entry.getKey()) == null);
        if (this.iceBuildup.size() > 16) this.iceBuildup.clear();
        if (this.tickCount % 10 == 0 && this.projectilePressure > 0) {
            this.projectilePressure--;
        }
        if (this.tickCount % 40 == 0) {
            this.bumpBehavior(Behavior.RANGED, -1);
        }
        if (this.tickCount % 20 == 0 && this.getArrowCount() > 6) {
            this.setArrowCount(6);
        }
        if (this.royalChargeOpeningDelayTicks > 0) {
            this.royalChargeOpeningDelayTicks--;
        }
        if (this.isRoyalPhaseTransitionActive()) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target != null && !this.isDeadOrDying() && this.level() instanceof ServerLevel level) {
            this.trackBehavior(target);
            this.tickDecree(level, target);
            this.tickKingCombatMovement(target);
            if (!this.blocksRoyalAttacksForMovement()) {
                this.tickKingWholeBody(level, target);
            }
            this.stabilizeKingRotation();
            return;
        }

        if (this.isDeadOrDying() || !(this.level() instanceof ServerLevel)) {
            return;
        }
        this.setRoyalFlying(true);
        this.resetKingCombatMovement();
        this.decreeRetreatPressure = false;
        if (this.treePatrolBound && this.treePatrolCenter != null) {
            this.tickTreePatrol();
            return;
        }
        if (this.patrolCenter == null || this.position().distanceToSqr(this.patrolCenter) > 64.0D * 64.0D) {
            this.patrolCenter = this.position();
        }
        if (this.patrolCooldownTicks-- <= 0) {
            this.patrolCooldownTicks = 100 + this.random.nextInt(100);
            this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.9F + this.random.nextFloat() * 0.15F);
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double radius = 12.0D + this.random.nextDouble() * 18.0D;
            double px = this.patrolCenter.x + Math.cos(angle) * radius;
            double pz = this.patrolCenter.z + Math.sin(angle) * radius;
            double py = this.groundYBelow(px, pz) + FLYING_PREFERRED_HOVER + this.random.nextDouble() * 5.0D;
            this.getMoveControl().setWantedPosition(px, py, pz, 1.0D);
        }
    }

    private void tickComeNoCloserIndicator() {
        boolean keepOut = this.activeDecree instanceof ComeNoCloserDecree;
        boolean closeQuarters = this.activeDecree instanceof CloseQuartersDecree;
        if ((!keepOut && !closeQuarters)
                || this.tickCount % COME_NO_CLOSER_PARTICLE_INTERVAL != 0
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double radius = keepOut ? COME_NO_CLOSER_RADIUS : CLOSE_QUARTERS_RADIUS;
        for (int index = 0; index < COME_NO_CLOSER_PARTICLE_COUNT; index++) {
            double angle = (Math.PI * 2.0D * index) / COME_NO_CLOSER_PARTICLE_COUNT;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(keepOut ? ParticleTypes.END_ROD : KING_GOLD_DUST,
                    x, this.getY() + 0.25D, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void tickTreePatrol() {
        Vec3 center = this.treePatrolCenter;
        if (center == null) {
            return;
        }
        this.treePatrolAngle += TREE_ORBIT_ANGLE_STEP;
        if (this.treePatrolAngle >= Math.PI * 2.0D) {
            this.treePatrolAngle -= Math.PI * 2.0D;
        }
        Vec3 waypoint = this.treePatrolWaypoint;
        if (waypoint != null && ++this.treePatrolProgressTicks >= 40) {
            if (this.treePatrolProgressPosition != null
                    && this.position().distanceToSqr(this.treePatrolProgressPosition) < 1.0D
                    && this.position().distanceToSqr(waypoint) > 36.0D) {
                this.treePatrolWaypoint = null;
                waypoint = null;
                this.treePatrolFailures++;
                this.treePatrolAngle += 0.35D;
            } else {
                this.treePatrolFailures = Math.max(0, this.treePatrolFailures - 1);
            }
            this.treePatrolProgressPosition = this.position();
            this.treePatrolProgressTicks = 0;
        }
        if (waypoint == null || this.position().distanceToSqr(waypoint) < 36.0D) {
            waypoint = this.findTreePatrolWaypoint(center, this.treePatrolAngle);
            this.treePatrolWaypoint = waypoint;
            this.treePatrolProgressPosition = this.position();
            this.treePatrolProgressTicks = 0;
        }
        if (waypoint != null) {
            this.getMoveControl().setWantedPosition(waypoint.x, waypoint.y, waypoint.z, 1.0D);
            this.getLookControl().setLookAt(center.x,
                    (this.treePatrolMinimumY + this.treePatrolMaximumY) * 0.5D,
                    center.z, 30.0F, 30.0F);
        }
    }

    private void tickRoyalBoundary(ServerLevel level) {
        if (!this.treePatrolBound || this.treePatrolCenter == null) {
            return;
        }
        double boundaryRadius = Math.max(32.0D, AntarchySettings.royalBoundaryRadius());
        double distance = Math.sqrt(this.horizontalDistanceToTreeSqr());
        if (distance > boundaryRadius) {
            Vec3 towardCenter = this.treePatrolCenter.subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (towardCenter.lengthSqr() > 0.001D) {
                towardCenter = towardCenter.normalize();
                this.getMoveControl().setWantedPosition(this.getX() + towardCenter.x * 12.0D,
                        this.groundYBelow(this.getX() + towardCenter.x * 12.0D,
                                this.getZ() + towardCenter.z * 12.0D) + FLYING_PREFERRED_HOVER,
                        this.getZ() + towardCenter.z * 12.0D, 1.4D);
            }
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.royalBoundaryGraceTicks = 0;
            return;
        }
        double warningRadius = Math.min(boundaryRadius, Math.max(16.0D, AntarchySettings.royalBoundaryWarningRadius()));
        double targetDistance = Math.sqrt(target.position().subtract(this.treePatrolCenter).multiply(1.0D, 0.0D, 1.0D).lengthSqr());
        if (targetDistance > warningRadius) {
            if (this.tickCount % 5 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0D, target.getZ(),
                        4, 0.3D, 0.5D, 0.3D, 0.02D);
            }
            if (targetDistance > boundaryRadius) {
                this.royalBoundaryGraceTicks++;
                if (this.royalBoundaryGraceTicks > Math.max(0, AntarchySettings.royalBoundaryGraceTicks())
                        && this.tickCount % 20 == 0) {
                    this.invokeJudgment(target);
                }
            }
        } else {
            this.royalBoundaryGraceTicks = 0;
        }
    }

    private double horizontalDistanceToTreeSqr() {
        if (this.treePatrolCenter == null) {
            return 0.0D;
        }
        double dx = this.getX() - this.treePatrolCenter.x;
        double dz = this.getZ() - this.treePatrolCenter.z;
        return dx * dx + dz * dz;
    }

    @Nullable
    private Vec3 findTreePatrolWaypoint(Vec3 center, double angle) {
        double currentAngle = Math.atan2(this.getZ() - center.z, this.getX() - center.x);
        double baseAngle = Double.isFinite(currentAngle) ? currentAngle + 0.24D : angle;
        double[] radii = {TREE_ORBIT_RADIUS, TREE_ORBIT_RADIUS + 20.0D, TREE_ORBIT_RADIUS - 20.0D};
        double[] heightOffsets = {0.0D, 16.0D, -12.0D, 30.0D};
        for (int angleAttempt = 0; angleAttempt < 8; angleAttempt++) {
            double candidateAngle = baseAngle + angleAttempt * 0.16D;
            for (double radius : radii) {
                double x = center.x + Math.cos(candidateAngle) * radius;
                double z = center.z + Math.sin(candidateAngle) * radius;
                double vertical = 0.5D + 0.5D * Math.sin(candidateAngle);
                double baseY = this.treePatrolMinimumY
                        + (this.treePatrolMaximumY - this.treePatrolMinimumY) * vertical;
                for (double heightOffset : heightOffsets) {
                    double y = Mth.clamp(baseY + heightOffset,
                            this.treePatrolMinimumY + 8.0D, this.treePatrolMaximumY - 8.0D);
                    Vec3 candidate = new Vec3(x, y, z);
                    if (this.clearTreePatrolPath(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        Vec3 outward = this.position().subtract(center).multiply(1.0D, 0.0D, 1.0D);
        if (outward.lengthSqr() < 1.0E-4D) {
            outward = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
        }
        Vec3 tangent = new Vec3(-outward.z, 0.0D, outward.x).normalize();
        Vec3 recovery = this.position().add(tangent.scale(24.0D)).add(0.0D,
                this.treePatrolFailures >= 2 ? 18.0D : 8.0D, 0.0D);
        recovery = new Vec3(recovery.x,
                Mth.clamp(recovery.y, this.treePatrolMinimumY + 8.0D, this.treePatrolMaximumY - 8.0D), recovery.z);
        if (this.clearTreePatrolPath(recovery)) {
            return recovery;
        }
        return null;
    }

    private boolean clearTreePatrolPath(Vec3 destination) {
        Vec3 start = this.position();
        for (int sample = 1; sample <= TREE_ORBIT_PATH_SAMPLES; sample++) {
            double progress = sample / (double) TREE_ORBIT_PATH_SAMPLES;
            Vec3 point = start.lerp(destination, progress);
            AABB box = this.getBoundingBox().move(point.x - this.getX(), point.y - this.getY(), point.z - this.getZ());
            if (!this.level().noCollision(this, box)) {
                return false;
            }
        }
        return true;
    }

    private void tickKingCombatMovement(LivingEntity target) {
        if (this.royalChargeActive) {
            this.getNavigation().stop();
            return;
        }
        boolean closeQuarters = this.activeDecree instanceof CloseQuartersDecree;
        if (closeQuarters && this.isRoyalFlying() && this.kingLandingStage == KingLandingStage.AERIAL) {
            this.beginKingLanding(target);
        } else if (!closeQuarters && this.isRoyalFlying() && this.kingLandingStage == KingLandingStage.AERIAL
                && this.groundAssaultCooldownTicks-- <= 0 && target.onGround()) {
            this.beginKingLanding(target);
        }

        switch (this.kingLandingStage) {
            case TELEGRAPH -> {
                this.getNavigation().stop();
                this.setDeltaMovement(this.getDeltaMovement().scale(0.72D));
                this.tickKingLandingIndicator();
                if (--this.kingLandingStageTicks <= 0) {
                    this.kingLandingStage = KingLandingStage.DESCENT;
                    this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.72F);
                }
                return;
            }
            case DESCENT -> {
                Vec3 landing = this.kingLandingWaypoint;
                if (landing == null) {
                    this.resetKingCombatMovement();
                    return;
                }
                this.setRoyalFlying(true);
                this.getMoveControl().setWantedPosition(landing.x, landing.y, landing.z, 1.35D);
                this.tickKingLandingIndicator();
                Vec3 localLandingDelta = AntarchyGravityRotationUtil.vecWorldToPlayer(
                        this.position().subtract(landing), AntarchyGravityApi.getGravityDirection(this));
                double horizontalDistanceSquared = this.position().subtract(landing)
                        .multiply(1.0D, 0.0D, 1.0D).lengthSqr();
                boolean reachedLanding = this.onGround()
                        || this.position().distanceToSqr(landing) <= 2.25D
                        || Math.abs(localLandingDelta.y) <= 1.25D && horizontalDistanceSquared <= 4.0D;
                if (reachedLanding) {
                    this.setRoyalFlying(false);
                    this.setPos(this.getX(), landing.y, this.getZ());
                    this.kingLandingStage = KingLandingStage.IMPACT;
                    this.kingLandingStageTicks = LANDING_IMPACT_TICKS;
                    this.triggerAnim("body_action", "stomp");
                    this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.68F);
                    if (this.level() instanceof ServerLevel level) {
                        this.performStomp(level, target);
                    }
                }
                return;
            }
            case IMPACT -> {
                this.getNavigation().stop();
                this.setDeltaMovement(Vec3.ZERO);
                if (--this.kingLandingStageTicks <= 0) {
                    this.kingLandingStage = KingLandingStage.GROUND_COMBAT;
                    this.kingLandingStageTicks = closeQuarters
                            ? Math.max(this.groundAssaultTicksForPhase(), this.activeDecreeTicks + 20)
                            : this.groundAssaultTicksForPhase();
                    this.kingLandingWaypoint = null;
                }
                return;
            }
            case GROUND_COMBAT -> {
                if (closeQuarters) {
                    this.kingLandingStageTicks = Math.max(this.kingLandingStageTicks, this.activeDecreeTicks + 20);
                }
                if (this.kingGroundCombatWaypoint == null || --this.kingGroundCombatWaypointTicks <= 0
                        || this.position().distanceToSqr(this.kingGroundCombatWaypoint) < 4.0D) {
                    this.selectKingGroundCombatWaypoint(target);
                }
                if (this.kingGroundCombatWaypoint != null) {
                    this.getMoveControl().setWantedPosition(this.kingGroundCombatWaypoint.x,
                            this.kingGroundCombatWaypoint.y, this.kingGroundCombatWaypoint.z, 1.25D);
                }
                if (--this.kingLandingStageTicks <= 0 && !closeQuarters) {
                    this.beginKingTakeoff(target);
                }
                return;
            }
            case TAKEOFF -> {
                if (--this.kingLandingStageTicks <= 0) {
                    this.kingLandingStage = KingLandingStage.AERIAL;
                    this.groundAssaultCooldownTicks = GROUND_ASSAULT_COOLDOWN_MIN
                            + this.random.nextInt(GROUND_ASSAULT_COOLDOWN_VARIANCE + 1);
                    this.kingCombatWaypoint = null;
                }
                return;
            }
            case AERIAL -> {
            }
        }

        if (this.kingCombatWaypoint != null && ++this.kingCombatProgressTicks >= 40) {
            if (this.kingCombatProgressPosition != null
                    && this.position().distanceToSqr(this.kingCombatProgressPosition) < 1.0D
                    && this.position().distanceToSqr(this.kingCombatWaypoint) > 16.0D) {
                this.kingCombatWaypoint = null;
                this.kingStrafeDirection *= -1;
            }
            this.kingCombatProgressPosition = this.position();
            this.kingCombatProgressTicks = 0;
        }
        if (this.kingCombatWaypoint == null || --this.kingCombatWaypointTicks <= 0
                || this.position().distanceToSqr(this.kingCombatWaypoint) < 16.0D) {
            this.selectKingCombatWaypoint(target);
        }
        if (this.kingCombatWaypoint != null) {
            double speed = this.projectilePressure >= 30 ? 1.4D : 1.15D;
            this.getMoveControl().setWantedPosition(this.kingCombatWaypoint.x,
                    this.kingCombatWaypoint.y, this.kingCombatWaypoint.z, speed);
        }
    }

    private void beginKingLanding(LivingEntity target) {
        this.kingLandingWaypoint = this.findKingLandingPoint(target);
        if (this.kingLandingWaypoint != null) {
            this.kingLandingStage = KingLandingStage.TELEGRAPH;
            this.kingLandingStageTicks = LANDING_TELEGRAPH_TICKS;
            this.kingCombatWaypoint = null;
            this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 1.12F);
        } else {
            // Keep Close Quarters from silently dissolving when the first ring is obstructed.
            // The next aerial tick will retry the expanded landing search.
            this.kingLandingWaypoint = null;
            this.groundAssaultCooldownTicks = 100;
        }
    }

    private void beginKingTakeoff(LivingEntity target) {
        this.kingLandingStage = KingLandingStage.TAKEOFF;
        this.kingLandingStageTicks = TAKEOFF_TICKS;
        this.triggerAnim("wing_action", "wing_gust");
        this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.92F);
        if (this.level() instanceof ServerLevel level) {
            this.performWingGust(level, target);
        }
        this.setRoyalFlying(true);
        this.addGravityAwareImpulse(this, AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D, 0.42D, 0.0D, AntarchyGravityApi.getGravityDirection(this)));
        this.hasImpulse = true;
    }

    private void tickKingLandingIndicator() {
        if (!(this.level() instanceof ServerLevel level) || this.kingLandingWaypoint == null
                || this.tickCount % 2 != 0) {
            return;
        }
        double progress = this.kingLandingStage == KingLandingStage.TELEGRAPH
                ? 1.0D - this.kingLandingStageTicks / (double) LANDING_TELEGRAPH_TICKS : 1.0D;
        double radius = Mth.lerp(progress, 4.0D, STOMP_RADIUS);
        for (int index = 0; index < 36; index++) {
            double angle = Mth.TWO_PI * index / 36.0D;
            Vec3 indicator = this.kingLandingWaypoint.add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                    0.0D, 0.2D, 0.0D, AntarchyGravityApi.getGravityDirection(this)));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    this.kingLandingWaypoint.x + Math.cos(angle) * radius,
                    indicator.y,
                    this.kingLandingWaypoint.z + Math.sin(angle) * radius,
                    1, 0.0D, 0.02D, 0.0D, 0.0D);
        }
    }

    @Nullable
    private Vec3 findKingLandingPoint(LivingEntity target) {
        Vec3 away = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        double baseAngle = away.lengthSqr() > 1.0E-4D ? Math.atan2(away.z, away.x) : this.random.nextDouble() * Mth.TWO_PI;
        double[] radii = {9.0D, 13.0D, 17.0D, 6.0D, 4.0D, 2.0D, 0.0D};
        for (int angleIndex = 0; angleIndex < 16; angleIndex++) {
            double angle = baseAngle + angleIndex * Mth.TWO_PI / 16.0D;
            for (double radius : radii) {
                double x = target.getX() + Math.cos(angle) * radius;
                double z = target.getZ() + Math.sin(angle) * radius;
                double y = AntarchyGravityApi.isGravityInverted(this)
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
        double fallbackY = AntarchyGravityApi.isGravityInverted(this)
                ? target.position().y - 0.1D
                : this.groundYBelow(target.getX(), target.getZ()) + 0.1D;
        Vec3 fallback = new Vec3(target.getX(), fallbackY, target.getZ());
        AABB fallbackBox = this.getBoundingBox().move(
                fallback.x - this.getX(), fallback.y - this.getY(), fallback.z - this.getZ());
        if (this.level().noCollision(this, fallbackBox)) {
            return fallback;
        }
        // Last resort: land directly beneath the King if the target ring is obstructed.
        // This still gives Close Quarters a committed ground sequence instead of silently
        // remaining airborne over difficult tree geometry.
        double directY = AntarchyGravityApi.isGravityInverted(this)
                ? this.position().y - 0.1D
                : this.groundYBelow(this.getX(), this.getZ()) + 0.1D;
        Vec3 direct = new Vec3(this.getX(), directY, this.getZ());
        AABB directBox = this.getBoundingBox().move(
                direct.x - this.getX(), direct.y - this.getY(), direct.z - this.getZ());
        return this.level().noCollision(this, directBox) ? direct : null;
    }

    private void selectKingCombatWaypoint(LivingEntity target) {
        Vec3 radial = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        if (radial.lengthSqr() < 1.0E-4D) {
            radial = new Vec3(1.0D, 0.0D, 0.0D);
        }
        radial = radial.normalize();
        double range = this.activeDecree instanceof ComeNoCloserDecree || this.projectilePressure >= 30
                ? 40.0D : this.aerialCombatRangeForPhase();
        double currentAngle = Math.atan2(radial.z, radial.x);
        Vec3 orbitWaypoint = null;
        boolean treeBoundCombat = this.treePatrolBound && this.treePatrolCenter != null;
        double boundaryRadius = treeBoundCombat
                ? Math.max(32.0D, AntarchySettings.royalBoundaryRadius()) - this.getBbWidth() * 0.5D
                : Double.POSITIVE_INFINITY;
        double hoverOffset = this.phase() == Phase.ONE ? 8.0D : this.phase() == Phase.TWO ? 6.0D : 4.0D;
        for (int angleAttempt = 0; angleAttempt < 4 && orbitWaypoint == null; angleAttempt++) {
            double angle = currentAngle + this.kingStrafeDirection * COMBAT_ORBIT_ANGLE_STEP * (angleAttempt + 1);
            double candidateRange = range + (angleAttempt % 2 == 0 ? 0.0D : -6.0D);
            double x = target.getX() + Math.cos(angle) * candidateRange;
            double z = target.getZ() + Math.sin(angle) * candidateRange;
            double groundY = this.groundYBelow(x, z);
            double y = Mth.clamp(target.getY() + hoverOffset,
                    groundY + FLYING_MIN_HOVER, groundY + FLYING_MAX_HOVER_ABOVE_GROUND);
            Vec3 candidate = new Vec3(x, y, z);
            if (treeBoundCombat) {
                double dx = x - this.treePatrolCenter.x;
                double dz = z - this.treePatrolCenter.z;
                if (dx * dx + dz * dz > boundaryRadius * boundaryRadius) {
                    continue;
                }
            }
            if (this.clearTreePatrolPath(candidate)) {
                orbitWaypoint = candidate;
            }
        }
        if (orbitWaypoint == null && treeBoundCombat) {
            // If the tree blocks the next orbital lane, turn away and try the opposite side
            // before falling back to a clear target-relative position.
            this.kingStrafeDirection *= -1;
            double angle = currentAngle + this.kingStrafeDirection * COMBAT_ORBIT_ANGLE_STEP;
            double x = target.getX() + Math.cos(angle) * range;
            double z = target.getZ() + Math.sin(angle) * range;
            double groundY = this.groundYBelow(x, z);
            double y = Mth.clamp(target.getY() + hoverOffset,
                    groundY + FLYING_MIN_HOVER, groundY + FLYING_MAX_HOVER_ABOVE_GROUND);
            Vec3 candidate = new Vec3(x, y, z);
            double dx = candidate.x - this.treePatrolCenter.x;
            double dz = candidate.z - this.treePatrolCenter.z;
            if (dx * dx + dz * dz <= boundaryRadius * boundaryRadius
                    && this.clearTreePatrolPath(candidate)) {
                orbitWaypoint = candidate;
            }
        }
        if (orbitWaypoint == null) {
            double angle = currentAngle + this.kingStrafeDirection * COMBAT_ORBIT_ANGLE_STEP;
            double x = target.getX() + Math.cos(angle) * range;
            double z = target.getZ() + Math.sin(angle) * range;
            double groundY = this.groundYBelow(x, z);
            double y = Mth.clamp(target.getY() + hoverOffset,
                    groundY + FLYING_MIN_HOVER, groundY + FLYING_MAX_HOVER_ABOVE_GROUND);
            orbitWaypoint = new Vec3(x, y, z);
            if (treeBoundCombat) {
                orbitWaypoint = this.findTreePatrolWaypoint(this.treePatrolCenter, this.treePatrolAngle);
            }
        }
        this.kingCombatWaypoint = orbitWaypoint;
        this.kingCombatWaypointTicks = COMBAT_WAYPOINT_MIN_TICKS
                + this.random.nextInt(COMBAT_WAYPOINT_VARIANCE + 1);
        this.kingCombatProgressPosition = this.position();
        this.kingCombatProgressTicks = 0;
        // Keep a consistent orbit direction for several passes so movement reads as circling.
    }

    private void stabilizeKingRotation() {
        Vec3 movement = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        if (movement.lengthSqr() < 0.0025D) {
            return;
        }
        float desiredYaw = (float) (Mth.atan2(movement.z, movement.x) * 180.0D / Math.PI) - 90.0F;
        float turn = Mth.clamp(Mth.wrapDegrees(desiredYaw - this.getYRot()), -7.0F, 7.0F);
        this.setYRot(this.getYRot() + turn);
        this.yBodyRot = this.getYRot();
    }

    private void resetKingCombatMovement() {
        this.kingLandingStage = KingLandingStage.AERIAL;
        this.kingLandingStageTicks = 0;
        this.kingLandingWaypoint = null;
        this.kingCombatWaypoint = null;
        this.kingCombatProgressPosition = null;
        this.kingCombatWaypointTicks = 0;
        this.kingCombatProgressTicks = 0;
        this.kingGroundCombatWaypoint = null;
        this.kingGroundCombatWaypointTicks = 0;
        if (this.groundAssaultCooldownTicks <= 0) {
            this.groundAssaultCooldownTicks = 400;
        }
    }

    private void selectKingGroundCombatWaypoint(LivingEntity target) {
        Vec3 radial = this.position().subtract(target.position()).multiply(1.0D, 0.0D, 1.0D);
        if (radial.lengthSqr() < 1.0E-4D) {
            radial = new Vec3(1.0D, 0.0D, 0.0D);
        }
        radial = radial.normalize();
        Vec3 tangent = new Vec3(-radial.z * this.kingStrafeDirection, 0.0D,
                radial.x * this.kingStrafeDirection);
        double distance = 4.0D + this.random.nextDouble() * 3.0D;
        Vec3 candidate = target.position().add(radial.scale(distance)).add(tangent.scale(2.5D));
        Vec3 waypoint = new Vec3(candidate.x, target.getY(), candidate.z);
        AABB box = this.getBoundingBox().move(waypoint.x - this.getX(), waypoint.y - this.getY(), waypoint.z - this.getZ());
        if (!this.level().noCollision(this, box)) {
            waypoint = target.position().add(radial.scale(distance));
            waypoint = new Vec3(waypoint.x, target.getY(), waypoint.z);
            box = this.getBoundingBox().move(waypoint.x - this.getX(), waypoint.y - this.getY(), waypoint.z - this.getZ());
        }
        this.kingGroundCombatWaypoint = this.level().noCollision(this, box) ? waypoint : target.position();
        this.kingGroundCombatWaypointTicks = GROUND_COMBAT_WAYPOINT_MIN_TICKS
                + this.random.nextInt(GROUND_COMBAT_WAYPOINT_VARIANCE + 1);
        if (this.random.nextInt(4) == 0) {
            this.kingStrafeDirection *= -1;
        }
    }

    public void requestCloseQuartersAssault(LivingEntity target) {
        if (target != null && target.isAlive() && this.isRoyalFlying()
                && this.kingLandingStage == KingLandingStage.AERIAL) {
            this.beginKingLanding(target);
        }
    }

    public RoyalDecree.Evaluation evaluateCloseQuarters(LivingEntity target) {
        UUID targetId = target.getUUID();
        double distanceSquared = target.distanceToSqr(this);
        if (distanceSquared <= CLOSE_QUARTERS_RADIUS * CLOSE_QUARTERS_RADIUS) {
            this.closeQuartersEntered.add(targetId);
            this.closeQuartersOutsideTicks.remove(targetId);
            return RoyalDecree.Evaluation.COMPLIANT;
        }
        if (!this.closeQuartersEntered.contains(targetId)) {
            return 300 - this.activeDecreeTicks >= CLOSE_QUARTERS_APPROACH_TICKS
                    ? RoyalDecree.Evaluation.VIOLATED : RoyalDecree.Evaluation.COMPLIANT;
        }
        int outsideTicks = this.closeQuartersOutsideTicks.merge(targetId, 1, Integer::sum);
        return outsideTicks > CLOSE_QUARTERS_EXIT_GRACE_TICKS
                ? RoyalDecree.Evaluation.VIOLATED : RoyalDecree.Evaluation.COMPLIANT;
    }

    public int closeQuartersCountdownTicks(LivingEntity target) {
        if (this.closeQuartersEntered.contains(target.getUUID())) {
            return -1;
        }
        return Math.max(0, CLOSE_QUARTERS_APPROACH_TICKS - (300 - this.activeDecreeTicks));
    }

    private void tickRoyalPunishment() {
        if (this.exileTarget == null && this.titheTarget == null && this.targetShotPlayer == null && this.sealedTarget == null) {
            return;
        }
        if (this.exileTarget != null) {
            this.tickRoyalExile();
        }
        if (this.titheTarget != null) {
            this.tickRoyalTithe();
        }
        if (this.targetShotPlayer != null) {
            this.tickRoyalTargetShot();
        }
        if (this.sealedTarget != null) {
            this.tickRoyalSeal();
        }
    }

    private void tickRoyalExile() {
        if (this.exileTicks-- <= 0 || !this.exileTarget.isAlive() || this.exileTarget.level() != this.level()
                || this.isDeadOrDying()) {
            this.clearExile();
            return;
        }
        this.exileTarget.lookAt(EntityAnchorArgument.Anchor.EYES, this.getEyePosition());
        this.exileTarget.setPose(Pose.CROUCHING);
        this.exileTarget.setShiftKeyDown(true);
        if (this.exileTicks == 1) {
            Vec3 away = this.exileTarget.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (away.lengthSqr() < 1.0E-4D) {
                away = this.getLookAngle().multiply(-1.0D, 0.0D, -1.0D);
            }
            away = away.normalize();
            this.exileTarget.setDeltaMovement(away.x * 2.6D, 1.35D, away.z * 2.6D);
            this.exileTarget.hasImpulse = true;
            this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.82F);
            if (this.level() instanceof ServerLevel level) {
                this.spawnKingWingPressure(level, away);
            }
            this.exileWingAnimationPending = true;
        }
    }

    private void tickPendingExileAnimation() {
        if (!this.exileWingAnimationPending
                || !this.attackScheduler.ready("exile_wing_animation", RoyalAttackLane.BODY)) {
            return;
        }
        if (this.beginRoyalAttack("exile_wing_animation", RoyalAttackLane.BODY, 1, 0, 1,
                this.animationRecovery(55, 0, 1, 0), new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        KingEntity.this.exileWingAnimationPending = false;
                        KingEntity.this.triggerAnim("wing_action", "wing_gust");
                    }
                })) {
            this.exileWingAnimationPending = false;
        }
    }

    private void tickRoyalTithe() {
        if (!this.titheTarget.isAlive() || this.titheTarget.level() != this.level()
                || this.isDeadOrDying() || !(this.titheTarget.containerMenu instanceof RoyalJudgmentMenu)) {
            this.clearTithe();
            return;
        }
        if (--this.titheTicks <= 0) {
            for (int slot = 0; slot < this.titheTarget.getInventory().getContainerSize(); slot++) {
                ItemStack stack = this.titheTarget.getInventory().getItem(slot);
                if (stack.isEmpty() || !isRoyalTitheItem(stack)) continue;
                ItemStack thrown = stack.split(1);
                this.titheTarget.getInventory().setChanged();
                this.titheTarget.drop(thrown, true);
                this.clearTithe();
                return;
            }
            this.clearTithe();
        }
    }

    private void tickRoyalTargetShot() {
        if (!this.targetShotPlayer.isAlive() || this.targetShotVictim == null || !this.targetShotVictim.isAlive()
                || this.targetShotPlayer.level() != this.level() || this.isDeadOrDying()
                || !(this.targetShotPlayer.containerMenu instanceof RoyalJudgmentMenu)) {
            this.clearTargetShot();
            return;
        }
        this.targetShotPlayer.lookAt(EntityAnchorArgument.Anchor.EYES, this.targetShotVictim.getEyePosition());
        if (--this.targetShotTicks <= 0) {
            ItemStack ranged = findRoyalRangedItem(this.targetShotPlayer);
            if (!ranged.isEmpty()) {
                Arrow arrow = new Arrow((ServerLevel) this.level(), this.targetShotPlayer, ItemStack.EMPTY, ItemStack.EMPTY);
                arrow.setPos(this.targetShotPlayer.getX(), this.targetShotPlayer.getEyeY() - 0.1D, this.targetShotPlayer.getZ());
                Vec3 direction = this.targetShotVictim.getEyePosition().subtract(arrow.position()).normalize();
                arrow.setDeltaMovement(direction.scale(3.0D));
                arrow.setBaseDamage(2.0D);
                ((ServerLevel) this.level()).addFreshEntity(arrow);
                this.targetShotPlayer.displayClientMessage(Component.literal("The King takes your shot."), true);
            }
            this.clearTargetShot();
        }
    }

    private void tickRoyalSeal() {
        if (!this.sealedTarget.isAlive() || this.sealedTarget.level() != this.level() || this.isDeadOrDying()
                || !(this.sealedTarget.containerMenu instanceof RoyalJudgmentMenu) || --this.sealedTicks <= 0) {
            this.clearSeal();
        }
    }

    private void clearExile() {
        if (this.exileTarget != null && this.exileTarget.isAlive()) {
            this.exileTarget.setShiftKeyDown(false);
            this.exileTarget.setPose(Pose.STANDING);
        }
        this.exileTarget = null;
        this.exileTicks = 0;
        this.exileWingAnimationPending = false;
    }

    private void clearTithe() {
        if (this.titheTarget != null && this.titheTarget.containerMenu instanceof RoyalJudgmentMenu) {
            this.titheTarget.closeContainer();
        }
        this.titheTarget = null;
        this.titheTicks = 0;
    }

    private void clearTargetShot() {
        if (this.targetShotPlayer != null && this.targetShotPlayer.containerMenu instanceof RoyalJudgmentMenu) {
            this.targetShotPlayer.closeContainer();
        }
        this.targetShotPlayer = null;
        this.targetShotVictim = null;
        this.targetShotTicks = 0;
    }

    private void clearSeal() {
        if (this.sealedTarget != null) {
            RoyalJudgmentState.clear(this.sealedTarget.getUUID());
            if (this.sealedTarget.containerMenu instanceof RoyalJudgmentMenu) {
                this.sealedTarget.closeContainer();
            }
        }
        this.sealedTarget = null;
        this.sealedTicks = 0;
    }

    private void startTithe(ServerPlayer target) {
        this.clearTithe();
        this.titheTarget = target;
        this.titheTicks = 20;
        target.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.antarchy.royal_judgment");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                     Player player) {
                return new RoyalJudgmentMenu(containerId, inventory);
            }
        });
    }

    private void startTargetShot(ServerPlayer target, ServerPlayer victim) {
        this.clearTargetShot();
        this.targetShotPlayer = target;
        this.targetShotVictim = victim;
        this.targetShotTicks = 20;
        target.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.antarchy.royal_judgment");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                     Player player) {
                return new RoyalJudgmentMenu(containerId, inventory);
            }
        });
    }

    private void startSeal(ServerPlayer target) {
        this.clearSeal();
        java.util.Set<Integer> slots = new java.util.HashSet<>();
        for (int slot = 9; slot < 18; slot++) slots.add(slot);
        RoyalJudgmentState.seal(target.getUUID(), slots);
        this.sealedTarget = target;
        this.sealedTicks = 100;
        target.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("menu.antarchy.royal_judgment");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory,
                                                     Player player) {
                return new RoyalJudgmentMenu(containerId, inventory);
            }
        });
    }

    public boolean debugActivateDecree(String id, ServerPlayer target) {
        for (RoyalDecree decree : DECREES) {
            String decreeId = decree.translationKey().substring(decree.translationKey().lastIndexOf('.') + 1);
            if (decreeId.equalsIgnoreCase(id.replace('-', '_'))) {
                this.endDecree(this.getTarget());
                this.activeDecree = decree;
                this.activeDecreeTicks = 300;
                this.decreeResponseGraceTicks = DECREE_RESPONSE_GRACE_TICKS;
                this.decreeCooldownTicks = 0;
                this.closeQuartersEntered.clear();
                this.closeQuartersOutsideTicks.clear();
                this.decreeRecipients.clear();
                this.projectRoyalSound(AntarchySoundEvents.KING_DECREE.get(), 1.15F, 1.0F, target);
                this.broadcastActiveDecree((ServerLevel) this.level());
                return true;
            }
        }
        return false;
    }

    public boolean debugTriggerPunishment(RoyalPunishmentType type, ServerPlayer target) {
        if (!(this.level() instanceof ServerLevel level) || !target.isAlive() || target.level() != level) {
            return false;
        }
        switch (type) {
            case ROYAL_EXILE -> {
                this.clearExile();
                this.exileTarget = target;
                this.exileTicks = 25;
                return true;
            }
            case ROYAL_TITHE -> {
                this.startTithe(target);
                return true;
            }
            case KINGS_TARGET -> {
                ServerPlayer victim = level.players().stream()
                        .filter(candidate -> candidate != target && candidate.isAlive() && candidate.distanceToSqr(target) <= 4096.0D)
                        .min((left, right) -> Double.compare(left.distanceToSqr(target), right.distanceToSqr(target)))
                        .orElse(null);
                if (victim == null) return false;
                ItemStack ranged = findRoyalRangedItem(target);
                if (ranged.isEmpty()) return false;
                target.displayClientMessage(Component.literal("The King chooses your target."), true);
                this.startTargetShot(target, victim);
                return true;
            }
            case KINGS_SEAL -> {
                this.sealUntil = level.getGameTime() + 100L;
                target.displayClientMessage(Component.literal("The King seals your inventory."), true);
                level.sendParticles(KING_GOLD_DUST, target.getX(), target.getY() + 1.0D, target.getZ(),
                        30, 0.6D, 1.0D, 0.6D, 0.1D);
                this.startSeal(target);
                return true;
            }
        }
        return false;
    }

    private static boolean isRoyalTitheItem(ItemStack stack) {
        return stack.is(net.minecraft.world.item.Items.ARROW)
                || stack.is(net.minecraft.world.item.Items.BREAD)
                || stack.is(net.minecraft.world.item.Items.COBBLESTONE)
                || stack.is(net.minecraft.world.item.Items.POTION);
    }

    private static ItemStack findRoyalRangedItem(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof net.minecraft.world.item.BowItem
                    || stack.getItem() instanceof net.minecraft.world.item.CrossbowItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void trackBehavior(LivingEntity target) {
        double distance = target.distanceTo(this);
        this.bumpBehavior(Behavior.AIRBORNE, !target.onGround() && !target.isPassenger() ? 1 : -1);
        this.bumpBehavior(Behavior.SPRINTING, target.isSprinting() ? 1 : -1);
        this.bumpBehavior(Behavior.HUGGING, distance <= 10.0D ? 1 : -1);
        this.bumpBehavior(Behavior.KEEPING_FAR, distance >= 22.0D ? 1 : -1);
        if (this.patrolCenter != null) {
            this.bumpBehavior(Behavior.RETREATING, distance > 20.0D && target.getDeltaMovement().horizontalDistanceSqr() > 0.02D
                    && target.position().subtract(this.position()).dot(target.getDeltaMovement()) > 0.0D ? 1 : -1);
        }
    }

    private void bumpBehavior(Behavior behavior, int delta) {
        int updated = Math.max(0, Math.min(BEHAVIOR_SCORE_CAP, this.behaviorScores.getOrDefault(behavior, 0) + delta));
        this.behaviorScores.put(behavior, updated);
    }

    public int behaviorScore(Behavior behavior) {
        return this.behaviorScores.getOrDefault(behavior, 0);
    }

    public void setDecreeRetreatPressure(boolean value) {
        this.decreeRetreatPressure = value;
    }

    private void tickKingWholeBody(ServerLevel level, LivingEntity target) {
        if (this.isRoyalRecoveryActive()) {
            return;
        }
        boolean skyIsMine = this.activeDecree instanceof SkyIsMineDecree;
        boolean forceFar = this.decreeRetreatPressure
                || this.activeDecree instanceof ComeNoCloserDecree
                ;

        if (!forceFar && this.activeDecree == null && this.tryStartRoyalCharge(level, target)) {
            return;
        }

        {
            double distance = target.distanceTo(this);
            if (this.attackScheduler.ready("stomp", RoyalAttackLane.BODY)
                    && (distance <= STOMP_RADIUS || skyIsMine)
                    && this.beginRoyalAttack("stomp", RoyalAttackLane.BODY,
                    this.stompCooldownForPhase(skyIsMine),
                    STOMP_WINDUP_TICKS, 1, 18, new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim("body_action", "stomp");
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.7F);
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.performStomp(level, target);
                        }
                    })) {
                if (skyIsMine) {
                    this.getMoveControl().setWantedPosition(this.getX(), target.getY(), this.getZ(), 1.2D);
                }
            } else if (this.attackScheduler.ready("wing_gust", RoyalAttackLane.BODY)
                    && distance <= WING_GUST_RADIUS && (forceFar || this.random.nextInt(this.phase() == Phase.ONE ? 3 : 2) == 0)
                    && this.beginRoyalAttack("wing_gust", RoyalAttackLane.BODY, this.wingGustCooldownForPhase(),
                    WING_GUST_WINDUP_TICKS, 1, this.animationRecovery(55, WING_GUST_WINDUP_TICKS, 1, 16), new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim("wing_action", "wing_gust");
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.85F);
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.performWingGust(level, target);
                        }
                    })) {
            } else if (this.attackScheduler.ready("royal_muster", RoyalAttackLane.BODY)
                    && !this.findRoyalMusterTargets(level).isEmpty()
                    && (forceFar || this.random.nextInt(this.phase() == Phase.THREE ? 3 : 4) == 0)
                    && this.beginRoyalAttack("royal_muster", RoyalAttackLane.BODY,
                    AntarchySettings.kingRoyalMusterCooldownTicks(),
                    AntarchySettings.kingRoyalMusterWindupTicks(), 1,
                    this.animationRecovery(59, AntarchySettings.kingRoyalMusterWindupTicks(), 1, 20),
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim("body_action", "minion_spawn");
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 0.34F, 0.72F);
                            level.sendParticles(KING_GOLD_DUST,
                                    KingEntity.this.getX(), KingEntity.this.getY() + 2.0D, KingEntity.this.getZ(),
                                    36, 5.0D, 2.0D, 5.0D, 0.12D);
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.performRoyalMuster(level);
                        }
                        @Override public void onComplete() {
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.62F);
                        }
                    })) {
            }
        }

        if (forceFar && target.distanceTo(this) < 18.0D) {
            Vec3 away = this.position().subtract(target.position()).normalize().scale(24.0D);
            double ax = this.getX() + away.x;
            double az = this.getZ() + away.z;
            double ay = this.groundYBelow(ax, az) + FLYING_PREFERRED_HOVER + 6.0D;
            this.getMoveControl().setWantedPosition(ax, ay, az, 1.3D);
        }

        this.tickKingElementalProjectiles(level, target);
    }

    private boolean tryStartRoyalCharge(ServerLevel level, LivingEntity target) {
        double distance = this.distanceTo(target);
        if (this.royalChargeOpeningDelayTicks > 0
                || this.kingLandingStage != KingLandingStage.AERIAL
                || distance < ROYAL_CHARGE_MIN_RANGE || distance > ROYAL_CHARGE_MAX_RANGE
                || this.attackScheduler.laneBusy(RoyalAttackLane.LEFT_HEAD)
                || this.attackScheduler.laneBusy(RoyalAttackLane.CENTER_HEAD)
                || this.attackScheduler.laneBusy(RoyalAttackLane.RIGHT_HEAD)
                || !this.attackScheduler.ready("royal_charge", RoyalAttackLane.MOVEMENT)) {
            return false;
        }
        return this.beginRoyalAttack("royal_charge", RoyalAttackLane.MOVEMENT,
                this.royalChargeCooldownForPhase(), ROYAL_CHARGE_WINDUP_TICKS,
                ROYAL_CHARGE_ACTIVE_TICKS, ROYAL_CHARGE_RECOVERY_TICKS,
                new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        KingEntity.this.royalChargeActive = true;
                        KingEntity.this.royalChargeHitTargets.clear();
                        KingEntity.this.lockRoyalChargeDirection(target);
                        KingEntity.this.getNavigation().stop();
                        KingEntity.this.setDeltaMovement(KingEntity.this.getDeltaMovement().scale(0.2D));
                        KingEntity.this.triggerAnim("wing_action", "wing_gust");
                        KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_WING_FLAP.get(), 0.55F, 0.78F);
                    }

                    @Override
                    public void onWindup(int elapsedTicks, int remainingTicks) {
                        KingEntity.this.getNavigation().stop();
                        KingEntity.this.setDeltaMovement(KingEntity.this.getDeltaMovement().scale(0.72D));
                        if (target.isAlive() && target.level() == level && remainingTicks > 8) {
                            KingEntity.this.lockRoyalChargeDirection(target);
                        }
                        KingEntity.this.sendRoyalChargeTelegraph(level, elapsedTicks);
                    }

                    @Override
                    public void onActive(int elapsedTicks) {
                        KingEntity.this.setRoyalFlying(true);
                        KingEntity.this.setDeltaMovement(KingEntity.this.royalChargeDirection.scale(ROYAL_CHARGE_SPEED));
                        KingEntity.this.hasImpulse = true;
                        KingEntity.this.applyRoyalChargeHits(level);
                        level.sendParticles(elapsedTicks % 3 == 0 ? ParticleTypes.END_ROD : KING_GOLD_DUST,
                                KingEntity.this.getX(), KingEntity.this.getY() + KingEntity.this.getBbHeight() * 0.5D,
                                KingEntity.this.getZ(), 6, KingEntity.this.getBbWidth() * 0.3D,
                                KingEntity.this.getBbHeight() * 0.22D, KingEntity.this.getBbWidth() * 0.3D, 0.03D);
                        if (elapsedTicks == ROYAL_CHARGE_ACTIVE_TICKS - 1) {
                            KingEntity.this.setDeltaMovement(KingEntity.this.getDeltaMovement().scale(0.12D));
                        }
                    }

                    @Override
                    public void onComplete() {
                        KingEntity.this.royalChargeActive = false;
                        KingEntity.this.royalChargeDirection = Vec3.ZERO;
                        KingEntity.this.royalChargeHitTargets.clear();
                        KingEntity.this.setDeltaMovement(KingEntity.this.getDeltaMovement().scale(0.2D));
                        KingEntity.this.startRoyalRecovery(16);
                    }
                });
    }

    private void lockRoyalChargeDirection(LivingEntity target) {
        Vec3 aimPoint = target.getEyePosition().add(target.getDeltaMovement().scale(4.0D));
        Vec3 direction = aimPoint.subtract(this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D));
        if (direction.lengthSqr() > 1.0E-4D) {
            this.royalChargeDirection = direction.normalize();
            this.getLookControl().setLookAt(aimPoint.x, aimPoint.y, aimPoint.z, 30.0F, 30.0F);
        }
    }

    private void sendRoyalChargeTelegraph(ServerLevel level, int elapsedTicks) {
        if (this.royalChargeDirection.lengthSqr() < 1.0E-4D || elapsedTicks % 2 != 0) {
            return;
        }
        Vec3 origin = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        double length = 12.0D + elapsedTicks * 1.25D;
        int samples = Math.max(6, (int) (length / 2.5D));
        for (int sample = 1; sample <= samples; sample++) {
            Vec3 point = origin.add(this.royalChargeDirection.scale(length * sample / samples));
            level.sendParticles(sample % 4 == 0 ? ParticleTypes.END_ROD : KING_GOLD_DUST,
                    point.x, point.y, point.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
        }
    }

    private void applyRoyalChargeHits(ServerLevel level) {
        AABB hitBox = this.getBoundingBox().inflate(2.25D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> entity.isAlive() && entity instanceof Player && this.canDamageWithRoyalAttack(entity))) {
            if (!this.royalChargeHitTargets.add(living.getUUID())) {
                continue;
            }
            living.hurt(this.damageSources().mobAttack(this), this.scaleRoyalDamage(12.0D));
            Vec3 lift = AntarchyGravityRotationUtil.vecPlayerToWorld(
                    0.0D, 0.45D, 0.0D, AntarchyGravityApi.getGravityDirection(living));
            this.addGravityAwareImpulse(living, this.royalChargeDirection.scale(2.1D).add(lift));
            living.hasImpulse = true;
            level.sendParticles(ParticleTypes.END_ROD, living.getX(), living.getY() + living.getBbHeight() * 0.5D,
                    living.getZ(), 18, 0.55D, 0.7D, 0.55D, 0.08D);
        }
    }

    private int royalChargeCooldownForPhase() {
        return switch (this.phase()) {
            case ONE -> 300;
            case TWO -> 250;
            case THREE -> 210;
        };
    }

    private int groundAssaultTicksForPhase() {
        return switch (this.phase()) {
            case ONE -> GROUND_ASSAULT_TICKS;
            case TWO -> GROUND_ASSAULT_TICKS + 40;
            case THREE -> GROUND_ASSAULT_TICKS + 80;
        };
    }

    private double aerialCombatRangeForPhase() {
        return switch (this.phase()) {
            case ONE -> 34.0D;
            case TWO -> 29.0D;
            case THREE -> 24.0D;
        };
    }

    private int stompCooldownForPhase(boolean skyIsMine) {
        double scale = switch (this.phase()) {
            case ONE -> 1.0D;
            case TWO -> 0.88D;
            case THREE -> 0.72D;
        };
        int cooldown = Mth.floor(STOMP_COOLDOWN_TICKS * scale);
        return skyIsMine ? cooldown / 2 : cooldown;
    }

    private int wingGustCooldownForPhase() {
        double scale = switch (this.phase()) {
            case ONE -> 1.0D;
            case TWO -> 0.88D;
            case THREE -> 0.72D;
        };
        return Mth.floor(WING_GUST_COOLDOWN_TICKS * scale);
    }

    private List<Mob> findRoyalMusterTargets(ServerLevel level) {
        double radius = AntarchySettings.kingRoyalMusterRadius();
        return level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(radius), mob ->
                mob.isAlive() && mob != this
                        && (mob.getType().getCategory() == MobCategory.MONSTER || mob instanceof Enemy)
                        && !(mob instanceof RoyalBossEntity)
                        && RoyalEffectEligibility.canApplyCommanded(mob));
    }

    private void performRoyalMuster(ServerLevel level) {
        var commanded = RoyalEffectHooks.commandedHolder();
        if (commanded == null) {
            return;
        }
        int applied = 0;
        for (Mob mob : this.findRoyalMusterTargets(level)) {
            if (applied >= AntarchySettings.kingRoyalMusterCap()) {
                break;
            }
            if (mob.addEffect(new MobEffectInstance(commanded, AntarchySettings.kingRoyalMusterDurationTicks()), this)) {
                applied++;
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        mob.getX(), mob.getY() + mob.getBbHeight() + 0.3D, mob.getZ(),
                        5, 0.3D, 0.2D, 0.3D, 0.02D);
            }
        }
        if (applied > 0) {
            this.playRoyalSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 0.34F, 0.95F);
        }
    }

    private void tickKingElementalProjectiles(ServerLevel level, LivingEntity target) {
        if (this.fireballCooldownTicks > 0) this.fireballCooldownTicks--;
        if (this.iceballCooldownTicks > 0) this.iceballCooldownTicks--;
        if (this.iceSpikeCooldownTicks > 0) this.iceSpikeCooldownTicks--;

        double distance = this.distanceTo(target);
        String[] candidates = {"fireball", "iceball", "ice_spikes"};
        String selected = this.attackScheduler.chooseWeighted(candidates, id -> {
            boolean ready = switch (id) {
                case "fireball" -> distance >= 14.0D && distance <= 48.0D && this.fireballCooldownTicks <= 0
                        && this.royalHead(RoyalHead.Slot.LEFT).readyToAttack();
                case "iceball" -> distance >= 10.0D && distance <= 42.0D && this.iceballCooldownTicks <= 0
                        && this.royalHead(RoyalHead.Slot.RIGHT).readyToAttack();
                default -> distance <= 36.0D && this.iceSpikeCooldownTicks <= 0
                        && this.royalHead(RoyalHead.Slot.RIGHT).readyToAttack();
            };
            if (!ready) return 0;
            return switch (id) {
                case "fireball" -> this.behaviorScore(Behavior.KEEPING_FAR) + 4;
                case "iceball" -> this.behaviorScore(Behavior.RETREATING) + 4;
                default -> this.behaviorScore(Behavior.HUGGING) + 3;
            };
        }, this.random);
        if (selected == null) return;
        if ("fireball".equals(selected)) {
            if (!this.beginRoyalAttack("fireball", RoyalAttackLane.LEFT_HEAD,
                    AntarchySettings.kingFireballCooldownTicks(), 14, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim(RoyalHead.Slot.LEFT.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            Vec3 origin = KingEntity.this.headAnchor(KingEntity.this.royalHead(RoyalHead.Slot.LEFT));
                            Vec3 predicted = target.position().add(target.getDeltaMovement().scale(10.0D));
                            Vec3 direction = predicted.add(0.0D, target.getBbHeight() * 0.5D, 0.0D).subtract(origin).normalize();
                            RoyalElementalProjectileEntity fireball = RoyalElementalProjectileEntity.create(level, KingEntity.this,
                                    RoyalBeamElement.FIRE, origin, direction);
                            level.addFreshEntity(fireball);
                            if (KingEntity.this.random.nextInt(5) == 0) {
                                KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_FIREBALL_SHOOT.get(), 0.18F, 0.9F);
                            }
                        }
                    })) return;
            this.fireballCooldownTicks = AntarchySettings.kingFireballCooldownTicks();
        } else if ("iceball".equals(selected)) {
            if (!this.beginRoyalAttack("iceball", RoyalAttackLane.RIGHT_HEAD,
                    AntarchySettings.kingIceballCooldownTicks(), 16, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim(RoyalHead.Slot.RIGHT.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            Vec3 origin = KingEntity.this.headAnchor(KingEntity.this.royalHead(RoyalHead.Slot.RIGHT));
                            Vec3 direction = target.getEyePosition().subtract(origin).normalize();
                            RoyalElementalProjectileEntity iceball = RoyalElementalProjectileEntity.create(level, KingEntity.this,
                                    RoyalBeamElement.ICE, origin, direction);
                            level.addFreshEntity(iceball);
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_BEAM_SHOOT.get(), 1.2F);
                        }
                    })) return;
            this.iceballCooldownTicks = AntarchySettings.kingIceballCooldownTicks();
        } else {
            if (!this.beginRoyalAttack("ice_spikes", RoyalAttackLane.RIGHT_HEAD,
                    AntarchySettings.kingIceSpikeCooldownTicks(), 20, 1, 5,
                    new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                        @Override public void onStart() {
                            KingEntity.this.triggerAnim(RoyalHead.Slot.RIGHT.controllerName(), "shoot");
                        }
                        @Override public void onActive(int elapsedTicks) {
                            KingEntity.this.raiseIceSpikes(level, target);
                            KingEntity.this.playRoyalSound(AntarchySoundEvents.KING_ICE_SPIKES.get(), 0.28F, 0.8F);
                        }
                    })) return;
            this.iceSpikeCooldownTicks = AntarchySettings.kingIceSpikeCooldownTicks();
        }
    }

    private void raiseIceSpikes(ServerLevel level, LivingEntity target) {
        Vec3 start = this.position();
        Vec3 end = target.position();
        Vec3 line = end.subtract(start).multiply(1.0D, 0.0D, 1.0D);
        if (line.lengthSqr() < 1.0E-4D) return;
        line = line.normalize();
        Vec3 side = new Vec3(-line.z, 0.0D, line.x);
        for (int i = 1; i <= 5; i++) {
            double distance = i * 4.0D;
            for (int lane = -1; lane <= 1; lane++) {
                Vec3 point = start.add(line.scale(distance)).add(side.scale(lane * (i % 2 == 0 ? 2.0D : 0.0D)));
                double y = this.groundYBelow(point.x, point.z) + 0.1D;
                RoyalIceSpikeEntity spike = RoyalIceSpikeEntity.create(level, new Vec3(point.x, y, point.z), this);
                level.addFreshEntity(spike);
            }
        }
    }

    private void performStomp(ServerLevel level, LivingEntity target) {
        DamageSource source = this.damageSources().mobAttack(this);
        float base = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 1.4F;
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(STOMP_RADIUS),
                e -> e.isAlive() && e instanceof Player)) {
            double distance = Math.max(1.0D, living.distanceTo(this));
            float falloff = (float) Math.max(0.2D, 1.0D - distance / (STOMP_RADIUS + 6.0D));
            living.hurt(source, base * falloff);
            Vec3 push = living.position().subtract(this.position()).normalize().scale(2.4D * falloff);
            Vec3 vertical = AntarchyGravityRotationUtil.vecPlayerToWorld(
                    0.0D, 0.85D, 0.0D, AntarchyGravityApi.getGravityDirection(living));
            this.addGravityAwareImpulse(living, new Vec3(push.x, 0.0D, push.z).add(vertical));
            living.hasImpulse = true;
        }
        RoyalBlockDestruction.destroySphere(level, this,
                new Vec3(this.getX(), this.getY(), this.getZ()), STOMP_RADIUS * 0.5D, 96, 80.0D, 0.15F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 6, 4.0D, 0.5D, 4.0D, 0.0D);
        this.playRoyalSound(AntarchySoundEvents.KING_STOMP.get(), 0.8F);
    }

    private void performWingGust(ServerLevel level, LivingEntity target) {
        Vec3 flat = this.getForward().multiply(1.0D, 0.0D, 1.0D);
        Vec3 forward = flat.lengthSqr() < 1.0E-4D
                ? target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize()
                : flat.normalize();
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(WING_GUST_RADIUS),
                e -> e.isAlive() && e instanceof Player)) {
            Vec3 toTarget = living.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D);
            if (toTarget.lengthSqr() < 1.0E-4D || forward.dot(toTarget.normalize()) < 0.1D) {
                continue;
            }
            Vec3 push = toTarget.normalize().scale(3.2D);
            living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.5D, push.z));
            living.hasImpulse = true;
            living.hurt(this.damageSources().mobAttack(this), this.scaleRoyalDamage(6.0D));
        }
        this.spawnKingWingPressure(level, forward);
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX() + forward.x * 6.0D, this.getY() + 4.0D, this.getZ() + forward.z * 6.0D, 12, 4.0D, 2.0D, 4.0D, 0.0D);
    }

    private void spawnKingWingPressure(ServerLevel level, Vec3 forward) {
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        Vec3 origin = this.position().add(0.0D, this.getBbHeight() * 0.55D, 0.0D);
        for (int band = 1; band <= 5; band++) {
            double distance = band * 3.0D;
            double width = 1.2D + band * 1.05D;
            for (int sample = -3; sample <= 3; sample++) {
                Vec3 point = origin.add(forward.scale(distance)).add(side.scale(width * sample / 3.0D));
                Vec3 velocity = forward.scale(0.18D + band * 0.015D).add(0.0D, 0.025D, 0.0D);
                level.sendParticles(sample % 2 == 0 ? KING_WHITE_DUST : ParticleTypes.CLOUD,
                        point.x, point.y, point.z, 0, velocity.x, velocity.y, velocity.z, 1.0D);
            }
        }
    }

    private void tickDecree(ServerLevel level, LivingEntity target) {
        if (this.isRoyalRecoveryActive() && this.activeDecree == null) {
            return;
        }
        List<ServerPlayer> participants = this.royalEncounterPlayers(level);
        if (participants.isEmpty()) {
            if (this.activeDecree != null) {
                this.endDecree(target);
            }
            this.activeDecree = null;
            this.activeDecreeTicks = 0;
            this.decreeRetreatPressure = false;
            return;
        }
        if (this.activeDecree == null) {
            if (this.decreeCooldownTicks-- > 0) {
                return;
            }
            int decreeCooldown = this.decreeCooldownForPhase();
            if (!this.beginRoyalAttack("decree", RoyalAttackLane.DECREE,
                    decreeCooldown, 300)) {
                return;
            }
            this.activeDecreeTicks = 300;
            this.decreeResponseGraceTicks = DECREE_RESPONSE_GRACE_TICKS;
            this.activeDecree = this.pickDecree(target);
            this.decreeRetreatPressure = false;
            this.closeQuartersEntered.clear();
            this.closeQuartersOutsideTicks.clear();
            this.decreeRecipients.clear();
            this.playRoyalSound(AntarchySoundEvents.KING_DECREE_CAST.get(), 0.34F,
                    0.9F + this.random.nextFloat() * 0.15F);
            this.playRoyalSound(AntarchySoundEvents.KING_ROAR.get(), 0.8F + this.random.nextFloat() * 0.12F);
            this.projectRoyalSound(AntarchySoundEvents.KING_DECREE.get(), 1.15F, 1.0F, target);
            this.broadcastActiveDecree(level);
        }
        this.broadcastActiveDecree(level);
        boolean hasCompletableParticipants = false;
        boolean everyParticipantComplete = true;
        for (ServerPlayer participant : participants) {
            if (this.activeDecreeTicks % 20 == 0) {
                int deadlineTicks = this.activeDecree.countdownTicks(this, participant);
                int countdownTicks = deadlineTicks >= 0 ? deadlineTicks : this.activeDecreeTicks;
                int seconds = Math.max(0, (countdownTicks + 19) / 20);
                ChatFormatting countdownColor = seconds <= 3 ? ChatFormatting.RED : ChatFormatting.GOLD;
                participant.displayClientMessage(Component.empty()
                        .append(Component.translatable(this.activeDecree.translationKey())
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                        .append(Component.literal("  " + seconds).withStyle(countdownColor, ChatFormatting.BOLD)), true);
                float volume = seconds <= 3 ? 0.8F : 0.45F;
                float pitch = 1.0F + Math.max(0, 15 - seconds) * 0.035F;
                participant.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, volume, pitch);
            }
            this.activeDecree.apply(level, this, participant);
            if (this.activeDecree == null) {
                return;
            }
            RoyalDecree.Evaluation evaluation = this.activeDecree.evaluate(level, this, participant);
            if (evaluation == RoyalDecree.Evaluation.VIOLATED && this.decreeResponseGraceTicks <= 0) {
                this.failActiveDecree(participant);
                return;
            }
            if (evaluation == RoyalDecree.Evaluation.COMPLETE) {
                hasCompletableParticipants = true;
            } else {
                everyParticipantComplete = false;
            }
        }
        if (this.activeDecree != null && hasCompletableParticipants && everyParticipantComplete) {
            this.projectRoyalSound(AntarchySoundEvents.KING_SUCCESS.get(), 4.0F, 1.0F, target);
            this.endDecree(target);
            return;
        }
        if (this.decreeResponseGraceTicks > 0) {
            this.decreeResponseGraceTicks--;
        }
        if (this.activeDecree != null && --this.activeDecreeTicks <= 0) {
            this.projectRoyalSound(AntarchySoundEvents.KING_SUCCESS.get(), 4.0F, 1.0F, target);
            this.endDecree(target);
        }
    }

    private RoyalDecree pickDecree(LivingEntity target) {
        if ((this.projectilePressure >= 20 || this.behaviorScore(Behavior.RANGED) >= 6)
                && !"decree.antarchy.close_quarters".equals(this.lastDecreeKey)) {
            return DECREES.stream()
                    .filter(CloseQuartersDecree.class::isInstance)
                    .findFirst()
                    .orElse(DECREES.get(0));
        }
        int totalWeight = 0;
        int[] weights = new int[DECREES.size()];
        for (int i = 0; i < DECREES.size(); i++) {
            RoyalDecree decree = DECREES.get(i);
            weights[i] = decree.translationKey().equals(this.lastDecreeKey)
                    ? 0 : Math.max(1, decree.contextWeight(this, target));
            totalWeight += weights[i];
        }
        int roll = this.random.nextInt(totalWeight);
        for (int i = 0; i < DECREES.size(); i++) {
            roll -= weights[i];
            if (roll < 0) {
                return DECREES.get(i);
            }
        }
        return DECREES.get(this.random.nextInt(DECREES.size()));
    }

    private void sendDecreeTitle(ServerPlayer player) {
        if (this.activeDecree == null) return;
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 300, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable(this.activeDecree.translationKey())
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true))));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable(this.activeDecree.instructionKey())
                .withStyle(style -> style.withColor(ChatFormatting.WHITE))));
    }

    private void broadcastActiveDecree(ServerLevel level) {
        if (this.activeDecree == null) {
            return;
        }
        for (ServerPlayer participant : this.royalEncounterPlayers(level)) {
            if (this.decreeRecipients.add(participant.getUUID())) {
                if (this.activeDecree instanceof ShowNoMercyDecree) {
                    this.lastPlayerDamageTime.put(participant.getUUID(), level.getGameTime());
                }
                this.sendDecreeTitle(participant);
            }
        }
    }

    private int decreeCooldownForPhase() {
        double scale = switch (this.phase()) {
            case ONE -> 1.0D;
            case TWO -> 0.82D;
            case THREE -> 0.65D;
        };
        return Math.max(200, Mth.floor(AntarchySettings.royalDecreeCooldownTicks() * scale));
    }

    private void endDecree(@Nullable LivingEntity target) {
        if (this.level() instanceof ServerLevel level) {
            for (UUID recipientId : this.decreeRecipients) {
                Player recipientPlayer = level.getPlayerByUUID(recipientId);
                if (recipientPlayer instanceof ServerPlayer recipient) {
                    recipient.connection.send(new ClientboundClearTitlesPacket(false));
                    recipient.displayClientMessage(Component.empty(), true);
                }
            }
        }
        if (this.activeDecree != null) {
            this.lastDecreeKey = this.activeDecree.translationKey();
            this.activeDecree.onEnded();
        }
        this.activeDecree = null;
        this.activeDecreeTicks = 0;
        this.decreeResponseGraceTicks = 0;
        this.decreeRetreatPressure = false;
        this.closeQuartersEntered.clear();
        this.closeQuartersOutsideTicks.clear();
        this.decreeRecipients.clear();
        this.decreeCooldownTicks = this.decreeCooldownForPhase();
        this.startRoyalRecovery(40);
    }

    public void clearActiveDecree() {
        this.clearActiveDecree(null);
    }

    public void clearActiveDecree(@Nullable LivingEntity target) {
        this.endDecree(target);
    }

    public void failActiveDecree(@Nullable LivingEntity target) {
        if (this.activeDecree == null || target == null || !target.isAlive()) {
            return;
        }
        this.attackScheduler.cancel(RoyalAttackLane.DECREE);
        this.attackScheduler.cancel(RoyalAttackLane.HAZARD);
        if (target instanceof ServerPlayer player) {
            player.addEffect(new MobEffectInstance(
                    AntarchyObjects.ROYAL_VITALITY_PENALTY_EFFECT.get(), 20 * 30, 0, false, false, false));
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        }
        this.endDecree(target);
        this.projectRoyalSound(AntarchySoundEvents.KING_JUDGEMENT.get(), 1.1F, 1.0F, target);
        if (target instanceof ServerPlayer player) {
            this.sendDecreeViolationTitle(player);
        }
        this.startRoyalJudgmentBarrage(target);
    }

    private void sendDecreeViolationTitle(ServerPlayer player) {
        player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 45, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("decree.antarchy.violated")
                .withStyle(style -> style.withColor(ChatFormatting.GOLD).withBold(true))));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("decree.antarchy.violated.subtitle")
                .withStyle(style -> style.withColor(ChatFormatting.YELLOW))));
    }

    private void startRoyalJudgmentBarrage(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.beginRoyalAttack("royal_judgment", RoyalAttackLane.HAZARD, 80,
                20, 9, 12, new com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
                            KingEntity.this.triggerAnim(slot.controllerName(), "shoot");
                        }
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                KingEntity.this.getX(), KingEntity.this.getY() + KingEntity.this.getBbHeight() * 0.7D,
                                KingEntity.this.getZ(), 70, 4.0D, 3.0D, 4.0D, 0.12D);
                        KingEntity.this.sendRoyalJudgmentTelegraph(level, target, 0);
                        Vec3 targetFlash = target.getEyePosition();
                        level.sendParticles(ParticleTypes.END_ROD, targetFlash.x, targetFlash.y, targetFlash.z,
                                24, 0.35D, 0.45D, 0.35D, 0.04D);
                    }

                    @Override
                    public void onWindup(int elapsedTicks, int remainingTicks) {
                        if (target.isAlive() && target.level() == level) {
                            KingEntity.this.sendRoyalJudgmentTelegraph(level, target, elapsedTicks);
                        }
                    }

                    @Override
                    public void onActive(int elapsedTicks) {
                        if (target.isAlive() && target.level() == level) {
                            if (elapsedTicks == 0) {
                                Vec3 targetFlash = target.getEyePosition();
                                level.sendParticles(ParticleTypes.END_ROD, targetFlash.x, targetFlash.y, targetFlash.z,
                                        32, 0.45D, 0.55D, 0.45D, 0.05D);
                                if (target instanceof ServerPlayer player) {
                                    KingJudgmentFlashSync.send(player, 12);
                                    player.playNotifySound(SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 0.8F, 1.65F);
                                }
                                target.hurt(KingEntity.this.damageSources().magic(),
                                        KingEntity.this.scaleRoyalDamage(6.0D));
                            }
                        }
                        if (elapsedTicks % 4 == 0 && target.isAlive() && target.level() == level) {
                            KingEntity.this.fireRoyalJudgmentWave(level, target);
                        }
                    }
        });
    }

    private void sendRoyalJudgmentTelegraph(ServerLevel level, LivingEntity target, int elapsedTicks) {
        Vec3 targetPoint = target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D, 0.35D, 0.0D, AntarchyGravityApi.getGravityDirection(target)));
        double pulse = 0.8D + Math.min(1.0D, elapsedTicks / 20.0D) * 0.5D;
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            RoyalHead head = this.royalHead(slot);
            Vec3 origin = this.headAnchor(head);
            Vec3 direction = targetPoint.subtract(origin);
            double length = direction.length();
            if (length < 0.01D) {
                continue;
            }
            direction = direction.scale(1.0D / length);
            int samples = Math.max(4, (int) (length / 2.0D));
            for (int sample = 1; sample <= samples; sample++) {
                Vec3 point = origin.add(direction.scale(length * sample / samples));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                        1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        }
        double radius = 1.4D + pulse * 0.8D;
        for (int index = 0; index < 20; index++) {
            double angle = Mth.TWO_PI * index / 20.0D;
            Vec3 point = targetPoint.add(Math.cos(angle) * radius, 0.0D, Math.sin(angle) * radius);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    1, 0.0D, 0.03D, 0.0D, 0.0D);
        }
    }

    private void fireRoyalJudgmentWave(ServerLevel level, LivingEntity target) {
        Vec3 predicted = target.getEyePosition().add(target.getDeltaMovement().scale(4.0D));
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            RoyalHead head = this.royalHead(slot);
            Vec3 origin = this.headAnchor(head);
            Vec3 direction = predicted.subtract(origin).normalize();
            RoyalBoltEntity bolt = new RoyalBoltEntity(AntarchyObjects.ROYAL_BOLT.get(), level);
            bolt.setOwner(this);
            bolt.setElement(RoyalBeamElement.KING_GOLD);
            bolt.setPos(origin.x, origin.y, origin.z);
            bolt.shoot(direction.x, direction.y, direction.z, 1.55F, 1.0F);
            level.addFreshEntity(bolt);
        }
        this.playRoyalSound(AntarchySoundEvents.KING_BEAM_SHOOT.get(), 1.15F);
    }

    public boolean blocksHealing() {
        return this.activeDecree instanceof NoRespiteDecree;
    }

    public long ticksSincePlayerDamage(LivingEntity target) {
        long last = this.lastPlayerDamageTime.getOrDefault(target.getUUID(), 0L);
        return this.level().getGameTime() - last;
    }

    public void invokeJudgment(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        long now = this.level().getGameTime();
        if (this.judgmentCooldowns.getOrDefault(target.getUUID(), 0L) <= now) {
            this.judgmentCooldowns.put(target.getUUID(), now + 20L);
            this.projectRoyalSound(AntarchySoundEvents.KING_JUDGEMENT.get(), 1.1F, 1.0F, target);
            target.hurt(this.damageSources().magic(), this.scaleRoyalDamage(6.0D));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            this.lastPlayerDamageTime.put(attacker.getUUID(), this.level().getGameTime());
            boolean projectile = source.getDirectEntity() instanceof Projectile;
            this.bumpBehavior(projectile ? Behavior.RANGED : Behavior.HUGGING, 2);
            if (projectile) {
                this.projectilePressure = Math.min(PROJECTILE_PRESSURE_MAX,
                        this.projectilePressure + PROJECTILE_PRESSURE_PER_HIT);
                if (this.isRoyalFlying() && !(this.activeDecree instanceof CloseQuartersDecree)) {
                    if (this.projectilePressure >= 40) {
                        amount *= 0.55F;
                    } else if (this.projectilePressure >= 20) {
                        amount *= 0.75F;
                    }
                }
            }
            boolean decreeApplies = attacker instanceof ServerPlayer
                    && this.decreeRecipients.contains(attacker.getUUID());
            if (decreeApplies && this.decreeResponseGraceTicks <= 0) {
                if (this.activeDecree instanceof CloseQuartersDecree && projectile) {
                    this.failActiveDecree(attacker);
                } else if (this.activeDecree instanceof ComeNoCloserDecree && !projectile) {
                    this.failActiveDecree(attacker);
                } else if (this.activeDecree instanceof HandsOffTheCrownDecree) {
                    this.failActiveDecree(attacker);
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("TreePatrolBound", this.treePatrolBound);
        if (this.treePatrolCenter != null) {
            tag.putDouble("TreePatrolCenterX", this.treePatrolCenter.x);
            tag.putDouble("TreePatrolCenterY", this.treePatrolCenter.y);
            tag.putDouble("TreePatrolCenterZ", this.treePatrolCenter.z);
            tag.putDouble("TreePatrolMinimumY", this.treePatrolMinimumY);
            tag.putDouble("TreePatrolMaximumY", this.treePatrolMaximumY);
            tag.putDouble("TreePatrolAngle", this.treePatrolAngle);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("TreePatrolBound") && tag.contains("TreePatrolCenterX")
                && tag.contains("TreePatrolCenterY") && tag.contains("TreePatrolCenterZ")) {
            this.setTreePatrolHome(
                    new Vec3(tag.getDouble("TreePatrolCenterX"), tag.getDouble("TreePatrolCenterY"), tag.getDouble("TreePatrolCenterZ")),
                    tag.getDouble("TreePatrolMinimumY"), tag.getDouble("TreePatrolMaximumY"), tag.getDouble("TreePatrolAngle"));
        }
    }

    public static boolean blocksHealingAround(LivingEntity target) {
        if (target.level().isClientSide) return false;
        return target.level().getEntitiesOfClass(KingEntity.class, target.getBoundingBox().inflate(128.0D),
                king -> king.isAlive() && king.blocksHealing()).stream().findFirst().isPresent();
    }
}
