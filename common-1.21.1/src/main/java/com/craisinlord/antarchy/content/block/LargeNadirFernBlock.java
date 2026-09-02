package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LargeNadirFernBlock extends Block {
    public static final MapCodec<LargeNadirFernBlock> CODEC = simpleCodec(LargeNadirFernBlock::new);
    public static final DirectionProperty VERTICAL_DIRECTION = BlockStateProperties.VERTICAL_DIRECTION;
    public static final EnumProperty<Half> HALF = EnumProperty.create("half", Half.class);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public LargeNadirFernBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VERTICAL_DIRECTION, Direction.UP)
                .setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace() == Direction.DOWN ? Direction.DOWN : Direction.UP;
        BlockPos otherPos = context.getClickedPos().relative(direction);
        if (!context.getLevel().getBlockState(otherPos).canBeReplaced(context)) {
            return null;
        }
        return this.defaultBlockState().setValue(VERTICAL_DIRECTION, direction);
    }

    @Override
    public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        placeOtherHalf(level, pos, state, Block.UPDATE_ALL);
    }

    public static void placeAt(LevelAccessor level, BlockPos pos, BlockState bottomState, int flags) {
        level.setBlock(pos, bottomState.setValue(HALF, Half.BOTTOM), flags);
        placeOtherHalf(level, pos, bottomState, flags);
    }

    private static void placeOtherHalf(LevelAccessor level, BlockPos pos, BlockState state, int flags) {
        Direction direction = state.getValue(VERTICAL_DIRECTION);
        level.setBlock(pos.relative(direction), state.setValue(HALF, Half.TOP), flags);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(VERTICAL_DIRECTION);
        if (state.getValue(HALF) == Half.TOP) {
            BlockState otherState = level.getBlockState(pos.relative(direction.getOpposite()));
            return otherState.is(this)
                    && otherState.getValue(HALF) == Half.BOTTOM
                    && otherState.getValue(VERTICAL_DIRECTION) == direction;
        }

        BlockPos supportPos = pos.relative(direction.getOpposite());
        BlockState supportState = level.getBlockState(supportPos);
        BlockState topState = level.getBlockState(pos.relative(direction));
        boolean topMatches = topState.isAir()
                || topState.is(this)
                && topState.getValue(HALF) == Half.TOP
                && topState.getValue(VERTICAL_DIRECTION) == direction;
        return topMatches && supportState.isFaceSturdy(level, supportPos, direction);
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
        return state.canSurvive(level, pos)
                ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            Direction verticalDirection = state.getValue(VERTICAL_DIRECTION);
            BlockPos otherPos = state.getValue(HALF) == Half.BOTTOM
                    ? pos.relative(verticalDirection)
                    : pos.relative(verticalDirection.getOpposite());
            if (level.getBlockState(otherPos).is(this)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION, HALF);
    }

    public enum Half implements StringRepresentable {
        TOP("top"),
        BOTTOM("bottom");

        private final String name;

        Half(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
