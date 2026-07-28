package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class FallenKingCrownItem extends ArmorItem {

    public FallenKingCrownItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, Type.HELMET, properties.stacksTo(1).durability(Type.HELMET.getDurability(7)));
    }

    @Override
    public int getDefense() {
        return AntarchySettings.fallenKingCrownArmorValue();
    }

    @Override
    public float getToughness() {
        return (float) AntarchySettings.fallenKingCrownArmorToughness();
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("fallen_king_crown_armor"),
                                getDefense(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(Type.HELMET.getSlot())
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace("fallen_king_crown_armor_toughness"),
                                getToughness(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(Type.HELMET.getSlot())
                )
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.fallen_king_crown.hero").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
