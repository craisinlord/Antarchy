package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class PeachForestMossyBoulderFeature extends Feature<BoulderPaletteConfiguration> {
    private static final int ANGLE_SAMPLES = 16;

    public PeachForestMossyBoulderFeature(Codec<BoulderPaletteConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BoulderPaletteConfiguration> context) {
        WorldGenLevel level = context.level();
        BoulderPaletteConfiguration.Palette palette = context.config().resolve();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos surfacePos = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1, origin.getZ());
        if (!isSoil(level.getBlockState(surfacePos))) {
            return false;
        }

        BlockPos base = surfacePos.below(random.nextInt(2));
        int height = 5 + random.nextInt(4);
        int baseRadius = 3 + random.nextInt(3);
        boolean placed = false;

        double[] silhouetteNoise = generateSilhouetteNoise(random);
        double leanMagnitude = random.nextDouble() * 1.6D;
        double leanAngle = random.nextDouble() * Math.PI * 2.0D;
        double wobbleAmplitude = 0.4D + random.nextDouble() * 0.4D;
        double wobblePhase = random.nextDouble() * Math.PI * 2.0D;

        int topRadius = 1;
        BlockPos topCenter = base;
        for (int y = 0; y < height; y++) {
            double heightFraction = (double) y / (height - 1);
            double taper = Math.cos(heightFraction * Math.PI * 0.5D);
            int radius = Math.max(1, Math.round((float) (baseRadius * taper)));

            double wobble = Math.sin(heightFraction * Math.PI * 1.5D + wobblePhase) * wobbleAmplitude;
            int driftX = (int) Math.round(Math.cos(leanAngle) * leanMagnitude * heightFraction + Math.cos(leanAngle + Math.PI * 0.5D) * wobble);
            int driftZ = (int) Math.round(Math.sin(leanAngle) * leanMagnitude * heightFraction + Math.sin(leanAngle + Math.PI * 0.5D) * wobble);
            BlockPos levelCenter = base.above(y).offset(driftX, 0, driftZ);

            placed |= this.placeDisk(level, levelCenter, radius, heightFraction, silhouetteNoise, random, palette);
            topCenter = levelCenter;
            topRadius = radius;
        }

        if (placed) {
            this.softCapWithSlabs(level, topCenter, topRadius, random, palette);
        }

        return placed;
    }

    private static double[] generateSilhouetteNoise(RandomSource random) {
        double[] noise = new double[ANGLE_SAMPLES];
        for (int i = 0; i < ANGLE_SAMPLES; i++) {
            noise[i] = 0.88D + random.nextDouble() * 0.24D;
        }
        return noise;
    }

    private static double sampleSilhouetteNoise(double[] noise, int x, int z) {
        if (x == 0 && z == 0) {
            return 1.0D;
        }

        double angle = Math.atan2(z, x);
        double normalized = (angle / (Math.PI * 2.0D) + 1.0D) * ANGLE_SAMPLES;
        int index0 = ((int) Math.floor(normalized)) % ANGLE_SAMPLES;
        int index1 = (index0 + 1) % ANGLE_SAMPLES;
        double t = normalized - Math.floor(normalized);
        return Mth.lerp(t, noise[index0], noise[index1]);
    }

    private boolean placeDisk(WorldGenLevel level, BlockPos center, int radius, double heightFraction, double[] silhouetteNoise, RandomSource random, BoulderPaletteConfiguration.Palette palette) {
        boolean placed = false;
        double rr = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double noiseFactor = sampleSilhouetteNoise(silhouetteNoise, x, z);
                double distance = (x * x + z * z) / (Math.max(1.0D, rr) * noiseFactor * noiseFactor);
                if (distance > 1.0D) {
                    continue;
                }

                BlockPos targetPos = center.offset(x, 0, z);
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.isAir() || targetState.canBeReplaced() || isSoil(targetState)) {
                    level.setBlock(targetPos, palette.block(), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private void softCapWithSlabs(WorldGenLevel level, BlockPos center, int radius, RandomSource random, BoulderPaletteConfiguration.Palette palette) {
        if (palette.slab() == null) {
            return;
        }
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos topPos = center.offset(x, 0, z);
                while (topPos.getY() < level.getMaxY() && isBoulderBlock(level.getBlockState(topPos.above()), palette)) {
                    topPos = topPos.above();
                }

                BlockState topState = level.getBlockState(topPos);
                if (!isBoulderBlock(topState, palette)) {
                    continue;
                }

                BlockPos slabPos = topPos.above();
                if (!level.isEmptyBlock(slabPos) || random.nextFloat() > 0.25F) {
                    continue;
                }

                int openSides = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (!isBoulderBlock(level.getBlockState(topPos.relative(direction)), palette)) {
                        openSides++;
                    }
                }

                if (openSides == 0) {
                    continue;
                }

                BlockState slabState = palette.slab();
                level.setBlock(slabPos, slabState, 2);
            }
        }
    }

    private static boolean isBoulderBlock(BlockState state, BoulderPaletteConfiguration.Palette palette) {
        return palette.matches(state);
    }

    private static boolean isSoil(BlockState state) {
        return state.is(ElythiaGeologyFeature.NATURAL_STONE) || state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT);
    }
}
