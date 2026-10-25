package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.function.DoubleSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

public class UltimateSwordItem extends SwordItem {
    private final Tier tier;
    private final DoubleSupplier attackDamage;
    private final float attackSpeed;

    public UltimateSwordItem(Tier tier, Item.Properties properties, DoubleSupplier attackDamage, float attackSpeed) {
        super(tier, (int) Math.round(attackDamage.getAsDouble()), (float) AntarchySettings.ultimateSwordAttackSpeed(), properties);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
    }

    @Override
    public com.google.common.collect.Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> getDefaultAttributeModifiers(net.minecraft.world.entity.EquipmentSlot slot) {
        if (slot != net.minecraft.world.entity.EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        return com.craisinlord.antarchy.compat.item.WeaponAttributes.base(this.tier, (float) Math.round(this.attackDamage.getAsDouble()), (float) AntarchySettings.ultimateSwordAttackSpeed()).build();
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return AntarchySettings.ultimateToolEnchantability();
    }
}
