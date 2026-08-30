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

        int tierCount = Mth.clamp(2 + branchCount / 2, 3, 5);
        int outerRadius = 4 + bottomRadius + random.nextInt(3);

        placeTrunk(spine, logs, random);
        placeRootWaves(spine, bottomRadius, logs, foliage, random, veilRadius);
        placeChandelierTiers(spine, tierCount, outerRadius, logs, foliage, random, veilRadius);
        placeBranches(spine, Math.max(1, branchCount / 2), branchLength, logs, foliage, random, veilRadius);
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

    private void placeTrunk(List<BlockPos> spine, Map<BlockPos, Direction.Axis> logs, RandomSource random) {
        for (int index = 0; index < spine.size(); index++) {
            fillTrunkLayer(spine.get(index), index < 2, logs, random);
        }
    }

    private void fillTrunkLayer(BlockPos center, boolean flare, Map<BlockPos, Direction.Axis> logs, RandomSource random) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                putLog(logs, center.offset(dx, 0, dz), Direction.Axis.Y);
            }
        }
        if (flare) {
            for (int dx = -1; dx <= 2; dx++) {
                for (int dz = -1; dz <= 2; dz++) {
                    if ((dx == -1 || dx == 2 || dz == -1 || dz == 2) && random.nextFloat() < 0.5F) {
                        putLog(logs, center.offset(dx, 0, dz), Direction.Axis.Y);
                    }
                }
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

    private void placeChandelierTiers(
            List<BlockPos> spine,
            int tierCount,
            int outerRadius,
            Map<BlockPos, Direction.Axis> logs,
            Set<BlockPos> foliage,
            RandomSource random,
            int veilRadius
    ) {
        int top = Math.max(2, spine.size() / 5);
        int bottom = Math.max(top + 1, spine.size() - 2);
        for (int tier = 0; tier < tierCount; tier++) {
            float progress = tierCount == 1 ? 0.0F : tier / (float) (tierCount - 1);
            int index = Mth.clamp(Math.round(Mth.lerp(progress, top, bottom)), 0, spine.size() - 1);
            BlockPos hub = spine.get(index);
            int radius = Math.max(2, Math.round(Mth.lerp(progress, outerRadius, 2.0F)) + random.nextInt(2));
            int droop = 1 + random.nextInt(2);
            BlockPos ringCenter = hub.below(droop);

            placeTierRing(ringCenter, radius, logs, random);
            placeTierSpokes(hub, ringCenter, radius, logs);

            for (Direction spoke : Direction.Plane.HORIZONTAL) {
                BlockPos rim = ringCenter.relative(spoke, radius);
                Direction tangent = spoke.getClockWise();
                placeVeilRibbon(foliage, rim, tangent, Math.max(2, veilRadius + random.nextInt(2)), random);
            }
        }
    }

    private void placeTierRing(BlockPos center, int radius, Map<BlockPos, Direction.Axis> logs, RandomSource random) {
        int outerSq = (radius + 1) * (radius + 1);
        int innerSq = (radius - 1) * (radius - 1);
        for (int dx = -radius - 1; dx <= radius + 1; dx++) {
            for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                int distSq = dx * dx + dz * dz;
                if (distSq > outerSq || distSq < innerSq) {
                    continue;
                }
                if (random.nextFloat() < 0.12F) {
                    continue;
                }
                Direction.Axis axis = Math.abs(dx) >= Math.abs(dz) ? Direction.Axis.X : Direction.Axis.Z;
                putLog(logs, center.offset(dx, 0, dz), axis);
            }
        }
    }

    private void placeTierSpokes(BlockPos hub, BlockPos ringCenter, int radius, Map<BlockPos, Direction.Axis> logs) {
        for (Direction spoke : Direction.Plane.HORIZONTAL) {
            BlockPos previous = hub;
            for (int step = 1; step <= radius; step++) {
                float t = step / (float) radius;
                int y = Math.round(Mth.lerp(t, hub.getY(), ringCenter.getY()));
                BlockPos next = new BlockPos(hub.getX() + spoke.getStepX() * step, y, hub.getZ() + spoke.getStepZ() * step);
                putLog(logs, next, axisForSegment(previous, next));
                previous = next;
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
        int span = Math.max(4, length + 2);
        int bands = 1 + random.nextInt(2);
        float phase = random.nextFloat() * Mth.PI * 2.0F;
        float frequency = 0.55F + random.nextFloat() * 0.45F;
        for (int band = 0; band < bands; band++) {
            BlockPos rowAnchor = start.relative(side, band);
            float bandPhase = phase + band * 1.7F;
            for (int step = 0; step < span; step++) {
                float wave = Mth.sin(step * frequency + bandPhase);
                int lift = Math.round(wave * 1.5F);
                BlockPos top = rowAnchor.relative(flow, step).offset(0, lift, 0);
                int strand = Math.max(2, 3 + Math.round((wave + 1.0F) * 2.0F) + random.nextInt(2) - (1 + random.nextInt(3)));
                BlockPos cursor = top;
                for (int drop = 0; drop < strand; drop++) {
                    foliage.add(cursor);
                    cursor = cursor.below();
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
