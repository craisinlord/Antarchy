package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class SpringyBootsItem extends ArmorItem {
    public SpringyBootsItem(ArmorMaterial material, Properties properties) {
        super(material, Type.BOOTS, properties);
    }

    public static boolean isWearingSpringyBoots(LivingEntity entity) {
        return entity != null && entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof SpringyBootsItem;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.springy_boots").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
