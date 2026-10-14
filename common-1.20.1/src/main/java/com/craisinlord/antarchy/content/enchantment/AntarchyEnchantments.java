package com.craisinlord.antarchy.content.enchantment;

import com.craisinlord.antarchy.Antarchy;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class AntarchyEnchantments {
    public static final ResourceKey<Enchantment> FEATHER_RISING = key("feather_rising");
    public static final ResourceKey<Enchantment> CHRONOSPHERE = key("chronosphere");
    private static Supplier<? extends Enchantment> featherRising = () -> null;
    private static Supplier<? extends Enchantment> chronosphere = () -> null;

    private AntarchyEnchantments() {
    }

    public static void bind(Supplier<? extends Enchantment> feather, Supplier<? extends Enchantment> chrono) {
        featherRising = feather;
        chronosphere = chrono;
    }

    public static Enchantment featherRising() {
        return featherRising.get();
    }

    public static Enchantment chronosphere() {
        return chronosphere.get();
    }

    public static int featherRisingLevel(LivingEntity entity) {
        return level(entity, EquipmentSlot.FEET, featherRising());
    }

    public static int chronosphereLevel(LivingEntity entity) {
        return level(entity, EquipmentSlot.CHEST, chronosphere());
    }

    public static int level(LivingEntity entity, EquipmentSlot slot, Enchantment enchantment) {
        return enchantment == null ? 0 : EnchantmentHelper.getItemEnchantmentLevel(enchantment, entity.getItemBySlot(slot));
    }

    public static void ensure(ItemStack stack, Enchantment enchantment, int level) {
        if (enchantment == null) {
            return;
        }
        Map<Enchantment, Integer> enchantments = new java.util.LinkedHashMap<>(EnchantmentHelper.getEnchantments(stack));
        if (enchantments.getOrDefault(enchantment, 0) < level) {
            enchantments.put(enchantment, level);
            EnchantmentHelper.setEnchantments(enchantments, stack);
        }
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, new ResourceLocation(Antarchy.MODID, path));
    }
}
