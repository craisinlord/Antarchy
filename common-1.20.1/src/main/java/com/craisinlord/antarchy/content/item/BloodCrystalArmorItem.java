package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.Level;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
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

public class BloodCrystalArmorItem extends ArmorItem {
    private final Type armorType;

    public BloodCrystalArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties.stacksTo(1).durability(resolveDurability(type)));
        this.armorType = type;
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.bloodCrystalHelmetDefense();
            case CHESTPLATE -> AntarchySettings.bloodCrystalChestplateDefense();
            case LEGGINGS -> AntarchySettings.bloodCrystalLeggingsDefense();
            case BOOTS -> AntarchySettings.bloodCrystalBootsDefense();
        };
    }

    @Override
    public float getToughness() {
        return (float) AntarchySettings.bloodCrystalArmorToughness();
    }

    private static int resolveDurability(Type type) {
        return switch (type) {
            case HELMET -> AntarchySettings.bloodCrystalHelmetDurability();
            case CHESTPLATE -> AntarchySettings.bloodCrystalChestplateDurability();
            case LEGGINGS -> AntarchySettings.bloodCrystalLeggingsDurability();
            case BOOTS -> AntarchySettings.bloodCrystalBootsDurability();
        };
    }

    @Override
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.armorType.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }

        String prefix = "blood_crystal_" + this.armorType.getName();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(
                Attributes.ARMOR,
                new AttributeModifier(
                        UUID.nameUUIDFromBytes((prefix + "_armor").getBytes()),
                        prefix + "_armor",
                        getDefense(),
                        AttributeModifier.Operation.ADDITION
                )
        );
        builder.put(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(
                        UUID.nameUUIDFromBytes((prefix + "_armor_toughness").getBytes()),
                        prefix + "_armor_toughness",
                        getToughness(),
                        AttributeModifier.Operation.ADDITION
                )
        );
        builder.put(
                AntarchyObjects.BLOODGLASS_MAX_HEARTS.get().value(),
                new AttributeModifier(
                        UUID.nameUUIDFromBytes((prefix + "_bloodglass_max").getBytes()),
                        prefix + "_bloodglass_max",
                        1.0,
                        AttributeModifier.Operation.ADDITION
                )
        );
        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.antarchy.blood_crystal_armor.tooltip").withStyle(ChatFormatting.RED));
        if (this.armorType == Type.BOOTS) {
            tooltipComponents.add(Component.translatable("item.antarchy.blood_crystal_boots.tooltip2").withStyle(ChatFormatting.DARK_RED));
        }
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
