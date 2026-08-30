package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.VortexLensBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.Nullable;

public class VortexLensBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty PUSHING = BooleanProperty.create("pushing");

    private final Supplier<? extends BlockEntityType<VortexLensBlockEntity>> blockEntityTypeSupplier;
    private final MapCodec<VortexLensBlock> codec;

    public VortexLensBlock(Supplier<? extends BlockEntityType<VortexLensBlockEntity>> blockEntityTypeSupplier,
            BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.codec = Block.simpleCodec(ignored -> this);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP).setValue(POWERED, false).setValue(PUSHING, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return this.codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, PUSHING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        boolean bluestone = BluestoneSignalHelper.getBestNeighborSignal(level, pos) > 0;
        return this.defaultBlockState()
                .setValue(FACING, context.getClickedFace())
                .setValue(POWERED, hasAnySignal(level, pos))
                .setValue(PUSHING, bluestone);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof VortexLensBlockEntity lens) {
            lens.refreshImmediately();
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VortexLensBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), (tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                VortexLensBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
            }
        });
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof VortexLensBlockEntity lens) {
            lens.removeActiveVortex();
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

    private static boolean hasAnySignal(Level level, BlockPos pos) {
        return level.getBestNeighborSignal(pos) > 0 || BluestoneSignalHelper.getBestNeighborSignal(level, pos) > 0;
    }
}
