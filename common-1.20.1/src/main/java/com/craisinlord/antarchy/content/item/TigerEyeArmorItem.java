package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.client.TigerEyeClientHooks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public final class TigerEyeArmorItem extends ArmorItem {
    private static final int BASE_DURABILITY_MULTIPLIER = 33;

    public TigerEyeArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties.stacksTo(1).durability(resolveDurability(type)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.passive").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.full_set").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.activate", TigerEyeClientHooks.camouflageKeyText()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.active_bonus").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private static int resolveDurability(Type armorType) {
        return switch (armorType) {
            case HELMET -> 11 * BASE_DURABILITY_MULTIPLIER;
            case CHESTPLATE -> 16 * BASE_DURABILITY_MULTIPLIER;
            case LEGGINGS -> 15 * BASE_DURABILITY_MULTIPLIER;
            case BOOTS -> 13 * BASE_DURABILITY_MULTIPLIER;
        };
    }
}
