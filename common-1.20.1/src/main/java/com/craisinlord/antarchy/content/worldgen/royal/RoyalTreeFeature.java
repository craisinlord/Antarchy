package com.craisinlord.antarchy.content.worldgen.royal;

import com.craisinlord.antarchy.content.block.RoyalLeavesBlock;
import com.mojang.serialization.Codec;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class RoyalTreeFeature extends Feature<RoyalTreeConfiguration> {
    public RoyalTreeFeature(Codec<RoyalTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<RoyalTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        RoyalTreeConfiguration config = context.config();

        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos ground = origin.offset(dx, -1, dz);
                if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) {
                    return false;
                }
                if (!canReplace(level, origin.offset(dx, 0, dz))) {
                    return false;
                }
            }
        }

        Set<BlockPos> logs = new HashSet<>();
        Set<BlockPos> leaves = new HashSet<>();

        int height = config.height().sample(random);
        int drift = config.trunkDrift().sample(random);
        int backBend = Math.round(drift * 0.45F);
        Direction driftDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        int splitLen = 2 + random.nextInt(3);
        int splitStartY = Math.max(2, height - splitLen);

        int[] topOffset = curveOffset(splitStartY, height, drift, backBend, driftDir);

        for (int y = 0; y < splitStartY; y++) {
            int[] off = curveOffset(y, height, drift, backBend, driftDir);
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    placeLog(level, random, config, origin.offset(off[0] + dx, y, off[1] + dz), logs);
                }
            }
        }

        placeButtress(level, random, config, origin, logs);

        int struts = Mth.clamp(config.strutCount().sample(random), 0, 4);
        int[][] corners = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        List<int[]> pool = new ArrayList<>(List.of(corners));
        Collections.shuffle(pool, new Random(random.nextLong()));
        for (int i = 0; i < struts && i < pool.size(); i++) {
            int[] corner = pool.get(i);
            int outX = corner[0] == 1 ? 1 : -1;
            int outZ = corner[1] == 1 ? 1 : -1;
            for (int y = splitStartY; y <= height; y++) {
                int spread = Math.min((y - splitStartY + 1) / 2, 2);
                int sx = topOffset[0] + corner[0] + outX * spread;
                int sz = topOffset[1] + corner[1] + outZ * spread;
                placeLog(level, random, config, origin.offset(sx, y, sz), logs);
            }
        }

        int radius = config.canopyRadius().sample(random);
        int layers = config.canopyLayers().sample(random);
        BlockPos canopyCenter = origin.offset(topOffset[0], height, topOffset[1]);
        placeCanopy(level, random, config, canopyCenter, radius, layers, 2, 2, leaves);

        int branches = Mth.clamp(config.branchCount().sample(random), 0, 8);
        int branchSpan = Math.max(1, height - 6);
        for (int b = 0; b < branches; b++) {
            int startY = 3 + random.nextInt(branchSpan);
            int[] branchOff = curveOffset(startY, height, drift, backBend, driftDir);
            Direction branchDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int branchLen = 2 + random.nextInt(5);
            int bx = branchOff[0] + (branchDir.getStepX() > 0 ? 1 : 0);
            int bz = branchOff[1] + (branchDir.getStepZ() > 0 ? 1 : 0);
            BlockPos cursor = origin.offset(bx, startY, bz);
            for (int s = 1; s <= branchLen; s++) {
                cursor = cursor.relative(branchDir);
                if (s % 2 == 0) {
                    cursor = cursor.above();
                }
                placeLog(level, random, config, cursor, logs);
            }
            int padRadius = 2 + random.nextInt(3);
            placeCanopy(level, random, config, cursor.above(), padRadius, Math.max(2, padRadius - 1), 1, 0, leaves);
        }

        refreshLeafDistances(level, leaves, logs);

        return true;
    }

    private void placeButtress(WorldGenLevel level, RandomSource random, RoyalTreeConfiguration config, BlockPos origin, Set<BlockPos> logs) {
        int legs = 2 + random.nextInt(3);
        List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        Collections.shuffle(dirs, new Random(random.nextLong()));
        for (int i = 0; i < legs && i < dirs.size(); i++) {
            Direction dir = dirs.get(i);
            int len = 2 + random.nextInt(3);
            int startX = dir.getStepX() > 0 ? 1 : 0;
            int startZ = dir.getStepZ() > 0 ? 1 : 0;
            BlockPos cursor = origin.offset(startX, 0, startZ);
            for (int s = 1; s <= len; s++) {
                cursor = cursor.relative(dir);
                int downY = -(s / 2);
                BlockPos upper = cursor.offset(0, downY, 0);
                BlockPos lower = cursor.offset(0, downY - 1, 0);
                setBlock(level, upper, config.trunkProvider().getState(random, upper));
                setBlock(level, lower, config.trunkProvider().getState(random, lower));
                logs.add(upper.immutable());
                logs.add(lower.immutable());
            }
        }
    }

    private void placeCanopy(
            WorldGenLevel level,
            RandomSource random,
            RoyalTreeConfiguration config,
            BlockPos center,
            int radius,
            int layers,
            int shell,
            int bottomRecess,
            Set<BlockPos> leaves
    ) {
        float rf = Math.max(1.0F, radius);
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                float d = Mth.sqrt(dx * dx + dz * dz);
                float norm = d / rf;
                if (norm > 1.05F) {
                    continue;
                }

                int rimCut = 0;
                if (norm > 0.7F && random.nextFloat() < 0.38F) {
                    rimCut = 1;
                }
                if (d - rimCut > rf) {
                    continue;
                }

                int topY = Math.round(layers * Mth.sqrt(Math.max(0.0F, 1.0F - norm * norm)));
                if (topY < 1) {
                    topY = 1;
                }
                int bottomY = Math.round(bottomRecess * (1.0F - Math.min(1.0F, norm)));
                if (bottomY > topY) {
                    continue;
                }

                for (int y = bottomY; y <= topY; y++) {
                    boolean interior = topY - y >= shell && y - bottomY >= shell && rf - d >= shell;
                    if (interior) {
                        continue;
                    }
                    placeLeaf(level, random, config, center.offset(dx, y, dz), leaves);
                }
            }
        }
    }

    private void placeLog(WorldGenLevel level, RandomSource random, RoyalTreeConfiguration config, BlockPos pos, Set<BlockPos> logs) {
        if (!canReplace(level, pos)) {
            return;
        }
        setBlock(level, pos, config.trunkProvider().getState(random, pos));
        logs.add(pos.immutable());
    }

    private void placeLeaf(WorldGenLevel level, RandomSource random, RoyalTreeConfiguration config, BlockPos pos, Set<BlockPos> leaves) {
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !existing.canBeReplaced() && !(existing.getBlock() instanceof LeavesBlock)) {
            return;
        }
        setBlock(level, pos, config.leavesProvider().getState(random, pos));
        leaves.add(pos.immutable());
    }

    private void refreshLeafDistances(WorldGenLevel level, Set<BlockPos> leafPositions, Set<BlockPos> logPositions) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(logPositions);
        Map<BlockPos, Integer> distances = new HashMap<>();
        for (BlockPos logPos : logPositions) {
            distances.put(logPos, 0);
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            int currentDistance = distances.getOrDefault(current, 0);
            if (currentDistance >= RoyalLeavesBlock.ROYAL_MAX_DISTANCE) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (distances.containsKey(next)) {
                    continue;
                }
                if (!(level.getBlockState(next).getBlock() instanceof LeavesBlock)) {
                    continue;
                }
                distances.put(next, currentDistance + 1);
                queue.addLast(next);
            }
        }

        for (BlockPos pos : leafPositions) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof RoyalLeavesBlock)) {
                continue;
            }
            int distance = Math.min(
                    RoyalLeavesBlock.ROYAL_DECAY_DISTANCE,
                    distances.getOrDefault(pos, RoyalLeavesBlock.ROYAL_DECAY_DISTANCE)
            );
            if (state.getValue(RoyalLeavesBlock.ROYAL_DISTANCE) != distance) {
                setBlock(level, pos, RoyalLeavesBlock.setRoyalDistanceForWorldgen(state, distance));
            }
        }
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.getBlock() instanceof LeavesBlock;
    }

    private static int[] curveOffset(int y, int height, int drift, int backBend, Direction dir) {
        float f = height <= 1 ? 0.0F : (float) y / (height - 1);
        float outPhase = smoothstep(0.0F, 0.6F, f);
        float backPhase = smoothstep(0.55F, 1.0F, f);
        int step = Math.round(drift * outPhase - backBend * backPhase);
        return new int[]{dir.getStepX() * step, dir.getStepZ() * step};
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
