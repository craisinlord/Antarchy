package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.AntarchyItemProperties;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

public class UltimateArmorItem extends Item {
    private final ArmorType armorType;

    public UltimateArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(
                AntarchyItemProperties.configAttributes(
                        properties.humanoidArmor(material, type),
                        () -> createAttributes(type)
                ),
                AntarchySettings::ultimateArmorEnchantability
        ));
        this.armorType = type;
    }

    public ArmorType getArmorType() {
        return this.armorType;
    }

    private static ItemAttributeModifiers createAttributes(ArmorType type) {
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace(type.getName() + "_armor"),
                                getDefense(type),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace(type.getName() + "_armor_toughness"),
                                getToughness(type),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                );

        float knockbackResistance = (float) AntarchySettings.ultimateArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            Identifier.withDefaultNamespace(type.getName() + "_armor_knockback_resistance"),
                            knockbackResistance,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slot
            );
        }

        return builder.build();
    }

    public static int getDefense(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.ultimateHelmetArmorValue();
            case CHESTPLATE, BODY -> AntarchySettings.ultimateChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.ultimateLeggingsArmorValue();
            case BOOTS -> AntarchySettings.ultimateBootsArmorValue();
        };
    }

    public static float getToughness(ArmorType type) {
        return (float) switch (type) {
            case HELMET -> AntarchySettings.ultimateHelmetArmorToughness();
            case CHESTPLATE, BODY -> AntarchySettings.ultimateChestplateArmorToughness();
            case LEGGINGS -> AntarchySettings.ultimateLeggingsArmorToughness();
            case BOOTS -> AntarchySettings.ultimateBootsArmorToughness();
        };
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        UltimateGearHelper.ensureUltimateArmorEnchantments(stack, level.registryAccess());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        UltimateGearHelper.ensureUltimateArmorEnchantments(stack, player.level().registryAccess());
    }
}
