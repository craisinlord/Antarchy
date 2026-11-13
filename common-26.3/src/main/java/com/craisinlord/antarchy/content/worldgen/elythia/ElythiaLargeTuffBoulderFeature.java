package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ElythiaLargeTuffBoulderFeature implements Feature {
    public static final MapCodec<ElythiaLargeTuffBoulderFeature> CODEC = BoulderPaletteConfiguration.CODEC.xmap(ElythiaLargeTuffBoulderFeature::new, ElythiaLargeTuffBoulderFeature::config);

    private final BoulderPaletteConfiguration config;

    private static final int FOOTPRINT = 7;
    private static final int MAX_DROP = 2;

    public ElythiaLargeTuffBoulderFeature(BoulderPaletteConfiguration config) {
        this.config = config;
    }

    public BoulderPaletteConfiguration config() {
        return this.config;
    }

    @Override
    public MapCodec<ElythiaLargeTuffBoulderFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BoulderPaletteConfiguration.Palette palette = this.config.resolve();
        BlockPos surfacePos = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1, origin.getZ());
        if (!isSoil(level.getBlockState(surfacePos))) {
            return false;
        }
        if (!isLevelGround(level, surfacePos)) {
            return false;
        }

        BlockPos center = surfacePos.above(2);
        int blobs = 1 + random.nextInt(3);
        boolean placed = this.placeEllipsoid(level, center, 2 + random.nextInt(2), 2 + random.nextInt(2), 2 + random.nextInt(2), random, palette);
        for (int i = 0; i < blobs; i++) {
            BlockPos blobCenter = center.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            int radiusX = 2 + random.nextInt(2);
            int radiusY = 2 + random.nextInt(2);
            int radiusZ = 2 + random.nextInt(2);
            placed |= this.placeEllipsoid(level, blobCenter, radiusX, radiusY, radiusZ, random, palette);
            if (random.nextFloat() < 0.35F) {
                placed |= this.placeEllipsoid(level, blobCenter.above(), Math.max(2, radiusX - 1), 1, Math.max(2, radiusZ - 1), random, palette);
            }
        }

        if (placed) {
            this.softCapWithSlabs(level, center, 7, random, palette);
        }

        return placed;
    }

    private boolean placeEllipsoid(WorldGenLevel level, BlockPos center, int radiusX, int radiusY, int radiusZ, RandomSource random, BoulderPaletteConfiguration.Palette palette) {
        boolean placed = false;
        double rx = radiusX * radiusX;
        double ry = radiusY * radiusY;
        double rz = radiusZ * radiusZ;
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double distance = (x * x) / rx + (y * y) / ry + (z * z) / rz;
                    if (distance > 1.0D + random.nextDouble() * 0.2D) {
                        continue;
                    }

                    BlockPos targetPos = center.offset(x, y, z);
                    BlockState targetState = level.getBlockState(targetPos);
                    if (targetState.isAir() || targetState.canBeReplaced() || isSoil(targetState)) {
                        level.setBlock(targetPos, palette.block(), 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private void softCapWithSlabs(WorldGenLevel level, BlockPos center, int radius, RandomSource random, BoulderPaletteConfiguration.Palette palette) {
        if (palette.slab() == null) {
            return;
        }
        BlockState slabState = palette.slab();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos topPos = center.offset(x, 7, z);
                while (topPos.getY() > center.getY() - 5 && !level.getBlockState(topPos).is(palette.block().getBlock())) {
                    topPos = topPos.below();
                }

                if (!level.getBlockState(topPos).is(palette.block().getBlock())) {
                    continue;
                }

                BlockPos slabPos = topPos.above();
                if (!level.isEmptyBlock(slabPos) || random.nextFloat() > 0.22F) {
                    continue;
                }

                int openSides = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!level.getBlockState(topPos.relative(direction)).is(palette.block().getBlock())) {
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

    private static boolean isLevelGround(WorldGenLevel level, BlockPos surfacePos) {
        for (int x = -FOOTPRINT; x <= FOOTPRINT; x += FOOTPRINT) {
            for (int z = -FOOTPRINT; z <= FOOTPRINT; z += FOOTPRINT) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, surfacePos.getX() + x, surfacePos.getZ() + z) - 1;
                if (Math.abs(y - surfacePos.getY()) > MAX_DROP) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isSoil(BlockState state) {
        return state.is(ElythiaGeologyFeature.NATURAL_STONE) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT);
    }
}
