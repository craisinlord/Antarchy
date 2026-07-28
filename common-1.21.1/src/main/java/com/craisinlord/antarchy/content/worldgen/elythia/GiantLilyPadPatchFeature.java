package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.GiantLilyPadBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class GiantLilyPadPatchFeature extends Feature<NoneFeatureConfiguration> {
    private final float lotusChance;

    public GiantLilyPadPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        this(codec, 1.0F);
    }

    public GiantLilyPadPatchFeature(Codec<NoneFeatureConfiguration> codec, float lotusChance) {
        super(codec);
        this.lotusChance = lotusChance;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placedAny = false;

        for (int i = 0; i < 12; i++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos fluidPos = new BlockPos(x, surfaceY, z);
            if (!level.getFluidState(fluidPos).is(AntarchyTags.Fluids.GIANT_LILY_PAD_SUPPORTING_FLUIDS) || !level.getFluidState(fluidPos).isSource()) {
                continue;
            }

            BlockPos padPos = fluidPos.above();
            if (!GiantLilyPadBlock.canPlaceStructure(level, padPos)) {
                continue;
            }

            GiantLilyPadBlock.placeStructure(level, padPos, GiantLilyPadBlock.PadRotation.values()[random.nextInt(4)]);
            placedAny = true;

            if (random.nextFloat() < this.lotusChance) {
                placeLotus(level, padPos);
            }
        }

        return placedAny;
    }

    private static void placeLotus(WorldGenLevel level, BlockPos pos) {
        BlockState padState = level.getBlockState(pos);
        if (!padState.is(AntarchyObjects.GIANT_LILY_PAD.get()) || padState.getValue(GiantLilyPadBlock.HAS_LOTUS)) {
            return;
        }

        level.setBlock(pos, padState.setValue(GiantLilyPadBlock.HAS_LOTUS, true), Block.UPDATE_CLIENTS);
    }
}
