package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class QueenTrailSpawnMarkerBlock extends BaseEntityBlock {
    private final Supplier<? extends BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> blockEntityTypeSupplier;
    private final MapCodec<QueenTrailSpawnMarkerBlock> codec;

    public QueenTrailSpawnMarkerBlock(Supplier<? extends BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> blockEntityTypeSupplier, BlockBehaviour.Properties properties) {
        super(properties);
        this.blockEntityTypeSupplier = blockEntityTypeSupplier;
        this.codec = Block.simpleCodec(ignored -> this);
    }

    @Override
    public MapCodec<QueenTrailSpawnMarkerBlock> codec() {
        return this.codec;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QueenTrailSpawnMarkerBlockEntity(pos, state, this.blockEntityTypeSupplier);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.blockEntityTypeSupplier.get(), (tickLevel, tickPos, tickState, marker) -> {
            if (tickLevel instanceof ServerLevel serverLevel) {
                QueenTrailSpawnMarkerBlockEntity.serverTick(serverLevel, marker);
            }
        });
    }
}
