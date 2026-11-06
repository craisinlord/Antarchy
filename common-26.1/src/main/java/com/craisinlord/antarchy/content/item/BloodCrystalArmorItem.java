package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

public class BloodCrystalArmorItem extends Item {
    private final ArmorType armorType;

    public BloodCrystalArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.humanoidArmor(material, type).stacksTo(1).durability(resolveDurability(type)),
                () -> buildAttributes(type)
        ));
        this.armorType = type;
    }

    public ArmorType getArmorType() {
        return this.armorType;
    }

    public int getDefense() {
        return resolveDefense(this.armorType);
    }

    public float getToughness() {
        return (float) AntarchySettings.bloodCrystalArmorToughness();
    }

    private static int resolveDefense(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.bloodCrystalHelmetDefense();
            case CHESTPLATE, BODY -> AntarchySettings.bloodCrystalChestplateDefense();
            case LEGGINGS -> AntarchySettings.bloodCrystalLeggingsDefense();
            case BOOTS -> AntarchySettings.bloodCrystalBootsDefense();
        };
    }

    private static int resolveDurability(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.bloodCrystalHelmetDurability();
            case CHESTPLATE, BODY -> AntarchySettings.bloodCrystalChestplateDurability();
            case LEGGINGS -> AntarchySettings.bloodCrystalLeggingsDurability();
            case BOOTS -> AntarchySettings.bloodCrystalBootsDurability();
        };
    }

    private static ItemAttributeModifiers buildAttributes(ArmorType type) {
        String prefix = "blood_crystal_" + type.getName();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        return ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace(prefix + "_armor"),
                                resolveDefense(type),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace(prefix + "_armor_toughness"),
                                (float) AntarchySettings.bloodCrystalArmorToughness(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                )
                .add(
                        AntarchyObjects.BLOODGLASS_MAX_HEARTS.get(),
                        new AttributeModifier(
                                Identifier.fromNamespaceAndPath("antarchy", prefix + "_bloodglass_max"),
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                )
                .build();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (this.armorType == ArmorType.BOOTS) {
            AntarchyEnchantments.ensure(stack, level.registryAccess(), AntarchyEnchantments.FEATHER_RISING, 1);
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        if (this.armorType == ArmorType.BOOTS) {
            AntarchyEnchantments.ensure(stack, player.level().registryAccess(), AntarchyEnchantments.FEATHER_RISING, 1);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.accept(Component.translatable("item.antarchy.blood_crystal_armor.tooltip").withStyle(ChatFormatting.RED));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }
}
