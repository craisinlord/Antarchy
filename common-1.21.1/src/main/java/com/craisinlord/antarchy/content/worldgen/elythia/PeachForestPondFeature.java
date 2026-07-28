package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class PeachForestPondFeature extends Feature<PeachForestPondConfiguration> {
    private static final int NOISE_SAMPLES = 12;
    private static final ResourceLocation CAMELLIA_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "camellia");
    private static final ResourceLocation SPIDER_LILY_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "spider_lily");

    public PeachForestPondFeature(Codec<PeachForestPondConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<PeachForestPondConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        PeachForestPondConfiguration config = context.config();

        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()) - 1;
        BlockPos center = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        if (!isSoil(level.getBlockState(center))) {
            return false;
        }

        int radiusX = Mth.nextInt(random, config.minRadius(), config.maxRadius());
        int radiusZ = Mth.clamp(radiusX + random.nextInt(3) - 1, config.minRadius(), config.maxRadius());
        int depth = Mth.nextInt(random, config.minDepth(), config.maxDepth());
        double[] edgeNoise = generateEdgeNoise(random);
        boolean placed = false;

        int rimHeight = 2 + Math.max(0, (Math.max(radiusX, radiusZ) - 10) / 6);
        int rimTopY = center.getY() + rimHeight;

        for (int x = -radiusX - 2; x <= radiusX + 2; x++) {
            for (int z = -radiusZ - 2; z <= radiusZ + 2; z++) {
                double noiseFactor = sampleEdgeNoise(edgeNoise, x, z);
                double distance = ((x * x) / (double) (radiusX * radiusX) + (z * z) / (double) (radiusZ * radiusZ)) / (noiseFactor * noiseFactor);
                BlockPos topPos = center.offset(x, 0, z);

                if (distance <= 1.0D) {
                    int floorY = center.getY() - (distance < 0.4D ? depth + 1 : depth);
                    for (int y = center.getY(); y > floorY; y--) {
                        BlockPos waterPos = new BlockPos(topPos.getX(), y, topPos.getZ());
                        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
                        LumenWorldgenHelper.scheduleFluidTick(level, waterPos);
                    }
                    level.setBlock(new BlockPos(topPos.getX(), floorY, topPos.getZ()), Blocks.TUFF.defaultBlockState(), 2);
                    clearFloatingVegetation(level, topPos, center.getY());
                    placed = true;
                } else if (distance <= 1.2D) {
                    int floorY = center.getY() - depth;
                    for (int y = rimTopY; y >= floorY; y--) {
                        level.setBlock(new BlockPos(topPos.getX(), y, topPos.getZ()), Blocks.TUFF.defaultBlockState(), 2);
                    }
                    clearFloatingVegetation(level, topPos, rimTopY);
                    placed = true;
                } else if (distance <= 1.45D && isSoil(level.getBlockState(topPos))) {
                    level.setBlock(topPos, Blocks.TUFF.defaultBlockState(), 2);
                    clearFloatingVegetation(level, topPos, center.getY());
                }
            }
        }

        if (placed) {
            this.placeLilyPads(level, center, radiusX, radiusZ, edgeNoise, random);
            this.placeFrogspawn(level, center, radiusX, radiusZ, edgeNoise, random);
        }

        return placed;
    }

    private static double[] generateEdgeNoise(RandomSource random) {
        double[] noise = new double[NOISE_SAMPLES];
        for (int i = 0; i < NOISE_SAMPLES; i++) {
            noise[i] = 0.72D + random.nextDouble() * 0.56D;
        }
        return noise;
    }

    private static double sampleEdgeNoise(double[] edgeNoise, int x, int z) {
        if (x == 0 && z == 0) {
            return 1.0D;
        }

        double angle = Math.atan2(z, x);
        double normalized = (angle / (Math.PI * 2.0D) + 1.0D) * NOISE_SAMPLES;
        int index0 = ((int) Math.floor(normalized)) % NOISE_SAMPLES;
        int index1 = (index0 + 1) % NOISE_SAMPLES;
        double t = normalized - Math.floor(normalized);
        return Mth.lerp(t, edgeNoise[index0], edgeNoise[index1]);
    }

    private static void clearFloatingVegetation(WorldGenLevel level, BlockPos topPos, int surfaceY) {
        int topOfDebris = Math.max(surfaceY, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, topPos.getX(), topPos.getZ()) - 1);
        for (int y = surfaceY + 1; y <= topOfDebris + 32; y++) {
            BlockPos abovePos = new BlockPos(topPos.getX(), y, topPos.getZ());
            BlockState aboveState = level.getBlockState(abovePos);
            if (isPeachFlower(aboveState)) {
                removePeachFlower(level, abovePos, aboveState);
                continue;
            }
            if (!aboveState.isAir() && aboveState.canBeReplaced()) {
                level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private static boolean isPeachFlower(BlockState state) {
        return state.is(BuiltInRegistries.BLOCK.get(CAMELLIA_ID)) || state.is(BuiltInRegistries.BLOCK.get(SPIDER_LILY_ID));
    }

    private static void removePeachFlower(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        if (state.is(BuiltInRegistries.BLOCK.get(CAMELLIA_ID)) && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockPos otherHalfPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
            if (level.getBlockState(otherHalfPos).is(BuiltInRegistries.BLOCK.get(CAMELLIA_ID))) {
                level.setBlock(otherHalfPos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private void placeLilyPads(WorldGenLevel level, BlockPos center, int radiusX, int radiusZ, double[] edgeNoise, RandomSource random) {
        int attempts = 6 + (radiusX + radiusZ) * 2;
        for (int i = 0; i < attempts; i++) {
            int x = random.nextInt(radiusX * 2 + 1) - radiusX;
            int z = random.nextInt(radiusZ * 2 + 1) - radiusZ;
            double noiseFactor = sampleEdgeNoise(edgeNoise, x, z);
            double distance = ((x * x) / (double) (radiusX * radiusX) + (z * z) / (double) (radiusZ * radiusZ)) / (noiseFactor * noiseFactor);
            if (distance > 0.85D || random.nextFloat() > 0.55F) {
                continue;
            }

            BlockPos padPos = center.offset(x, 1, z);
            if (!level.getFluidState(padPos.below()).is(FluidTags.WATER) || !level.isEmptyBlock(padPos)) {
                continue;
            }

            BlockState lilyPad = Blocks.LILY_PAD.defaultBlockState();
            if (!lilyPad.canSurvive(level, padPos)) {
                continue;
            }

            level.setBlock(padPos, lilyPad, 2);
        }
    }

    private void placeFrogspawn(WorldGenLevel level, BlockPos center, int radiusX, int radiusZ, double[] edgeNoise, RandomSource random) {
        int attempts = 2 + (radiusX + radiusZ) / 3;
        for (int i = 0; i < attempts; i++) {
            int x = random.nextInt(radiusX * 2 + 1) - radiusX;
            int z = random.nextInt(radiusZ * 2 + 1) - radiusZ;
            double noiseFactor = sampleEdgeNoise(edgeNoise, x, z);
            double distance = ((x * x) / (double) (radiusX * radiusX) + (z * z) / (double) (radiusZ * radiusZ)) / (noiseFactor * noiseFactor);
            if (distance > 0.8D || random.nextFloat() > 0.45F) {
                continue;
            }

            BlockPos spawnPos = center.offset(x, 1, z);
            if (!level.getFluidState(spawnPos.below()).is(FluidTags.WATER) || !level.isEmptyBlock(spawnPos)) {
                continue;
            }

            BlockState frogspawn = Blocks.FROGSPAWN.defaultBlockState();
            if (!frogspawn.canSurvive(level, spawnPos)) {
                continue;
            }

            level.setBlock(spawnPos, frogspawn, 2);
        }
    }

    private static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.ROOTED_DIRT);
    }
}
