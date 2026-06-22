package com.craisinlord.antarchy.fabric.content.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class AntiwaterLiquidBlock extends LiquidBlock {
    public AntiwaterLiquidBlock(FlowingFluid fluid, BlockBehaviour.Properties properties) {
        super(fluid, properties);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        scheduleCurrentAntiwaterTick(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        scheduleCurrentAntiwaterTick(level, pos);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        scheduleCurrentAntiwaterTick(level, pos);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            scheduleDependentAntiwaterTicks(level, pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private void scheduleCurrentAntiwaterTick(LevelAccessor level, BlockPos pos) {
        level.scheduleTick(pos, this.fluid, this.fluid.getTickDelay(level));
    }

    private void scheduleDependentAntiwaterTicks(LevelAccessor level, BlockPos pos) {
        level.scheduleTick(pos.above(), this.fluid, this.fluid.getTickDelay(level));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.scheduleTick(pos.relative(direction), this.fluid, this.fluid.getTickDelay(level));
        }
    }
}
