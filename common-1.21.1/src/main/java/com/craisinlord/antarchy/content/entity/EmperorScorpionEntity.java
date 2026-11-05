package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.damage.AntarchyDamageSources;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEcho;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.tags.DamageTypeTags;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.loot.LootTable;
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

public class EmperorScorpionEntity extends Monster implements GeoEntity, GlimmerEcho {
    private static final EntityDataAccessor<Integer> ANIMATION_STATE =
            SynchedEntityData.defineId(EmperorScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HARDEN_PHASE =
            SynchedEntityData.defineId(EmperorScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> GLIMMER_ECHO =
            SynchedEntityData.defineId(EmperorScorpionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTION_SERIAL =
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
    private static final int ANIM_PHASE_ROAR = 8;
    private static final int ANIM_HARDEN_CAST = 9;
    private static final double WALK_ANIM_SPEED_SQR = 1.0E-3D;
    private static final double CLAW_LUNGE_SPEED = 0.12D;
    private static final double CLAW_LUNGE_LIFT = 0.02D;
    private static final double STING_LUNGE_SPEED = 0.08D;
    private static final double STING_LUNGE_LIFT = 0.01D;
    private static final double CLAW_REACH = 5.0D;
    private static final double STING_REACH = 6.0D;
    private static final double CLAW_HIT_REACH = 6.25D;
    private static final double STING_HIT_REACH = 7.5D;
    private static final double DIRECT_APPROACH_RANGE = 12.0D;
    private static final float DIRECT_APPROACH_TURN_RATE = 7.0F;
    private static final double DIRECT_APPROACH_SPEED_SCALE = 0.77D;
    private static final int DIRECT_APPROACH_BLOCKED_TICKS = 20;
    private static final float MELEE_TURN_RATE = 8.0F;
    private static final float MELEE_FACING_DEADBAND = 5.0F;
    private static final int ATTACK_COMMIT_LEAD_TICKS = 6;
    private static final double ATTACK_ARC_COS = -0.34D;
    private static final int ATTACK_RECOVERY_TICKS = 6;
    private static final int PHASE_TWO_ATTACK_RECOVERY_TICKS = 4;
    private static final double PHASE_TWO_ATTACK_COOLDOWN_MULTIPLIER = 0.8D;
    private static final double PHASE_TWO_HARDEN_COOLDOWN_MULTIPLIER = 0.7D;
    private static final int PHASE_TWO_EXTRA_SCORPIONS = 1;
    private static final int PHASE_TRANSITION_TICKS = 30;
    private static final int PHASE_TRANSITION_SHOVE_TICK = 18;
    private static final double PHASE_TRANSITION_SHOVE_RADIUS = 8.0D;
    private static final int PHASE_TRANSITION_SUMMONS = 2;
    private static final int PHASE_TRANSITION_HARDEN_COOLDOWN_TICKS = 600;
    private static final int HARDEN_CAST_WINDUP_TICKS = 20;
    private static final int HARDEN_CAST_CAPPED_TICKS = 30;
    private static final double HARDEN_CAST_MAX_RANGE = 32.0D;
    private static final double HARDEN_END_BURST_RADIUS = 7.0D;
    private static final float HARDEN_END_BURST_DAMAGE_FRACTION = 0.5F;
    private static final int VENOM_AIM_TICKS = 15;
    private static final int VENOM_LOCK_LEAD_TICKS = 8;
    private static final int VENOM_COOLDOWN_TICKS = 140;
    private static final int VENOM_PRESSURE_THRESHOLD_TICKS = 60;
    private static final int VENOM_PRESSURE_MAX_TICKS = 200;
    private static final double VENOM_ELEVATION_THRESHOLD = 3.0D;
    private static final double VENOM_MAX_RANGE = 32.0D;
    private static final float VENOM_SPEED = 1.4F;
    private static final double VENOM_GRAVITY = 0.03D;
    private static final double RANGE_FALLOFF_MAX_RATIO = 80.0D / 48.0D;
    private static final int RANGE_FEEDBACK_INTERVAL_TICKS = 20;
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
    private static final double IDLE_HOME_DRIFT_RADIUS = 16.0D;
    private static final double RESET_PARTICIPANT_RADIUS = 32.0D;
    private static final int RESET_DELAY_TICKS = 240;
    private static final double HOME_RETURN_SPEED = 1.0D;
    private static final int HOME_REPATH_INTERVAL_TICKS = 20;
    private static final double NEARBY_SCORPION_CHECK_RADIUS = 32.0D;
    private static final int GLIMMER_XP_MULTIPLIER = 5;
    private static final int GLIMMER_EXTRA_SCORPIONS = 2;
    private static final int GLIMMER_PULSE_INTERVAL_TICKS = 40;
    private static final int GLIMMER_PULSE_TELEGRAPH_TICKS = 15;
    private static final double GLIMMER_PULSE_RADIUS = 6.0D;
    private static final float GLIMMER_PULSE_DAMAGE_FRACTION = 0.4F;
    private static final double GLIMMER_EFFECT_DURATION_MULTIPLIER = 1.5D;
    private static final double GLIMMER_ABANDON_RANGE = 48.0D;
    private static final int GLIMMER_ABANDON_TICKS = 600;
    private static final ResourceKey<LootTable> GLIMMER_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "entities/emperor_scorpion_glimmer"));

    private static final ResourceKey<Level> THORAXIS_KEY =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis"));
    private static final ResourceKey<net.minecraft.world.level.biome.Biome> DREAM_DUNES = ResourceKey.create(
            Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_dunes"));

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation CLAW_ATTACK_ANIM = RawAnimation.begin().thenPlay("snap");
    private static final RawAnimation STING_ATTACK_ANIM = RawAnimation.begin().thenPlay("sting");
    private static final RawAnimation HARDEN_START_ANIM = RawAnimation.begin().thenPlay("harden");
    private static final RawAnimation HARDEN_ACTIVE_ANIM = RawAnimation.begin().thenLoop("harden_state");
    private static final RawAnimation HARDEN_END_ANIM = RawAnimation.begin().thenPlay("end_of_harden_state");
    private static final RawAnimation DEATH_ANIM = RawAnimation.begin().thenPlay("death");
    private static final RawAnimation PHASE_ROAR_ANIM = RawAnimation.begin().thenPlay("agro");
    private static final RawAnimation HARDEN_CAST_ANIM = RawAnimation.begin().thenLoop("agro");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int animatedActionSerial;
    private final ServerBossEvent bossEvent =
            new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this, this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    private int attackAnimTicks;
    private int clawCooldownTicks;
    private int stingCooldownTicks;
    private int hardenCooldownTicks = AntarchySettings.emperorScorpionHardenCooldownTicks();
    private int hardenCastTicks;
    private int hardenCastTotalTicks;
    private int hardenCastSpawnTicks;
    private int hardenStateTicks;
    private int hardenDurationTicks;
    private int hardenHealTicks;
    private int hardenSummonedScorpions;
    private int disengageTicks;
    private int homeRepathCooldown;
    private boolean attackDamageApplied;
    private AttackType currentAttack = AttackType.NONE;
    private HardenPhase hardenPhase = HardenPhase.NONE;
    private AttackType previousAttack = AttackType.NONE;
    @Nullable private BlockPos encounterHome;
    @Nullable private LivingEntity attackTarget;
    private final Set<UUID> summonedScorpionIds = new HashSet<>();
    private boolean hasTakenDamage;
    private int glimmerPulseTicks;
    private int glimmerAbandonTicks;
    private boolean phaseTwo;
    private int phaseTransitionTicks;
    private int attackRecoveryTicks;
    private int venomCooldownTicks;
    private int venomPressureTicks;
    private boolean venomAimLocked;
    @Nullable private Vec3 venomAimPoint;
    private int lastRangeFeedbackTick = -RANGE_FEEDBACK_INTERVAL_TICKS;
    private boolean encounterActive;
    private boolean leashReturning;

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
        double health = AntarchySettings.emperorScorpionHealth();
        if (this.isGlimmerEcho()) {
            health *= AntarchySettings.glimmerEmperorScorpionHealthMultiplier();
        }
        ConfiguredMobSpawnUtil.applyConfiguredHealth(this, health);
        if (ThoraxisUndersideManager.isUndersideCeilingSpot(level, this.blockPosition(), this.getType())) {
            ThoraxisUndersideManager.applyUndersideInversion(this);
        }
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnData);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION_STATE, ANIM_IDLE);
        builder.define(HARDEN_PHASE, HardenPhase.NONE.ordinal());
        builder.define(GLIMMER_ECHO, false);
        builder.define(ACTION_SERIAL, 0);
    }

    @Override
    public boolean isGlimmerEcho() {
        return this.entityData.get(GLIMMER_ECHO);
    }

    @Override
    public void setGlimmerEcho(boolean glimmerEcho) {
        boolean wasGlimmerEcho = this.isGlimmerEcho();
        this.entityData.set(GLIMMER_ECHO, glimmerEcho);
        int baseXpReward = AntarchySettings.emperorScorpionXpReward();
        this.xpReward = glimmerEcho ? baseXpReward * GLIMMER_XP_MULTIPLIER : baseXpReward;
        if (glimmerEcho || wasGlimmerEcho) {
            AttributeInstance attackDamage = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamage != null) {
                attackDamage.setBaseValue(AntarchySettings.emperorScorpionAttackDamage() * this.glimmerEchoDamageMultiplier());
            }
        }
        this.bossEvent.setName(this.getDisplayName());
        this.bossEvent.setColor(glimmerEcho ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.PURPLE);
    }

    @Override
    public float glimmerEchoDamageMultiplier() {
        return this.isGlimmerEcho() ? (float) AntarchySettings.glimmerEmperorScorpionDamageMultiplier() : 1.0F;
    }

    @Override
    protected Component getTypeName() {
        return this.isGlimmerEcho() ? Component.translatable("entity.antarchy.emperor_scorpion.glimmer") : super.getTypeName();
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return this.isGlimmerEcho() ? GLIMMER_LOOT_TABLE : super.getDefaultLootTable();
    }

    private int scaledCooldown(int ticks) {
        if (!this.isGlimmerEcho()) {
            return ticks;
        }
        return Math.max(10, (int) Math.round(ticks * AntarchySettings.glimmerEmperorScorpionCooldownMultiplier()));
    }

    private int maxSummonedScorpions() {
        return AntarchySettings.emperorScorpionMaxSummonedScorpions() + this.extraScorpions();
    }

    private int maxNearbyScorpions() {
        return AntarchySettings.emperorScorpionMaxNearbyScorpions() + this.extraScorpions();
    }

    private int extraScorpions() {
        return (this.isGlimmerEcho() ? GLIMMER_EXTRA_SCORPIONS : 0) + (this.phaseTwo ? PHASE_TWO_EXTRA_SCORPIONS : 0);
    }

    private int attackCooldown(int ticks) {
        return this.scaledCooldown(this.phaseTwo ? (int) Math.round(ticks * PHASE_TWO_ATTACK_COOLDOWN_MULTIPLIER) : ticks);
    }

    private int hardenCooldown() {
        int ticks = Math.max(1, AntarchySettings.emperorScorpionHardenCooldownTicks());
        return this.phaseTwo ? Math.max(1, (int) Math.round(ticks * PHASE_TWO_HARDEN_COOLDOWN_MULTIPLIER)) : ticks;
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
            this.glimmerPulseTicks = GLIMMER_PULSE_INTERVAL_TICKS;
        }
        this.glimmerPulseTicks--;
        if (this.level() instanceof ServerLevel serverLevel
                && this.glimmerPulseTicks > 0
                && this.glimmerPulseTicks <= GLIMMER_PULSE_TELEGRAPH_TICKS
                && this.glimmerPulseTicks % 5 == 0) {
            double progress = 1.0D - this.glimmerPulseTicks / (double) GLIMMER_PULSE_TELEGRAPH_TICKS;
            GlimmerEchoes.ring(serverLevel, this.position(), 1.5D + GLIMMER_PULSE_RADIUS * progress, 32);
        }
        if (this.glimmerPulseTicks == 0) {
            this.applyGlimmerPulse();
        }
    }

    private void applyGlimmerPulse() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * GLIMMER_PULSE_DAMAGE_FRACTION;
        double rangeSqr = GLIMMER_PULSE_RADIUS * GLIMMER_PULSE_RADIUS;
        for (ServerPlayer player : serverLevel.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isCreative() || player.distanceToSqr(this) > rangeSqr) {
                continue;
            }
            if (player.hurt(AntarchyDamageSources.emperorScorpionSting(serverLevel, this), damage)) {
                Vec3 offset = player.position().subtract(this.position());
                Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
                if (horizontal.lengthSqr() > 1.0E-4D) {
                    horizontal = horizontal.normalize();
                    player.push(horizontal.x * 1.1D, 0.4D, horizontal.z * 1.1D);
                    player.hurtMarked = true;
                }
            }
        }
        GlimmerEchoes.ring(serverLevel, this.position(), GLIMMER_PULSE_RADIUS, 48);
        this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0F, 0.6F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EmperorScorpionCombatGoal());
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !EmperorScorpionEntity.this.encounterActive && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !EmperorScorpionEntity.this.encounterActive && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            if (this.isHardenedStateActive() || this.phaseTransitionTicks > 0) {
                this.playResistFeedback();
                return false;
            }
            double fullRange = AntarchySettings.emperorScorpionDamageRange();
            float falloff = BossCombatUtil.damageRangeMultiplier(this, source, fullRange, fullRange * RANGE_FALLOFF_MAX_RATIO);
            if (falloff < 1.0F) {
                this.playResistFeedback();
                if (falloff <= 0.0F) {
                    return false;
                }
                amount *= falloff;
            }
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt) {
            this.hasTakenDamage = true;
            this.tryBeginPhaseTransition();
        }
        return hurt;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float preHealth = this.getHealth();
        super.actuallyHurt(source, amount);
        if (!this.phaseTwo) {
            BossCombatUtil.clampHalfHealthCrossing(this, preHealth, source);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.isInvulnerableTo(source);
        }
        return this.isHardenedStateActive() || this.phaseTransitionTicks > 0 || super.isInvulnerableTo(source);
    }

    private void playResistFeedback() {
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
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide) {
            this.resetAttackState();
            this.setHardenPhase(HardenPhase.NONE);
            this.phaseTransitionTicks = 0;
            this.getNavigation().stop();
        }
        super.die(damageSource);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        if (this.isGlimmerEcho() || this.hasTakenDamage || this.tickCount < AntarchySettings.emperorScorpionMinDespawnTicks()) {
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
                || spawnReason == MobSpawnType.TRIAL_SPAWNER
                || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }
        if (!level.getLevel().dimension().equals(THORAXIS_KEY)) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (ThoraxisUndersideManager.isUndersideCeilingSpawn(entityType, level, pos)) {
            return true;
        }
        if (pos.getY() < 0 && level.getBiome(pos).is(DREAM_DUNES) && level.isEmptyBlock(pos)) {
            BlockState below = level.getBlockState(pos.below());
            return !below.is(Blocks.BEDROCK) && below.isFaceSturdy(level, pos.below(), Direction.UP);
        }
        return Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
    }

    @Override
    public float maxUpStep() {
        return 1.5F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (this.isDeadOrDying()) {
            this.bossEvent.setProgress(0.0F);
            this.updateAnimationState();
            return;
        }
        if (this.isGlimmerEcho()) {
            GlimmerEchoes.tickAmbient(this, 3, 4);
            if (this.tickGlimmerAbandonment()) {
                return;
            }
        }
        this.tickEncounterState();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setVisible(this.shouldShowBossBar());

        if (this.clawCooldownTicks > 0) {
            this.clawCooldownTicks--;
        }
        if (this.stingCooldownTicks > 0) {
            this.stingCooldownTicks--;
        }
        if (this.venomCooldownTicks > 0) {
            this.venomCooldownTicks--;
        }
        if (this.attackRecoveryTicks > 0) {
            this.attackRecoveryTicks--;
        }
        if (this.attackAnimTicks > 0) {
            this.tickActiveAttack();
            if (this.attackAnimTicks > 0 && --this.attackAnimTicks <= 0) {
                this.resetAttackState();
                this.attackRecoveryTicks = this.phaseTwo ? PHASE_TWO_ATTACK_RECOVERY_TICKS : ATTACK_RECOVERY_TICKS;
            }
        }
        this.tryBeginPhaseTransition();
        this.tickPhaseTransition();
        this.tickHardenState();

        this.updateAnimationState();
    }

    private void tryBeginPhaseTransition() {
        if (this.level().isClientSide
                || this.phaseTwo
                || this.isDeadOrDying()
                || this.getHealth() > this.getMaxHealth() * 0.5F
                || this.isHardenedStateActive()) {
            return;
        }
        this.phaseTwo = true;
        this.phaseTransitionTicks = PHASE_TRANSITION_TICKS;
        this.resetAttackState();
        this.attackRecoveryTicks = 0;
        this.setHardenPhase(HardenPhase.NONE);
        this.getNavigation().stop();
        this.setAggressive(false);
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 2.0F, 0.7F);
    }

    private void tickPhaseTransition() {
        if (this.phaseTransitionTicks <= 0) {
            return;
        }
        this.haltHorizontalMovement();
        this.phaseTransitionTicks--;
        if (this.phaseTransitionTicks == PHASE_TRANSITION_SHOVE_TICK) {
            this.applyPhaseTransitionShove();
        }
        if (this.phaseTransitionTicks == 0) {
            this.hardenCooldownTicks = Math.min(this.hardenCooldown(), PHASE_TRANSITION_HARDEN_COOLDOWN_TICKS);
            for (int i = 0; i < PHASE_TRANSITION_SUMMONS && this.countOwnedNearbyScorpionMinions() < this.maxNearbyScorpions(); i++) {
                this.summonScorpionMinion();
            }
        }
    }

    private void applyPhaseTransitionShove() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double rangeSqr = PHASE_TRANSITION_SHOVE_RADIUS * PHASE_TRANSITION_SHOVE_RADIUS;
        for (ServerPlayer player : serverLevel.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isCreative()
                    || player.distanceToSqr(this) > rangeSqr || !this.hasLineOfSight(player)) {
                continue;
            }
            this.pushAway(player, 1.1D, 0.35D);
        }
        this.sendRing(serverLevel, ParticleTypes.CRIT, PHASE_TRANSITION_SHOVE_RADIUS);
    }

    private void pushAway(ServerPlayer player, double horizontalStrength, double lift) {
        Vec3 offset = player.position().subtract(this.position());
        Vec3 horizontal = new Vec3(offset.x, 0.0D, offset.z);
        if (horizontal.lengthSqr() > 1.0E-4D) {
            horizontal = horizontal.normalize();
            player.push(horizontal.x * horizontalStrength, lift, horizontal.z * horizontalStrength);
            player.hurtMarked = true;
        }
    }

    private void sendRing(ServerLevel serverLevel, ParticleOptions particle, double radius) {
        ParticleOptions resolved = GlimmerEchoes.particle(this, particle);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * 2.0D * i / 8.0D;
            serverLevel.sendParticles(resolved, this.getX() + Math.cos(angle) * radius, this.getY() + 0.2D, this.getZ() + Math.sin(angle) * radius,
                    3, 0.6D, 0.05D, 0.6D, 0.0D);
        }
    }

    private void haltHorizontalMovement() {
        this.getNavigation().stop();
        GravityDirection direction = GravityApi.getGravityDirection(this);
        Vec3 motion = GravityRotationUtil.vecWorldToPlayer(this.getDeltaMovement(), direction);
        this.setDeltaMovement(GravityRotationUtil.vecPlayerToWorld(0.0D, motion.y, 0.0D, direction));
    }

    private void faceTarget(LivingEntity target, float maxTurn) {
        GravityDirection direction = GravityApi.getGravityDirection(this);
        Vec3 localDelta = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(this.position()), direction);
        if (localDelta.x * localDelta.x + localDelta.z * localDelta.z < 1.0D) {
            return;
        }
        float targetYaw = (float) (Mth.atan2(localDelta.z, localDelta.x) * (180.0D / Math.PI)) - 90.0F;
        float currentYaw = this.getYRot();
        if (Math.abs(Mth.wrapDegrees(targetYaw - currentYaw)) <= MELEE_FACING_DEADBAND) {
            return;
        }
        float newYaw = this.approachYaw(currentYaw, targetYaw, maxTurn);
        this.setYRot(newYaw);
        this.setYHeadRot(newYaw);
        this.yBodyRot = newYaw;
        this.yBodyRotO = newYaw;
    }

    private boolean isInFrontArc(LivingEntity target) {
        GravityDirection direction = GravityApi.getGravityDirection(this);
        Vec3 localDelta = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(this.position()), direction);
        Vec3 horizontal = new Vec3(localDelta.x, 0.0D, localDelta.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return true;
        }
        return Vec3.directionFromRotation(0.0F, this.getYRot()).dot(horizontal.normalize()) >= ATTACK_ARC_COS;
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
        int weaknessDuration = AntarchySettings.emperorScorpionWeaknessTicks();
        if (this.isGlimmerEcho()) {
            poisonDuration = (int) Math.round(poisonDuration * GLIMMER_EFFECT_DURATION_MULTIPLIER);
            weaknessDuration = (int) Math.round(weaknessDuration * GLIMMER_EFFECT_DURATION_MULTIPLIER);
        }
        target.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 1));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weaknessDuration, 0));
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 1.0F, 0.88F + this.random.nextFloat() * 0.08F);
        return true;
    }

    private void beginAttack(LivingEntity target, AttackType attackType) {
        if (this.isHardenSequenceActive() || this.phaseTransitionTicks > 0) {
            return;
        }
        this.attackTarget = target;
        this.currentAttack = attackType;
        this.previousAttack = attackType;
        this.attackAnimTicks = attackType.animTicks();
        this.attackDamageApplied = false;
        this.venomAimLocked = false;
        this.venomAimPoint = null;
        this.getNavigation().stop();
        if (attackType == AttackType.CLAW) {
            this.clawCooldownTicks = this.attackCooldown(AntarchySettings.emperorScorpionClawCooldownTicks());
            this.commitLunge(target, CLAW_LUNGE_SPEED, CLAW_LUNGE_LIFT);
        } else if (attackType == AttackType.STING) {
            this.stingCooldownTicks = this.attackCooldown(AntarchySettings.emperorScorpionStingCooldownTicks());
            this.commitLunge(target, STING_LUNGE_SPEED, STING_LUNGE_LIFT);
        } else if (attackType == AttackType.VENOM) {
            this.venomCooldownTicks = this.attackCooldown(VENOM_COOLDOWN_TICKS);
            this.venomPressureTicks = 0;
            this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 1.4F, 1.2F);
        }
    }

    private void tickActiveAttack() {
        LivingEntity target = this.attackTarget;
        if (target == null || !target.isAlive() || this.isHardenSequenceActive() || this.phaseTransitionTicks > 0) {
            this.resetAttackState();
            return;
        }

        if (this.currentAttack == AttackType.VENOM) {
            this.tickVenomAttack(target);
            return;
        }

        if (this.attackAnimTicks > this.currentAttack.hitTick() + ATTACK_COMMIT_LEAD_TICKS) {
            this.faceTarget(target, MELEE_TURN_RATE);
        }

        if (!this.attackDamageApplied
                && this.attackAnimTicks == this.currentAttack.hitTick()
                && this.canHitAttackTarget(target, this.currentAttack)) {
            this.attackDamageApplied = true;
            if (this.currentAttack == AttackType.STING) {
                this.doStingAttack(target);
            } else {
                this.doHurtTarget(target);
            }
        }
    }

    private void tickVenomAttack(LivingEntity target) {
        this.haltHorizontalMovement();
        int hitTick = AttackType.VENOM.hitTick();
        if (!this.venomAimLocked) {
            this.faceTarget(target, MELEE_TURN_RATE);
            if (this.attackAnimTicks <= hitTick + VENOM_LOCK_LEAD_TICKS) {
                this.venomAimLocked = true;
                this.venomAimPoint = target.getEyePosition();
            }
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 origin = this.venomOrigin();
        if (this.attackAnimTicks % 3 == 0) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.ITEM_SLIME), origin.x, origin.y, origin.z, 6, 0.4D, 0.4D, 0.4D, 0.02D);
        }
        Vec3 aimPoint = this.venomAimPoint;
        if (aimPoint == null) {
            return;
        }
        if (this.attackAnimTicks == hitTick + VENOM_LOCK_LEAD_TICKS) {
            Vec3 step = aimPoint.subtract(origin).scale(1.0D / 12.0D);
            for (int i = 1; i <= 12; i++) {
                Vec3 point = origin.add(step.scale(i));
                serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.ITEM_SLIME), point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        if (!this.attackDamageApplied && this.attackAnimTicks == hitTick) {
            this.attackDamageApplied = true;
            this.fireVenom(serverLevel, origin, aimPoint);
        }
    }

    private Vec3 venomOrigin() {
        GravityDirection direction = GravityApi.getGravityDirection(this);
        Vec3 up = GravityRotationUtil.vecPlayerToWorld(0.0D, this.getBbHeight() + 0.4D, 0.0D, direction);
        return this.position().add(up);
    }

    @SuppressWarnings("unchecked")
    private void fireVenom(ServerLevel serverLevel, Vec3 origin, Vec3 aimPoint) {
        EntityType<SpitBugProjectileEntity> projectileType = (EntityType<SpitBugProjectileEntity>) BuiltInRegistries.ENTITY_TYPE.getOptional(
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "spit_bug_projectile")
        ).orElse(null);
        if (projectileType == null) {
            return;
        }
        SpitBugProjectileEntity projectile = new SpitBugProjectileEntity(projectileType, this, serverLevel);
        double flightTicks = aimPoint.distanceTo(origin) / VENOM_SPEED;
        Vec3 direction = aimPoint.add(0.0D, 0.5D * VENOM_GRAVITY * flightTicks * flightTicks, 0.0D).subtract(origin).normalize();
        projectile.setDirectHitDamage(SpitBugProjectileEntity.DEFAULT_DIRECT_HIT_DAMAGE * this.glimmerEchoDamageMultiplier());
        projectile.setPos(origin.x, origin.y, origin.z);
        projectile.shoot(direction.x, direction.y, direction.z, VENOM_SPEED, 0.0F);
        serverLevel.addFreshEntity(projectile);
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 1.2F, 0.75F + this.random.nextFloat() * 0.08F);
    }

    private boolean canHitAttackTarget(LivingEntity target, AttackType attackType) {
        if (!target.isAlive() || !this.isInFrontArc(target) || !this.hasLineOfSight(target)) {
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
        this.venomAimLocked = false;
        this.venomAimPoint = null;
    }

    private float approachYaw(float current, float target, float maxDeltaPerTick) {
        float delta = Mth.wrapDegrees(target - current);
        delta = Math.max(-maxDeltaPerTick, Math.min(maxDeltaPerTick, delta));
        return current + delta;
    }

    private void tickHardenState() {
        if (this.phaseTransitionTicks > 0) {
            return;
        }
        if (this.getHardenPhase() == HardenPhase.NONE) {
            LivingEntity target = this.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }
            if (this.hardenCooldownTicks > 0) {
                this.hardenCooldownTicks--;
                return;
            }
            if (!this.isAttackLocked() && this.distanceToSqr(target) <= HARDEN_CAST_MAX_RANGE * HARDEN_CAST_MAX_RANGE) {
                this.beginHardenCast();
            }
            return;
        }

        this.getNavigation().stop();
        this.setSpeed(0.0F);
        this.setZza(0.0F);

        switch (this.getHardenPhase()) {
            case CASTING -> {
                int elapsed = this.hardenCastTotalTicks - this.hardenCastTicks;
                boolean atCap = this.countOwnedNearbyScorpionMinions() >= this.maxNearbyScorpions();
                if (atCap && elapsed >= HARDEN_CAST_CAPPED_TICKS) {
                    this.beginHardening();
                    return;
                }
                if (elapsed >= HARDEN_CAST_WINDUP_TICKS
                        && (elapsed - HARDEN_CAST_WINDUP_TICKS) % this.hardenCastSpawnTicks == 0
                        && this.hardenSummonedScorpions < this.maxSummonedScorpions()
                        && !atCap) {
                    if (this.summonScorpionMinion()) {
                        this.hardenSummonedScorpions++;
                    }
                    if (this.hardenSummonedScorpions >= this.maxSummonedScorpions()) {
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
                GravityDirection direction = GravityApi.getGravityDirection(this);
                Vec3 motion = GravityRotationUtil.vecWorldToPlayer(this.getDeltaMovement(), direction);
                this.setDeltaMovement(GravityRotationUtil.vecPlayerToWorld(0.0D, motion.y, 0.0D, direction));
                if (--this.hardenDurationTicks <= 0) {
                    this.beginHardeningEnd();
                    return;
                }
                if (--this.hardenHealTicks <= 0) {
                    this.hardenHealTicks = HARDEN_HEAL_INTERVAL_TICKS;
                    this.heal(HARDEN_HEAL_AMOUNT);
                }
                if (this.isGlimmerEcho()) {
                    this.tickGlimmerPulse();
                }
            }
            case END -> {
                GravityDirection direction = GravityApi.getGravityDirection(this);
                Vec3 motion = GravityRotationUtil.vecWorldToPlayer(this.getDeltaMovement(), direction);
                this.setDeltaMovement(GravityRotationUtil.vecPlayerToWorld(0.0D, motion.y, 0.0D, direction));
                this.tickHardenEndBurst();
                if (--this.hardenStateTicks <= 0) {
                    this.setHardenPhase(HardenPhase.NONE);
                    this.hardenCooldownTicks = this.hardenCooldown();
                }
            }
        }
    }

    private void tickHardenEndBurst() {
        if (!this.phaseTwo || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (this.hardenStateTicks > 1 && this.hardenStateTicks % 5 == 0) {
            double progress = 1.0D - this.hardenStateTicks / (double) HARDEN_END_TICKS;
            this.sendRing(serverLevel, ParticleTypes.CRIT, 1.5D + (HARDEN_END_BURST_RADIUS - 1.5D) * progress);
        }
        if (this.hardenStateTicks != 1) {
            return;
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * HARDEN_END_BURST_DAMAGE_FRACTION;
        double rangeSqr = HARDEN_END_BURST_RADIUS * HARDEN_END_BURST_RADIUS;
        for (ServerPlayer player : serverLevel.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isCreative()
                    || player.distanceToSqr(this) > rangeSqr || !this.hasLineOfSight(player)) {
                continue;
            }
            if (player.hurt(AntarchyDamageSources.emperorScorpionSting(serverLevel, this), damage)) {
                this.pushAway(player, 1.1D, 0.4D);
            }
        }
        this.sendRing(serverLevel, ParticleTypes.CRIT, HARDEN_END_BURST_RADIUS);
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ATTACK.get(), 1.6F, 0.6F);
    }

    private void beginHardenCast() {
        this.setHardenPhase(HardenPhase.CASTING);
        this.hardenCastTotalTicks = Math.max(HARDEN_CAST_WINDUP_TICKS + 1, AntarchySettings.emperorScorpionSummonIntervalTicks());
        this.hardenCastTicks = this.hardenCastTotalTicks;
        this.hardenCastSpawnTicks = Math.max(1, (this.hardenCastTotalTicks - HARDEN_CAST_WINDUP_TICKS) / Math.max(1, this.maxSummonedScorpions()));
        this.hardenStateTicks = 0;
        this.hardenDurationTicks = 0;
        this.hardenHealTicks = 0;
        this.hardenSummonedScorpions = 0;
        this.resetAttackState();
        this.previousAttack = AttackType.NONE;
        this.getNavigation().stop();
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 1.5F, 0.75F);
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
        this.glimmerPulseTicks = GLIMMER_PULSE_INTERVAL_TICKS;
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

    private boolean summonScorpionMinion() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        ScorpionEntity scorpion = AntarchyObjects.SCORPION.get().create(serverLevel);
        if (scorpion == null) {
            return false;
        }

        BlockPos spawnPos = this.findSummonSpawnPos(scorpion);
        if (spawnPos == null) {
            scorpion.discard();
            return false;
        }
        Vec3 spawnCenter = Vec3.atCenterOf(spawnPos);
        scorpion.moveTo(spawnCenter.x, spawnCenter.y, spawnCenter.z, this.random.nextFloat() * 360.0F, 0.0F);
        GlimmerEchoes.markSummon(this, scorpion);
        scorpion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
        scorpion.setPersistenceRequired();
        scorpion.setSummonOwner(this.getUUID());
        if (this.getTarget() instanceof LivingEntity target) {
            scorpion.setTarget(target);
        }
        serverLevel.addFreshEntity(scorpion);
        this.summonedScorpionIds.add(scorpion.getUUID());
        serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), spawnCenter.x, spawnCenter.y, spawnCenter.z, 12, 0.5D, 0.3D, 0.5D, 0.02D);
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 0.8F, 1.3F);
        return true;
    }

    private void commitLunge(LivingEntity target, double horizontalSpeed, double verticalSpeed) {
        GravityDirection direction = GravityApi.getGravityDirection(this);
        Vec3 localLunge = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(this.position()), direction);
        Vec3 horizontal = new Vec3(localLunge.x, 0.0D, localLunge.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return;
        }
        horizontal = horizontal.normalize().scale(horizontalSpeed);
        Vec3 localImpulse = new Vec3(horizontal.x, verticalSpeed, horizontal.z);
        Vec3 worldImpulse = GravityRotationUtil.vecPlayerToWorld(localImpulse, direction);
        this.setDeltaMovement(this.getDeltaMovement().add(worldImpulse.x, worldImpulse.y, worldImpulse.z));
        this.hasImpulse = true;
    }

    @Nullable
    private BlockPos findSummonSpawnPos(ScorpionEntity scorpion) {
        GravityDirection direction = GravityApi.getGravityDirection(this);
        LivingEntity target = this.getTarget();
        Vec3[] anchors = target != null ? new Vec3[]{target.position(), this.position()} : new Vec3[]{this.position()};
        for (Vec3 anchor : anchors) {
            for (int attempt = 0; attempt < 4; attempt++) {
                Vec3 localRing = Vec3.directionFromRotation(0.0F, this.random.nextFloat() * 360.0F)
                        .scale(2.75D + this.random.nextDouble() * 1.75D);
                BlockPos found = this.findSummonFloor(scorpion, anchor.add(GravityRotationUtil.vecPlayerToWorld(localRing, direction)));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findSummonFloor(ScorpionEntity scorpion, Vec3 center) {
        BlockPos base = BlockPos.containing(center);
        if (!this.level().hasChunkAt(base)) {
            return null;
        }
        Direction gravityDown = GravityRotationUtil.getGravityDownDirection(this);
        Direction gravityUp = gravityDown.getOpposite();
        for (int step = -2; step <= 2; step++) {
            BlockPos candidate = base.relative(gravityUp, step);
            BlockPos support = candidate.relative(gravityDown);
            if (this.level().isEmptyBlock(candidate)
                    && this.level().isEmptyBlock(candidate.relative(gravityUp))
                    && this.level().getBlockState(support).isFaceSturdy(this.level(), support, gravityUp)) {
                Vec3 candidateCenter = Vec3.atCenterOf(candidate);
                if (this.level().noCollision(scorpion, scorpion.getType().getSpawnAABB(candidateCenter.x, candidateCenter.y, candidateCenter.z))) {
                    return candidate;
                }
            }
        }
        return null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 3, this::mainController));
    }

    private PlayState mainController(AnimationState<EmperorScorpionEntity> state) {
        int actionSerial = this.entityData.get(ACTION_SERIAL);
        if (actionSerial != this.animatedActionSerial) {
            this.animatedActionSerial = actionSerial;
            state.getController().forceAnimationReset();
        }
        return switch (this.getAnimationState()) {
            case ANIM_WALK -> state.setAndContinue(WALK_ANIM);
            case ANIM_CLAW_ATTACK -> state.setAndContinue(CLAW_ATTACK_ANIM);
            case ANIM_STING_ATTACK -> state.setAndContinue(STING_ATTACK_ANIM);
            case ANIM_HARDEN_START -> state.setAndContinue(HARDEN_START_ANIM);
            case ANIM_HARDEN_ACTIVE -> state.setAndContinue(HARDEN_ACTIVE_ANIM);
            case ANIM_HARDEN_END -> state.setAndContinue(HARDEN_END_ANIM);
            case ANIM_DEATH -> state.setAndContinue(DEATH_ANIM);
            case ANIM_PHASE_ROAR -> state.setAndContinue(PHASE_ROAR_ANIM);
            case ANIM_HARDEN_CAST -> state.setAndContinue(HARDEN_CAST_ANIM);
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
        if (this.entityData.get(ANIMATION_STATE) != animationState) {
            this.entityData.set(ANIMATION_STATE, animationState);
            if (animationState != ANIM_IDLE && animationState != ANIM_WALK) {
                this.entityData.set(ACTION_SERIAL, this.entityData.get(ACTION_SERIAL) + 1);
            }
        }
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
        if (this.phaseTransitionTicks > 0) {
            this.setAnimationState(ANIM_PHASE_ROAR);
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
        if (currentHardenPhase == HardenPhase.CASTING) {
            this.setAnimationState(ANIM_HARDEN_CAST);
            return;
        }
        if (this.attackAnimTicks > 0) {
            if (this.currentAttack == AttackType.VENOM && this.attackAnimTicks > AntarchySettings.emperorScorpionStingAnimTicks()) {
                this.setAnimationState(ANIM_IDLE);
            } else {
                this.setAnimationState(this.currentAttack == AttackType.CLAW ? ANIM_CLAW_ATTACK : ANIM_STING_ATTACK);
            }
            return;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > WALK_ANIM_SPEED_SQR) {
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
        tag.putBoolean("PhaseTwo", this.phaseTwo);
        tag.putInt("PhaseTransitionTicks", this.phaseTransitionTicks);
        tag.putBoolean("EncounterActive", this.encounterActive);
        tag.putBoolean("LeashReturning", this.leashReturning);
        tag.putBoolean(GlimmerEchoes.SAVE_KEY, this.isGlimmerEcho());
        if (this.encounterHome != null) {
            tag.putInt("EncounterHomeX", this.encounterHome.getX());
            tag.putInt("EncounterHomeY", this.encounterHome.getY());
            tag.putInt("EncounterHomeZ", this.encounterHome.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setGlimmerEcho(tag.getBoolean(GlimmerEchoes.SAVE_KEY));
        if (tag.contains("HardenPhase")) {
            int phaseOrdinal = tag.getInt("HardenPhase");
            HardenPhase[] phases = HardenPhase.values();
            this.setHardenPhase(phaseOrdinal >= 0 && phaseOrdinal < phases.length ? phases[phaseOrdinal] : HardenPhase.NONE);
        }
        if (tag.contains("HardenCooldownTicks")) {
            this.hardenCooldownTicks = Math.min(tag.getInt("HardenCooldownTicks"), Math.max(1, AntarchySettings.emperorScorpionHardenCooldownTicks()));
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
        this.phaseTwo = tag.getBoolean("PhaseTwo");
        this.phaseTransitionTicks = Math.max(0, Math.min(PHASE_TRANSITION_TICKS, tag.getInt("PhaseTransitionTicks")));
        this.encounterActive = tag.contains("EncounterActive") ? tag.getBoolean("EncounterActive") : this.getHealth() < this.getMaxHealth();
        this.leashReturning = tag.getBoolean("LeashReturning");
        this.resetAttackState();
        this.attackRecoveryTicks = 0;
        this.venomPressureTicks = 0;
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
        if (this.leashReturning || !this.isValidEncounterTarget(target)) {
            if (target != null) {
                this.setTarget(null);
            }
            target = null;
        }

        if (target != null) {
            this.encounterActive = true;
            this.disengageTicks = 0;
            this.homeRepathCooldown = 0;
            if (this.isOutsideLeashRadius() && !this.isAttackLocked() && !this.isActionBlocked()) {
                this.leashReturning = true;
                this.setTarget(null);
                this.getNavigation().stop();
                this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 1.5F, 0.8F);
            }
            return;
        }

        if (this.isAttackLocked() || this.isActionBlocked()) {
            return;
        }

        if (!this.encounterActive) {
            this.disengageTicks = 0;
            if (this.distanceToSqr(Vec3.atCenterOf(this.encounterHome)) > IDLE_HOME_DRIFT_RADIUS * IDLE_HOME_DRIFT_RADIUS) {
                this.navigateHome();
            }
            return;
        }

        boolean atHome = this.isAtEncounterHome();
        if (!atHome) {
            this.navigateHome();
        }
        if (!this.leashReturning && this.hasResetBlockingPlayerNearby()) {
            this.disengageTicks = 0;
            return;
        }
        this.disengageTicks++;
        if (this.disengageTicks >= RESET_DELAY_TICKS || (this.leashReturning && atHome)) {
            this.resetEncounter(true);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.leashReturning && this.isValidEncounterTarget(target) && super.canAttack(target);
    }

    private boolean hasResetBlockingPlayerNearby() {
        double rangeSqr = RESET_PARTICIPANT_RADIUS * RESET_PARTICIPANT_RADIUS;
        for (Player player : this.level().players()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative() && player.distanceToSqr(this) <= rangeSqr) {
                return true;
            }
        }
        return false;
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
                || this.phaseTransitionTicks > 0
                || this.encounterActive;
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

    private void navigateHome() {
        if (this.encounterHome == null) {
            return;
        }
        Vec3 homeCenter = Vec3.atCenterOf(this.encounterHome);
        if (this.homeRepathCooldown > 0) {
            this.homeRepathCooldown--;
            return;
        }
        this.homeRepathCooldown = HOME_REPATH_INTERVAL_TICKS;
        this.getNavigation().moveTo(homeCenter.x, homeCenter.y, homeCenter.z, HOME_RETURN_SPEED);
    }

    private void resetEncounter(boolean restoreHealth) {
        this.resetAttackState();
        this.getNavigation().stop();
        this.setTarget(null);
        this.setAggressive(false);
        this.setHardenPhase(HardenPhase.NONE);
        this.hardenCastTicks = 0;
        this.hardenCastTotalTicks = 0;
        this.hardenCastSpawnTicks = 0;
        this.hardenStateTicks = 0;
        this.hardenDurationTicks = 0;
        this.hardenHealTicks = 0;
        this.hardenSummonedScorpions = 0;
        this.disengageTicks = 0;
        this.previousAttack = AttackType.NONE;
        this.phaseTwo = false;
        this.phaseTransitionTicks = 0;
        this.attackRecoveryTicks = 0;
        this.venomCooldownTicks = 0;
        this.venomPressureTicks = 0;
        this.encounterActive = false;
        this.leashReturning = false;
        this.hasTakenDamage = false;
        this.hardenCooldownTicks = this.hardenCooldown();
        this.playReturnEffects();
        if (restoreHealth) {
            this.setHealth(this.getMaxHealth());
        }
        this.discardOwnedScorpions();
        if (this.encounterHome != null && !this.isAtEncounterHome()) {
            Vec3 homeCenter = Vec3.atCenterOf(this.encounterHome);
            this.moveTo(homeCenter.x, homeCenter.y, homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
            this.playReturnEffects();
        }
    }

    private void playReturnEffects() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.HAPPY_VILLAGER), this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    30, this.getBbWidth() * 0.4D, this.getBbHeight() * 0.4D, this.getBbWidth() * 0.4D, 0.02D);
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), this.getX(), this.getY() + 0.5D, this.getZ(),
                    30, this.getBbWidth() * 0.4D, 0.3D, this.getBbWidth() * 0.4D, 0.03D);
        }
        this.playSound(AntarchySoundEvents.EMPEROR_SCORPION_ROAR.get(), 1.5F, 0.6F);
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
        private int directApproachBlockedTicks;
        private boolean pathFallback;
        private boolean pathFailed;

        private EmperorScorpionCombatGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = EmperorScorpionEntity.this.getTarget();
            return target != null && target.isAlive() && !EmperorScorpionEntity.this.isActionBlocked();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = EmperorScorpionEntity.this.getTarget();
            return EmperorScorpionEntity.this.isAttackLocked()
                    || (target != null && target.isAlive() && !EmperorScorpionEntity.this.isActionBlocked());
        }

        @Override
        public void start() {
            EmperorScorpionEntity.this.setAggressive(true);
            this.repathCooldownTicks = 0;
            this.directApproachBlockedTicks = 0;
            this.pathFallback = false;
            this.pathFailed = false;
        }

        @Override
        public void stop() {
            EmperorScorpionEntity.this.setAggressive(false);
            EmperorScorpionEntity.this.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (EmperorScorpionEntity.this.isAttackLocked() || EmperorScorpionEntity.this.isActionBlocked()) {
                return;
            }

            LivingEntity target = EmperorScorpionEntity.this.getTarget();
            if (target == null) {
                return;
            }

            double distSqr = EmperorScorpionEntity.this.distanceToSqr(target);
            boolean lineOfSight = EmperorScorpionEntity.this.hasLineOfSight(target);
            boolean strikeRange = EmperorScorpionEntity.this.isWithinStrikeRange(distSqr);
            this.updateVenomPressure(target, distSqr, strikeRange);

            if (lineOfSight && EmperorScorpionEntity.this.canFireVenom(distSqr)) {
                EmperorScorpionEntity.this.haltHorizontalMovement();
                EmperorScorpionEntity.this.beginAttack(target, AttackType.VENOM);
                return;
            }

            if (strikeRange && lineOfSight) {
                this.pathFailed = false;
                this.pathFallback = false;
                this.directApproachBlockedTicks = 0;
                EmperorScorpionEntity.this.haltHorizontalMovement();
                EmperorScorpionEntity.this.faceTarget(target, MELEE_TURN_RATE);
                if (EmperorScorpionEntity.this.attackRecoveryTicks <= 0) {
                    AttackType attackType = EmperorScorpionEntity.this.selectAttack(distSqr);
                    if (attackType != AttackType.NONE) {
                        EmperorScorpionEntity.this.beginAttack(target, attackType);
                    }
                }
                return;
            }

            if (this.pathFallback) {
                if (this.directApproachBlockedTicks > 0) {
                    this.directApproachBlockedTicks--;
                }
                if (this.directApproachBlockedTicks > 0 || (lineOfSight && !EmperorScorpionEntity.this.getNavigation().isDone())) {
                    this.tryRepath(target);
                    return;
                }
                this.pathFallback = false;
            }

            if (distSqr <= DIRECT_APPROACH_RANGE * DIRECT_APPROACH_RANGE && lineOfSight) {
                if (EmperorScorpionEntity.this.horizontalCollision) {
                    this.pathFallback = true;
                    this.directApproachBlockedTicks = DIRECT_APPROACH_BLOCKED_TICKS;
                    this.repathCooldownTicks = 0;
                    this.tryRepath(target);
                    return;
                }
                this.pathFailed = false;
                EmperorScorpionEntity.this.getNavigation().stop();
                this.approachDirectly(target);
                return;
            }

            this.tryRepath(target);
        }

        private void updateVenomPressure(LivingEntity target, double distSqr, boolean strikeRange) {
            GravityDirection direction = GravityApi.getGravityDirection(EmperorScorpionEntity.this);
            double localDeltaY = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(EmperorScorpionEntity.this.position()), direction).y;
            boolean elevated = Math.abs(localDeltaY) > VENOM_ELEVATION_THRESHOLD && distSqr > CLAW_REACH * CLAW_REACH;
            boolean unreachable = !strikeRange && (elevated || this.pathFailed || this.pathFallback);
            EmperorScorpionEntity.this.venomPressureTicks = unreachable
                    ? Math.min(VENOM_PRESSURE_MAX_TICKS, EmperorScorpionEntity.this.venomPressureTicks + 1)
                    : Math.max(0, EmperorScorpionEntity.this.venomPressureTicks - 2);
        }

        private void approachDirectly(LivingEntity target) {
            GravityDirection direction = GravityApi.getGravityDirection(EmperorScorpionEntity.this);
            Vec3 localDelta = GravityRotationUtil.vecWorldToPlayer(target.position().subtract(EmperorScorpionEntity.this.position()), direction);
            double dx = localDelta.x;
            double dz = localDelta.z;
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

            Vec3 currentMotion = GravityRotationUtil.vecWorldToPlayer(EmperorScorpionEntity.this.getDeltaMovement(), direction);
            float headingError = Math.abs(Mth.wrapDegrees(targetYaw - currentYaw));
            if (headingError <= DIRECT_APPROACH_ALIGN_THRESHOLD) {
                double speed = EmperorScorpionEntity.this.getAttributeValue(Attributes.MOVEMENT_SPEED) * CHASE_SPEED * DIRECT_APPROACH_SPEED_SCALE;
                Vec3 forward = Vec3.directionFromRotation(0.0F, newYaw).scale(speed);
                Vec3 worldForward = GravityRotationUtil.vecPlayerToWorld(forward.x, currentMotion.y, forward.z, direction);
                EmperorScorpionEntity.this.setDeltaMovement(worldForward);
            } else {
                EmperorScorpionEntity.this.setDeltaMovement(GravityRotationUtil.vecPlayerToWorld(0.0D, currentMotion.y, 0.0D, direction));
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
            boolean started = EmperorScorpionEntity.this.getNavigation().moveTo(target, CHASE_SPEED);
            if (!started) {
                this.repathCooldownTicks += 15;
            }
            Path path = EmperorScorpionEntity.this.getNavigation().getPath();
            this.pathFailed = !started || path == null || !path.canReach();
        }
    }

    private boolean isActionBlocked() {
        return this.isHardenSequenceActive() || this.phaseTransitionTicks > 0;
    }

    private boolean isWithinStrikeRange(double distSqr) {
        return distSqr <= CLAW_REACH * CLAW_REACH
                || (this.stingCooldownTicks <= 0 && distSqr <= STING_REACH * STING_REACH);
    }

    private boolean canFireVenom(double distSqr) {
        return this.venomPressureTicks >= VENOM_PRESSURE_THRESHOLD_TICKS
                && this.venomCooldownTicks <= 0
                && this.attackRecoveryTicks <= 0
                && distSqr <= VENOM_MAX_RANGE * VENOM_MAX_RANGE;
    }

    @Override
    public boolean isWithinMeleeAttackRange(LivingEntity target) {
        return this.distanceToSqr(target) <= STING_REACH * STING_REACH;
    }

    private AttackType selectAttack(double distSqr) {
        boolean canClaw = this.clawCooldownTicks <= 0 && distSqr <= CLAW_REACH * CLAW_REACH;
        boolean canSting = this.stingCooldownTicks <= 0 && distSqr <= STING_REACH * STING_REACH;
        if (canClaw && canSting) {
            float stingChance = this.previousAttack == AttackType.CLAW ? 0.6F : this.previousAttack == AttackType.STING ? 0.2F : 0.35F;
            return this.random.nextFloat() < stingChance ? AttackType.STING : AttackType.CLAW;
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
        STING,
        VENOM;

        private int animTicks() {
            return switch (this) {
                case CLAW -> AntarchySettings.emperorScorpionClawAnimTicks();
                case STING -> AntarchySettings.emperorScorpionStingAnimTicks();
                case VENOM -> AntarchySettings.emperorScorpionStingAnimTicks() + VENOM_AIM_TICKS;
                default -> 0;
            };
        }

        private int hitTick() {
            return switch (this) {
                case CLAW -> Mth.clamp(AntarchySettings.emperorScorpionClawHitTick(), 1, Math.max(1, AntarchySettings.emperorScorpionClawAnimTicks()));
                case STING, VENOM -> Mth.clamp(AntarchySettings.emperorScorpionStingHitTick(), 1, Math.max(1, AntarchySettings.emperorScorpionStingAnimTicks()));
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
