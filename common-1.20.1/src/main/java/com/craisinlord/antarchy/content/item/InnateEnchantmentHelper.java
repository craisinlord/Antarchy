package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.ultimate.UltimateArmorItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateBowItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

public final class InnateEnchantmentHelper {
    private InnateEnchantmentHelper() {
    }

    public static boolean hasInnateNonCurseEnchantments(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.isCurse() && isInnateEnchantment(stack, enchantment, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRemovableNonCurseEnchantments(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (!enchantment.isCurse() && !isInnateEnchantment(stack, enchantment, entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack stripOnlyRemovableNonCurseEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>(EnchantmentHelper.getEnchantments(stack));
        enchantments.keySet().removeIf(enchantment ->
                !enchantment.isCurse()
                        && !isInnateEnchantment(stack, enchantment, enchantments.get(enchantment)));
        EnchantmentHelper.setEnchantments(enchantments, stack);
        return stack;
    }

    private static boolean isInnateEnchantment(ItemStack stack, Enchantment enchantment, int level) {
        if (level <= 0) {
            return false;
        }

        if (stack.getItem() instanceof UltimateArmorItem) {
            return AntarchySettings.ultimateArmorComesEnchanted()
                    && level == 5
                    && (enchantment == Enchantments.ALL_DAMAGE_PROTECTION
                    || enchantment == Enchantments.FIRE_PROTECTION
                    || enchantment == Enchantments.PROJECTILE_PROTECTION
                    || enchantment == Enchantments.BLAST_PROTECTION);
        }

        if (stack.getItem() instanceof UltimateBowItem) {
            return AntarchySettings.ultimateBowComesEnchantedWithFlame()
                    && level == 1
                    && enchantment == Enchantments.FLAMING_ARROWS;
        }

        if (stack.getItem() instanceof KrakensGraspItem) {
            int desiredLevel = Mth.clamp(AntarchySettings.krakensGraspInnateLoyaltyLevel(), 1, 3);
            return AntarchySettings.krakensGraspInnateLoyalty()
                    && level == desiredLevel
                    && enchantment == Enchantments.LOYALTY;
        }

        return false;
    }
}
