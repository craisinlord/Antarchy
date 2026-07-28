package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class NightmareArmorItem extends ArmorItem {
    private final Type armorType;

    public NightmareArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
        this.armorType = type;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        String prefix = "nightmare_" + this.armorType.getName();
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
                );

        float knockbackResistance = (float) AntarchySettings.nightmareArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            ResourceLocation.withDefaultNamespace(prefix + "_knockback_resistance"),
                            knockbackResistance,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.bySlot(this.armorType.getSlot())
            );
        }

        double doubleDamageChance = getDoubleDamageChance();
        if (doubleDamageChance > 0.0) {
            builder.add(
                    AntarchyObjects.DOUBLE_DAMAGE_CHANCE.get(),
                    new AttributeModifier(
                            ResourceLocation.fromNamespaceAndPath("antarchy", prefix + "_double_damage_chance"),
                            doubleDamageChance,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.bySlot(this.armorType.getSlot())
            );
        }

        return builder.build();
    }

    private double getDoubleDamageChance() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.nightmareHelmetDoubleDamageChance();
            case CHESTPLATE, BODY -> AntarchySettings.nightmareChestplateDoubleDamageChance();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsDoubleDamageChance();
            case BOOTS -> AntarchySettings.nightmareBootsDoubleDamageChance();
        };
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.nightmareHelmetArmorValue();
            case CHESTPLATE, BODY -> AntarchySettings.nightmareChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsArmorValue();
            case BOOTS -> AntarchySettings.nightmareBootsArmorValue();
        };
    }

    @Override
    public float getToughness() {
        return (float) switch (this.armorType) {
            case HELMET -> AntarchySettings.nightmareHelmetArmorToughness();
            case CHESTPLATE, BODY -> AntarchySettings.nightmareChestplateArmorToughness();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsArmorToughness();
            case BOOTS -> AntarchySettings.nightmareBootsArmorToughness();
        };
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int pct = (int) Math.round(getDoubleDamageChance() * 100);
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_armor_double_damage", pct).withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_armor_set_bonus").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
