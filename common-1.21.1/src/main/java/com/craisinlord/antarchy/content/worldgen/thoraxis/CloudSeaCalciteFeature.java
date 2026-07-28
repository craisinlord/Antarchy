package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class CloudSeaCalciteFeature extends Feature<NoneFeatureConfiguration> {
    public CloudSeaCalciteFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        int banks = 2 + random.nextInt(3);
        boolean placedAny = false;
        for (int i = 0; i < banks; i++) {
            BlockPos center = origin.offset(random.nextInt(25) - 12, random.nextInt(17) - 8, random.nextInt(25) - 12);
            int radiusX = 3 + random.nextInt(4);
            int radiusY = 1 + random.nextInt(3);
            int radiusZ = 3 + random.nextInt(4);
            placedAny |= placeBank(level, center, radiusX, radiusY, radiusZ, random);
        }

        return placedAny;
    }

    private boolean placeBank(WorldGenLevel level, BlockPos center, int radiusX, int radiusY, int radiusZ, RandomSource random) {
        boolean placedAny = false;
        int xRange = Math.max(1, radiusX);
        int yRange = Math.max(1, radiusY);
        int zRange = Math.max(1, radiusZ);
        BlockState cloud = AntarchyObjects.CLOUD_BLOCK.get().defaultBlockState();

        for (int x = -xRange; x <= xRange; x++) {
            for (int y = -yRange; y <= yRange; y++) {
                for (int z = -zRange; z <= zRange; z++) {
                    double normalized = (x * x) / (double) (xRange * xRange)
                            + (y * y) / (double) (yRange * yRange)
                            + (z * z) / (double) (zRange * zRange);
                    if (normalized > 1.0D + random.nextDouble() * 0.18D) {
                        continue;
                    }

                    boolean hollowCore = normalized < 0.58D;
                    if (hollowCore && random.nextFloat() < 0.72F) {
                        BlockPos target = center.offset(x, y, z);
                        BlockState current = level.getBlockState(target);
                        if (current.is(Blocks.BEDROCK)) {
                            continue;
                        }
                        if (current.isAir() || current.canBeReplaced() || !current.getFluidState().isEmpty()) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 2);
                            placedAny = true;
                        }
                        continue;
                    }

                    if (Math.abs(y) == yRange && random.nextFloat() < 0.55F) {
                        continue;
                    }

                    BlockPos target = center.offset(x, y, z);
                    BlockState current = level.getBlockState(target);
                    if (current.is(Blocks.BEDROCK) || (!current.isAir() && !current.canBeReplaced() && current.getFluidState().isEmpty())) {
                        continue;
                    }

                    level.setBlock(target, cloud, 2);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }
}
