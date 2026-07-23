package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyTags;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BluestoneWireBlock extends Block implements BluestoneSignalSource {
    public static final MapCodec<BluestoneWireBlock> CODEC = simpleCodec(BluestoneWireBlock::new);
    public static final EnumProperty<BluestoneSide> NORTH = EnumProperty.create("north", BluestoneSide.class);
    public static final EnumProperty<BluestoneSide> EAST = EnumProperty.create("east", BluestoneSide.class);
    public static final EnumProperty<BluestoneSide> SOUTH = EnumProperty.create("south", BluestoneSide.class);
    public static final EnumProperty<BluestoneSide> WEST = EnumProperty.create("west", BluestoneSide.class);
    public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 15);
    public static final Map<Direction, EnumProperty<BluestoneSide>> PROPERTY_BY_DIRECTION = ImmutableMap.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST
    );
    private static final VoxelShape DOT_SHAPE = Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape NORTH_SHAPE = Block.box(3.0D, 14.0D, 0.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(3.0D, 14.0D, 3.0D, 13.0D, 16.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 14.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    private static final VoxelShape EAST_SHAPE = Block.box(3.0D, 14.0D, 3.0D, 16.0D, 16.0D, 13.0D);
    private static boolean shouldSignal = true;

    public BluestoneWireBlock() {
        this(BlockBehaviour.Properties.of().noCollission().instabreak().sound(SoundType.WOOL));
    }

    public BluestoneWireBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(NORTH, BluestoneSide.NONE)
                .setValue(EAST, BluestoneSide.NONE)
                .setValue(SOUTH, BluestoneSide.NONE)
                .setValue(WEST, BluestoneSide.NONE)
                .setValue(POWER, 0));
    }

    public MapCodec<BluestoneWireBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BluestonePlacementHelper.hasCeilingSupport(level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!BluestonePlacementHelper.canPlaceOnCeiling(context)) {
            return null;
        }
        return this.updatePowerFromNeighbors(context.getLevel(), context.getClickedPos(), this.calculateShape(context.getLevel(), context.getClickedPos(), this.defaultBlockState()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = DOT_SHAPE;
        if (state.getValue(NORTH) != BluestoneSide.NONE) {
            shape = Shapes.or(shape, NORTH_SHAPE);
        }
        if (state.getValue(EAST) != BluestoneSide.NONE) {
            shape = Shapes.or(shape, EAST_SHAPE);
        }
        if (state.getValue(SOUTH) != BluestoneSide.NONE) {
            shape = Shapes.or(shape, SOUTH_SHAPE);
        }
        if (state.getValue(WEST) != BluestoneSide.NONE) {
            shape = Shapes.or(shape, WEST_SHAPE);
        }
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        if (direction.getAxis().isHorizontal() || direction == Direction.UP || direction == Direction.DOWN) {
            return this.calculateShape(level, pos, state);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!oldState.is(state.getBlock())) {
            this.updateNeighbors(level, pos, state);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighborState = level.getBlockState(neighborPos);
                if (neighborState.is(this)) {
                    this.updateNeighbors(level, neighborPos, neighborState);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide) {
            this.updateNeighbors(level, pos, state);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return shouldSignal;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!shouldSignal || direction == Direction.UP) {
            return 0;
        }
        if (direction.getAxis().isHorizontal()) {
            EnumProperty<BluestoneSide> property = PROPERTY_BY_DIRECTION.get(direction);
            if (property != null && state.getValue(property) == BluestoneSide.NONE) {
                return 0;
            }
        }
        return state.getValue(POWER);
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        boolean isDot = state.getValue(NORTH) == BluestoneSide.NONE
                && state.getValue(EAST) == BluestoneSide.NONE
                && state.getValue(SOUTH) == BluestoneSide.NONE
                && state.getValue(WEST) == BluestoneSide.NONE;
        BlockState updated = isDot
                ? this.calculateShape(level, pos, state)
                : state.setValue(NORTH, BluestoneSide.NONE)
                        .setValue(EAST, BluestoneSide.NONE)
                        .setValue(SOUTH, BluestoneSide.NONE)
                        .setValue(WEST, BluestoneSide.NONE);
        level.setBlock(pos, updated, 3);
        this.updateNeighbors(level, pos, updated);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_180 -> state.setValue(NORTH, state.getValue(SOUTH))
                    .setValue(EAST, state.getValue(WEST))
                    .setValue(SOUTH, state.getValue(NORTH))
                    .setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> state.setValue(NORTH, state.getValue(EAST))
                    .setValue(EAST, state.getValue(SOUTH))
                    .setValue(SOUTH, state.getValue(WEST))
                    .setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90 -> state.setValue(NORTH, state.getValue(WEST))
                    .setValue(EAST, state.getValue(NORTH))
                    .setValue(SOUTH, state.getValue(EAST))
                    .setValue(WEST, state.getValue(SOUTH));
            default -> state;
        };
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK -> state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER);
    }

    @Override
    public int getBluestoneSignal(LevelReader level, BlockPos pos, BlockState state, Direction direction) {
        return direction == Direction.UP ? 0 : state.getValue(POWER);
    }

    private void updateNeighbors(Level level, BlockPos pos, BlockState state) {
        if (!state.is(this)) {
            return;
        }
        BlockState shaped = this.calculateShape(level, pos, state);
        BlockState powered = this.updatePowerFromNeighbors(level, pos, shaped);
        if (!powered.equals(state)) {
            level.setBlock(pos, powered, 3);
        }
        BluestoneSignalHelper.updateBluestoneNeighbors(level, pos);
    }

    private BlockState updatePowerFromNeighbors(Level level, BlockPos pos, BlockState state) {
        int newPower = this.calculateTargetStrength(level, pos);
        return state.getValue(POWER) == newPower ? state : state.setValue(POWER, newPower);
    }

    private int calculateTargetStrength(Level level, BlockPos pos) {
        int external = this.getExternalSignal(level, pos);
        int neighborWirePower = 0;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = pos.relative(direction);
            neighborWirePower = Math.max(neighborWirePower, BluestoneSignalHelper.getWireSignal(level, sidePos));
            neighborWirePower = Math.max(neighborWirePower, BluestoneSignalHelper.getWireSignal(level, sidePos.below()));
            if (level.getBlockState(sidePos).isRedstoneConductor(level, sidePos)) {
                neighborWirePower = Math.max(neighborWirePower, BluestoneSignalHelper.getWireSignal(level, sidePos.above()));
            }
        }

        return Math.max(external, Math.max(0, neighborWirePower - 1));
    }

    private int getExternalSignal(Level level, BlockPos pos) {
        int signal = 0;
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.is(net.minecraft.world.level.block.Blocks.REDSTONE_WIRE)) {
                continue;
            }
            if (neighborState.getBlock() instanceof BluestoneWireBlock) {
                continue;
            }
            signal = Math.max(signal, neighborState.getSignal(level, neighborPos, direction));
            if (signal >= 15) {
                return 15;
            }
        }
        return signal;
    }

    private BlockState calculateShape(LevelReader level, BlockPos pos, BlockState state) {
        return state.setValue(NORTH, this.getConnectingSide(level, pos, Direction.NORTH))
                .setValue(EAST, this.getConnectingSide(level, pos, Direction.EAST))
                .setValue(SOUTH, this.getConnectingSide(level, pos, Direction.SOUTH))
                .setValue(WEST, this.getConnectingSide(level, pos, Direction.WEST));
    }

    private BluestoneSide getConnectingSide(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos sidePos = pos.relative(direction);
        BlockState sideState = level.getBlockState(sidePos);
        if (this.canConnectTo(sideState, direction)) {
            return BluestoneSide.SIDE;
        }

        if (!sideState.isRedstoneConductor(level, sidePos) && this.canConnectTo(level.getBlockState(sidePos.below()), direction)) {
            return BluestoneSide.DOWN;
        }

        if (sideState.isRedstoneConductor(level, sidePos) && this.canConnectTo(level.getBlockState(sidePos.above()), direction)) {
            return BluestoneSide.SIDE;
        }

        return BluestoneSide.NONE;
    }

    private boolean canConnectTo(BlockState state, Direction direction) {
        if (state.getBlock() instanceof BluestoneWireBlock || state.getBlock() instanceof BluestoneTorchBlock || state.getBlock() instanceof BluestoneBlock) {
            return true;
        }
        if (state.getBlock() instanceof BluestoneRepeaterBlock || state.getBlock() instanceof BluestoneComparatorBlock) {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return facing == direction || facing == direction.getOpposite();
        }
        return state.is(AntarchyTags.Blocks.BLUESTONE_CONNECTABLE);
    }
}
