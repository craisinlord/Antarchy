package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class ThoraxisRibColumnsFeature extends Feature<ThoraxisRibColumnsConfiguration> {
    public ThoraxisRibColumnsFeature(Codec<ThoraxisRibColumnsConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ThoraxisRibColumnsConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        ThoraxisRibColumnsConfiguration config = context.config();

        int ribs = randomBetween(random, config.minRibs(), config.maxRibs());
        boolean upward = origin.getY() < level.getHeight() / 2;
        int direction = upward ? 1 : -1;
        int spread = 2 + random.nextInt(4);

        for (int rib = 0; rib < ribs; rib++) {
            double phase = (rib - (ribs - 1) / 2.0D) / Math.max(1.0D, ribs - 1.0D);
            int xShift = (int) Math.round(phase * spread * 2.0D);
            int zShift = random.nextInt(3) - 1;
            int length = randomBetween(random, config.minLength(), config.maxLength());

            for (int step = 0; step < length; step++) {
                double progress = step / (double) Math.max(1, length - 1);
                double curve = Math.sin(progress * Math.PI * 0.85D);
                int bend = randomBetween(random, 0, Math.max(0, config.curveStrength()));
                int x = origin.getX() + xShift + (int) Math.round(curve * phase * spread) + (direction * bend / 4);
                int z = origin.getZ() + zShift + (int) Math.round(curve * (random.nextBoolean() ? 1 : -1)) - (direction * bend / 6);
                int y = origin.getY() + (step * direction) + (int) Math.round(progress * progress * direction * 2.5D);
                BlockPos pos = new BlockPos(x, y, z);

                int thickness = randomBetween(random, config.minThickness(), config.maxThickness())
                        + (step > length / 2 ? 1 : 0)
                        + (random.nextFloat() < config.clawGapChance() ? 1 : 0);
                placeClaw(level, pos, thickness, random);
            }
        }

        return true;
    }

    private void placeClaw(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        int actualRadius = Math.max(1, radius);
        for (int x = -actualRadius; x <= actualRadius; x++) {
            for (int y = -actualRadius; y <= actualRadius; y++) {
                for (int z = -actualRadius; z <= actualRadius; z++) {
                    if (x * x + y * y + z * z > actualRadius * actualRadius + random.nextInt(2)) {
                        continue;
                    }

                    if (Math.abs(y) > actualRadius - 1 && random.nextFloat() < 0.4F) {
                        continue;
                    }

                    BlockPos target = center.offset(x, y, z);
                    BlockState state = level.getBlockState(target);
                    if (state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty()) {
                        level.setBlock(target, com.craisinlord.antarchy.content.AntarchyObjects.NYXITE.get().defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }
}
