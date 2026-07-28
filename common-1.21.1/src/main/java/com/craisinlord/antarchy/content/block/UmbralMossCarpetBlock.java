package com.craisinlord.antarchy.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class UmbralMossCarpetBlock extends Block {
    public static final MapCodec<UmbralMossCarpetBlock> CODEC = Block.simpleCodec(UmbralMossCarpetBlock::new);
    public static final BooleanProperty BOTTOM = BlockStateProperties.BOTTOM;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<WallSide> NORTH_WALL = BlockStateProperties.NORTH_WALL;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<WallSide> EAST_WALL = BlockStateProperties.EAST_WALL;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<WallSide> SOUTH_WALL = BlockStateProperties.SOUTH_WALL;
    public static final net.minecraft.world.level.block.state.properties.EnumProperty<WallSide> WEST_WALL = BlockStateProperties.WEST_WALL;

    private static final VoxelShape FLOOR_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
    private static final VoxelShape NORTH_LOW = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 1.0D);
    private static final VoxelShape NORTH_TALL = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
    private static final VoxelShape EAST_LOW = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D);
    private static final VoxelShape EAST_TALL = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SOUTH_LOW = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 6.0D, 16.0D);
    private static final VoxelShape SOUTH_TALL = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_LOW = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 6.0D, 16.0D);
    private static final VoxelShape WEST_TALL = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);

    public UmbralMossCarpetBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BOTTOM, true)
                .setValue(NORTH_WALL, WallSide.NONE)
                .setValue(EAST_WALL, WallSide.NONE)
                .setValue(SOUTH_WALL, WallSide.NONE)
                .setValue(WEST_WALL, WallSide.NONE));
    }

    @Override
    public MapCodec<UmbralMossCarpetBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BOTTOM, NORTH_WALL, EAST_WALL, SOUTH_WALL, WEST_WALL);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState state = this.defaultBlockState()
                .setValue(BOTTOM, face == Direction.UP)
                .setValue(NORTH_WALL, WallSide.NONE)
                .setValue(EAST_WALL, WallSide.NONE)
                .setValue(SOUTH_WALL, WallSide.NONE)
                .setValue(WEST_WALL, WallSide.NONE);
        if (!state.getValue(BOTTOM)) {
            state = switch (face) {
                case NORTH -> state.setValue(NORTH_WALL, WallSide.TALL);
                case EAST -> state.setValue(EAST_WALL, WallSide.TALL);
                case SOUTH -> state.setValue(SOUTH_WALL, WallSide.TALL);
                case WEST -> state.setValue(WEST_WALL, WallSide.TALL);
                default -> state;
            };
        }
        return state;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(BOTTOM)) {
            return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        }

        if (supportsWall(level, pos, Direction.NORTH, state.getValue(NORTH_WALL))) {
            return true;
        }
        if (supportsWall(level, pos, Direction.EAST, state.getValue(EAST_WALL))) {
            return true;
        }
        if (supportsWall(level, pos, Direction.SOUTH, state.getValue(SOUTH_WALL))) {
            return true;
        }
        if (supportsWall(level, pos, Direction.WEST, state.getValue(WEST_WALL))) {
            return true;
        }

        return level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? state : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(BOTTOM)) {
            return FLOOR_SHAPE;
        }

        VoxelShape shape = Shapes.empty();
        shape = combine(shape, state.getValue(NORTH_WALL), NORTH_LOW, NORTH_TALL);
        shape = combine(shape, state.getValue(EAST_WALL), EAST_LOW, EAST_TALL);
        shape = combine(shape, state.getValue(SOUTH_WALL), SOUTH_LOW, SOUTH_TALL);
        shape = combine(shape, state.getValue(WEST_WALL), WEST_LOW, WEST_TALL);
        return shape.isEmpty() ? FLOOR_SHAPE : shape;
    }

    private static boolean supportsWall(LevelReader level, BlockPos pos, Direction direction, WallSide side) {
        if (side == WallSide.NONE) {
            return false;
        }
        BlockPos supportPos = pos.relative(direction);
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, direction.getOpposite());
    }

    private static VoxelShape combine(VoxelShape current, WallSide side, VoxelShape lowShape, VoxelShape tallShape) {
        if (side == WallSide.NONE) {
            return current;
        }
        return Shapes.or(current, side == WallSide.TALL ? tallShape : lowShape);
    }
}
