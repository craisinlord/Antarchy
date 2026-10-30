package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.block.Fossils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class FossilBlockItem extends BlockItem {
    public FossilBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation id = Fossils.entityOf(stack);
        if (id != null) {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .<Component>map(type -> Component.translatable("item.antarchy.fossilized", type.getDescription()))
                    .orElseGet(() -> super.getName(stack));
        }
        return super.getName(stack);
    }
}
