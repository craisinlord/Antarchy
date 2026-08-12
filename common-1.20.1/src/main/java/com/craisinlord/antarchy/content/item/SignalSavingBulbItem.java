package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public final class SignalSavingBulbItem extends BlockItem {
    public SignalSavingBulbItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.signal_saving_bulb").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
