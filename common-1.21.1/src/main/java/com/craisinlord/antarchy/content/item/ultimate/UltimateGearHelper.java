package com.craisinlord.antarchy.content.item.ultimate;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

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
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getEnchantments());
        setEnchantmentLevel(enchantments, registries, Enchantments.PROTECTION, 5);
        setEnchantmentLevel(enchantments, registries, Enchantments.FIRE_PROTECTION, 5);
        setEnchantmentLevel(enchantments, registries, Enchantments.PROJECTILE_PROTECTION, 5);
        setEnchantmentLevel(enchantments, registries, Enchantments.BLAST_PROTECTION, 5);
        net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable().withTooltip(true));
    }

    public static void ensureUltimateBowEnchantments(ItemStack stack, HolderLookup.Provider registries) {
        if (!AntarchySettings.ultimateBowComesEnchantedWithFlame()) {
            return;
        }

        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getEnchantments());
        setEnchantmentLevel(enchantments, registries, Enchantments.FLAME, 1);
        net.minecraft.world.item.enchantment.EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable().withTooltip(true));
    }

    private static void setEnchantmentLevel(
            ItemEnchantments.Mutable enchantments,
            HolderLookup.Provider registries,
            ResourceKey<Enchantment> enchantmentKey,
            int level
    ) {
        var enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(enchantmentKey);
        if (enchantments.getLevel(enchantment) < level) {
            enchantments.set(enchantment, level);
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
