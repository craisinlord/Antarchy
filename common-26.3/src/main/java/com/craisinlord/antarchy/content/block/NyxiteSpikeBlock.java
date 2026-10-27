package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SpeleothemThickness;
import net.minecraft.world.level.material.Fluids;

public class NyxiteSpikeBlock extends PointedDripstoneBlock {
    public NyxiteSpikeBlock(BlockBehaviour.Properties properties) {
        super(Blocks.DRIPSTONE_BLOCK.defaultBlockState(), properties);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction direction = calculateTipDirection(level, pos, context.getNearestLookingVerticalDirection().getOpposite());
        if (direction == null) {
            return null;
        }

        boolean mergeTips = !context.isSecondaryUseActive();
        SpeleothemThickness thickness = calculateDripstoneThickness(level, pos, direction, mergeTips);
        if (thickness == null) {
            return null;
        }

        return this.defaultBlockState()
                .setValue(TIP_DIRECTION, direction)
                .setValue(THICKNESS, thickness)
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction != Direction.UP && direction != Direction.DOWN) {
            return state;
        }

        Direction tipDirection = state.getValue(TIP_DIRECTION);
        if (tipDirection == Direction.DOWN && tickAccess.getBlockTicks().hasScheduledTick(pos, this)) {
            return state;
        }

        if (direction == tipDirection.getOpposite() && !this.canSurvive(state, level, pos)) {
            if (tipDirection == Direction.DOWN) {
                tickAccess.scheduleTick(pos, this, 2);
            } else {
                tickAccess.scheduleTick(pos, this, 1);
            }

            return state;
        }

        boolean mergeTips = state.getValue(THICKNESS) == SpeleothemThickness.TIP_MERGE;
        return state.setValue(THICKNESS, calculateDripstoneThickness(level, pos, tipDirection, mergeTips));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidPointedDripstonePlacement(level, pos, state.getValue(TIP_DIRECTION));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.canSurvive(state, level, pos)) {
            return;
        }

        if (state.getValue(TIP_DIRECTION) == Direction.DOWN) {
            BlockPos.MutableBlockPos root = pos.mutable();
            while (isNyxiteSpikeWithDirection(level.getBlockState(root.above()), Direction.DOWN)) {
                root.move(Direction.UP);
            }

            BlockPos.MutableBlockPos cursor = root.mutable();
            while (isNyxiteSpikeWithDirection(level.getBlockState(cursor), Direction.DOWN)) {
                BlockState fallingState = level.getBlockState(cursor);
                if (isNyxiteSpikeWithDirection(fallingState, Direction.DOWN)) {
                    FallingBlockEntity.fall(level, cursor, fallingState);
                }
                cursor.move(Direction.DOWN);
            }
            return;
        }

        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (isNyxiteSpikeWithDirection(level.getBlockState(cursor), Direction.UP)) {
            BlockState spikeState = level.getBlockState(cursor);
            UpwardFallingBlockEntity.fallUp(level, cursor, spikeState);
            cursor.move(Direction.UP);
        }
    }

    private static Direction calculateTipDirection(LevelReader level, BlockPos pos, Direction preferredDirection) {
        if (isValidPointedDripstonePlacement(level, pos, preferredDirection)) {
            return preferredDirection;
        }

        Direction opposite = preferredDirection.getOpposite();
        return isValidPointedDripstonePlacement(level, pos, opposite) ? opposite : null;
    }

    private static SpeleothemThickness calculateDripstoneThickness(LevelReader level, BlockPos pos, Direction direction, boolean mergeTips) {
        Direction opposite = direction.getOpposite();
        BlockState forwardState = level.getBlockState(pos.relative(direction));
        if (isNyxiteSpikeWithDirection(forwardState, opposite)) {
            return !mergeTips && forwardState.getValue(THICKNESS) != SpeleothemThickness.TIP_MERGE
                    ? SpeleothemThickness.TIP
                    : SpeleothemThickness.TIP_MERGE;
        }

        if (!isNyxiteSpikeWithDirection(forwardState, direction)) {
            return SpeleothemThickness.TIP;
        }

        SpeleothemThickness forwardThickness = forwardState.getValue(THICKNESS);
        if (forwardThickness != SpeleothemThickness.TIP && forwardThickness != SpeleothemThickness.TIP_MERGE) {
            BlockState backState = level.getBlockState(pos.relative(opposite));
            return !isNyxiteSpikeWithDirection(backState, direction) ? SpeleothemThickness.BASE : SpeleothemThickness.MIDDLE;
        }

        return SpeleothemThickness.FRUSTUM;
    }

    private static boolean isValidPointedDripstonePlacement(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos supportPos = pos.relative(direction.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, direction) || isNyxiteSpikeWithDirection(supportState, direction);
    }

    private static boolean isNyxiteSpikeWithDirection(BlockState state, Direction direction) {
        return state.is(AntarchyObjects.NYXITE_SPIKE.get()) && state.getValue(TIP_DIRECTION) == direction;
    }
}
