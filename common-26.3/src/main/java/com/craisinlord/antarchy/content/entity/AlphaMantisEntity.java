package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.boss.BossCombatUtil;
import com.craisinlord.anthology.gravity.GravityApi;
import com.craisinlord.anthology.gravity.GravityDirection;
import com.craisinlord.anthology.gravity.GravityRotationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlphaMantisEntity extends MantisEntity {
    private static final double ALPHA_ATTACK_START_RANGE = 6.0D;
    private static final double ALPHA_ATTACK_HIT_REACH = 7.25D;
    private static final double ALPHA_ATTACK_HALF_WIDTH = 3.5D;
    private static final double ALPHA_ATTACK_VERTICAL_TOLERANCE = 4.25D;
    private static final int PHASE_TRANSITION_TICKS = 35;
    private static final double ENCOUNTER_HOME_RADIUS = 72.0D;
    private static final double ENCOUNTER_LEASH_RADIUS = 96.0D;
    private static final double FORCED_RETURN_RADIUS = 144.0D;
    private static final int RESET_DELAY_TICKS = 300;
    private static final double HOME_RETURN_SPEED = 1.05D;
    private static final int HOME_REPATH_INTERVAL_TICKS = 20;

    private final ServerBossEvent bossEvent =
            new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this.getUUID(), Component.translatable("entity.antarchy.alpha_mantis"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    private int summonCooldownTicks;
    private int phaseTransitionTicks;
    private int disengageTicks;
    private int homeRepathCooldown;
    private boolean halfHealthTransitionComplete;
    @Nullable
    private BlockPos encounterHome;

    public AlphaMantisEntity(EntityType<? extends AlphaMantisEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 60;
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
        if (this.phaseTransitionTicks > 0 || super.isInvulnerableTo(level, source)) {
            return true;
        }
        return BossCombatUtil.isOutOfDamageRange(this, AntarchySettings.alphaMantisDamageRange());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (BossCombatUtil.isMagicWarded(this) && BossCombatUtil.isMagicBurstSource(source)) {
            amount *= (1.0F - (float) AntarchySettings.bossMagicWardReductionFraction());
        }
        float preHealth = this.getHealth();
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt) {
            this.handleHalfHealthGate(preHealth);
        }
        return hurt;
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
    protected double getCombatSpeed() {
        return 1.05D;
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
        return this.phaseTransitionTicks <= 0 && super.canBeginCommittedAttack(target);
    }

    @Override
    protected double configuredMaxHealth() {
        return AntarchySettings.alphaMantisHealth();
    }

    @Override
    protected double configuredAttackDamage() {
        return AntarchySettings.alphaMantisAttackDamage();
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

        this.tickEncounterState();
        this.tickPhaseTransition();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.bossEvent.setVisible(this.shouldShowBossBar());
        this.tickMinionSummon();
    }

    private void tickMinionSummon() {
        if (this.phaseTransitionTicks > 0) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        if (this.summonCooldownTicks > 0) {
            this.summonCooldownTicks--;
            return;
        }
        this.summonCooldownTicks = AntarchySettings.alphaMantisSummonIntervalTicks();

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        int nearbyMinions = serverLevel.getEntitiesOfClass(
                MantisEntity.class,
                this.getBoundingBox().inflate(32.0D),
                mantis -> mantis.isAlive() && !(mantis instanceof AlphaMantisEntity)
        ).size();
        int maxMinions = AntarchySettings.alphaMantisMaxMinions();
        if (nearbyMinions >= maxMinions) {
            return;
        }

        int toSummon = Math.min(1 + this.random.nextInt(2), maxMinions - nearbyMinions);
        for (int i = 0; i < toSummon; i++) {
            MantisEntity minion = AntarchyObjects.MANTIS.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
            if (minion == null) {
                continue;
            }
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = 3.0D + this.random.nextDouble() * 3.0D;
            minion.snapTo(
                    this.getX() + Math.cos(angle) * distance,
                    this.getY() + 0.5D,
                    this.getZ() + Math.sin(angle) * distance,
                    this.random.nextFloat() * 360.0F,
                    0.0F
            );
            minion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(minion.blockPosition()), EntitySpawnReason.MOB_SUMMONED, null);
            minion.setTarget(target);
            serverLevel.addFreshEntity(minion);
        }
        this.playSound(com.craisinlord.antarchy.content.AntarchySoundEvents.MANTIS_AMBIENT.get(), 2.0F, 0.6F);
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
        this.phaseTransitionTicks = 0;
        this.disengageTicks = 0;
        this.finishCommittedAttack();
        this.stopFlightBurst();
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        this.bossEvent.removeAllPlayers();
    }

    private void handleHalfHealthGate(float preHitHealth) {
        float halfHealth = this.getMaxHealth() * 0.5F;
        if (!this.halfHealthTransitionComplete && preHitHealth > halfHealth && this.getHealth() < halfHealth) {
            this.setHealth(halfHealth);
            this.beginHalfHealthTransition();
        }
    }

    private void beginHalfHealthTransition() {
        this.halfHealthTransitionComplete = true;
        this.phaseTransitionTicks = PHASE_TRANSITION_TICKS;
        this.finishCommittedAttack();
        this.stopFlightBurst();
        this.getNavigation().stop();
        Vec3 localVelocity = this.toLocal(this.getDeltaMovement());
        this.setDeltaMovement(this.toWorld(0.0D, localVelocity.y, 0.0D));
        this.setAggressive(false);
        this.summonCooldownTicks = Math.min(this.summonCooldownTicks, AntarchySettings.alphaMantisSummonIntervalTicks() / 2);
    }

    private void tickPhaseTransition() {
        if (this.phaseTransitionTicks <= 0) {
            return;
        }
        this.phaseTransitionTicks--;
        this.getNavigation().stop();
        Vec3 localVelocity = this.toLocal(this.getDeltaMovement());
        this.setDeltaMovement(this.toWorld(0.0D, localVelocity.y, 0.0D));
        if (this.phaseTransitionTicks <= 0 && this.getTarget() != null) {
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

        if (target != null) {
            this.disengageTicks = 0;
            this.homeRepathCooldown = 0;
            if (this.isOutsideLeashRadius()) {
                this.resetEncounter(true);
            }
            return;
        }

        if (this.isAttackLocked() || this.phaseTransitionTicks > 0) {
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
                || this.phaseTransitionTicks > 0
                || !this.isAtEncounterHome();
    }

    private boolean isAtEncounterHome() {
        if (this.encounterHome == null) {
            return true;
        }
        return this.distanceToSqr(Vec3.atCenterOf(this.encounterHome)) <= ENCOUNTER_HOME_RADIUS;
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
            this.snapTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
            this.needsSync = true;
            return;
        }
        this.getNavigation().moveTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, HOME_RETURN_SPEED);
    }

    private void resetEncounter(boolean restoreHealth) {
        this.finishCommittedAttack();
        this.stopFlightBurst();
        this.getNavigation().stop();
        this.setTarget(null);
        this.setAggressive(false);
        this.phaseTransitionTicks = 0;
        this.disengageTicks = 0;
        this.summonCooldownTicks = Math.max(1, AntarchySettings.alphaMantisSummonIntervalTicks() / 2);
        this.halfHealthTransitionComplete = false;
        if (restoreHealth) {
            this.setHealth(this.getMaxHealth());
        }
        if (this.encounterHome != null) {
            Vec3 homeCenter = Vec3.atCenterOf(this.encounterHome);
            this.snapTo(homeCenter.x, this.encounterHome.getY(), homeCenter.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(Vec3.ZERO);
        }
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
}
