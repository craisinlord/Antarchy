package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ThoraxisSpikeFeature(
        Orientation orientation,
        int minLength,
        int maxLength,
        int baseRadius,
        int tipRadius,
        int sway,
        float branchChance
) implements Feature {
    public enum Orientation {
        UP,
        DOWN;

        public static Orientation fromName(String name) {
            for (Orientation value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown Thoraxis spike orientation: " + name);
        }
    }

    public static final Codec<Orientation> ORIENTATION_CODEC = Codec.STRING.xmap(Orientation::fromName, Orientation::name);
    public static final MapCodec<ThoraxisSpikeFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ORIENTATION_CODEC.fieldOf("orientation").forGetter(ThoraxisSpikeFeature::orientation),
            Codec.intRange(1, 256).fieldOf("min_length").forGetter(ThoraxisSpikeFeature::minLength),
            Codec.intRange(1, 256).fieldOf("max_length").forGetter(ThoraxisSpikeFeature::maxLength),
            Codec.intRange(1, 24).fieldOf("base_radius").forGetter(ThoraxisSpikeFeature::baseRadius),
            Codec.intRange(1, 24).fieldOf("tip_radius").forGetter(ThoraxisSpikeFeature::tipRadius),
            Codec.intRange(0, 32).fieldOf("sway").forGetter(ThoraxisSpikeFeature::sway),
            Codec.floatRange(0.0F, 1.0F).fieldOf("branch_chance").forGetter(ThoraxisSpikeFeature::branchChance)
    ).apply(instance, ThoraxisSpikeFeature::new));

    @Override
    public MapCodec<ThoraxisSpikeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int length = randomBetween(random, this.minLength(), this.maxLength());
        int direction = this.orientation() == Orientation.UP ? 1 : -1;
        int baseRadius = Math.max(1, this.baseRadius());
        int tipRadius = Math.max(1, this.tipRadius());
        int sway = Math.max(0, this.sway());
        boolean placedAny = false;

        for (int step = 0; step < length; step++) {
            double progress = length <= 1 ? 0.0D : step / (double) (length - 1);
            int y = origin.getY() + step * direction;
            if (y <= level.getMinY() || y >= level.getMaxY() + 1) {
                break;
            }

            double wave = Math.sin(progress * Math.PI * 1.5D);
            int x = origin.getX() + (int) Math.round(wave * sway * 0.35D) + random.nextInt(3) - 1;
            int z = origin.getZ() + (int) Math.round(Math.cos(progress * Math.PI * 1.25D) * sway * 0.35D) + random.nextInt(3) - 1;
            BlockPos center = new BlockPos(x, y, z);
            int radius = Math.max(tipRadius, (int) Math.round(lerp(progress, baseRadius, tipRadius)));
            placedAny |= placeSphere(level, center, radius, random);

            if (step > 0 && step < length - 1 && random.nextFloat() < this.branchChance()) {
                BlockPos branchCenter = center.offset(random.nextInt(5) - 2, random.nextInt(3) - 1, random.nextInt(5) - 2);
                placedAny |= placeSphere(level, branchCenter, Math.max(1, radius - 1), random);
            }
        }

        return placedAny;
    }

    private boolean placeSphere(WorldGenLevel level, BlockPos center, int radius, RandomSource random) {
        boolean placedAny = false;
        int actualRadius = Math.max(1, radius);
        for (int x = -actualRadius; x <= actualRadius; x++) {
            for (int y = -actualRadius; y <= actualRadius; y++) {
                for (int z = -actualRadius; z <= actualRadius; z++) {
                    double distance = x * x + y * y + z * z;
                    if (distance > actualRadius * actualRadius + random.nextInt(3)) {
                        continue;
                    }

                    BlockPos target = center.offset(x, y, z);
                    BlockState state = level.getBlockState(target);
                    if (state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    if (!state.isAir() || !state.canBeReplaced() || !state.getFluidState().isEmpty() || random.nextFloat() < 0.65F) {
                        level.setBlock(target, AntarchyObjects.NYXITE.get().defaultBlockState(), 2);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    private static double lerp(double t, double a, double b) {
        return a + (b - a) * t;
    }
}
