package com.craisinlord.antarchy.neoforge.worldgen;

import com.craisinlord.antarchy.content.block.CornCropBlock;
import com.craisinlord.antarchy.neoforge.registry.AntarchyNeoforgeBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public record CornPatchFeature() implements Feature {
    public static final MapCodec<CornPatchFeature> CODEC = MapCodec.unit(CornPatchFeature::new);

    @Override
    public MapCodec<CornPatchFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Block wildCorn = AntarchyNeoforgeBlocks.WILD_CORN.get();
        int radius = 4 + random.nextInt(3);
        int targetPlacements = 10 + random.nextInt(11);
        int attempts = targetPlacements * 4;
        int placed = 0;

        for (int i = 0; i < attempts && placed < targetPlacements; i++) {
            int x = origin.getX() + random.nextInt(radius * 2 + 1) - radius;
            int z = origin.getZ() + random.nextInt(radius * 2 + 1) - radius;
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            BlockPos lowerPos = new BlockPos(x, surfaceY, z);
            BlockPos upperPos = lowerPos.above();

            if (!level.getBlockState(lowerPos.below()).is(Blocks.GRASS_BLOCK)) {
                continue;
            }
            if (!level.getBlockState(lowerPos).canBeReplaced() || !level.getBlockState(upperPos).canBeReplaced()) {
                continue;
            }

            BlockState lowerState = wildCorn.defaultBlockState()
                    .setValue(CornCropBlock.AGE, 3)
                    .setValue(CornCropBlock.HALF, DoubleBlockHalf.LOWER);
            BlockState upperState = wildCorn.defaultBlockState()
                    .setValue(CornCropBlock.AGE, 3)
                    .setValue(CornCropBlock.HALF, DoubleBlockHalf.UPPER);

            level.setBlock(lowerPos, lowerState, 2);
            level.setBlock(upperPos, upperState, 2);
            placed++;
        }

        return placed > 0;
    }
}
