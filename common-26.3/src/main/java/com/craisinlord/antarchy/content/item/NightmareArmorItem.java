package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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

public class NightmareArmorItem extends Item {
    private static final int BASE_DURABILITY_MULTIPLIER = 33;
    private final ArmorType armorType;

    public NightmareArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(AntarchyItemProperties.configAttributes(
                properties.humanoidArmor(material, type).stacksTo(1).durability(resolveDurability(type)),
                () -> buildAttributes(type)
        ));
        this.armorType = type;
    }

    public ArmorType getArmorType() {
        return this.armorType;
    }

    private static ItemAttributeModifiers buildAttributes(ArmorType type) {
        String prefix = "nightmare_" + type.getName();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
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
                                resolveToughness(type),
                                AttributeModifier.Operation.ADD_VALUE
                        ),
                        slot
                );

        float knockbackResistance = (float) AntarchySettings.nightmareArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
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

        double doubleDamageChance = resolveDoubleDamageChance(type);
        if (doubleDamageChance > 0.0) {
            builder.add(
                    AntarchyObjects.DOUBLE_DAMAGE_CHANCE.get(),
                    new AttributeModifier(
                            Identifier.fromNamespaceAndPath("antarchy", prefix + "_double_damage_chance"),
                            doubleDamageChance,
                            AttributeModifier.Operation.ADD_VALUE
                    ),
                    slot
            );
        }

        return builder.build();
    }

    private static double resolveDoubleDamageChance(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.nightmareHelmetDoubleDamageChance();
            case CHESTPLATE, BODY -> AntarchySettings.nightmareChestplateDoubleDamageChance();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsDoubleDamageChance();
            case BOOTS -> AntarchySettings.nightmareBootsDoubleDamageChance();
        };
    }

    private static int resolveDefense(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.nightmareHelmetArmorValue();
            case CHESTPLATE, BODY -> AntarchySettings.nightmareChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsArmorValue();
            case BOOTS -> AntarchySettings.nightmareBootsArmorValue();
        };
    }

    private static float resolveToughness(ArmorType type) {
        return (float) switch (type) {
            case HELMET -> AntarchySettings.nightmareHelmetArmorToughness();
            case CHESTPLATE, BODY -> AntarchySettings.nightmareChestplateArmorToughness();
            case LEGGINGS -> AntarchySettings.nightmareLeggingsArmorToughness();
            case BOOTS -> AntarchySettings.nightmareBootsArmorToughness();
        };
    }

    public int getDefense() {
        return resolveDefense(this.armorType);
    }

    public float getToughness() {
        return resolveToughness(this.armorType);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (livingEntity.getItemBySlot(this.armorType.getSlot()) != stack || !isWearingFullSet(livingEntity)) {
            return;
        }

        livingEntity.removeEffect(MobEffects.WITHER);
        livingEntity.removeEffect(AntarchyObjects.DREAD.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int pct = (int) Math.round(resolveDoubleDamageChance(this.armorType) * 100);
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.nightmare_armor_double_damage", pct).withStyle(ChatFormatting.DARK_PURPLE));
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.nightmare_armor_set_bonus").withStyle(ChatFormatting.DARK_RED));
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    public static boolean isWearingFullSet(LivingEntity livingEntity) {
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR
                    && !(livingEntity.getItemBySlot(slot).getItem() instanceof NightmareArmorItem)) {
                return false;
            }
        }
        return true;
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
