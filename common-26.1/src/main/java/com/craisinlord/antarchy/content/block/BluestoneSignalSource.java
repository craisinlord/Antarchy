package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface BluestoneSignalSource {
    int getBluestoneSignal(LevelReader level, BlockPos pos, BlockState state, Direction direction);
}
