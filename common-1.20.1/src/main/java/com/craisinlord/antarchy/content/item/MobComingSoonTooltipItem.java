package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class MobComingSoonTooltipItem extends Item {
    public MobComingSoonTooltipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.mob_coming_soon").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
