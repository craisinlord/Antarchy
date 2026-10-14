package com.craisinlord.antarchy.content.block.entity.undertrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public enum UndertrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, -1.0D, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, 200.0D, true),
    ACTIVE("active", 8, 1000.0D, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, -1.0D, false),
    EJECTING_REWARD("ejecting_reward", 8, -1.0D, false),
    COOLDOWN("cooldown", 0, -1.0D, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final boolean isCapableOfSpawning;

    UndertrialSpawnerState(String name, int lightLevel, double spinningMobSpeed, boolean isCapableOfSpawning) {
        this.name = name;
        this.lightLevel = lightLevel;
        this.spinningMobSpeed = spinningMobSpeed;
        this.isCapableOfSpawning = isCapableOfSpawning;
    }

    UndertrialSpawnerState tickAndGetNext(BlockPos pos, UndertrialSpawner spawner, ServerLevel level) {
        UndertrialSpawnerData data = spawner.getData();
        UndertrialSpawnerConfig config = spawner.getConfig();
        switch (this) {
            case INACTIVE:
                return data.getOrCreateDisplayEntity(spawner, level, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
            case WAITING_FOR_PLAYERS:
                if (!spawner.canSpawnInLevel(level)) {
                    data.reset();
                    return this;
                }
                if (!data.hasMobToSpawn(spawner, level.random)) {
                    return INACTIVE;
                }
                data.tryDetectPlayers(level, pos, spawner);
                return data.detectedPlayers.isEmpty() ? this : ACTIVE;
            case ACTIVE: {
                if (!spawner.canSpawnInLevel(level)) {
                    data.reset();
                    return WAITING_FOR_PLAYERS;
                }
                if (!data.hasMobToSpawn(spawner, level.random)) {
                    return INACTIVE;
                }
                int additionalPlayers = data.countAdditionalPlayers(pos);
                data.tryDetectPlayers(level, pos, spawner);
                if (spawner.isOminous()) {
                    spawnOminousItem(level, pos, spawner);
                }
                if (data.hasFinishedSpawningAllMobs(config, additionalPlayers)) {
                    if (data.haveAllCurrentMobsDied()) {
                        data.cooldownEndsAt = level.getGameTime() + spawner.getTargetCooldownLength();
                        data.totalMobsSpawned = 0;
                        data.nextMobSpawnsAt = 0L;
                        return WAITING_FOR_REWARD_EJECTION;
                    }
                } else if (data.isReadyToSpawnNextMob(level, config, additionalPlayers)) {
                    spawner.spawnMob(level, pos).ifPresent(uuid -> {
                        data.currentMobs.add(uuid);
                        data.totalMobsSpawned++;
                        data.nextMobSpawnsAt = level.getGameTime() + config.ticksBetweenSpawn();
                        config.spawnPotentialsDefinition().getRandom(level.getRandom()).ifPresent(wrapper -> {
                            data.nextSpawnData = Optional.of(wrapper.getData());
                            spawner.markUpdated();
                        });
                    });
                }
                return this;
            }
            case WAITING_FOR_REWARD_EJECTION:
                if (data.isReadyToOpenShutter(level, DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB, spawner.getTargetCooldownLength())) {
                    UndertrialSounds.playServer(level, pos, UndertrialSounds.TRIAL_SPAWNER_OPEN_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
                    return EJECTING_REWARD;
                }
                return this;
            case EJECTING_REWARD:
                if (!data.isReadyToEjectItems(level, TIME_BETWEEN_EACH_EJECTION, spawner.getTargetCooldownLength())) {
                    return this;
                }
                if (data.detectedPlayers.isEmpty()) {
                    UndertrialSounds.playServer(level, pos, UndertrialSounds.TRIAL_SPAWNER_CLOSE_SHUTTER, SoundSource.BLOCKS, 1.0F, 1.0F);
                    data.ejectingLootTable = Optional.empty();
                    return COOLDOWN;
                }
                if (data.ejectingLootTable.isEmpty()) {
                    data.ejectingLootTable = config.lootTablesToEject().getRandomValue(level.getRandom());
                }
                data.ejectingLootTable.ifPresent(lootTable -> spawner.ejectReward(level, pos, lootTable));
                data.detectedPlayers.remove(data.detectedPlayers.iterator().next());
                return this;
            case COOLDOWN:
            default:
                data.tryDetectPlayers(level, pos, spawner);
                if (!data.detectedPlayers.isEmpty()) {
                    data.totalMobsSpawned = 0;
                    data.nextMobSpawnsAt = 0L;
                    return ACTIVE;
                }
                if (data.isCooldownFinished(level)) {
                    spawner.removeOminous(level, pos);
                    data.reset();
                    return WAITING_FOR_PLAYERS;
                }
                return this;
        }
    }

    private void spawnOminousItem(ServerLevel level, BlockPos pos, UndertrialSpawner spawner) {
        UndertrialSpawnerData data = spawner.getData();
        UndertrialSpawnerConfig config = spawner.getConfig();
        ItemStack stack = data.getDispensingItems(level, config, pos).getRandomValue(level.random).orElse(ItemStack.EMPTY);
        if (stack.isEmpty() || level.getGameTime() < data.cooldownEndsAt) {
            return;
        }
        calculatePositionToSpawnItem(level, pos, spawner, data).ifPresent(position -> {
            ItemEntity item = new ItemEntity(level, position.x, position.y, position.z, stack.copy());
            item.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(item);
            float pitch = (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F;
            UndertrialSounds.playServer(level, BlockPos.containing(position), UndertrialSounds.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, pitch);
            data.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
        });
    }

    private static Optional<Vec3> calculatePositionToSpawnItem(ServerLevel level, BlockPos pos, UndertrialSpawner spawner, UndertrialSpawnerData data) {
        List<Player> players = new ArrayList<>();
        for (UUID uuid : data.detectedPlayers) {
            Player player = level.getPlayerByUUID(uuid);
            if (player != null && !player.isCreative() && !player.isSpectator() && player.isAlive()
                    && player.distanceToSqr(pos.getCenter()) <= Mth.square(spawner.getRequiredPlayerRange())) {
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            return Optional.empty();
        }
        Entity entity = selectEntityToSpawnItemAbove(players, data.currentMobs, spawner, pos, level);
        return entity == null ? Optional.empty() : calculatePositionAbove(entity, level);
    }

    private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel level) {
        Vec3 start = entity.position();
        Vec3 end = start.relative(Direction.UP, entity.getBbHeight() + 2.0F + level.random.nextInt(4));
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null));
        Vec3 position = hit.getBlockPos().getCenter().relative(Direction.DOWN, 1.0D);
        BlockPos blockPos = BlockPos.containing(position);
        return !level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty() ? Optional.empty() : Optional.of(position);
    }

    private static Entity selectEntityToSpawnItemAbove(List<Player> players, Set<UUID> mobs, UndertrialSpawner spawner, BlockPos pos, ServerLevel level) {
        List<Entity> livingMobs = new ArrayList<>();
        for (UUID uuid : mobs) {
            Entity mob = level.getEntity(uuid);
            if (mob != null && mob.isAlive() && mob.distanceToSqr(pos.getCenter()) <= Mth.square(spawner.getRequiredPlayerRange())) {
                livingMobs.add(mob);
            }
        }
        List<? extends Entity> candidates = level.random.nextBoolean() ? livingMobs : players;
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.size() == 1 ? candidates.get(0) : Objects.requireNonNull(Util.getRandom(candidates, level.random));
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0D;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
