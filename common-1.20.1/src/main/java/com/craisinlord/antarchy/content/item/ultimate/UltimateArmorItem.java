package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.google.common.collect.ImmutableMultimap;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UltimateArmorItem extends ArmorItem {
    private final Type armorType;

    public UltimateArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        this.armorType = type;
    }

    @Override
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.armorType.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }

        String prefix = this.armorType.getName();
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

        float knockbackResistance = (float) AntarchySettings.ultimateArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.put(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            UUID.nameUUIDFromBytes((prefix + "_armor_knockback_resistance").getBytes()),
                            prefix + "_armor_knockback_resistance",
                            knockbackResistance,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        return builder.build();
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.ultimateHelmetArmorValue();
            case CHESTPLATE -> AntarchySettings.ultimateChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.ultimateLeggingsArmorValue();
            case BOOTS -> AntarchySettings.ultimateBootsArmorValue();
        };
    }

    @Override
    public float getToughness() {
        return (float) switch (this.armorType) {
            case HELMET -> AntarchySettings.ultimateHelmetArmorToughness();
            case CHESTPLATE -> AntarchySettings.ultimateChestplateArmorToughness();
            case LEGGINGS -> AntarchySettings.ultimateLeggingsArmorToughness();
            case BOOTS -> AntarchySettings.ultimateBootsArmorToughness();
        };
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.ultimateArmorEnchantability();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        UltimateGearHelper.ensureUltimateArmorEnchantments(stack, level.registryAccess());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        UltimateGearHelper.ensureUltimateArmorEnchantments(stack, level.registryAccess());
    }
}
