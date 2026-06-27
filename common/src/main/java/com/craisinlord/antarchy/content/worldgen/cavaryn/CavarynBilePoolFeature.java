package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.FluidState;

public final class CavarynBilePoolFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation BILE_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "bile");
    private static final int SEARCH_RADIUS = 8;
    private static final int SEARCH_ATTEMPTS = 24;
    private static final int FLOOR_SCAN_RANGE = 14;

    public CavarynBilePoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        Block bileBlock = getBlock(BILE_ID);
        if (!(bileBlock instanceof LiquidBlock)) {
            return false;
        }

        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos floorCenter = findPoolFloor(level, context.origin(), random);
        if (floorCenter == null) {
            return false;
        }

        return placePool(level, floorCenter, bileBlock.defaultBlockState(), random);
    }

    private static BlockPos findPoolFloor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int attempt = 0; attempt < SEARCH_ATTEMPTS; attempt++) {
            int x = origin.getX() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int z = origin.getZ() + random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int startY = origin.getY() + random.nextInt(FLOOR_SCAN_RANGE * 2 + 1) - FLOOR_SCAN_RANGE;
            startY = Math.max(level.getMinBuildHeight() + 3, Math.min(level.getMaxBuildHeight() - 4, startY));

            for (int y = startY; y >= Math.max(level.getMinBuildHeight() + 2, startY - FLOOR_SCAN_RANGE); y--) {
                mutable.set(x, y, z);
                BlockState floorState = level.getBlockState(mutable);
                if (!isValidFloor(level, mutable, floorState)) {
                    continue;
                }

                BlockState aboveState = level.getBlockState(mutable.above());
                if (!isOpenBlock(aboveState)) {
                    continue;
                }

                if (countSolidNeighbors(level, mutable) >= 3) {
                    return mutable.immutable();
                }
            }
        }

        return null;
    }

    private static boolean placePool(WorldGenLevel level, BlockPos center, BlockState bileState, RandomSource random) {
        boolean placedAny = false;
        int radius = 2 + random.nextInt(3);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double normalized = (dx * dx + dz * dz) / (double) Math.max(1, radius * radius);
                if (normalized > 1.0D + random.nextDouble() * 0.2D) {
                    continue;
                }

                double density = 1.0D - Math.min(1.0D, normalized);
                int localDepth = density > 0.45D || random.nextFloat() < 0.35F ? 2 : 1;
                localDepth = Mth.clamp(localDepth, 1, 2);

                for (int depth = 0; depth < localDepth; depth++) {
                    mutable.set(center.getX() + dx, center.getY() - depth, center.getZ() + dz);
                    BlockState existing = level.getBlockState(mutable);
                    FluidState existingFluid = existing.getFluidState();
                    if (existing.is(Blocks.BEDROCK)) {
                        break;
                    }
                    if (!existing.canBeReplaced() && existingFluid.isEmpty() && !existing.blocksMotion()) {
                        continue;
                    }

                    level.setBlock(mutable, bileState, 2);
                    scheduleFluidTick(level, mutable, bileState);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private static boolean isValidFloor(WorldGenLevel level, BlockPos pos, BlockState state) {
        return !state.isAir()
                && !state.is(Blocks.BEDROCK)
                && state.getFluidState().isEmpty()
                && state.blocksMotion()
                && state.isFaceSturdy(level, pos, Direction.UP);
    }

    private static boolean isOpenBlock(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty();
    }

    private static int countSolidNeighbors(WorldGenLevel level, BlockPos pos) {
        int solidNeighbors = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutable.setWithOffset(pos, direction);
            if (isValidFloor(level, mutable, level.getBlockState(mutable))) {
                solidNeighbors++;
            }
        }
        return solidNeighbors;
    }

    private static void scheduleFluidTick(WorldGenLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    private static Block getBlock(ResourceLocation id) {
        Optional<Block> block = BuiltInRegistries.BLOCK.getOptional(id);
        return block.orElse(null);
    }
}
