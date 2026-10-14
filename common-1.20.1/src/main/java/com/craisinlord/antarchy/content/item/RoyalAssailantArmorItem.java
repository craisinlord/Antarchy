package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.compat.item.ArmorDurability;
import com.craisinlord.antarchy.content.item.royal.RoyalGearHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

public class RoyalAssailantArmorItem extends ArmorItem {
    private final Type armorType;

    public RoyalAssailantArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties.durability(ArmorDurability.forType(type, AntarchySettings.royalArmorDurabilityMultiplier())));
        this.armorType = type;
    }

    public Type getArmorType() {
        return this.armorType;
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.royalAssailantHelmetArmorValue();
            case CHESTPLATE -> AntarchySettings.royalAssailantChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.royalAssailantLeggingsArmorValue();
            case BOOTS -> AntarchySettings.royalAssailantBootsArmorValue();
        };
    }

    @Override
    public float getToughness() {
        return (float) AntarchySettings.royalAssailantArmorToughness();
    }

    private double getDoubleDamageChance() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.royalAssailantHelmetDoubleDamageChance();
            case CHESTPLATE -> AntarchySettings.royalAssailantChestplateDoubleDamageChance();
            case LEGGINGS -> AntarchySettings.royalAssailantLeggingsDoubleDamageChance();
            case BOOTS -> AntarchySettings.royalAssailantBootsDoubleDamageChance();
        };
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.armorType.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }
        String prefix = "royal_assailant_" + this.armorType.getName();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ARMOR,
                new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_armor").getBytes()), prefix + "_armor", getDefense(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_armor_toughness").getBytes()), prefix + "_armor_toughness", getToughness(), AttributeModifier.Operation.ADDITION));

        float knockbackResistance = (float) AntarchySettings.royalAssailantArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_knockback_resistance").getBytes()), prefix + "_knockback_resistance", knockbackResistance, AttributeModifier.Operation.ADDITION));
        }

        double doubleDamageChance = getDoubleDamageChance();
        if (doubleDamageChance > 0.0) {
            builder.put(AntarchyObjects.DOUBLE_DAMAGE_CHANCE.get().value(),
                    new AttributeModifier(UUID.nameUUIDFromBytes((prefix + "_double_damage_chance").getBytes()), prefix + "_double_damage_chance", doubleDamageChance, AttributeModifier.Operation.ADDITION));
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

        if (level.isClientSide || this.armorType != Type.CHESTPLATE || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (!isWearingChestplate(living)) {
            clearToggledInversion(living);
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, level.registryAccess());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int pct = (int) Math.round(getDoubleDamageChance() * 100);
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_armor_double_damage", pct).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (this.armorType == Type.CHESTPLATE) {
            tooltipComponents.add(Component.translatable("tooltip.antarchy.royal_assailant_chestplate.invert").withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean isWearingChestplate(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RoyalAssailantArmorItem armor
                && armor.armorType == Type.CHESTPLATE;
    }

    public static void toggleInversion(ServerPlayer player) {
        if (!isWearingChestplate(player)) {
            return;
        }
        MobEffectInstance existing = player.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
        if (existing != null && existing.isInfiniteDuration()) {
            player.removeEffect(AntarchyObjects.INVERTED_EFFECT.get());
        } else {
            player.addEffect(new MobEffectInstance(AntarchyObjects.INVERTED_EFFECT.get(), -1, 0, false, false, true));
        }
    }

    private static void clearToggledInversion(LivingEntity entity) {
        MobEffectInstance existing = entity.getEffect(AntarchyObjects.INVERTED_EFFECT.get());
        if (existing != null && existing.isInfiniteDuration()) {
            entity.removeEffect(AntarchyObjects.INVERTED_EFFECT.get());
        }
    }
}
