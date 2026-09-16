package com.craisinlord.antarchy.content.enchantment;

import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Loader-neutral access to the 1.20 registered enchantments. */
public final class AntarchyEnchantments {
    private static Supplier<? extends Enchantment> featherRising = () -> null;
    private static Supplier<? extends Enchantment> chronosphere = () -> null;

    private AntarchyEnchantments() {}

    public static void bind(Supplier<? extends Enchantment> feather, Supplier<? extends Enchantment> chrono) {
        featherRising = feather;
        chronosphere = chrono;
    }

    public static Enchantment featherRising() { return featherRising.get(); }
    public static Enchantment chronosphere() { return chronosphere.get(); }

    public static int featherRisingLevel(LivingEntity entity) { return level(entity, EquipmentSlot.FEET, featherRising()); }
    public static int chronosphereLevel(LivingEntity entity) { return level(entity, EquipmentSlot.CHEST, chronosphere()); }

    private static int level(LivingEntity entity, EquipmentSlot slot, Enchantment enchantment) {
        return enchantment == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(enchantment, entity.getItemBySlot(slot));
    }

    public static void ensure(ItemStack stack, Enchantment enchantment, int level) {
        if (enchantment == null) return;
        Map<Enchantment, Integer> enchantments = new java.util.HashMap<>(EnchantmentHelper.getEnchantments(stack));
        if (enchantments.getOrDefault(enchantment, 0) < level) {
            enchantments.put(enchantment, level);
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
    }
}
