package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ElythiaPondFeature extends Feature<NoneFeatureConfiguration> {
    public ElythiaPondFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        BlockPos center = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        if (!isSoil(level.getBlockState(center))) {
            return false;
        }

        int radiusX = 3 + random.nextInt(3);
        int radiusZ = 3 + random.nextInt(3);
        int depth = 1 + random.nextInt(2);
        boolean placed = false;

        for (int x = -radiusX - 1; x <= radiusX + 1; x++) {
            for (int z = -radiusZ - 1; z <= radiusZ + 1; z++) {
                double distance = (x * x) / (double) (radiusX * radiusX) + (z * z) / (double) (radiusZ * radiusZ);
                BlockPos topPos = center.offset(x, 0, z);
                if (distance <= 1.0D) {
                    int floorY = center.getY() - (distance < 0.35D ? depth + 1 : depth);
                    for (int y = center.getY(); y > floorY; y--) {
                        BlockPos waterPos = new BlockPos(topPos.getX(), y, topPos.getZ());
                        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
                    }
                    level.setBlock(new BlockPos(topPos.getX(), floorY, topPos.getZ()), Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                    placed = true;
                } else if (distance <= 1.25D && isSoil(level.getBlockState(topPos))) {
                    level.setBlock(topPos, Blocks.MOSS_BLOCK.defaultBlockState(), 2);
                }
            }
        }

        return placed;
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT);
    }
}
