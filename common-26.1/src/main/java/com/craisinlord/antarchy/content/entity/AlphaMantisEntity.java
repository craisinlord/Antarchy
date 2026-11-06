package com.craisinlord.antarchy.content.entity;

import java.util.Optional;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchySoundEvents;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.antarchy.content.glimmerecho.GlimmerEchoes;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AlphaMantisEntity extends MantisEntity {
    private static final double ALPHA_ATTACK_START_RANGE = 5.5D;
    private static final double ALPHA_ATTACK_HIT_REACH = 6.5D;
    private static final double ALPHA_ATTACK_HALF_WIDTH = 2.5D;
    private static final double ALPHA_ATTACK_VERTICAL_TOLERANCE = 4.25D;
    private static final int PHASE_TRANSITION_TICKS = 35;
    private static final double ENCOUNTER_HOME_RADIUS = 40.0D;
    private static final double ENCOUNTER_LEASH_RADIUS = 96.0D;
    private static final double FORCED_RETURN_RADIUS = 144.0D;
    private static final int RESET_DELAY_TICKS = 300;
    private static final int RETURN_TIMEOUT_TICKS = 300;
    private static final int UNSEEN_MEMORY_TICKS = 300;
    private static final double PARTICIPANT_RANGE = 48.0D;
    private static final double HOME_RETURN_SPEED = 1.05D;
    private static final int HOME_REPATH_INTERVAL_TICKS = 20;
    private static final int BASE_XP_REWARD = 60;
    private static final int GLIMMER_XP_REWARD = 300;
    private static final int GLIMMER_EXTRA_MINIONS_PER_WAVE = 1;
    private static final int GLIMMER_EXTRA_MAX_MINIONS = 2;
    private static final double GLIMMER_PULSE_RADIUS = 8.0D;
    private static final float GLIMMER_PULSE_DAMAGE_FRACTION = 0.5F;
    private static final double GLIMMER_ABANDON_RANGE = 48.0D;
    private static final int GLIMMER_ABANDON_TICKS = 600;
    private static final double GATE_SHOVE_RADIUS = 6.0D;
    private static final double RANGE_FALLOFF_MAX_RATIO = 80.0D / 48.0D;
    private static final int RANGE_FEEDBACK_INTERVAL_TICKS = 20;

    private static final double CHASE_SPEED_PHASE_ONE = 0.79D;
    private static final double CHASE_SPEED_PHASE_TWO = 0.89D;
    private static final double STALK_SPEED = 0.55D;
    private static final double STALK_RADIUS = 7.5D;
    private static final double STALK_MAX_HORIZONTAL_RANGE = 10.0D;
    private static final int STALK_MIN_TICKS = 20;
    private static final int STALK_RANDOM_TICKS = 21;
    private static final int STALK_REPATH_TICKS = 10;
    private static final double STALK_ANGLE_STEP = 0.45D;
    private static final double PHASE_TWO_TEMPO = 0.75D;

    private static final int STRIKE_COOLDOWN_TICKS = 40;
    private static final int STRIKE_RECOVERY_TICKS = 16;
    private static final int STRIKE_COMMIT_REMAINING_TICKS = 16;
    private static final float STRIKE_TURN_RATE = 18.0F;

    private static final int FEINT_COOLDOWN_TICKS = 120;
    private static final int FEINT_RAISE_TICKS = 5;
    private static final int FEINT_HOP_TICKS = 5;
    private static final int FEINT_RECOVERY_TICKS = 10;
    private static final float FEINT_DAMAGE_MULTIPLIER = 1.1F;
    private static final int FEINT_SHIELD_DISABLE_TICKS = 40;
    private static final double FEINT_HOP_SPEED = 0.8D;
    private static final double FEINT_HOP_CLEARANCE = 2.5D;

    private static final int POUNCE_COOLDOWN_TICKS = 120;
    private static final int POUNCE_BLOCKED_RETRY_TICKS = 20;
    private static final int POUNCE_LOCK_TICK = 10;
    private static final int POUNCE_WINDUP_TICKS = 16;
    private static final double POUNCE_MIN_RANGE = 8.0D;
    private static final double POUNCE_MAX_RANGE = 20.0D;
    private static final double POUNCE_MAX_RISE = 12.0D;
    private static final double POUNCE_MAX_DROP = 8.0D;
    private static final double POUNCE_LEAP_SPEED = 1.1D;
    private static final int POUNCE_MIN_LEAP_TICKS = 6;
    private static final int POUNCE_MAX_LEAP_TICKS = 24;
    private static final int POUNCE_OVERSHOOT_TICKS = 10;
    private static final double POUNCE_MAX_STEP = 2.5D;
    private static final double POUNCE_CONTACT_INFLATE = 0.5D;
    private static final double POUNCE_WALL_CONTACT_INFLATE = 1.5D;
    private static final float POUNCE_DAMAGE_MULTIPLIER = 1.4F;
    private static final int POUNCE_RECOVERY_TICKS = 30;
    private static final int POUNCE_WALL_RECOVERY_TICKS = 50;
    private static final double PILLAR_HEIGHT = 6.0D;
    private static final int PILLAR_RESPONSE_TICKS = 60;
    private static final double MINION_PINCER_RANGE = 4.0D;

    private static final int CATCH_COOLDOWN_TICKS = 200;
    private static final int CATCH_HOLD_TICKS = 30;
    private static final int CATCH_MISS_RECOVERY_TICKS = 20;
    private static final int CATCH_FLANKED_RECOVERY_TICKS = 30;
    private static final int CATCH_SNAP_RECOVERY_TICKS = 15;
    private static final int CATCH_SNAP_COMMIT_REMAINING_TICKS = 18;
    private static final float CATCH_FRONT_DAMAGE_MULTIPLIER = 0.5F;
    private static final float CATCH_FLANK_DAMAGE_MULTIPLIER = 1.25F;
    private static final float CATCH_SNAP_DAMAGE_MULTIPLIER = 1.2F;
    private static final int CATCH_SNAP_SLOWNESS_TICKS = 30;
    private static final double CATCH_FRONT_ARC_DOT = 0.7071D;
    private static final double CATCH_SNAP_MAX_RANGE = 8.0D;
    private static final float CATCH_TURN_RATE = 6.0F;
    private static final int CATCH_MELEE_WINDOW_TICKS = 60;
    private static final int CATCH_SHIELD_TICKS = 20;
    private static final double CATCH_TRIGGER_RANGE = 8.0D;

    private static final int CALL_TELEGRAPH_TICKS = 25;
    private static final int CALL_RECOVERY_TICKS = 35;
    private static final int CALL_MAX_ALIVE_MINIONS = 1;
    private static final int CALL_PHASE_ONE_CAP = 3;
    private static final int MINION_SPAWN_ATTEMPTS = 8;
    private static final double MINION_SPAWN_MIN_DISTANCE = 4.0D;
    private static final double MINION_SPAWN_RANDOM_DISTANCE = 3.0D;
    private static final double OWNED_MINION_SCAN_RANGE = 64.0D;
    private static final int MINION_TOKEN_TICKS = 60;

    private static final float KNOCKBACK_STRENGTH = 0.5F;

    private static final net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> GLIMMER_LOOT_TABLE = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE,
            net.minecraft.resources.Identifier.fromNamespaceAndPath(com.craisinlord.antarchy.Antarchy.MODID, "entities/alpha_mantis_glimmer"));

    private enum AlphaAction {
        NONE,
        STRIKE,
        FEINT_RAISE,
        FEINT_HOP,
        FEINT_STRIKE,
        POUNCE_WINDUP,
        POUNCE_LEAP,
        CATCH,
        CATCH_SNAP,
        CALL,
        RECOVERY
    }

    private final ServerBossEvent bossEvent =
            new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this, Component.translatable("entity.antarchy.alpha_mantis"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    private int summonCooldownTicks;
    private int phaseTransitionTicks;
    private int disengageTicks;
    private int homeRepathCooldown;
    private boolean halfHealthTransitionComplete;
    private int glimmerAbandonTicks;
    @Nullable
    private BlockPos encounterHome;
    private boolean encounterActive;
    private boolean returningHome;
    private int returnTicks;
    private boolean gateHitActive;
    private boolean gateCrossed;
    private int lastRangeFeedbackTick;

    private AlphaAction action = AlphaAction.NONE;
    private AlphaAction previousAttack = AlphaAction.NONE;
    private int actionTicks;
    private int recoveryTicks;
    private int strikeCooldownTicks;
    private int feintCooldownTicks;
    private int pounceCooldownTicks;
    private int catchCooldownTicks;
    private int stalkTicks;
    private int stalkRepathTicks;
    private int chaseRepathTicks;
    private double stalkAngle;
    private int stalkSide = 1;
    private int pillarTicks;
    private int shieldTicks;
    private int previousMeleeHitTick = Integer.MIN_VALUE / 2;
    private int olderMeleeHitTick = Integer.MIN_VALUE / 2;
    private boolean catchReady;
    private boolean catchFlanked;
    @Nullable
    private LivingEntity pendingSnapTarget;
    @Nullable
    private UUID minionTokenHolder;
    private int minionTokenExpireTick;

    private Vec3 pounceStart = Vec3.ZERO;
    private Vec3 pounceEnd = Vec3.ZERO;
    private Vec3 pounceDirection = Vec3.ZERO;
    private int pounceDuration;
    private double pounceArc;
    private final Set<UUID> pounceHits = new HashSet<>();

    public AlphaMantisEntity(EntityType<? extends AlphaMantisEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = BASE_XP_REWARD;
        this.summonCooldownTicks = AntarchySettings.alphaMantisSummonIntervalTicks() / 2;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, AntarchySettings.alphaMantisHealth())
                .add(Attributes.ATTACK_DAMAGE, AntarchySettings.alphaMantisAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, AntarchySettings.alphaMantisMovementSpeed())
                .add(Attributes.FLYING_SPEED, AntarchySettings.alphaMantisFlyingSpeed())
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.ARMOR_TOUGHNESS, AntarchySettings.alphaMantisArmorToughness())
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        if (this.phaseTransitionTicks > 0 && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }
        return super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean bypass = source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        if (!bypass) {
            float falloff = this.rangeFalloff(source);
            if (falloff < 1.0F) {
                this.playRangeResistFeedback();
                if (falloff <= 0.0F) {
                    return false;
                }
                amount *= falloff;
            }
        }
        boolean catching = !bypass && this.action == AlphaAction.CATCH;
        float catchMultiplier = catching ? this.catchDamageMultiplier(source) : 1.0F;
        amount *= catchMultiplier;
        this.gateHitActive = !bypass && !this.halfHealthTransitionComplete;
        boolean hurt;
        try {
            hurt = super.hurtServer(level, source, amount);
        } finally {
            this.gateHitActive = false;
        }
        if (hurt && catching) {
            this.applyCatchOutcome(source, catchMultiplier);
        }
        if (this.gateCrossed) {
            this.gateCrossed = false;
            this.beginHalfHealthTransition();
        }
        if (hurt && !bypass) {
            this.recordMeleeHit(source);
        }
        return hurt;
    }

    @Override
    public void setHealth(float health) {
        if (this.gateHitActive) {
            float halfHealth = this.getMaxHealth() * 0.5F;
            if (this.getHealth() > halfHealth && health <= halfHealth) {
                health = halfHealth;
                this.gateCrossed = true;
            }
        }
        super.setHealth(health);
    }

    @Override
    public void die(DamageSource damageSource) {
        this.clearAction();
        super.die(damageSource);
    }

    private float rangeFalloff(DamageSource source) {
        double fullRange = AntarchySettings.alphaMantisDamageRange();
        Entity attacker = source.getEntity();
        if (fullRange <= 0.0D || attacker == null) {
            return 1.0F;
        }
        double distance = attacker.distanceTo(this);
        if (distance <= fullRange) {
            return 1.0F;
        }
        double maxRange = fullRange * RANGE_FALLOFF_MAX_RATIO;
        if (distance >= maxRange) {
            return 0.0F;
        }
        return (float) ((maxRange - distance) / (maxRange - fullRange));
    }

    private void playRangeResistFeedback() {
        if (this.tickCount - this.lastRangeFeedbackTick < RANGE_FEEDBACK_INTERVAL_TICKS) {
            return;
        }
        this.lastRangeFeedbackTick = this.tickCount;
        this.playSound(SoundEvents.SHIELD_BLOCK.value(), 1.5F, 0.6F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.ENCHANTED_HIT), this.getX(), this.getY() + this.getBbHeight() * 0.6D, this.getZ(),
                    10, this.getBbWidth() * 0.4D, this.getBbHeight() * 0.3D, this.getBbWidth() * 0.4D, 0.1D);
        }
    }

    private float catchDamageMultiplier(DamageSource source) {
        Vec3 sourcePosition = source.getSourcePosition();
        if (sourcePosition == null) {
            return 1.0F;
        }
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        Vec3 toSource = new Vec3(sourcePosition.x - this.getX(), 0.0D, sourcePosition.z - this.getZ());
        if (toSource.lengthSqr() < 1.0E-4D) {
            return 1.0F;
        }
        return forward.dot(toSource.normalize()) < CATCH_FRONT_ARC_DOT ? CATCH_FLANK_DAMAGE_MULTIPLIER : CATCH_FRONT_DAMAGE_MULTIPLIER;
    }

    private void applyCatchOutcome(DamageSource source, float multiplier) {
        if (multiplier == CATCH_FLANK_DAMAGE_MULTIPLIER) {
            this.catchFlanked = true;
        } else if (multiplier == CATCH_FRONT_DAMAGE_MULTIPLIER) {
            this.playSound(SoundEvents.SHIELD_BLOCK.value(), 1.5F, 1.4F);
            if (source.getEntity() instanceof LivingEntity attacker && attacker.distanceToSqr(this) <= CATCH_SNAP_MAX_RANGE * CATCH_SNAP_MAX_RANGE) {
                this.pendingSnapTarget = attacker;
            }
        }
    }

    public boolean isEncounterActive() {
        return this.encounterActive;
    }

    private void recordMeleeHit(DamageSource source) {
        Entity attacker = source.getEntity();
        if (attacker == null || source.getDirectEntity() != attacker || attacker.distanceToSqr(this) > CATCH_TRIGGER_RANGE * CATCH_TRIGGER_RANGE) {
            return;
        }
        if (this.tickCount - this.olderMeleeHitTick <= CATCH_MELEE_WINDOW_TICKS) {
            this.catchReady = true;
        }
        this.olderMeleeHitTick = this.previousMeleeHitTick;
        this.previousMeleeHitTick = this.tickCount;
    }

    public static boolean checkAlphaMantisSpawnRules(EntityType<AlphaMantisEntity> entityType, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == EntitySpawnReason.SPAWN_ITEM_USE
                || spawnReason == EntitySpawnReason.SPAWNER
                || spawnReason == EntitySpawnReason.TRIAL_SPAWNER
                || spawnReason == EntitySpawnReason.COMMAND) {
            return true;
        }

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (spawnReason != EntitySpawnReason.NATURAL) {
            return false;
        }

        boolean atSurface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= pos.getY();
        boolean hasFloorClearance = level.isEmptyBlock(pos.above());
        boolean hasCeilingClearance = level.isEmptyBlock(pos.below());
        boolean floorSupport = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && !(level.getBlockState(pos.below()).getBlock() instanceof LeavesBlock);
        boolean ceilingSupport = level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN)
                && !(level.getBlockState(pos.above()).getBlock() instanceof LeavesBlock);
        boolean posOpen = level.isEmptyBlock(pos) && (hasFloorClearance || hasCeilingClearance) && level.getFluidState(pos).isEmpty();
        boolean onSolidGround = floorSupport || ceilingSupport;

        if (!posOpen || !onSolidGround || !atSurface) {
            return false;
        }

        if (!level.getLevel().getEntitiesOfClass(AlphaMantisEntity.class, new AABB(pos).inflate(128.0D), Entity::isAlive).isEmpty()) {
            return false;
        }

        boolean ignoreLightLevel = AntarchySettings.mantisIgnoreLightLevel();

        if (level.getLevel().isBrightOutside() && level.getBiome(pos).is(AntarchyTags.Biomes.MANTIS_OVERWORLD_SPAWN_BIOMES)) {
            return level.canSeeSky(pos);
        }

        if (level.getLevel().isDarkOutside() && level.getBiome(pos).is(AntarchyTags.Biomes.MANTIS_SPAWN_BIOMES)) {
            return ignoreLightLevel || Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
        }

        return false;
    }

    @Override
    protected Goal createCombatGoal() {
        return new AlphaCombatGoal();
    }

    @Override
    protected int targetUnseenMemoryTicks() {
        return UNSEEN_MEMORY_TICKS;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !this.returningHome && super.canAttack(target);
    }

    @Override
    protected double getCombatSpeed() {
        return this.halfHealthTransitionComplete ? CHASE_SPEED_PHASE_TWO : CHASE_SPEED_PHASE_ONE;
    }

    @Override
    protected double getAttackStartRange() {
        return ALPHA_ATTACK_START_RANGE;
    }

    @Override
    protected double getAttackHitReach() {
        return ALPHA_ATTACK_HIT_REACH;
    }

    @Override
    protected double getAttackHalfWidth() {
        return ALPHA_ATTACK_HALF_WIDTH;
    }

    @Override
    protected double getAttackVerticalTolerance() {
        return ALPHA_ATTACK_VERTICAL_TOLERANCE;
    }

    @Override
    protected boolean canBeginCommittedAttack(LivingEntity target) {
        return this.phaseTransitionTicks <= 0 && this.action == AlphaAction.NONE && super.canBeginCommittedAttack(target);
    }

    @Override
    protected int committedAttackCooldownTicks() {
        return 0;
    }

    @Override
    protected int attackCommitRemainingTicks() {
        return this.action == AlphaAction.CATCH_SNAP ? CATCH_SNAP_COMMIT_REMAINING_TICKS : STRIKE_COMMIT_REMAINING_TICKS;
    }

    @Override
    protected boolean isHoldingAirborne() {
        return this.action == AlphaAction.POUNCE_LEAP;
    }

    @Override
    protected int animationStateOverride() {
        if (this.phaseTransitionTicks > 0) {
            return ANIM_IDLE;
        }
        return switch (this.action) {
            case FEINT_RAISE -> ANIM_ATTACK;
            case POUNCE_WINDUP -> ANIM_FLY;
            case POUNCE_LEAP -> ANIM_FLY_ATTACK;
            case CATCH -> ANIM_CATCH;
            case FEINT_HOP, CALL, RECOVERY -> ANIM_IDLE;
            default -> -1;
        };
    }

    @Override
    protected boolean applyCommittedAttackDamage(LivingEntity target) {
        float multiplier = switch (this.action) {
            case FEINT_STRIKE -> FEINT_DAMAGE_MULTIPLIER;
            case CATCH_SNAP -> CATCH_SNAP_DAMAGE_MULTIPLIER;
            default -> 1.0F;
        };
        if (this.action == AlphaAction.FEINT_STRIKE && target instanceof Player player && player.isBlocking()) {
            player.getCooldowns().addCooldown(player.getUseItem(), FEINT_SHIELD_DISABLE_TICKS);
            player.stopUsingItem();
            this.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BREAK, player.getSoundSource(), 0.8F, 0.8F + this.random.nextFloat() * 0.4F);
        }
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * multiplier;
        boolean hit = this.level() instanceof ServerLevel serverLevel && target.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage);
        if (hit) {
            this.setLastHurtMob(target);
            float yawRadians = this.committedAttackYaw * Mth.DEG_TO_RAD;
            target.knockback(KNOCKBACK_STRENGTH, Mth.sin(yawRadians), -Mth.cos(yawRadians));
            if (this.action == AlphaAction.CATCH_SNAP) {
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, CATCH_SNAP_SLOWNESS_TICKS, 1), this);
            }
        }
        return hit;
    }

    @Override
    protected double configuredMaxHealth() {
        double health = AntarchySettings.alphaMantisHealth();
        return this.isGlimmerEcho() ? health * AntarchySettings.glimmerAlphaMantisHealthMultiplier() : health;
    }

    @Override
    protected double configuredAttackDamage() {
        return AntarchySettings.alphaMantisAttackDamage() * this.glimmerEchoDamageMultiplier();
    }

    @Override
    public void setGlimmerEcho(boolean glimmerEcho) {
        super.setGlimmerEcho(glimmerEcho);
        this.xpReward = glimmerEcho ? GLIMMER_XP_REWARD : BASE_XP_REWARD;
        this.bossEvent.setName(this.getDisplayName());
        this.bossEvent.setColor(glimmerEcho ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.GREEN);
        if (glimmerEcho) {
            this.summonCooldownTicks = Math.min(this.summonCooldownTicks, this.summonIntervalTicks() / 2);
        }
    }

    @Override
    public float glimmerEchoDamageMultiplier() {
        return this.isGlimmerEcho() ? (float) AntarchySettings.glimmerAlphaMantisDamageMultiplier() : 1.0F;
    }

    @Override
    protected Component getTypeName() {
        return this.isGlimmerEcho() ? Component.translatable("entity.antarchy.alpha_mantis.glimmer") : super.getTypeName();
    }

    @Override
    protected void dropFromLootTable(ServerLevel level, DamageSource damageSource, boolean causedByPlayer) {
        if (this.isGlimmerEcho()) {
            this.dropFromLootTable(level, damageSource, causedByPlayer, GLIMMER_LOOT_TABLE);
            return;
        }
        super.dropFromLootTable(level, damageSource, causedByPlayer);
    }

    private int summonIntervalTicks() {
        int interval = AntarchySettings.alphaMantisSummonIntervalTicks();
        if (!this.isGlimmerEcho()) {
            return interval;
        }
        return Math.min(interval, Math.max(20, (int) Math.round(interval * AntarchySettings.glimmerAlphaMantisCooldownMultiplier())));
    }

    private int scaledCooldown(int baseTicks) {
        double scaled = baseTicks;
        if (this.halfHealthTransitionComplete) {
            scaled *= PHASE_TWO_TEMPO;
        }
        if (this.isGlimmerEcho()) {
            scaled *= AntarchySettings.glimmerAlphaMantisCooldownMultiplier();
        }
        return Math.max(1, (int) Math.round(scaled));
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

    private void applyGlimmerPulse() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB pulseArea = this.getBoundingBox().inflate(GLIMMER_PULSE_RADIUS, 2.0D, GLIMMER_PULSE_RADIUS);
        List<Player> victims = serverLevel.getEntitiesOfClass(Player.class, pulseArea,
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
                        && this.horizontalDistanceSqrTo(player) <= GLIMMER_PULSE_RADIUS * GLIMMER_PULSE_RADIUS);
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * GLIMMER_PULSE_DAMAGE_FRACTION;
        for (Player victim : victims) {
            if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage)) {
                Vec3 knockDir = victim.position().subtract(this.position()).normalize();
                victim.push(knockDir.x * 1.6D, 0.5D, knockDir.z * 1.6D);
                victim.hurtMarked = true;
            }
        }
        GlimmerEchoes.ring(serverLevel, this.position(), GLIMMER_PULSE_RADIUS, 48);
        this.playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 2.0F, 0.7F);
    }

    private void applyGateShove() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB shoveArea = this.getBoundingBox().inflate(GATE_SHOVE_RADIUS, 2.0D, GATE_SHOVE_RADIUS);
        List<Player> victims = serverLevel.getEntitiesOfClass(Player.class, shoveArea,
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
                        && this.horizontalDistanceSqrTo(player) <= GATE_SHOVE_RADIUS * GATE_SHOVE_RADIUS);
        for (Player victim : victims) {
            Vec3 knockDir = new Vec3(victim.getX() - this.getX(), 0.0D, victim.getZ() - this.getZ());
            if (knockDir.lengthSqr() > 1.0E-4D) {
                knockDir = knockDir.normalize();
            }
            victim.push(knockDir.x * 1.4D, 0.45D, knockDir.z * 1.4D);
            victim.hurtMarked = true;
        }
        this.sendGateRing(serverLevel, GATE_SHOVE_RADIUS);
        this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 3.0F, 0.5F);
    }

    private void sendGateRing(ServerLevel serverLevel, double radius) {
        int segments = 8;
        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2.0D / segments;
            serverLevel.sendParticles(ParticleTypes.POOF, this.getX() + Math.cos(angle) * radius, this.getY() + 0.2D,
                    this.getZ() + Math.sin(angle) * radius, 3, 0.4D, 0.1D, 0.4D, 0.01D);
        }
    }

    private double horizontalDistanceSqrTo(Entity other) {
        double dx = other.getX() - this.getX();
        double dz = other.getZ() - this.getZ();
        return dx * dx + dz * dz;
    }

    @Override
    protected double configuredMovementSpeed() {
        return AntarchySettings.alphaMantisMovementSpeed();
    }

    @Override
    protected double configuredFlyingSpeed() {
        return AntarchySettings.alphaMantisFlyingSpeed();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        if (this.isDeadOrDying()) {
            this.bossEvent.setProgress(0.0F);
            return;
        }

        if (this.isGlimmerEcho() && this.tickGlimmerAbandonment()) {
            return;
        }

        this.tickEncounterState();
        this.tickPhaseTransition();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setVisible(this.shouldShowBossBar());
        this.tickCooldowns();
        if (this.phaseTransitionTicks <= 0) {
            this.tickAction();
        }
    }

    private void tickCooldowns() {
        if (this.strikeCooldownTicks > 0) {
            this.strikeCooldownTicks--;
        }
        if (this.feintCooldownTicks > 0) {
            this.feintCooldownTicks--;
        }
        if (this.pounceCooldownTicks > 0) {
            this.pounceCooldownTicks--;
        }
        if (this.catchCooldownTicks > 0) {
            this.catchCooldownTicks--;
        }
        if (this.summonCooldownTicks > 0 && this.getTarget() != null) {
            this.summonCooldownTicks--;
        }
    }

    private void tickAction() {
        switch (this.action) {
            case STRIKE -> this.tickBaseStrike(STRIKE_RECOVERY_TICKS);
            case FEINT_STRIKE -> this.tickBaseStrike(FEINT_RECOVERY_TICKS);
            case CATCH_SNAP -> this.tickBaseStrike(CATCH_SNAP_RECOVERY_TICKS);
            case FEINT_RAISE -> this.tickFeintRaise();
            case FEINT_HOP -> this.tickFeintHop();
            case POUNCE_WINDUP -> this.tickPounceWindup();
            case POUNCE_LEAP -> this.tickPounceLeap();
            case CATCH -> this.tickCatch();
            case CALL -> this.tickCall();
            case RECOVERY -> this.tickRecovery();
            default -> {
            }
        }
    }

    private void tickBaseStrike(int recovery) {
        if (!this.isAttackLocked()) {
            this.enterRecovery(recovery);
        }
    }

    private void enterRecovery(int ticks) {
        this.action = AlphaAction.RECOVERY;
        this.actionTicks = 0;
        this.recoveryTicks = ticks;
        this.getNavigation().stop();
    }

    private void tickRecovery() {
        this.getNavigation().stop();
        if (--this.recoveryTicks > 0) {
            return;
        }
        this.action = AlphaAction.NONE;
        this.beginStalk();
    }

    private void beginStalk() {
        int ticks = STALK_MIN_TICKS + this.random.nextInt(STALK_RANDOM_TICKS);
        this.stalkTicks = this.halfHealthTransitionComplete ? (int) Math.round(ticks * PHASE_TWO_TEMPO) : ticks;
        this.stalkRepathTicks = 0;
        this.stalkSide = this.random.nextBoolean() ? 1 : -1;
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.stalkAngle = Math.atan2(this.getZ() - target.getZ(), this.getX() - target.getX());
        }
    }

    private void clearAction() {
        if (this.action == AlphaAction.POUNCE_LEAP) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.2D, 0.0D, 0.2D));
        }
        this.action = AlphaAction.NONE;
        this.actionTicks = 0;
        this.recoveryTicks = 0;
        this.pendingSnapTarget = null;
        this.catchFlanked = false;
        this.pounceHits.clear();
    }

    private void tickCombatDecision() {
        if (this.action != AlphaAction.NONE || this.isAttackLocked() || this.phaseTransitionTicks > 0 || this.returningHome) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        this.updatePressureTrackers(target);
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        boolean urgent = (this.catchReady && this.catchCooldownTicks <= 0)
                || (this.pillarTicks >= PILLAR_RESPONSE_TICKS && this.pounceCooldownTicks <= 0);
        if (this.stalkTicks > 0 && !urgent) {
            this.stalkTicks--;
            this.moveWhileStalking(target);
            return;
        }

        AlphaAction chosen = this.chooseAttack(target);
        if (chosen != AlphaAction.NONE && this.startAttack(chosen, target)) {
            return;
        }
        this.chaseTarget(target);
    }

    private void updatePressureTrackers(LivingEntity target) {
        if (target.getY() - this.getY() >= PILLAR_HEIGHT) {
            this.pillarTicks++;
        } else {
            this.pillarTicks = 0;
        }
        if (target instanceof Player player && player.isBlocking() && this.distanceToSqr(player) <= CATCH_TRIGGER_RANGE * CATCH_TRIGGER_RANGE) {
            this.shieldTicks++;
            if (this.shieldTicks >= CATCH_SHIELD_TICKS) {
                this.catchReady = true;
            }
        } else {
            this.shieldTicks = 0;
        }
    }

    private void moveWhileStalking(LivingEntity target) {
        if (this.horizontalDistanceSqrTo(target) > STALK_MAX_HORIZONTAL_RANGE * STALK_MAX_HORIZONTAL_RANGE) {
            this.chaseTarget(target);
            return;
        }
        if (--this.stalkRepathTicks > 0) {
            return;
        }
        this.stalkRepathTicks = STALK_REPATH_TICKS;
        this.stalkAngle += this.stalkSide * STALK_ANGLE_STEP;
        boolean started = this.getNavigation().moveTo(
                target.getX() + Math.cos(this.stalkAngle) * STALK_RADIUS,
                target.getY(),
                target.getZ() + Math.sin(this.stalkAngle) * STALK_RADIUS,
                STALK_SPEED);
        if (!started) {
            this.stalkSide = -this.stalkSide;
        }
    }

    private void chaseTarget(LivingEntity target) {
        if (--this.chaseRepathTicks > 0) {
            return;
        }
        this.chaseRepathTicks = 4 + this.random.nextInt(5);
        this.getNavigation().moveTo(target, this.getCombatSpeed());
    }

    private AlphaAction chooseAttack(LivingEntity target) {
        double dy = target.getY() - this.getY();
        double horizontal = Math.sqrt(this.horizontalDistanceSqrTo(target));
        boolean inStrikeRange = this.distanceToSqr(target) <= ALPHA_ATTACK_START_RANGE * ALPHA_ATTACK_START_RANGE
                && Math.abs(dy) <= ALPHA_ATTACK_VERTICAL_TOLERANCE;
        boolean forcedPounce = this.pillarTicks >= PILLAR_RESPONSE_TICKS && dy <= POUNCE_MAX_RISE;
        boolean pounceInRange = (horizontal >= POUNCE_MIN_RANGE && horizontal <= POUNCE_MAX_RANGE && dy <= POUNCE_MAX_RISE && dy >= -POUNCE_MAX_DROP) || forcedPounce;
        boolean callReady = this.summonCooldownTicks <= 0 && this.tickCount % 20 == 0 && this.canCallColony();
        boolean catchPossible = this.catchReady && this.catchCooldownTicks <= 0 && this.distanceToSqr(target) <= CATCH_TRIGGER_RANGE * CATCH_TRIGGER_RANGE;
        boolean anyInReach = inStrikeRange || (pounceInRange && this.pounceCooldownTicks <= 0) || catchPossible;
        if (!anyInReach && !callReady) {
            return AlphaAction.NONE;
        }
        boolean visible = this.hasLineOfSight(target);

        int strikeWeight = inStrikeRange && visible && this.strikeCooldownTicks <= 0 ? 6 : 0;
        int feintWeight = inStrikeRange && visible && this.feintCooldownTicks <= 0 && this.halfHealthTransitionComplete ? 3 : 0;
        int pounceWeight = 0;
        if (pounceInRange && visible && this.pounceCooldownTicks <= 0) {
            pounceWeight = forcedPounce ? 100 : (this.hasMinionNear(target) ? 10 : 5);
        }
        int catchWeight = catchPossible && visible ? 8 : 0;
        int callWeight = callReady ? 4 : 0;

        strikeWeight = this.reduceRepeat(AlphaAction.STRIKE, strikeWeight);
        feintWeight = this.reduceRepeat(AlphaAction.FEINT_RAISE, feintWeight);
        pounceWeight = this.reduceRepeat(AlphaAction.POUNCE_WINDUP, pounceWeight);
        catchWeight = this.reduceRepeat(AlphaAction.CATCH, catchWeight);
        callWeight = this.reduceRepeat(AlphaAction.CALL, callWeight);

        int total = strikeWeight + feintWeight + pounceWeight + catchWeight + callWeight;
        if (total <= 0) {
            return AlphaAction.NONE;
        }
        int roll = this.random.nextInt(total);
        if ((roll -= strikeWeight) < 0) {
            return AlphaAction.STRIKE;
        }
        if ((roll -= feintWeight) < 0) {
            return AlphaAction.FEINT_RAISE;
        }
        if ((roll -= pounceWeight) < 0) {
            return AlphaAction.POUNCE_WINDUP;
        }
        if (roll - catchWeight < 0) {
            return AlphaAction.CATCH;
        }
        return AlphaAction.CALL;
    }

    private int reduceRepeat(AlphaAction attack, int weight) {
        return this.previousAttack == attack ? (weight + 1) / 2 : weight;
    }

    private boolean startAttack(AlphaAction chosen, LivingEntity target) {
        switch (chosen) {
            case STRIKE -> {
                this.action = AlphaAction.STRIKE;
                this.strikeCooldownTicks = this.scaledCooldown(STRIKE_COOLDOWN_TICKS);
                this.beginCommittedAttack(target);
            }
            case FEINT_RAISE -> {
                this.action = AlphaAction.FEINT_RAISE;
                this.actionTicks = 0;
                this.feintCooldownTicks = this.scaledCooldown(FEINT_COOLDOWN_TICKS);
                this.getNavigation().stop();
                this.playSound(AntarchySoundEvents.MANTIS_ATTACK.get(), 1.0F, 0.8F);
            }
            case POUNCE_WINDUP -> {
                if (this.pillarTicks < PILLAR_RESPONSE_TICKS && !this.hasClearPounceLane(target)) {
                    this.pounceCooldownTicks = POUNCE_BLOCKED_RETRY_TICKS;
                    return false;
                }
                this.action = AlphaAction.POUNCE_WINDUP;
                this.actionTicks = 0;
                this.pillarTicks = 0;
                this.pounceCooldownTicks = this.scaledCooldown(POUNCE_COOLDOWN_TICKS);
                this.getNavigation().stop();
                this.playSound(AntarchySoundEvents.MANTIS_FLY_LOOP.get(), 1.5F, 0.8F);
            }
            case CATCH -> {
                this.action = AlphaAction.CATCH;
                this.actionTicks = 0;
                this.catchReady = false;
                this.catchFlanked = false;
                this.shieldTicks = 0;
                this.pendingSnapTarget = null;
                this.catchCooldownTicks = this.scaledCooldown(CATCH_COOLDOWN_TICKS);
                this.getNavigation().stop();
                this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 1.5F, 1.3F);
            }
            case CALL -> {
                this.action = AlphaAction.CALL;
                this.actionTicks = 0;
                this.getNavigation().stop();
                this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 2.0F, 0.6F);
            }
            default -> {
                return false;
            }
        }
        this.previousAttack = chosen;
        return true;
    }

    private boolean hasClearPounceLane(LivingEntity target) {
        Vec3 start = this.position();
        Vec3 end = target.position();
        double arc = this.pounceArcFor(start, end);
        AABB box = this.getBoundingBox().deflate(0.1D);
        for (int i = 1; i <= 3; i++) {
            double s = i * 0.25D;
            Vec3 point = this.pounceArcPoint(start, end, arc, s);
            if (!this.level().noCollision(this, box.move(point.subtract(start)))) {
                return false;
            }
        }
        return true;
    }

    private double pounceArcFor(Vec3 start, Vec3 end) {
        double horizontal = Math.sqrt((end.x - start.x) * (end.x - start.x) + (end.z - start.z) * (end.z - start.z));
        return 2.0D + horizontal * 0.15D + Math.max(0.0D, end.y - start.y) * 0.25D;
    }

    private Vec3 pounceArcPoint(Vec3 start, Vec3 end, double arc, double s) {
        return new Vec3(
                Mth.lerp(s, start.x, end.x),
                Mth.lerp(s, start.y, end.y) + arc * 4.0D * s * (1.0D - s),
                Mth.lerp(s, start.z, end.z));
    }

    private void tickFeintRaise() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.enterRecovery(FEINT_RECOVERY_TICKS);
            return;
        }
        this.getNavigation().stop();
        this.faceTargetForAttack(target, STRIKE_TURN_RATE);
        if (++this.actionTicks < FEINT_RAISE_TICKS) {
            return;
        }
        this.action = AlphaAction.FEINT_HOP;
        this.actionTicks = 0;
        this.startFeintHop();
    }

    private void startFeintHop() {
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.yBodyRot);
        Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
        if (this.random.nextBoolean()) {
            side = side.reverse();
        }
        if (!this.hasHopClearance(side)) {
            side = side.reverse();
            if (!this.hasHopClearance(side)) {
                return;
            }
        }
        this.setDeltaMovement(side.x * FEINT_HOP_SPEED, 0.3D, side.z * FEINT_HOP_SPEED);
        this.needsSync = true;
        this.playSound(AntarchySoundEvents.MANTIS_FLY_LOOP.get(), 0.8F, 1.3F);
    }

    private boolean hasHopClearance(Vec3 side) {
        return this.level().noCollision(this, this.getBoundingBox().move(side.scale(FEINT_HOP_CLEARANCE)).move(0.0D, 0.25D, 0.0D));
    }

    private void tickFeintHop() {
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        if (target != null) {
            this.faceTargetForAttack(target, STRIKE_TURN_RATE);
        }
        if (++this.actionTicks < FEINT_HOP_TICKS) {
            return;
        }
        if (target == null || !target.isAlive()) {
            this.enterRecovery(FEINT_RECOVERY_TICKS);
            return;
        }
        this.action = AlphaAction.FEINT_STRIKE;
        this.beginCommittedAttack(target);
    }

    private void tickPounceWindup() {
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        if (this.actionTicks < POUNCE_LOCK_TICK) {
            if (target == null || !target.isAlive()) {
                this.enterRecovery(FEINT_RECOVERY_TICKS);
                return;
            }
            this.faceTargetForAttack(target, STRIKE_TURN_RATE);
            this.pounceEnd = target.position();
            this.committedAttackYaw = this.yBodyRot;
        } else {
            this.lockAttackYaw();
            if (this.actionTicks == POUNCE_LOCK_TICK) {
                this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 1.5F, 1.5F);
            }
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), this.getX(), this.getY() + 0.2D, this.getZ(),
                    2, this.getBbWidth() * 0.4D, 0.05D, this.getBbWidth() * 0.4D, 0.01D);
            if (this.actionTicks >= POUNCE_LOCK_TICK && this.actionTicks % 2 == 0) {
                serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.CRIT), this.pounceEnd.x, this.pounceEnd.y + 0.1D, this.pounceEnd.z,
                        6, 1.2D, 0.05D, 1.2D, 0.0D);
            }
        }
        if (++this.actionTicks >= POUNCE_WINDUP_TICKS) {
            this.launchPounce();
        }
    }

    private void launchPounce() {
        this.pounceStart = this.position();
        Vec3 delta = this.pounceEnd.subtract(this.pounceStart);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        this.pounceDirection = horizontal > 1.0E-3D ? new Vec3(delta.x / horizontal, 0.0D, delta.z / horizontal) : Vec3.directionFromRotation(0.0F, this.committedAttackYaw);
        this.pounceDuration = Mth.clamp((int) Math.round(horizontal / POUNCE_LEAP_SPEED), POUNCE_MIN_LEAP_TICKS, POUNCE_MAX_LEAP_TICKS);
        this.pounceArc = this.pounceArcFor(this.pounceStart, this.pounceEnd);
        this.pounceHits.clear();
        this.action = AlphaAction.POUNCE_LEAP;
        this.actionTicks = 0;
        this.setPounceVelocity();
        this.setNoGravity(true);
        this.playSound(AntarchySoundEvents.MANTIS_ATTACK.get(), 1.5F, 0.7F);
    }

    private void setPounceVelocity() {
        Vec3 velocity;
        if (this.actionTicks < this.pounceDuration) {
            double s = (this.actionTicks + 1) / (double) this.pounceDuration;
            velocity = this.pounceArcPoint(this.pounceStart, this.pounceEnd, this.pounceArc, s).subtract(this.position());
            if (velocity.length() > POUNCE_MAX_STEP) {
                velocity = velocity.normalize().scale(POUNCE_MAX_STEP);
            }
        } else {
            double horizontalSpeed = Math.max(0.2D, POUNCE_LEAP_SPEED - (this.actionTicks - this.pounceDuration) * 0.1D);
            velocity = new Vec3(this.pounceDirection.x * horizontalSpeed, Math.max(-1.2D, this.getDeltaMovement().y - 0.2D), this.pounceDirection.z * horizontalSpeed);
        }
        this.setDeltaMovement(velocity);
        this.needsSync = true;
    }

    private void tickPounceLeap() {
        this.actionTicks++;
        this.lockAttackYaw();
        this.getNavigation().stop();
        this.sweepPounceContact(POUNCE_CONTACT_INFLATE);
        if (this.actionTicks > 1 && this.horizontalCollision) {
            this.finishPounce(true);
            return;
        }
        if (this.actionTicks > this.pounceDuration / 2 && this.onGround()) {
            this.finishPounce(false);
            return;
        }
        if (this.actionTicks >= this.pounceDuration + POUNCE_OVERSHOOT_TICKS) {
            this.finishPounce(false);
            return;
        }
        this.setPounceVelocity();
    }

    private void sweepPounceContact(double inflate) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB sweep = this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(inflate);
        LivingEntity target = this.getTarget();
        List<LivingEntity> victims = this.level().getEntitiesOfClass(LivingEntity.class, sweep, entity -> entity != this
                && entity.isAlive()
                && !(entity instanceof MantisEntity)
                && !this.pounceHits.contains(entity.getUUID())
                && (entity == target || (entity instanceof Player player && !player.isCreative() && !player.isSpectator())));
        float damage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * POUNCE_DAMAGE_MULTIPLIER;
        for (LivingEntity victim : victims) {
            this.pounceHits.add(victim.getUUID());
            if (victim.hurtServer(serverLevel, this.damageSources().mobAttack(this), damage)) {
                this.setLastHurtMob(victim);
                victim.knockback(KNOCKBACK_STRENGTH * 1.5F, -this.pounceDirection.x, -this.pounceDirection.z);
            }
        }
    }

    private void finishPounce(boolean hitWall) {
        if (hitWall) {
            this.sweepPounceContact(POUNCE_WALL_CONTACT_INFLATE);
        }
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x * 0.2D, Math.min(motion.y, 0.0D), motion.z * 0.2D);
        this.setNoGravity(false);
        this.pounceHits.clear();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), this.getX(), this.getY() + 0.2D, this.getZ(),
                    hitWall ? 24 : 12, this.getBbWidth() * 0.4D, 0.1D, this.getBbWidth() * 0.4D, 0.04D);
        }
        if (hitWall) {
            this.playSound(SoundEvents.ANVIL_LAND, 1.0F, 0.6F);
        } else {
            this.playSound(SoundEvents.GENERIC_SMALL_FALL, 2.0F, 0.6F);
        }
        this.enterRecovery(hitWall ? POUNCE_WALL_RECOVERY_TICKS : POUNCE_RECOVERY_TICKS);
    }

    private void tickCatch() {
        this.getNavigation().stop();
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            this.faceTargetForAttack(target, CATCH_TURN_RATE);
        }
        if (this.catchFlanked) {
            this.catchFlanked = false;
            this.pendingSnapTarget = null;
            this.playSound(AntarchySoundEvents.MANTIS_HURT.get(), 1.5F, 0.7F);
            this.enterRecovery(CATCH_FLANKED_RECOVERY_TICKS);
            return;
        }
        if (this.pendingSnapTarget != null) {
            LivingEntity snapTarget = this.pendingSnapTarget;
            this.pendingSnapTarget = null;
            if (snapTarget.isAlive() && snapTarget.level() == this.level()) {
                this.action = AlphaAction.CATCH_SNAP;
                this.beginCommittedAttack(snapTarget);
                return;
            }
        }
        if (++this.actionTicks >= CATCH_HOLD_TICKS) {
            this.enterRecovery(CATCH_MISS_RECOVERY_TICKS);
        }
    }

    private void tickCall() {
        this.getNavigation().stop();
        if (++this.actionTicks < CALL_TELEGRAPH_TICKS) {
            return;
        }
        this.summonWave(this.getTarget());
        this.enterRecovery(CALL_RECOVERY_TICKS);
    }

    private int minionCap() {
        int configured = AntarchySettings.alphaMantisMaxMinions();
        int cap = this.halfHealthTransitionComplete ? configured : Math.min(CALL_PHASE_ONE_CAP, configured);
        return cap + (this.isGlimmerEcho() ? GLIMMER_EXTRA_MAX_MINIONS : 0);
    }

    private List<MantisEntity> ownedMinions() {
        UUID ownerId = this.getUUID();
        return this.level().getEntitiesOfClass(MantisEntity.class, this.getBoundingBox().inflate(OWNED_MINION_SCAN_RANGE),
                mantis -> mantis.isAlive() && mantis.isOwnedBy(ownerId));
    }

    private boolean canCallColony() {
        return this.minionCap() > 0 && this.ownedMinions().size() <= CALL_MAX_ALIVE_MINIONS;
    }

    private boolean hasMinionNear(LivingEntity target) {
        UUID ownerId = this.getUUID();
        return !this.level().getEntitiesOfClass(MantisEntity.class, target.getBoundingBox().inflate(MINION_PINCER_RANGE),
                mantis -> mantis.isAlive() && mantis.isOwnedBy(ownerId)).isEmpty();
    }

    private void summonWave(@Nullable LivingEntity target) {
        this.summonCooldownTicks = this.halfHealthTransitionComplete
                ? (int) Math.round(this.summonIntervalTicks() * PHASE_TWO_TEMPO)
                : this.summonIntervalTicks();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int waveSize = 2 + this.random.nextInt(2) + (this.isGlimmerEcho() ? GLIMMER_EXTRA_MINIONS_PER_WAVE : 0);
        int toSummon = Math.min(waveSize, this.minionCap() - this.ownedMinions().size());
        int summoned = 0;
        for (int i = 0; i < toSummon; i++) {
            if (this.spawnMinion(serverLevel, target)) {
                summoned++;
            }
        }
        if (summoned > 0) {
            this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 2.0F, 0.6F);
        }
    }

    private boolean spawnMinion(ServerLevel serverLevel, @Nullable LivingEntity target) {
        MantisEntity minion = AntarchyObjects.MANTIS.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (minion == null) {
            return false;
        }
        for (int attempt = 0; attempt < MINION_SPAWN_ATTEMPTS; attempt++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = MINION_SPAWN_MIN_DISTANCE + this.random.nextDouble() * MINION_SPAWN_RANDOM_DISTANCE;
            double x = this.getX() + Math.cos(angle) * distance;
            double z = this.getZ() + Math.sin(angle) * distance;
            minion.snapTo(x, this.getY() + 0.5D, z, this.random.nextFloat() * 360.0F, 0.0F);
            if (!serverLevel.noCollision(minion) || serverLevel.containsAnyLiquid(minion.getBoundingBox())) {
                continue;
            }
            minion.setAlphaOwner(this);
            GlimmerEchoes.markSummon(this, minion);
            minion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(minion.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            minion.setTarget(target);
            serverLevel.addFreshEntity(minion);
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), minion.getX(), minion.getY() + 0.5D, minion.getZ(), 8, 0.5D, 0.3D, 0.5D, 0.02D);
            return true;
        }
        minion.discard();
        return false;
    }

    public boolean allowsMinionAttack(MantisEntity minion) {
        if (this.action == AlphaAction.RECOVERY) {
            return true;
        }
        if (this.minionTokenHolder == null || this.tickCount >= this.minionTokenExpireTick) {
            this.minionTokenHolder = minion.getUUID();
            this.minionTokenExpireTick = this.tickCount + MINION_TOKEN_TICKS;
        }
        return minion.getUUID().equals(this.minionTokenHolder);
    }

    @Override
    protected void onActiveTarget(LivingEntity target) {
        if (this.flyBurstTicks > 0 || this.flyCooldownTicks > 0) {
            this.stopFlightBurst();
        }
    }

    @Override
    protected boolean shouldStartFlightBurst() {
        return false;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target != null && this.returningHome) {
            return;
        }
        super.setTarget(target);
        if (target != null) {
            this.setPersistenceRequired();
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
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("SummonCooldownTicks", this.summonCooldownTicks);
        output.putBoolean("HalfHealthTransitionComplete", this.halfHealthTransitionComplete);
        output.putBoolean("EncounterActive", this.encounterActive);
        output.putInt("PhaseTransitionTicks", this.phaseTransitionTicks);
        if (this.encounterHome != null) {
            output.putInt("EncounterHomeX", this.encounterHome.getX());
            output.putInt("EncounterHomeY", this.encounterHome.getY());
            output.putInt("EncounterHomeZ", this.encounterHome.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getInt("SummonCooldownTicks").ifPresent(value -> this.summonCooldownTicks = Math.max(0, value));
        this.halfHealthTransitionComplete = input.getBooleanOr("HalfHealthTransitionComplete", this.halfHealthTransitionComplete);
        Optional<Integer> homeX = input.getInt("EncounterHomeX");
        Optional<Integer> homeY = input.getInt("EncounterHomeY");
        Optional<Integer> homeZ = input.getInt("EncounterHomeZ");
        if (homeX.isPresent() && homeY.isPresent() && homeZ.isPresent()) {
            this.encounterHome = new BlockPos(homeX.get(), homeY.get(), homeZ.get());
        }
        this.phaseTransitionTicks = Mth.clamp(input.getIntOr("PhaseTransitionTicks", 0), 0, PHASE_TRANSITION_TICKS);
        this.disengageTicks = 0;
        this.returningHome = false;
        this.encounterActive = input.getBooleanOr("EncounterActive", this.halfHealthTransitionComplete || this.getHealth() < this.getMaxHealth());
        this.finishCommittedAttack();
        this.stopFlightBurst();
        this.clearAction();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    private void beginHalfHealthTransition() {
        this.halfHealthTransitionComplete = true;
        this.phaseTransitionTicks = PHASE_TRANSITION_TICKS;
        this.clearAction();
        this.stalkTicks = 0;
        this.finishCommittedAttack();
        this.stopFlightBurst();
        this.getNavigation().stop();
        Vec3 localVelocity = this.toLocal(this.getDeltaMovement());
        this.setDeltaMovement(this.toWorld(0.0D, localVelocity.y, 0.0D));
        this.setAggressive(false);
        this.summonCooldownTicks = 0;
        this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 3.0F, 0.5F);
    }

    private void tickPhaseTransition() {
        if (this.phaseTransitionTicks <= 0) {
            return;
        }
        this.phaseTransitionTicks--;
        this.getNavigation().stop();
        Vec3 localVelocity = this.toLocal(this.getDeltaMovement());
        this.setDeltaMovement(this.toWorld(0.0D, localVelocity.y, 0.0D));
        if (this.level() instanceof ServerLevel serverLevel && this.phaseTransitionTicks % 5 == 0) {
            double progress = 1.0D - this.phaseTransitionTicks / (double) PHASE_TRANSITION_TICKS;
            if (this.isGlimmerEcho()) {
                GlimmerEchoes.ring(serverLevel, this.position(), 1.5D + GLIMMER_PULSE_RADIUS * progress, 32);
            } else {
                this.sendGateRing(serverLevel, 1.5D + GATE_SHOVE_RADIUS * progress);
            }
        }
        if (this.phaseTransitionTicks > 0) {
            return;
        }
        if (this.isGlimmerEcho()) {
            this.applyGlimmerPulse();
            this.summonWave(this.getTarget());
        } else {
            this.applyGateShove();
        }
        if (this.getTarget() != null) {
            this.setAggressive(true);
        }
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

        if (this.returningHome && this.isAtEncounterHome()) {
            this.returningHome = false;
        }

        if (target != null) {
            this.encounterActive = true;
            this.disengageTicks = 0;
            this.homeRepathCooldown = 0;
            if (!this.isOutsideLeashRadius() || this.action != AlphaAction.NONE || this.isAttackLocked()) {
                return;
            }
            this.setTarget(null);
            this.returningHome = true;
            this.returnTicks = 0;
            this.announceReturn();
        }

        if (this.isAttackLocked() || this.phaseTransitionTicks > 0 || this.action != AlphaAction.NONE) {
            return;
        }

        boolean participantNearby = this.hasParticipantNearby();

        if (this.returningHome) {
            this.navigateHome();
            if (++this.returnTicks < RETURN_TIMEOUT_TICKS) {
                return;
            }
            this.returnTicks = 0;
            this.returningHome = false;
            if (participantNearby || this.isGlimmerEcho()) {
                this.disengageTicks = 0;
                return;
            }
            this.resetEncounter();
            return;
        }

        if (this.encounterActive && participantNearby) {
            this.disengageTicks = 0;
            return;
        }

        if (!this.isAtEncounterHome()) {
            this.navigateHome();
        }

        if (!this.encounterActive) {
            this.disengageTicks = 0;
            return;
        }

        if (++this.disengageTicks < RESET_DELAY_TICKS) {
            return;
        }

        this.disengageTicks = 0;
        if (!this.isGlimmerEcho()) {
            this.resetEncounter();
        }
    }

    private boolean hasParticipantNearby() {
        double rangeSqr = PARTICIPANT_RANGE * PARTICIPANT_RANGE;
        for (Player player : this.level().players()) {
            if (player.isAlive() && !player.isCreative() && !player.isSpectator() && player.distanceToSqr(this) <= rangeSqr) {
                return true;
            }
        }
        return false;
    }

    private void announceReturn() {
        this.playSound(AntarchySoundEvents.MANTIS_AMBIENT.get(), 2.0F, 0.8F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(GlimmerEchoes.particle(this, ParticleTypes.POOF), this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ(),
                    24, this.getBbWidth() * 0.4D, this.getBbHeight() * 0.3D, this.getBbWidth() * 0.4D, 0.03D);
        }
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
                || this.phaseTransitionTicks > 0
                || this.action != AlphaAction.NONE
                || this.returningHome;
    }

    private boolean isAtEncounterHome() {
        if (this.encounterHome == null) {
            return true;
        }
        return this.distanceToSqr(Vec3.atCenterOf(this.encounterHome)) <= ENCOUNTER_HOME_RADIUS * ENCOUNTER_HOME_RADIUS;
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
        if (this.distanceToSqr(homeCenter) > FORCED_RETURN_RADIUS * FORCED_RETURN_RADIUS) {
            this.announceReturn();
            this.snapTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
            this.needsSync = true;
            this.announceReturn();
            return;
        }
        this.getNavigation().moveTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, HOME_RETURN_SPEED);
    }

    private void resetEncounter() {
        this.announceReturn();
        this.clearAction();
        this.finishCommittedAttack();
        this.stopFlightBurst();
        this.getNavigation().stop();
        this.returningHome = false;
        this.encounterActive = false;
        this.setTarget(null);
        this.setAggressive(false);
        this.phaseTransitionTicks = 0;
        this.disengageTicks = 0;
        this.summonCooldownTicks = Math.max(1, this.summonIntervalTicks() / 2);
        this.halfHealthTransitionComplete = false;
        this.strikeCooldownTicks = 0;
        this.feintCooldownTicks = 0;
        this.pounceCooldownTicks = 0;
        this.catchCooldownTicks = 0;
        this.stalkTicks = 0;
        this.pillarTicks = 0;
        this.shieldTicks = 0;
        this.catchReady = false;
        this.previousAttack = AlphaAction.NONE;
        this.minionTokenHolder = null;
        for (MantisEntity minion : this.ownedMinions()) {
            minion.dismissFromAlpha();
        }
        this.setHealth(this.getMaxHealth());
        if (this.encounterHome != null) {
            Vec3 homeCenter = Vec3.atCenterOf(this.encounterHome);
            this.snapTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.announceReturn();
    }

    private GravityDirection gravityDirection() {
        return GravityApi.getGravityDirection(this);
    }

    private Vec3 toLocal(Vec3 worldVector) {
        return GravityRotationUtil.vecWorldToPlayer(worldVector, this.gravityDirection());
    }

    private Vec3 toWorld(double x, double y, double z) {
        return GravityRotationUtil.vecPlayerToWorld(x, y, z, this.gravityDirection());
    }

    private final class AlphaCombatGoal extends Goal {
        private AlphaCombatGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = AlphaMantisEntity.this.getTarget();
            return target != null && target.isAlive() && !AlphaMantisEntity.this.returningHome;
        }

        @Override
        public boolean canContinueToUse() {
            return AlphaMantisEntity.this.isAttackLocked() || AlphaMantisEntity.this.action != AlphaAction.NONE || this.canUse();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            AlphaMantisEntity.this.setAggressive(true);
            AlphaMantisEntity.this.chaseRepathTicks = 0;
        }

        @Override
        public void stop() {
            if (!AlphaMantisEntity.this.isAttackLocked()) {
                AlphaMantisEntity.this.setAggressive(false);
                AlphaMantisEntity.this.getNavigation().stop();
            }
        }

        @Override
        public void tick() {
            AlphaMantisEntity.this.tickCombatDecision();
        }
    }
}
