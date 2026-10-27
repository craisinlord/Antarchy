package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

public class PrimordialArmorItem extends Item {
    private static final int BASE_DURABILITY_MULTIPLIER = 33;
    private final ArmorMaterial material;
    private final ArmorType armorType;

    public PrimordialArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.humanoidArmor(material, type).stacksTo(1).durability(resolveDurability(type)),
                () -> buildAttributes(material, type)
        ));
        this.material = material;
        this.armorType = type;
    }

    public ArmorType getArmorType() {
        return this.armorType;
    }

    private static ItemAttributeModifiers buildAttributes(ArmorMaterial material, ArmorType type) {
        String prefix = "primordial_" + type.getName();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(
                        Attributes.ARMOR,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace(prefix + "_armor"),
                                material.defense().getOrDefault(type, 0),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                )
                .add(
                        Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(
                                Identifier.withDefaultNamespace(prefix + "_armor_toughness"),
                                material.toughness(),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                );

        double knockbackResistance = getKnockbackResistance(type);
        if (knockbackResistance > 0.0D) {
            builder.add(
                    Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            Identifier.withDefaultNamespace(prefix + "_knockback_resistance"),
                            knockbackResistance,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slot
            );
        }

        builder.add(
                Attributes.ATTACK_KNOCKBACK,
                new AttributeModifier(
                        Identifier.withDefaultNamespace(prefix + "_attack_knockback"),
                        AntarchySettings.primordialArmorKnockbackPerPiece(),
                        AttributeModifier.Operation.ADD_VALUE
                ),
                slot
        );

        return builder.build();
    }

    public int getDefense() {
        return this.material.defense().getOrDefault(this.armorType, 0);
    }

    public float getToughness() {
        return this.material.toughness();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (this.armorType == ArmorType.HELMET && entity instanceof LivingEntity living) {
            if (living.getItemBySlot(EquipmentSlot.HEAD) == stack) {
                living.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, false, false, true));
            }
        }
        super.inventoryTick(stack, level, entity, slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (this.armorType == ArmorType.HELMET) {
            tooltipComponents.accept(Component.translatable("tooltip.antarchy.primordial_helmet_water_breathing").withStyle(ChatFormatting.AQUA));
        }
        String knockbackDealt = String.format(Locale.ROOT, "%.1f", AntarchySettings.primordialArmorKnockbackPerPiece());
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.primordial_armor_knockback_dealt", knockbackDealt).withStyle(ChatFormatting.YELLOW));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.primordial_armor_set_bonus").withStyle(ChatFormatting.DARK_GREEN));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    public static double getKnockbackDealtBonus(LivingEntity entity) {
        int pieces = 0;
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                    && entity.getItemBySlot(slot).getItem() instanceof PrimordialArmorItem) {
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

    private static double getKnockbackResistance(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> 0.20D;
            case CHESTPLATE, BODY -> 0.30D;
            case LEGGINGS -> 0.30D;
            case BOOTS -> 0.20D;
        };
    }

    private static int resolveDurability(ArmorType armorType) {
        return switch (armorType) {
            case HELMET -> ArmorType.HELMET.getDurability(BASE_DURABILITY_MULTIPLIER);
            case CHESTPLATE, BODY -> ArmorType.CHESTPLATE.getDurability(BASE_DURABILITY_MULTIPLIER);
            case LEGGINGS -> ArmorType.LEGGINGS.getDurability(BASE_DURABILITY_MULTIPLIER);
            case BOOTS -> ArmorType.BOOTS.getDurability(BASE_DURABILITY_MULTIPLIER);
        };
    }
}
