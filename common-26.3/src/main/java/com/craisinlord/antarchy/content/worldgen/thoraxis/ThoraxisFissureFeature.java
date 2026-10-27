package com.craisinlord.antarchy.content.worldgen.thoraxis;

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

public record ThoraxisFissureFeature(
        int minSpan,
        int maxSpan,
        int minRadius,
        int maxRadius,
        int maxBend,
        float branchChance
) implements Feature {
    public static final MapCodec<ThoraxisFissureFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 256).fieldOf("min_span").forGetter(ThoraxisFissureFeature::minSpan),
            Codec.intRange(1, 256).fieldOf("max_span").forGetter(ThoraxisFissureFeature::maxSpan),
            Codec.intRange(1, 8).fieldOf("min_radius").forGetter(ThoraxisFissureFeature::minRadius),
            Codec.intRange(1, 8).fieldOf("max_radius").forGetter(ThoraxisFissureFeature::maxRadius),
            Codec.intRange(0, 32).fieldOf("max_bend").forGetter(ThoraxisFissureFeature::maxBend),
            Codec.floatRange(0.0F, 1.0F).fieldOf("branch_chance").forGetter(ThoraxisFissureFeature::branchChance)
    ).apply(instance, ThoraxisFissureFeature::new));

    @Override
    public MapCodec<ThoraxisFissureFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int span = randomBetween(random, this.minSpan(), this.maxSpan());
        int halfWidth = randomBetween(random, this.minRadius(), this.maxRadius());
        int bendLimit = Math.max(0, this.maxBend());
        int bendX = bendLimit == 0 ? 0 : random.nextInt(bendLimit * 2 + 1) - bendLimit;
        int bendZ = bendLimit == 0 ? 0 : random.nextInt(bendLimit * 2 + 1) - bendLimit;

        if (origin.getY() <= 4 || origin.getY() >= level.getMaxY() - 4) {
            return false;
        }

        for (int step = -span; step <= span; step++) {
            double t = Math.abs(step) / (double) span;
            int y = origin.getY() + step;
            if (y <= level.getMinY() || y >= level.getMaxY() + 1) {
                continue;
            }

            int centerX = origin.getX() + (int) Math.round(bendX * (t * t - t));
            int centerZ = origin.getZ() + (int) Math.round(bendZ * (t * t - t));
            int width = halfWidth
                    + (step % 4 == 0 ? 1 : 0)
                    + (Math.abs(step) <= span / 4 ? 1 : 0)
                    + (random.nextFloat() < 0.18F ? 1 : 0);

            for (int xOffset = -width; xOffset <= width; xOffset++) {
                for (int zOffset = -width; zOffset <= width; zOffset++) {
                    if (xOffset * xOffset + zOffset * zOffset > width * width + 1) {
                        continue;
                    }

                    BlockPos targetPos = new BlockPos(centerX + xOffset, y, centerZ + zOffset);
                    carveNode(level, targetPos, random, width);
                    if (random.nextFloat() < this.branchChance() && y + 1 < level.getMaxY() + 1) {
                        carveNode(level, targetPos.offset(random.nextInt(3) - 1, random.nextInt(2), random.nextInt(3) - 1), random, Math.max(1, width - 1));
                    }
                    if (random.nextFloat() < 0.1F && y - 1 > level.getMinY()) {
                        carveNode(level, targetPos.below(), random, Math.max(1, width - 1));
                    }
                }
            }
        }

        return true;
    }

    private void carveNode(WorldGenLevel level, BlockPos pos, RandomSource random, int radius) {
        int actualRadius = Math.max(1, radius);
        for (int x = -actualRadius; x <= actualRadius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -actualRadius; z <= actualRadius; z++) {
                    if (x * x + z * z + (y * y * 2) > actualRadius * actualRadius + random.nextInt(3)) {
                        continue;
                    }

                    BlockPos target = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(target);
                    if (!state.isAir()) {
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), 2);
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
