package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.item.royal.RoyalGearHelper;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
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

public class RoyalAssailantArmorItem extends Item {
    private final ArmorType armorType;

    public RoyalAssailantArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(AntarchyItemProperties.configEnchantable(
                AntarchyItemProperties.configAttributes(
                        properties.humanoidArmor(material, type)
                                .stacksTo(1)
                                .durability(type.getDurability(AntarchySettings.royalArmorDurabilityMultiplier())),
                        () -> buildAttributes(type)
                ),
                AntarchySettings::royalArmorEnchantability
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
        return (float) AntarchySettings.royalAssailantArmorToughness();
    }

    private static int resolveDefense(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.royalAssailantHelmetArmorValue();
            case CHESTPLATE, BODY -> AntarchySettings.royalAssailantChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.royalAssailantLeggingsArmorValue();
            case BOOTS -> AntarchySettings.royalAssailantBootsArmorValue();
        };
    }

    private static double resolveDoubleDamageChance(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.royalAssailantHelmetDoubleDamageChance();
            case CHESTPLATE, BODY -> AntarchySettings.royalAssailantChestplateDoubleDamageChance();
            case LEGGINGS -> AntarchySettings.royalAssailantLeggingsDoubleDamageChance();
            case BOOTS -> AntarchySettings.royalAssailantBootsDoubleDamageChance();
        };
    }

    private static ItemAttributeModifiers buildAttributes(ArmorType type) {
        String prefix = "royal_assailant_" + type.getName();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(Identifier.withDefaultNamespace(prefix + "_armor"), resolveDefense(type), AttributeModifier.Operation.ADD_VALUE),
                        slot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(Identifier.withDefaultNamespace(prefix + "_armor_toughness"), (float) AntarchySettings.royalAssailantArmorToughness(), AttributeModifier.Operation.ADD_VALUE),
                        slot);

        float knockbackResistance = (float) AntarchySettings.royalAssailantArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(Identifier.withDefaultNamespace(prefix + "_knockback_resistance"), knockbackResistance, AttributeModifier.Operation.ADD_VALUE),
                    slot);
        }

        double doubleDamageChance = resolveDoubleDamageChance(type);
        if (doubleDamageChance > 0.0) {
            builder.add(AntarchyObjects.DOUBLE_DAMAGE_CHANCE.get(),
                    new AttributeModifier(Identifier.fromNamespaceAndPath("antarchy", prefix + "_double_damage_chance"), doubleDamageChance, AttributeModifier.Operation.ADD_VALUE),
                    slot);
        }
        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, level.registryAccess());

        if (this.armorType != ArmorType.CHESTPLATE || !(entity instanceof LivingEntity living)) {
            return;
        }
        if (!isWearingChestplate(living)) {
            clearToggledInversion(living);
        }
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, player.level().registryAccess());
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        int pct = (int) Math.round(resolveDoubleDamageChance(this.armorType) * 100);
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.nightmare_armor_double_damage", pct).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (this.armorType == ArmorType.CHESTPLATE) {
            tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_assailant_chestplate.invert").withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    public static boolean isWearingChestplate(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof RoyalAssailantArmorItem armor
                && armor.armorType == ArmorType.CHESTPLATE;
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
