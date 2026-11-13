package com.craisinlord.antarchy.content.item.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class RoyalGearHelper {
    private RoyalGearHelper() {
    }

    public static ItemStack createRoyalArmorStack(Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        ensureRoyalArmorEnchantments(stack, registries);
        return stack;
    }

    public static void ensureRoyalArmorEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (stack.getItem() instanceof RoyalAssailantArmorItem armor
                && armor.getArmorType() == ArmorItem.Type.BOOTS) {
            AntarchyEnchantments.ensure(stack, AntarchyEnchantments.featherRising(), 1);
        }
        if (!AntarchySettings.royalArmorComesEnchanted()) {
            return;
        }
        int level = AntarchySettings.royalArmorProtectionLevel();
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        setLevel(enchantments, Enchantments.ALL_DAMAGE_PROTECTION, level);
        setLevel(enchantments, Enchantments.FIRE_PROTECTION, level);
        setLevel(enchantments, Enchantments.PROJECTILE_PROTECTION, level);
        setLevel(enchantments, Enchantments.BLAST_PROTECTION, level);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    private static void setLevel(Map<Enchantment, Integer> enchantments, Enchantment enchantment, int level) {
        if (enchantments.getOrDefault(enchantment, 0) < level) {
            enchantments.put(enchantment, level);
        }
    }

}
