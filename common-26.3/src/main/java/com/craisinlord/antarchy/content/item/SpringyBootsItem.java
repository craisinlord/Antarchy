package com.craisinlord.antarchy.content.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public final class SpringyBootsItem extends Item {
    public SpringyBootsItem(ArmorMaterial material, Item.Properties properties) {
        super(properties.humanoidArmor(material, ArmorType.BOOTS));
    }

    public static boolean isWearingSpringyBoots(net.minecraft.world.entity.LivingEntity entity) {
        return entity != null && entity.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET).getItem() instanceof SpringyBootsItem;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.springy_boots").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
