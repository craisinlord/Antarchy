package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.BedBugEggBlock;
import com.craisinlord.antarchy.content.entity.BedBugEntity;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class BedBugSurfaceClusterFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SURFACE_SAMPLE_RADIUS = 8;
    private static final int SURFACE_SAMPLE_ATTEMPTS = 24;
    private static final int FLOOR_SCAN_RANGE = 12;
    private static final int MIN_BUGS_PER_FEATURE = 1;
    private static final int MAX_BUGS_PER_FEATURE = 4;

    public BedBugSurfaceClusterFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placedAny = false;

        BlockPos surfacePos = findSurfacePos(level, origin, random);
        if (surfacePos == null) {
            return false;
        }

        List<BlockPos> eggSpots = placeEggPatch(level, surfacePos, random);
        if (eggSpots.isEmpty()) {
            return false;
        }

        placedAny = true;
        if (spawnBedBugsAroundEggs(level, surfacePos, eggSpots, random)) {
            placedAny = true;
        }

        return placedAny;
    }

    private static BlockPos findSurfacePos(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SURFACE_SAMPLE_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SURFACE_SAMPLE_RADIUS * 2 + 1) - SURFACE_SAMPLE_RADIUS;
            int z = origin.getZ() + random.nextInt(SURFACE_SAMPLE_RADIUS * 2 + 1) - SURFACE_SAMPLE_RADIUS;
            int startY = origin.getY() + random.nextInt(FLOOR_SCAN_RANGE * 2 + 1) - FLOOR_SCAN_RANGE;
            startY = Math.max(level.getMinBuildHeight() + 2, Math.min(level.getMaxBuildHeight() - 3, startY));

            for (int y = startY; y >= Math.max(level.getMinBuildHeight() + 1, startY - FLOOR_SCAN_RANGE); y--) {
                mutable.set(x, y, z);
                BlockState floorState = level.getBlockState(mutable);
                BlockState airState = level.getBlockState(mutable.above());
                BlockState aboveAirState = level.getBlockState(mutable.above(2));
                if (floorState.getFluidState().isEmpty()
                        && floorState.blocksMotion()
                        && floorState.isFaceSturdy(level, mutable, Direction.UP)
                        && airState.canBeReplaced()
                        && aboveAirState.canBeReplaced()) {
                    return mutable.immutable();
                }
            }
        }

        return null;
    }

    private static List<BlockPos> placeEggPatch(WorldGenLevel level, BlockPos originEggPos, RandomSource random) {
        List<BlockPos> eggSpots = new ArrayList<>(3);
        int spotCount = 2 + random.nextInt(2);

        for (int i = 0; i < spotCount; i++) {
            BlockPos eggPos = originEggPos.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
            if (!level.getBlockState(eggPos).canBeReplaced()) {
                continue;
            }

            BlockPos supportPos = eggPos.below();
            BlockState supportState = level.getBlockState(supportPos);
            if (!supportState.isFaceSturdy(level, supportPos, Direction.UP) || !supportState.getFluidState().isEmpty()) {
                continue;
            }

            int eggs = 1 + random.nextInt(4);
            level.setBlock(
                    eggPos,
                    AntarchyObjects.BED_BUG_EGG.get().defaultBlockState().setValue(BedBugEggBlock.EGGS, eggs),
                    Block.UPDATE_ALL
            );
            eggSpots.add(eggPos.immutable());
        }

        return eggSpots;
    }

    private static boolean spawnBedBugsAroundEggs(WorldGenLevel level, BlockPos surfacePos, List<BlockPos> eggSpots, RandomSource random) {
        if (!(level instanceof ServerLevelAccessor serverLevel)) {
            return false;
        }

        DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(surfacePos);
        int count = MIN_BUGS_PER_FEATURE + random.nextInt(MAX_BUGS_PER_FEATURE - MIN_BUGS_PER_FEATURE + 1);
        boolean spawnedAny = false;

        for (int i = 0; i < count; i++) {
            BlockPos eggAnchor = eggSpots.get(random.nextInt(eggSpots.size()));
            BlockPos spawnPos = findOpenSpawnPos(level, eggAnchor, random);
            if (spawnPos == null) {
                spawnPos = findOpenSpawnPos(level, surfacePos.above(), random);
            }
            if (spawnPos == null) {
                continue;
            }

            BedBugEntity bedBug = AntarchyObjects.BED_BUG.get().create(serverLevel.getLevel());
            if (bedBug == null) {
                continue;
            }

            bedBug.moveTo(
                    spawnPos.getX() + 1.0D + (random.nextDouble() - 0.5D) * 0.25D,
                    spawnPos.getY() + 0.02D,
                    spawnPos.getZ() + 1.0D + (random.nextDouble() - 0.5D) * 0.25D,
                    random.nextFloat() * 360.0F,
                    0.0F
                );
            bedBug.setHomeEggPos(eggAnchor);
            bedBug.finalizeSpawn(serverLevel, difficulty, MobSpawnType.CHUNK_GENERATION, null);
            serverLevel.addFreshEntity(bedBug);
            spawnedAny = true;
        }

        return spawnedAny;
    }

    private static BlockPos findOpenSpawnPos(WorldGenLevel level, BlockPos center, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < 12; attempt++) {
            int x = center.getX() + random.nextInt(5) - 2;
            int y = center.getY() + random.nextInt(3) - 1;
            int z = center.getZ() + random.nextInt(5) - 2;
            mutable.set(x, y, z);

            if (canFitBedBugIn2x2(level, mutable)) {
                return mutable.immutable();
            }
        }

        return null;
    }

    private static boolean canFitBedBugIn2x2(WorldGenLevel level, BlockPos basePos) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos feetPos = basePos.offset(dx, 0, dz);
                BlockPos headPos = feetPos.above();
                BlockPos floorPos = feetPos.below();

                BlockState feetState = level.getBlockState(feetPos);
                BlockState headState = level.getBlockState(headPos);
                BlockState floorState = level.getBlockState(floorPos);
                if (!feetState.canBeReplaced()
                        || !headState.canBeReplaced()
                        || !floorState.blocksMotion()
                        || !floorState.isFaceSturdy(level, floorPos, Direction.UP)
                        || !floorState.getFluidState().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
