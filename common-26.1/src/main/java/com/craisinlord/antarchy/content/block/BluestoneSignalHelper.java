package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BluestoneSignalHelper {
    private BluestoneSignalHelper() {
    }

    public static int getBestNeighborSignal(LevelReader level, BlockPos pos) {
        int signal = 0;
        for (Direction direction : Direction.values()) {
            signal = Math.max(signal, getDirectBluestoneSignal(level, pos.relative(direction), direction));
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
    }

    public static int getDirectBluestoneSignal(LevelReader level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BluestoneSignalSource source) {
            return source.getBluestoneSignal(level, pos, state, direction);
        }
        return 0;
    }

    public static int getWireSignal(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BluestoneSignalSource source) {
            return source.getBluestoneSignal(level, pos, state, Direction.DOWN);
        }
        return 0;
    }

    public static int getSignalExcludingRedstoneWire(LevelReader level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        if (isVanillaRedstoneComponent(state)) {
            return 0;
        }
        int signal = state.getSignal(level, pos, direction);
        if (state.isRedstoneConductor(level, pos)) {
            signal = Math.max(signal, getDirectSignalToExcludingRedstoneWire(level, pos));
        }
        return signal;
    }

    private static int getDirectSignalToExcludingRedstoneWire(BlockGetter level, BlockPos pos) {
        int signal = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (isVanillaRedstoneComponent(neighborState)) {
                continue;
            }
            signal = Math.max(signal, neighborState.getDirectSignal(level, neighborPos, direction));
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
    }

    public static boolean canBluestoneConnectTo(BlockState state) {
        return state.is(AntarchyTags.Blocks.BLUESTONE_CONNECTABLE);
    }

    public static boolean isBluestoneComponent(BlockState state) {
        return state.is(AntarchyTags.Blocks.BLUESTONE_COMPONENTS);
    }

    public static void updateBluestoneNeighbors(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), level.getBlockState(pos).getBlock());
        }
    }

    public static boolean isVanillaRedstoneComponent(BlockState state) {
        return state.is(Blocks.REDSTONE_WIRE)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.REDSTONE_WALL_TORCH)
                || state.is(Blocks.REPEATER)
                || state.is(Blocks.COMPARATOR);
    }
}
