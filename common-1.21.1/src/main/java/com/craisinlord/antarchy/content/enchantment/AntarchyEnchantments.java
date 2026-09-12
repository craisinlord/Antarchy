package com.craisinlord.antarchy.content.enchantment;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class AntarchyEnchantments {
    public static final ResourceKey<Enchantment> FEATHER_RISING = key("feather_rising");
    public static final ResourceKey<Enchantment> CHRONOSPHERE = key("chronosphere");

    private AntarchyEnchantments() {
    }

    public static int featherRisingLevel(LivingEntity entity) {
        return level(entity, EquipmentSlot.FEET, FEATHER_RISING);
    }

    public static int chronosphereLevel(LivingEntity entity) {
        return level(entity, EquipmentSlot.CHEST, CHRONOSPHERE);
    }

    public static int level(LivingEntity entity, EquipmentSlot slot, ResourceKey<Enchantment> key) {
        var enchantment = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
        return enchantment.map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, entity.getItemBySlot(slot))).orElse(0);
    }

    public static void ensure(ItemStack stack, HolderLookup.Provider registries, ResourceKey<Enchantment> key, int level) {
        var enchantment = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(stack.getEnchantments());
        if (enchantments.getLevel(enchantment) < level) {
            enchantments.set(enchantment, level);
            EnchantmentHelper.setEnchantments(stack, enchantments.toImmutable().withTooltip(true));
        }
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
