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
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import java.util.Optional;
import java.util.OptionalInt;

public class NadirTreeFeature extends Feature<NadirTreeConfiguration> {
    public NadirTreeFeature(Codec<NadirTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NadirTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = findCeilingOrigin(level, context.origin(), 72);
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
        placeRootWaves(spine, bottomRadius, logs, foliage, random, veilRadius);
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

    private static BlockPos findCeilingOrigin(WorldGenLevel level, BlockPos origin, int searchRange) {
        if (level.getBlockState(origin).isAir() && level.getBlockState(origin.above()).isFaceSturdy(level, origin.above(), Direction.DOWN)) {
            return origin;
        }
        Optional<Column> optional = Column.scan(level, origin, searchRange, state -> state.isAir(), state -> !state.isAir());
        if (optional.isEmpty()) {
            return origin;
        }
        OptionalInt ceiling = optional.get().getCeiling();
        if (ceiling.isEmpty()) {
            return origin;
        }
        return origin.atY(ceiling.getAsInt() - 1);
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
            float wave = Mth.sin(progress * Mth.PI * 2.75F) * 0.55F + Mth.sin(progress * Mth.PI * 5.5F) * 0.25F;
            int radius = Mth.clamp(Math.round((0.45F + progress * 0.3F + wave * 0.22F) * bottomRadius), 0, Math.max(1, bottomRadius - 1));
            if (index < 2) {
                radius = Math.max(radius, 1);
            }
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
                if (dist > radiusSq - radius && random.nextFloat() < 0.48F) {
                    continue;
                }
                putLog(logs, center.offset(dx, 0, dz), Direction.Axis.Y);
            }
        }
    }

    private void placeRootWaves(
            List<BlockPos> spine,
            int bottomRadius,
            Map<BlockPos, Direction.Axis> logs,
            Set<BlockPos> foliage,
            RandomSource random,
            int veilRadius
    ) {
        int waveCount = Math.max(4, bottomRadius + 3);
        for (int wave = 0; wave < waveCount; wave++) {
            int index = Mth.nextInt(random, 0, Math.max(0, spine.size() - 3));
            BlockPos center = spine.get(index);
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int length = Mth.nextInt(random, 2, Math.max(3, bottomRadius + 3));
            BlockPos cursor = center;
            BlockPos previous = center;
            for (int step = 0; step < length; step++) {
                cursor = cursor.relative(direction);
                if (random.nextFloat() < 0.55F) {
                    cursor = cursor.below();
                }
                if (step > 1 && random.nextFloat() < 0.38F) {
                    direction = random.nextBoolean() ? direction.getClockWise() : direction.getCounterClockWise();
                }
                putLog(logs, cursor, axisForSegment(previous, cursor));
                previous = cursor;
            }
            if (random.nextFloat() < 0.75F) {
                placeVeilRibbon(foliage, cursor, direction, Math.max(2, veilRadius + random.nextInt(2)), random);
            }
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

            placeVeilRibbon(foliage, cursor, direction, Math.max(2, veilRadius + random.nextInt(3)), random);

            if (random.nextFloat() < 0.45F) {
                BlockPos fork = cursor;
                Direction forkDirection = random.nextBoolean() ? direction.getClockWise() : direction.getCounterClockWise();
                int forkLength = Math.max(2, length / 2);
                for (int step = 0; step < forkLength; step++) {
                    BlockPos next = fork.relative(forkDirection).below();
                    putLog(logs, next, axisForSegment(fork, next));
                    fork = next;
                }
                placeVeilRibbon(foliage, fork, forkDirection, Math.max(2, veilRadius), random);
            }
        }
    }

    private void placeVeilCurtains(List<BlockPos> spine, Set<BlockPos> foliage, RandomSource random, int veilRadius) {
        for (int index = 1; index < spine.size(); index++) {
            if (random.nextFloat() > 0.24F) {
                continue;
            }
            BlockPos anchor = spine.get(index);
            Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            placeVeilRibbon(foliage, anchor.relative(side), side, Math.max(2, veilRadius + random.nextInt(3)), random);
        }
    }

    private void placeVeilRibbon(Set<BlockPos> foliage, BlockPos start, Direction flow, int length, RandomSource random) {
        Direction side = random.nextBoolean() ? flow.getClockWise() : flow.getCounterClockWise();
        BlockPos cursor = start;
        for (int step = 0; step < length; step++) {
            foliage.add(cursor);
            if (random.nextFloat() < 0.34F) {
                foliage.add(cursor.relative(side));
            }
            if (random.nextFloat() < 0.18F) {
                foliage.add(cursor.relative(side.getOpposite()));
            }
            cursor = cursor.below();
            if (step > 0 && random.nextFloat() < 0.4F) {
                cursor = cursor.relative(random.nextBoolean() ? side : side.getOpposite());
            }
            if (random.nextFloat() < 0.18F) {
                BlockPos drift = cursor.relative(flow);
                if (random.nextBoolean()) {
                    foliage.add(drift);
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
