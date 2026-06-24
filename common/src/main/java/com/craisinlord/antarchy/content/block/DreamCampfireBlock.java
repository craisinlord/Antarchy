package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.block.entity.DreamCampfireBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack stack) {
        var silkTouch = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        boolean hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, stack) > 0;
        ItemStack droppedCampfire = hasSilkTouch ? this.getCloneItemStack(level, pos, state) : ItemStack.EMPTY;

        super.playerDestroy(level, player, pos, state, blockEntity, stack);

        if (hasSilkTouch && !droppedCampfire.isEmpty() && level instanceof ServerLevel serverLevel) {
            Block.popResource(serverLevel, pos, droppedCampfire);
        }
    }
}
