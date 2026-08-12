package com.craisinlord.antarchy.content.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class MucusBlockItem extends BlockItem {
    public MucusBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
//        tooltipComponents.add(Component.translatable("tooltip.antarchy.mucus_slippery").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
