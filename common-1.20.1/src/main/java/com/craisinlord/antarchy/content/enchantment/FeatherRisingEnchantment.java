package com.craisinlord.antarchy.content.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class FeatherRisingEnchantment extends Enchantment {
    public FeatherRisingEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.ARMOR_FEET, slots);
    }

    @Override
    public int getMaxLevel() { return 3; }
}
