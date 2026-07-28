package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ElythiaTuffBoulderFeature extends Feature<NoneFeatureConfiguration> {
    public ElythiaTuffBoulderFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos surfacePos = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1, origin.getZ());
        if (!isSoil(level.getBlockState(surfacePos))) {
            return false;
        }

        BlockPos center = surfacePos.below(random.nextInt(2));
        int blobs = 1 + random.nextInt(2);
        boolean placed = false;
        for (int i = 0; i < blobs; i++) {
            BlockPos blobCenter = center.offset(random.nextInt(5) - 2, random.nextInt(2), random.nextInt(5) - 2);
            int radiusX = 1 + random.nextInt(2);
            int radiusZ = 1 + random.nextInt(2);
            placed |= this.placeEllipsoid(level, blobCenter, radiusX, 1, radiusZ, random);
            if (random.nextFloat() < 0.18F) {
                placed |= this.placeEllipsoid(level, blobCenter.above(), 1, 1, 1 + random.nextInt(2), random);
            }
        }

        if (placed) {
            this.softCapWithSlabs(level, center, 5, random);
        }

        return placed;
    }

    private boolean placeEllipsoid(WorldGenLevel level, BlockPos center, int radiusX, int radiusY, int radiusZ, RandomSource random) {
        boolean placed = false;
        double rx = radiusX * radiusX;
        double ry = radiusY * radiusY;
        double rz = radiusZ * radiusZ;
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double distance = (x * x) / rx + (y * y) / ry + (z * z) / rz;
                    if (distance > 1.0D + random.nextDouble() * 0.18D) {
                        continue;
                    }

                    BlockPos targetPos = center.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.isAir() || targetState.canBeReplaced() || isSoil(targetState)) {
                        level.setBlock(targetPos, Blocks.TUFF.defaultBlockState(), 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private void softCapWithSlabs(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        BlockState slabState = Blocks.TUFF_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos topPos = center.offset(x, 0, z);
                while (topPos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(topPos.above()).is(Blocks.TUFF)) {
                    topPos = topPos.above();
                }

                if (!level.getBlockState(topPos).is(Blocks.TUFF)) {
                    continue;
                }

                BlockPos slabPos = topPos.above();
                if (!level.isEmptyBlock(slabPos) || random.nextFloat() > 0.18F) {
                    continue;
                }

                int openSides = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!level.getBlockState(topPos.relative(direction)).is(Blocks.TUFF)) {
                        openSides++;
                    }
                }

                if (openSides == 0) {
                    continue;
                }

                level.setBlock(slabPos, slabState, 2);
            }
        }
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT);
    }
}
