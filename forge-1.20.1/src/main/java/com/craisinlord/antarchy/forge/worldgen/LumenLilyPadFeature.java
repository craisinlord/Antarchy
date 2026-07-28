package com.craisinlord.antarchy.forge.worldgen;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class LumenLilyPadFeature extends Feature<NoneFeatureConfiguration> {
    public LumenLilyPadFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
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
