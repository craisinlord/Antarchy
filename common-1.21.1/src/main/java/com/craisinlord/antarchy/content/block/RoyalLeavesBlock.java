package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class RoyalLeavesBlock extends LeavesBlock {
    public static final MapCodec<RoyalLeavesBlock> CODEC = Block.simpleCodec(RoyalLeavesBlock::new);
    public static final IntegerProperty ROYAL_DISTANCE = IntegerProperty.create("royal_distance", 1, 17);
    public static final BooleanProperty PERSISTENT = LeavesBlock.PERSISTENT;
    public static final BooleanProperty WATERLOGGED = LeavesBlock.WATERLOGGED;
    public static final int ROYAL_MAX_DISTANCE = 16;
    public static final int ROYAL_DECAY_DISTANCE = ROYAL_MAX_DISTANCE + 1;

    public RoyalLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(DISTANCE, DECAY_DISTANCE)
                .setValue(ROYAL_DISTANCE, ROYAL_DECAY_DISTANCE)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public MapCodec<RoyalLeavesBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = this.defaultBlockState()
                .setValue(PERSISTENT, true)
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

        return recalculateDistance(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LeavesBlock.DISTANCE, ROYAL_DISTANCE, PERSISTENT, WATERLOGGED);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return decaying(state);
    }

    @Override
    protected boolean decaying(BlockState state) {
        return !state.getValue(PERSISTENT) && state.getValue(ROYAL_DISTANCE) > ROYAL_MAX_DISTANCE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (decaying(state)) {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, recalculateDistance(state, level, pos), Block.UPDATE_ALL);
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

        int neighborDistance = Math.min(ROYAL_DECAY_DISTANCE, getDistanceAt(neighborState) + 1);
        int currentDistance = state.getValue(ROYAL_DISTANCE);

        if (neighborDistance != 1 || currentDistance != neighborDistance) {
            level.scheduleTick(pos, this, 1);
        }

        return state;
    }

    private static BlockState recalculateDistance(BlockState state, LevelReader level, BlockPos pos) {
        int bestDistance = ROYAL_DECAY_DISTANCE;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            cursor.setWithOffset(pos, direction);
            bestDistance = Math.min(bestDistance, getDistanceAt(level.getBlockState(cursor)) + 1);
            if (bestDistance == 1) {
                break;
            }
        }

        return setRoyalDistance(state, Math.min(ROYAL_DECAY_DISTANCE, bestDistance));
    }

    private static BlockState setRoyalDistance(BlockState state, int distance) {
        int royalDistance = Math.max(1, Math.min(ROYAL_DECAY_DISTANCE, distance));
        int vanillaDistance = Math.min(LeavesBlock.DECAY_DISTANCE, royalDistance);

        return state
                .setValue(ROYAL_DISTANCE, royalDistance)
                .setValue(LeavesBlock.DISTANCE, vanillaDistance);
    }

    private static int getDistanceAt(BlockState state) {
        if (state.is(BlockTags.LOGS)) {
            return 0;
        }
        if (state.hasProperty(ROYAL_DISTANCE)) {
            return state.getValue(ROYAL_DISTANCE);
        }
        if (state.hasProperty(LeavesBlock.DISTANCE)) {
            return state.getValue(LeavesBlock.DISTANCE);
        }

        return ROYAL_DECAY_DISTANCE;
    }

    public static BlockState setRoyalDistanceForWorldgen(BlockState state, int distance) {
        return setRoyalDistance(state, distance);
    }
}
