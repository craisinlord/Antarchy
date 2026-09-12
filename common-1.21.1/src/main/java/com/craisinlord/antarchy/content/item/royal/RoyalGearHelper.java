package com.craisinlord.antarchy.content.item.royal;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantments;
import com.craisinlord.antarchy.content.item.RoyalAssailantArmorItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class RoyalGearHelper {
    private RoyalGearHelper() {
    }

    public static ItemStack createRoyalArmorStack(net.minecraft.world.item.Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        ensureRoyalArmorEnchantments(stack, registries);
        return stack;
    }

    public static void ensureRoyalArmorEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (stack.getItem() instanceof RoyalAssailantArmorItem armor
                && armor.getArmorType() == net.minecraft.world.item.ArmorItem.Type.BOOTS) {
            AntarchyEnchantments.ensure(stack, registries, AntarchyEnchantments.FEATHER_RISING, 1);
        }
        if (!AntarchySettings.royalArmorComesEnchanted()) {
            return;
        }
        int level = AntarchySettings.royalArmorProtectionLevel();
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getEnchantments());
        setLevel(enchantments, registries, Enchantments.PROTECTION, level);
        setLevel(enchantments, registries, Enchantments.FIRE_PROTECTION, level);
        setLevel(enchantments, registries, Enchantments.PROJECTILE_PROTECTION, level);
        setLevel(enchantments, registries, Enchantments.BLAST_PROTECTION, level);
        EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable().withTooltip(true));
    }

    private static void setLevel(ItemEnchantments.Mutable enchantments, HolderLookup.Provider registries,
            ResourceKey<Enchantment> key, int level) {
        var enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        if (enchantments.getLevel(enchantment) < level) {
            enchantments.set(enchantment, level);
        }
    }

}
