package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.compat.item.ArmorDurability;
import com.craisinlord.antarchy.content.item.royal.RoyalGearHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class RoyalGuardianArmorItem extends ArmorItem {
    private final Type armorType;

    public RoyalGuardianArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties.durability(ArmorDurability.forType(type, AntarchySettings.royalArmorDurabilityMultiplier())));
        this.armorType = type;
    }

    public Type getArmorType() {
        return this.armorType;
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.royalGuardianHelmetArmorValue();
            case CHESTPLATE -> AntarchySettings.royalGuardianChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.royalGuardianLeggingsArmorValue();
            case BOOTS -> AntarchySettings.royalGuardianBootsArmorValue();
        };
    }

    @Override
    public float getToughness() {
        return (float) AntarchySettings.royalGuardianArmorToughness();
    }

    public static double judgmentDamageMultiplier(Player player) {
        double bonus = 0.0D;
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof RoyalGuardianArmorItem) bonus += AntarchySettings.royalGuardianHelmetJudgmentBonus();
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RoyalGuardianArmorItem) bonus += AntarchySettings.royalGuardianChestplateJudgmentBonus();
        if (player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof RoyalGuardianArmorItem) bonus += AntarchySettings.royalGuardianLeggingsJudgmentBonus();
        if (player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof RoyalGuardianArmorItem) bonus += AntarchySettings.royalGuardianBootsJudgmentBonus();
        return 1.0D + bonus;
    }

    private double judgmentBonus() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.royalGuardianHelmetJudgmentBonus();
            case CHESTPLATE -> AntarchySettings.royalGuardianChestplateJudgmentBonus();
            case LEGGINGS -> AntarchySettings.royalGuardianLeggingsJudgmentBonus();
            case BOOTS -> AntarchySettings.royalGuardianBootsJudgmentBonus();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String slotName = switch (this.armorType) {
            case HELMET -> "helmet";
            case CHESTPLATE -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
        };
        tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_guardian_" + slotName + ".judgment",
                Math.round(judgmentBonus() * 100.0D)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (this.armorType == Type.CHESTPLATE) {
            tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_guardian_chestplate.boundary")
                    .withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.armorType.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }
        String prefix = "royal_guardian_" + this.armorType.getName();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ARMOR,
                new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_armor").getBytes()), prefix + "_armor", getDefense(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_armor_toughness").getBytes()), prefix + "_armor_toughness", getToughness(), AttributeModifier.Operation.ADDITION));

        float knockbackResistance = (float) AntarchySettings.royalGuardianArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_knockback_resistance").getBytes()), prefix + "_knockback_resistance", knockbackResistance, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalArmorEnchantability();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, level.registryAccess());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, level.registryAccess());
    }
}
