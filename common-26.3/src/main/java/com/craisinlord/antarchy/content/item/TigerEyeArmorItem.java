package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.client.TigerEyeClientHooks;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public final class TigerEyeArmorItem extends Item {
    private static final int BASE_DURABILITY_MULTIPLIER = 33;

    public TigerEyeArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(properties.humanoidArmor(material, type).stacksTo(1).durability(resolveDurability(type)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("item.antarchy.tigers_eye_armor.passive").withStyle(ChatFormatting.GOLD));
        tooltipComponents.accept(Component.translatable("item.antarchy.tigers_eye_armor.full_set").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.accept(Component.translatable("item.antarchy.tigers_eye_armor.activate", TigerEyeClientHooks.camouflageKeyText()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("item.antarchy.tigers_eye_armor.active_bonus").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    private static int resolveDurability(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> ArmorType.HELMET.getDurability(BASE_DURABILITY_MULTIPLIER);
            case CHESTPLATE, BODY -> ArmorType.CHESTPLATE.getDurability(BASE_DURABILITY_MULTIPLIER);
            case LEGGINGS -> ArmorType.LEGGINGS.getDurability(BASE_DURABILITY_MULTIPLIER);
            case BOOTS -> ArmorType.BOOTS.getDurability(BASE_DURABILITY_MULTIPLIER);
        };
    }
}
