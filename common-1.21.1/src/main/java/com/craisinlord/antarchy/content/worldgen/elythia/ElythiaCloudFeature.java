package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Scatters solid puffs of {@link AntarchyObjects#CLOUD_BLOCK} sized and shaped
 * anywhere from thin wispy veils to large, dense masses, with a softly feathered edge.
 */
public final class ElythiaCloudFeature extends Feature<NoneFeatureConfiguration> {
    public ElythiaCloudFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private enum CloudSize {
        WISPY,
        SMALL,
        LARGE
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        CloudSize size = pickSize(random);
        int banks = switch (size) {
            case WISPY -> 1 + random.nextInt(2);
            case SMALL -> 1;
            case LARGE -> 3 + random.nextInt(3);
        };
        int spread = switch (size) {
            case WISPY -> 20;
            case SMALL -> 10;
            case LARGE -> 28;
        };

        boolean placedAny = false;
        for (int i = 0; i < banks; i++) {
            BlockPos center = origin.offset(
                    random.nextInt(spread * 2 + 1) - spread,
                    random.nextInt(9) - 4,
                    random.nextInt(spread * 2 + 1) - spread);
            int[] radii = rollRadii(size, random);
            placedAny |= placeBank(level, center, radii[0], radii[1], radii[2], size, random);
        }

        return placedAny;
    }

    private static CloudSize pickSize(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.40F) return CloudSize.WISPY;
        if (roll < 0.75F) return CloudSize.SMALL;
        return CloudSize.LARGE;
    }

    private static int[] rollRadii(CloudSize size, RandomSource random) {
        return switch (size) {
            case WISPY -> new int[]{5 + random.nextInt(5), 1, 5 + random.nextInt(5)};
            case SMALL -> new int[]{2 + random.nextInt(2), 1 + random.nextInt(2), 2 + random.nextInt(2)};
            case LARGE -> new int[]{6 + random.nextInt(6), 3 + random.nextInt(4), 6 + random.nextInt(6)};
        };
    }

    private boolean placeBank(WorldGenLevel level, BlockPos center, int radiusX, int radiusY, int radiusZ, CloudSize size, RandomSource random) {
        int xRange = Math.max(1, radiusX);
        int yRange = Math.max(1, radiusY);
        int zRange = Math.max(1, radiusZ);
        BlockState cloud = AntarchyObjects.CLOUD_BLOCK.get().defaultBlockState();
        // Clouds are solid puffs, not hollow shells — only the outer rind is feathered for a
        // soft, ragged edge. Wispy clouds get a lower edge-fill chance so they read as thin
        // and sparse without turning the whole body into visible holes.
        float edgeFillChance = size == CloudSize.WISPY ? 0.55F : 0.85F;
        double edgeStart = 0.7D;

        boolean placedAny = false;
        for (int x = -xRange; x <= xRange; x++) {
            for (int y = -yRange; y <= yRange; y++) {
                for (int z = -zRange; z <= zRange; z++) {
                    double normalized = (x * x) / (double) (xRange * xRange)
                            + (y * y) / (double) (yRange * yRange)
                            + (z * z) / (double) (zRange * zRange);
                    if (normalized > 1.0D + random.nextDouble() * 0.1D) {
                        continue;
                    }

                    if (normalized > edgeStart && random.nextFloat() > edgeFillChance) {
                        continue;
                    }

                    BlockPos target = center.offset(x, y, z);
                    BlockState current = level.getBlockState(target);
                    if (!current.isAir() && !current.canBeReplaced()) {
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
