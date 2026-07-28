package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DreamCampfireBlock extends CampfireBlock {
    @SuppressWarnings("rawtypes")
    public static final MapCodec<CampfireBlock> CODEC = (MapCodec) simpleCodec(DreamCampfireBlock::new);

    public DreamCampfireBlock(BlockBehaviour.Properties properties) {
        super(false, 2, properties);
    }

    @Override
    public MapCodec<CampfireBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        DreamCampfireBlockEntity.beginConstruction();
        try {
            return new DreamCampfireBlockEntity(pos, state);
        } finally {
            DreamCampfireBlockEntity.endConstruction();
        }
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        BlockEntityType<DreamCampfireBlockEntity> dreamCampfireType = AntarchyObjects.DREAM_CAMPFIRE_BLOCK_ENTITY.get();
        if (level.isClientSide) {
            return state.getValue(LIT)
                    ? createTickerHelper(blockEntityType, dreamCampfireType, CampfireBlockEntity::particleTick)
                    : null;
        }
        return state.getValue(LIT)
                ? createTickerHelper(blockEntityType, dreamCampfireType, CampfireBlockEntity::cookTick)
                : createTickerHelper(blockEntityType, dreamCampfireType, CampfireBlockEntity::cooldownTick);
    }
}
