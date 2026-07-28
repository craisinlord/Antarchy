package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.FluidState;

public final class ThoraxisAntiwaterPoolFeature extends Feature<ThoraxisAntiwaterPoolConfiguration> {
    private static final int MIN_CAVITY_HEIGHT = 8;
    private static final int MAX_CAVITY_HEIGHT = 40;

    public ThoraxisAntiwaterPoolFeature(Codec<ThoraxisAntiwaterPoolConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ThoraxisAntiwaterPoolConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        ThoraxisAntiwaterPoolConfiguration config = context.config();

        int radius = randomBetween(random, config.minRadius(), config.maxRadius());
        int depth = randomBetween(random, config.minDepth(), config.maxDepth());
        BlockState antiwaterSourceState = config.state().getState(random, origin);
        BlockState nyxiteState = AntarchyObjects.NYXITE.get().defaultBlockState();

        int pools = randomBetween(random, 1, Math.max(1, 1 + depth / 8));
        boolean placedAny = false;

        for (int i = 0; i < pools; i++) {
            int offsetX = random.nextInt(radius * 2 + 1) - radius;
            int offsetZ = random.nextInt(radius * 2 + 1) - radius;
            int centerX = origin.getX() + offsetX;
            int centerZ = origin.getZ() + offsetZ;

            int floorY = findFloorY(level, centerX, centerZ);
            if (floorY <= level.getMinBuildHeight()) {
                continue;
            }

            int ceilingY = findCeilingY(level, centerX, centerZ, floorY);
            if (ceilingY <= floorY + MIN_CAVITY_HEIGHT || ceilingY - floorY > MAX_CAVITY_HEIGHT) {
                continue;
            }

            int poolRadius = randomBetween(random, Math.max(2, radius / 6), Math.max(3, radius / 4 + 1));
        int ventCount = randomBetween(random, 0, Math.max(1, depth / 4));
            placedAny |= placeSparsePool(level, random, centerX, centerZ, floorY, ceilingY, poolRadius, ventCount, antiwaterSourceState, nyxiteState);
        }

        return placedAny;
    }

    private boolean placeSparsePool(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int centerZ,
            int floorY,
            int ceilingY,
            int poolRadius,
            int ventCount,
            BlockState antiwaterSourceState,
            BlockState nyxiteState
    ) {
        boolean placedAny = false;
        int ceilingBandY = ceilingY - 1;
        int sourceY = floorY + 1;

        placedAny |= placeNyxiteBase(level, random, centerX, floorY, centerZ, poolRadius, nyxiteState);
        placedAny |= seedAntiwaterVent(level, sourceY, centerX, centerZ, antiwaterSourceState);

        for (int i = 0; i < ventCount; i++) {
            int ventX = centerX + random.nextInt(poolRadius * 2 + 1) - poolRadius;
            int ventZ = centerZ + random.nextInt(poolRadius * 2 + 1) - poolRadius;
            int distance = Math.abs(ventX - centerX) + Math.abs(ventZ - centerZ);
            if (distance > poolRadius + random.nextInt(2)) {
                continue;
            }

            placedAny |= placeNyxiteBase(level, random, ventX, floorY, ventZ, Math.max(1, poolRadius - 1), nyxiteState);
            placedAny |= seedAntiwaterVent(level, sourceY, ventX, ventZ, antiwaterSourceState);
            placedAny |= seedCeilingPocket(level, random, ventX, ventZ, ceilingBandY, Math.max(1, poolRadius - 1), antiwaterSourceState);
            placedAny |= seedStem(level, ventX, ventZ, floorY, ceilingBandY, antiwaterSourceState);
        }

        return placedAny;
    }

    private static boolean placeNyxiteBase(WorldGenLevel level, RandomSource random, int centerX, int floorY, int centerZ, int radius, BlockState nyxiteState) {
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius + random.nextDouble()) {
                    continue;
                }

                double density = 1.0D - (distance / Math.max(1.0D, radius));
                double chance = Mth.clamp(0.18D + density * 0.45D, 0.0D, 0.78D);
                if (random.nextDouble() > chance) {
                    continue;
                }

