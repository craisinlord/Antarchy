package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.entity.UpwardFallingBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.material.Fluids;

public class TyphoniteSpikeBlock extends PointedDripstoneBlock {
    @SuppressWarnings("rawtypes")
    public static final MapCodec<PointedDripstoneBlock> CODEC = (MapCodec) Block.simpleCodec(TyphoniteSpikeBlock::new);

    public TyphoniteSpikeBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PointedDripstoneBlock> codec() {
        return CODEC;
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
        DripstoneThickness thickness = calculateDripstoneThickness(level, pos, direction, mergeTips);
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
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (direction != Direction.UP && direction != Direction.DOWN) {
            return state;
        }

        Direction tipDirection = state.getValue(TIP_DIRECTION);
        if (tipDirection == Direction.DOWN && level.getBlockTicks().hasScheduledTick(pos, this)) {
            return state;
        }

        if (direction == tipDirection.getOpposite() && !this.canSurvive(state, level, pos)) {
            if (tipDirection == Direction.DOWN) {
                level.scheduleTick(pos, this, 2);
            } else {
                level.scheduleTick(pos, this, 1);
            }

            return state;
        }

        boolean mergeTips = state.getValue(THICKNESS) == DripstoneThickness.TIP_MERGE;
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
            while (isTyphoniteSpikeWithDirection(level.getBlockState(root.above()), Direction.DOWN)) {
                root.move(Direction.UP);
            }

            BlockPos.MutableBlockPos cursor = root.mutable();
            while (isTyphoniteSpikeWithDirection(level.getBlockState(cursor), Direction.DOWN)) {
                BlockState fallingState = level.getBlockState(cursor);
                if (isTyphoniteSpikeWithDirection(fallingState, Direction.DOWN)) {
                    FallingBlockEntity.fall(level, cursor, fallingState);
                }
                cursor.move(Direction.DOWN);
            }
            return;
        }

        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (isTyphoniteSpikeWithDirection(level.getBlockState(cursor), Direction.UP)) {
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

    private static DripstoneThickness calculateDripstoneThickness(LevelReader level, BlockPos pos, Direction direction, boolean mergeTips) {
        Direction opposite = direction.getOpposite();
        BlockState forwardState = level.getBlockState(pos.relative(direction));
        if (isTyphoniteSpikeWithDirection(forwardState, opposite)) {
            return !mergeTips && forwardState.getValue(THICKNESS) != DripstoneThickness.TIP_MERGE
                    ? DripstoneThickness.TIP
                    : DripstoneThickness.TIP_MERGE;
        }

        if (!isTyphoniteSpikeWithDirection(forwardState, direction)) {
            return DripstoneThickness.TIP;
        }

        DripstoneThickness forwardThickness = forwardState.getValue(THICKNESS);
        if (forwardThickness == DripstoneThickness.TIP || forwardThickness == DripstoneThickness.TIP_MERGE) {
            return DripstoneThickness.MIDDLE;
        }

        return DripstoneThickness.BASE;
    }

    private static boolean isValidPointedDripstonePlacement(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos supportPos = pos.relative(direction.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, direction) || isTyphoniteSpikeWithDirection(supportState, direction);
    }

    private static boolean isTyphoniteSpikeWithDirection(BlockState state, Direction direction) {
        return state.getBlock() instanceof TyphoniteSpikeBlock && state.getValue(TIP_DIRECTION) == direction;
    }
}
