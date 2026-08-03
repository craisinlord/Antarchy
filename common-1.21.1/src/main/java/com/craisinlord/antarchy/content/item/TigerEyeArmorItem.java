package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.content.client.TigerEyeClientHooks;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class TigerEyeArmorItem extends ArmorItem {
    private static final int BASE_DURABILITY_MULTIPLIER = 33;

    public TigerEyeArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties.stacksTo(1).durability(resolveDurability(type)));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.passive").withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.full_set").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.activate", TigerEyeClientHooks.camouflageKeyText()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.antarchy.tigers_eye_armor.active_bonus").withStyle(ChatFormatting.DARK_AQUA));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    private static int resolveDurability(Type armorType) {
        return switch (armorType) {
            case HELMET -> Type.HELMET.getDurability(BASE_DURABILITY_MULTIPLIER);
            case CHESTPLATE, BODY -> Type.CHESTPLATE.getDurability(BASE_DURABILITY_MULTIPLIER);
            case LEGGINGS -> Type.LEGGINGS.getDurability(BASE_DURABILITY_MULTIPLIER);
            case BOOTS -> Type.BOOTS.getDurability(BASE_DURABILITY_MULTIPLIER);
        };
    }
}
