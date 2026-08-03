package com.craisinlord.antarchy.content.item;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.ultimate.UltimateArmorItem;
import com.craisinlord.antarchy.content.item.ultimate.UltimateBowItem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class InnateEnchantmentHelper {
    private InnateEnchantmentHelper() {
    }

    public static boolean hasInnateNonCurseEnchantments(ItemStack stack) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!enchantment.is(EnchantmentTags.CURSE) && isInnateEnchantment(stack, enchantment, entry.getIntValue())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRemovableNonCurseEnchantments(ItemStack stack) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet()) {
            Holder<Enchantment> enchantment = entry.getKey();
            if (!enchantment.is(EnchantmentTags.CURSE) && !isInnateEnchantment(stack, enchantment, entry.getIntValue())) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack stripOnlyRemovableNonCurseEnchantments(ItemStack stack) {
        EnchantmentHelper.updateEnchantments(stack, mutable ->
                mutable.removeIf(enchantment ->
                        !enchantment.is(EnchantmentTags.CURSE)
                                && !isInnateEnchantment(stack, enchantment, mutable.getLevel(enchantment))));
        return stack;
    }

    private static boolean isInnateEnchantment(ItemStack stack, Holder<Enchantment> enchantment, int level) {
        if (level <= 0) {
            return false;
        }

        if (stack.getItem() instanceof UltimateArmorItem) {
            return AntarchySettings.ultimateArmorComesEnchanted()
                    && level == 5
                    && (enchantment.is(Enchantments.PROTECTION)
                    || enchantment.is(Enchantments.FIRE_PROTECTION)
                    || enchantment.is(Enchantments.PROJECTILE_PROTECTION)
                    || enchantment.is(Enchantments.BLAST_PROTECTION));
        }

        if (stack.getItem() instanceof UltimateBowItem) {
            return AntarchySettings.ultimateBowComesEnchantedWithFlame()
                    && level == 1
                    && enchantment.is(Enchantments.FLAME);
        }

        if (stack.getItem() instanceof KrakensGraspItem) {
            int desiredLevel = Mth.clamp(AntarchySettings.krakensGraspInnateLoyaltyLevel(), 1, 3);
            return AntarchySettings.krakensGraspInnateLoyalty()
                    && level == desiredLevel
                    && enchantment.is(Enchantments.LOYALTY);
        }

        return false;
    }
}
