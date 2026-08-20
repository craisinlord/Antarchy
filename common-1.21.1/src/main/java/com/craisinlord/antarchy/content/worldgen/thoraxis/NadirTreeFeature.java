package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class NadirTreeFeature extends Feature<NadirTreeConfiguration> {
    public NadirTreeFeature(Codec<NadirTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NadirTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos supportPos = origin.above();
        if (!level.getBlockState(supportPos).isFaceSturdy(level, supportPos, Direction.DOWN)) {
            return false;
        }

        RandomSource random = context.random();
        NadirTreeConfiguration config = context.config();
        int height = config.height().sample(random);
        int bottomRadius = config.bottomRadius().sample(random);
        int branchCount = config.branchCount().sample(random);
        int branchLength = config.branchLength().sample(random);
        int veilRadius = config.veilRadius().sample(random);

        List<BlockPos> spine = buildSpine(origin, height, random);
        Map<BlockPos, Direction.Axis> logs = new LinkedHashMap<>();
        Set<BlockPos> foliage = new LinkedHashSet<>();

        placeTrunk(spine, bottomRadius, logs, random);
        placeLowerMass(spine, bottomRadius, logs, foliage, random, veilRadius);
        placeBranches(spine, branchCount, branchLength, logs, foliage, random, veilRadius);
        placeVeilCurtains(spine, foliage, random, veilRadius);

        if (logs.isEmpty()) {
            return false;
        }
        if (!canPlace(level, logs.keySet(), foliage)) {
            return false;
        }

        for (Map.Entry<BlockPos, Direction.Axis> entry : logs.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = config.trunkProvider().getState(random, pos);
            if (state.hasProperty(RotatedPillarBlock.AXIS)) {
                state = state.setValue(RotatedPillarBlock.AXIS, entry.getValue());
            }
            setBlock(level, pos, state);
        }

        for (BlockPos pos : foliage) {
            if (!logs.containsKey(pos)) {
                setBlock(level, pos, config.foliageProvider().getState(random, pos));
            }
        }

        return true;
    }

    private List<BlockPos> buildSpine(BlockPos origin, int height, RandomSource random) {
        List<BlockPos> spine = new ArrayList<>(height);
        BlockPos current = origin;
        Direction lateral = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int step = 0; step < height; step++) {
            if (step > 1 && random.nextFloat() < 0.42F) {
                if (random.nextFloat() < 0.3F) {
                    lateral = lateral.getClockWise();
                } else if (random.nextFloat() < 0.3F) {
                    lateral = lateral.getCounterClockWise();
                }
                current = current.relative(lateral);
            }
            if (step > 0) {
                current = current.below();
            }
            spine.add(current);
        }
        return spine;
    }

    private void placeTrunk(List<BlockPos> spine, int bottomRadius, Map<BlockPos, Direction.Axis> logs, RandomSource random) {
        for (int index = 0; index < spine.size(); index++) {
            BlockPos center = spine.get(index);
            float progress = spine.size() == 1 ? 1.0F : index / (float) (spine.size() - 1);
            int radius = Mth.clamp(Math.round(progress * bottomRadius), 0, bottomRadius);
            fillTrunkLayer(center, radius, logs, random);
        }
    }

    private void fillTrunkLayer(BlockPos center, int radius, Map<BlockPos, Direction.Axis> logs, RandomSource random) {
        if (radius <= 0) {
            putLog(logs, center, Direction.Axis.Y);
            return;
        }

        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int dist = dx * dx + dz * dz;
                if (dist > radiusSq + 1) {
                    continue;
                }
                if (dist > radiusSq - radius && random.nextFloat() < 0.2F) {
                    continue;
                }
                putLog(logs, center.offset(dx, 0, dz), Direction.Axis.Y);
            }
        }
    }

    private void placeLowerMass(
            List<BlockPos> spine,
            int bottomRadius,
            Map<BlockPos, Direction.Axis> logs,
            Set<BlockPos> foliage,
            RandomSource random,
            int veilRadius
    ) {
        int start = Math.max(0, spine.size() - Math.max(3, bottomRadius + 2));
        for (int index = start; index < spine.size(); index++) {
            BlockPos center = spine.get(index);
            int extraRadius = Math.max(1, bottomRadius - Math.max(0, spine.size() - 1 - index));
            for (int dy = 0; dy <= Math.min(2, index - start); dy++) {
                fillTrunkLayer(center.below(dy), extraRadius, logs, random);
            }
            placeVeilCluster(foliage, center.below(), Math.max(1, veilRadius), random, true);
        }
    }

    private void placeBranches(
            List<BlockPos> spine,
            int branchCount,
            int branchLength,
            Map<BlockPos, Direction.Axis> logs,
            Set<BlockPos> foliage,
            RandomSource random,
            int veilRadius
    ) {
        int minAttach = Math.min(spine.size() - 1, Math.max(1, spine.size() / 4));
        int maxAttach = Math.max(minAttach, spine.size() - 2);
        for (int i = 0; i < branchCount; i++) {
            int attachIndex = Mth.nextInt(random, minAttach, maxAttach);
            BlockPos attach = spine.get(attachIndex);
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos cursor = attach;
            BlockPos previous = attach;
            int length = Math.max(2, branchLength - random.nextInt(Math.max(1, branchLength / 2 + 1)));

            for (int step = 0; step < length; step++) {
                cursor = cursor.relative(direction);
                if (step > 0 || random.nextFloat() < 0.85F) {
                    cursor = cursor.below();
                }
                if (random.nextFloat() < 0.25F) {
                    direction = random.nextBoolean() ? direction.getClockWise() : direction.getCounterClockWise();
                }
                putLog(logs, cursor, axisForSegment(previous, cursor));
                if (random.nextFloat() < 0.35F) {
                    putLog(logs, cursor.below(), Direction.Axis.Y);
                }
                previous = cursor;
            }

            placeVeilCluster(foliage, cursor, veilRadius + random.nextInt(2), random, true);
            placeVeilCluster(foliage, cursor.below(1 + random.nextInt(2)), Math.max(1, veilRadius - 1), random, true);

            if (random.nextFloat() < 0.45F) {
                BlockPos fork = cursor;
                Direction forkDirection = random.nextBoolean() ? direction.getClockWise() : direction.getCounterClockWise();
                int forkLength = Math.max(2, length / 2);
                for (int step = 0; step < forkLength; step++) {
                    BlockPos next = fork.relative(forkDirection).below();
                    putLog(logs, next, axisForSegment(fork, next));
                    fork = next;
                }
                placeVeilCluster(foliage, fork, Math.max(1, veilRadius - 1), random, true);
            }
        }
    }

    private void placeVeilCurtains(List<BlockPos> spine, Set<BlockPos> foliage, RandomSource random, int veilRadius) {
        for (int index = 1; index < spine.size(); index++) {
            if (random.nextFloat() > 0.33F) {
                continue;
            }
            BlockPos anchor = spine.get(index);
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            BlockPos start = anchor.relative(side);
            int length = 2 + random.nextInt(Math.max(2, veilRadius + 1));
            for (int i = 0; i < length; i++) {
                BlockPos pos = start.below(i);
                foliage.add(pos);
                if (random.nextFloat() < 0.22F) {
                    foliage.add(pos.relative(side.getClockWise()));
                }
            }
        }
    }

    private void placeVeilCluster(Set<BlockPos> foliage, BlockPos center, int radius, RandomSource random, boolean stretchDown) {
        int radiusSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -radius; dy <= 1; dy++) {
                    int adjustedDy = stretchDown ? Math.max(0, -dy) : Math.abs(dy);
                    int dist = dx * dx + dz * dz + adjustedDy * adjustedDy;
                    if (dist > radiusSq + 1) {
                        continue;
                    }
                    if (dist > radiusSq - radius && random.nextFloat() < 0.28F) {
                        continue;
                    }
                    foliage.add(center.offset(dx, dy, dz));
                }
            }
        }
    }

    private boolean canPlace(WorldGenLevel level, Set<BlockPos> logs, Set<BlockPos> foliage) {
        for (BlockPos pos : logs) {
            if (!canReplace(level, pos)) {
                return false;
            }
        }
        for (BlockPos pos : foliage) {
            if (logs.contains(pos)) {
                continue;
            }
            if (!canReplace(level, pos)) {
                return false;
            }
        }
        return true;
    }

    private void putLog(Map<BlockPos, Direction.Axis> logs, BlockPos pos, Direction.Axis axis) {
        Direction.Axis existing = logs.get(pos);
        if (existing == null || existing == Direction.Axis.Y) {
            logs.put(pos, axis);
        }
    }

    private static Direction.Axis axisForSegment(BlockPos from, BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());
        if (dy >= dx && dy >= dz) {
            return Direction.Axis.Y;
        }
        return dx >= dz ? Direction.Axis.X : Direction.Axis.Z;
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() || state.getBlock() instanceof LeavesBlock;
    }

    private void setBlock(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level instanceof LevelWriter writer) {
            setBlock(writer, pos, state);
        }
    }
}
