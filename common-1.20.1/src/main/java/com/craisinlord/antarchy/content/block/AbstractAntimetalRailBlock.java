package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
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
    public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        RailShape shape = state.getValue(getShapeProperty());
        return shape.isAscending() ? CEILING_SLOPE_SHAPE : CEILING_FLAT_SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
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
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (direction != Direction.DOWN && direction != Direction.UP) {
            AntimetalRailHelper.updateConnections(this, state, level, pos);
        }
        return state;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(state.getBlock())) {
            AntimetalRailHelper.updateConnections(this, state, level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
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
