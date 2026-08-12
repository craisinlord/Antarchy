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

public final class LumenStreamFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MAX_LENGTH = 36;
    private static final int MIN_LENGTH = 12;
    private static final int MAX_WIDTH = 2;

    public LumenStreamFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        StreamStart start = findStart(level, origin);
        if (start == null) {
            return false;
        }

        BlockState lumenBlockState = AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState();
        BlockState shellstoneState = AntarchyObjects.SHELLSTONE.get().defaultBlockState();
        BlockState tuffState = Blocks.TUFF.defaultBlockState();

        Direction direction = pickDownhillDirection(level, start.x(), start.z(), start.surfaceY(), random);
        if (direction == null) {
            return false;
        }

        int length = randomBetween(random, MIN_LENGTH, MAX_LENGTH);
        int x = start.x();
        int z = start.z();
        int previousSurfaceY = start.surfaceY();
        boolean placedAny = false;

        for (int step = 0; step < length; step++) {
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            if (surfaceY <= level.getMinBuildHeight() + 4) {
                break;
            }

            if (step > 0 && surfaceY - previousSurfaceY > 2) {
                break;
            }

            int channelDepth = step == 0 ? 4 : 3 + random.nextInt(2);
            int waterlineY = surfaceY - 1;
            int floorY = waterlineY - channelDepth;
            if (!hasSolidSubstrate(level, x, z, floorY)) {
                break;
            }

            placedAny |= carveChannelSegment(level, random, x, z, surfaceY, waterlineY, floorY, lumenBlockState, shellstoneState, tuffState, step);

            Direction nextDirection = chooseNextDirection(level, x, z, direction, random);
            if (nextDirection == null) {
                break;
            }

            direction = nextDirection;
            previousSurfaceY = surfaceY;
            x += direction.getStepX();
            z += direction.getStepZ();
        }

        if (placedAny) {
            placedAny |= createTerminalBasin(level, random, x, z, lumenBlockState, shellstoneState, tuffState);
        }

        return placedAny;
    }

    private static boolean carveChannelSegment(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int centerZ,
            int surfaceY,
            int waterlineY,
            int floorY,
            BlockState lumenBlockState,
            BlockState shellstoneState,
            BlockState tuffState,
            int step
    ) {
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int width = 1 + random.nextInt(MAX_WIDTH);

        for (int dx = -width; dx <= width; dx++) {
            for (int dz = -width; dz <= width; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > width + 0.4D) {
                    continue;
                }

                int worldX = centerX + dx;
                int worldZ = centerZ + dz;
                int localSurfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) - 1;
                if (localSurfaceY < floorY + 1) {
                    continue;
                }

                double normalized = Mth.clamp((float) (distance / Math.max(width, 1)), 0.0F, 1.25F);
                int localDepth = Math.max(2, Mth.floor((1.0D - (normalized * normalized)) * (waterlineY - floorY)));
                if (distance <= width * 0.45D) {
                    localDepth = Math.max(localDepth, waterlineY - floorY - 1);
                }
                int localWaterlineY = Math.min(waterlineY, localSurfaceY - 1);
                int localFloorY = localWaterlineY - localDepth;
                if (!hasSolidSubstrate(level, worldX, worldZ, localFloorY)) {
                    continue;
                }

                boolean edge = distance >= width - 0.45D;
                boolean innerWall = !edge && distance >= width - 0.8D;
                int bankTopY = Math.max(localWaterlineY + 1, Math.min(localSurfaceY, surfaceY));
                for (int y = localSurfaceY; y > localFloorY; y--) {
                    mutable.set(worldX, y, worldZ);
                    if (y > localWaterlineY) {
                        level.setBlock(mutable, y <= bankTopY && (edge || innerWall) ? pickWallState(random, shellstoneState, tuffState) : Blocks.AIR.defaultBlockState(), 2);
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
                level.setBlock(mutable, random.nextFloat() < 0.65F ? shellstoneState : tuffState, 2);
                placedAny = true;
            }
        }

        return placedAny;
    }

    private static boolean createTerminalBasin(
            WorldGenLevel level,
            RandomSource random,
            int centerX,
            int centerZ,
            BlockState lumenBlockState,
            BlockState shellstoneState,
            BlockState tuffState
    ) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerX, centerZ) - 1;
        int radius = 3 + random.nextInt(2);
        int waterlineY = surfaceY - 1;
        int maxDepth = 3 + random.nextInt(2);
        int floorY = waterlineY - maxDepth;
        if (!hasSolidSubstrate(level, centerX, centerZ, floorY)) {
            return false;
        }

        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius + 0.6D) {
                    continue;
                }

                int worldX = centerX + dx;
                int worldZ = centerZ + dz;
                int localSurfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ) - 1;
                if (localSurfaceY < floorY + 1) {
                    continue;
                }

                double normalized = Mth.clamp((float) (distance / Math.max(radius, 1)), 0.0F, 1.25F);
                int localDepth = Math.max(2, Mth.floor((1.0D - (normalized * normalized)) * maxDepth));
                int localWaterlineY = Math.min(waterlineY, localSurfaceY - 1);
                int localFloorY = localWaterlineY - localDepth;
                boolean edge = distance >= radius - 0.45D;
                boolean innerWall = !edge && distance >= radius - 0.85D;
                int bankTopY = Math.max(localWaterlineY + 1, Math.min(localSurfaceY, surfaceY));
                for (int y = localSurfaceY; y > localFloorY; y--) {
                    mutable.set(worldX, y, worldZ);
                    if (y > localWaterlineY) {
                        level.setBlock(mutable, y <= bankTopY && (edge || innerWall) ? pickWallState(random, shellstoneState, tuffState) : Blocks.AIR.defaultBlockState(), 2);
                        placedAny = true;
                        continue;
                    }

                    if (edge || (innerWall && y >= localFloorY + 1)) {
                        level.setBlock(mutable, pickWallState(random, shellstoneState, tuffState), 2);
                        placedAny = true;
                        continue;
                    }

                    level.setBlock(mutable, lumenBlockState, 2);
                    LumenWorldgenHelper.scheduleFluidTick(level, mutable.immutable());
                    placedAny = true;
                }

                mutable.set(worldX, localFloorY, worldZ);
                level.setBlock(mutable, random.nextFloat() < 0.75F ? shellstoneState : tuffState, 2);
                placedAny = true;
            }
        }

        return placedAny;
    }

    private static StreamStart findStart(WorldGenLevel level, BlockPos origin) {
        int bestY = Integer.MIN_VALUE;
        StreamStart best = null;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
                BlockPos floor = LumenWorldgenHelper.findNaturalFloor(level, x, z, topY, level.getMinBuildHeight() + 1);
                if (floor != null && floor.getY() > bestY) {
                    bestY = floor.getY();
                    best = new StreamStart(x, z, floor.getY());
                }
            }
        }
        return best;
    }

    private static BlockState pickWallState(RandomSource random, BlockState shellstoneState, BlockState tuffState) {
        return random.nextFloat() < 0.7F ? shellstoneState : tuffState;
    }

    private static Direction pickDownhillDirection(WorldGenLevel level, int x, int z, int surfaceY, RandomSource random) {
        Direction bestDirection = null;
        int bestDrop = Integer.MIN_VALUE;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int nextX = x + direction.getStepX() * 3;
            int nextZ = z + direction.getStepZ() * 3;
            int nextY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, nextX, nextZ) - 1;
            int drop = surfaceY - nextY;
            if (drop > bestDrop) {
                bestDrop = drop;
                bestDirection = direction;
            }
        }

        if (bestDirection == null) {
            return null;
        }

        if (bestDrop < -1 && random.nextInt(4) != 0) {
            return null;
        }

        return bestDirection;
    }

    private static Direction chooseNextDirection(WorldGenLevel level, int x, int z, Direction current, RandomSource random) {
        Direction[] candidates = new Direction[] {
                current,
                turnLeft(current),
                turnRight(current)
        };

        Direction best = null;
        int bestScore = Integer.MAX_VALUE;
        for (Direction candidate : candidates) {
            int nextX = x + candidate.getStepX() * 2;
            int nextZ = z + candidate.getStepZ() * 2;
            int nextY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, nextX, nextZ) - 1;
            int score = nextY * 8 + random.nextInt(3);
            if (score < bestScore) {
                bestScore = score;
                best = candidate;
            }
        }

        return best;
    }

    private static Direction turnLeft(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case SOUTH -> Direction.EAST;
            case EAST -> Direction.NORTH;
            default -> direction;
        };
    }

    private static Direction turnRight(Direction direction) {
        return switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
            default -> direction;
        };
    }

    private static boolean hasSolidSubstrate(WorldGenLevel level, int x, int z, int floorY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = floorY - 1; y >= floorY - 4; y--) {
            mutable.set(x, y, z);
            if (LumenWorldgenHelper.isSolidSupport(level.getBlockState(mutable))) {
                return true;
            }
        }
        return false;
    }

    private static int randomBetween(RandomSource random, int minInclusive, int maxInclusive) {
        int min = Math.min(minInclusive, maxInclusive);
        int max = Math.max(minInclusive, maxInclusive);
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    private record StreamStart(int x, int z, int surfaceY) {
    }
}
