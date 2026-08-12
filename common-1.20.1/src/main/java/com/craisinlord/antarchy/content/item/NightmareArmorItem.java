package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class NightmareArmorItem extends ArmorItem {
    private final Type armorType;

    public NightmareArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        this.armorType = type;
    }

    @Override
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.armorType.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }

        String prefix = "nightmare_" + this.armorType.getName();
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

        float knockbackResistance = (float) AntarchySettings.nightmareArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.put(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            UUID.nameUUIDFromBytes((prefix + "_knockback_resistance").getBytes()),
                            prefix + "_knockback_resistance",
                            knockbackResistance,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        double doubleDamageChance = getDoubleDamageChance();
        if (doubleDamageChance > 0.0) {
            builder.put(
                    AntarchyObjects.DOUBLE_DAMAGE_CHANCE.get().value(),
                    new AttributeModifier(
                            UUID.nameUUIDFromBytes((prefix + "_double_damage_chance").getBytes()),
                            prefix + "_double_damage_chance",
                            doubleDamageChance,
                            AttributeModifier.Operation.ADDITION
                    )
            );
        }

        return builder.build();
    }

    private double getDoubleDamageChance() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.nightmareHelmetDoubleDamageChance();
            case CHESTPLATE -> AntarchySettings.nightmareChestplateDoubleDamageChance();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsDoubleDamageChance();
            case BOOTS -> AntarchySettings.nightmareBootsDoubleDamageChance();
        };
    }

    @Override
    public int getDefense() {
        return switch (this.armorType) {
            case HELMET -> AntarchySettings.nightmareHelmetArmorValue();
            case CHESTPLATE -> AntarchySettings.nightmareChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsArmorValue();
            case BOOTS -> AntarchySettings.nightmareBootsArmorValue();
        };
    }

    @Override
    public float getToughness() {
        return (float) switch (this.armorType) {
            case HELMET -> AntarchySettings.nightmareHelmetArmorToughness();
            case CHESTPLATE -> AntarchySettings.nightmareChestplateArmorToughness();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsArmorToughness();
            case BOOTS -> AntarchySettings.nightmareBootsArmorToughness();
        };
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (livingEntity.getItemBySlot(this.armorType.getSlot()) != stack || !isWearingFullSet(livingEntity)) {
            return;
        }

        livingEntity.removeEffect(MobEffects.WITHER);
        livingEntity.removeEffect(AntarchyObjects.DREAD.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int pct = (int) Math.round(getDoubleDamageChance() * 100);
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_armor_double_damage", pct).withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.nightmare_armor_set_bonus").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean isWearingFullSet(LivingEntity livingEntity) {
        for (ItemStack armorStack : livingEntity.getArmorSlots()) {
            if (!(armorStack.getItem() instanceof NightmareArmorItem)) {
                return false;
            }
        }
        return true;
    }
}
