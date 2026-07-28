package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.AntarchyTags;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public final class MinersDreamCaveGenerator {
    private static final double MIN_HORIZONTAL_RADIUS = 2.0D;
    private static final double MAX_HORIZONTAL_RADIUS = 5.0D;
    private static final double MIN_VERTICAL_RADIUS = 1.5D;
    private static final double MAX_VERTICAL_RADIUS = 4.0D;
    private static final double MAX_VERTICAL_DEVIATION = 8.0D;
    private static final double SOFT_VERTICAL_DEVIATION = 4.0D;
    private static final int WORLD_MARGIN = 3;

    private MinersDreamCaveGenerator() {
    }

    public record CaveNode(BlockPos center, double horizontalRadius, double verticalRadius) {
    }

    public record CaveGenerationResult(List<CaveNode> nodes, Set<Integer> torchCheckpoints) {
    }

    public static CaveGenerationResult generate(BlockPos start, Direction initialDirection, int length,
                                                 int torchSpacing, int minBuildHeight, int maxBuildHeight, RandomSource random) {
        List<CaveNode> nodes = new ArrayList<>(length);
        Set<Integer> torchCheckpoints = new HashSet<>();

        double x = start.getX() + 0.5D;
        double y = start.getY() + 0.5D;
        double z = start.getZ() + 0.5D;
        double startY = y;

        double headingX = initialDirection.getStepX();
        double headingZ = initialDirection.getStepZ();
        if (headingX == 0.0D && headingZ == 0.0D) {
            headingX = 1.0D;
        }

        double verticalVelocity = 0.0D;
        double horizontalRadius = 3.0D;
        double verticalRadius = 2.0D;
        int minY = minBuildHeight + WORLD_MARGIN;
        int maxY = maxBuildHeight - WORLD_MARGIN;

        int spacing = Math.max(4, torchSpacing);
        int traveledSinceTorch = 0;
        int nextTorchDistance = spacing + random.nextInt(5) - 2;

        for (int i = 0; i < length; i++) {
            BlockPos center = BlockPos.containing(x, Mth.clamp(y, minY, maxY), z);
            nodes.add(new CaveNode(center, horizontalRadius, verticalRadius));

            traveledSinceTorch++;
            if (traveledSinceTorch >= Math.max(1, nextTorchDistance)) {
                torchCheckpoints.add(i);
                traveledSinceTorch = 0;
                nextTorchDistance = spacing + random.nextInt(5) - 2;
            }

            double turnDegrees = random.nextGaussian() * 8.0D;
            if (random.nextFloat() < 0.1F) {
                turnDegrees = random.nextGaussian() * 15.0D;
            }
            turnDegrees = Mth.clamp(turnDegrees, -25.0D, 25.0D);
            double turnRadians = Math.toRadians(turnDegrees);
            double cos = Math.cos(turnRadians);
            double sin = Math.sin(turnRadians);
            double newHeadingX = headingX * cos - headingZ * sin;
            double newHeadingZ = headingX * sin + headingZ * cos;
            double headingLength = Math.sqrt(newHeadingX * newHeadingX + newHeadingZ * newHeadingZ);
            headingX = newHeadingX / headingLength;
            headingZ = newHeadingZ / headingLength;

            verticalVelocity += random.nextGaussian() * 0.15D;
            double deviation = y - startY;
            verticalVelocity -= deviation * 0.06D;
            if (deviation > SOFT_VERTICAL_DEVIATION) {
                verticalVelocity -= 0.08D;
            } else if (deviation < -SOFT_VERTICAL_DEVIATION) {
                verticalVelocity += 0.08D;
            }
            verticalVelocity = Mth.clamp(verticalVelocity, -0.6D, 0.6D);

            x += headingX;
            z += headingZ;
            y = Mth.clamp(y + verticalVelocity, startY - MAX_VERTICAL_DEVIATION, startY + MAX_VERTICAL_DEVIATION);

            horizontalRadius = Mth.clamp(horizontalRadius + random.nextGaussian() * 0.15D, MIN_HORIZONTAL_RADIUS, MAX_HORIZONTAL_RADIUS);
            verticalRadius = Mth.clamp(verticalRadius + random.nextGaussian() * 0.12D, MIN_VERTICAL_RADIUS, MAX_VERTICAL_RADIUS);
            if (random.nextFloat() < 0.05F) {
                horizontalRadius = Mth.clamp(horizontalRadius + 0.6D, MIN_HORIZONTAL_RADIUS, MAX_HORIZONTAL_RADIUS);
                verticalRadius = Mth.clamp(verticalRadius + 0.5D, MIN_VERTICAL_RADIUS, MAX_VERTICAL_RADIUS);
            }
        }

        return new CaveGenerationResult(nodes, torchCheckpoints);
    }

    public static void collectNodeCandidates(ServerLevel level, CaveNode node, LongOpenHashSet visited, RandomSource random, List<BlockPos> out) {
        BlockPos center = node.center();
        int hr = Mth.ceil(node.horizontalRadius());
        double upExtent = node.verticalRadius() * 1.25D;
        double downExtent = node.verticalRadius() * 0.65D;
        int upR = Mth.ceil(upExtent);
        int downR = Mth.ceil(downExtent);
        int minBuildHeight = level.getMinBuildHeight();
        int maxBuildHeight = level.getMaxBuildHeight();

        for (int dy = -downR; dy <= upR; dy++) {
            double vExtent = dy >= 0 ? upExtent : downExtent;
            if (vExtent <= 0.0D) {
                continue;
            }
            double vTerm = (dy * dy) / (vExtent * vExtent);
            if (vTerm > 1.05D) {
                continue;
            }

            for (int dx = -hr; dx <= hr; dx++) {
                double hxTerm = (dx * dx) / (node.horizontalRadius() * node.horizontalRadius());
                for (int dz = -hr; dz <= hr; dz++) {
                    double hzTerm = (dz * dz) / (node.horizontalRadius() * node.horizontalRadius());
                    double distance = hxTerm + vTerm + hzTerm;
                    if (distance > 1.0D) {
                        continue;
                    }
                    if (distance > 0.7D) {
                        double edgeChance = (1.0D - distance) / 0.3D * 0.85D;
                        if (random.nextFloat() > edgeChance) {
                            continue;
                        }
                    }

                    BlockPos pos = center.offset(dx, dy, dz);
                    if (pos.getY() < minBuildHeight || pos.getY() >= maxBuildHeight) {
                        continue;
                    }

                    long packed = pos.asLong();
                    if (visited.contains(packed)) {
                        continue;
                    }

                    BlockState state = level.getBlockState(pos);
                    if (!state.is(AntarchyTags.Blocks.MINERS_DREAM_CARVABLE)) {
                        continue;
                    }
                    if (level.getBlockEntity(pos) != null) {
                        continue;
                    }

                    visited.add(packed);
                    out.add(pos);
                }
            }
        }
    }

    public static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
        return level.isLoaded(pos);
    }
}
