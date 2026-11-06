package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class AbstractAntimetalRailBlock extends BaseRailBlock {
    protected static final VoxelShape CEILING_FLAT_SHAPE = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape CEILING_SLOPE_SHAPE = Block.box(0.0D, 6.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    protected AbstractAntimetalRailBlock(boolean isStraight, BlockBehaviour.Properties properties) {
        super(isStraight, properties);
    }

    public static boolean isAntimetalRail(BlockState state) {
        return state.getBlock() instanceof AbstractAntimetalRailBlock;
    }

    public static boolean hasCeilingSupport(LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        RailShape shape = state.getValue(getShapeProperty());
        return shape.isSlope() ? CEILING_SLOPE_SHAPE : CEILING_FLAT_SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return hasCeilingSupport(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.DOWN && context.getClickedFace() != Direction.UP && !context.replacingClickedOnBlock()) {
            return null;
        }
        if (!hasCeilingSupport(context.getLevel(), context.getClickedPos())) {
            return null;
        }
        return AntimetalRailHelper.getStateForPlacement(this, context);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction != Direction.DOWN && direction != Direction.UP && level instanceof Level realLevel) {
            AntimetalRailHelper.updateConnections(this, state, realLevel, pos);
        }
        return state;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            AntimetalRailHelper.updateConnections(this, state, level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        if (!state.canSurvive(level, pos)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, movedByPiston);
            return;
        }
        AntimetalRailHelper.updateConnections(this, state, level, pos);
    }
}
