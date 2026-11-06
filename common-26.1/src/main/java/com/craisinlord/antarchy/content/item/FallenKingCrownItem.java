package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public final class FallenKingCrownItem extends Item {

    public FallenKingCrownItem(ArmorMaterial material, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.humanoidArmor(material, ArmorType.HELMET).stacksTo(1).durability(ArmorType.HELMET.getDurability(7)),
                FallenKingCrownItem::buildAttributes
        ));
    }

    public int getDefense() {
        return AntarchySettings.fallenKingCrownArmorValue();
    }

    public float getToughness() {
        return (float) AntarchySettings.fallenKingCrownArmorToughness();
    }

    private static ItemAttributeModifiers buildAttributes() {
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace("fallen_king_crown_armor"),
                                AntarchySettings.fallenKingCrownArmorValue(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(ArmorType.HELMET.getSlot())
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace("fallen_king_crown_armor_toughness"),
                                (float) AntarchySettings.fallenKingCrownArmorToughness(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(ArmorType.HELMET.getSlot())
                )
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.fallen_king_crown.hero").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
