package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.multipart.MultipartEntityOwner;
import com.craisinlord.antarchy.content.entity.multipart.MultipartLayout;
import com.craisinlord.antarchy.content.entity.multipart.MultipartPartDefinition;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamController;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamSettings;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackLane;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

public abstract class RoyalBossEntity extends Monster implements GeoEntity, MultipartEntityOwner {
    public static final float MODEL_RENDER_SCALE = 2.0F;
    public static final float GAMEPLAY_WIDTH = 28.0F;
    public static final float GAMEPLAY_HEIGHT = 15.0F;

    public enum Phase {
        ONE(1, 1.00F),
        TWO(2, 0.72F),
        THREE(3, 0.48F);

        private final int maxConcurrentHeadAttacks;
        private final float cooldownScale;

        Phase(int maxConcurrentHeadAttacks, float cooldownScale) {
            this.maxConcurrentHeadAttacks = maxConcurrentHeadAttacks;
            this.cooldownScale = cooldownScale;
        }

        public int maxConcurrentHeadAttacks() {
            return this.maxConcurrentHeadAttacks;
        }

        public float cooldownScale() {
            return this.cooldownScale;
        }
    }

    private static final float PHASE_TWO_THRESHOLD = 0.70F;
    private static final float PHASE_THREE_THRESHOLD = 0.35F;

    private static final int BITE_DURATION_TICKS = 16;
    private static final int BITE_HIT_TICK = 6;
    private static final int HEAD_ATTACK_ANIMATION_TICKS = 25;
    private static final int DEATH_TICKS = 60;
    private static final double CRUSH_RADIUS = 3.5D;
    private static final int CRUSH_MAX_BLOCKS = 24;
    private static final double CRUSH_MAX_RESISTANCE = 60.0D;
    private static final float CRUSH_DROP_CHANCE = 0.1F;
    private static final int RECOVERY_AFTER_BEAM_TICKS = 30;
    private static final ResourceLocation MULTIPLAYER_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_multiplayer_damage");
    private static final int OBSTRUCTION_CLEAR_INTERVAL_TICKS = 5;
    private static final double OBSTRUCTION_CLEAR_RADIUS = 3.5D;
    private static final int OBSTRUCTION_CLEAR_MAX_BLOCKS = 24;
    private static final double OBSTRUCTION_CLEAR_MAX_RESISTANCE = 60.0D;
    private static final float OBSTRUCTION_CLEAR_DROP_CHANCE = 0.1F;
    private static final double BEAM_MUZZLE_FORWARD = 27.375D;
    private static final double BEAM_MUZZLE_CENTER_Y = 11.5D;
    private static final double BEAM_MUZZLE_SIDE_Y = 10.25D;
    private static final double BEAM_MUZZLE_LATERAL = 2.625D;

