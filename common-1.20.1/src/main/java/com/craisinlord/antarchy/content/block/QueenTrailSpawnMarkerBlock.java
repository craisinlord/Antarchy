package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.QueenTrailSpawnMarkerBlockEntity;
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

/** Invisible worldgen marker which activates the Queen at a generated trail terminus. */
public final class QueenTrailSpawnMarkerBlock extends BaseEntityBlock {
    private final Supplier<? extends BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> type;

    public QueenTrailSpawnMarkerBlock(Supplier<? extends BlockEntityType<QueenTrailSpawnMarkerBlockEntity>> type,
                                      BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QueenTrailSpawnMarkerBlockEntity(pos, state, type);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;
        return createTickerHelper(blockEntityType, type.get(), (tickLevel, tickPos, tickState, marker) -> {
            if (tickLevel instanceof ServerLevel serverLevel) QueenTrailSpawnMarkerBlockEntity.serverTick(serverLevel, marker);
        });
    }
}
