package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
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

public class BloodCrystalArmorItem extends ArmorItem {
    private final Type armorType;

    public BloodCrystalArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties.stacksTo(1).durability(resolveDurability(type)));
        this.armorType = type;
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.bloodCrystalHelmetDefense();
            case CHESTPLATE, BODY -> AntarchySettings.bloodCrystalChestplateDefense();
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
            case CHESTPLATE, BODY -> AntarchySettings.bloodCrystalChestplateDurability();
            case LEGGINGS -> AntarchySettings.bloodCrystalLeggingsDurability();
            case BOOTS -> AntarchySettings.bloodCrystalBootsDurability();
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        String prefix = "blood_crystal_" + this.armorType.getName();
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace(prefix + "_armor"),
                                getDefense(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(this.armorType.getSlot())
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                ResourceLocation.withDefaultNamespace(prefix + "_armor_toughness"),
                                getToughness(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(this.armorType.getSlot())
                )
                .add(
                        AntarchyObjects.BLOODGLASS_MAX_HEARTS.get(),
                        new AttributeModifier(
                                ResourceLocation.fromNamespaceAndPath("antarchy", prefix + "_bloodglass_max"),
                                1.0,
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        EquipmentSlotGroup.bySlot(this.armorType.getSlot())
                );
        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.antarchy.blood_crystal_armor.tooltip").withStyle(ChatFormatting.RED));
        if (this.armorType == Type.BOOTS) {
            tooltipComponents.add(Component.translatable("item.antarchy.blood_crystal_boots.tooltip2").withStyle(ChatFormatting.DARK_RED));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
