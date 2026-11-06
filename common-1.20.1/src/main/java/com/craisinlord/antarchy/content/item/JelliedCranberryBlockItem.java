package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class JelliedCranberryBlockItem extends BlockItem {
    public JelliedCranberryBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.jellied_cranberry_block.cling").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.jellied_cranberry_block.servings").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
