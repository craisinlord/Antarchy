package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class GlimmeringFallFeature extends Feature<NoneFeatureConfiguration> {
    private static final float FALL_CHANCE = 0.3F;
    private static final int SCAN_STRIDE = 2;
    private static final int DROP_PROBE = 6;
    private static final int MIN_DROP = 30;
    private static final int CHANNEL_LENGTH = 5;
    private static final int MAX_SHIFTS = 24;
    private static final int MAX_CARVE = 6;
    private static final int FALLING_LEVEL = 8;

    public GlimmeringFallFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (random.nextFloat() >= FALL_CHANCE) {
            return false;
        }
        ChunkPos chunk = new ChunkPos(context.origin());
        Bounds bounds = new Bounds(chunk.getMinBlockX() - 15, chunk.getMinBlockZ() - 15, chunk.getMaxBlockX() + 15, chunk.getMaxBlockZ() + 15);

        BlockPos best = null;
        Direction bestDirection = null;
        int bestDrop = MIN_DROP - 1;
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x += SCAN_STRIDE) {
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z += SCAN_STRIDE) {
                int top = surface(level, x, z);
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    int inward = surface(level, x - direction.getStepX() * 3, z - direction.getStepZ() * 3);
                    if (inward < top - 2 || inward > top + 2) {
                        continue;
                    }
                    int drop = top - surface(level, x + direction.getStepX() * DROP_PROBE, z + direction.getStepZ() * DROP_PROBE);
                    if (drop > bestDrop) {
                        bestDrop = drop;
                        best = new BlockPos(x, top, z);
                        bestDirection = direction;
                    }
                }
            }
        }
        if (best == null) {
            return false;
        }

        int topY = best.getY();
        BlockPos rim = best;
        for (int step = 0; step < DROP_PROBE; step++) {
            BlockPos next = rim.relative(bestDirection);
            if (surface(level, next.getX(), next.getZ()) < topY - 2) {
                break;
            }
            rim = next.atY(topY);
        }

        int width = 3 + random.nextInt(2);
        Direction side = bestDirection.getClockWise();
        BlockPos firstLane = rim.relative(side, -(width / 2));
        int landingX = 0;
        int landingZ = 0;
        int landingY = Integer.MAX_VALUE;
        int landings = 0;
        for (int lane = 0; lane < width; lane++) {
            BlockPos laneRim = firstLane.relative(side, lane);
            if (!bounds.contains(laneRim)) {
                continue;
            }
            carveChannel(level, bounds, laneRim, bestDirection, topY, lane == 0 || lane == width - 1);
            BlockPos landing = pour(level, bounds, laneRim.relative(bestDirection).atY(topY), bestDirection);
            if (landing != null) {
                landingX += landing.getX();
                landingZ += landing.getZ();
                landingY = Math.min(landingY, landing.getY());
                landings++;
            }
        }
        if (landings > 0) {
            pool(level, random, bounds, new BlockPos(Math.round((float) landingX / landings), landingY, Math.round((float) landingZ / landings)), width);
        }
        return true;
    }

    private static void carveChannel(WorldGenLevel level, Bounds bounds, BlockPos rim, Direction direction, int topY, boolean outer) {
        BlockState source = lumen(0);
        BlockState bank = Blocks.TUFF.defaultBlockState();
        Direction side = direction.getClockWise();
        for (int k = 0; k <= CHANNEL_LENGTH; k++) {
            BlockPos column = rim.relative(direction, -k);
            if (!bounds.contains(column)) {
                break;
            }
            int y = surface(level, column.getX(), column.getZ());
            if (Math.abs(y - topY) > 2) {
                break;
            }
            BlockPos pos = column.atY(y);
            clearAbove(level, pos);
            if (!level.getBlockState(pos.below()).isSolid()) {
                level.setBlock(pos.below(), bank, 2);
            }
            level.setBlock(pos, source, 2);
            if (outer) {
                for (Direction flank : new Direction[]{side, side.getOpposite()}) {
                    BlockPos neighbor = pos.relative(flank);
                    if (bounds.contains(neighbor) && !level.getBlockState(neighbor).isSolid() && level.getFluidState(neighbor).isEmpty()) {
                        level.setBlock(neighbor, bank, 2);
                    }
                }
            }
        }
    }

    private static BlockPos pour(WorldGenLevel level, Bounds bounds, BlockPos start, Direction direction) {
        BlockState falling = lumen(FALLING_LEVEL);
        BlockPos.MutableBlockPos cursor = start.mutable();
        int shifts = 0;
        int minY = level.getMinBuildHeight() + 4;
        while (cursor.getY() > minY) {
            BlockState state = level.getBlockState(cursor);
            if (state.isAir() || state.canBeReplaced() && state.getFluidState().isEmpty()) {
                level.setBlock(cursor, falling, 2);
                cursor.move(Direction.DOWN);
                continue;
            }
            if (!state.getFluidState().isEmpty() && !state.isSolid()) {
                cursor.move(Direction.DOWN);
                continue;
            }
            BlockPos next = cursor.relative(direction).above();
            if (shifts >= MAX_SHIFTS || !bounds.contains(next) || surface(level, next.getX(), next.getZ()) >= cursor.getY()) {
                return cursor.immutable();
            }
            cursor.set(next);
            shifts++;
        }
        return null;
    }

    private static void pool(WorldGenLevel level, RandomSource random, Bounds bounds, BlockPos landing, int width) {
        BlockState source = lumen(0);
        int waterline = landing.getY();
        int radius = Math.max(4, width + 1) + random.nextInt(2);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                int x = landing.getX() + dx;
                int z = landing.getZ() + dz;
                if (!bounds.contains(x, z)) {
                    continue;
                }
                double distance = Math.sqrt(dx * dx + dz * dz);
                int columnTop = surface(level, x, z);
                if (distance <= radius) {
                    if (columnTop > waterline + MAX_CARVE) {
                        continue;
                    }
                    for (int y = columnTop; y > waterline; y--) {
                        pos.set(x, y, z);
                        if (level.getFluidState(pos).isEmpty()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                    clearAbove(level, new BlockPos(x, Math.max(columnTop, waterline), z));
                    double edge = distance / radius;
                    int depth = Math.max(1, Mth.floor(3.0D * (1.0D - edge * edge)) + 1);
                    for (int y = waterline; y > waterline - depth; y--) {
                        pos.set(x, y, z);
                        level.setBlock(pos, source, 2);
                    }
                    pos.set(x, waterline - depth, z);
                    if (!level.getBlockState(pos).isSolid()) {
                        level.setBlock(pos, bankState(random), 2);
                    }
                } else if (distance <= radius + 1.0D) {
                    for (int y = waterline - 3; y <= waterline; y++) {
                        pos.set(x, y, z);
                        BlockState state = level.getBlockState(pos);
                        if (!state.isSolid() && !state.is(AntarchyObjects.LUMEN_BLOCK.get())) {
                            level.setBlock(pos, bankState(random), 2);
                        }
                    }
                }
            }
        }
    }

    private static BlockState bankState(RandomSource random) {
        return random.nextBoolean() ? Blocks.TUFF.defaultBlockState() : AntarchyObjects.SHELLSTONE.get().defaultBlockState();
    }

    private static void clearAbove(WorldGenLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState state = level.getBlockState(above);
        if (!state.isAir() && state.canBeReplaced() && state.getFluidState().isEmpty()) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static int surface(WorldGenLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
    }

    private static BlockState lumen(int fluidLevel) {
        return LumenWorldgenHelper.lumenState(AntarchyObjects.LUMEN_BLOCK.get().defaultBlockState(), fluidLevel);
    }

    private record Bounds(int minX, int minZ, int maxX, int maxZ) {
        boolean contains(BlockPos pos) {
            return this.contains(pos.getX(), pos.getZ());
        }

        boolean contains(int x, int z) {
            return x >= this.minX && x <= this.maxX && z >= this.minZ && z <= this.maxZ;
        }
    }
}
