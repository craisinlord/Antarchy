package com.craisinlord.antarchy.content.entity.royal;

import com.craisinlord.anthology.beam.BeamOwner;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.anthology.multipart.MultipartEntityOwner;
import com.craisinlord.anthology.multipart.MultipartLayout;
import com.craisinlord.anthology.multipart.MultipartPartDefinition;
import com.craisinlord.anthology.beam.BeamController;
import com.craisinlord.anthology.beam.BeamSettings;
import com.craisinlord.anthology.beam.BeamTerrainMode;
import com.craisinlord.antarchy.content.entity.royal.beam.RoyalBeamElement;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackLane;
import com.craisinlord.antarchy.content.entity.royal.attack.RoyalAttackScheduler;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class RoyalBossEntity extends Monster implements GeoEntity, MultipartEntityOwner, BeamOwner {
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
    private static final int RIVAL_OBSTRUCTION_CLEAR_INTERVAL_TICKS = 10;
    private static final int BODY_CRUSH_INTERVAL_TICKS = 10;
    private static final int RIVAL_BODY_CRUSH_INTERVAL_TICKS = 20;
    private static final double OBSTRUCTION_CLEAR_RADIUS = 3.5D;
    private static final int OBSTRUCTION_CLEAR_MAX_BLOCKS = 24;
    private static final double OBSTRUCTION_CLEAR_MAX_RESISTANCE = 60.0D;
    private static final float OBSTRUCTION_CLEAR_DROP_CHANCE = 0.1F;
    private static final double BEAM_MUZZLE_FORWARD = 27.375D;
    private static final double BEAM_MUZZLE_CENTER_Y = 11.5D;
    private static final double BEAM_MUZZLE_SIDE_Y = 10.25D;
    private static final double BEAM_MUZZLE_LATERAL = 2.625D;
    private static final int HEAVY_BLOCK_CHECK_INTERVAL_TICKS = 4;
    private static final String WING_BUFFET_ID = "royal_wing_buffet";
    private static final int WING_BUFFET_WINDUP_TICKS = 16;
    private static final double WING_BUFFET_EXTRA_RADIUS = 5.0D;
    private static final double WING_BUFFET_BELOW = 14.0D;
    private static final double WING_BUFFET_ABOVE = 6.0D;
    private static final double WING_BUFFET_DAMAGE_SCALE = 0.85D;
    private static final double WING_BUFFET_PUSH = 2.6D;
    private static final double WING_BUFFET_LIFT = 0.75D;
    private static final String EXCAVATION_ID = "royal_excavation";
    private static final int EXCAVATION_TRIGGER_TICKS = 100;
    private static final int EXCAVATION_WINDUP_TICKS = 24;
    private static final int EXCAVATION_COOLDOWN_TICKS = 200;
    private static final double EXCAVATION_RANGE = 72.0D;
    private static final double EXCAVATION_RADIUS = 2.5D;
    private static final double EXCAVATION_DEPTH = 10.0D;
    private static final int EXCAVATION_MAX_BLOCKS = 20;
    private static final double EXCAVATION_CONCEALED_DAMAGE = 5.0D;
    private static final int STUCK_SAMPLE_TICKS = 40;
    private static final double STUCK_MIN_PROGRESS_SQR = 1.0D;
    private static final double STUCK_MIN_WANTED_DISTANCE_SQR = 36.0D;
    private static final int STUCK_TAKEOFF_STRIKES = 2;
    private static final int STUCK_TELEPORT_STRIKES = 4;
    private static final int STUCK_TELEPORT_COOLDOWN_TICKS = 600;
    private static final int STUCK_TELEPORT_ATTEMPTS = 16;
    private static final int LANDING_FOR_COMBAT_TIMEOUT_TICKS = 120;
    private static final int GROUND_SCAN_DEPTH = 48;
    private static final int GROUND_SCAN_RISE = 24;
    private static final double MAX_BEAM_PITCH = Math.toRadians(55.0D);
    private static final int ELEVATED_TARGET_HEIGHT = 10;
    private static final float FACING_TURN_RATE = 6.0F;

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
    protected final BeamController[] beamControllers = new BeamController[3];
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
    private int landingForCombatTicks;
    private int royalDeadZoneTicks;
    private int royalConcealedTicks;
    private int royalElevatedTargetTicks;
    private int royalLastMoveRequestTick = Integer.MIN_VALUE;
    @Nullable
    private Vec3 royalStuckSamplePosition;
    private int royalStuckSampleTicks;
    private int royalStuckStrikes;
    private int royalTeleportCooldownTicks;

    @Nullable
    private Entity[] multipartParts;

    protected RoyalBossEntity(EntityType<? extends RoyalBossEntity> entityType, Level level) {
        super(entityType, level);
        for (int i = 0; i < this.beamControllers.length; i++) {
            this.beamControllers[i] = new BeamController(this);
        }
        this.xpReward = 500;
        this.groundMoveControl = new RoyalGroundMoveControl(this);
        this.flyingMoveControl = new RoyalFlyingMoveControl(this, 20);
        this.groundNavigation = new GroundPathNavigation(this, level);
        FlyingPathNavigation flyingNavigation = new FlyingPathNavigation(this, level);
        flyingNavigation.setCanFloat(true);
        this.flyingNavigation = flyingNavigation;
        if (this.isFlyingBoss()) {
            this.moveControl = this.flyingMoveControl;
            this.navigation = this.flyingNavigation;
        } else {
            this.moveControl = this.groundMoveControl;
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

    protected abstract BeamSettings royalBeamSettings();

    protected abstract BeamTerrainMode royalBeamTerrainMode();

    protected RoyalBeamElement royalBeamElement(@Nullable RoyalHead head) {
        return RoyalBeamElement.GENERIC;
    }

    protected SoundEvent royalBeamStartSound(RoyalHead head) {
        return this.royalBeamStartSound();
    }

    protected float royalBeamStartSoundVolume(RoyalHead head) {
        return (float) AntarchySettings.royalBossSoundVolume();
    }

    protected void tickRoyalBeamEffects(RoyalHead head, Vec3 start, Vec3 end) {
    }

    protected BeamTerrainMode royalBeamTerrainMode(@Nullable RoyalHead head) {
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

    /** Prevents new beam volleys while a bespoke attack has reserved the heads. */
    protected boolean blocksRoyalBeamScheduling() {
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

    /** Allows either royal boss to tune bite damage without changing the other. */
    protected double biteDamageMultiplier() {
        return AntarchySettings.royalBossBiteDamageMultiplier();
    }

    public String geoNameForRender() {
        return this.geoName();
    }

    @Override
    public MultipartLayout anthology$getMultipartLayout() {
        return RoyalBossMultipartLayout.INSTANCE;
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
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setUnseenMemoryTicks(ROYAL_UNSEEN_MEMORY_TICKS));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true)
                .setUnseenMemoryTicks(ROYAL_UNSEEN_MEMORY_TICKS));
    }

    protected static final int ROYAL_UNSEEN_MEMORY_TICKS = 400;

    protected SoundEvent royalWingBuffetSound() {
        return this.royalBiteSound();
    }

    protected int royalWingBuffetCooldownTicks(Phase phase) {
        return switch (phase) {
            case ONE -> 110;
            case TWO -> 90;
            case THREE -> 70;
        };
    }

    protected int royalDeadZoneTriggerTicks(Phase phase) {
        return switch (phase) {
            case ONE -> 40;
            case TWO -> 32;
            case THREE -> 24;
        };
    }

    protected boolean royalShouldFaceTarget(LivingEntity target) {
        return true;
    }

    protected void onRoyalStuck(LivingEntity target) {
    }

    protected final int royalElevatedTargetTicks() {
        return this.royalElevatedTargetTicks;
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

    protected void playRoyalSound(SoundEvent sound, float volume, float pitch) {
        this.playSound(sound, volume, pitch);
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

    private final java.util.Map<net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid>, CachedFluidCheck> cachedFluidChecks = new java.util.HashMap<>(2);

    public boolean shouldRunHeavyBlockChecks() {
        return (this.tickCount + this.getId()) % HEAVY_BLOCK_CHECK_INTERVAL_TICKS == 0;
    }

    @Override
    protected void tryCheckInsideBlocks() {
        if (this.shouldRunHeavyBlockChecks()) {
            super.tryCheckInsideBlocks();
        }
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(net.minecraft.tags.TagKey<net.minecraft.world.level.material.Fluid> fluidTag, double motionScale) {
        CachedFluidCheck cached = this.cachedFluidChecks.get(fluidTag);
        if (cached == null || this.shouldRunHeavyBlockChecks()) {
            boolean inFluid = super.updateFluidHeightAndDoFluidPushing(fluidTag, motionScale);
            this.cachedFluidChecks.put(fluidTag, new CachedFluidCheck(inFluid, this.getFluidHeight(fluidTag)));
            return inFluid;
        }
        if (cached.height() > 0.0D) {
            this.fluidHeight.put(fluidTag, cached.height());
        }
        return cached.inFluid();
    }

    private record CachedFluidCheck(boolean inFluid, double height) {
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
                && this.tickCount % (this.isFightingRoyalRival() ? RIVAL_OBSTRUCTION_CLEAR_INTERVAL_TICKS : OBSTRUCTION_CLEAR_INTERVAL_TICKS) == 0
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
            this.setNoGravity(!GravityApi.isGravityInverted(this));
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
            this.resetRoyalCombatTracking();
            return;
        }

        this.initializeMultiplayerScaling(primaryTarget);
        this.assignHeadTargets(primaryTarget);
        this.tickRoyalTargetElevation(primaryTarget);

        this.tickCombatLocomotionMode(primaryTarget);
        this.tickRoyalStuckRecovery(primaryTarget);
        boolean movementBlocksAttacks = this.blocksRoyalAttacksForMovement();
        if (!movementBlocksAttacks) {
            boolean aeriallyStabilized = this.steerTowardTarget(primaryTarget);
            if (!aeriallyStabilized) {
                this.getLookControl().setLookAt(primaryTarget, 30.0F, 30.0F);
            }
        }
        this.tickRoyalBeam(primaryTarget,
                !movementBlocksAttacks && !this.blocksRoyalBeamScheduling());
        if (movementBlocksAttacks) {
            return;
        }
        this.tickBodyCrush();
        this.tickRoyalCloseRangeAnswer(primaryTarget);
        this.tickRoyalConcealedTarget(primaryTarget);

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

    private void tickRoyalBeam(LivingEntity primaryTarget, boolean allowScheduling) {
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
            BeamController controller = this.beamControllers[index];
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
                        end -> this.setRoyalBeamEndPosition(head.slot(), end),
                        this.royalBeamUsesTrackedAim());
                Vec3 beamEnd = controller.beamEndPosition();
                if (beamEnd != null) {
                    this.tickRoyalBeamEffects(head, this.beamAnchor(head), beamEnd);
                }
                if (!controller.isFiring()) {
                    this.stopRoyalBeam(head);
                }
                continue;
            }
        }
        if (!allowScheduling || this.recoveryWindowTicks > 0) {
            return;
        }
        for (RoyalHead head : this.heads) {
            int index = head.slot().ordinal();
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
            BeamSettings beamSettings = this.royalBeamSettings();
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
                            RoyalBossEntity.this.playRoyalSound(RoyalBossEntity.this.royalBeamStartSound(head),
                                    RoyalBossEntity.this.royalBeamStartSoundVolume(head), 1.0F);
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

    /** Uses the beam controller's smoothed target point instead of the boss body's forward axis. */
    protected boolean royalBeamUsesTrackedAim() {
        return false;
    }

    protected boolean isRoyalRecoveryActive() {
        return this.recoveryWindowTicks > 0;
    }

    protected void startRoyalRecovery(int ticks) {
        this.recoveryWindowTicks = Math.max(this.recoveryWindowTicks, ticks);
    }

    protected final void addGravityAwareImpulse(Entity entity, Vec3 worldImpulse) {
        GravityApi.setWorldVelocity(entity,
                GravityApi.getWorldVelocity(entity).add(worldImpulse));
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
        double encounterRange = Math.max(32.0D, this.getAttributeValue(Attributes.FOLLOW_RANGE));
        List<ServerPlayer> players = level.getPlayers(player -> player.isAlive()
                && !player.isSpectator()
                && player.distanceToSqr(this) <= encounterRange * encounterRange);
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

    protected final void stopRoyalBeamsForDirectedAttack() {
        for (RoyalHead head : this.heads) {
            if (head.beamActive() || head.shooting()) {
                this.attackScheduler.cancel(this.headLane(head));
                this.stopRoyalBeam(head);
            }
        }
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
        double standoff = this.combatStandoffDistance();
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
        boolean inverted = GravityApi.isGravityInverted(this);
        double groundBelowTarget = this.groundYBelow(wanted.x, wanted.z, target.getY() + 1.0D);
        double wantedY = this.landingForCombat
                ? (inverted ? target.getY() + target.getBbHeight() - this.getBbHeight() : groundBelowTarget)
                : inverted
                ? target.getY() - 3.0D
                : Mth.clamp(target.getY() + 3.0D,
                groundBelowTarget + FLYING_MIN_HOVER,
                Math.max(groundBelowTarget + FLYING_MAX_HOVER_ABOVE_GROUND, target.getY() + 4.0D));
        if (!this.landingForCombat && verticalDistance > 6.0D) {
            if (this.aerialCombatAnchor == null || this.aerialCombatAnchorTicks-- <= 0) {
                this.aerialCombatAnchor = new Vec3(wanted.x, wantedY, wanted.z);
                this.aerialCombatAnchorTicks = 20;
            }
            Vec3 anchor = this.aerialCombatAnchor;
            this.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, Math.min(approachSpeed, 0.85D));
            return true;
        }
        this.aerialCombatAnchor = null;
        this.aerialCombatAnchorTicks = 0;

        boolean closeVertically = Math.abs(wantedY - this.getY()) <= 2.5D;
        if (atStandoff && closeVertically) {
            this.getNavigation().stop();
        } else {
            this.getMoveControl().setWantedPosition(wanted.x, wantedY, wanted.z, approachSpeed);
        }
        return false;
    }

    private void resetRoyalCombatTracking() {
        this.royalDeadZoneTicks = 0;
        this.royalConcealedTicks = 0;
        this.royalElevatedTargetTicks = 0;
        this.royalStuckSamplePosition = null;
        this.royalStuckSampleTicks = 0;
        this.royalStuckStrikes = 0;
    }

    private void tickRoyalTargetElevation(LivingEntity target) {
        Vec3 local = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(this.position()),
                GravityApi.getGravityDirection(this));
        if (local.y > ELEVATED_TARGET_HEIGHT) {
            this.royalElevatedTargetTicks++;
        } else {
            this.royalElevatedTargetTicks = 0;
        }
    }

    final void markRoyalMoveRequested() {
        this.royalLastMoveRequestTick = this.tickCount;
    }

    private void tickRoyalStuckRecovery(LivingEntity target) {
        if (this.royalTeleportCooldownTicks > 0) {
            this.royalTeleportCooldownTicks--;
        }
        MoveControl control = this.getMoveControl();
        boolean requested = (long) this.tickCount - (long) this.royalLastMoveRequestTick <= 2L
                && this.distanceToSqr(control.getWantedX(), control.getWantedY(), control.getWantedZ())
                > STUCK_MIN_WANTED_DISTANCE_SQR;
        if (!requested) {
            this.royalStuckSamplePosition = null;
            this.royalStuckSampleTicks = 0;
            this.royalStuckStrikes = 0;
            return;
        }
        if (this.royalStuckSamplePosition == null) {
            this.royalStuckSamplePosition = this.position();
            this.royalStuckSampleTicks = 0;
            return;
        }
        if (++this.royalStuckSampleTicks < STUCK_SAMPLE_TICKS) {
            return;
        }
        boolean progressed = this.position().distanceToSqr(this.royalStuckSamplePosition) >= STUCK_MIN_PROGRESS_SQR;
        this.royalStuckSamplePosition = this.position();
        this.royalStuckSampleTicks = 0;
        if (progressed) {
            this.royalStuckStrikes = 0;
            return;
        }
        this.royalStuckStrikes++;
        this.onRoyalMovementObstructed(target);
    }

    private void onRoyalMovementObstructed(LivingEntity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.clearRoyalPathTowardWanted(serverLevel);
        }
        if (this.royalStuckStrikes >= STUCK_TAKEOFF_STRIKES) {
            this.onRoyalStuck(target);
            if (this.isFlyingBoss()) {
                this.addGravityAwareImpulse(this, GravityRotationUtil.vecPlayerToWorld(
                        0.0D, 0.6D, 0.0D, GravityApi.getGravityDirection(this)));
                this.hasImpulse = true;
            }
        }
        if (this.royalStuckStrikes >= STUCK_TELEPORT_STRIKES && this.royalTeleportCooldownTicks <= 0
                && this.tryRoyalRecoveryTeleport(target)) {
            this.royalStuckStrikes = 0;
            this.royalStuckSamplePosition = null;
            this.royalTeleportCooldownTicks = STUCK_TELEPORT_COOLDOWN_TICKS;
        }
    }

    private void clearRoyalPathTowardWanted(ServerLevel level) {
        MoveControl control = this.getMoveControl();
        Vec3 toWanted = new Vec3(control.getWantedX() - this.getX(), control.getWantedY() - this.getY(),
                control.getWantedZ() - this.getZ());
        Vec3 horizontal = toWanted.multiply(1.0D, 0.0D, 1.0D);
        Vec3 direction;
        if (horizontal.lengthSqr() > 1.0E-4D) {
            direction = horizontal.normalize();
        } else {
            double yaw = this.yBodyRot * Mth.DEG_TO_RAD;
            direction = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
        }
        Vec3 side = new Vec3(-direction.z, 0.0D, direction.x);
        var gravity = GravityApi.getGravityDirection(this);
        double reach = this.getBbWidth() * 0.5D + 1.0D;
        double[] heights = {1.5D, this.getBbHeight() * 0.4D, this.getBbHeight() * 0.75D};
        double[] laterals = {-this.getBbWidth() * 0.3D, 0.0D, this.getBbWidth() * 0.3D};
        for (double height : heights) {
            for (double lateral : laterals) {
                Vec3 center = this.position().add(direction.scale(reach)).add(side.scale(lateral))
                        .add(GravityRotationUtil.vecPlayerToWorld(0.0D, height, 0.0D, gravity));
                RoyalBlockDestruction.destroySphere(level, this, center, OBSTRUCTION_CLEAR_RADIUS,
                        OBSTRUCTION_CLEAR_MAX_BLOCKS, OBSTRUCTION_CLEAR_MAX_RESISTANCE, OBSTRUCTION_CLEAR_DROP_CHANCE);
            }
        }
        if (toWanted.y > 3.0D) {
            RoyalBlockDestruction.destroySphere(level, this,
                    this.position().add(GravityRotationUtil.vecPlayerToWorld(0.0D, this.getBbHeight() + 1.0D, 0.0D, gravity)),
                    OBSTRUCTION_CLEAR_RADIUS, OBSTRUCTION_CLEAR_MAX_BLOCKS, OBSTRUCTION_CLEAR_MAX_RESISTANCE,
                    OBSTRUCTION_CLEAR_DROP_CHANCE);
        }
    }

    private boolean tryRoyalRecoveryTeleport(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level)) {
            return false;
        }
        boolean inverted = GravityApi.isGravityInverted(this);
        for (int attempt = 0; attempt < STUCK_TELEPORT_ATTEMPTS; attempt++) {
            double angle = this.random.nextDouble() * Mth.TWO_PI;
            double radius = 22.0D + this.random.nextDouble() * 14.0D;
            double x = target.getX() + Math.cos(angle) * radius;
            double z = target.getZ() + Math.sin(angle) * radius;
            double y = inverted
                    ? target.getY() + target.getBbHeight() - this.getBbHeight()
                    : this.groundYBelow(x, z, target.getY() + 1.0D)
                    + (this.isRoyalFlying() ? FLYING_PREFERRED_HOVER : 0.1D);
            AABB box = this.getBoundingBox().move(x - this.getX(), y - this.getY(), z - this.getZ());
            if (!level.noCollision(this, box) || level.containsAnyLiquid(box)) {
                continue;
            }
            this.sendRoyalRelocationBurst(level);
            this.getNavigation().stop();
            this.teleportTo(x, y, z);
            this.setDeltaMovement(Vec3.ZERO);
            this.sendRoyalRelocationBurst(level);
            level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 3.0F, 0.55F);
            return true;
        }
        return false;
    }

    private void sendRoyalRelocationBurst(ServerLevel level) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                80, this.getBbWidth() * 0.3D, this.getBbHeight() * 0.3D, this.getBbWidth() * 0.3D, 0.2D);
    }

    private double royalDeadZoneRadius() {
        return this.getBbWidth() * 0.5D + WING_BUFFET_EXTRA_RADIUS;
    }

    private boolean isInRoyalDeadZone(LivingEntity living) {
        Vec3 local = GravityRotationUtil.vecWorldToPlayer(living.position().subtract(this.position()),
                GravityApi.getGravityDirection(this));
        double radius = this.royalDeadZoneRadius();
        if (local.x * local.x + local.z * local.z > radius * radius
                || local.y < -WING_BUFFET_BELOW
                || local.y > this.getBbHeight() + WING_BUFFET_ABOVE) {
            return false;
        }
        for (RoyalHead head : this.heads) {
            if (this.headWithinBiteReach(head, living)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasRoyalDeadZoneIntruder(ServerLevel level, LivingEntity primaryTarget) {
        if (!(primaryTarget instanceof Player) && this.isInRoyalDeadZone(primaryTarget)) {
            return true;
        }
        AABB area = this.getBoundingBox().inflate(WING_BUFFET_EXTRA_RADIUS + 1.0D, WING_BUFFET_BELOW,
                WING_BUFFET_EXTRA_RADIUS + 1.0D);
        for (Player player : level.getEntitiesOfClass(Player.class, area,
                candidate -> candidate.isAlive() && !candidate.isSpectator() && !candidate.isCreative())) {
            if (this.canDamageWithRoyalAttack(player) && this.isInRoyalDeadZone(player)) {
                return true;
            }
        }
        return false;
    }

    private void tickRoyalCloseRangeAnswer(LivingEntity primaryTarget) {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        boolean intruder = this.hasRoyalDeadZoneIntruder(level, primaryTarget);
        this.royalDeadZoneTicks = intruder ? this.royalDeadZoneTicks + 1 : Math.max(0, this.royalDeadZoneTicks - 2);
        Phase phase = this.phase();
        if (this.royalDeadZoneTicks < this.royalDeadZoneTriggerTicks(phase)
                || this.recoveryWindowTicks > 0
                || !this.attackScheduler.ready(WING_BUFFET_ID, RoyalAttackLane.BODY)) {
            return;
        }
        if (this.beginRoyalAttack(WING_BUFFET_ID, RoyalAttackLane.BODY, this.royalWingBuffetCooldownTicks(phase),
                WING_BUFFET_WINDUP_TICKS, 1, this.animationRecovery(55, WING_BUFFET_WINDUP_TICKS, 1, 12),
                new RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        RoyalBossEntity.this.triggerAnim("wing_action", "wing_gust");
                        RoyalBossEntity.this.playRoyalSound(RoyalBossEntity.this.royalWingBuffetSound(), 0.75F);
                    }

                    @Override
                    public void onWindup(int elapsedTicks, int remainingTicks) {
                        RoyalBossEntity.this.sendRoyalWingBuffetTelegraph(level, elapsedTicks);
                    }

                    @Override
                    public void onActive(int elapsedTicks) {
                        RoyalBossEntity.this.performRoyalWingBuffet(level);
                    }
                })) {
            this.royalDeadZoneTicks = 0;
        }
    }

    private void sendRoyalWingBuffetTelegraph(ServerLevel level, int elapsedTicks) {
        if (elapsedTicks % 2 != 0) {
            return;
        }
        double progress = Math.min(1.0D, elapsedTicks / (double) WING_BUFFET_WINDUP_TICKS);
        double radius = Mth.lerp(progress, this.getBbWidth() * 0.3D, this.royalDeadZoneRadius());
        var gravity = GravityApi.getGravityDirection(this);
        for (int index = 0; index < 40; index++) {
            double angle = Mth.TWO_PI * index / 40.0D;
            Vec3 point = this.position().add(GravityRotationUtil.vecPlayerToWorld(
                    Math.cos(angle) * radius, 0.3D, Math.sin(angle) * radius, gravity));
            level.sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z, 1, 0.0D, 0.02D, 0.0D, 0.0D);
        }
    }

    private void performRoyalWingBuffet(ServerLevel level) {
        float damage = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * WING_BUFFET_DAMAGE_SCALE);
        DamageSource source = this.damageSources().mobAttack(this);
        var gravity = GravityApi.getGravityDirection(this);
        double radius = this.royalDeadZoneRadius() + 2.0D;
        AABB area = this.getBoundingBox().inflate(WING_BUFFET_EXTRA_RADIUS + 3.0D, WING_BUFFET_BELOW + 2.0D,
                WING_BUFFET_EXTRA_RADIUS + 3.0D);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive() && entity != this && this.canDamageWithRoyalAttack(entity))) {
            if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
                continue;
            }
            Vec3 local = GravityRotationUtil.vecWorldToPlayer(living.position().subtract(this.position()), gravity);
            double horizontalSqr = local.x * local.x + local.z * local.z;
            if (horizontalSqr > radius * radius
                    || local.y < -WING_BUFFET_BELOW - 2.0D
                    || local.y > this.getBbHeight() + WING_BUFFET_ABOVE + 2.0D) {
                continue;
            }
            living.hurt(source, damage);
            if (living instanceof RoyalBossEntity) {
                continue;
            }
            Vec3 outwardLocal = horizontalSqr > 1.0E-4D
                    ? new Vec3(local.x, 0.0D, local.z).normalize()
                    : new Vec3(1.0D, 0.0D, 0.0D);
            Vec3 outward = GravityRotationUtil.vecPlayerToWorld(outwardLocal, gravity);
            Vec3 lift = GravityRotationUtil.vecPlayerToWorld(0.0D, WING_BUFFET_LIFT, 0.0D,
                    GravityApi.getGravityDirection(living));
            this.addGravityAwareImpulse(living, outward.scale(WING_BUFFET_PUSH).add(lift));
            living.hasImpulse = true;
            living.hurtMarked = true;
        }
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = this.getBbWidth() * 0.22D * ring;
            int samples = 16 + ring * 8;
            for (int sample = 0; sample < samples; sample++) {
                double angle = Mth.TWO_PI * sample / samples;
                Vec3 point = this.position().add(GravityRotationUtil.vecPlayerToWorld(
                        Math.cos(angle) * ringRadius, 1.0D, Math.sin(angle) * ringRadius, gravity));
                Vec3 velocity = GravityRotationUtil.vecPlayerToWorld(
                        Math.cos(angle) * 0.45D, 0.04D, Math.sin(angle) * 0.45D, gravity);
                level.sendParticles(ParticleTypes.CLOUD, point.x, point.y, point.z,
                        0, velocity.x, velocity.y, velocity.z, 1.0D);
            }
        }
        level.sendParticles(ParticleTypes.SWEEP_ATTACK, this.getX(), this.getY() + 2.0D, this.getZ(),
                16, this.getBbWidth() * 0.3D, 1.0D, this.getBbWidth() * 0.3D, 0.0D);
        this.playRoyalSound(this.royalWingBuffetSound(), 0.6F);
    }

    private void tickRoyalConcealedTarget(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel level) || this.tickCount % 10 != 0) {
            return;
        }
        if (this.distanceToSqr(target) > EXCAVATION_RANGE * EXCAVATION_RANGE || this.royalCanSeeTarget(target)) {
            this.royalConcealedTicks = 0;
            return;
        }
        this.royalConcealedTicks += 10;
        if (this.royalConcealedTicks < EXCAVATION_TRIGGER_TICKS
                || this.recoveryWindowTicks > 0
                || !this.attackScheduler.ready(EXCAVATION_ID, RoyalAttackLane.SUMMON)) {
            return;
        }
        RoyalHead head = this.heads[RoyalHead.Slot.CENTER.ordinal()];
        LivingEntity committedTarget = target;
        if (this.beginRoyalAttack(EXCAVATION_ID, RoyalAttackLane.SUMMON, EXCAVATION_COOLDOWN_TICKS,
                EXCAVATION_WINDUP_TICKS, 1, 10, new RoyalAttackScheduler.Action() {
                    @Override
                    public void onStart() {
                        if (head.readyToAttack()) {
                            RoyalBossEntity.this.triggerAnim(head.slot().controllerName(), "shoot");
                        }
                        RoyalBossEntity.this.playRoyalSound(RoyalBossEntity.this.royalBeamStartSound(head), 0.8F);
                    }

                    @Override
                    public void onWindup(int elapsedTicks, int remainingTicks) {
                        if (committedTarget.isAlive() && committedTarget.level() == level && elapsedTicks % 2 == 0) {
                            RoyalBossEntity.this.sendRoyalExcavationTelegraph(level, committedTarget);
                        }
                    }

                    @Override
                    public void onActive(int elapsedTicks) {
                        RoyalBossEntity.this.performRoyalExcavation(level, committedTarget);
                    }
                })) {
            this.royalConcealedTicks = 0;
        }
    }

    private boolean royalCanSeeTarget(LivingEntity target) {
        Vec3 targetEye = target.getEyePosition();
        if (this.clearRoyalSightLine(this.getEyePosition(), targetEye)) {
            return true;
        }
        for (RoyalHead head : this.heads) {
            if (this.clearRoyalSightLine(this.headAnchor(head), targetEye)) {
                return true;
            }
        }
        return false;
    }

    private boolean clearRoyalSightLine(Vec3 from, Vec3 to) {
        return this.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this))
                .getType() == HitResult.Type.MISS;
    }

    private void sendRoyalExcavationTelegraph(ServerLevel level, LivingEntity target) {
        Vec3 origin = this.headAnchor(this.heads[RoyalHead.Slot.CENTER.ordinal()]);
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 line = targetCenter.subtract(origin);
        double length = line.length();
        if (length > 1.0E-3D) {
            Vec3 direction = line.scale(1.0D / length);
            for (double walked = 0.0D; walked <= length; walked += 2.0D) {
                Vec3 point = origin.add(direction.scale(walked));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
            }
        }
        for (int index = 0; index < 20; index++) {
            double angle = Mth.TWO_PI * index / 20.0D;
            level.sendParticles(ParticleTypes.END_ROD,
                    targetCenter.x + Math.cos(angle) * EXCAVATION_RADIUS, targetCenter.y,
                    targetCenter.z + Math.sin(angle) * EXCAVATION_RADIUS,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void performRoyalExcavation(ServerLevel level, LivingEntity target) {
        if (!target.isAlive() || target.level() != level) {
            return;
        }
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 origin = this.headAnchor(this.heads[RoyalHead.Slot.CENTER.ordinal()]);
        Vec3 toward = origin.subtract(targetCenter);
        double length = toward.length();
        Vec3 direction = length > 1.0E-4D ? toward.scale(1.0D / length) : Vec3.ZERO;
        double depth = Math.min(length, EXCAVATION_DEPTH);
        for (double step = 1.5D; step <= depth; step += EXCAVATION_RADIUS) {
            Vec3 center = targetCenter.add(direction.scale(step));
            RoyalBlockDestruction.destroySphere(level, this, center, EXCAVATION_RADIUS, EXCAVATION_MAX_BLOCKS,
                    OBSTRUCTION_CLEAR_MAX_RESISTANCE, OBSTRUCTION_CLEAR_DROP_CHANCE);
            level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 1, 0.4D, 0.4D, 0.4D, 0.0D);
        }
        level.playSound(null, targetCenter.x, targetCenter.y, targetCenter.z,
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.7F);
        if (!this.royalCanSeeTarget(target)) {
            target.hurt(this.damageSources().indirectMagic(this, this),
                    this.scaleRoyalDamage(EXCAVATION_CONCEALED_DAMAGE));
        }
    }

    @Nullable
    final Vec3 royalFacingPoint() {
        if (this.level().isClientSide || this.isDeadOrDying() || this.phaseTransitionTicks > 0
                || GravityApi.isGravityInverted(this)) {
            return null;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || !this.canAttack(target) || !this.royalShouldFaceTarget(target)) {
            return null;
        }
        return target.position();
    }

    final void turnRoyalBodyToward(Vec3 point) {
        double dx = point.x - this.getX();
        double dz = point.z - this.getZ();
        if (dx * dx + dz * dz < 1.0E-4D) {
            return;
        }
        float desiredYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float turn = Mth.clamp(Mth.wrapDegrees(desiredYaw - this.getYRot()), -FACING_TURN_RATE, FACING_TURN_RATE);
        this.setYRot(this.getYRot() + turn);
        this.yBodyRot = this.getYRot();
    }

    private static void applyRelativeInput(Mob mob, double dx, double dz, float speed) {
        double length = Math.sqrt(dx * dx + dz * dz);
        double yaw = mob.getYRot() * Mth.DEG_TO_RAD;
        double nx = dx / length;
        double nz = dz / length;
        float forward = (float) (-nx * Math.sin(yaw) + nz * Math.cos(yaw));
        float strafe = (float) (nx * Math.cos(yaw) + nz * Math.sin(yaw));
        mob.setSpeed(speed);
        mob.setZza(forward * speed);
        mob.setXxa(strafe * speed);
    }

    private static final class RoyalGroundMoveControl extends MoveControl {
        private final RoyalBossEntity boss;

        private RoyalGroundMoveControl(RoyalBossEntity boss) {
            super(boss);
            this.boss = boss;
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speed) {
            super.setWantedPosition(x, y, z, speed);
            this.boss.markRoyalMoveRequested();
        }

        @Override
        public void tick() {
            Vec3 facing = this.boss.royalFacingPoint();
            if (facing == null) {
                super.tick();
                return;
            }
            if (this.operation != Operation.MOVE_TO) {
                boolean waiting = this.operation == Operation.WAIT;
                super.tick();
                if (waiting) {
                    this.mob.setXxa(0.0F);
                }
                this.boss.turnRoyalBodyToward(facing);
                return;
            }
            this.operation = Operation.WAIT;
            double dx = this.wantedX - this.mob.getX();
            double dy = this.wantedY - this.mob.getY();
            double dz = this.wantedZ - this.mob.getZ();
            double horizontalSqr = dx * dx + dz * dz;
            this.boss.turnRoyalBodyToward(facing);
            if (horizontalSqr < 2.5E-7D) {
                this.mob.setZza(0.0F);
                this.mob.setXxa(0.0F);
                return;
            }
            float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            applyRelativeInput(this.mob, dx, dz, speed);
            if (dy > this.mob.maxUpStep() && horizontalSqr < Math.max(1.0F, this.mob.getBbWidth())) {
                this.mob.getJumpControl().jump();
                this.operation = Operation.JUMPING;
            }
        }
    }

    private static final class RoyalFlyingMoveControl extends FlyingMoveControl {
        private final RoyalBossEntity boss;
        private final int maxPitchTurn;

        private RoyalFlyingMoveControl(RoyalBossEntity boss, int maxTurn) {
            super(boss, maxTurn, true);
            this.boss = boss;
            this.maxPitchTurn = maxTurn;
        }

        @Override
        public void setWantedPosition(double x, double y, double z, double speed) {
            super.setWantedPosition(x, y, z, speed);
            this.boss.markRoyalMoveRequested();
        }

        @Override
        public void tick() {
            Vec3 facing = this.boss.royalFacingPoint();
            if (facing == null) {
                super.tick();
                return;
            }
            if (this.operation != Operation.MOVE_TO) {
                super.tick();
                this.mob.setXxa(0.0F);
                this.boss.turnRoyalBodyToward(facing);
                return;
            }
            this.operation = Operation.WAIT;
            this.mob.setNoGravity(true);
            double dx = this.wantedX - this.mob.getX();
            double dy = this.wantedY - this.mob.getY();
            double dz = this.wantedZ - this.mob.getZ();
            this.boss.turnRoyalBodyToward(facing);
            if (dx * dx + dy * dy + dz * dz < 2.5E-7D) {
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
                this.mob.setXxa(0.0F);
                return;
            }
            float speed = (float) (this.speedModifier * this.mob.getAttributeValue(
                    this.mob.onGround() ? Attributes.MOVEMENT_SPEED : Attributes.FLYING_SPEED));
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            if (horizontal > 1.0E-5D) {
                applyRelativeInput(this.mob, dx, dz, speed);
            } else {
                this.mob.setSpeed(speed);
                this.mob.setZza(0.0F);
                this.mob.setXxa(0.0F);
            }
            float pitch = (float) (-(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG));
            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), pitch, this.maxPitchTurn));
            this.mob.setYya(dy > 0.0D ? speed : -speed);
        }
    }

    protected double combatStandoffDistance() {
        return Math.max(this.getBbWidth() * 0.55D,
                BEAM_MUZZLE_FORWARD - this.biteReach() * 0.65D + 1.0D);
    }

    private void tickCombatLocomotionMode(LivingEntity target) {
        if (!this.isFlyingBoss() || this.managesOwnCombatLocomotion()) {
            return;
        }
        boolean inverted = GravityApi.isGravityInverted(this);
        double groundY = this.groundYBelow(this.getX(), this.getZ());
        if (this.landingForCombat) {
            if (!target.onGround() || ++this.landingForCombatTicks > LANDING_FOR_COMBAT_TIMEOUT_TICKS) {
                this.landingForCombat = false;
                this.landingForCombatTicks = 0;
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
                this.landingForCombatTicks = 0;
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
        this.setNoGravity(flying && !GravityApi.isGravityInverted(this));
    }

    protected double groundYBelow(double x, double z) {
        return this.groundYBelow(x, z, this.getY() + 1.0D);
    }

    protected double groundYBelow(double x, double z, double referenceY) {
        Level level = this.level();
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int surface = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                blockX, blockZ);
        int startY = Mth.floor(referenceY);
        if (startY >= surface) {
            return surface;
        }
        net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos(blockX, startY, blockZ);
        if (this.isRoyalGroundBlock(level, cursor)) {
            int top = Math.min(surface, startY + GROUND_SCAN_RISE);
            for (int y = startY + 1; y <= top; y++) {
                cursor.setY(y);
                if (!this.isRoyalGroundBlock(level, cursor)) {
                    return y;
                }
            }
            return surface;
        }
        int bottom = Math.max(level.getMinBuildHeight(), startY - GROUND_SCAN_DEPTH);
        for (int y = startY - 1; y >= bottom; y--) {
            cursor.setY(y);
            if (this.isRoyalGroundBlock(level, cursor)) {
                return y + 1;
            }
        }
        return bottom;
    }

    protected double ceilingYAbove(double x, double z, double referenceY) {
        Level level = this.level();
        net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos(
                Mth.floor(x), Mth.floor(referenceY), Mth.floor(z));
        int startY = cursor.getY();
        int top = Math.min(level.getMaxBuildHeight() - 1, startY + GROUND_SCAN_DEPTH);
        for (int y = startY; y <= top; y++) {
            cursor.setY(y);
            if (this.isRoyalGroundBlock(level, cursor)) {
                return y;
            }
        }
        return top;
    }

    private boolean isRoyalGroundBlock(Level level, net.minecraft.core.BlockPos pos) {
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
            return false;
        }
        return !state.getFluidState().isEmpty() || !state.getCollisionShape(level, pos).isEmpty();
    }

    private void tickFlyingAltitude() {
        if (GravityApi.isGravityInverted(this)) {
            return;
        }
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.2D, 0.0D));
        }
        double groundY = this.groundYBelow(this.getX(), this.getZ());
        double ceiling = groundY + FLYING_MAX_HOVER_ABOVE_GROUND;
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            ceiling = Math.max(ceiling, target.getY() + 8.0D);
        }
        if (this.getY() > ceiling) {
            double desiredY = groundY + FLYING_PREFERRED_HOVER;
            this.getMoveControl().setWantedPosition(this.getX(), desiredY, this.getZ(), 1.0D);
            Vec3 delta = this.getDeltaMovement();
            if (delta.y > -0.1D) {
                this.setDeltaMovement(delta.x, Math.max(-0.5D, delta.y - 0.1D), delta.z);
            }
        }
    }

    private boolean isFightingRoyalRival() {
        return this.getTarget() instanceof RoyalBossEntity;
    }

    private void tickBodyCrush() {
        if (!(this.level() instanceof ServerLevel serverLevel)
                || this.tickCount % (this.isFightingRoyalRival() ? RIVAL_BODY_CRUSH_INTERVAL_TICKS : BODY_CRUSH_INTERVAL_TICKS) != 0) {
            return;
        }
        Vec3 horizontalMovement = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        boolean moving = horizontalMovement.lengthSqr() >= 0.0016D;
        if (!moving && !this.horizontalCollision) {
            return;
        }
        Vec3 forward;
        if (moving) {
            forward = horizontalMovement.normalize();
        } else {
            Vec3 toWanted = new Vec3(this.getMoveControl().getWantedX() - this.getX(), 0.0D,
                    this.getMoveControl().getWantedZ() - this.getZ());
            if (toWanted.lengthSqr() > 1.0E-4D) {
                forward = toWanted.normalize();
            } else {
                double yaw = this.yBodyRot * Mth.DEG_TO_RAD;
                forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
            }
        }
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
        float damage = (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) * this.biteDamageMultiplier());
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
        return this.position().add(GravityRotationUtil.vecPlayerToWorld(localOffset,
                GravityApi.getGravityDirection(this)));
    }

    protected Vec3 beamAnchor(RoyalHead head) {
        return beamAnchor(head.slot(), this.getX(), this.getY(), this.getZ(), this.getYRot());
    }

    protected Vec3 beamDirection(RoyalHead head) {
        float yaw = this.getYRot() * Mth.DEG_TO_RAD;
        double pitch = this.royalBeamPitch(head);
        double horizontal = Math.cos(pitch);
        Vec3 localDirection = new Vec3(-Math.sin(yaw) * horizontal, Math.sin(pitch), Math.cos(yaw) * horizontal);
        return GravityRotationUtil.vecPlayerToWorld(localDirection,
                GravityApi.getGravityDirection(this)).normalize();
    }

    private double royalBeamPitch(RoyalHead head) {
        if (this.level().isClientSide) {
            return 0.0D;
        }
        LivingEntity target = head.target(this.level());
        if (target == null) {
            return 0.0D;
        }
        Vec3 local = GravityRotationUtil.vecWorldToPlayer(target.getEyePosition().subtract(this.beamAnchor(head)),
                GravityApi.getGravityDirection(this));
        double horizontal = Math.sqrt(local.x * local.x + local.z * local.z);
        if (horizontal < 1.0E-3D) {
            return 0.0D;
        }
        return Mth.clamp(Math.atan2(local.y, horizontal), -MAX_BEAM_PITCH, MAX_BEAM_PITCH);
    }

    protected boolean targetInBeamLine(RoyalHead head, LivingEntity target) {
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
        return new Vec3(x, y, z).add(GravityRotationUtil.vecPlayerToWorld(localOffset,
                GravityApi.getGravityDirection(this)));
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
            for (BeamController controller : this.beamControllers) {
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
        RoyalLootDropGlow.start(itemEntity);
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
        float cap = (float) AntarchySettings.royalBossMaxSingleHitDamage();
        float cappedAmount = amount <= cap ? amount : cap + (amount - cap) * (float) AntarchySettings.royalBossHitOverflowRate();
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

    public BeamController beamController() {
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

    @Override
    public float scaleBeamDamage(float damage) {
        return this.scaleRoyalDamage(damage);
    }

    @Override
    public boolean canBeamDamage(LivingEntity target) {
        return this.canDamageWithRoyalAttack(target);
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
