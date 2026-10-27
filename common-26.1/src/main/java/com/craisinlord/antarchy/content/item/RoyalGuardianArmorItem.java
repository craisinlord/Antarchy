package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.royal.RoyalGearHelper;
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

public class RoyalGuardianArmorItem extends Item {
    private final ArmorType armorType;

    public RoyalGuardianArmorItem(ArmorMaterial material, ArmorType type, Item.Properties properties) {
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
        return (float) AntarchySettings.royalGuardianArmorToughness();
    }

    private static int resolveDefense(ArmorType type) {
        return switch (type) {
            case HELMET -> AntarchySettings.royalGuardianHelmetArmorValue();
            case CHESTPLATE, BODY -> AntarchySettings.royalGuardianChestplateArmorValue();
            case LEGGINGS -> AntarchySettings.royalGuardianLeggingsArmorValue();
            case BOOTS -> AntarchySettings.royalGuardianBootsArmorValue();
        };
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
            case CHESTPLATE, BODY -> AntarchySettings.royalGuardianChestplateJudgmentBonus();
            case LEGGINGS -> AntarchySettings.royalGuardianLeggingsJudgmentBonus();
            case BOOTS -> AntarchySettings.royalGuardianBootsJudgmentBonus();
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        String slotName = switch (this.armorType) {
            case HELMET -> "helmet";
            case CHESTPLATE, BODY -> "chestplate";
            case LEGGINGS -> "leggings";
            case BOOTS -> "boots";
        };
        tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_guardian_" + slotName + ".judgment",
                Math.round(judgmentBonus() * 100.0D)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (this.armorType == ArmorType.CHESTPLATE) {
            tooltipComponents.accept(Component.translatable("tooltip.antarchy.royal_guardian_chestplate.boundary")
                    .withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
    }

    private static ItemAttributeModifiers buildAttributes(ArmorType type) {
        String prefix = "royal_guardian_" + type.getName();
        EquipmentSlotGroup slot = EquipmentSlotGroup.bySlot(type.getSlot());
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(Identifier.withDefaultNamespace(prefix + "_armor"), resolveDefense(type), AttributeModifier.Operation.ADD_VALUE),
                        slot)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(Identifier.withDefaultNamespace(prefix + "_armor_toughness"), (float) AntarchySettings.royalGuardianArmorToughness(), AttributeModifier.Operation.ADD_VALUE),
                        slot);

        float knockbackResistance = (float) AntarchySettings.royalGuardianArmorKnockbackResistance();
        if (knockbackResistance > 0.0F) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(Identifier.withDefaultNamespace(prefix + "_knockback_resistance"), knockbackResistance, AttributeModifier.Operation.ADD_VALUE),
                    slot);
        }
        return builder.build();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, level.registryAccess());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Player player) {
        super.onCraftedBy(stack, player);
        RoyalGearHelper.ensureRoyalArmorEnchantments(stack, player.level().registryAccess());
    }
}
