package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.PotentNyxiteBlock;
import com.craisinlord.antarchy.content.block.state.PotentNyxiteState;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record PotentNyxiteFeature() implements Feature {
    public static final MapCodec<PotentNyxiteFeature> CODEC = MapCodec.unit(PotentNyxiteFeature::new);

    @Override
    public MapCodec<PotentNyxiteFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean placedAny = false;

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                for (int dy = -5; dy <= 5; dy++) {
                    BlockPos candidatePos = origin.offset(dx, dy, dz);
                    if (!level.getBlockState(candidatePos).is(AntarchyObjects.NYXITE.get())) {
                        continue;
                    }

                    if (random.nextFloat() >= 0.12F) {
                        continue;
                    }

                    BlockState potentNyxite = AntarchyObjects.POTENT_NYXITE.get()
                            .defaultBlockState()
                            .setValue(PotentNyxiteBlock.STATE, PotentNyxiteState.DRY);

                    level.setBlock(candidatePos, potentNyxite, 2);
                    BlockPos abovePos = candidatePos.above();
                    if (level.getBlockState(abovePos).isAir()) {
                        level.setBlock(abovePos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                    }
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

}
