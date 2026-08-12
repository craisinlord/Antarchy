package com.craisinlord.antarchy.content.item;

import com.google.common.collect.ImmutableMultimap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class PrimordialArmorItem extends ArmorItem {
    private final Type armorType;

    public PrimordialArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties.stacksTo(1).durability(resolveDurability(type)));
        this.armorType = type;
    }

    @Override
    public com.google.common.collect.Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.armorType.getSlot()) {
            return super.getDefaultAttributeModifiers(slot);
        }

        String prefix = "primordial_" + this.armorType.getName();
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

        double knockbackResistance = getKnockbackResistance(this.armorType);
        if (knockbackResistance > 0.0D) {
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

        builder.put(
                Attributes.ATTACK_KNOCKBACK,
                new AttributeModifier(
                        UUID.nameUUIDFromBytes((prefix + "_attack_knockback").getBytes()),
                        prefix + "_attack_knockback",
                        AntarchySettings.primordialArmorKnockbackPerPiece(),
                        AttributeModifier.Operation.ADDITION
                )
        );

        return builder.build();
    }

    @Override
    public int getDefense() {
        return super.getDefense();
    }

    @Override
    public float getToughness() {
        return super.getToughness();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && this.armorType == Type.HELMET && entity instanceof LivingEntity living) {
            if (living.getItemBySlot(EquipmentSlot.HEAD) == stack) {
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, false, false, true));
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (this.armorType == Type.HELMET) {
            tooltipComponents.add(Component.translatable("tooltip.antarchy.primordial_helmet_water_breathing").withStyle(ChatFormatting.AQUA));
        }
        String knockbackDealt = String.format(Locale.ROOT, "%.1f", AntarchySettings.primordialArmorKnockbackPerPiece());
        tooltipComponents.add(Component.translatable("tooltip.antarchy.primordial_armor_knockback_dealt", knockbackDealt).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("tooltip.antarchy.primordial_armor_set_bonus").withStyle(ChatFormatting.DARK_GREEN));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static double getKnockbackDealtBonus(LivingEntity entity) {
        int pieces = 0;
        for (ItemStack armorStack : entity.getArmorSlots()) {
            if (armorStack.getItem() instanceof PrimordialArmorItem) {
                pieces++;
            }
        }
        return pieces * AntarchySettings.primordialArmorKnockbackPerPiece();
    }

    public static boolean hasFullSet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof PrimordialArmorItem
                && entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof PrimordialArmorItem
                && entity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof PrimordialArmorItem
                && entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof PrimordialArmorItem;
    }

    private static double getKnockbackResistance(Type armorType) {
        return switch (armorType) {
            case HELMET -> 0.20D;
            case CHESTPLATE -> 0.30D;
            case LEGGINGS -> 0.30D;
            case BOOTS -> 0.20D;
        };
    }

    private static int resolveDurability(Type armorType) {
        return switch (armorType) {
            case HELMET -> 407;
            case CHESTPLATE -> 592;
            case LEGGINGS -> 555;
            case BOOTS -> 481;
        };
    }
}
