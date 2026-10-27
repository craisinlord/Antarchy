package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;

public final class AntimetalPowerHelper {
    private static final int MAX_POWER_PROPAGATION = 8;

    private AntimetalPowerHelper() {
    }

    public static boolean hasAnySignal(LevelReader level, BlockPos pos) {
        if (level instanceof Level realLevel && realLevel.hasNeighborSignal(pos)) {
            return true;
        }
        return BluestoneSignalHelper.getBestNeighborSignal(level, pos) > 0;
    }

    public static int getBestAnySignal(LevelReader level, BlockPos pos) {
        int vanillaSignal = level instanceof Level realLevel ? realLevel.getBestNeighborSignal(pos) : 0;
        int bluestoneSignal = BluestoneSignalHelper.getBestNeighborSignal(level, pos);
        return Math.max(vanillaSignal, bluestoneSignal);
    }

    public static void notifyAllSignalNeighbors(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
    }

    public static boolean isPoweredWithChain(Level level, BlockPos pos, BlockState state, EnumProperty<RailShape> shapeProperty, Class<? extends Block> railClass) {
        if (!railClass.isInstance(state.getBlock())) {
            return false;
        }
        if (hasAnySignal(level, pos)) {
            return true;
        }
        RailShape shape = state.getValue(shapeProperty);
        for (Direction direction : exitDirections(shape)) {
            if (chain(level, pos, direction, shapeProperty, railClass, 1)) {
                return true;
            }
        }
        return false;
    }

    private static boolean chain(Level level, BlockPos pos, Direction direction, EnumProperty<RailShape> shapeProperty, Class<? extends Block> railClass, int distance) {
        if (distance > MAX_POWER_PROPAGATION) {
            return false;
        }
        BlockPos[] candidates = {pos.relative(direction), pos.relative(direction).above(), pos.relative(direction).below()};
        for (BlockPos candidate : candidates) {
            BlockState candidateState = level.getBlockState(candidate);
            if (railClass.isInstance(candidateState.getBlock()) && sameAxis(candidateState.getValue(shapeProperty), direction)) {
                if (hasAnySignal(level, candidate)) {
                    return true;
                }
                if (chain(level, candidate, direction, shapeProperty, railClass, distance + 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sameAxis(RailShape shape, Direction direction) {
        boolean zAxis = shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH;
        return direction.getAxis() == (zAxis ? Direction.Axis.Z : Direction.Axis.X);
    }

    private static Direction[] exitDirections(RailShape shape) {
        boolean zAxis = shape == RailShape.NORTH_SOUTH || shape == RailShape.ASCENDING_NORTH || shape == RailShape.ASCENDING_SOUTH;
        return zAxis ? new Direction[]{Direction.NORTH, Direction.SOUTH} : new Direction[]{Direction.EAST, Direction.WEST};
    }
}
