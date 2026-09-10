package com.craisinlord.antarchy.content.item.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class RoyalGearHelper {
    private RoyalGearHelper() {
    }

    public static ItemStack createRoyalArmorStack(net.minecraft.world.item.Item item, net.minecraft.core.RegistryAccess registries) {
        ItemStack stack = new ItemStack(item);
        ensureRoyalArmorEnchantments(stack, registries);
        return stack;
    }

    public static void ensureRoyalArmorEnchantments(ItemStack stack, net.minecraft.core.RegistryAccess registries) {
        if (!AntarchySettings.royalArmorComesEnchanted()) {
            return;
        }
        int level = AntarchySettings.royalArmorProtectionLevel();
        java.util.Map<Enchantment, Integer> enchantments = new java.util.HashMap<>(EnchantmentHelper.getEnchantments(stack));
        setLevel(enchantments, Enchantments.ALL_DAMAGE_PROTECTION, level);
        setLevel(enchantments, Enchantments.FIRE_PROTECTION, level);
        setLevel(enchantments, Enchantments.PROJECTILE_PROTECTION, level);
        setLevel(enchantments, Enchantments.BLAST_PROTECTION, level);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    private static void setLevel(java.util.Map<Enchantment, Integer> enchantments, Enchantment enchantment, int level) {
        if (enchantments.getOrDefault(enchantment, 0) < level) {
            enchantments.put(enchantment, level);
        }
    }

    public static boolean hasUpwardFallImmunityBoots(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof RoyalAssailantArmorItem armor
                && armor.getArmorType() == net.minecraft.world.item.ArmorItem.Type.BOOTS;
    }
}
