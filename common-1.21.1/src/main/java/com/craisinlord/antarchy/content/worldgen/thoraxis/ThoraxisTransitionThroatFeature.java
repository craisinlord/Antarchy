package com.craisinlord.antarchy.content.worldgen.thoraxis;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class ThoraxisTransitionThroatFeature extends Feature<ThoraxisTransitionThroatConfiguration> {
    private static final int UPPER_SCAN_LIMIT = 96;

    public ThoraxisTransitionThroatFeature() {
        super(ThoraxisTransitionThroatConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ThoraxisTransitionThroatConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        boolean large = context.config().large();
        int searchRadius = large ? 18 : 9;
        double baseRadius = large ? 10.0D : 5.0D;
        double radiusVariance = large ? 5.0D : 2.5D;
        double layerOffsetScale = large ? 4.0D : 2.0D;

        BlockPos center = new BlockPos(origin.getX(), ThoraxisUndersideManager.GRAVITY_FLIP_Y, origin.getZ());
        int upperSurface = findUpperSurface(level, center);
        int undersideSurface = findUndersideSurface(level, center);
        if (upperSurface < ThoraxisUndersideManager.GRAVITY_FLIP_Y || undersideSurface < ThoraxisUndersideManager.MOUNTAIN_SURFACE_Y) {
            return false;
        }

        long seed = context.random().nextLong();
        int minX = (origin.getX() >> 4 << 4) - 16;
        int minZ = (origin.getZ() >> 4 << 4) - 16;
        int maxX = minX + 47;
        int maxZ = minZ + 47;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int y = undersideSurface; y <= upperSurface; y++) {
            int layerOffsetX = (int) (noise(seed, Math.floorDiv(y, 5), 17) * layerOffsetScale);
            int layerOffsetZ = (int) (noise(seed, Math.floorDiv(y, 5), 31) * layerOffsetScale);
            double radiusX = baseRadius + noise01(seed, Math.floorDiv(y, 11), 43) * radiusVariance;
            double radiusZ = baseRadius + noise01(seed, Math.floorDiv(y, 13), 59) * radiusVariance;

            for (int x = -searchRadius; x <= searchRadius; x++) {
                int worldX = center.getX() + x;
                if (worldX < minX || worldX > maxX) {
                    continue;
                }
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    int worldZ = center.getZ() + z;
                    if (worldZ < minZ || worldZ > maxZ) {
                        continue;
                    }
                    double dx = x - layerOffsetX;
                    double dz = z - layerOffsetZ;
                    double distance = Math.sqrt((dx * dx) / (radiusX * radiusX) + (dz * dz) / (radiusZ * radiusZ));
                    double edgeNoise = noise01(seed, x * 3 + Math.floorDiv(y, 3), z * 3 + 71);
                    if (distance <= 0.72D + edgeNoise * 0.42D) {
                        cursor.set(worldX, y, worldZ);
                        if (!level.getBlockState(cursor).isAir()) {
                            level.setBlock(cursor, air, 2);
                        }
                    }
                }
            }
        }

        return true;
    }

    private static int findUpperSurface(WorldGenLevel level, BlockPos center) {
        int maxY = Math.min(level.getMaxBuildHeight() - 1, ThoraxisUndersideManager.GRAVITY_FLIP_Y + UPPER_SCAN_LIMIT);
        for (int y = ThoraxisUndersideManager.GRAVITY_FLIP_Y; y <= maxY; y++) {
            BlockState state = level.getBlockState(center.atY(y));
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                return y - 1;
            }
        }
        return Integer.MIN_VALUE;
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
        while (y > minY && y >= ThoraxisUndersideManager.MOUNTAIN_SURFACE_Y && !level.getBlockState(center.atY(y - 1)).isAir()) {
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
