package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BluestoneLampBlock extends Block {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public BluestoneLampBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(LIT, shouldLight(context.getLevel(), context.getClickedPos()));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) {
            return;
        }
        boolean lit = state.getValue(LIT);
        boolean powered = shouldLight(level, pos);
        if (lit && !powered) {
            level.scheduleTick(pos, this, 4);
        } else if (!lit && powered) {
            level.setBlock(pos, state.setValue(LIT, true), 2);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && !shouldLight(level, pos)) {
            level.setBlock(pos, state.setValue(LIT, false), 2);
        }
    }

    private static boolean shouldLight(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos) || BluestoneSignalHelper.getBestNeighborSignal(level, pos) > 0;
    }
}
