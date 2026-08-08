package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.entity.ScorpionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EmperorScorpionEntity extends Monster implements GeoEntity {
    private static final byte CLAW_ATTACK_EVENT = 4;
    private static final byte STING_ATTACK_EVENT = 5;
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(EmperorScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HARDEN_PHASE =
            SynchedEntityData.defineId(EmperorScorpionEntity.class, EntityDataSerializers.INT);

    private static final int DEATH_ANIM_TICKS = 40;
    private static final int ANIM_IDLE = 0;
    private static final int ANIM_WALK = 1;
    private static final int ANIM_CLAW_ATTACK = 2;
    private static final int ANIM_STING_ATTACK = 3;
    private static final int ANIM_DEATH = 4;
    private static final int ANIM_HARDEN_START = 5;
    private static final int ANIM_HARDEN_ACTIVE = 6;
    private static final int ANIM_HARDEN_END = 7;
    private static final double CLAW_LUNGE_SPEED = 0.12D;
    private static final double CLAW_LUNGE_LIFT = 0.02D;
    private static final double STING_LUNGE_SPEED = 0.08D;
    private static final double STING_LUNGE_LIFT = 0.01D;
    private static final double CLAW_REACH = 5.0D;
    private static final double STING_REACH = 6.0D;
    private static final double CLAW_HIT_REACH = 6.25D;
    private static final double STING_HIT_REACH = 7.5D;
    private static final double DIRECT_APPROACH_RANGE = 12.0D;
    private static final float DIRECT_APPROACH_TURN_RATE = 10.0F;
    private static final float DIRECT_APPROACH_ALIGN_THRESHOLD = 45.0F;
    private static final int HARDEN_START_TICKS = 11;
    private static final int HARDEN_END_TICKS = 20;
    private static final int HARDEN_HEAL_INTERVAL_TICKS = 20;
    private static final float HARDEN_HEAL_AMOUNT = 2.0F;
    private static final int HARDEN_MIN_DURATION_TICKS = 100;
    private static final int HARDEN_MAX_DURATION_TICKS = 200;
    private static final double ENCOUNTER_HOME_RADIUS = 72.0D;
    private static final double ENCOUNTER_LEASH_RADIUS = 96.0D;
    private static final double HOME_REACHED_RADIUS = 9.0D;
    private static final double FORCED_RETURN_RADIUS = 144.0D;
    private static final int RESET_DELAY_TICKS = 240;
    private static final double HOME_RETURN_SPEED = 1.0D;
    private static final double NEARBY_SCORPION_CHECK_RADIUS = 32.0D;

    private static final ResourceKey<Level> THORAXIS_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis"));

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation CLAW_ATTACK_ANIM = RawAnimation.begin().thenPlay("snap");
    private static final RawAnimation STING_ATTACK_ANIM = RawAnimation.begin().thenPlay("sting");
    private static final RawAnimation HARDEN_START_ANIM = RawAnimation.begin().thenPlay("harden");
    private static final RawAnimation HARDEN_ACTIVE_ANIM = RawAnimation.begin().thenLoop("harden_state");
    private static final RawAnimation HARDEN_END_ANIM = RawAnimation.begin().thenPlay("end_of_harden_state");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final ServerBossEvent bossEvent =
            new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private int attackAnimTicks;
    private int clawCooldownTicks;
    private int stingCooldownTicks;
    private int hardenCooldownTicks = AntarchySettings.emperorScorpionHardenCooldownTicks();
    private int hardenCastTicks;
    private int hardenCastSpawnTicks;
    private int hardenStateTicks;
    private int hardenDurationTicks;
    private int hardenHealTicks;
    private int hardenSummonedScorpions;
    private int disengageTicks;
    private boolean attackDamageApplied;
    private AttackType currentAttack = AttackType.NONE;
    private HardenPhase hardenPhase = HardenPhase.NONE;
    private AttackType previousAttack = AttackType.NONE;
    @Nullable private BlockPos encounterHome;
    @Nullable private LivingEntity attackTarget;
    private final Set<UUID> summonedScorpionIds = new HashSet<>();
    private boolean hasTakenDamage;

    public EmperorScorpionEntity(EntityType<? extends EmperorScorpionEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = AntarchySettings.emperorScorpionXpReward();
        this.bossEvent.setDarkenScreen(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.emperorScorpionHealth())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.emperorScorpionMovementSpeed())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.emperorScorpionAttackDamage())
                .add(Attributes.ARMOR, AntarchySettings.emperorScorpionArmor())
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, AntarchySettings.emperorScorpionKnockbackResistance())
                .add(Attributes.FOLLOW_RANGE, AntarchySettings.emperorScorpionFollowRange());
    }

    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType spawnReason, @Nullable net.minecraft.world.entity.SpawnGroupData spawnData) {
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, AntarchySettings.emperorScorpionHealth());
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(HARDEN_PHASE, HardenPhase.NONE.ordinal());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EmperorScorpionCombatGoal());
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isHardenedStateActive()) {
            return false;
        }
        float preHealth = this.getHealth();
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            this.hasTakenDamage = true;
            BossCombatUtil.clampHalfHealthCrossing(this, preHealth);
        }
        return hurt;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (this.isHardenedStateActive() || super.isInvulnerableTo(source)) {
            return true;
        }
        return BossCombatUtil.isOutOfDamageRange(this, AntarchySettings.emperorScorpionDamageRange());
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.hasTakenDamage || this.tickCount < AntarchySettings.emperorScorpionMinDespawnTicks()) {
            return false;
        }
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public void lavaHurt() {
    }

    public static boolean canSpawn(EntityType<EmperorScorpionEntity> entityType, ServerLevelAccessor level,
                                   MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG
                || spawnReason == MobSpawnType.SPAWNER
                || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        if (!level.getLevel().dimension().equals(THORAXIS_KEY)) {
            return false;
        }
        return level.getDifficulty() != Difficulty.PEACEFUL
                && Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    public float maxUpStep() {
        return 1.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            this.tickEncounterState();
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setVisible(this.shouldShowBossBar());

        if (this.clawCooldownTicks > 0) {
            this.clawCooldownTicks--;
        }
        if (this.stingCooldownTicks > 0) {
            this.stingCooldownTicks--;
        }
        if (this.hardenCooldownTicks > 0) {
            this.hardenCooldownTicks--;
        }
        if (this.attackAnimTicks > 0) {
            if (!this.level().isClientSide) {
                this.tickActiveAttack();
            }
            this.attackAnimTicks--;
            if (this.attackAnimTicks <= 0) {
                this.resetAttackState();
            }
        }
        if (!this.level().isClientSide) {
            this.tickHardenState();
        }

        this.updateAnimationState();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (!hurt) {
            return false;
        }
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 0.9F, 0.95F + this.random.nextFloat() * 0.08F);
        return true;
    }

    private boolean doStingAttack(LivingEntity target) {
        boolean hurt;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            hurt = target.hurt(
                    AntarchyDamageSources.emperorScorpionSting(serverLevel, this),
                    (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE)
            );
        } else {
            hurt = target.hurt(this.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        }

        if (!hurt) {
            return false;
        }

        int poisonDuration = this.level().getDifficulty() == Difficulty.HARD
                ? AntarchySettings.emperorScorpionPoisonTicks() * 2
                : AntarchySettings.emperorScorpionPoisonTicks();
        target.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, AntarchySettings.emperorScorpionWeaknessTicks(), 0));
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 1.0F, 0.88F + this.random.nextFloat() * 0.08F);
        return true;
    }

    private void beginAttack(LivingEntity target, AttackType attackType) {
        if (this.isHardenSequenceActive()) {
            return;
        }
        this.attackTarget = target;
        this.currentAttack = attackType;
        this.previousAttack = attackType;
        this.attackAnimTicks = attackType.animTicks();
        this.attackDamageApplied = false;
        this.getNavigation().stop();
        if (attackType == AttackType.CLAW) {
            this.clawCooldownTicks = AntarchySettings.emperorScorpionClawCooldownTicks();
            this.level().broadcastEntityEvent(this, CLAW_ATTACK_EVENT);
            this.commitLunge(target, CLAW_LUNGE_SPEED, CLAW_LUNGE_LIFT);
        } else if (attackType == AttackType.STING) {
            this.stingCooldownTicks = AntarchySettings.emperorScorpionStingCooldownTicks();
            this.level().broadcastEntityEvent(this, STING_ATTACK_EVENT);
            this.commitLunge(target, STING_LUNGE_SPEED, STING_LUNGE_LIFT);
        }
    }

    private void tickActiveAttack() {
        if (this.attackTarget == null || !this.attackTarget.isAlive() || this.isHardenSequenceActive()) {
            this.resetAttackState();
            return;
        }

        if (!this.attackDamageApplied
                && this.attackAnimTicks == this.currentAttack.hitTick()
                && this.canHitAttackTarget(this.attackTarget, this.currentAttack)) {
            this.attackDamageApplied = true;
            if (this.currentAttack == AttackType.STING) {
                this.doStingAttack(this.attackTarget);
            } else {
                this.doHurtTarget(this.attackTarget);
            }
        }
    }

    private boolean canHitAttackTarget(LivingEntity target, AttackType attackType) {
        if (!target.isAlive() || !this.hasLineOfSight(target)) {
            return false;
        }
        double reach = attackType == AttackType.STING ? STING_HIT_REACH : CLAW_HIT_REACH;
        return this.distanceToSqr(target) <= reach * reach;
    }

    private void resetAttackState() {
        this.attackAnimTicks = 0;
        this.attackDamageApplied = false;
        this.currentAttack = AttackType.NONE;
        this.attackTarget = null;
    }

    private float approachYaw(float current, float target, float maxDeltaPerTick) {
        float delta = Mth.wrapDegrees(target - current);
        delta = Math.max(-maxDeltaPerTick, Math.min(maxDeltaPerTick, delta));
        return current + delta;
    }

    private void tickHardenState() {
        if (this.getHardenPhase() == HardenPhase.NONE) {
            if (this.hardenCooldownTicks <= 0 && this.getTarget() != null && this.getTarget().isAlive()) {
                this.beginHardenCast();
            }
            return;
        }

        this.getNavigation().stop();
        this.setSpeed(0.0F);
        this.setZza(0.0F);

        switch (this.getHardenPhase()) {
            case CASTING -> {
                if (this.hardenCastTicks % this.hardenCastSpawnTicks == 0
                        && this.hardenSummonedScorpions < AntarchySettings.emperorScorpionMaxSummonedScorpions()
                        && this.countOwnedNearbyScorpionMinions() < AntarchySettings.emperorScorpionMaxNearbyScorpions()) {
                    this.summonScorpionMinion();
                    this.hardenSummonedScorpions++;
                    if (this.hardenSummonedScorpions >= AntarchySettings.emperorScorpionMaxSummonedScorpions()) {
                        this.beginHardening();
                        return;
                    }
                }
                if (--this.hardenCastTicks <= 0) {
                    this.beginHardening();
                }
            }
            case START -> {
                if (--this.hardenStateTicks <= 0) {
                    this.beginHardened();
                }
            }
            case ACTIVE -> {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0.0D, motion.y, 0.0D);
                if (--this.hardenDurationTicks <= 0) {
                    this.beginHardeningEnd();
                    return;
                }
                if (--this.hardenHealTicks <= 0) {
                    this.hardenHealTicks = HARDEN_HEAL_INTERVAL_TICKS;
                    this.heal(HARDEN_HEAL_AMOUNT);
                }
            }
            case END -> {
                Vec3 motion = this.getDeltaMovement();
                this.setDeltaMovement(0.0D, motion.y, 0.0D);
                if (--this.hardenStateTicks <= 0) {
                    this.setHardenPhase(HardenPhase.NONE);
                    this.hardenCooldownTicks = Math.max(1, AntarchySettings.emperorScorpionHardenCooldownTicks());
                }
            }
        }
    }

    private void beginHardenCast() {
        this.setHardenPhase(HardenPhase.CASTING);
        this.hardenCastTicks = Math.max(1, AntarchySettings.emperorScorpionSummonIntervalTicks());
        this.hardenCastSpawnTicks = Math.max(1, this.hardenCastTicks / Math.max(1, AntarchySettings.emperorScorpionMaxSummonedScorpions()));
        this.hardenStateTicks = 0;
        this.hardenDurationTicks = 0;
        this.hardenHealTicks = 0;
        this.hardenSummonedScorpions = 0;
        this.resetAttackState();
        this.previousAttack = AttackType.NONE;
        this.getNavigation().stop();
    }

    private void beginHardening() {
        this.setHardenPhase(HardenPhase.START);
        this.hardenStateTicks = HARDEN_START_TICKS;
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 1.0F, 0.95F + this.random.nextFloat() * 0.05F);
        this.setAggressive(false);
    }

    private void beginHardened() {
        this.setHardenPhase(HardenPhase.ACTIVE);
        this.hardenDurationTicks = HARDEN_MIN_DURATION_TICKS + this.random.nextInt(HARDEN_MAX_DURATION_TICKS - HARDEN_MIN_DURATION_TICKS + 1);
        this.hardenHealTicks = HARDEN_HEAL_INTERVAL_TICKS;
    }

    private void beginHardeningEnd() {
        this.setHardenPhase(HardenPhase.END);
        this.hardenStateTicks = HARDEN_END_TICKS;
        this.setAggressive(false);
    }

    private int countOwnedNearbyScorpionMinions() {
        UUID ownerId = this.getUUID();
        return this.level().getEntitiesOfClass(
                ScorpionEntity.class,
                this.getBoundingBox().inflate(NEARBY_SCORPION_CHECK_RADIUS),
                scorpion -> scorpion.isAlive() && scorpion.isSummonedFor(ownerId)
        ).size();
    }

    private void summonScorpionMinion() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ScorpionEntity scorpion = AntarchyObjects.SCORPION.get().create(serverLevel);
        if (scorpion == null) {
            return;
        }

        Vec3 anchor = this.getTarget() != null ? this.getTarget().position() : this.position();
        Vec3 ring = Vec3.directionFromRotation(0.0F, this.random.nextFloat() * 360.0F)
                .scale(2.75D + this.random.nextDouble() * 1.75D);
        BlockPos spawnPos = BlockPos.containing(anchor.x + ring.x, this.getY(), anchor.z + ring.z);
        scorpion.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, this.random.nextFloat() * 360.0F, 0.0F);
        scorpion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
        scorpion.setPersistenceRequired();
        scorpion.setSummonOwner(this.getUUID());
        if (this.getTarget() instanceof LivingEntity target) {
            scorpion.setTarget(target);
        }
        serverLevel.addFreshEntity(scorpion);
        this.summonedScorpionIds.add(scorpion.getUUID());
    }

    private void commitLunge(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        Vec3 lunge = target.position().subtract(this.position());
        Vec3 horizontal = new Vec3(lunge.x, 0.0D, lunge.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return;
        }
        horizontal = horizontal.normalize().scale(horizontalSpeed);
        this.setDeltaMovement(this.getDeltaMovement().add(horizontal.x, verticalSpeed, horizontal.z));
        this.hasImpulse = true;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == CLAW_ATTACK_EVENT) {
            this.currentAttack = AttackType.CLAW;
            this.attackAnimTicks = AntarchySettings.emperorScorpionClawAnimTicks();
            this.attackDamageApplied = false;
            return;
        }
        if (id == STING_ATTACK_EVENT) {
            this.currentAttack = AttackType.STING;
            this.attackAnimTicks = AntarchySettings.emperorScorpionStingAnimTicks();
            this.attackDamageApplied = false;
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::mainController));
    }

    private PlayState mainController(AnimationState<EmperorScorpionEntity> state) {
        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_CLAW_ATTACK -> state.setAndContinue(CLAW_ATTACK_ANIM);
            case ANIM_STING_ATTACK -> state.setAndContinue(STING_ATTACK_ANIM);
            case ANIM_HARDEN_START -> state.setAndContinue(HARDEN_START_ANIM);
            case ANIM_HARDEN_ACTIVE -> state.setAndContinue(HARDEN_ACTIVE_ANIM);
            case ANIM_HARDEN_END -> state.setAndContinue(HARDEN_END_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            default -> state.setAndContinue(IDLE_ANIM);
        };
    }

    private int getAnimationState() {
        return this.entityData.get(ANIMATION_STATE);
    }

    private HardenPhase getHardenPhase() {
        HardenPhase[] phases = HardenPhase.values();
        int phaseOrdinal = this.entityData.get(HARDEN_PHASE);
        return phaseOrdinal >= 0 && phaseOrdinal < phases.length ? phases[phaseOrdinal] : HardenPhase.NONE;
    }

    public int getHardenPhaseIndex() {
        return this.entityData.get(HARDEN_PHASE);
    }

    private void setAnimationState(int animationState) {
        this.entityData.set(ANIMATION_STATE, animationState);
    }

    private void setHardenPhase(HardenPhase phase) {
        this.hardenPhase = phase;
        this.entityData.set(HARDEN_PHASE, phase.ordinal());
    }

    private void updateAnimationState() {
        if (this.isDeadOrDying()) {
            this.setAnimationState(ANIM_DEATH);
            return;
        }
        HardenPhase currentHardenPhase = this.getHardenPhase();
        if (currentHardenPhase == HardenPhase.START) {
            this.setAnimationState(ANIM_HARDEN_START);
            return;
        }
        if (currentHardenPhase == HardenPhase.ACTIVE) {
            this.setAnimationState(ANIM_HARDEN_ACTIVE);
            return;
        }
        if (currentHardenPhase == HardenPhase.END) {
            this.setAnimationState(ANIM_HARDEN_END);
            return;
        }
        if (this.attackAnimTicks > 0) {
            this.setAnimationState(this.currentAttack == AttackType.STING ? ANIM_STING_ATTACK : ANIM_CLAW_ATTACK);
            return;
        }
        if (currentHardenPhase == HardenPhase.CASTING) {
            this.setAnimationState(ANIM_IDLE);
            return;
        }
        if (this.getTarget() != null || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            this.setAnimationState(ANIM_WALK);
            return;
        }
        this.setAnimationState(ANIM_IDLE);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= DEATH_ANIM_TICKS && !this.level().isClientSide && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AntarchySoundEvents.EMPEROR_SCORPION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return AntarchySoundEvents.EMPEROR_SCORPION_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AntarchySoundEvents.EMPEROR_SCORPION_HURT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("HardenPhase", this.hardenPhase.ordinal());
        tag.putInt("HardenCooldownTicks", this.hardenCooldownTicks);
        tag.putInt("HardenCastTicks", this.hardenCastTicks);
        tag.putInt("HardenCastSpawnTicks", this.hardenCastSpawnTicks);
        tag.putInt("HardenStateTicks", this.hardenStateTicks);
        tag.putInt("HardenDurationTicks", this.hardenDurationTicks);
        tag.putInt("HardenHealTicks", this.hardenHealTicks);
        tag.putInt("HardenSummonedScorpions", this.hardenSummonedScorpions);
        tag.putBoolean("HasTakenDamage", this.hasTakenDamage);
        if (this.encounterHome != null) {
            tag.putInt("EncounterHomeX", this.encounterHome.getX());
            tag.putInt("EncounterHomeY", this.encounterHome.getY());
            tag.putInt("EncounterHomeZ", this.encounterHome.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HardenPhase")) {
            int phaseOrdinal = tag.getInt("HardenPhase");
            HardenPhase[] phases = HardenPhase.values();
            this.setHardenPhase(phaseOrdinal >= 0 && phaseOrdinal < phases.length ? phases[phaseOrdinal] : HardenPhase.NONE);
        }
        if (tag.contains("HardenCooldownTicks")) {
            this.hardenCooldownTicks = tag.getInt("HardenCooldownTicks");
        }
        if (tag.contains("HardenCastTicks")) {
            this.hardenCastTicks = tag.getInt("HardenCastTicks");
        }
        if (tag.contains("HardenCastSpawnTicks")) {
            this.hardenCastSpawnTicks = tag.getInt("HardenCastSpawnTicks");
        }
        if (tag.contains("HardenStateTicks")) {
            this.hardenStateTicks = tag.getInt("HardenStateTicks");
        }
        if (tag.contains("HardenDurationTicks")) {
            this.hardenDurationTicks = tag.getInt("HardenDurationTicks");
        }
        if (tag.contains("HardenHealTicks")) {
            this.hardenHealTicks = tag.getInt("HardenHealTicks");
        }
        if (tag.contains("HardenSummonedScorpions")) {
            this.hardenSummonedScorpions = tag.getInt("HardenSummonedScorpions");
        }
        if (tag.contains("HasTakenDamage")) {
            this.hasTakenDamage = tag.getBoolean("HasTakenDamage");
        }
        if (tag.contains("EncounterHomeX") && tag.contains("EncounterHomeY") && tag.contains("EncounterHomeZ")) {
            this.encounterHome = new BlockPos(tag.getInt("EncounterHomeX"), tag.getInt("EncounterHomeY"), tag.getInt("EncounterHomeZ"));
        }
        this.resetAttackState();
        if (this.getHardenPhase() == HardenPhase.CASTING) {
            this.setHardenPhase(HardenPhase.NONE);
        }
        this.disengageTicks = 0;
        this.summonedScorpionIds.clear();
    }

    @Override
    public void setCustomName(@Nullable net.minecraft.network.chat.Component name) {
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
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide) {
            this.discardOwnedScorpions();
        }
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    private boolean isAttackLocked() {
        return this.attackAnimTicks > 0;
    }

    private void tickEncounterState() {
        if (this.encounterHome == null) {
            this.encounterHome = this.blockPosition();
        }

        LivingEntity target = this.getTarget();
        if (!this.isValidEncounterTarget(target)) {
            if (target != null) {
                this.setTarget(null);
            }
            target = null;
        }

        if (target != null) {
            this.disengageTicks = 0;
            if (this.isOutsideLeashRadius()) {
                this.resetEncounter(true);
            }
            return;
        }

        if (this.isAttackLocked() || this.isHardenSequenceActive()) {
            return;
        }

        if (this.hasOwnedScorpionsNearby()) {
            this.disengageTicks = 0;
            return;
        }

        if (!this.isAtEncounterHome()) {
            this.disengageTicks++;
            if (this.disengageTicks >= RESET_DELAY_TICKS) {
                this.resetEncounter(true);
            } else {
                this.navigateHome();
            }
            return;
        }

        this.disengageTicks = 0;
    }

    private boolean isValidEncounterTarget(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || !target.level().equals(this.level())) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        if (this.encounterHome == null) {
            return true;
        }
        return target.distanceToSqr(Vec3.atCenterOf(this.encounterHome)) <= ENCOUNTER_LEASH_RADIUS * ENCOUNTER_LEASH_RADIUS;
    }

    private boolean shouldShowBossBar() {
        return this.getTarget() != null
                || this.isAttackLocked()
                || this.isHardenSequenceActive()
                || this.hasOwnedScorpionsNearby()
                || !this.isAtEncounterHome();
    }

    private boolean isAtEncounterHome() {
        if (this.encounterHome == null) {
            return true;
        }
        return this.distanceToSqr(Vec3.atCenterOf(this.encounterHome)) <= HOME_REACHED_RADIUS;
    }

    private boolean isOutsideLeashRadius() {
        if (this.encounterHome == null) {
            return false;
        }
        return this.distanceToSqr(Vec3.atCenterOf(this.encounterHome)) > ENCOUNTER_LEASH_RADIUS * ENCOUNTER_LEASH_RADIUS;
    }

    private boolean hasOwnedScorpionsNearby() {
        return this.countOwnedNearbyScorpionMinions() > 0;
    }

    private void navigateHome() {
        if (this.encounterHome == null) {
            return;
        }
        Vec3 homeCenter = Vec3.atCenterOf(this.encounterHome);
        if (this.distanceToSqr(homeCenter) > FORCED_RETURN_RADIUS * FORCED_RETURN_RADIUS) {
            this.moveTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
            this.hasImpulse = true;
            return;
        }
        this.getNavigation().moveTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, HOME_RETURN_SPEED);
    }

    private void resetEncounter(boolean restoreHealth) {
        this.resetAttackState();
        this.getNavigation().stop();
        this.setTarget(null);
        this.setAggressive(false);
        this.setHardenPhase(HardenPhase.NONE);
        this.hardenCastTicks = 0;
        this.hardenCastSpawnTicks = 0;
        this.hardenStateTicks = 0;
        this.hardenDurationTicks = 0;
        this.hardenHealTicks = 0;
        this.hardenSummonedScorpions = 0;
        this.hardenCooldownTicks = Math.max(1, AntarchySettings.emperorScorpionHardenCooldownTicks());
        this.disengageTicks = 0;
        this.previousAttack = AttackType.NONE;
        if (restoreHealth) {
            this.setHealth(this.getMaxHealth());
        }
        this.discardOwnedScorpions();
        if (this.encounterHome != null) {
            Vec3 homeCenter = Vec3.atCenterOf(this.encounterHome);
            this.moveTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void discardOwnedScorpions() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.summonedScorpionIds.clear();
            return;
        }
        UUID ownerId = this.getUUID();
        for (UUID scorpionId : Set.copyOf(this.summonedScorpionIds)) {
            Entity entity = serverLevel.getEntity(scorpionId);
            if (entity instanceof ScorpionEntity scorpion && scorpion.isAlive() && scorpion.isSummonedFor(ownerId)) {
                scorpion.remove(RemovalReason.DISCARDED);
            }
        }
        this.level().getEntitiesOfClass(
                ScorpionEntity.class,
                this.getBoundingBox().inflate(NEARBY_SCORPION_CHECK_RADIUS * 2.0D),
                scorpion -> scorpion.isAlive() && scorpion.isSummonedFor(ownerId)
        ).forEach(scorpion -> scorpion.remove(RemovalReason.DISCARDED));
        this.summonedScorpionIds.clear();
    }

    public boolean isHardenSequenceActive() {
        return this.getHardenPhase() != HardenPhase.NONE;
    }

    public boolean isHardenedStateActive() {
        HardenPhase currentHardenPhase = this.getHardenPhase();
        return currentHardenPhase == HardenPhase.START
                || currentHardenPhase == HardenPhase.ACTIVE
                || currentHardenPhase == HardenPhase.END;
    }

    public boolean isHardened() {
        return this.getHardenPhase() == HardenPhase.ACTIVE;
    }

    public boolean shouldUseHardenTexture() {
        return this.getHardenPhase() == HardenPhase.START
                || this.getHardenPhase() == HardenPhase.ACTIVE
                || this.getHardenPhase() == HardenPhase.END;
    }

    private final class EmperorScorpionCombatGoal extends Goal {
        private static final double CHASE_SPEED = 1.15D;

        private int repathCooldownTicks;
        private double repathTargetX;
        private double repathTargetY;
        private double repathTargetZ;

        private EmperorScorpionCombatGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = EmperorScorpionEntity.this.getTarget();
            return target != null && target.isAlive() && !EmperorScorpionEntity.this.isHardenSequenceActive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = EmperorScorpionEntity.this.getTarget();
            return EmperorScorpionEntity.this.isAttackLocked()
                    || (target != null && target.isAlive() && !EmperorScorpionEntity.this.isHardenSequenceActive());
        }

        @Override
        public void start() {
            EmperorScorpionEntity.this.setAggressive(true);
            this.repathCooldownTicks = 0;
        }

        @Override
        public void stop() {
            EmperorScorpionEntity.this.setAggressive(false);
            EmperorScorpionEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (EmperorScorpionEntity.this.isAttackLocked() || EmperorScorpionEntity.this.isHardenSequenceActive()) {
                return;
            }

            LivingEntity target = EmperorScorpionEntity.this.getTarget();
            if (target == null) {
                return;
            }

            if (EmperorScorpionEntity.this.isWithinMeleeAttackRange(target) && EmperorScorpionEntity.this.hasLineOfSight(target)) {
                EmperorScorpionEntity.this.getNavigation().stop();
                EmperorScorpionEntity.this.setDeltaMovement(0.0D, EmperorScorpionEntity.this.getDeltaMovement().y, 0.0D);
                AttackType attackType = EmperorScorpionEntity.this.selectAttack(target);
                if (attackType != AttackType.NONE) {
                    EmperorScorpionEntity.this.beginAttack(target, attackType);
                }
                return;
            }

            if (EmperorScorpionEntity.this.distanceToSqr(target) <= DIRECT_APPROACH_RANGE * DIRECT_APPROACH_RANGE
                    && EmperorScorpionEntity.this.hasLineOfSight(target)) {
                EmperorScorpionEntity.this.getNavigation().stop();
                this.approachDirectly(target);
                return;
            }

            this.tryRepath(target);
        }

        private void approachDirectly(LivingEntity target) {
            double dx = target.getX() - EmperorScorpionEntity.this.getX();
            double dz = target.getZ() - EmperorScorpionEntity.this.getZ();
            if (dx * dx + dz * dz < 1.0E-4D) {
                return;
            }
            float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            float currentYaw = EmperorScorpionEntity.this.getYRot();
            float newYaw = EmperorScorpionEntity.this.approachYaw(currentYaw, targetYaw, DIRECT_APPROACH_TURN_RATE);
            EmperorScorpionEntity.this.setYRot(newYaw);
            EmperorScorpionEntity.this.setYHeadRot(newYaw);
            EmperorScorpionEntity.this.yBodyRot = newYaw;
            EmperorScorpionEntity.this.yBodyRotO = newYaw;

            Vec3 currentMotion = EmperorScorpionEntity.this.getDeltaMovement();
            float headingError = Math.abs(Mth.wrapDegrees(targetYaw - currentYaw));
            if (headingError <= DIRECT_APPROACH_ALIGN_THRESHOLD) {
                double speed = EmperorScorpionEntity.this.getAttributeValue(Attributes.MOVEMENT_SPEED) * CHASE_SPEED;
                Vec3 forward = Vec3.directionFromRotation(0.0F, newYaw).scale(speed);
                EmperorScorpionEntity.this.setDeltaMovement(forward.x, currentMotion.y, forward.z);
            } else {
                EmperorScorpionEntity.this.setDeltaMovement(0.0D, currentMotion.y, 0.0D);
            }
        }

        private void tryRepath(LivingEntity target) {
            if (--this.repathCooldownTicks > 0
                    && target.distanceToSqr(this.repathTargetX, this.repathTargetY, this.repathTargetZ) < 1.0D) {
                return;
            }
            this.repathTargetX = target.getX();
            this.repathTargetY = target.getY();
            this.repathTargetZ = target.getZ();
            this.repathCooldownTicks = 4 + EmperorScorpionEntity.this.random.nextInt(7);
            if (!EmperorScorpionEntity.this.getNavigation().moveTo(target, CHASE_SPEED)) {
                this.repathCooldownTicks += 15;
            }
        }
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return this.distanceToSqr(target) <= CLAW_REACH * CLAW_REACH;
    }

    private AttackType selectAttack(LivingEntity target) {
        double distSqr = this.distanceToSqr(target);
        boolean canClaw = this.clawCooldownTicks <= 0 && distSqr <= CLAW_REACH * CLAW_REACH && this.hasLineOfSight(target);
        boolean canSting = this.stingCooldownTicks <= 0 && distSqr <= STING_REACH * STING_REACH && this.hasLineOfSight(target);
        if (canClaw && canSting) {
            return this.previousAttack == AttackType.CLAW && this.random.nextFloat() < 0.55F ? AttackType.STING : AttackType.CLAW;
        }
        if (canSting) {
            return AttackType.STING;
        }
        if (canClaw) {
            return AttackType.CLAW;
        }
        return AttackType.NONE;
    }

    private enum AttackType {
        NONE,
        CLAW,
        STING;

        private int animTicks() {
            return switch (this) {
                case CLAW -> AntarchySettings.emperorScorpionClawAnimTicks();
                case STING -> AntarchySettings.emperorScorpionStingAnimTicks();
                default -> 0;
            };
        }

        private int hitTick() {
            return switch (this) {
                case CLAW -> AntarchySettings.emperorScorpionClawHitTick();
                case STING -> AntarchySettings.emperorScorpionStingHitTick();
                default -> 0;
            };
        }
    }

    private enum HardenPhase {
        NONE,
        CASTING,
        START,
        ACTIVE,
        END
    }
}
