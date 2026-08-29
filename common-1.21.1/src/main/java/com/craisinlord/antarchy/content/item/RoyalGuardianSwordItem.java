package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class RoyalGuardianSwordItem extends SwordItem {
    private static final ResourceLocation REACH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_guardian_sword_reach");
    private static final ResourceLocation KNOCKBACK_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("antarchy", "royal_guardian_sword_knockback");
    private final Tier tier;

    public RoyalGuardianSwordItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
        this.tier = tier;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return SwordItem.createAttributes(
                        this.tier,
                        (int) Math.round(AntarchySettings.royalGuardianSwordAttackDamage()),
                        (float) AntarchySettings.royalGuardianSwordAttackSpeed())
                .withModifierAdded(
                        Attributes.ENTITY_INTERACTION_RANGE,
                        new AttributeModifier(REACH_MODIFIER_ID, AntarchySettings.royalWeaponAttackReachBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .withModifierAdded(
                        Attributes.ATTACK_KNOCKBACK,
                        new AttributeModifier(KNOCKBACK_MODIFIER_ID, AntarchySettings.royalWeaponAttackKnockbackBonus(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.royalWeaponEnchantability();
    }
}
