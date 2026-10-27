package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ThoraxisRibColumnsFeature(
        int minRibs,
        int maxRibs,
        int minLength,
        int maxLength,
        int minThickness,
        int maxThickness,
        int curveStrength,
        float clawGapChance
) implements Feature {
    public static final MapCodec<ThoraxisRibColumnsFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 24).fieldOf("min_ribs").forGetter(ThoraxisRibColumnsFeature::minRibs),
            Codec.intRange(1, 24).fieldOf("max_ribs").forGetter(ThoraxisRibColumnsFeature::maxRibs),
            Codec.intRange(1, 64).fieldOf("min_length").forGetter(ThoraxisRibColumnsFeature::minLength),
            Codec.intRange(1, 64).fieldOf("max_length").forGetter(ThoraxisRibColumnsFeature::maxLength),
            Codec.intRange(1, 8).fieldOf("min_thickness").forGetter(ThoraxisRibColumnsFeature::minThickness),
            Codec.intRange(1, 8).fieldOf("max_thickness").forGetter(ThoraxisRibColumnsFeature::maxThickness),
            Codec.intRange(0, 16).fieldOf("curve_strength").forGetter(ThoraxisRibColumnsFeature::curveStrength),
            Codec.floatRange(0.0F, 1.0F).fieldOf("claw_gap_chance").forGetter(ThoraxisRibColumnsFeature::clawGapChance)
    ).apply(instance, ThoraxisRibColumnsFeature::new));

    @Override
    public MapCodec<ThoraxisRibColumnsFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int ribs = randomBetween(random, this.minRibs(), this.maxRibs());
        boolean upward = origin.getY() < level.getHeight() / 2;
        int direction = upward ? 1 : -1;
        int spread = 2 + random.nextInt(4);

        for (int rib = 0; rib < ribs; rib++) {
            double phase = (rib - (ribs - 1) / 2.0D) / Math.max(1.0D, ribs - 1.0D);
            int xShift = (int) Math.round(phase * spread * 2.0D);
            int zShift = random.nextInt(3) - 1;
            int length = randomBetween(random, this.minLength(), this.maxLength());

            for (int step = 0; step < length; step++) {
                double progress = step / (double) Math.max(1, length - 1);
                double curve = Math.sin(progress * Math.PI * 0.85D);
                int bend = randomBetween(random, 0, Math.max(0, this.curveStrength()));
                int x = origin.getX() + xShift + (int) Math.round(curve * phase * spread) + (direction * bend / 4);
                int z = origin.getZ() + zShift + (int) Math.round(curve * (random.nextBoolean() ? 1 : -1)) - (direction * bend / 6);
                int y = origin.getY() + (step * direction) + (int) Math.round(progress * progress * direction * 2.5D);
                BlockPos pos = new BlockPos(x, y, z);

                int thickness = randomBetween(random, this.minThickness(), this.maxThickness())
                        + (step > length / 2 ? 1 : 0)
                        + (random.nextFloat() < this.clawGapChance() ? 1 : 0);
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
