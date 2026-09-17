package com.craisinlord.antarchy.content.worldgen.thoraxis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ThoraxisTransitionThroatFeature extends Feature<NoneFeatureConfiguration> {
    public ThoraxisTransitionThroatFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos center = new BlockPos(origin.getX(), ThoraxisUndersideManager.GRAVITY_FLIP_Y, origin.getZ());
        int upperSurface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ()) - 1;
        int undersideSurface = findUndersideSurface(level, center);
        if (upperSurface < ThoraxisUndersideManager.GRAVITY_FLIP_Y || undersideSurface < level.getMinBuildHeight()) {
            return false;
        }

        long seed = context.random().nextLong();
        for (int y = undersideSurface; y <= upperSurface; y++) {
            int layerOffsetX = noise(seed, Math.floorDiv(y, 5), 17) * 2;
            int layerOffsetZ = noise(seed, Math.floorDiv(y, 5), 31) * 2;
            double radiusX = 5.0D + noise01(seed, Math.floorDiv(y, 11), 43) * 2.5D;
            double radiusZ = 5.0D + noise01(seed, Math.floorDiv(y, 13), 59) * 2.5D;

            for (int x = -9; x <= 9; x++) {
                for (int z = -9; z <= 9; z++) {
                    double dx = x - layerOffsetX;
                    double dz = z - layerOffsetZ;
                    double distance = Math.sqrt((dx * dx) / (radiusX * radiusX) + (dz * dz) / (radiusZ * radiusZ));
                    double edgeNoise = noise01(seed, x * 3 + Math.floorDiv(y, 3), z * 3 + 71);
                    if (distance <= 0.72D + edgeNoise * 0.42D) {
                        level.setBlock(center.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }

    private static int findUndersideSurface(WorldGenLevel level, BlockPos center) {
        int minY = level.getMinBuildHeight();
        int firstSolid = Integer.MIN_VALUE;
        for (int y = ThoraxisUndersideManager.GRAVITY_FLIP_Y - 1; y >= minY; y--) {
            if (!level.getBlockState(center.atY(y)).isAir()) {
                firstSolid = y;
                break;
            }
        }
        if (firstSolid == Integer.MIN_VALUE) {
            return minY;
        }

        int y = firstSolid;
        while (y > minY && !level.getBlockState(center.atY(y - 1)).isAir()) {
            y--;
        }
        return y;
    }

    private static double noise01(long seed, int x, int z) {
        long value = seed + 0x9E3779B97F4A7C15L;
        value ^= x * 0x632BE59BD9B4E019L;
        value ^= z * 0xC6BC279692B5CC83L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53;
    }

    private static int noise(long seed, int x, int z) {
        return noise01(seed, x, z) < 0.5D ? -1 : 1;
    }
}
