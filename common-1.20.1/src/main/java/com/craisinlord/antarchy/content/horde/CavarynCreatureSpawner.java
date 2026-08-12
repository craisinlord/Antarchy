package com.craisinlord.antarchy.content.horde;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import java.util.HashMap;
import java.util.Map;

/**
 * Tops up stink bug / rolly polly populations near players in Cavaryn.
 *
 * Vanilla's NaturalSpawner picks a spawn Y uniformly at random between the dimension's min_y
 * and a column's heightmap top. In Cavaryn that range spans the full 208-block dimension height,
 * but any given column's actual floor only occupies a sliver of that, so the vanilla algorithm
 * almost never lands on real ground even though floor itself is common. This scans a column
 * directly for a real floor spot instead.
 */
public final class CavarynCreatureSpawner {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation(Antarchy.MODID, "cavaryn")
    );

    private static final int CHECK_INTERVAL_TICKS = 20 * 10;
    private static final int AREA_COOLDOWN_TICKS = 20 * 60;
    private static final int AREA_CELL_SIZE = 40;
    private static final double NEARBY_RADIUS = 40.0D;
    private static final int TARGET_NEARBY_COUNT = 4;
    private static final double MIN_SPAWN_DISTANCE = 16.0D;
    private static final double MAX_SPAWN_DISTANCE = 36.0D;
    private static final int STINK_BUG_MIN_GROUP_SIZE = 1;
    private static final int STINK_BUG_MAX_GROUP_SIZE = 5;
    private static final int ROLLY_POLLY_MIN_GROUP_SIZE = 1;
    private static final int ROLLY_POLLY_MAX_GROUP_SIZE = 3;
    private static final int STINK_BUG_WEIGHT = 750;
    private static final int ROLLY_POLLY_WEIGHT = 250;
    private static final int MEMBER_JITTER_RADIUS = 4;
    private static final int MEMBER_VERTICAL_SEARCH = 3;

    private static final Map<Long, Long> AREA_COOLDOWNS = new HashMap<>();

    private CavarynCreatureSpawner() {
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(CAVARYN)) {
            return;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0L) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative() || !player.isAlive()) {
                continue;
            }
            attemptSpawnNear(level, player, gameTime);
        }
    }

    private static void attemptSpawnNear(ServerLevel level, ServerPlayer player, long gameTime) {
        BlockPos playerPos = player.blockPosition();
        long area = areaKey(playerPos);
        Long cooldownUntil = AREA_COOLDOWNS.get(area);
        if (cooldownUntil != null && gameTime < cooldownUntil) {
            return;
        }

        EntityType<?> stinkBugType = AntarchyObjects.STINK_BUG.get();
        EntityType<?> rollyPollyType = AntarchyObjects.ROLLY_POLLY.get();
        int nearbyStinkBugs = countNearby(level, playerPos, stinkBugType);
        int nearbyRollyPollies = countNearby(level, playerPos, rollyPollyType);
        boolean needStinkBug = nearbyStinkBugs < TARGET_NEARBY_COUNT;
        boolean needRollyPolly = nearbyRollyPollies < TARGET_NEARBY_COUNT;
        if (!needStinkBug && !needRollyPolly) {
            AREA_COOLDOWNS.put(area, gameTime + AREA_COOLDOWN_TICKS);
            return;
        }

        RandomSource random = level.random;
        BlockPos anchor = pickColumnNear(level, player, random);
        if (anchor == null) {
            return;
        }

        EntityType<?> chosenType = chooseType(needStinkBug, needRollyPolly, random);
        boolean isStinkBug = chosenType == AntarchyObjects.STINK_BUG.get();
        int minGroupSize = isStinkBug ? STINK_BUG_MIN_GROUP_SIZE : ROLLY_POLLY_MIN_GROUP_SIZE;
        int maxGroupSize = isStinkBug ? STINK_BUG_MAX_GROUP_SIZE : ROLLY_POLLY_MAX_GROUP_SIZE;
        int groupSize = minGroupSize + random.nextInt(maxGroupSize - minGroupSize + 1);
        int spawned = 0;
        for (int i = 0; i < groupSize; i++) {
            BlockPos memberPos = jitter(anchor, random);
            BlockPos memberFloor = findFloorNear(level, memberPos);
            if (memberFloor == null) {
                continue;
            }
            if (spawnMob(level, chosenType, memberFloor)) {
                spawned++;
            }
        }

        if (spawned > 0) {
            AREA_COOLDOWNS.put(area, gameTime + AREA_COOLDOWN_TICKS);
        }
    }

    private static int countNearby(ServerLevel level, BlockPos center, EntityType<?> type) {
        AABB box = new AABB(center).inflate(NEARBY_RADIUS);
        return level.getEntities(type, box, entity -> true).size();
    }

    private static EntityType<?> chooseType(boolean needStinkBug, boolean needRollyPolly, RandomSource random) {
        if (needStinkBug && !needRollyPolly) {
            return AntarchyObjects.STINK_BUG.get();
        }
        if (needRollyPolly && !needStinkBug) {
            return AntarchyObjects.ROLLY_POLLY.get();
        }
        int roll = random.nextInt(STINK_BUG_WEIGHT + ROLLY_POLLY_WEIGHT);
        return roll < STINK_BUG_WEIGHT ? AntarchyObjects.STINK_BUG.get() : AntarchyObjects.ROLLY_POLLY.get();
    }

    private static BlockPos pickColumnNear(ServerLevel level, ServerPlayer player, RandomSource random) {
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 12; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);
            if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                continue;
            }
            BlockPos floor = scanColumnForFloor(level, x, z);
            if (floor != null) {
                return floor;
            }
        }
        return null;
    }

    private static BlockPos scanColumnForFloor(ServerLevel level, int x, int z) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = minY; y <= maxY; y++) {
            cursor.set(x, y, z);
            if (isValidGroundSpawn(level, cursor)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private static BlockPos jitter(BlockPos anchor, RandomSource random) {
        int dx = random.nextInt(MEMBER_JITTER_RADIUS * 2 + 1) - MEMBER_JITTER_RADIUS;
        int dz = random.nextInt(MEMBER_JITTER_RADIUS * 2 + 1) - MEMBER_JITTER_RADIUS;
        return anchor.offset(dx, 0, dz);
    }

    private static BlockPos findFloorNear(ServerLevel level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= MEMBER_VERTICAL_SEARCH; dy++) {
            cursor.set(start.getX(), start.getY() - dy, start.getZ());
            if (isValidGroundSpawn(level, cursor)) {
                return cursor.immutable();
            }
            if (dy != 0) {
                cursor.set(start.getX(), start.getY() + dy, start.getZ());
                if (isValidGroundSpawn(level, cursor)) {
                    return cursor.immutable();
                }
            }
        }
        return null;
    }

    private static boolean isValidGroundSpawn(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos.above()).isEmpty()
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    private static boolean spawnMob(ServerLevel level, EntityType<?> type, BlockPos pos) {
        if (!level.noCollision(type.getAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
            return false;
        }
        Entity entity = type.create(level);
        if (!(entity instanceof Mob mob)) {
            return false;
        }
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null, null);
        return level.addFreshEntity(mob);
    }

    private static long areaKey(BlockPos pos) {
        return BlockPos.asLong(
                Math.floorDiv(pos.getX(), AREA_CELL_SIZE) * AREA_CELL_SIZE,
                Math.floorDiv(pos.getY(), AREA_CELL_SIZE) * AREA_CELL_SIZE,
                Math.floorDiv(pos.getZ(), AREA_CELL_SIZE) * AREA_CELL_SIZE
        );
    }
}
