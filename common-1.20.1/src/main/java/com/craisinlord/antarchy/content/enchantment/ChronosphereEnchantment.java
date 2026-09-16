package com.craisinlord.antarchy.content.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class ChronosphereEnchantment extends Enchantment {
    public ChronosphereEnchantment(Rarity rarity, EquipmentSlot... slots) {
        super(rarity, EnchantmentCategory.ARMOR_CHEST, slots);
    }

    @Override
    public int getMaxLevel() { return 3; }

    @Override
    public int getMinCost(int level) { return 15 + (level - 1) * 15; }

    @Override
    public int getMaxCost(int level) { return getMinCost(level) + 35; }
}
