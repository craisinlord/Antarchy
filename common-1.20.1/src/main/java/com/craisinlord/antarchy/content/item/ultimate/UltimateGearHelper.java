package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class UltimateGearHelper {
    public static final String ULTIMATE_BOW_ARROW_TAG = Antarchy.MODID + ".ultimate_bow_arrow";
    public static final String ULTIMATE_CROSSBOW_PROJECTILE_TAG = Antarchy.MODID + ".ultimate_crossbow_projectile";

    private UltimateGearHelper() {
    }

    public static ItemStack createUltimateArmorStack(net.minecraft.world.item.Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        ensureUltimateArmorEnchantments(stack, registries);
        return stack;
    }

    public static ItemStack createUltimateBowStack(net.minecraft.world.item.Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        ensureUltimateBowEnchantments(stack, registries);
        return stack;
    }

    public static void ensureUltimateArmorEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (!AntarchySettings.ultimateArmorComesEnchanted()) {
            return;
        }
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        setEnchantmentLevel(enchantments, Enchantments.ALL_DAMAGE_PROTECTION, 5);
        setEnchantmentLevel(enchantments, Enchantments.FIRE_PROTECTION, 5);
        setEnchantmentLevel(enchantments, Enchantments.PROJECTILE_PROTECTION, 5);
        setEnchantmentLevel(enchantments, Enchantments.BLAST_PROTECTION, 5);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    public static void ensureUltimateBowEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (!AntarchySettings.ultimateBowComesEnchantedWithFlame()) {
            return;
        }

        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        setEnchantmentLevel(enchantments, Enchantments.FLAMING_ARROWS, 1);
        EnchantmentHelper.setEnchantments(enchantments, stack);
    }

    private static void setEnchantmentLevel(Map<Enchantment, Integer> enchantments, Enchantment enchantment, int level) {
        if (enchantments.getOrDefault(enchantment, 0) < level) {
            enchantments.put(enchantment, level);
        }
    }

    public static void tagUltimateBowArrow(AbstractArrow arrow) {
        arrow.addTag(ULTIMATE_BOW_ARROW_TAG);
    }

    public static boolean isUltimateBowArrow(Entity entity) {
        return entity != null && entity.getTags().contains(ULTIMATE_BOW_ARROW_TAG);
    }

    public static void tagUltimateCrossbowProjectile(Entity projectile) {
        projectile.addTag(ULTIMATE_CROSSBOW_PROJECTILE_TAG);
    }

    public static boolean isUltimateCrossbowProjectile(Entity entity) {
        return entity != null && entity.getTags().contains(ULTIMATE_CROSSBOW_PROJECTILE_TAG);
    }
}
