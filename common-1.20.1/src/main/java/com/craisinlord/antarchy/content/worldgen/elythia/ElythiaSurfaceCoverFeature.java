package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ElythiaSurfaceCoverFeature extends Feature<NoneFeatureConfiguration> {
    public ElythiaSurfaceCoverFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int startX = origin.getX() & ~15;
        int startZ = origin.getZ() & ~15;
        boolean placed = false;

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = startX + localX;
                int worldZ = startZ + localZ;
                int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ) - 1;
                if (topY <= level.getMinBuildHeight()) {
                    continue;
                }

                BlockPos topPos = new BlockPos(worldX, topY, worldZ);
                BlockPos abovePos = topPos.above();
                BlockState topState = level.getBlockState(topPos);
                if (!level.getFluidState(abovePos).isEmpty()) {
                    continue;
                }

                if (!level.canSeeSky(abovePos)) {
                    continue;
                }

                if (!isSurfaceSoil(topState)) {
                    continue;
                }

                level.setBlock(topPos, surfaceState(random), 2);
                this.fillSubsurface(level, topPos, random);
                placed = true;
            }
        }

        return placed;
    }

    private void fillSubsurface(WorldGenLevel level, BlockPos topPos, RandomSource random) {
        int depth = 5 + random.nextInt(2);
        int maxDepth = topPos.getY() - level.getMinBuildHeight();
        for (int step = 1; step <= maxDepth; step++) {
            BlockPos soilPos = topPos.below(step);
            BlockState state = level.getBlockState(soilPos);
            if (!isSurfaceSoil(state) && !state.is(Blocks.GRANITE)) {
                break;
            }

            if (step <= depth) {
                level.setBlock(soilPos, subsurfaceState(random), 2);
                continue;
            }

            if (state.is(Blocks.GRASS_BLOCK)) {
                level.setBlock(soilPos, Blocks.DIRT.defaultBlockState(), 2);
            }
        }
    }

    private static BlockState surfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.7F) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        }
        if (roll < 0.99F) {
            return Blocks.MOSS_BLOCK.defaultBlockState();
        }
        return Blocks.ROOTED_DIRT.defaultBlockState();
    }

    private static BlockState subsurfaceState(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.12F) {
            return Blocks.COARSE_DIRT.defaultBlockState();
        }
        if (roll < 0.18F) {
            return Blocks.ROOTED_DIRT.defaultBlockState();
        }
        return Blocks.DIRT.defaultBlockState();
    }

    private static boolean isExposed(WorldGenLevel level, BlockPos pos) {
        if (level.getBlockState(pos.above()).isAir()) {
            return true;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSurfaceSoil(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.COARSE_DIRT);
    }
}
