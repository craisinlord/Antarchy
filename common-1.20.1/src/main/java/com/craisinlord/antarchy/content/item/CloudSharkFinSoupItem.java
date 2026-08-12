package com.craisinlord.antarchy.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class CloudSharkFinSoupItem extends Item {
    public CloudSharkFinSoupItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.cloud_shark_fin_soup").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
