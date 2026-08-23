package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.PortalGunPortalBaseBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PortalGunPortalBaseBlock extends BaseEntityBlock {
    private static final VoxelShape EMPTY = Shapes.empty();
    private final Supplier<? extends BlockEntityType<PortalGunPortalBaseBlockEntity>> blockEntityTypeSupplier;
    private final MapCodec<PortalGunPortalBaseBlock> codec;

    public PortalGunPortalBaseBlock(Supplier<? extends BlockEntityType<PortalGunPortalBaseBlockEntity>> blockEntityTypeSupplier, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.codec = Block.simpleCodec(ignored -> this);
    }

    @Override
    public MapCodec<PortalGunPortalBaseBlock> codec() {
        return this.codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EMPTY;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return EMPTY;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortalGunPortalBaseBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), (tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                PortalGunPortalBaseBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
            }
        });
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PortalGunPortalBaseBlockEntity blockEntity) {
            blockEntity.onBroken();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
