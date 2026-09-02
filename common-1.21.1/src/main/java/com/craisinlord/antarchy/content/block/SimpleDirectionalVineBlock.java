package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SimpleDirectionalVineBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<SimpleDirectionalVineBlock> CODEC = simpleCodec(SimpleDirectionalVineBlock::new);
    public static final EnumProperty<Direction> GROWTH_DIRECTION = EnumProperty.create("growth_direction", Direction.class, Direction.UP, Direction.DOWN);
    public static final BooleanProperty TOP_CAP = BooleanProperty.create("top_cap");
    public static final BooleanProperty BOTTOM_CAP = BooleanProperty.create("bottom_cap");
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 6);
    protected static final int MAX_DISTANCE = 6;
    protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public SimpleDirectionalVineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(GROWTH_DIRECTION, Direction.DOWN)
                .setValue(TOP_CAP, false)
                .setValue(BOTTOM_CAP, true)
                .setValue(DISTANCE, 0));
    }

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction growthDirection = resolveGrowthDirection(level, pos);
        return computeState(this.defaultBlockState().setValue(GROWTH_DIRECTION, growthDirection).setValue(DISTANCE, 0), level, pos);
    }

    private Direction resolveGrowthDirection(BlockGetter level, BlockPos pos) {
        BlockState belowState = level.getBlockState(pos.below());
        if (belowState.is(this) && belowState.getValue(GROWTH_DIRECTION) == Direction.UP) {
            return Direction.UP;
        }

        BlockState aboveState = level.getBlockState(pos.above());
        if (aboveState.is(this) && aboveState.getValue(GROWTH_DIRECTION) == Direction.DOWN) {
            return Direction.DOWN;
        }

        return hasSturdyAnchor(level, pos, Direction.UP) ? Direction.DOWN : Direction.UP;
    }

    private static boolean hasSturdyAnchor(BlockGetter level, BlockPos pos, Direction towardAnchor) {
        BlockPos anchorPos = pos.relative(towardAnchor);
        BlockState anchorState = level.getBlockState(anchorPos);
        return anchorState.isFaceSturdy(level, anchorPos, towardAnchor.getOpposite());
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(this) || state.isFaceSturdy(level, pos, Direction.DOWN) || state.isFaceSturdy(level, pos, Direction.UP);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction anchorDirection = state.getValue(GROWTH_DIRECTION).getOpposite();
        BlockPos anchorPos = pos.relative(anchorDirection);
        BlockState anchorState = level.getBlockState(anchorPos);
        if (anchorState.is(this)) {
            return true;
        }
        return anchorState.isFaceSturdy(level, anchorPos, anchorDirection.getOpposite());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return computeState(state, level, pos);
        }
        return state;
    }

    protected static BlockState computeState(BlockState state, BlockGetter level, BlockPos pos) {
        boolean aboveIsSame = level.getBlockState(pos.above()).is(state.getBlock());
        boolean belowIsSame = level.getBlockState(pos.below()).is(state.getBlock());
        Direction growthDirection = state.getValue(GROWTH_DIRECTION);

        boolean topCap = growthDirection == Direction.UP && !aboveIsSame;
        boolean bottomCap = growthDirection == Direction.DOWN && !belowIsSame;
        return state.setValue(TOP_CAP, topCap).setValue(BOTTOM_CAP, bottomCap);
    }

    protected int maxDistance() {
        return MAX_DISTANCE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return isTip(state) && state.getValue(DISTANCE) < maxDistance();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            grow(level, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return isTip(state) && state.getValue(DISTANCE) < maxDistance() && level.getBlockState(pos.relative(state.getValue(GROWTH_DIRECTION))).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos, state);
    }

    private void grow(ServerLevel level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(GROWTH_DIRECTION);
        BlockPos targetPos = pos.relative(direction);
        if (!level.getBlockState(targetPos).isAir()) {
            return;
        }

        BlockState grownState = this.defaultBlockState()
                .setValue(GROWTH_DIRECTION, direction)
                .setValue(DISTANCE, state.getValue(DISTANCE) + 1);
        level.setBlock(targetPos, grownState, Block.UPDATE_ALL);

        BlockState posState = level.getBlockState(pos);
        if (posState.is(this)) {
            level.setBlock(pos, computeState(posState, level, pos), Block.UPDATE_CLIENTS);
        }
        BlockState targetState = level.getBlockState(targetPos);
        if (targetState.is(this)) {
            level.setBlock(targetPos, computeState(targetState, level, targetPos), Block.UPDATE_CLIENTS);
        }
    }

    protected static boolean isTip(BlockState state) {
        return state.getValue(GROWTH_DIRECTION) == Direction.DOWN ? state.getValue(BOTTOM_CAP) : state.getValue(TOP_CAP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GROWTH_DIRECTION, TOP_CAP, BOTTOM_CAP, DISTANCE);
    }
}
