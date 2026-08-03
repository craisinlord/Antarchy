package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class AlphaMantisEntity extends MantisEntity {
    private final ServerBossEvent bossEvent =
            new com.craisinlord.antarchy.content.boss.EntityLinkedServerBossEvent(this.getUUID(), Component.translatable("entity.antarchy.alpha_mantis"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    private int summonCooldownTicks;

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
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    public static boolean checkAlphaMantisSpawnRules(EntityType<AlphaMantisEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
        if (spawnReason == MobSpawnType.SPAWN_EGG
                || spawnReason == MobSpawnType.SPAWNER
                || spawnReason == MobSpawnType.TRIAL_SPAWNER
                || spawnReason == MobSpawnType.COMMAND) {
            return true;
        }

        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }

        if (spawnReason != MobSpawnType.NATURAL) {
            return false;
        }

        boolean atSurface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos).getY() <= pos.getY();
        boolean posOpen = level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above()) && level.getFluidState(pos).isEmpty();
        boolean onSolidGround = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                && !(level.getBlockState(pos.below()).getBlock() instanceof LeavesBlock);

        if (!posOpen || !onSolidGround || !atSurface) {
            return false;
        }

        if (!level.getLevel().getEntitiesOfClass(AlphaMantisEntity.class, new AABB(pos).inflate(128.0D), Entity::isAlive).isEmpty()) {
            return false;
        }

        boolean ignoreLightLevel = AntarchySettings.mantisIgnoreLightLevel();

        if (level.getLevel().isDay() && level.getBiome(pos).is(AntarchyTags.Biomes.MANTIS_OVERWORLD_SPAWN_BIOMES)) {
            return level.canSeeSky(pos);
        }

        if (level.getLevel().isNight() && level.getBiome(pos).is(AntarchyTags.Biomes.MANTIS_SPAWN_BIOMES)) {
            return ignoreLightLevel || Monster.checkMonsterSpawnRules(entityType, level, spawnReason, pos, random);
        }

        return false;
    }

    @Override
    protected MeleeAttackGoal createCombatGoal() {
        return new MeleeAttackGoal(this, this.getCombatSpeed(), true);
    }

    @Override
    protected double getCombatSpeed() {
        return 1.05D;
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
        if (this.level().isClientSide) {
            return;
        }

        if (this.isDeadOrDying()) {
            this.bossEvent.setProgress(0.0F);
            return;
        }

        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        this.tickMinionSummon();
    }

    private void tickMinionSummon() {
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
            MantisEntity minion = AntarchyObjects.MANTIS.get().create(serverLevel);
            if (minion == null) {
                continue;
            }
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            double distance = 3.0D + this.random.nextDouble() * 3.0D;
            minion.moveTo(
                    this.getX() + Math.cos(angle) * distance,
                    this.getY() + 0.5D,
                    this.getZ() + Math.sin(angle) * distance,
                    this.random.nextFloat() * 360.0F,
                    0.0F
            );
            minion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
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
}
