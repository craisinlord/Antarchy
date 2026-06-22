package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public class UltimateArmorItem extends ArmorItem {
    private final Type armorType;

    public UltimateArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
        this.armorType = type;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace(this.armorType.getName() + "_armor"),
                                getDefense(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(this.armorType.getSlot())
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace(this.armorType.getName() + "_armor_toughness"),
                                getToughness(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(this.armorType.getSlot())
                );

        float knockbackResistance = (float) AntarchySettings.ultimateArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            ResourceLocation.withDefaultNamespace(this.armorType.getName() + "_armor_knockback_resistance"),
                            knockbackResistance,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.bySlot(this.armorType.getSlot())
            );
        }

        return builder.build();
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.ultimateHelmetArmorValue();
            case CHESTPLATE, BODY -> AntarchySettings.ultimateChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.ultimateLeggingsArmorValue();
            case BOOTS -> AntarchySettings.ultimateBootsArmorValue();
        };
    }

    @Override
    public float getToughness() {
        return (float) switch (this.armorType) {
            case HELMET -> AntarchySettings.ultimateHelmetArmorToughness();
            case CHESTPLATE, BODY -> AntarchySettings.ultimateChestplateArmorToughness();
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