                mutable.set(centerX + dx, floorY, centerZ + dz);
                BlockState existing = level.getBlockState(mutable);
                if (existing.is(Blocks.BEDROCK) || existing.blocksMotion()) {
                    continue;
                }

                level.setBlock(mutable, nyxiteState, 2);
                placedAny = true;
            }
        }

        return placedAny;
    }

    private static boolean seedAntiwaterVent(WorldGenLevel level, int y, int x, int z, BlockState antiwaterSourceState) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        FluidState fluidState = state.getFluidState();
        if (state.is(Blocks.BEDROCK) || state.blocksMotion() || !fluidState.isEmpty()) {
            return false;
        }

        level.setBlock(pos, antiwaterSourceState, 2);
        scheduleFluidTick(level, pos, antiwaterSourceState);
        return true;
    }

    private static boolean seedCeilingPocket(WorldGenLevel level, RandomSource random, int centerX, int centerZ, int ceilingBandY, int radius, BlockState antiwaterSourceState) {
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockState flowingAntiwater = flowingAntiwaterState(antiwaterSourceState, Math.max(1, Math.min(7, 6 + random.nextInt(2))));

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius + random.nextDouble() * 0.5D) {
                    continue;
                }

                double density = 1.0D - (distance / Math.max(1.0D, radius));
                double chance = Mth.clamp(0.22D + density * 0.52D, 0.0D, 0.84D);
                if (random.nextDouble() > chance) {
                    continue;
                }

                mutable.set(centerX + dx, ceilingBandY, centerZ + dz);
                BlockState existing = level.getBlockState(mutable);
                FluidState fluidState = existing.getFluidState();
                if (existing.is(Blocks.BEDROCK) || existing.blocksMotion() || !fluidState.isEmpty()) {
                    continue;
                }

                level.setBlock(mutable, flowingAntiwater, 2);
                scheduleFluidTick(level, mutable, flowingAntiwater);
                placedAny = true;
            }
        }

        return placedAny;
    }

    private static boolean seedStem(WorldGenLevel level, int x, int z, int floorY, int ceilingBandY, BlockState antiwaterSourceState) {
        boolean placedAny = false;
        int stemHeight = Math.max(2, Math.min(ceilingBandY - floorY - 1, 5));
        for (int i = 1; i <= stemHeight; i++) {
            int y = floorY + i;
            if (y >= ceilingBandY) {
                break;
            }

            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.BEDROCK) || state.blocksMotion()) {
                break;
            }

            BlockState stemState = i == 1 ? antiwaterSourceState : flowingAntiwaterState(antiwaterSourceState, Math.max(1, 8 - i));
            level.setBlock(pos, stemState, 2);
            scheduleFluidTick(level, pos, stemState);
            placedAny = true;
        }

        return placedAny;
    }

    private static BlockState flowingAntiwaterState(BlockState sourceState, int level) {
        if (!sourceState.hasProperty(LiquidBlock.LEVEL)) {
            return sourceState;
        }

        int clamped = Mth.clamp(level, 1, 7);
        return sourceState.setValue(LiquidBlock.LEVEL, clamped);
    }

    private static void scheduleFluidTick(WorldGenLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    private static int findFloorY(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int floorY = Integer.MIN_VALUE;

        for (int y = level.getMinBuildHeight() + 1; y < level.getMaxBuildHeight() - 1; y++) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (!isSolidSurface(state)) {
                continue;
            }

            mutable.set(x, y + 1, z);
            if (isOpenBlock(level.getBlockState(mutable))) {
                floorY = y;
            }
        }

        return floorY;
    }

    private static int findCeilingY(WorldGenLevel level, int x, int z, int floorY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = floorY + 2; y < level.getMaxBuildHeight() - 1; y++) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (!isSolidSurface(state)) {
                continue;
            }

            mutable.set(x, y - 1, z);
            if (isOpenBlock(level.getBlockState(mutable))) {
                return y;
            }
        }

        return Integer.MIN_VALUE;
    }

    private static boolean isSolidSurface(BlockState state) {
        return !state.isAir() && state.blocksMotion() && state.getFluidState().isEmpty();
    }

    private static boolean isOpenBlock(BlockState state) {
        return state.isAir();
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }
}
