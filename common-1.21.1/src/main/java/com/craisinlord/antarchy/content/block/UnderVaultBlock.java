package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.UnderVaultBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Supplier;

public final class UnderVaultBlock extends VaultBlock {
    public static final DirectionProperty UNDER_FACING = DirectionProperty.create("under_facing", Direction.values());
    private final Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> blockEntityType;

    public UnderVaultBlock(Supplier<? extends BlockEntityType<UnderVaultBlockEntity>> blockEntityType, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityType = blockEntityType;
        this.registerDefaultState(this.defaultBlockState().setValue(UNDER_FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UNDER_FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.DOWN) {
            return null;
        }
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(UNDER_FACING, Direction.DOWN);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return super.rotate(state, rotation).setValue(UNDER_FACING, rotation.rotate(state.getValue(UNDER_FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror).setValue(UNDER_FACING, mirror.getRotation(state.getValue(UNDER_FACING)).rotate(state.getValue(UNDER_FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UnderVaultBlockEntity(pos, state, blockEntityType);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (this.blockEntityType == null || type != this.blockEntityType.get()) {
            return null;
        }
        if (level.isClientSide) {
            return (BlockEntityTicker<T>) (BlockEntityTicker<UnderVaultBlockEntity>) UnderVaultBlockEntity::clientTick;
        }
        return (BlockEntityTicker<T>) (BlockEntityTicker<UnderVaultBlockEntity>) ((serverLevel, pos, blockState, blockEntity) -> {
            if (serverLevel instanceof ServerLevel levelServer) {
                UnderVaultBlockEntity.serverTick(levelServer, pos, blockState, blockEntity);
            }
        });
    }
}
