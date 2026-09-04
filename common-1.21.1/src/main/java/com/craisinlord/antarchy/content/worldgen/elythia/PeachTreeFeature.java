package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.block.PeachLeavesBlock;
import com.mojang.serialization.Codec;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class PeachTreeFeature extends Feature<PeachTreeConfiguration> {
    private static final ResourceLocation PEACH_LOG_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "peach_log");
    private static final ResourceLocation PEACH_LEAVES_ID = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "peach_leaves");

    public PeachTreeFeature(Codec<PeachTreeConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<PeachTreeConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        PeachTreeConfiguration config = context.config();
        int height = config.height().sample(random);
        int canopyRadius = config.canopyRadius().sample(random);
        int trunkWidth = config.trunkWidth();

        if (!canGrowOn(level, origin.below())) {
            return false;
        }

        List<BlockPos> trunkPath = buildTrunkPath(origin, height, random);
        for (BlockPos trunkPos : trunkPath) {
            for (int dx = 0; dx < trunkWidth; dx++) {
                for (int dz = 0; dz < trunkWidth; dz++) {
                    if (!canReplace(level, trunkPos.offset(dx, 0, dz))) {
                        return false;
                    }
                }
            }
        }
        Set<BlockPos> logPositions = new LinkedHashSet<>();
        Set<BlockPos> leafPositions = new LinkedHashSet<>();

        placeTrunk(level, trunkPath, logPositions, height, trunkWidth, random, config);

        int branchStart = Math.max(6, height - 5);
        List<BranchTip> branchTips = buildBranches(level, trunkPath, branchStart, logPositions, leafPositions, canopyRadius, trunkWidth, random, config);
        BlockPos canopyCenter = trunkPath.get(trunkPath.size() - 1).offset(trunkWidth - 1, 1, trunkWidth - 1);
        placeCentralCanopy(level, canopyCenter, canopyRadius, logPositions, leafPositions, random, config);
        for (BranchTip tip : branchTips) {
            placeBranchCanopy(level, tip, canopyCenter, canopyRadius, logPositions, leafPositions, random, config);
        }
        if (trunkWidth == 2) {
            reinforceLargeCanopy(level, canopyCenter, branchTips, logPositions, leafPositions, random, config);
        }
        connectCanopy(level, canopyCenter, branchTips, logPositions, leafPositions, random);
        refreshLeafDistances(level, leafPositions, logPositions);
        placeHangingPeaches(level, canopyCenter, canopyRadius, leafPositions, random, config);

        return !logPositions.isEmpty() || !leafPositions.isEmpty();
    }

    private List<BlockPos> buildTrunkPath(BlockPos origin, int height, RandomSource random) {
        List<BlockPos> trunkPath = new ArrayList<>(height);
        Direction leanDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        double driftX = leanDirection.getStepX() * (0.12D + random.nextDouble() * 0.05D);
        double driftZ = leanDirection.getStepZ() * (0.12D + random.nextDouble() * 0.05D);
        double wobbleX = (random.nextDouble() - 0.5D) * 0.04D;
        double wobbleZ = (random.nextDouble() - 0.5D) * 0.04D;
        double offsetX = 0.0D;
        double offsetZ = 0.0D;

        for (int y = 0; y < height; y++) {
            double progress = y / (double) Math.max(1, height - 1);
            double bend = progress < 0.35D ? 0.0D : (progress - 0.35D) / 0.65D;
            offsetX += (driftX + wobbleX) * bend;
            offsetZ += (driftZ + wobbleZ) * bend;
            trunkPath.add(origin.offset(Mth.floor(offsetX), y, Mth.floor(offsetZ)));
        }

        return trunkPath;
    }

    private void placeTrunk(WorldGenLevel level, List<BlockPos> trunkPath, Set<BlockPos> logPositions, int height, int trunkWidth, RandomSource random, PeachTreeConfiguration config) {
        Direction leanDirection = Direction.NORTH;
        if (trunkPath.size() >= 2) {
            BlockPos base = trunkPath.get(0);
            BlockPos next = trunkPath.get(1);
            leanDirection = directionFromDelta(next.getX() - base.getX(), next.getZ() - base.getZ());
        }

        for (int i = 0; i < trunkPath.size(); i++) {
            BlockPos pos = trunkPath.get(i);
            placeTrunkColumn(level, pos, trunkWidth, Direction.Axis.Y, logPositions, config, random);

            if (trunkWidth == 1 && height >= 13 && i < 2) {
                placeLog(level, pos.relative(leanDirection.getOpposite()), Direction.Axis.Y, logPositions, config, random);
            }
            if (trunkWidth == 1 && height >= 15 && i == 0) {
                placeLog(level, pos.relative(leanDirection.getClockWise()), Direction.Axis.Y, logPositions, config, random);
            }
        }
    }

    private List<BranchTip> buildBranches(
            WorldGenLevel level,
            List<BlockPos> trunkPath,
            int branchStart,
            Set<BlockPos> logPositions,
            Set<BlockPos> leafPositions,
            int canopyRadius,
            int trunkWidth,
            RandomSource random
            , PeachTreeConfiguration config
    ) {
        int branchCount = config.branchCount().sample(random);
        double baseAngle = random.nextDouble() * Math.PI * 2.0D;
        List<BranchTip> tips = new ArrayList<>(branchCount);

        for (int i = 0; i < branchCount; i++) {
            int attachIndex = Math.min(trunkPath.size() - 1, branchStart + random.nextInt(Math.max(1, trunkPath.size() - branchStart)));
            BlockPos anchor = trunkPath.get(attachIndex).offset(trunkWidth - 1, 0, trunkWidth - 1);
            double angle = baseAngle + (Math.PI * 2.0D * i / branchCount) + (random.nextDouble() - 0.5D) * 0.55D;
            int length = trunkWidth == 2 ? 6 + random.nextInt(5) : 3 + random.nextInt(4);
            int rise = trunkWidth == 2 ? 2 + random.nextInt(3) : 1 + random.nextInt(2);
            BlockPos tip = placeBranch(level, anchor, angle, length, rise, logPositions, leafPositions, random, config);
            tips.add(new BranchTip(anchor, tip, angle));

            if (random.nextFloat() < 0.30F) {
                double forkAngle = angle + (random.nextBoolean() ? Math.PI / 3.0D : -Math.PI / 3.0D);
                placeBranch(level, tip, forkAngle, trunkWidth == 2 ? 2 + random.nextInt(3) : 1 + random.nextInt(3), random.nextInt(2), logPositions, leafPositions, random, config);
            }
        }

        return tips;
    }

    private BlockPos placeBranch(
            WorldGenLevel level,
            BlockPos start,
            double angle,
            int length,
            int rise,
            Set<BlockPos> logPositions,
            Set<BlockPos> leafPositions,
            RandomSource random,
            PeachTreeConfiguration config
    ) {
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        double perpX = -dirZ;
        double perpZ = dirX;
        double curveStrength = 0.75D + random.nextDouble() * 0.55D;
        BlockPos tip = start;

        for (int step = 1; step <= length; step++) {
            double t = step / (double) length;
            double curve = Math.sin(t * Math.PI) * curveStrength;
            int x = Mth.floor(Mth.lerp(t, start.getX(), start.getX() + dirX * length) + perpX * curve);
            int y = Mth.floor(Mth.lerp(t, start.getY(), start.getY() + rise) + Math.sin(t * Math.PI) * 0.35D);
            int z = Mth.floor(Mth.lerp(t, start.getZ(), start.getZ() + dirZ * length) + perpZ * curve);
            BlockPos current = new BlockPos(x, y, z);
            BlockPos previous = tip;
            tip = current;

            Direction.Axis axis = axisForSegment(previous, current);
            placeLog(level, current, axis, logPositions, config, random);
            if (step >= length - 1) {
                placeLeafCluster(level, current.above(), 2 + random.nextInt(2), leafPositions, random);
            }
        }

        return tip;
    }

    private void placeCentralCanopy(WorldGenLevel level, BlockPos center, int canopyRadius, Set<BlockPos> logPositions, Set<BlockPos> leafPositions, RandomSource random, PeachTreeConfiguration config) {
        placeCanopyLobe(level, center.below(), canopyRadius + 1, 1, canopyRadius, 0.35D, leafPositions, random);
        placeCanopyLobe(level, center, canopyRadius + 1, 2, canopyRadius + 1, 0.42D, leafPositions, random);
        int topRadius = Math.max(2, Mth.floor(canopyRadius * config.topCanopyScale()));
        placeCanopyLobe(level, center.above(), topRadius, 1, topRadius, 0.24D, leafPositions, random);
        placeLeafCluster(level, center.above(2), Math.max(2, topRadius - 1), leafPositions, random);
        placeLog(level, center.below(), Direction.Axis.Y, logPositions, config, random);
    }

    private void placeBranchCanopy(
            WorldGenLevel level,
            BranchTip tip,
            BlockPos canopyCenter,
            int canopyRadius,
            Set<BlockPos> logPositions,
            Set<BlockPos> leafPositions,
            RandomSource random,
            PeachTreeConfiguration config
    ) {
        int lobeRadius = Math.max(3, canopyRadius - 1 + random.nextInt(2));
        BlockPos lobeCenter = tip.tip().above(random.nextInt(2));
        placeCanopyLobe(level, lobeCenter, lobeRadius, 2, lobeRadius, 0.36D, leafPositions, random);
        placeCanopyLobe(level, lobeCenter.above(), Math.max(2, lobeRadius - 1), 1, Math.max(2, lobeRadius - 1), 0.22D, leafPositions, random);

        BlockPos bridgeCenter = tip.tip().offset(
                Mth.floor(Math.cos(tip.angle()) * 2.0D),
                0,
                Mth.floor(Math.sin(tip.angle()) * 2.0D)
        ).above(1);
        placeLeafCluster(level, bridgeCenter, Math.max(2, canopyRadius - 2), leafPositions, random);
        placeLog(level, canopyCenter, Direction.Axis.Y, logPositions, config, random);
    }

    private void reinforceLargeCanopy(
            WorldGenLevel level,
            BlockPos canopyCenter,
            List<BranchTip> branchTips,
            Set<BlockPos> logPositions,
            Set<BlockPos> leafPositions,
            RandomSource random,
            PeachTreeConfiguration config
    ) {
        placeLog(level, canopyCenter.above(), Direction.Axis.Y, logPositions, config, random);
        placeLog(level, canopyCenter.above(2), Direction.Axis.Y, logPositions, config, random);
        placeLog(level, canopyCenter.below(), Direction.Axis.Y, logPositions, config, random);

        for (BranchTip tip : branchTips) {
            BlockPos mid = interpolate(tip.tip(), canopyCenter, 0.5D, 0.0D).above(1);
            placeLog(level, mid, axisForSegment(tip.tip(), mid), logPositions, config, random);

            BlockPos nearTip = interpolate(tip.tip(), canopyCenter, 0.25D, 0.0D);
            placeLog(level, nearTip.above(), axisForSegment(nearTip, canopyCenter), logPositions, config, random);

            BlockPos supportStart = tip.anchor().above(1);
            BlockPos supportEnd = tip.tip().above(1);
            placeLog(level, supportStart, axisForSegment(supportStart, supportEnd), logPositions, config, random);
            placeLog(level, supportEnd, axisForSegment(supportStart, supportEnd), logPositions, config, random);
        }

        placeLeafCluster(level, canopyCenter.above(1), Math.max(2, config.canopyRadius().sample(random) - 1), leafPositions, random);
    }

    private void connectCanopy(
            WorldGenLevel level,
            BlockPos canopyCenter,
            List<BranchTip> branchTips,
            Set<BlockPos> logPositions,
            Set<BlockPos> leafPositions,
            RandomSource random
    ) {
        for (BranchTip tip : branchTips) {
            BlockPos start = tip.tip();
            int steps = 2 + random.nextInt(2);
            for (int i = 1; i <= steps; i++) {
                double t = i / (double) (steps + 1);
                BlockPos pos = interpolate(start, canopyCenter, t, 0.0D).above(random.nextInt(2));
                placeLeaf(level, pos, leafPositions);
            }
        }
    }

    private void placeCanopyLobe(
            WorldGenLevel level,
            BlockPos center,
            int radiusX,
            int radiusY,
            int radiusZ,
            double edgeNoise,
            Set<BlockPos> leafPositions,
            RandomSource random
    ) {
        int minY = -radiusY;
        int maxY = radiusY;
        for (int dy = minY; dy <= maxY; dy++) {
            double vertical = dy / (double) Math.max(1, radiusY);
            for (int dx = -radiusX; dx <= radiusX; dx++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    double nx = dx / (double) Math.max(1, radiusX);
                    double nz = dz / (double) Math.max(1, radiusZ);
                    double normalized = nx * nx + (vertical * vertical * 1.2D) + nz * nz;
                    if (normalized > 1.0D + edgeNoise + random.nextDouble() * 0.04D) {
                        continue;
                    }
                    if (dy < 0 && normalized > 0.65D && random.nextFloat() < 0.55F) {
                        continue;
                    }
                    if (dy > 0 && normalized < 0.25D && random.nextFloat() < 0.18F) {
                        continue;
                    }

                    BlockPos pos = center.offset(dx, dy, dz);
                    placeLeaf(level, pos, leafPositions);
                }
            }
        }
    }

    private void placeLeafCluster(WorldGenLevel level, BlockPos center, int radius, Set<BlockPos> leafPositions, RandomSource random) {
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    int distanceSq = x * x + z * z + y * y;
                    if (distanceSq > radiusSq) {
                        continue;
                    }
                    if (distanceSq > radiusSq - radius && random.nextFloat() < 0.20F) {
                        continue;
                    }
                    placeLeaf(level, center.offset(x, y, z), leafPositions);
                }
            }
        }
    }

    private void placeHangingPeaches(WorldGenLevel level, BlockPos canopyCenter, int canopyRadius, Set<BlockPos> leafPositions, RandomSource random, PeachTreeConfiguration config) {
        int fruitAttempts = 12 + random.nextInt(10);
        List<BlockPos> candidates = new ArrayList<>(leafPositions);
        for (int i = 0; i < fruitAttempts && !candidates.isEmpty(); i++) {
            BlockPos leafPos = candidates.get(random.nextInt(candidates.size()));
            int dx = Math.abs(leafPos.getX() - canopyCenter.getX());
            int dz = Math.abs(leafPos.getZ() - canopyCenter.getZ());
            int horizontal = dx + dz;
            if (horizontal < Math.max(2, canopyRadius - 2)) {
                continue;
            }
            BlockPos fruitPos = leafPos.below();
            if (!level.getBlockState(fruitPos).isAir()) {
                continue;
            }
            if (random.nextFloat() > config.fruitChance()) {
                continue;
            }
            setBlock(level, fruitPos, PeachLeavesBlock.createHangingPeachState());
        }
    }

    private void refreshLeafDistances(WorldGenLevel level, Set<BlockPos> leafPositions, Set<BlockPos> logPositions) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(logPositions);
        java.util.Map<BlockPos, Integer> distances = new java.util.HashMap<>();
        for (BlockPos logPos : logPositions) {
            distances.put(logPos, 0);
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            int currentDistance = distances.getOrDefault(current, 0);
            if (currentDistance >= PeachLeavesBlock.PEACH_MAX_DISTANCE) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (distances.containsKey(next)) {
                    continue;
                }
                BlockState state = level.getBlockState(next);
                if (!(state.getBlock() instanceof LeavesBlock)) {
                    continue;
                }
                distances.put(next, currentDistance + 1);
                queue.addLast(next);
            }
        }

        for (BlockPos pos : leafPositions) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof LeavesBlock)) {
                continue;
            }

            IntegerProperty property = state.getBlock() instanceof PeachLeavesBlock ? PeachLeavesBlock.PEACH_DISTANCE : null;
            if (property == null) {
                continue;
            }

            int distance = Math.min(PeachLeavesBlock.PEACH_DECAY_DISTANCE, distances.getOrDefault(pos, PeachLeavesBlock.PEACH_DECAY_DISTANCE));
            if (state.getValue(property) != distance) {
                setBlock(level, pos, PeachLeavesBlock.setPeachDistanceForWorldgen(state, distance));
            }
        }
    }

    private void placeTrunkColumn(WorldGenLevel level, BlockPos pos, int trunkWidth, Direction.Axis axis, Set<BlockPos> logPositions, PeachTreeConfiguration config, RandomSource random) {
        for (int dx = 0; dx < trunkWidth; dx++) {
            for (int dz = 0; dz < trunkWidth; dz++) {
                placeLog(level, pos.offset(dx, 0, dz), axis, logPositions, config, random);
            }
        }
    }

    private boolean placeLog(WorldGenLevel level, BlockPos pos, Direction.Axis axis, Set<BlockPos> logPositions, PeachTreeConfiguration config, RandomSource random) {
        if (!canReplace(level, pos)) {
            return false;
        }
        BlockState state = config.trunkProvider().getState(random, pos);
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            state = state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        setBlock(level, pos, state);
        logPositions.add(pos);
        return true;
    }

    private boolean placeLeaf(WorldGenLevel level, BlockPos pos, Set<BlockPos> leafPositions) {
        if (!canReplace(level, pos)) {
            return false;
        }
        setBlock(level, pos, leafState());
        leafPositions.add(pos);
        return true;
    }

    private BlockState leafState() {
        return peachBlock(PEACH_LEAVES_ID).defaultBlockState();
    }

    private static Block peachBlock(ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id)
                .orElseThrow(() -> new IllegalStateException("Missing peach block: " + id));
    }

    private static boolean canGrowOn(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFaceSturdy(level, pos, Direction.UP);
    }

    private static boolean canReplace(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.getBlock() instanceof LeavesBlock;
    }

    private static Direction.Axis axisForSegment(BlockPos from, BlockPos to) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        int dz = Math.abs(to.getZ() - from.getZ());
        if (dy > dx && dy > dz) {
            return Direction.Axis.Y;
        }
        return dx >= dz ? Direction.Axis.X : Direction.Axis.Z;
    }

    private static Direction directionFromDelta(int dx, int dz) {
        if (Math.abs(dx) >= Math.abs(dz)) {
            return dx >= 0 ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static BlockPos interpolate(BlockPos start, BlockPos end, double t, double sidewaysOffset) {
        double x = Mth.lerp(t, start.getX(), end.getX());
        double y = Mth.lerp(t, start.getY(), end.getY());
        double z = Mth.lerp(t, start.getZ(), end.getZ());
        if (sidewaysOffset != 0.0D) {
            double dx = end.getX() - start.getX();
            double dz = end.getZ() - start.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 0.0D) {
                x += (-dz / length) * sidewaysOffset;
                z += (dx / length) * sidewaysOffset;
            }
        }
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    private record BranchTip(BlockPos anchor, BlockPos tip, double angle) {
    }
}
