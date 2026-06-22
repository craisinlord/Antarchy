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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class BedBugNestFeature extends Feature<NoneFeatureConfiguration> {
    public BedBugNestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = findNestCenter(level, context.origin(), random);
        if (center == null) {
            return false;
        }

        Direction wallDirection = findWallDirection(level, center);
        if (wallDirection == null) {
            return false;
        }

        boolean carved = carvePocket(level, center, wallDirection);
        if (!carved) {
            return false;
        }

        List<BlockPos> eggSpots = placeEggClusters(level, center, wallDirection, random);
        if (eggSpots.isEmpty()) {
            return false;
        }

        spawnBedBugs(level, center, eggSpots, random);
        return true;
    }

    private static BlockPos findNestCenter(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = origin.getX() + random.nextInt(15) - 7;
            int y = origin.getY() + random.nextInt(13) - 6;
            int z = origin.getZ() + random.nextInt(15) - 7;
            mutable.set(x, y, z);

            BlockState state = level.getBlockState(mutable);
            BlockState belowState = level.getBlockState(mutable.below());
            if (!isOpen(state) || !isNestFloor(belowState)) {
                continue;
            }

            if (findWallDirection(level, mutable) != null) {
                return mutable.immutable();
            }
        }

        return null;
    }

    private static Direction findWallDirection(WorldGenLevel level, BlockPos center) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos wallPos = center.relative(direction);
            BlockPos pocketPos = center.relative(direction, 2);
            if (isCarvable(level.getBlockState(wallPos)) && isCarvable(level.getBlockState(pocketPos))) {
                return direction;
            }
        }
        return null;
    }

    private static boolean carvePocket(WorldGenLevel level, BlockPos center, Direction direction) {
        boolean carved = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int depth = 1; depth <= 2; depth++) {
            for (int side = -1; side <= 1; side++) {
                for (int height = 0; height <= 1; height++) {
                    BlockPos target = center.relative(direction, depth)
                            .relative(direction.getClockWise(), side)
                            .above(height);
                    mutable.set(target);
                    BlockState state = level.getBlockState(mutable);
                    if (!isCarvable(state)) {
                        continue;
                    }

                    level.setBlock(mutable, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    carved = true;
                }
            }
        }

        return carved;
    }

    private static List<BlockPos> placeEggClusters(WorldGenLevel level, BlockPos center, Direction direction, RandomSource random) {
        List<BlockPos> eggSpots = new ArrayList<>(3);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int attempt = 0; attempt < 3; attempt++) {
            int depth = 1 + random.nextInt(2);
            int side = random.nextInt(3) - 1;
            mutable.set(center.relative(direction, depth).relative(direction.getClockWise(), side));
            if (!level.getBlockState(mutable).isAir()) {
                continue;
            }
            if (!level.getBlockState(mutable.below()).isFaceSturdy(level, mutable.below(), Direction.UP)) {
                continue;
            }

            int eggs = 1 + random.nextInt(4);
            level.setBlock(mutable, AntarchyObjects.BED_BUG_EGG.get().defaultBlockState().setValue(BedBugEggBlock.EGGS, eggs), Block.UPDATE_ALL);
            eggSpots.add(mutable.immutable());
        }

        if (eggSpots.isEmpty()) {
            BlockPos fallback = center.relative(direction, 2);
            if (level.getBlockState(fallback).isAir() && level.getBlockState(fallback.below()).isFaceSturdy(level, fallback.below(), Direction.UP)) {
                int eggs = 1 + random.nextInt(4);
                level.setBlock(fallback, AntarchyObjects.BED_BUG_EGG.get().defaultBlockState().setValue(BedBugEggBlock.EGGS, eggs), Block.UPDATE_ALL);
                eggSpots.add(fallback.immutable());
            }
        }

        return eggSpots;
    }

    private static void spawnBedBugs(WorldGenLevel level, BlockPos center, List<BlockPos> eggSpots, RandomSource random) {
        if (!(level instanceof ServerLevelAccessor serverLevel)) {
            return;
        }

        DifficultyInstance difficulty = serverLevel.getCurrentDifficultyAt(center);
        int count = 1 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            BlockPos eggAnchor = eggSpots.get(random.nextInt(eggSpots.size()));
            BlockPos spawnPos = findOpenSpawnPos(level, eggAnchor, center, random);
            if (spawnPos == null) {
                continue;
            }

            BedBugEntity bedBug = AntarchyObjects.BED_BUG.get().create(serverLevel.getLevel());
            if (bedBug == null) {
                continue;
            }

            double spawnX = spawnPos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            double spawnY = spawnPos.getY() + 0.02D;
            double spawnZ = spawnPos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.35D;
            bedBug.moveTo(spawnX, spawnY, spawnZ, random.nextFloat() * 360.0F, 0.0F);
            bedBug.setHomeEggPos(eggAnchor);
            bedBug.finalizeSpawn(serverLevel, difficulty, MobSpawnType.CHUNK_GENERATION, null);
            serverLevel.addFreshEntity(bedBug);
        }
    }

    private static BlockPos findOpenSpawnPos(WorldGenLevel level, BlockPos eggAnchor, BlockPos center, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < 16; attempt++) {
            BlockPos base = attempt < 10 ? eggAnchor : center;
            int x = base.getX() + random.nextInt(5) - 2;
            int y = base.getY() + random.nextInt(3) - 1;
            int z = base.getZ() + random.nextInt(5) - 2;
            mutable.set(x, y, z);

            BlockState feetState = level.getBlockState(mutable);
            BlockState headState = level.getBlockState(mutable.above());
            BlockState floorState = level.getBlockState(mutable.below());
            if (feetState.canBeReplaced()
                    && headState.canBeReplaced()
                    && floorState.blocksMotion()
                    && floorState.isFaceSturdy(level, mutable.below(), Direction.UP)
                    && floorState.getFluidState().isEmpty()) {
                return mutable.immutable();
            }
        }

        return null;
    }

    private static boolean isNestFloor(BlockState state) {
        return !state.isAir() && state.blocksMotion() && state.getFluidState().isEmpty();
    }

    private static boolean isCarvable(BlockState state) {
        return !state.isAir() && state.blocksMotion() && state.getFluidState().isEmpty() && !state.is(Blocks.BEDROCK);
    }

    private static boolean isOpen(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.blocksMotion();
    }
}
