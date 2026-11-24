package com.craisinlord.antarchy.content.worldgen.overworld;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CornPatchFeature extends Feature<NoneFeatureConfiguration> {
    public CornPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Block wildCorn = AntarchyObjects.WILD_CORN.get();
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

            DoublePlantBlock.placeAt(level, wildCorn.defaultBlockState(), lowerPos, 2);
            placed++;
        }

        return placed > 0;
    }
}
