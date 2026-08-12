package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.Level;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public final class FallenKingCrownItem extends ArmorItem {

    public FallenKingCrownItem(ArmorMaterial material, Properties properties) {
        super(material, Type.HELMET, properties.stacksTo(1).durability(77));
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
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != Type.HELMET.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
                Attributes.ARMOR,
                new AttributeModifier(
                        UUID.nameUUIDFromBytes("fallen_king_crown_armor".getBytes()),
                        "fallen_king_crown_armor",
                        getDefense(),
                        AttributeModifier.Operation.ADDITION
                )
        );
        builder.put(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        UUID.nameUUIDFromBytes("fallen_king_crown_armor_toughness".getBytes()),
                        "fallen_king_crown_armor_toughness",
                        getToughness(),
                        AttributeModifier.Operation.ADDITION
                )
        );
        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.antarchy.fallen_king_crown.hero").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