    protected static final double FLYING_MIN_HOVER = 2.5D;
    protected static final double FLYING_PREFERRED_HOVER = 6.0D;
    protected static final double FLYING_MAX_HOVER_ABOVE_GROUND = 24.0D;

    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Boolean>[] BEAM_ACTIVE = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN)
    };
    private static final EntityDataAccessor<Boolean> ACCELERATED =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROYAL_FLYING =
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.BOOLEAN);
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Integer>[] BEAM_ELEMENT = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.INT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.INT)
    };
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] BEAM_END_X = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT)
    };
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] BEAM_END_Y = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT)
    };
    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Float>[] BEAM_END_Z = new EntityDataAccessor[] {
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT),
            SynchedEntityData.defineId(RoyalBossEntity.class, EntityDataSerializers.FLOAT)
    };

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlayAndHold("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected final RoyalBeamController[] beamControllers = new RoyalBeamController[3];
    protected final RoyalAttackScheduler attackScheduler = new RoyalAttackScheduler();
    private final ServerBossEvent bossEvent;
    private final RoyalHead[] heads = {
            new RoyalHead(RoyalHead.Slot.LEFT),
            new RoyalHead(RoyalHead.Slot.CENTER),
            new RoyalHead(RoyalHead.Slot.RIGHT)
    };
    private final int[] beamLoopSoundTicks = new int[3];
    private final int[] assignedHeadCounts = new int[3];
    private int beamVolleyLimit;
    @Nullable
    private Phase beamVolleyPhase;
    private final MoveControl groundMoveControl;
    private final FlyingMoveControl flyingMoveControl;
    private final PathNavigation groundNavigation;
    private final PathNavigation flyingNavigation;
    private int locomotionDecisionTicks = 80;
    private boolean landingForCombat;
    @Nullable
    private Vec3 aerialCombatAnchor;
    private int aerialCombatAnchorTicks;
    private int recoveryWindowTicks;
    @Nullable
    private Phase trackedPhase;
    private int phaseTransitionTicks;
    private int phaseTransitionDuration;
    private boolean multiplayerScalingInitialized;
    private final Set<UUID> encounterParticipants = new HashSet<>();
    private double royalDamageMultiplier = 1.0D;

    @Nullable
    private Entity[] multipartParts;

    protected RoyalBossEntity(EntityType<? extends RoyalBossEntity> entityType, Level level) {
        super(entityType, level);
        for (int i = 0; i < this.beamControllers.length; i++) {
            this.beamControllers[i] = new RoyalBeamController(this);
        }
        this.xpReward = 500;
        this.groundMoveControl = new MoveControl(this);
        this.flyingMoveControl = new FlyingMoveControl(this, 20, true);
        this.groundNavigation = new GroundPathNavigation(this, level);
        FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level);
        flyingNavigation.setCanFloat(true);
        this.flyingNavigation = flyingNavigation;
        if (this.isFlyingBoss()) {
            this.moveControl = this.flyingMoveControl;
            this.navigation = this.flyingNavigation;
        } else {
            this.navigation = this.groundNavigation;
        }
        this.bossEvent = new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(
                this.getUUID(),
                Component.translatable(entityType.getDescriptionId()),
                this.bossBarColor(),
                BossEvent.BossBarOverlay.PROGRESS);
    }

    protected abstract BossEvent.BossBarColor bossBarColor();

    protected abstract SoundEvent royalIdleSound();

    protected abstract SoundEvent royalHurtSound();

    protected abstract SoundEvent royalDeathSound();

    @Nullable
    protected SoundEvent royalFlyLoopSound() {
        return null;
    }

    protected abstract SoundEvent royalBiteSound();

    protected abstract SoundEvent royalBeamShootSound();

    protected abstract SoundEvent royalBeamStartSound();

    protected abstract SoundEvent royalBeamLoopSound();

    protected abstract SoundEvent royalBeamEndSound();

    protected abstract RoyalBeamSettings royalBeamSettings();

    protected abstract RoyalBeamTerrainMode royalBeamTerrainMode();

    protected RoyalBeamElement royalBeamElement(@Nullable RoyalHead head) {
        return RoyalBeamElement.GENERIC;
    }

    protected SoundEvent royalBeamStartSound(RoyalHead head) {
        return this.royalBeamStartSound();
    }

    protected void tickRoyalBeamEffects(RoyalHead head, Vec3 start, Vec3 end) {
    }

    protected RoyalBeamTerrainMode royalBeamTerrainMode(@Nullable RoyalHead head) {
        return this.royalBeamTerrainMode();
    }

    public static AttributeSupplier.Builder createBaseAttributes(double health, double attackDamage) {
        return createBaseAttributes(health, attackDamage, AntarchySettings.royalBossArmor());
    }

    protected static AttributeSupplier.Builder createBaseAttributes(double health, double attackDamage, double armor) {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.FOLLOW_RANGE, AntarchySettings.royalBossFollowRange())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.royalBossMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.royalBossMovementSpeed())
                .add(Attributes.KNOCKBACK_RESISTANCE, AntarchySettings.royalBossKnockbackResistance())
                .add(Attributes.STEP_HEIGHT, AntarchySettings.royalBossStepHeight());
    }

    protected abstract String geoName();

    protected abstract boolean isFlyingBoss();

    protected double groundCombatBias() {
        return 0.35D;
    }

    /** Lets a boss provide a bespoke air/ground cadence without changing the other royal encounter. */
    protected boolean managesOwnCombatLocomotion() {
        return false;
    }

    protected boolean shouldClearObstruction() {
        return true;
    }

    protected double biteApproachSpeed() {
        return 1.3D;
    }

    /** Prevents normal head/body scheduling while a bespoke movement sequence owns the boss. */
    protected boolean blocksRoyalAttacksForMovement() {
        return false;
    }

    protected int royalPhaseTransitionDuration(Phase nextPhase) {
        return 50;
    }

    protected void onRoyalPhaseTransitionStarted(Phase previousPhase, Phase nextPhase) {
    }

    protected void tickRoyalPhaseTransition(Phase nextPhase, int elapsedTicks, int remainingTicks) {
    }

    protected void onRoyalPhaseTransitionCompleted(Phase nextPhase) {
    }

    /** Allows a boss to tune its head-bite concurrency without changing the other royal boss. */
    protected int maxConcurrentHeadAttacks(Phase phase) {
        return phase.maxConcurrentHeadAttacks();
    }

    /** Allows a boss to tune bite pacing without changing the other royal boss. */
    protected int biteCooldownTicks(Phase phase) {
        return Math.max(10, Mth.floor(AntarchySettings.royalBossBiteCooldownTicks() * phase.cooldownScale()));
    }

    public String geoNameForRender() {
        return this.geoName();
    }

    @Override
    public MultipartLayout antarchy$getMultipartLayout() {
        return RoyalBossMultipartLayout.INSTANCE;
    }

    @Override
    @Nullable
    public Entity[] antarchy$getMultipartParts() {
        return this.multipartParts;
    }

    @Override
    public void antarchy$setMultipartParts(@Nullable Entity[] parts) {
        this.multipartParts = parts;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (this.isFlyingBoss()) {
            FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
            navigation.setCanOpenDoors(false);
            navigation.setCanFloat(true);
            return navigation;
        }
        return super.createNavigation(level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 48.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.registerRoyalTargetGoals();
    }

    protected void registerRoyalTargetGoals() {
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean canDamageWithRoyalAttack(LivingEntity target) {
        return target != this
                && !(target instanceof RoyalBossEntity)
                && target.getType() != this.getType()
                && !this.isAlliedTo(target);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACCELERATED, false);
        builder.define(ROYAL_FLYING, true);
        for (int i = 0; i < 3; i++) {
            builder.define(BEAM_ACTIVE[i], false);
            builder.define(BEAM_ELEMENT[i], RoyalBeamElement.GENERIC.ordinal());
            builder.define(BEAM_END_X[i], 0.0F);
            builder.define(BEAM_END_Y[i], 0.0F);
            builder.define(BEAM_END_Z[i], 0.0F);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void checkDespawn() {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.royalIdleSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.royalHurtSound();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.royalDeathSound();
    }

    @Override
    protected float getSoundVolume() {
        return (float) AntarchySettings.royalBossSoundVolume();
    }

    protected void playRoyalSound(SoundEvent sound, float pitch) {
        this.playSound(sound, (float) AntarchySettings.royalBossSoundVolume(), pitch);
    }

    protected void projectRoyalSound(SoundEvent sound, float volume, float pitch, @Nullable LivingEntity focus) {
        if (this.level().isClientSide) {
            return;
        }
        java.util.Set<ServerPlayer> recipients = new java.util.LinkedHashSet<>(this.bossEvent.getPlayers());
        if (focus instanceof ServerPlayer focusPlayer) {
            recipients.add(focusPlayer);
        }
        for (ServerPlayer player : recipients) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                    net.minecraft.core.Holder.direct(sound),
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    player.getX(), player.getY(), player.getZ(),
                    volume, pitch,
                    this.random.nextLong()));
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isRoyalFlying() && this.isEffectiveAi()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.shouldClearObstruction()
                && !this.level().isClientSide
                && !this.isDeadOrDying()
                && this.tickCount % OBSTRUCTION_CLEAR_INTERVAL_TICKS == 0
                && this.isInWall()) {
            RoyalBlockDestruction.destroySphere(
                    (ServerLevel) this.level(),
                    this,
                    this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D),
                    OBSTRUCTION_CLEAR_RADIUS,
                    OBSTRUCTION_CLEAR_MAX_BLOCKS,
                    OBSTRUCTION_CLEAR_MAX_RESISTANCE,
                    OBSTRUCTION_CLEAR_DROP_CHANCE);
        }
        if (this.isFlyingBoss()) {
            this.moveControl = this.isRoyalFlying() ? this.flyingMoveControl : this.groundMoveControl;
            this.navigation = this.isRoyalFlying() ? this.flyingNavigation : this.groundNavigation;
            this.setNoGravity(!AntarchyGravityApi.isGravityInverted(this));
            if (!this.isRoyalFlying()) {
                this.setNoGravity(false);
            }
        }
        if (this.level().isClientSide) {
            return;
        }
        if (this.isRoyalFlying() && !this.isDeadOrDying() && !this.landingForCombat
                && !this.blocksRoyalAttacksForMovement()) {
            this.tickFlyingAltitude();
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.attackScheduler.tick();
        if (this.recoveryWindowTicks > 0) {
            this.recoveryWindowTicks--;
        }

        if (this.isRoyalFlying() && this.tickCount % 40 == 0 && this.royalFlyLoopSound() != null) {
            this.playRoyalSound(this.royalFlyLoopSound(), 0.92F + this.random.nextFloat() * 0.12F);
        }

        if (this.isDeadOrDying()) {
            return;
        }

        Phase currentPhase = this.phase();
        if (this.trackedPhase == null) {
            this.trackedPhase = currentPhase;
        } else if (currentPhase != this.trackedPhase) {
            Phase previousPhase = this.trackedPhase;
            this.trackedPhase = currentPhase;
            this.startRoyalPhaseTransition(previousPhase, currentPhase);
        }
        if (this.phaseTransitionTicks > 0) {
            int elapsedTicks = this.phaseTransitionDuration - this.phaseTransitionTicks;
            this.tickRoyalPhaseTransition(this.trackedPhase, elapsedTicks, this.phaseTransitionTicks);
            if (--this.phaseTransitionTicks == 0) {
                this.onRoyalPhaseTransitionCompleted(this.trackedPhase);
            }
            return;
        }

        LivingEntity primaryTarget = this.getTarget();
        if (primaryTarget == null || !primaryTarget.isAlive() || !this.canAttack(primaryTarget)) {
            for (RoyalHead head : this.heads) {
                head.setTarget(null);
            }
            return;
        }

        this.initializeMultiplayerScaling(primaryTarget);

        this.tickCombatLocomotionMode(primaryTarget);
        if (this.blocksRoyalAttacksForMovement()) {
            return;
        }
        boolean aeriallyStabilized = this.steerTowardTarget(primaryTarget);
        if (!aeriallyStabilized) {
            this.getLookControl().setLookAt(primaryTarget, 30.0F, 30.0F);
        }
        this.assignHeadTargets(primaryTarget);
        this.tickRoyalBeam(primaryTarget);
        this.tickBodyCrush();

        Phase phase = this.phase();
        int activeHeadAttacks = 0;
        if (this.recoveryWindowTicks > 0) {
            return;
        }
        for (RoyalHead head : this.heads) {
            if (this.attackScheduler.laneBusy(this.headLane(head))) {
                activeHeadAttacks++;
            }
        }

        for (RoyalHead head : this.heads) {
            if (activeHeadAttacks >= this.maxConcurrentHeadAttacks(phase)) {
                break;
            }
            if (!head.readyToAttack() || this.attackScheduler.laneBusy(this.headLane(head))) {
                continue;
            }
            LivingEntity headTarget = head.target(this.level());
            if (headTarget != null && this.headWithinBiteReach(head, headTarget)) {
                this.startBite(head, phase);
                activeHeadAttacks++;
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(net.minecraft.world.damagesource.DamageTypes.IN_WALL)
                || super.isInvulnerableTo(source);
    }

    private void tickRoyalBeam(LivingEntity primaryTarget) {
        if (this.recoveryWindowTicks > 0) {
            return;
        }
        Phase phase = this.phase();
        int activeBeams = 0;
        for (RoyalHead head : this.heads) {
            if (head.beamActive()) {
                activeBeams++;
            }
        }
        if (activeBeams == 0 || this.beamVolleyPhase != phase) {
            this.beamVolleyPhase = phase;
            this.beamVolleyLimit = Math.max(1, this.selectBeamVolleyLimit(phase));
        }
        for (RoyalHead head : this.heads) {
            int index = head.slot().ordinal();
            RoyalBeamController controller = this.beamControllers[index];
            if (controller.isFiring()) {
                LivingEntity beamTarget = head.target(this.level());
                if (!head.shooting() || beamTarget == null || !beamTarget.isAlive()
                        || this.distanceTo(beamTarget) < this.royalBeamMinimumRange()) {
                    this.stopRoyalBeam(head);
                    continue;
                }
                if (this.beamLoopSoundTicks[index]-- <= 0) {
                    this.beamLoopSoundTicks[index] = 20;
                    this.playRoyalSound(this.royalBeamLoopSound(), 1.0F);
                }
                controller.tick(
                        this.beamAnchor(head),
                        this.beamDirection(head),
                        beamTarget,
                        this.royalBeamSettings(),
                        this.royalBeamTerrainMode(head),
                        end -> this.setRoyalBeamEndPosition(head.slot(), end));
                Vec3 beamEnd = controller.beamEndPosition();
                if (beamEnd != null) {
                    this.tickRoyalBeamEffects(head, this.beamAnchor(head), beamEnd);
                }
                if (!controller.isFiring()) {
                    this.stopRoyalBeam(head);
                }
                continue;
            }
            if (activeBeams >= this.beamVolleyLimit) {
                continue;
            }
            LivingEntity target = head.target(this.level());
            if (!head.readyToAttack() || this.attackScheduler.laneBusy(this.headLane(head))
                    || target == null || !this.canAttack(target)
                    || this.distanceTo(target) < this.royalBeamMinimumRange()
                    || this.headWithinBiteReach(head, target)
                    || !this.targetInBeamLine(head, target)) {
                continue;
            }
            RoyalBeamSettings beamSettings = this.royalBeamSettings();
            int duration = Math.max(1, beamSettings.durationTicks());
            int cooldown = Math.max(1, Mth.floor(beamSettings.cooldownTicks() * phase.cooldownScale()));
            int windup = Math.max(0, AntarchySettings.queenBeamWindupTicks());
            if (this instanceof KingEntity) {
                windup = Math.max(0, AntarchySettings.kingBeamWindupTicks());
            }
            LivingEntity committedTarget = target;
            if (!this.beginRoyalAttack("beam_" + head.slot().name(), this.headLane(head), cooldown,
                    windup, duration, 8, new RoyalAttackScheduler.Action() {
                        @Override
                        public void onStart() {
                            head.startShoot();
                            RoyalBossEntity.this.triggerAnim(head.slot().controllerName(), "shoot");
                        }

                        @Override
                        public void onActive(int elapsedTicks) {
                            if (elapsedTicks != 0 || RoyalBossEntity.this.beamControllers[index].isFiring()) {
                                return;
                            }
                            RoyalBossEntity.this.beamControllers[index].start(
                                    RoyalBossEntity.this.beamAnchor(head), committedTarget.getEyePosition(), duration);
                            RoyalBossEntity.this.entityData.set(BEAM_ELEMENT[index], RoyalBossEntity.this.royalBeamElement(head).ordinal());
                            RoyalBossEntity.this.entityData.set(BEAM_ACTIVE[index], true);
                            RoyalBossEntity.this.beamLoopSoundTicks[index] = 20;
                            RoyalBossEntity.this.playRoyalSound(RoyalBossEntity.this.royalBeamStartSound(head), 1.0F);
                            RoyalBossEntity.this.playRoyalSound(RoyalBossEntity.this.royalBeamShootSound(), 1.0F);
                        }

                        @Override
                        public void onComplete() {
                            RoyalBossEntity.this.stopRoyalBeam(head);
                            RoyalBossEntity.this.startRoyalRecovery(RECOVERY_AFTER_BEAM_TICKS);
                        }
                    })) {
                continue;
            }
            head.setBeamActive(true);
            activeBeams++;
        }
    }

    protected int selectBeamVolleyLimit(Phase phase) {
        return phase.maxConcurrentHeadAttacks();
    }

    protected boolean isRoyalRecoveryActive() {
        return this.recoveryWindowTicks > 0;
    }

    protected void startRoyalRecovery(int ticks) {
        this.recoveryWindowTicks = Math.max(this.recoveryWindowTicks, ticks);
    }

    protected final boolean isRoyalPhaseTransitionActive() {
        return this.phaseTransitionTicks > 0;
    }

    private void startRoyalPhaseTransition(Phase previousPhase, Phase nextPhase) {
        this.phaseTransitionDuration = Math.max(1, this.royalPhaseTransitionDuration(nextPhase));
        this.phaseTransitionTicks = this.phaseTransitionDuration;
        for (RoyalAttackLane lane : RoyalAttackLane.values()) {
            this.attackScheduler.cancel(lane);
        }
        for (RoyalHead head : this.heads) {
            if (head.beamActive() || head.shooting()) {
                this.stopRoyalBeam(head);
            }
        }
        this.getNavigation().stop();
        this.onRoyalPhaseTransitionStarted(previousPhase, nextPhase);
    }

    /** Returns the live players currently participating in the encounter. */
    protected final List<ServerPlayer> royalEncounterPlayers(ServerLevel level) {
        double range = Math.max(32.0D, this.getAttributeValue(Attributes.FOLLOW_RANGE));
        LinkedHashSet<ServerPlayer> players = new LinkedHashSet<>(this.bossEvent.getPlayers());
        players.addAll(level.getPlayers(player -> player.isAlive()
                && !player.isSpectator()
                && player.distanceToSqr(this) <= range * range));
        players.removeIf(player -> !player.isAlive() || player.isSpectator() || player.level() != level
                || player.distanceToSqr(this) > range * range);
        return List.copyOf(players);
    }

    private void initializeMultiplayerScaling(LivingEntity target) {
        if (this.multiplayerScalingInitialized || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.multiplayerScalingInitialized = true;
        if (!AntarchySettings.royalBossMultiplayerScalingEnabled()) {
            return;
        }
        int maxPlayers = Math.max(1, AntarchySettings.royalBossScalingMaxPlayers());
        List<ServerPlayer> players = level.getPlayers(player -> player.isAlive()
                && !player.isSpectator()
                && player.distanceToSqr(this) <= AntarchySettings.royalBossFollowRange() * AntarchySettings.royalBossFollowRange());
        if (target instanceof ServerPlayer player && player.isAlive() && !players.contains(player)) {
            players.add(player);
        }
        int participantCount = Math.min(maxPlayers, Math.max(1, players.size()));
        for (ServerPlayer player : players) {
            if (this.encounterParticipants.size() >= participantCount) {
                break;
            }
            this.encounterParticipants.add(player.getUUID());
        }
        double healthMultiplier = 1.0D + Math.max(0, participantCount - 1)
                * Math.max(0.0D, AntarchySettings.royalBossHealthPerAdditionalPlayer());
        double baseHealth = this.getAttributeBaseValue(Attributes.MAX_HEALTH);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * healthMultiplier);
        this.setHealth(this.getMaxHealth());
        double damageMultiplier = Math.max(0.0D, participantCount - 1)
                * Math.max(0.0D, AntarchySettings.royalBossDamagePerAdditionalPlayer());
        this.royalDamageMultiplier = 1.0D + damageMultiplier;
        this.applyRoyalDamageModifier();
    }

    private void stopRoyalBeam(RoyalHead head) {
        int index = head.slot().ordinal();
        head.setBeamActive(false);
        head.stopShoot();
        this.beamControllers[index].stop();
        this.entityData.set(BEAM_ACTIVE[index], false);
        this.entityData.set(BEAM_ELEMENT[index], RoyalBeamElement.GENERIC.ordinal());
        this.playRoyalSound(this.royalBeamEndSound(), 1.0F);
        this.setRoyalBeamEndPosition(head.slot(), null);
    }

    public Phase phase() {
        float fraction = this.getMaxHealth() <= 0.0F ? 1.0F : this.getHealth() / this.getMaxHealth();
        if (fraction > PHASE_TWO_THRESHOLD) {
            return Phase.ONE;
        }
        if (fraction > PHASE_THREE_THRESHOLD) {
            return Phase.TWO;
        }
        return Phase.THREE;
    }

    private void assignHeadTargets(LivingEntity fallback) {
        if (!this.shouldDistributeHeadTargets(fallback)) {
            for (RoyalHead head : this.heads) {
                head.setTarget(fallback);
            }
            return;
        }
        List<Player> candidates = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(AntarchySettings.royalBossFollowRange()),
                player -> player.isAlive() && this.canAttack(player));
        if (candidates.isEmpty()) {
            for (RoyalHead head : this.heads) {
                head.setTarget(fallback);
            }
            return;
        }

        Arrays.fill(this.assignedHeadCounts, 0);
        for (RoyalHead head : this.heads) {
            Vec3 anchor = this.headAnchor(head);
            int bestIndex = 0;
            int bestCount = Integer.MAX_VALUE;
            double bestDistance = Double.MAX_VALUE;
            for (int i = 0; i < candidates.size(); i++) {
                double distance = candidates.get(i).position().distanceToSqr(anchor);
                if (this.assignedHeadCounts[i] < bestCount || (this.assignedHeadCounts[i] == bestCount && distance < bestDistance)) {
                    bestIndex = i;
                    bestCount = this.assignedHeadCounts[i];
                    bestDistance = distance;
                }
            }
            this.assignedHeadCounts[bestIndex]++;
            head.setTarget(candidates.get(bestIndex));
        }
    }

    protected boolean shouldDistributeHeadTargets(LivingEntity primaryTarget) {
        return true;
    }

    private boolean steerTowardTarget(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        Vec3 awayFromTarget = horizontalDistance > 1.0E-4D
                ? new Vec3(-dx / horizontalDistance, 0.0D, -dz / horizontalDistance)
                : this.getViewVector(1.0F).multiply(-1.0D, 0.0D, -1.0D).normalize();
        double standoff = Math.max(this.getBbWidth() * 0.55D,
                BEAM_MUZZLE_FORWARD - this.biteReach() * 0.65D + 1.0D);
        Vec3 wanted = target.position().add(awayFromTarget.scale(standoff));
        boolean atStandoff = this.position().multiply(1.0D, 0.0D, 1.0D)
                .distanceToSqr(wanted.multiply(1.0D, 0.0D, 1.0D)) <= 4.0D;
        boolean anyHeadInBiteRange = Arrays.stream(this.heads)
                .anyMatch(head -> this.headWithinBiteReach(head, target));
        double approachSpeed = anyHeadInBiteRange ? 1.0D : this.biteApproachSpeed();

        if (!this.isRoyalFlying()) {
            this.aerialCombatAnchor = null;
            this.aerialCombatAnchorTicks = 0;
            if (atStandoff) {
                this.getNavigation().stop();
            } else {
                this.getMoveControl().setWantedPosition(wanted.x, target.getY(), wanted.z, approachSpeed);
            }
            return false;
        }

        double verticalDistance = Math.abs(target.getY() + target.getBbHeight() * 0.5D
                - (this.getY() + this.getBbHeight() * 0.5D));
        if (!this.landingForCombat && verticalDistance > 6.0D) {
            if (this.aerialCombatAnchor == null || this.aerialCombatAnchorTicks-- <= 0) {
                double anchorX = this.getX();
                double anchorZ = this.getZ();
                if (horizontalDistance > 10.0D) {
                    anchorX = wanted.x;
                    anchorZ = wanted.z;
                }
                this.aerialCombatAnchor = new Vec3(anchorX, this.getY(), anchorZ);
                this.aerialCombatAnchorTicks = 20;
            }
            Vec3 anchor = this.aerialCombatAnchor;
            this.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, Math.min(approachSpeed, 0.85D));
            return true;
        }
        this.aerialCombatAnchor = null;
        this.aerialCombatAnchorTicks = 0;

        boolean inverted = AntarchyGravityApi.isGravityInverted(this);
        double groundBelowTarget = this.groundYBelow(wanted.x, wanted.z);
        double wantedY = this.landingForCombat
                ? (inverted ? target.getY() + target.getBbHeight() - this.getBbHeight() : groundBelowTarget)
                : inverted
                ? target.getY() - 3.0D
                : Mth.clamp(target.getY() + 3.0D,
                groundBelowTarget + FLYING_MIN_HOVER,
                groundBelowTarget + FLYING_MAX_HOVER_ABOVE_GROUND);
        boolean closeVertically = Math.abs(wantedY - this.getY()) <= 2.5D;
        if (atStandoff && closeVertically) {
            this.getNavigation().stop();
        } else {
            this.getMoveControl().setWantedPosition(wanted.x, wantedY, wanted.z, approachSpeed);
        }
        return false;
    }

    private void tickCombatLocomotionMode(LivingEntity target) {
        if (!this.isFlyingBoss() || this.managesOwnCombatLocomotion()) {
            return;
        }
        boolean inverted = AntarchyGravityApi.isGravityInverted(this);
        double groundY = this.groundYBelow(this.getX(), this.getZ());
        if (this.landingForCombat) {
            if (!target.onGround()) {
                this.landingForCombat = false;
                this.locomotionDecisionTicks = 60;
            } else if (this.onGround() || !inverted && this.getY() <= groundY + 1.0D) {
                this.landingForCombat = false;
                this.setRoyalFlying(false);
                this.locomotionDecisionTicks = 100 + this.random.nextInt(100);
            }
            return;
        }

        if (--this.locomotionDecisionTicks > 0) {
            return;
        }
        if (this.isRoyalFlying()) {
            this.locomotionDecisionTicks = 60 + this.random.nextInt(80);
            if (target.onGround() && this.random.nextDouble() < this.groundCombatBias()) {
                this.landingForCombat = true;
            }
        } else if (!target.onGround() || this.random.nextDouble() >= this.groundCombatBias()) {
            this.setRoyalFlying(true);
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.35D, 0.0D));
            this.locomotionDecisionTicks = 80 + this.random.nextInt(100);
        } else {
            this.locomotionDecisionTicks = 80 + this.random.nextInt(80);
        }
    }

    public boolean isRoyalFlying() {
        return this.isFlyingBoss() && this.entityData.get(ROYAL_FLYING);
    }

    protected final void setRoyalFlying(boolean flying) {
        if (this.entityData.get(ROYAL_FLYING) == flying) {
            return;
        }
        this.navigation.stop();
        this.entityData.set(ROYAL_FLYING, flying);
        this.moveControl = flying ? this.flyingMoveControl : this.groundMoveControl;
        this.navigation = flying ? this.flyingNavigation : this.groundNavigation;
        this.setNoGravity(flying && !AntarchyGravityApi.isGravityInverted(this));
    }

    protected double groundYBelow(double x, double z) {
        return this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Mth.floor(x), Mth.floor(z));
    }

    private void tickFlyingAltitude() {
        if (AntarchyGravityApi.isGravityInverted(this)) {
            return;
        }
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.2D, 0.0D));
        }
        double groundY = this.groundYBelow(this.getX(), this.getZ());
        double ceiling = groundY + FLYING_MAX_HOVER_ABOVE_GROUND;
        if (this.getY() > ceiling) {
            double desiredY = groundY + FLYING_PREFERRED_HOVER;
            this.getMoveControl().setWantedPosition(this.getX(), desiredY, this.getZ(), 1.0D);
            Vec3 delta = this.getDeltaMovement();
            if (delta.y > -0.1D) {
                this.setDeltaMovement(delta.x, Math.max(-0.5D, delta.y - 0.1D), delta.z);
            }
        }
    }

    private void tickBodyCrush() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.tickCount % 10 != 0) {
            return;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() < 0.0016D) {
            return;
        }
        double yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 center = new Vec3(
                this.getX() + forward.x * (this.getBbWidth() * 0.45D),
                this.getY() + (this.isRoyalFlying() ? 0.0D : 1.0D),
                this.getZ() + forward.z * (this.getBbWidth() * 0.45D));
        RoyalBlockDestruction.destroySphere(serverLevel, this, center, CRUSH_RADIUS, CRUSH_MAX_BLOCKS, CRUSH_MAX_RESISTANCE, CRUSH_DROP_CHANCE);
    }

    private void startBite(RoyalHead head, Phase phase) {
        int cooldown = this.biteCooldownTicks(phase);
        if (!this.beginRoyalAttack("bite_" + head.slot().name(), headLane(head), cooldown,
                BITE_HIT_TICK, 1, BITE_DURATION_TICKS - BITE_HIT_TICK,
                new RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        RoyalBossEntity.this.triggerAnim(head.slot().controllerName(), "bite");
                        RoyalBossEntity.this.playRoyalSound(RoyalBossEntity.this.royalBiteSound(),
                                0.9F + RoyalBossEntity.this.random.nextFloat() * 0.2F);
                    }

                    @Override
                    public void onActive(int elapsedTicks) {
                        RoyalBossEntity.this.applyBiteHit(head);
                    }
                })) {
            return;
        }
    }

    protected RoyalAttackLane headLane(RoyalHead head) {
        return switch (head.slot()) {
            case LEFT -> RoyalAttackLane.LEFT_HEAD;
            case CENTER -> RoyalAttackLane.CENTER_HEAD;
            case RIGHT -> RoyalAttackLane.RIGHT_HEAD;
        };
    }

    protected RoyalAttackLane beamLane() {
        return RoyalAttackLane.BODY;
    }

    protected boolean beginRoyalAttack(String id, RoyalAttackLane lane, int cooldownTicks, int activeTicks) {
        return this.attackScheduler.start(id, lane, cooldownTicks, activeTicks);
    }

    protected boolean beginRoyalAttack(String id, RoyalAttackLane lane, int cooldownTicks, int windupTicks,
                                       int activeTicks, int recoveryTicks, RoyalAttackScheduler.Action action) {
        int protectedRecovery = recoveryTicks;
        if (lane == RoyalAttackLane.LEFT_HEAD || lane == RoyalAttackLane.CENTER_HEAD || lane == RoyalAttackLane.RIGHT_HEAD) {
            protectedRecovery = Math.max(recoveryTicks,
                    HEAD_ATTACK_ANIMATION_TICKS - Math.max(0, windupTicks) - Math.max(1, activeTicks));
        }
        return this.attackScheduler.start(id, lane, cooldownTicks, windupTicks, activeTicks, protectedRecovery, action);
    }

    protected int animationRecovery(int animationTicks, int windupTicks, int activeTicks, int recoveryTicks) {
        return Math.max(recoveryTicks, animationTicks - Math.max(0, windupTicks) - Math.max(1, activeTicks));
    }

    private void applyBiteHit(RoyalHead head) {
        if (this.level().isClientSide) {
            return;
        }
        Vec3 anchor = this.headAnchor(head);
        double reach = this.biteReach();
        float damage = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * AntarchySettings.royalBossBiteDamageMultiplier());
        DamageSource damageSource = this.damageSources().mobAttack(this);
        AABB box = new AABB(anchor, anchor).inflate(reach);
        for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive() && entity != this)) {
            if (!this.canDamageWithRoyalAttack(living)) {
                continue;
            }
            if (living.position().distanceToSqr(anchor) > reach * reach) {
                continue;
            }
            if (living.hurt(damageSource, damage)) {
                Vec3 push = living.position().subtract(anchor).multiply(1.0D, 0.0D, 1.0D);
                if (push.lengthSqr() < 1.0E-4D) {
                    push = this.getViewVector(1.0F).multiply(1.0D, 0.0D, 1.0D);
                }
                push = push.normalize().scale(1.4D);
                living.push(push.x, 0.42D, push.z);
                living.hurtMarked = true;
            }
        }
    }

    private boolean headWithinBiteReach(RoyalHead head, LivingEntity target) {
        Vec3 anchor = this.headAnchor(head);
        double reach = this.biteReach() + target.getBbWidth();
        return target.position().distanceToSqr(anchor) <= reach * reach;
    }

    private double biteReach() {
        return AntarchySettings.royalBossBiteReach();
    }

    protected Vec3 headAnchor(RoyalHead head) {
        int partIndex = head.slot().partIndex();
        Entity[] parts = this.multipartParts;
        if (parts != null && partIndex < parts.length && parts[partIndex] != null) {
            Entity part = parts[partIndex];
            return part.position().add(0.0D, part.getBbHeight() * 0.5D, 0.0D);
        }

        MultipartPartDefinition spec = RoyalBossMultipartLayout.INSTANCE.parts()[partIndex];
        double yaw = this.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 localOffset = new Vec3(
                forward.x * spec.forwardOffset() + right.x * spec.lateralOffset(),
                spec.yOffset(),
                forward.z * spec.forwardOffset() + right.z * spec.lateralOffset());
        return this.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(localOffset,
                AntarchyGravityApi.getGravityDirection(this)));
    }

    protected Vec3 beamAnchor(RoyalHead head) {
        return beamAnchor(head.slot(), this.getX(), this.getY(), this.getZ(), this.getYRot());
    }

    protected Vec3 beamDirection(RoyalHead head) {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        Vec3 localDirection = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        return AntarchyGravityRotationUtil.vecPlayerToWorld(localDirection,
                AntarchyGravityApi.getGravityDirection(this)).normalize();
    }

    private boolean targetInBeamLine(RoyalHead head, LivingEntity target) {
        Vec3 origin = this.beamAnchor(head);
        Vec3 direction = this.beamDirection(head);
        Vec3 toTarget = target.getEyePosition().subtract(origin);
        double forwardDistance = toTarget.dot(direction);
        if (forwardDistance <= 0.0D) {
            return false;
        }
        Vec3 perpendicular = toTarget.subtract(direction.scale(forwardDistance));
        double tolerance = Math.max(1.5D, target.getBbWidth() * 1.5D);
        return perpendicular.lengthSqr() <= tolerance * tolerance;
    }

    private Vec3 beamAnchor(RoyalHead.Slot slot, double x, double y, double z, float yaw) {
        double yawRadians = yaw * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        double lateral = switch (slot) {
            case LEFT -> -BEAM_MUZZLE_LATERAL;
            case CENTER -> 0.0D;
            case RIGHT -> BEAM_MUZZLE_LATERAL;
        };
        double vertical = slot == RoyalHead.Slot.CENTER ? BEAM_MUZZLE_CENTER_Y : BEAM_MUZZLE_SIDE_Y;
        Vec3 localOffset = new Vec3(
                forward.x * BEAM_MUZZLE_FORWARD + right.x * lateral,
                vertical,
                forward.z * BEAM_MUZZLE_FORWARD + right.z * lateral);
        return new Vec3(x, y, z).add(AntarchyGravityRotationUtil.vecPlayerToWorld(localOffset,
                AntarchyGravityApi.getGravityDirection(this)));
    }

    protected double royalBeamMinimumRange() {
        return AntarchySettings.queenBeamMinimumRange();
    }

    protected RoyalHead royalHead(RoyalHead.Slot slot) {
        return this.heads[slot.ordinal()];
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.stopTriggeredAnim("wing_action", null);
            this.stopTriggeredAnim("head_left", null);
            this.stopTriggeredAnim("head_center", null);
            this.stopTriggeredAnim("head_right", null);
            this.stopTriggeredAnim("body_action", null);
            for (RoyalHead head : this.heads) {
                head.reset();
            }
            for (RoyalBeamController controller : this.beamControllers) {
                controller.stop();
            }
            this.setFiringRoyalBeam(false);
            for (RoyalHead head : this.heads) {
                this.stopRoyalBeam(head);
            }
            this.setRoyalFlying(false);
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.setXRot(0.0F);
            this.setYHeadRot(this.getYRot());
            this.yBodyRot = this.getYRot();
        }
        super.die(damageSource);
    }

    @Override
    @Nullable
    public ItemEntity spawnAtLocation(ItemStack stack, float offsetY) {
        if (stack.isEmpty() || this.level().isClientSide) {
            return null;
        }
        double spread = this.getBbWidth() * 0.6D;
        double height = 20.0D + this.random.nextDouble() * 10.0D;
        ItemEntity itemEntity = new ItemEntity(
                this.level(),
                this.getX() + (this.random.nextDouble() - 0.5D) * spread,
                this.getY() + height,
                this.getZ() + (this.random.nextDouble() - 0.5D) * spread,
                stack);
        itemEntity.setDeltaMovement(
                (this.random.nextDouble() - 0.5D) * 0.12D,
                -0.2D - this.random.nextDouble() * 0.15D,
                (this.random.nextDouble() - 0.5D) * 0.12D);
        itemEntity.setDefaultPickUpDelay();
        this.level().addFreshEntity(itemEntity);
        return itemEntity;
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        this.setNoGravity(false);
        this.setXRot(0.0F);
        this.setYHeadRot(this.getYRot());
        this.yBodyRot = this.getYRot();
        Vec3 movement = this.getDeltaMovement();
        if (this.onGround()) {
            this.setDeltaMovement(movement.x * 0.6D, 0.0D, movement.z * 0.6D);
        } else {
            this.setDeltaMovement(movement.x * 0.8D, Math.min(-0.12D, movement.y), movement.z * 0.8D);
        }
        if (this.deathTime == 20 && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
        }
        if (this.deathTime >= DEATH_TICKS) {
            this.remove(RemovalReason.KILLED);
            this.dropExperience(this);
        }
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
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount);
        }
        float cappedAmount = (float) Math.min(amount, AntarchySettings.royalBossMaxSingleHitDamage());
        return super.hurt(source, cappedAmount);
    }

    @Override
    public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effectInstance) {
        if (effectInstance.is(com.craisinlord.antarchy.content.AntarchyObjects.INVERTED_EFFECT.get())) {
            return ThoraxisUndersideManager.isThoraxis(this.level());
        }
        return super.canBeAffected(effectInstance);
    }

    public boolean isRoyalAccelerated() {
        return this.entityData.get(ACCELERATED);
    }

    public void setRoyalAccelerated(boolean accelerated) {
        this.entityData.set(ACCELERATED, accelerated);
    }

    public boolean isFiringRoyalBeam() {
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            if (this.isFiringRoyalBeam(slot)) return true;
        }
        return false;
    }

    public void setFiringRoyalBeam(boolean firing) {
    }

    public RoyalBeamController beamController() {
        return this.beamControllers[0];
    }

    public void setRoyalBeamEndPosition(@Nullable Vec3 position) {
        this.setRoyalBeamEndPosition(RoyalHead.Slot.CENTER, position);
    }

    public void setRoyalBeamEndPosition(RoyalHead.Slot slot, @Nullable Vec3 position) {
        int index = slot.ordinal();
        this.entityData.set(BEAM_ACTIVE[index], position != null || this.beamControllers[index].isFiring());
        if (position != null) {
            this.entityData.set(BEAM_END_X[index], (float) position.x);
            this.entityData.set(BEAM_END_Y[index], (float) position.y);
            this.entityData.set(BEAM_END_Z[index], (float) position.z);
        }
    }

    @Nullable
    public Vec3 getRoyalBeamEndPosition() {
        return this.getRoyalBeamEndPosition(RoyalHead.Slot.CENTER);
    }

    public Vec3 getRoyalBeamShootFrom(float partialTicks) {
        return this.getRoyalBeamShootFrom(RoyalHead.Slot.CENTER, partialTicks);
    }

    public Vec3 getRoyalBeamShootFrom(RoyalHead.Slot slot, float partialTicks) {
        double x = Mth.lerp(partialTicks, this.xo, this.getX());
        double y = Mth.lerp(partialTicks, this.yo, this.getY());
        double z = Mth.lerp(partialTicks, this.zo, this.getZ());
        float yaw = Mth.rotLerp(partialTicks, this.yRotO, this.getYRot());
        return this.beamAnchor(slot, x, y, z, yaw);
    }

    public RoyalBeamElement getRoyalBeamElement() {
        return this.getRoyalBeamElement(RoyalHead.Slot.CENTER);
    }

    public RoyalBeamElement getRoyalBeamElement(RoyalHead.Slot slot) {
        int ordinal = this.entityData.get(BEAM_ELEMENT[slot.ordinal()]);
        return ordinal >= 0 && ordinal < RoyalBeamElement.values().length
                ? RoyalBeamElement.values()[ordinal]
                : RoyalBeamElement.GENERIC;
    }

    public boolean isFiringRoyalBeam(RoyalHead.Slot slot) {
        return this.entityData.get(BEAM_ACTIVE[slot.ordinal()]);
    }

    @Nullable
    public Vec3 getRoyalBeamEndPosition(RoyalHead.Slot slot) {
        int index = slot.ordinal();
        if (!this.entityData.get(BEAM_ACTIVE[index])) return null;
        return new Vec3(this.entityData.get(BEAM_END_X[index]), this.entityData.get(BEAM_END_Y[index]), this.entityData.get(BEAM_END_Z[index]));
    }

    @Nullable
    public RoyalHead.Slot getRoyalBeamHeadSlot() {
        for (RoyalHead.Slot slot : RoyalHead.Slot.values()) {
            if (this.isFiringRoyalBeam(slot)) return slot;
        }
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 4, this::locomotionPredicate));
        controllers.add(new AnimationController<>(this, "wing_action", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("wing_gust", RawAnimation.begin().thenPlay("wing_gust")));
        controllers.add(new AnimationController<>(this, "head_left", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite_3"))
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("shoot_3")));
        controllers.add(new AnimationController<>(this, "head_center", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite_1"))
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("shoot_1")));
        controllers.add(new AnimationController<>(this, "head_right", 0, RoyalBossEntity::headIdle)
                .triggerableAnim("bite", RawAnimation.begin().thenPlay("bite_2"))
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("shoot_2")));
        controllers.add(new AnimationController<>(this, "body_action", 0, this::bodyActionPredicate)
                .triggerableAnim("stomp", RawAnimation.begin().thenPlay("stomp"))
                .triggerableAnim("minion_spawn", RawAnimation.begin().thenPlay("minion_spawn")));
    }

    private PlayState locomotionPredicate(AnimationState<RoyalBossEntity> state) {
        if (this.isDeadOrDying()) {
            return PlayState.STOP;
        }
        if (this.isRoyalFlying()) {
            state.setAndContinue(FLY_ANIM);
        } else if (state.isMoving()) {
            state.setAndContinue(WALK_ANIM);
        } else {
            state.setAndContinue(IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    private PlayState bodyActionPredicate(AnimationState<RoyalBossEntity> state) {
        if (this.isDeadOrDying()) {
            return state.setAndContinue(DEATH_ANIM);
        }
        return PlayState.STOP;
    }

    private static PlayState headIdle(AnimationState<RoyalBossEntity> state) {
        return PlayState.STOP;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("RoyalFlying", this.isRoyalFlying());
        tag.putBoolean("RoyalScalingInitialized", this.multiplayerScalingInitialized);
        tag.putInt("RoyalScalingParticipants", this.encounterParticipants.size());
        tag.putDouble("RoyalDamageMultiplier", this.royalDamageMultiplier);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setRoyalFlying(!tag.contains("RoyalFlying") || tag.getBoolean("RoyalFlying"));
        this.multiplayerScalingInitialized = tag.getBoolean("RoyalScalingInitialized");
        this.royalDamageMultiplier = Math.max(1.0D, tag.getDouble("RoyalDamageMultiplier"));
        if (tag.contains("RoyalScalingParticipants")) {
            int count = Math.max(1, tag.getInt("RoyalScalingParticipants"));
            for (int i = 0; i < count; i++) {
                this.encounterParticipants.add(new UUID(0L, i + 1L));
            }
        }
        this.applyRoyalDamageModifier();
    }

    public float scaleRoyalDamage(double damage) {
        return (float) (damage * this.royalDamageMultiplier);
    }

    private void applyRoyalDamageModifier() {
        AttributeInstance attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamage == null || this.royalDamageMultiplier <= 1.0D
                || attackDamage.getModifier(MULTIPLAYER_DAMAGE_ID) != null) {
            return;
        }
        attackDamage.addPermanentModifier(new AttributeModifier(MULTIPLAYER_DAMAGE_ID,
                this.royalDamageMultiplier - 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
