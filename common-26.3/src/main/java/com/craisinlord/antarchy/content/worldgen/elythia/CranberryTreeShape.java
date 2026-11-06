package com.craisinlord.antarchy.content.worldgen.elythia;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

final class CranberryTreeShape {
    static Shape create(BlockPos origin, RandomSource random) {
        int height = 9 + random.nextInt(5);
        int top = origin.getY() + height - 1;
        Direction lean = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction side = random.nextBoolean() ? lean.getClockWise() : lean.getCounterClockWise();
        Map<BlockPos, Direction.Axis> logs = new LinkedHashMap<>();
        Set<BlockPos> leaves = new LinkedHashSet<>();
        BlockPos previous = origin;
        BlockPos fork = origin;
        int forkY = 3 + random.nextInt(2);
        for (int y = 0; y <= height - 4; y++) {
            double t = y / (double) (height - 4);
            int forward = (int) Math.round(Math.sin(t * Math.PI * 0.9) * 1.8);
            int lateral = (int) Math.round(Math.sin(t * Math.PI * 1.6) * 0.9);
            BlockPos pos = origin.above(y).relative(lean, forward).relative(side, lateral);
            connect(logs, previous, pos);
            if (y == forkY) fork = pos;
            previous = pos;
        }
        BlockPos highTip = previous.relative(lean, 3).relative(side);
        connect(logs, previous, previous.relative(lean).above());
        connect(logs, previous.relative(lean).above(), highTip.above());
        BlockPos crown = highTip.above();
        BlockPos lowTip = fork.relative(lean.getOpposite(), 3).relative(side.getOpposite());
        lowTip = new BlockPos(lowTip.getX(), origin.getY() + height - 5, lowTip.getZ());
        BlockPos elbow = fork.relative(lean.getOpposite(), 2);
        connect(logs, fork, elbow);
        connect(logs, elbow, lowTip);
        addFan(leaves, crown, lean, side, 4, top, origin.getY() + 3);
        addFan(leaves, lowTip.above(), lean.getOpposite(), side.getOpposite(), 3, top, origin.getY() + 3);
        addHangers(leaves, crown, lean, side, random, origin.getY() + 3);
        addHangers(leaves, lowTip.above(), lean.getOpposite(), side.getOpposite(), random, origin.getY() + 3);
        leaves.removeAll(logs.keySet());
        Map<BlockPos, Integer> distances = new LinkedHashMap<>();
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        for (BlockPos pos : logs.keySet()) {
            distances.put(pos, 0);
            frontier.add(pos);
        }
        while (!frontier.isEmpty()) {
            BlockPos pos = frontier.removeFirst();
            int distance = distances.get(pos) + 1;
            if (distance > 6) continue;
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (leaves.contains(next) && !distances.containsKey(next)) {
                    distances.put(next, distance);
                    frontier.addLast(next);
                }
            }
        }
        leaves.retainAll(distances.keySet());
        return new Shape(Map.copyOf(logs), Set.copyOf(leaves), Map.copyOf(distances), crown);
    }

    private static void connect(Map<BlockPos, Direction.Axis> logs, BlockPos from, BlockPos to) {
        BlockPos pos = from;
        logs.putIfAbsent(pos, Direction.Axis.Y);
        while (pos.getY() != to.getY()) {
            pos = pos.above(Integer.signum(to.getY() - pos.getY()));
            logs.put(pos, Direction.Axis.Y);
        }
        while (pos.getX() != to.getX()) {
            pos = pos.offset(Integer.signum(to.getX() - pos.getX()), 0, 0);
            logs.put(pos, Direction.Axis.X);
        }
        while (pos.getZ() != to.getZ()) {
            pos = pos.offset(0, 0, Integer.signum(to.getZ() - pos.getZ()));
            logs.put(pos, Direction.Axis.Z);
        }
    }

    private static void addFan(Set<BlockPos> leaves, BlockPos center, Direction forward, Direction side, int radius, int top, int minimumY) {
        for (int y = -1; y <= 2; y++) {
            double length = radius - (y == 2 ? 1.3 : y == 1 ? 0.5 : y == -1 ? 0.7 : 0);
            double width = y == 2 ? 1.4 : y == -1 ? 1.7 : 2.3;
            for (int u = -radius; u <= radius; u++) {
                double sweep = Math.max(0, u) * 0.25;
                for (int v = -3; v <= 3; v++) {
                    if (u * u / (length * length) + (v - sweep) * (v - sweep) / (width * width) > 1.12) continue;
                    if (u < -1 && v == 0 && y <= 0) continue;
                    BlockPos pos = center.relative(forward, u).relative(side, v).above(y);
                    if (pos.getY() <= top && pos.getY() >= minimumY) leaves.add(pos);
                }
            }
        }
    }

    private static void addHangers(Set<BlockPos> leaves, BlockPos center, Direction forward, Direction side, RandomSource random, int minimumY) {
        for (int sign : new int[] {-1, 1}) {
            BlockPos anchor = center.relative(forward, (sign < 0 ? 1 : 1 + random.nextInt(2))).relative(side, sign * 2);
            while (!leaves.contains(anchor) && anchor.getY() > center.getY() - 1) anchor = anchor.below();
            if (!leaves.contains(anchor)) continue;
            int length = sign < 0 ? 4 : 2 + random.nextInt(2);
            for (int down = 1; down <= length; down++) {
                BlockPos pos = anchor.below(down);
                if (pos.getY() >= minimumY) leaves.add(pos);
            }
        }
    }

    record Shape(Map<BlockPos, Direction.Axis> logs, Set<BlockPos> leaves, Map<BlockPos, Integer> distances, BlockPos crown) {
        boolean fits(Predicate<BlockPos> replaceable) {
            return logs.keySet().stream().allMatch(replaceable) && leaves.stream().allMatch(replaceable);
        }
    }
}
