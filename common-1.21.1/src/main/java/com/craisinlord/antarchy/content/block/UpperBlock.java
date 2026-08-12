package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.UpperBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class UpperBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", direction -> direction != Direction.DOWN);
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

    private static final VoxelShape TOP = Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    private static final VoxelShape MIDDLE = Block.box(4.0, 6.0, 4.0, 12.0, 12.0, 12.0);
    private static final VoxelShape BASE = Shapes.or(TOP, MIDDLE);
    private static final VoxelShape UP_SHAPE = Shapes.or(BASE, Block.box(6.0, 12.0, 6.0, 10.0, 16.0, 10.0));
    private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0, 8.0, 0.0, 10.0, 12.0, 4.0));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0, 8.0, 12.0, 10.0, 12.0, 16.0));
    private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0, 8.0, 6.0, 4.0, 12.0, 10.0));
    private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0, 8.0, 6.0, 16.0, 12.0, 10.0));

    private final Supplier<? extends BlockEntityType<UpperBlockEntity>> blockEntityTypeSupplier;
    private final MapCodec<UpperBlock> codec;

    public UpperBlock(Supplier<? extends BlockEntityType<UpperBlockEntity>> blockEntityTypeSupplier, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.codec = Block.simpleCodec(ignored -> this);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(ENABLED, true));
    }

    @Override
    public MapCodec<UpperBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ENABLED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction opposite = context.getClickedFace().getOpposite();
        Direction facing = opposite == Direction.DOWN ? Direction.UP : opposite;
        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        this.updateEnabled(level, pos, state);
    }

    private void updateEnabled(Level level, BlockPos pos, BlockState state) {
        boolean shouldBeEnabled = !level.hasNeighborSignal(pos);
        if (shouldBeEnabled != state.getValue(ENABLED)) {
            level.setBlock(pos, state.setValue(ENABLED, shouldBeEnabled), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> UP_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UpperBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), (tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                UpperBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
            }
        });
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MenuProvider menuProvider) {
            player.openMenu(menuProvider);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof UpperBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
