package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.GlimmeringReedBlock;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class GlimmeringReedFeature extends Feature<NoneFeatureConfiguration> {
    public GlimmeringReedFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockState reed = AntarchyObjects.GLIMMERING_REED.get().defaultBlockState();
        boolean placedAny = false;

        for (int i = 0; i < 18; i++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos groundPos = new BlockPos(x, surfaceY, z);
            BlockPos reedPos = groundPos.above();
            if (!level.getBlockState(groundPos).isSolid() || !level.isEmptyBlock(reedPos)
                    || !level.isEmptyBlock(reedPos.above()) || !reed.canSurvive(level, reedPos)) {
                continue;
            }

            boolean besideLumen = false;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (level.getFluidState(groundPos.relative(direction)).is(AntarchyTags.Fluids.LUMEN)
                        && level.getFluidState(groundPos.relative(direction)).isSource()) {
                    besideLumen = true;
                    break;
                }
            }
            if (!besideLumen) {
                continue;
            }

            DoublePlantBlock.placeAt(level, reed, reedPos, 2);
            placedAny = true;
        }
        return placedAny;
    }
}
