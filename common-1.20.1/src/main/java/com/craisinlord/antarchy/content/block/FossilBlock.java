package com.craisinlord.antarchy.content.block;

import com.craisinlord.antarchy.content.block.entity.FossilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FossilBlock extends BaseEntityBlock {
    public FossilBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FossilBlockEntity(pos, state);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        ResourceLocation id = level.getBlockEntity(pos) instanceof FossilBlockEntity fossil ? fossil.entityId() : Fossils.entityAt(pos);
        if (id != null) {
            Fossils.setEntity(stack, id);
        }
        return stack;
    }
}
