package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class LumenPoolFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SMALL_RADIUS_MIN = 2;
    private static final int SMALL_RADIUS_MAX = 4;
    private static final int LARGE_RADIUS_MIN = 5;
    private static final int LARGE_RADIUS_MAX = 6;

    public LumenPoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos center = findSurfaceCenter(level, origin);
        if (center == null) {
            return false;
        }

        int radius = random.nextInt(8) == 0
                ? randomBetween(random, LARGE_RADIUS_MIN, LARGE_RADIUS_MAX)
                : randomBetween(random, SMALL_RADIUS_MIN, SMALL_RADIUS_MAX);
        int maxDepth = radius >= LARGE_RADIUS_MIN ? randomBetween(random, 4, 6) : randomBetween(random, 3, 4);
        BlockState lumenBlockState = AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState();
        BlockState shellstoneState = AntarchyObjects.SHELLSTONE.get().defaultBlockState();
        BlockState tuffState = Blocks.TUFF.defaultBlockState();
        BlockState mossState = Blocks.MOSS_BLOCK.defaultBlockState();
        int waterlineY = center.getY() - 1;

        int floorY = waterlineY - maxDepth;
        if (floorY <= level.getMinBuildHeight() + 2) {
            return false;
        }

        if (!hasSolidSubstrate(level, center.getX(), center.getZ(), floorY)) {
            return false;
        }

        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -radius - 2; dx <= radius + 2; dx++) {
            for (int dz = -radius - 2; dz <= radius + 2; dz++) {
                double distance = warpedDistance(dx, dz, random, radius);
                if (distance > radius + 0.7D) {
                    continue;
                }

                int worldX = center.getX() + dx;
                int worldZ = center.getZ() + dz;
                int localSurfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) - 1;
                if (localSurfaceY < center.getY() - 2 || localSurfaceY > center.getY() + 1) {
                    continue;
                }

                double normalized = Mth.clamp((float) (distance / Math.max(radius, 1)), 0.0F, 1.25F);
                int basinDepth = Math.max(2, Mth.floor((1.0D - (normalized * normalized)) * maxDepth));
                if (distance <= radius * 0.45D) {
                    basinDepth = Math.max(basinDepth, maxDepth - 1);
                }
                int localWaterlineY = Math.min(waterlineY, localSurfaceY - 1);
                int localFloorY = localWaterlineY - basinDepth;
                if (localFloorY <= level.getMinBuildHeight() + 1) {
                    continue;
                }
                if (!hasSolidSubstrate(level, worldX, worldZ, localFloorY)) {
                    continue;
                }

                boolean edge = distance >= radius - 0.55D;
                boolean shoulder = !edge && distance >= radius - 1.2D;
                boolean innerWall = !edge && distance >= radius - 0.9D;
                int bankTopY = Math.max(localWaterlineY + 1, Math.min(localSurfaceY, center.getY()));

                for (int y = localSurfaceY; y > localFloorY; y--) {
                    mutable.set(worldX, y, worldZ);
                    if (y > localWaterlineY) {
                        level.setBlock(mutable, y <= bankTopY && (edge || shoulder) ? pickWallState(random, shellstoneState, tuffState) : Blocks.AIR.defaultBlockState(), 2);
                        placedAny = true;
                        continue;
                    }

                    if (edge || (innerWall && y >= localFloorY + 1)) {
                        level.setBlock(mutable, pickWallState(random, shellstoneState, tuffState), 2);
                        placedAny = true;
                        continue;
                    }

                    level.setBlock(mutable, LumenWorldgenHelper.lumenState(lumenBlockState, 0), 2);
                    LumenWorldgenHelper.scheduleFluidTick(level, mutable.immutable());
                    placedAny = true;
                }

                mutable.set(worldX, localFloorY, worldZ);
                BlockState floorState = pickFloorState(random, shellstoneState, tuffState, mossState, edge);
                level.setBlock(mutable, floorState, 2);
                placedAny = true;

                clearFloatingVegetation(level, worldX, worldZ, localSurfaceY);
            }
        }

        return placedAny;
    }

    private static void clearFloatingVegetation(WorldGenLevel level, int x, int z, int surfaceY) {
        int topOfDebris = Math.max(surfaceY, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int offset = 1; offset <= 32; offset++) {
            mutable.set(x, topOfDebris + offset, z);
            BlockState state = level.getBlockState(mutable);
            if (!state.isAir() && state.canBeReplaced()) {
                level.setBlock(mutable, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private static BlockPos findSurfaceCenter(WorldGenLevel level, BlockPos origin) {
        int bestY = Integer.MIN_VALUE;
        BlockPos bestPos = null;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos pos = LumenWorldgenHelper.findNaturalFloor(level, x, z, topY, level.getMinBuildHeight() + 1);
                if (pos != null && pos.getY() > bestY) {
                    bestY = pos.getY();
                    bestPos = pos;
                }
            }
        }
        return bestPos;
    }

    private static boolean hasSolidSubstrate(WorldGenLevel level, int x, int z, int floorY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = floorY - 1; y >= floorY - 3; y--) {
            mutable.set(x, y, z);
            if (LumenWorldgenHelper.isSolidSupport(level.getBlockState(mutable))) {
                return true;
            }
        }
        return false;
    }

    private static BlockState pickWallState(RandomSource random, BlockState shellstoneState, BlockState tuffState) {
        return random.nextFloat() < 0.72F ? shellstoneState : tuffState;
    }

    private static BlockState pickFloorState(RandomSource random, BlockState shellstoneState, BlockState tuffState, BlockState mossState, boolean edge) {
        int roll = random.nextInt(100);
        if (roll < 65) {
            return shellstoneState;
        }
        if (roll < 95) {
            return tuffState;
        }
        return edge ? mossState : shellstoneState;
    }

    private static double warpedDistance(int dx, int dz, RandomSource random, int radius) {
        double scaleX = 1.0D + random.nextDouble() * 0.18D;
        double scaleZ = 0.85D + random.nextDouble() * 0.22D;
        double warpX = Math.sin((dz * 0.42D) + random.nextDouble() * Math.PI * 2.0D) * 0.18D;
        double warpZ = Math.cos((dx * 0.48D) + random.nextDouble() * Math.PI * 2.0D) * 0.18D;
        double warpedX = (dx / scaleX) + warpX;
        double warpedZ = (dz / scaleZ) + warpZ;
        return Mth.sqrt((float) ((warpedX * warpedX) + (warpedZ * warpedZ)));
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }
}
