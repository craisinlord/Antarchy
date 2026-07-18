package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingCreeprootsBlock extends BushBlock {
    public static final MapCodec<HangingCreeprootsBlock> CODEC = Block.simpleCodec(HangingCreeprootsBlock::new);
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 4);
    public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");
    private static final int MAX_DISTANCE = 4;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public HangingCreeprootsBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, 0).setValue(BOTTOM, true));
    }

    @Override
    public MapCodec<HangingCreeprootsBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(this) || state.isFaceSturdy(level, pos, Direction.DOWN);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        return this.mayPlaceOn(level.getBlockState(abovePos), level, abovePos);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.DOWN) {
            boolean bottom = !neighborState.is(this);
            if (bottom != state.getValue(BOTTOM)) {
                state = state.setValue(BOTTOM, bottom);
            }
        }
        return state;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(BOTTOM) && state.getValue(DISTANCE) < MAX_DISTANCE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(BOTTOM) || state.getValue(DISTANCE) >= MAX_DISTANCE) {
            return;
        }

        BlockPos belowPos = pos.below();
        if (random.nextInt(4) != 0 || !level.getBlockState(belowPos).isAir()) {
            return;
        }

        int distance = state.getValue(DISTANCE);
        level.setBlock(pos, state.setValue(BOTTOM, false), Block.UPDATE_CLIENTS);
        level.setBlock(belowPos, this.defaultBlockState().setValue(DISTANCE, distance + 1).setValue(BOTTOM, true), Block.UPDATE_ALL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, BOTTOM);
    }
}
