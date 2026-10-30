package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.chunk.ChunkGenerator;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class LumenLilyPadFeature implements Feature {
    public static final MapCodec<LumenLilyPadFeature> CODEC = MapCodec.unit(LumenLilyPadFeature::new);

    @Override
    public MapCodec<LumenLilyPadFeature> codec() { return CODEC; }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean placedAny = false;

        for (int i = 0; i < 24; i++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos fluidPos = new BlockPos(x, surfaceY, z);
            if (!level.getFluidState(fluidPos).is(AntarchyTags.Fluids.LUMEN) || !level.getFluidState(fluidPos).isSource()) {
                continue;
            }

            BlockPos padPos = fluidPos.above();
            if (!level.isEmptyBlock(padPos)) {
                continue;
            }

            BlockState lilyPad = Blocks.LILY_PAD.defaultBlockState();
            if (!lilyPad.canSurvive(level, padPos)) {
                continue;
            }

            level.setBlock(padPos, lilyPad, 2);
            placedAny = true;
        }

        return placedAny;
    }
}
