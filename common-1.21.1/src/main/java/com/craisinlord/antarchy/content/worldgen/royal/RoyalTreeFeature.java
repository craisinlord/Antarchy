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
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class RoyalTreeFeature extends Feature<RoyalTreeConfiguration> {
    private static final float TWO_PI = (float) (Math.PI * 2.0);

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

        int radius = config.canopyRadius().sample(random);
        int layers = config.canopyLayers().sample(random);
        int limbs = Mth.clamp(config.strutCount().sample(random), 0, 4);

        int leaderX = topOffset[0] + random.nextInt(2);
        int leaderZ = topOffset[1] + random.nextInt(2);
        int crownY = height + 2 + random.nextInt(2);
        placeLimb(level, random, config, origin.offset(leaderX, splitStartY, leaderZ), origin.offset(leaderX, crownY - 1, leaderZ), logs);

        List<BlockPos> clumpCenters = new ArrayList<>();
        List<int[]> clumpSizes = new ArrayList<>();
        int centerRadius = limbs == 0 ? radius : Math.round(radius * 0.55F) + random.nextInt(2);
        clumpCenters.add(origin.offset(leaderX, crownY, leaderZ));
        clumpSizes.add(new int[]{Math.max(2, centerRadius), Math.max(2, Math.round(layers * 0.6F) + 1), Math.max(1, Math.round(layers * 0.4F))});

        int[][] corners = {{0, 0}, {1, 0}, {0, 1}, {1, 1}};
        List<int[]> pool = new ArrayList<>(List.of(corners));
        Collections.shuffle(pool, new Random(random.nextLong()));
        for (int i = 0; i < limbs && i < pool.size(); i++) {
            int[] corner = pool.get(i);
            int outX = corner[0] == 1 ? 1 : -1;
            int outZ = corner[1] == 1 ? 1 : -1;
            double angle = Math.atan2(outZ, outX) + (random.nextFloat() - 0.5F) * 0.7F;
            float reach = Math.max(2.0F, radius * (0.45F + random.nextFloat() * 0.15F));
            int limbTopY = height + random.nextInt(Math.max(1, layers - 1));

            BlockPos start = origin.offset(topOffset[0] + corner[0], splitStartY, topOffset[1] + corner[1]);
            int endX = (int) Math.round(Math.cos(angle) * reach);
            int endZ = (int) Math.round(Math.sin(angle) * reach);
            int midY = splitStartY + Math.round((limbTopY - splitStartY) * 0.6F);
            BlockPos mid = new BlockPos(start.getX() + Math.round(endX * 0.35F), origin.getY() + midY, start.getZ() + Math.round(endZ * 0.35F));
            BlockPos end = new BlockPos(start.getX() + endX, origin.getY() + limbTopY, start.getZ() + endZ);
            placeLimb(level, random, config, start, mid, logs);
            placeLimb(level, random, config, mid, end, logs);

            int clumpRadius = Math.max(2, Math.round(radius * 0.5F) + random.nextInt(2));
            int up = Math.max(2, Math.round(layers * 0.6F) + random.nextInt(2));
            int down = Math.max(1, Math.round(up * 0.6F));
            clumpCenters.add(end.above());
            clumpSizes.add(new int[]{clumpRadius, up, down});
        }

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
                placeLog(level, random, config, cursor, logs, branchDir.getAxis());
            }
            int padRadius = 2 + random.nextInt(3);
            clumpCenters.add(cursor.above());
            clumpSizes.add(new int[]{padRadius, Math.max(1, padRadius - 1), 1});
        }

        for (int i = 0; i < clumpCenters.size(); i++) {
            int[] size = clumpSizes.get(i);
            placeClump(level, random, config, clumpCenters.get(i), size[0], size[1], size[2], leaves);
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

    private void placeClump(
            WorldGenLevel level,
            RandomSource random,
            RoyalTreeConfiguration config,
            BlockPos center,
            int radius,
            int up,
            int down,
            Set<BlockPos> leaves
    ) {
        int lobes = 2 + random.nextInt(3);
        float phase = random.nextFloat() * TWO_PI;
        float amp = 0.14F + random.nextFloat() * 0.1F;
        float noiseX = random.nextFloat() * TWO_PI;
        float noiseZ = random.nextFloat() * TWO_PI;
        int bound = Mth.ceil(radius * (1.0F + amp)) + 1;
        for (int dx = -bound; dx <= bound; dx++) {
            for (int dz = -bound; dz <= bound; dz++) {
                float d = Mth.sqrt(dx * dx + dz * dz);
                float angle = (float) Math.atan2(dz, dx);
                float edge = Math.max(1.0F, radius * (1.0F + amp * Mth.sin(lobes * angle + phase)));
                float norm = d / edge;
                if (norm > 1.0F) {
                    continue;
                }
                if (norm > 0.8F && random.nextFloat() < 0.25F) {
                    continue;
                }

                float k = Mth.sqrt(Math.max(0.0F, 1.0F - norm * norm));
                int jitter = 0;
                if (norm < 0.85F) {
                    float n = Mth.sin(dx * 0.8F + noiseX) * Mth.cos(dz * 0.8F + noiseZ);
                    jitter = n > 0.45F ? 1 : (n < -0.55F ? -1 : 0);
                }
                int topY = Math.max(0, Math.round(up * k) + jitter);
                int bottomY = -Math.round(down * k);
                for (int y = bottomY; y <= topY; y++) {
                    placeLeaf(level, random, config, center.offset(dx, y, dz), leaves);
                }

                if (norm > 0.6F && random.nextFloat() < 0.1F) {
                    int length = 1 + random.nextInt(3);
                    for (int h = 1; h <= length; h++) {
                        BlockPos hang = center.offset(dx, bottomY - h, dz);
                        if (!level.getBlockState(hang).isAir()) {
                            break;
                        }
                        placeLeaf(level, random, config, hang, leaves);
                    }
                }
            }
        }
    }

    private void placeLimb(WorldGenLevel level, RandomSource random, RoyalTreeConfiguration config, BlockPos from, BlockPos to, Set<BlockPos> logs) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        int dz = to.getZ() - from.getZ();
        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int az = Math.abs(dz);
        Direction.Axis axis = ay >= Math.max(ax, az) ? Direction.Axis.Y : (ax >= az ? Direction.Axis.X : Direction.Axis.Z);
        int x = 0;
        int y = 0;
        int z = 0;
        placeLog(level, random, config, from, logs, axis);
        int total = ax + ay + az;
        for (int i = 0; i < total; i++) {
            float px = Math.abs(x) < ax ? Math.abs(x) / (float) ax : Float.MAX_VALUE;
            float py = Math.abs(y) < ay ? Math.abs(y) / (float) ay : Float.MAX_VALUE;
            float pz = Math.abs(z) < az ? Math.abs(z) / (float) az : Float.MAX_VALUE;
            if (py <= px && py <= pz) {
                y += Integer.signum(dy);
            } else if (px <= pz) {
                x += Integer.signum(dx);
            } else {
                z += Integer.signum(dz);
            }
            placeLog(level, random, config, from.offset(x, y, z), logs, axis);
        }
    }

    private void placeLog(WorldGenLevel level, RandomSource random, RoyalTreeConfiguration config, BlockPos pos, Set<BlockPos> logs) {
        placeLog(level, random, config, pos, logs, Direction.Axis.Y);
    }

    private void placeLog(WorldGenLevel level, RandomSource random, RoyalTreeConfiguration config, BlockPos pos, Set<BlockPos> logs, Direction.Axis axis) {
        if (!canReplace(level, pos)) {
            return;
        }
        BlockState state = config.trunkProvider().getState(random, pos);
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            state = state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        setBlock(level, pos, state);
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
