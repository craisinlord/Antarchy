package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class ThoraxisDuneFeature extends Feature<ThoraxisDuneConfiguration> {
    public ThoraxisDuneFeature(Codec<ThoraxisDuneConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<ThoraxisDuneConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        ThoraxisDuneConfiguration config = context.config();

        int radius = randomBetween(random, config.minRadius(), config.maxRadius());
        int height = randomBetween(random, config.minHeight(), config.maxHeight());
        BlockState topState = config.topState().getState(random, origin);
        BlockState underState = config.underState().getState(random, origin);
        boolean placedAny = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                double normalized = distance / Math.max(1.0D, radius);
                if (normalized > 1.0D) {
                    continue;
                }

                double shape = 1.0D - normalized;
                double roughness = 0.70D + (random.nextDouble() * 0.30D);
                double mound = height * shape * roughness;
                if (random.nextFloat() < config.irregularity()) {
                    mound *= 0.65D + random.nextDouble() * 0.45D;
                }

                int columnHeight = Math.max(1, (int) Math.round(mound));
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                if (surfaceY <= level.getMinBuildHeight()) {
                    continue;
                }
                if (level.getBlockState(new BlockPos(x, surfaceY, z)).is(Blocks.BEDROCK)) {
                    continue;
                }

                int baseY = surfaceY + 1;
                for (int layer = 0; layer < columnHeight; layer++) {
                    int y = baseY + layer;
                    if (y >= level.getMaxBuildHeight() - 1) {
                        break;
                    }

                    boolean exposedBand = layer < columnHeight - 1
                            && layer > 0
                            && (layer % 3 == 0 || layer % 4 == 1)
                            && random.nextFloat() < 0.42D + (config.irregularity() * 0.35D);
                    BlockState state = layer == columnHeight - 1 || exposedBand ? topState : underState;
                    level.setBlock(new BlockPos(x, y, z), state, 2);
                    placedAny = true;
                }

                if (columnHeight < config.underDepth()) {
                    for (int layer = 0; layer < config.underDepth() - columnHeight; layer++) {
                        int y = surfaceY - layer;
                        if (y <= level.getMinBuildHeight() || level.getBlockState(new BlockPos(x, y, z)).is(Blocks.BEDROCK)) {
                            break;
                        }
                        if (layer > 1 && layer < config.underDepth() - 2 && random.nextFloat() < 0.22D + (config.irregularity() * 0.20D)) {
                            level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                            placedAny = true;
                            continue;
                        }
                        level.setBlock(new BlockPos(x, y, z), underState, 2);
                        placedAny = true;
                    }
                }

                if (columnHeight > 2 && random.nextFloat() < 0.34D + config.irregularity() * 0.22D) {
                    int cavityRadius = 1 + random.nextInt(2);
                    int cavityY = baseY + Math.max(1, columnHeight / 2) - random.nextInt(Math.max(1, columnHeight / 3 + 1));
                    carveBuriedCavity(level, x, cavityY, z, cavityRadius, random);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private void carveBuriedCavity(WorldGenLevel level, int centerX, int centerY, int centerZ, int radius, RandomSource random) {
        int actualRadius = Math.max(1, radius);
        for (int x = -actualRadius; x <= actualRadius; x++) {
            for (int y = -actualRadius; y <= actualRadius; y++) {
                for (int z = -actualRadius; z <= actualRadius; z++) {
                    double distance = x * x + (y * y * 1.5D) + z * z;
                    if (distance > actualRadius * actualRadius + random.nextInt(2)) {
                        continue;
                    }

                    BlockPos target = new BlockPos(centerX + x, centerY + y, centerZ + z);
                    BlockState state = level.getBlockState(target);
                    if (state.is(Blocks.BEDROCK)) {
                        continue;
                    }
                    if (!state.isAir() && !state.canBeReplaced() && state.getFluidState().isEmpty()) {
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
