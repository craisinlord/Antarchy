package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.DimensionalTearMarkerBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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

public class DimensionalTearMarkerBlock extends BaseEntityBlock {
    private static final VoxelShape EMPTY = Shapes.empty();
    private final Supplier<? extends BlockEntityType<DimensionalTearMarkerBlockEntity>> blockEntityTypeSupplier;
    private final MapCodec<DimensionalTearMarkerBlock> codec;

    public DimensionalTearMarkerBlock(Supplier<? extends BlockEntityType<DimensionalTearMarkerBlockEntity>> blockEntityTypeSupplier, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.codec = Block.simpleCodec(ignored -> this);
    }

    @Override
    public MapCodec<DimensionalTearMarkerBlock> codec() {
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
        return new DimensionalTearMarkerBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), (tickLevel, tickPos, tickState, blockEntity) -> {
            if (tickLevel instanceof ServerLevel serverLevel) {
                DimensionalTearMarkerBlockEntity.serverTick(serverLevel, tickPos, tickState, blockEntity);
            }
        });
    }
}
