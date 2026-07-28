package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class LumenSpireFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_HEIGHT = 15;
    private static final int MAX_HEIGHT = 20;
    private static final int MIN_LEVELS = 3;
    private static final int MAX_LEVELS = 4;
    private static final int SHAFT_RADIUS = 3;
    private static final int SHELF_RADIUS = SHAFT_RADIUS + 2;
    private static final int POOL_RADIUS = 2;

    public LumenSpireFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos base = findSurfaceCenter(level, origin);
        if (base == null) {
            return false;
        }

        int height = randomBetween(random, MIN_HEIGHT, MAX_HEIGHT);
        int levels = randomBetween(random, MIN_LEVELS, MAX_LEVELS);
        if (base.getY() + height >= level.getMaxBuildHeight() - 2) {
            return false;
        }

        BlockState shellstoneState = AntarchyObjects.SHELLSTONE.get().defaultBlockState();
        BlockState tuffState = Blocks.TUFF.defaultBlockState();
        BlockState mossState = Blocks.MOSS_BLOCK.defaultBlockState();
        BlockState lumenBlockState = AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState();

        Direction cascadeDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int cascadeX = cascadeDir.getStepX() * (SHAFT_RADIUS + 1);
        int cascadeZ = cascadeDir.getStepZ() * (SHAFT_RADIUS + 1);
        int lipX = cascadeDir.getStepX() * (SHAFT_RADIUS + 1 + POOL_RADIUS);
        int lipZ = cascadeDir.getStepZ() * (SHAFT_RADIUS + 1 + POOL_RADIUS);

        boolean placed = false;
        for (int y = 0; y <= height; y++) {
            double heightFraction = (double) y / height;
            int radius = Math.max(2, Mth.floor(SHAFT_RADIUS * (1.0D - heightFraction * 0.35D)));
            placed |= placePillarSlice(level, base.above(y), radius, random, shellstoneState, tuffState, mossState);
        }

        int[] terraceHeights = new int[levels];
        for (int i = 0; i < levels; i++) {
            terraceHeights[i] = Mth.floor(height * (double) (i + 1) / levels);
        }

        int lipWorldX = base.getX() + lipX;
        int lipWorldZ = base.getZ() + lipZ;
        int groundLandingY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, lipWorldX, lipWorldZ) - 1;

        for (int i = levels - 1; i >= 0; i--) {
            int terraceY = base.getY() + terraceHeights[i];
            BlockPos shelfCenter = new BlockPos(base.getX(), terraceY, base.getZ());
            placeShelfRing(level, shelfCenter, random, shellstoneState, tuffState, mossState);

            BlockPos poolCenter = new BlockPos(base.getX() + cascadeX, terraceY, base.getZ() + cascadeZ);
            carveTerracePool(level, poolCenter, shellstoneState, tuffState, lumenBlockState, i == levels - 1);

            BlockPos lipPos = new BlockPos(base.getX() + lipX, terraceY - 1, base.getZ() + lipZ);
            level.setBlock(lipPos, Blocks.AIR.defaultBlockState(), 2);

            int nextTerraceY = i > 0 ? base.getY() + terraceHeights[i - 1] : groundLandingY;
            for (int y = terraceY - 2; y > nextTerraceY; y--) {
                level.setBlock(new BlockPos(lipWorldX, y, lipWorldZ), Blocks.AIR.defaultBlockState(), 2);
            }

            clearAboveShelf(level, shelfCenter);
            placed = true;
        }

        carveBaseBasin(level, new BlockPos(lipWorldX, groundLandingY, lipWorldZ), random, shellstoneState, tuffState, mossState, lumenBlockState);

        return placed;
    }

    private static boolean placePillarSlice(WorldGenLevel level, BlockPos center, int radius, RandomSource random, BlockState shellstoneState, BlockState tuffState, BlockState mossState) {
        boolean placed = false;
        double rr = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double distance = (x * x + z * z) / Math.max(1.0D, rr);
                if (distance > 1.0D) {
                    continue;
                }

                BlockPos targetPos = center.offset(x, 0, z);
                BlockState existing = level.getBlockState(targetPos);
                if (!LumenWorldgenHelper.isSolidSupport(existing)) {
                    level.setBlock(targetPos, pickBodyState(distance, random, shellstoneState, tuffState, mossState), 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static void placeShelfRing(WorldGenLevel level, BlockPos center, RandomSource random, BlockState shellstoneState, BlockState tuffState, BlockState mossState) {
        double rr = SHELF_RADIUS * SHELF_RADIUS;
        for (int x = -SHELF_RADIUS; x <= SHELF_RADIUS; x++) {
            for (int z = -SHELF_RADIUS; z <= SHELF_RADIUS; z++) {
                double distance = (x * x + z * z) / Math.max(1.0D, rr);
                if (distance > 1.0D) {
                    continue;
                }

                BlockPos topPos = center.offset(x, 0, z);
                level.setBlock(topPos, pickBodyState(distance, random, shellstoneState, tuffState, mossState), 2);
                level.setBlock(topPos.below(), shellstoneState, 2);
            }
        }
    }

    private static void carveTerracePool(WorldGenLevel level, BlockPos poolCenter, BlockState shellstoneState, BlockState tuffState, BlockState lumenBlockState, boolean isApex) {
        double rr = POOL_RADIUS * POOL_RADIUS;
        for (int x = -POOL_RADIUS; x <= POOL_RADIUS; x++) {
            for (int z = -POOL_RADIUS; z <= POOL_RADIUS; z++) {
                double distance = (x * x + z * z) / Math.max(1.0D, rr);
                if (distance > 1.0D) {
                    continue;
                }

                BlockPos floorPos = poolCenter.offset(x, -1, z);
                BlockPos surfacePos = poolCenter.offset(x, 0, z);
                level.setBlock(floorPos, shellstoneState, 2);
                if (distance <= 0.55D) {
                    level.setBlock(surfacePos, LumenWorldgenHelper.lumenState(lumenBlockState, true), 2);
                    LumenWorldgenHelper.scheduleFluidTick(level, surfacePos);
                } else {
                    level.setBlock(surfacePos, tuffState, 2);
                }
            }
        }

        if (isApex) {
            level.setBlock(poolCenter, LumenWorldgenHelper.lumenState(lumenBlockState, true), 2);
            LumenWorldgenHelper.scheduleFluidTick(level, poolCenter);
        }
    }

    private static void carveBaseBasin(WorldGenLevel level, BlockPos landingPos, RandomSource random, BlockState shellstoneState, BlockState tuffState, BlockState mossState, BlockState lumenBlockState) {
        int radius = 4 + random.nextInt(2);
        int depth = 2 + random.nextInt(2);
        int waterlineY = landingPos.getY() - 1;
        int floorY = waterlineY - depth;
        double rr = radius * radius;

        for (int x = -radius - 1; x <= radius + 1; x++) {
            for (int z = -radius - 1; z <= radius + 1; z++) {
                double distance = (x * x + z * z) / Math.max(1.0D, rr);
                BlockPos topPos = landingPos.offset(x, 0, z);
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, topPos.getX(), topPos.getZ()) - 1;

                if (distance <= 1.0D) {
                    for (int y = surfaceY; y > floorY; y--) {
                        BlockPos fillPos = new BlockPos(topPos.getX(), y, topPos.getZ());
                        if (y > waterlineY) {
                            level.setBlock(fillPos, Blocks.AIR.defaultBlockState(), 2);
                        } else {
                            level.setBlock(fillPos, LumenWorldgenHelper.lumenState(lumenBlockState, 0), 2);
                            LumenWorldgenHelper.scheduleFluidTick(level, fillPos);
                        }
                    }
                    level.setBlock(new BlockPos(topPos.getX(), floorY, topPos.getZ()), pickBodyState(distance, random, shellstoneState, tuffState, mossState), 2);
                } else if (distance <= 1.25D) {
                    for (int y = surfaceY; y >= floorY; y--) {
                        level.setBlock(new BlockPos(topPos.getX(), y, topPos.getZ()), pickBodyState(distance, random, shellstoneState, tuffState, mossState), 2);
                    }
                }
            }
        }

        level.setBlock(landingPos, LumenWorldgenHelper.lumenState(lumenBlockState, true), 2);
        LumenWorldgenHelper.scheduleFluidTick(level, landingPos);
    }

    private static void clearAboveShelf(WorldGenLevel level, BlockPos shelfCenter) {
        for (int x = -SHELF_RADIUS; x <= SHELF_RADIUS; x++) {
            for (int z = -SHELF_RADIUS; z <= SHELF_RADIUS; z++) {
                double distance = (x * x + z * z) / (double) (SHELF_RADIUS * SHELF_RADIUS);
                if (distance > 1.0D) {
                    continue;
                }

                BlockPos columnTop = shelfCenter.offset(x, 0, z);
                for (int offset = 1; offset <= 16; offset++) {
                    BlockPos abovePos = columnTop.above(offset);
                    BlockState aboveState = level.getBlockState(abovePos);
                    if (!aboveState.isAir() && aboveState.canBeReplaced()) {
                        level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static BlockState pickBodyState(double distance, RandomSource random, BlockState shellstoneState, BlockState tuffState, BlockState mossState) {
        if (distance > 0.85D) {
            return random.nextFloat() < 0.4F ? mossState : tuffState;
        }
        return random.nextFloat() < 0.7F ? shellstoneState : tuffState;
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

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }
}
