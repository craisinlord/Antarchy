package com.craisinlord.antarchy.content.entity.brutalfly;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEcho;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BrutalflyEntity extends Monster implements GeoEntity, GlimmerEcho {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY_ANIM = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation SPIT_ANIM = RawAnimation.begin().thenPlay("spit");
    private static final RawAnimation SWIPE_ANIM = RawAnimation.begin().thenPlay("swipe");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation COOCON_ANIM = RawAnimation.begin().thenPlay("Coocon");
    private static final RawAnimation COOCON_HIT_ANIM = RawAnimation.begin().thenPlay("Coocon_hit");
    private static final RawAnimation COOCON_IDLE_ANIM = RawAnimation.begin().thenLoop("Coocon_idle");
    private static final RawAnimation COOCON_HATCH_ANIM = RawAnimation.begin().thenPlay("Coocon_hatch");

    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(BrutalflyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_COCOONED =
            SynchedEntityData.defineId(BrutalflyEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GLIMMER_ECHO =
            SynchedEntityData.defineId(BrutalflyEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ANIM_IDLE = 0;
    private static final int ANIM_FLY = 1;
    private static final int ANIM_SPIT = 2;
    private static final int ANIM_SWIPE = 3;
    private static final int ANIM_DEATH = 4;
    private static final int ANIM_COCOON = 5;
    private static final int ANIM_COCOON_HIT = 6;
    private static final int ANIM_COCOON_IDLE = 7;
    private static final int ANIM_COCOON_HATCH = 8;

    private static final int DEATH_ANIM_TICKS = 60;
    private static final int SPIT_WINDUP_TICKS = 16;
    private static final int SWIPE_TICKS = 30;
    private static final int SWIPE_LOCK_ELAPSED = 6;
    private static final int SWIPE_HIT_ELAPSED = 12;
    private static final int SWIPE_LUNGE_END_ELAPSED = 14;
    private static final double SWIPE_LUNGE_DISTANCE = 2.5D;
    private static final double SWIPE_REACH = 5.0D;
    private static final double SWIPE_ARC_COS = 0.2588D;
    private static final double SWIPE_REACH_BELOW = 3.0D;
    private static final double SWIPE_REACH_ABOVE = 1.5D;
    private static final double SWIPE_MAX_START_YAW_ERROR = 60.0D;
    private static final float SWIPE_KNOCKBACK = 2.8F;
    private static final int DIVE_NONE = 0;
    private static final int DIVE_TELEGRAPH = 1;
    private static final int DIVE_ACTIVE = 2;
    private static final int DIVE_RECOVERY = 3;
    private static final int DIVE_TELEGRAPH_TICKS = 22;
    private static final int DIVE_LOCK_REMAINING = 8;
    private static final int DIVE_STRIKE_TICKS = 12;
    private static final int DIVE_MAX_TICKS = 26;
    private static final int DIVE_FIRST_CONTACT_TICK = 6;
    private static final int DIVE_RECOVERY_TICKS = 26;
    private static final int DIVE_STAGGER_TICKS = 44;
    private static final int DIVE_RETRY_TICKS = 20;
    private static final int DIVE_CANCEL_COOLDOWN_TICKS = 40;
    private static final double DIVE_MIN_RANGE_SQR = 20.25D;
    private static final double DIVE_MAX_RANGE_SQR = 256.0D;
    private static final double DIVE_MAX_VERTICAL = 10.0D;
    private static final double DIVE_MIN_SPEED = 0.7D;
    private static final double DIVE_MAX_SPEED = 1.4D;
    private static final double DIVE_OVERSHOOT = 7.0D;
    private static final double DIVE_OVERSHOOT_RISE = 1.5D;
    private static final double DIVE_STRIKE_HEIGHT = 0.6D;
    private static final double DIVE_BLOCKED_MOVE_RATIO = 0.35D;
    private static final float DIVE_DAMAGE_MULTIPLIER = 1.25F;
    private static final float DIVE_KNOCKBACK = 1.8F;
    private static final float PHASE_TWO_THRESHOLD = 0.70F;
    private static final float PHASE_THREE_THRESHOLD = 0.35F;
    private static final int PHASE_TRANSITION_TICKS = 30;
    private static final int PHASE_TRANSITION_CLIP_TICKS = 20;
    private static final double PHASE_SHOVE_RADIUS = 6.0D;
    private static final float PHASE_SHOVE_STRENGTH = 1.4F;
    private static final double FLY_SPEED_SCALE = 0.55D;
    private static final double FLY_ACCELERATION = 0.045D;
    private static final double FLY_ARRIVAL_RATE = 0.18D;
    private static final float YAW_COMBAT_STEP = 8.0F;
    private static final float YAW_COMMIT_STEP = 20.0F;
    private static final float YAW_PATROL_STEP = 6.0F;
    private static final float PITCH_STEP = 5.0F;
    private static final float MAX_PITCH = 35.0F;
    private static final int ORBIT_REFRESH_INTERVAL_TICKS = 5;
    private static final double ORBIT_TANGENTIAL_RATIO = 0.7D;
    private static final double ORBIT_MAX_RATE = 0.09D;
    private static final double ORBIT_RATE_STEP = 0.004D;
    private static final double ORBIT_RADIUS_STEP = 0.08D;
    private static final double ORBIT_HEIGHT_STEP = 0.05D;
    private static final double ORBIT_BOB = 0.5D;
    private static final double ORBIT_MIN_HEIGHT = 0.6D;
    private static final float ORBIT_FLIP_CHANCE = 0.25F;
    private static final int ORBIT_STUCK_LIMIT = 30;
    private static final double[] ORBIT_RADIUS_SCALES = {1.0D, 0.65D};
    private static final double[] ORBIT_HEIGHT_OFFSETS = {0.0D, -1.2D, 1.5D};
    private static final int SIGHT_CHECK_INTERVAL_TICKS = 5;
    private static final int PURSUE_AFTER_UNSEEN_TICKS = 40;
    private static final int DISENGAGE_AFTER_UNSEEN_TICKS = 240;
    private static final int PATH_RETRY_TICKS = 20;
    private static final int PATH_FAILED_RETRY_TICKS = 40;
    private static final double TARGET_DROP_RANGE_SQR = 5184.0D;
    private static final double SWIPE_BENEATH_RANGE_SQR = 16.0D;
    private static final double SWIPE_BENEATH_BELOW = 4.0D;
    private static final double SWIPE_BENEATH_ABOVE = 2.0D;
    private static final int PATROL_RETARGET_TICKS = 60;
    private static final int PATROL_FAILED_RETRY_TICKS = 20;
    private static final int PATROL_GROUND_SCAN = 12;
    private static final double RANGE_FALLOFF_RATIO = 1.67D;
    private static final int RANGE_FEEDBACK_INTERVAL_TICKS = 20;
    private static final int COCOON_HIT_THRESHOLD = 5;
    private static final int COCOON_HIT_COOLDOWN_TICKS = 60;
    private static final int COCOON_HIT_ANIM_TICKS = 30;
    private static final int COCOON_HATCH_ANIM_TICKS = 40;
    private static final int COCOON_IDLE_ANIM_TICKS = 70;
    private static final int POST_COCOON_INVULNERABLE_TICKS = 100;
    private static final int BASE_XP_REWARD = 80;
    private static final int GLIMMER_XP_REWARD = BASE_XP_REWARD * 5;
    private static final double GLIMMER_ABANDON_RANGE = 48.0D;
    private static final int GLIMMER_ABANDON_TICKS = 600;
    private static final ResourceLocation GLIMMER_LOOT_TABLE = new ResourceLocation(Antarchy.MODID, "entities/brutalfly_glimmer");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new EntityLinkedServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);

    @Nullable
    private Vec3 patrolTarget;
    private int animationTicks;
    private int spitWindupTicks;
    private int spitCooldown = 50;
    private int meleeCooldown = 35;
    private int diveCooldown = 80;
    private int deathAnimationTicks;
    private int strafeTicks;
    private float orbitDirection = 1.0F;
    private double orbitAngle;
    private double orbitRate;
    private double currentStrafeRadius = 7.5D;
    private double currentFlightHeight = 4.0D;
    private double wantedStrafeRadius = 7.5D;
    private double wantedFlightHeight = 4.0D;
    private int orbitTargetId = -1;
    private int orbitStuckTicks;
    @Nullable
    private Vec3 orbitPoint;
    private int noSightTicks;
    private int pathRetryTicks;
    private int patrolRetargetTicks;
    private int nextPatrolSearchTick;
    private boolean yawLocked;
    private float lockedYaw;
    @Nullable
    private Vec3 swipeAnchor;
    private int diveStage = DIVE_NONE;
    private int diveStageTicks;
    private int diveActiveTicks;
    private double diveSpeed;
    private boolean diveOvershooting;
    private Vec3 diveStrikePoint = Vec3.ZERO;
    private Vec3 diveOvershootPoint = Vec3.ZERO;
    private Vec3 diveLastPosition = Vec3.ZERO;
    private final Set<Integer> diveHits = new HashSet<>();
    private int phaseStage;
    private int phaseTransitionTicks;
    private int lastRangeFeedbackTick = -RANGE_FEEDBACK_INTERVAL_TICKS;
    private boolean spawnedDeathButterflies;

    @Nullable
    BlockPos anchorPos;
    private int cocoonHits;
    private int cocoonHitCooldownTicks;
    private boolean isHatching;
    private int cocoonAnimTicks;
    private boolean pendingHatchAnim;
    private int postCocoonInvulnerableTicks;
    private int glimmerAbandonTicks;

    public BrutalflyEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new BrutalflyMoveControl(this);
        this.xpReward = BASE_XP_REWARD;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.brutalflyHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.brutalflySwipeDamage())
                .add(Attributes.ARMOR, AntarchySettings.brutalflyArmor())
                .add(Attributes.ARMOR_TOUGHNESS, AntarchySettings.brutalflyArmorToughness())
                .add(Attributes.KNOCKBACK_RESISTANCE, AntarchySettings.brutalflyKnockbackResistance())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.brutalflyMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.brutalflyFlyingSpeed())
                .add(Attributes.FOLLOW_RANGE, 72.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnReason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        double maxHealth = AntarchySettings.brutalflyHealth();
        double swipeDamage = AntarchySettings.brutalflySwipeDamage();
        if (this.isGlimmerEcho()) {
            maxHealth *= AntarchySettings.glimmerBrutalflyHealthMultiplier();
            swipeDamage *= AntarchySettings.glimmerBrutalflyDamageMultiplier();
        }
        Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
        this.setHealth((float) maxHealth);
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(swipeDamage);
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData, dataTag);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ANIMATION_STATE, ANIM_IDLE);
        this.entityData.define(IS_COCOONED, false);
        this.entityData.define(GLIMMER_ECHO, false);
    }

    @Override
    protected void registerGoals() {
        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this);
        hurtByTargetGoal.setAlertOthers(BrutalflyEntity.class);
        hurtByTargetGoal.setUnseenMemoryTicks(DISENGAGE_AFTER_UNSEEN_TICKS + 20);
        this.targetSelector.addGoal(1, hurtByTargetGoal);
        NearestAttackableTargetGoal<Player> playerTargetGoal = new NearestAttackableTargetGoal<>(this, Player.class, true);
        playerTargetGoal.setUnseenMemoryTicks(DISENGAGE_AFTER_UNSEEN_TICKS + 20);
        this.targetSelector.addGoal(2, playerTargetGoal);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (IS_COCOONED.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public boolean isInWall() {
        return !this.isCocooned() && super.isInWall();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        navigation.setCanOpenDoors(false);
        return navigation;
    }

    @Override
    protected void customServerAiStep() {
        if (this.isCocooned()) {
            return;
        }
        super.customServerAiStep();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 0, state -> {
            state.getController().transitionLength(this.isCocooned() ? 0 : 3);
            return switch (this.getAnimationState()) {
                case ANIM_SPIT -> state.setAndContinue(SPIT_ANIM);
                case ANIM_SWIPE -> state.setAndContinue(SWIPE_ANIM);
                case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
                case ANIM_FLY -> state.setAndContinue(FLY_ANIM);
                case ANIM_COCOON -> state.setAndContinue(COOCON_ANIM);
                case ANIM_COCOON_HIT -> state.setAndContinue(COOCON_HIT_ANIM);
                case ANIM_COCOON_IDLE -> state.setAndContinue(COOCON_IDLE_ANIM);
                case ANIM_COCOON_HATCH -> state.setAndContinue(COOCON_HATCH_ANIM);
                default -> state.setAndContinue(IDLE_ANIM);
            };
        })
                .triggerableAnim("death", DEATH_ANIM)
                .triggerableAnim("Coocon", COOCON_ANIM)
                .triggerableAnim("Coocon_hit", COOCON_HIT_ANIM)
                .triggerableAnim("Coocon_idle", COOCON_IDLE_ANIM)
                .triggerableAnim("Coocon_hatch", COOCON_HATCH_ANIM));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            if (!this.isCocooned()) {
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
            return;
        }

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
        }

        if (this.isCocooned()) {
            this.tickCocoon();
            return;
        }

        if (this.animationTicks > 0) {
            this.animationTicks--;
        }
        if (this.spitCooldown > 0) {
            this.spitCooldown--;
        }
        if (this.meleeCooldown > 0) {
            this.meleeCooldown--;
        }
        if (this.diveCooldown > 0) {
            this.diveCooldown--;
        }
        if (this.postCocoonInvulnerableTicks > 0) {
            this.postCocoonInvulnerableTicks--;
        }

        if (this.phaseTransitionTicks <= 0 && this.phaseStage < this.stageForHealth()) {
            this.beginPhaseTransition();
        }
        if (this.phaseTransitionTicks > 0) {
            this.tickPhaseTransition();
            this.updateBaseAnimationState();
            this.updateFlightRotation(this.getTarget());
            return;
        }

        if (this.spitWindupTicks > 0 && --this.spitWindupTicks == 0) {
            this.fireCurrentSpitPattern();
        }

        LivingEntity target = this.getTarget();
        if (target != null && !this.isValidCombatTarget(target)) {
            this.setTarget(null);
            target = null;
        }
        if (target == null && this.tickCount % 10 == 0) {
            Player nearbyPlayer = this.level().getNearestPlayer(this, 40.0D);
            if (nearbyPlayer != null && this.isValidCombatTarget(nearbyPlayer) && this.hasLineOfSight(nearbyPlayer)) {
                this.setTarget(nearbyPlayer);
                target = nearbyPlayer;
            }
        }

        if (this.diveStage != DIVE_NONE) {
            this.tickDive(target);
        } else if (this.getAnimationState() == ANIM_SWIPE && this.animationTicks > 0) {
            this.tickSwipe(target);
        } else {
            this.yawLocked = false;
            this.swipeAnchor = null;
            if (target != null) {
                this.tickCombat(target);
            } else {
                this.resetPursuitState();
                this.tickPatrol();
            }
        }

        this.updateBaseAnimationState();
        this.updateFlightRotation(target);
    }

    private boolean isValidCombatTarget(LivingEntity target) {
        if (!target.isAlive() || target.level() != this.level() || !this.canAttack(target)
                || this.distanceToSqr(target) > TARGET_DROP_RANGE_SQR) {
            return false;
        }
        return !(target instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }

    private void tickCocoon() {
        if (this.cocoonHitCooldownTicks > 0) {
            this.cocoonHitCooldownTicks--;
        }

        if (this.anchorPos != null) {
            this.setPos(this.anchorPos.getX() + 0.5D, this.anchorPos.getY() - 1.5D, this.anchorPos.getZ() + 0.5D);
            this.setDeltaMovement(Vec3.ZERO);

            if (!this.isHatching && !this.level().getBlockState(this.anchorPos).is(BlockTags.LOGS)) {
                this.startHatching();
                return;
            }
        }

        if (this.isHatching) {
            if (this.cocoonAnimTicks > 0) {
                this.cocoonAnimTicks--;
            }
            if (this.pendingHatchAnim && this.cocoonAnimTicks <= 0) {
                this.pendingHatchAnim = false;
                this.setAnimationState(ANIM_COCOON_HATCH);
                this.cocoonAnimTicks = COCOON_HATCH_ANIM_TICKS;
            } else if (!this.pendingHatchAnim && this.cocoonAnimTicks <= 0) {
                this.finishHatch();
            }
            return;
        }

        if (this.cocoonAnimTicks > 0) {
            this.cocoonAnimTicks--;
            if (this.cocoonAnimTicks <= 0) {
                this.setAnimationState(ANIM_COCOON);
            }
            return;
        }

        if (this.getAnimationState() == ANIM_COCOON_IDLE) {
        } else if (this.tickCount % 200 == 17 && this.random.nextFloat() < 0.4F) {
            this.setAnimationState(ANIM_COCOON_IDLE);
            this.cocoonAnimTicks = COCOON_IDLE_ANIM_TICKS;
        } else if (this.getAnimationState() != ANIM_COCOON) {
            this.setAnimationState(ANIM_COCOON);
        }
    }

    private void startHatching() {
        this.isHatching = true;
        this.pendingHatchAnim = true;
        this.postCocoonInvulnerableTicks = POST_COCOON_INVULNERABLE_TICKS;
        this.setAnimationState(ANIM_COCOON_HIT);
        this.cocoonAnimTicks = COCOON_HIT_ANIM_TICKS;
    }

    private void finishHatch() {
        this.entityData.set(IS_COCOONED, false);
        this.bossEvent.setVisible(true);
        this.isHatching = false;
        this.anchorPos = null;
        this.cocoonHits = 0;
        this.cocoonHitCooldownTicks = 0;
        this.refreshDimensions();
        this.setAnimationState(ANIM_FLY);
        Player nearbyPlayer = this.level().getNearestPlayer(this, 48.0D);
        if (nearbyPlayer != null && !nearbyPlayer.isCreative() && !nearbyPlayer.isSpectator()) {
            this.setTarget(nearbyPlayer);
        }
        this.spitCooldown = 0;
    }

    public float getDamageScale() {
        double base = AntarchySettings.brutalflySwipeDamage();
        return base <= 0.0D ? 1.0F : (float) (this.getAttributeValue(Attributes.ATTACK_DAMAGE) / base);
    }

    public boolean isCocooned() {
        return this.entityData.get(IS_COCOONED);
    }

    @Override
    public boolean isGlimmerEcho() {
        return this.entityData.get(GLIMMER_ECHO);
    }

    @Override
    public void setGlimmerEcho(boolean glimmerEcho) {
        boolean becameGlimmer = glimmerEcho && !this.isGlimmerEcho();
        this.entityData.set(GLIMMER_ECHO, glimmerEcho);
        if (becameGlimmer) {
            this.spitCooldown = this.scaledCooldown(this.spitCooldown);
            this.meleeCooldown = this.scaledCooldown(this.meleeCooldown);
            this.diveCooldown = this.scaledCooldown(this.diveCooldown);
        }
        if (glimmerEcho && this.isCocooned()) {
            this.setCocooned(false, null);
        }
        this.xpReward = glimmerEcho ? GLIMMER_XP_REWARD : BASE_XP_REWARD;
        this.bossEvent.setName(this.getDisplayName());
        this.bossEvent.setColor(glimmerEcho ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.YELLOW);
    }

    @Override
    public float glimmerEchoDamageMultiplier() {
        return this.isGlimmerEcho() ? (float) AntarchySettings.glimmerBrutalflyDamageMultiplier() : 1.0F;
    }

    @Override
    protected Component getTypeName() {
        return this.isGlimmerEcho() ? Component.translatable("entity.antarchy.brutalfly.glimmer") : super.getTypeName();
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return this.isGlimmerEcho() ? GLIMMER_LOOT_TABLE : super.getDefaultLootTable();
    }

    private int scaledCooldown(int ticks) {
        if (!this.isGlimmerEcho()) {
            return ticks;
        }
        return Math.max(10, (int) Math.round(ticks * AntarchySettings.glimmerBrutalflyCooldownMultiplier()));
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

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (isCocooned()) {
            return EntityDimensions.scalable(2.0F, 5.0F);
        }
        return super.getDimensions(pose);
    }

    public void setCocooned(boolean cocooned, @Nullable BlockPos anchor) {
        if (cocooned && this.isGlimmerEcho()) {
            return;
        }
        this.entityData.set(IS_COCOONED, cocooned);
        this.bossEvent.setVisible(!cocooned);
        this.anchorPos = anchor;
        this.refreshDimensions();
        if (cocooned) {
            this.cocoonHits = 0;
            this.cocoonHitCooldownTicks = 0;
            this.isHatching = false;
            this.pendingHatchAnim = false;
            this.setAnimationState(ANIM_COCOON);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        boolean bypassesInvulnerability = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (this.postCocoonInvulnerableTicks > 0 && !bypassesInvulnerability) {
            return false;
        }

        if (this.isCocooned() && !this.isHatching && !bypassesInvulnerability) {
            if (this.level().isClientSide || this.cocoonHitCooldownTicks > 0 || !(source.getEntity() instanceof LivingEntity)) {
                return false;
            }
            this.hurtTime = this.hurtDuration;
            this.cocoonHits++;
            this.cocoonHitCooldownTicks = COCOON_HIT_COOLDOWN_TICKS;
            if (this.cocoonHits >= COCOON_HIT_THRESHOLD) {
                this.startHatching();
            } else {
                this.setAnimationState(ANIM_COCOON_HIT);
                this.cocoonAnimTicks = COCOON_HIT_ANIM_TICKS;
            }
            SoundEvent hurtSound = this.getHurtSound(source);
            if (hurtSound != null) {
                this.playSound(hurtSound, this.getSoundVolume(), 1.0F);
            }
            return true;
        }
        if (!bypassesInvulnerability) {
            double fullRange = AntarchySettings.brutalflyDamageRange();
            float rangeMultiplier = BossCombatUtil.damageRangeMultiplier(this, source, fullRange, fullRange * RANGE_FALLOFF_RATIO);
            if (rangeMultiplier < 1.0F) {
                this.playRangeResistFeedback();
            }
            if (rangeMultiplier <= 0.0F) {
                return false;
            }
            amount *= rangeMultiplier;
        }
        return super.hurt(source, amount);
    }

    private void playRangeResistFeedback() {
        if (this.tickCount - this.lastRangeFeedbackTick < RANGE_FEEDBACK_INTERVAL_TICKS) {
            return;
        }
        this.lastRangeFeedbackTick = this.tickCount;
        this.playSound(SoundEvents.SHIELD_BLOCK, 1.5F, 0.6F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.ENCHANTED_HIT), this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(),
                    10, this.getBbWidth() * 0.4D, this.getBbHeight() * 0.3D, this.getBbWidth() * 0.4D, 0.1D);
        }
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float preHealth = this.getHealth();
        super.actuallyHurt(source, amount);
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || this.phaseTransitionTicks > 0 || this.phaseStage >= 2) {
            return;
        }
        float threshold = this.getMaxHealth() * (this.phaseStage == 0 ? PHASE_TWO_THRESHOLD : PHASE_THREE_THRESHOLD);
        if (preHealth > threshold && this.getHealth() <= threshold) {
            this.setHealth(threshold);
            this.beginPhaseTransition();
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (super.isInvulnerableTo(source)) {
            return true;
        }
        return (this.postCocoonInvulnerableTicks > 0 || this.phaseTransitionTicks > 0)
                && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    private int stageForHealth() {
        float healthRatio = this.getHealth() / this.getMaxHealth();
        if (healthRatio < PHASE_THREE_THRESHOLD) {
            return 2;
        }
        return healthRatio < PHASE_TWO_THRESHOLD ? 1 : 0;
    }

    private void beginPhaseTransition() {
        this.phaseStage = Math.min(2, this.phaseStage + 1);
        this.phaseTransitionTicks = PHASE_TRANSITION_TICKS;
        this.cancelActions();
        this.setAnimationState(ANIM_SPIT);
        this.animationTicks = PHASE_TRANSITION_CLIP_TICKS;
    }

    private void cancelActions() {
        this.diveStage = DIVE_NONE;
        this.diveHits.clear();
        this.yawLocked = false;
        this.swipeAnchor = null;
        this.spitWindupTicks = 0;
        this.animationTicks = 0;
        this.getNavigation().stop();
    }

    private void tickPhaseTransition() {
        if (this.phaseTransitionTicks == PHASE_TRANSITION_TICKS) {
            this.playSound(AntarchySoundEvents.BRUTALFLY_IDLE.get(), 3.0F, 0.6F);
            this.playSound(SoundEvents.PHANTOM_SWOOP, 2.5F, 0.7F);
            this.shoveNearbyEntities();
        }
        if (this.phaseTransitionTicks % 5 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.CLOUD), this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    14, this.getBbWidth() * 0.7D, this.getBbHeight() * 0.5D, this.getBbWidth() * 0.7D, 0.06D);
        }
        this.phaseTransitionTicks--;
        if (this.phaseTransitionTicks <= 0) {
            this.spitCooldown = Math.max(this.spitCooldown, 20);
            this.meleeCooldown = Math.max(this.meleeCooldown, 20);
            this.diveCooldown = Math.min(this.diveCooldown, 40);
        }
    }

    private void shoveNearbyEntities() {
        AABB area = this.getBoundingBox().inflate(PHASE_SHOVE_RADIUS);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area, this::isAttackableBySwing)) {
            double dx = entity.getX() - this.getX();
            double dz = entity.getZ() - this.getZ();
            if (dx * dx + dz * dz > PHASE_SHOVE_RADIUS * PHASE_SHOVE_RADIUS || !this.hasLineOfSight(entity)) {
                continue;
            }
            if (dx * dx + dz * dz < 1.0E-4D) {
                dx = this.random.nextDouble() - 0.5D;
                dz = this.random.nextDouble() - 0.5D;
            }
            entity.knockback(PHASE_SHOVE_STRENGTH, -dx, -dz);
            entity.hurtMarked = true;
        }
    }

    private boolean isAttackableBySwing(LivingEntity entity) {
        if (!entity.isAlive() || entity == this || entity instanceof BrutalflyEntity) {
            return false;
        }
        return !(entity instanceof Player player) || (!player.isCreative() && !player.isSpectator());
    }

    private void tickCombat(LivingEntity target) {
        FightPhase phase = this.getFightPhase();
        this.updateTargetSight(target);

        if (this.noSightTicks >= DISENGAGE_AFTER_UNSEEN_TICKS) {
            this.disengage();
            return;
        }

        if (this.getAnimationState() == ANIM_SPIT && this.animationTicks > 0) {
            this.tickStrafeMovement(target, phase, 0.72D);
            return;
        }

        if (this.noSightTicks >= PURSUE_AFTER_UNSEEN_TICKS) {
            this.tickPursuit(target, phase);
            return;
        }

        if (this.getNavigation().isInProgress()) {
            this.getNavigation().stop();
        }
        this.tickStrafeMovement(target, phase, phase.moveSpeed);

        if (this.meleeCooldown <= 0 && this.shouldSwipe(target, phase)) {
            this.startSwipe();
            return;
        }

        if (this.canStartDive(target)) {
            this.startDive();
            return;
        }

        if (this.canStartSpit(target)) {
            this.startSpit();
        }
    }

    private void updateTargetSight(LivingEntity target) {
        if (target.getId() != this.orbitTargetId) {
            this.orbitTargetId = target.getId();
            this.noSightTicks = 0;
            this.orbitPoint = null;
            this.orbitRate = 0.0D;
            this.orbitAngle = Math.atan2(this.getZ() - target.getZ(), this.getX() - target.getX());
        }
        if (this.tickCount % SIGHT_CHECK_INTERVAL_TICKS == 0) {
            this.noSightTicks = this.hasLineOfSight(target) ? 0 : this.noSightTicks + SIGHT_CHECK_INTERVAL_TICKS;
        }
    }

    private void tickPursuit(LivingEntity target, FightPhase phase) {
        if (--this.pathRetryTicks <= 0) {
            boolean started = this.getNavigation().moveTo(target, phase.moveSpeed);
            this.pathRetryTicks = started ? PATH_RETRY_TICKS : PATH_FAILED_RETRY_TICKS;
        }
        if (!this.getNavigation().isInProgress()) {
            this.tickStrafeMovement(target, phase, phase.moveSpeed);
        }
    }

    private void disengage() {
        this.playSound(SoundEvents.PHANTOM_AMBIENT, 2.5F, 0.7F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.CLOUD), this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    12, this.getBbWidth() * 0.5D, this.getBbHeight() * 0.4D, this.getBbWidth() * 0.5D, 0.04D);
        }
        this.setTarget(null);
        this.getNavigation().stop();
        this.patrolTarget = null;
        this.resetPursuitState();
    }

    private void resetPursuitState() {
        this.noSightTicks = 0;
        this.pathRetryTicks = 0;
        this.orbitTargetId = -1;
        this.orbitPoint = null;
        this.orbitStuckTicks = 0;
    }

    private void tickPatrol() {
        Vec3 destination = this.patrolTarget;
        if (destination == null || this.distanceToSqr(destination) < 4.0D || --this.patrolRetargetTicks <= 0) {
            destination = null;
            this.patrolTarget = null;
            if (this.tickCount >= this.nextPatrolSearchTick) {
                destination = this.findPatrolPoint();
                this.patrolTarget = destination;
                this.patrolRetargetTicks = PATROL_RETARGET_TICKS;
                if (destination == null) {
                    this.nextPatrolSearchTick = this.tickCount + PATROL_FAILED_RETRY_TICKS;
                }
            }
        }

        if (destination != null) {
            this.getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 0.8D);
        }
    }

    @Nullable
    private Vec3 findPatrolPoint() {
        double x = this.getX() + this.random.nextDouble() * 14.0D - 7.0D;
        double z = this.getZ() + this.random.nextDouble() * 14.0D - 7.0D;
        BlockPos.MutableBlockPos cursor = BlockPos.containing(x, this.getY(), z).mutable();
        if (!this.level().hasChunkAt(cursor)) {
            return null;
        }
        double groundY = this.getY() - 3.0D;
        for (int i = 0; i < PATROL_GROUND_SCAN; i++) {
            if (!this.level().getBlockState(cursor).getCollisionShape(this.level(), cursor).isEmpty()) {
                groundY = cursor.getY() + 1.0D;
                break;
            }
            cursor.move(0, -1, 0);
        }
        double preferredY = groundY + 3.5D + this.random.nextDouble() * 1.5D;
        if (this.isFlightPointClear(x, preferredY, z)) {
            return new Vec3(x, preferredY, z);
        }
        double lowY = groundY + 1.5D;
        return this.isFlightPointClear(x, lowY, z) ? new Vec3(x, lowY, z) : null;
    }

    private boolean isFlightPointClear(double x, double y, double z) {
        if (!this.level().hasChunkAt(BlockPos.containing(x, y, z))) {
            return false;
        }
        AABB box = this.getBoundingBox().move(x - this.getX(), y - this.getY(), z - this.getZ());
        return this.level().noCollision(this, box);
    }

    private boolean isFlightRouteClear(Vec3 from, Vec3 to, double sampleSpacing) {
        double distance = from.distanceTo(to);
        int samples = Math.max(1, Math.min(14, Mth.ceil(distance / sampleSpacing)));
        for (int i = 1; i <= samples; i++) {
            Vec3 sample = from.lerp(to, i / (double) samples);
            if (!this.isFlightPointClear(sample.x, sample.y, sample.z)) {
                return false;
            }
        }
        return true;
    }

    private void tickStrafeMovement(LivingEntity target, FightPhase phase, double speed) {
        if (target.getId() != this.orbitTargetId) {
            this.updateTargetSight(target);
        }
        if (--this.strafeTicks <= 0) {
            this.startNewStrafeCycle(phase);
        }

        this.currentStrafeRadius = Mth.approach((float) this.currentStrafeRadius, (float) this.wantedStrafeRadius, (float) ORBIT_RADIUS_STEP);
        this.currentFlightHeight = Mth.approach((float) this.currentFlightHeight, (float) this.wantedFlightHeight, (float) ORBIT_HEIGHT_STEP);
        double tangentialSpeed = this.flightTopSpeed(speed) * ORBIT_TANGENTIAL_RATIO;
        double wantedRate = this.orbitDirection * Math.min(ORBIT_MAX_RATE, tangentialSpeed / Math.max(1.0D, this.currentStrafeRadius));
        this.orbitRate = Mth.approach((float) this.orbitRate, (float) wantedRate, (float) ORBIT_RATE_STEP);
        this.orbitAngle += this.orbitRate;

        if ((this.horizontalCollision || (this.verticalCollision && !this.onGround())) && this.getDeltaMovement().lengthSqr() < 0.04D) {
            this.orbitStuckTicks += 2;
            if (this.orbitStuckTicks >= ORBIT_STUCK_LIMIT) {
                this.orbitDirection *= -1.0F;
                this.orbitStuckTicks = 0;
                this.orbitPoint = null;
            }
        } else if (this.orbitStuckTicks > 0) {
            this.orbitStuckTicks--;
        }

        if (this.orbitPoint == null || this.tickCount % ORBIT_REFRESH_INTERVAL_TICKS == 0) {
            this.orbitPoint = this.findOrbitPoint(target);
        }
        Vec3 point = this.orbitPoint;
        if (point != null) {
            this.getMoveControl().setWantedPosition(point.x, point.y, point.z, speed);
        } else {
            this.getMoveControl().setWantedPosition(target.getX(), target.getY() + this.currentFlightHeight, target.getZ(), speed);
        }
    }

    @Nullable
    private Vec3 findOrbitPoint(LivingEntity target) {
        Vec3 from = this.position();
        Vec3 eye = target.getEyePosition();
        Vec3 visibleless = null;
        double bob = Mth.sin((float) (this.orbitAngle * 1.5D)) * ORBIT_BOB;
        for (double radiusScale : ORBIT_RADIUS_SCALES) {
            double radius = this.currentStrafeRadius * radiusScale;
            double x = target.getX() + Math.cos(this.orbitAngle) * radius;
            double z = target.getZ() + Math.sin(this.orbitAngle) * radius;
            for (double heightOffset : ORBIT_HEIGHT_OFFSETS) {
                double y = target.getY() + Math.max(ORBIT_MIN_HEIGHT, this.currentFlightHeight + heightOffset + bob);
                if (!this.isFlightPointClear(x, y, z)) {
                    continue;
                }
                Vec3 candidate = new Vec3(x, y, z);
                if (!this.isFlightRouteClear(from, candidate, 4.0D)) {
                    continue;
                }
                if (this.hasClearSightFrom(candidate, eye)) {
                    return candidate;
                }
                if (visibleless == null) {
                    visibleless = candidate;
                }
            }
        }
        return visibleless;
    }

    private boolean hasClearSightFrom(Vec3 bodyPosition, Vec3 eye) {
        Vec3 viewpoint = bodyPosition.add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        return this.level().clip(new ClipContext(viewpoint, eye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private void startNewStrafeCycle(FightPhase phase) {
        this.strafeTicks = phase.minStrafeTicks + this.random.nextInt(phase.maxStrafeTicks - phase.minStrafeTicks + 1);
        this.wantedStrafeRadius = phase.minRadius + this.random.nextDouble() * (phase.maxRadius - phase.minRadius);
        this.wantedFlightHeight = phase.minHeight + this.random.nextDouble() * (phase.maxHeight - phase.minHeight);
        if (this.random.nextFloat() < ORBIT_FLIP_CHANCE) {
            this.orbitDirection *= -1.0F;
        }
    }

    private double flightTopSpeed(double speedModifier) {
        return this.getAttributeValue(Attributes.FLYING_SPEED) * speedModifier * FLY_SPEED_SCALE;
    }

    private boolean canStartSpit(LivingEntity target) {
        if (this.spitCooldown > 0 || this.noSightTicks > 0) {
            return false;
        }
        double distanceSqr = this.distanceToSqr(target);
        if (distanceSqr < 16.0D || distanceSqr > 400.0D || !this.hasLineOfSight(target)) {
            return false;
        }
        BlockPos muzzle = BlockPos.containing(this.getSpitOrigin());
        return this.level().hasChunkAt(muzzle) && this.level().getBlockState(muzzle).getCollisionShape(this.level(), muzzle).isEmpty();
    }

    private boolean canStartDive(LivingEntity target) {
        if (this.diveCooldown > 0 || this.noSightTicks > 0) {
            return false;
        }
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double horizontalSqr = dx * dx + dz * dz;
        if (horizontalSqr < DIVE_MIN_RANGE_SQR || horizontalSqr > DIVE_MAX_RANGE_SQR
                || Math.abs(target.getY() - this.getY()) > DIVE_MAX_VERTICAL) {
            return false;
        }
        Vec3 strike = target.position().add(0.0D, DIVE_STRIKE_HEIGHT, 0.0D);
        if (!this.hasLineOfSight(target) || !this.isFlightRouteClear(this.position(), strike, 1.5D)) {
            this.diveCooldown = DIVE_RETRY_TICKS;
            return false;
        }
        return true;
    }

    private void startDive() {
        this.diveStage = DIVE_TELEGRAPH;
        this.diveStageTicks = DIVE_TELEGRAPH_TICKS;
        this.diveCooldown = this.scaledCooldown(this.getFightPhase().nextDiveCooldown(this.random)) + DIVE_TELEGRAPH_TICKS + DIVE_MAX_TICKS;
        this.diveHits.clear();
        this.diveOvershooting = false;
        this.getNavigation().stop();
        this.playSound(SoundEvents.PHANTOM_SWOOP, 3.0F, 0.8F);
    }

    private void tickDive(@Nullable LivingEntity target) {
        switch (this.diveStage) {
            case DIVE_TELEGRAPH -> this.tickDiveTelegraph(target);
            case DIVE_ACTIVE -> this.tickDiveActive();
            default -> this.tickDiveRecovery();
        }
    }

    private void tickDiveTelegraph(@Nullable LivingEntity target) {
        this.diveStageTicks--;
        if (this.diveStageTicks >= DIVE_LOCK_REMAINING && target == null) {
            this.cancelDive();
            return;
        }
        if (this.diveStageTicks == DIVE_LOCK_REMAINING && target != null) {
            Vec3 strike = target.position().add(0.0D, DIVE_STRIKE_HEIGHT, 0.0D);
            if (!this.isFlightRouteClear(this.position(), strike, 1.5D)) {
                this.cancelDive();
                return;
            }
            this.diveStrikePoint = strike;
            this.yawLocked = true;
            this.lockedYaw = this.yawTowards(strike);
            Vec3 horizontal = new Vec3(strike.x - this.getX(), 0.0D, strike.z - this.getZ());
            Vec3 heading = horizontal.lengthSqr() < 1.0E-4D ? this.getViewVector(0.0F) : horizontal.normalize();
            this.diveOvershootPoint = strike.add(heading.x * DIVE_OVERSHOOT, DIVE_OVERSHOOT_RISE, heading.z * DIVE_OVERSHOOT);
            this.playSound(SoundEvents.PHANTOM_BITE, 2.0F, 0.7F);
        }
        if (this.diveStageTicks % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.CLOUD), this.getX(), this.getY() + this.getBbHeight() * 0.3D, this.getZ(),
                    6, this.getBbWidth() * 0.5D, 0.2D, this.getBbWidth() * 0.5D, 0.02D);
        }
        if (this.diveStageTicks <= 0) {
            this.diveStage = DIVE_ACTIVE;
            this.diveActiveTicks = 0;
            this.diveLastPosition = this.position();
            double distance = this.position().distanceTo(this.diveStrikePoint);
            this.diveSpeed = Mth.clamp(distance / DIVE_STRIKE_TICKS, DIVE_MIN_SPEED, DIVE_MAX_SPEED);
            this.setAnimationState(ANIM_SWIPE);
            this.animationTicks = SWIPE_TICKS;
            return;
        }
        if (this.isFlightPointClear(this.getX(), this.getY() + 1.0D, this.getZ())) {
            this.getMoveControl().setWantedPosition(this.getX(), this.getY() + 1.0D, this.getZ(), 0.3D);
        }
    }

    private void cancelDive() {
        this.diveStage = DIVE_NONE;
        this.yawLocked = false;
        this.diveCooldown = Math.max(this.diveCooldown - DIVE_MAX_TICKS, DIVE_CANCEL_COOLDOWN_TICKS);
    }

    private void tickDiveActive() {
        this.diveActiveTicks++;
        Vec3 position = this.position();
        double moved = position.distanceTo(this.diveLastPosition);
        if (this.diveActiveTicks > 2 && moved < this.diveSpeed * DIVE_BLOCKED_MOVE_RATIO) {
            this.finishDive(true);
            return;
        }
        Vec3 sweep = position.subtract(this.diveLastPosition);
        this.diveLastPosition = position;

        Vec3 destination = this.diveOvershooting ? this.diveOvershootPoint : this.diveStrikePoint;
        Vec3 toDestination = destination.subtract(position);
        double remaining = toDestination.length();
        if (!this.diveOvershooting && remaining <= this.diveSpeed * 1.2D) {
            this.diveOvershooting = true;
            destination = this.diveOvershootPoint;
            toDestination = destination.subtract(position);
            remaining = toDestination.length();
        }
        if (this.diveActiveTicks >= DIVE_FIRST_CONTACT_TICK) {
            this.performDiveContact(sweep);
        }
        if ((this.diveOvershooting && remaining <= this.diveSpeed) || this.diveActiveTicks >= DIVE_MAX_TICKS) {
            this.finishDive(false);
            return;
        }
        this.setDeltaMovement(toDestination.normalize().scale(this.diveSpeed));
    }

    private void performDiveContact(Vec3 sweep) {
        AABB swept = this.getBoundingBox().expandTowards(sweep.reverse()).inflate(0.8D, 0.4D, 0.8D);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * DIVE_DAMAGE_MULTIPLIER;
        boolean finalPhase = this.getFightPhase() == FightPhase.PHASE_THREE;
        Vec3 heading = this.getDeltaMovement();
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, swept, this::isAttackableBySwing)) {
            if (!this.diveHits.add(target.getId())) {
                continue;
            }
            if (!target.hurt(this.damageSources().mobAttack(this), damage)) {
                continue;
            }
            if (finalPhase) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
            }
            target.knockback(DIVE_KNOCKBACK, -heading.x, -heading.z);
            target.setDeltaMovement(target.getDeltaMovement().add(GravityRotationUtil.vecPlayerToWorld(0.0D, 0.3D, 0.0D, GravityApi.getGravityDirection(this))));
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6F, 0.7F);
        }
    }

    private void finishDive(boolean hitTerrain) {
        this.diveStage = DIVE_RECOVERY;
        this.diveStageTicks = hitTerrain ? DIVE_STAGGER_TICKS : DIVE_RECOVERY_TICKS;
        this.diveHits.clear();
        Vec3 velocity = this.getDeltaMovement();
        this.setDeltaMovement(velocity.scale(hitTerrain ? 0.1D : 0.45D));
        if (hitTerrain) {
            this.playSound(SoundEvents.PHANTOM_HURT, 2.5F, 0.6F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                        16, this.getBbWidth() * 0.6D, this.getBbHeight() * 0.4D, this.getBbWidth() * 0.6D, 0.05D);
            }
        }
    }

    private void tickDiveRecovery() {
        if (--this.diveStageTicks <= 0) {
            this.diveStage = DIVE_NONE;
            this.yawLocked = false;
            this.spitCooldown = Math.max(this.spitCooldown, 10);
            LivingEntity target = this.getTarget();
            if (target != null) {
                this.orbitAngle = Math.atan2(this.getZ() - target.getZ(), this.getX() - target.getX());
                this.orbitRate = 0.0D;
                this.orbitPoint = null;
            }
            return;
        }
        if (this.diveStageTicks <= DIVE_RECOVERY_TICKS && this.isFlightPointClear(this.getX(), this.getY() + 0.6D, this.getZ())) {
            this.getMoveControl().setWantedPosition(this.getX(), this.getY() + 0.6D, this.getZ(), 0.3D);
        }
    }

    private void startSpit() {
        this.setAnimationState(ANIM_SPIT);
        this.animationTicks = 20;
        this.spitWindupTicks = SPIT_WINDUP_TICKS;
        this.spitCooldown = this.scaledCooldown(this.getFightPhase().nextSpitCooldown(this.random));
        this.gameEvent(GameEvent.PROJECTILE_SHOOT);
    }

    private void fireCurrentSpitPattern() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        Vec3 origin = this.getSpitOrigin();
        this.playSound(AntarchySoundEvents.BRUTALFLY_SPIT.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
        switch (this.getFightPhase()) {
            case PHASE_THREE -> {
                if (this.isGlimmerEcho()) {
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, -21.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.STICKY, -7.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.STICKY, 7.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, 21.0F);
                } else {
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, -14.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.STICKY, 0.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, 14.0F);
                }
            }
            case PHASE_TWO -> {
                if (this.isGlimmerEcho()) {
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, -12.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.STICKY, 0.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, 12.0F);
                } else {
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.POISON, -8.0F);
                    this.launchOrb(serverLevel, origin, target, BrutalflyOrbEntity.OrbVariant.STICKY, 8.0F);
                }
            }
            default -> {
                if (this.isGlimmerEcho()) {
                    boolean poisonFirst = this.random.nextBoolean();
                    this.launchOrb(serverLevel, origin, target, poisonFirst ? BrutalflyOrbEntity.OrbVariant.POISON : BrutalflyOrbEntity.OrbVariant.STICKY, -6.0F);
                    this.launchOrb(serverLevel, origin, target, poisonFirst ? BrutalflyOrbEntity.OrbVariant.STICKY : BrutalflyOrbEntity.OrbVariant.POISON, 6.0F);
                } else {
                    BrutalflyOrbEntity.OrbVariant variant = this.random.nextBoolean()
                            ? BrutalflyOrbEntity.OrbVariant.POISON
                            : BrutalflyOrbEntity.OrbVariant.STICKY;
                    this.launchOrb(serverLevel, origin, target, variant, 0.0F);
                }
            }
        }
    }

    private void launchOrb(ServerLevel serverLevel, Vec3 origin, LivingEntity target, BrutalflyOrbEntity.OrbVariant variant, float yawOffsetDegrees) {
        BrutalflyOrbEntity orb = AntarchyObjects.BRUTALFLY_ORB.get().create(serverLevel);
        if (orb == null) {
            return;
        }

        orb.setOwner(this);
        orb.setOrbVariant(variant);
        orb.moveTo(origin.x, origin.y, origin.z, this.getYRot(), this.getXRot());

        Vec3 shot = this.createShotVector(origin, target, yawOffsetDegrees);
        orb.shoot(shot.x, shot.y, shot.z, 0.95F, 0.0F);
        GlimmerEchoes.markSummon(this, orb);
        serverLevel.addFreshEntity(orb);
    }

    private Vec3 createShotVector(Vec3 origin, LivingEntity target, float yawOffsetDegrees) {
        Vec3 leadTarget = target.position().add(target.getDeltaMovement().scale(6.0D));
        Vec3 direction = leadTarget.subtract(origin).normalize();
        if (yawOffsetDegrees == 0.0F) {
            return direction;
        }

        double radians = Math.toRadians(yawOffsetDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
                direction.x * cos - direction.z * sin,
                direction.y,
                direction.x * sin + direction.z * cos
        ).normalize();
    }

    private Vec3 getSpitOrigin() {
        return this.position().add(0.0D, this.getBbHeight() * 0.58D, 0.0D).add(this.getViewVector(0.0F).scale(2.3D));
    }

    private boolean shouldSwipe(LivingEntity target, FightPhase phase) {
        Vec3 localDelta = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(this.position()), GravityApi.getGravityDirection(this));
        double horizontalDistanceSqr = new Vec3(localDelta.x, 0.0D, localDelta.z).lengthSqr();
        boolean beneathBoss = horizontalDistanceSqr <= SWIPE_BENEATH_RANGE_SQR
                && localDelta.y >= -SWIPE_BENEATH_BELOW
                && localDelta.y <= SWIPE_BENEATH_ABOVE;
        if (!beneathBoss && this.distanceToSqr(target) > phase.swipeRangeSqr) {
            return false;
        }
        return Math.abs(Mth.degreesDifference(this.getYRot(), this.yawTowards(target.position()))) <= SWIPE_MAX_START_YAW_ERROR
                && this.hasLineOfSight(target);
    }

    private float yawTowards(Vec3 point) {
        return (float) (Mth.atan2(point.z - this.getZ(), point.x - this.getX()) * (180.0F / Math.PI)) - 90.0F;
    }

    private void startSwipe() {
        this.setAnimationState(ANIM_SWIPE);
        this.animationTicks = SWIPE_TICKS;
        this.meleeCooldown = SWIPE_TICKS + this.scaledCooldown(this.getFightPhase().nextMeleeCooldown(this.random));
        this.swipeAnchor = null;
        this.getNavigation().stop();
    }

    private void tickSwipe(@Nullable LivingEntity target) {
        int elapsed = SWIPE_TICKS - this.animationTicks;
        if (elapsed == SWIPE_LOCK_ELAPSED) {
            this.lockedYaw = target != null ? this.yawTowards(target.position()) : this.getYRot();
            this.yawLocked = true;
            Vec3 heading = Vec3.directionFromRotation(0.0F, this.lockedYaw);
            double lift = target != null ? Mth.clamp(target.getY() - this.getY(), -1.5D, 1.5D) : 0.0D;
            Vec3 anchor = this.position().add(heading.x * SWIPE_LUNGE_DISTANCE, lift, heading.z * SWIPE_LUNGE_DISTANCE);
            this.swipeAnchor = this.isFlightPointClear(anchor.x, anchor.y, anchor.z) ? anchor : null;
        }

        Vec3 anchor = this.swipeAnchor;
        if (anchor != null && elapsed >= SWIPE_LOCK_ELAPSED && elapsed < SWIPE_LUNGE_END_ELAPSED) {
            this.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, 1.45D);
        }

        if (elapsed == SWIPE_HIT_ELAPSED) {
            this.performSwipeHit();
        }
    }

    private void performSwipeHit() {
        float swipeDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean finalPhase = this.getFightPhase() == FightPhase.PHASE_THREE;
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.yawLocked ? this.lockedYaw : this.getYRot());
        AABB attackBox = this.getBoundingBox().inflate(SWIPE_REACH, SWIPE_REACH_BELOW, SWIPE_REACH);
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6F, 0.8F);
        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, attackBox, this::isAttackableBySwing)) {
            double offsetX = target.getX() - this.getX();
            double offsetZ = target.getZ() - this.getZ();
            double horizontal = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
            if (horizontal - target.getBbWidth() * 0.5D > SWIPE_REACH) {
                continue;
            }
            if (horizontal > 1.0E-3D && (offsetX * forward.x + offsetZ * forward.z) / horizontal < SWIPE_ARC_COS) {
                continue;
            }
            if (target.getBoundingBox().maxY < this.getY() - SWIPE_REACH_BELOW
                    || target.getBoundingBox().minY > this.getBoundingBox().maxY + SWIPE_REACH_ABOVE
                    || !this.hasLineOfSight(target)) {
                continue;
            }

            if (!target.hurt(this.damageSources().mobAttack(this), swipeDamage)) {
                continue;
            }
            if (finalPhase) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
            }

            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            if (dx * dx + dz * dz < 1.0E-4D) {
                dx = this.random.nextDouble() - 0.5D;
                dz = this.random.nextDouble() - 0.5D;
            }
            target.knockback(SWIPE_KNOCKBACK, -dx, -dz);
            target.setDeltaMovement(target.getDeltaMovement().add(GravityRotationUtil.vecPlayerToWorld(0.0D, 0.3D, 0.0D, GravityApi.getGravityDirection(this))));
        }
    }

    private FightPhase getFightPhase() {
        return switch (this.phaseStage) {
            case 0 -> FightPhase.PHASE_ONE;
            case 1 -> FightPhase.PHASE_TWO;
            default -> FightPhase.PHASE_THREE;
        };
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private void setAnimationState(int animationState) {
        int previous = this.entityData.get(ANIMATION_STATE);
        if (previous != animationState) {
            this.entityData.set(ANIMATION_STATE, animationState);
            String trigger = switch (animationState) {
                case ANIM_DEATH -> "death";
                case ANIM_COCOON -> "Coocon";
                case ANIM_COCOON_HIT -> "Coocon_hit";
                case ANIM_COCOON_IDLE -> "Coocon_idle";
                case ANIM_COCOON_HATCH -> "Coocon_hatch";
                default -> null;
            };
            if (trigger != null) {
                this.triggerAnim("main_controller", trigger);
            }
            if (previous == ANIM_COCOON_IDLE && animationState != ANIM_COCOON_IDLE) {
                this.stopTriggeredAnimation("main_controller", "Coocon_idle");
            }
            if (previous == ANIM_COCOON_HATCH && animationState != ANIM_COCOON_HATCH) {
                this.stopTriggeredAnimation("main_controller", "Coocon_hatch");
            }
        }
    }

    private void updateBaseAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }

        if (this.animationTicks > 0 && (this.getAnimationState() == ANIM_SPIT || this.getAnimationState() == ANIM_SWIPE)) {
            return;
        }

        if (this.onGround()) {
            this.setAnimationState(ANIM_IDLE);
        } else {
            this.setAnimationState(ANIM_FLY);
        }
    }

    private void updateFlightRotation(@Nullable LivingEntity target) {
        Vec3 movement = this.getDeltaMovement();
        float yaw = this.getYRot();
        if (this.yawLocked) {
            yaw = Mth.approachDegrees(yaw, this.lockedYaw, YAW_COMMIT_STEP);
        } else if (target != null) {
            yaw = Mth.approachDegrees(yaw, this.yawTowards(target.position()), YAW_COMBAT_STEP);
        } else if (movement.horizontalDistanceSqr() > 1.0E-3D) {
            float travelYaw = (float) (Mth.atan2(movement.z, movement.x) * (180.0F / Math.PI)) - 90.0F;
            yaw = Mth.approachDegrees(yaw, travelYaw, YAW_PATROL_STEP);
        }
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;

        float travelPitch = movement.lengthSqr() < 4.0E-4D
                ? 0.0F
                : (float) (-(Mth.atan2(movement.y, movement.horizontalDistance()) * (180.0F / Math.PI)));
        this.setXRot(Mth.approachDegrees(this.getXRot(), Mth.clamp(travelPitch, -MAX_PITCH, MAX_PITCH), PITCH_STEP));
    }

    @Override
    protected void tickDeath() {
        this.deathAnimationTicks++;
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level().isClientSide) {
            return;
        }
        this.setAnimationState(ANIM_DEATH);

        if (!this.spawnedDeathButterflies && this.deathAnimationTicks >= DEATH_ANIM_TICKS - 10) {
            this.spawnDeathButterflies();
            this.spawnedDeathButterflies = true;
        }

        if (this.deathAnimationTicks >= DEATH_ANIM_TICKS) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
            this.gameEvent(GameEvent.ENTITY_DIE);
        }
    }

    private void spawnDeathButterflies() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int count = 5 + this.random.nextInt(6);
        for (int i = 0; i < count; i++) {
            var butterfly = AntarchyObjects.BUTTERFLY.get().create(serverLevel);
            if (butterfly == null) {
                continue;
            }

            butterfly.moveTo(
                    this.getX() + (this.random.nextDouble() - 0.5D) * 2.5D,
                    this.getY() + 1.0D + this.random.nextDouble() * 1.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 2.5D,
                    this.random.nextFloat() * 360.0F,
                    0.0F
            );
            butterfly.setDeltaMovement(
                    (this.random.nextDouble() - 0.5D) * 0.35D,
                    0.12D + this.random.nextDouble() * 0.15D,
                    (this.random.nextDouble() - 0.5D) * 0.35D
            );
            GlimmerEchoes.markSummon(this, butterfly);
            serverLevel.addFreshEntity(butterfly);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        this.bossEvent.addPlayer(serverPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer serverPlayer) {
        super.stopSeenByPlayer(serverPlayer);
        this.bossEvent.removePlayer(serverPlayer);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(GlimmerEchoes.SAVE_KEY, this.isGlimmerEcho());
        tag.putBoolean("IsCocooned", this.isCocooned());
        tag.putInt("CocoonHits", this.cocoonHits);
        tag.putInt("CocoonHitCooldown", this.cocoonHitCooldownTicks);
        tag.putBoolean("IsHatching", this.isHatching);
        tag.putBoolean("PendingHatchAnim", this.pendingHatchAnim);
        tag.putInt("CocoonAnimTicks", this.cocoonAnimTicks);
        tag.putInt("PostCocoonInvulnerableTicks", this.postCocoonInvulnerableTicks);
        tag.putInt("PhaseStage", this.phaseStage);
        tag.putInt("PhaseTransitionTicks", this.phaseTransitionTicks);
        if (this.anchorPos != null) {
            tag.putInt("AnchorX", this.anchorPos.getX());
            tag.putInt("AnchorY", this.anchorPos.getY());
            tag.putInt("AnchorZ", this.anchorPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setGlimmerEcho(tag.getBoolean(GlimmerEchoes.SAVE_KEY));
        this.phaseStage = tag.contains("PhaseStage") ? Mth.clamp(tag.getInt("PhaseStage"), 0, 2) : this.stageForHealth();
        this.phaseTransitionTicks = Mth.clamp(tag.getInt("PhaseTransitionTicks"), 0, PHASE_TRANSITION_TICKS);
        this.postCocoonInvulnerableTicks = Math.max(0, tag.getInt("PostCocoonInvulnerableTicks"));
        if (tag.getBoolean("IsCocooned") && !this.isGlimmerEcho()) {
            BlockPos anchor = null;
            if (tag.contains("AnchorX")) {
                anchor = new BlockPos(tag.getInt("AnchorX"), tag.getInt("AnchorY"), tag.getInt("AnchorZ"));
            }
            this.setCocooned(true, anchor);
            this.cocoonHits = tag.getInt("CocoonHits");
            this.cocoonHitCooldownTicks = tag.getInt("CocoonHitCooldown");
            if (tag.getBoolean("IsHatching")) {
                this.isHatching = true;
                this.pendingHatchAnim = tag.getBoolean("PendingHatchAnim");
                this.cocoonAnimTicks = Math.max(0, tag.getInt("CocoonAnimTicks"));
                this.setAnimationState(this.pendingHatchAnim ? ANIM_COCOON_HIT : ANIM_COCOON_HATCH);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.BRUTALFLY_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.BRUTALFLY_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.4F;
    }

    private enum FightPhase {
        PHASE_ONE(0.95D, 24, 34, 7.0D, 9.0D, 3.5D, 4.6D, 22.0D),
        PHASE_TWO(1.12D, 18, 28, 5.3D, 7.0D, 2.4D, 3.2D, 19.0D),
        PHASE_THREE(1.28D, 14, 22, 3.8D, 5.4D, 1.3D, 2.1D, 26.0D);

        final double moveSpeed;
        final int minStrafeTicks;
        final int maxStrafeTicks;
        final double minRadius;
        final double maxRadius;
        final double minHeight;
        final double maxHeight;
        final double swipeRangeSqr;

        FightPhase(double moveSpeed, int minStrafeTicks, int maxStrafeTicks, double minRadius, double maxRadius, double minHeight, double maxHeight, double swipeRange) {
            this.moveSpeed = moveSpeed;
            this.minStrafeTicks = minStrafeTicks;
            this.maxStrafeTicks = maxStrafeTicks;
            this.minRadius = minRadius;
            this.maxRadius = maxRadius;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
            this.swipeRangeSqr = swipeRange;
        }

        int nextSpitCooldown(RandomSource random) {
            return switch (this) {
                case PHASE_ONE -> 70 + random.nextInt(26);
                case PHASE_TWO -> 42 + random.nextInt(19);
                case PHASE_THREE -> 26 + random.nextInt(15);
            };
        }

        int nextMeleeCooldown(RandomSource random) {
            return switch (this) {
                case PHASE_ONE -> 42 + random.nextInt(10);
                case PHASE_TWO -> 30 + random.nextInt(8);
                case PHASE_THREE -> 20 + random.nextInt(7);
            };
        }

        int nextDiveCooldown(RandomSource random) {
            return switch (this) {
                case PHASE_ONE -> 180 + random.nextInt(61);
                case PHASE_TWO -> 130 + random.nextInt(41);
                case PHASE_THREE -> 90 + random.nextInt(31);
            };
        }
    }

    private static final class BrutalflyMoveControl extends MoveControl {
        private final BrutalflyEntity brutalfly;

        private BrutalflyMoveControl(BrutalflyEntity brutalfly) {
            super(brutalfly);
            this.brutalfly = brutalfly;
        }

        @Override
        public void tick() {
            this.brutalfly.setYya(0.0F);
            this.brutalfly.setZza(0.0F);
            if (this.brutalfly.isCocooned() || this.brutalfly.diveStage == DIVE_ACTIVE) {
                this.operation = Operation.WAIT;
                return;
            }

            Vec3 velocity = this.brutalfly.getDeltaMovement();
            Vec3 wanted = Vec3.ZERO;
            if (this.operation == Operation.MOVE_TO) {
                this.operation = Operation.WAIT;
                Vec3 offset = new Vec3(this.wantedX, this.wantedY, this.wantedZ).subtract(this.brutalfly.position());
                double distance = offset.length();
                if (distance > 0.1D) {
                    double speed = Math.min(this.brutalfly.flightTopSpeed(this.speedModifier), distance * FLY_ARRIVAL_RATE);
                    wanted = offset.scale(speed / distance);
                }
            }

            Vec3 change = wanted.subtract(velocity);
            double changeLength = change.length();
            if (changeLength > FLY_ACCELERATION) {
                change = change.scale(FLY_ACCELERATION / changeLength);
            }
            this.brutalfly.setDeltaMovement(velocity.add(change));
        }
    }
}
