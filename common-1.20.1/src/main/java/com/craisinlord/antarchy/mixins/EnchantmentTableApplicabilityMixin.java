package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantability;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentTableApplicabilityMixin {
    @Unique
    private static final ThreadLocal<Enchantment> ANTARCHY_CURRENT = new ThreadLocal<>();

    @Redirect(method = "getAvailableEnchantmentResults", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/Enchantment;isDiscoverable()Z"))
    private static boolean antarchy$trackEnchantment(Enchantment enchantment) {
        ANTARCHY_CURRENT.set(enchantment);
        return enchantment.isDiscoverable();
    }

    @Redirect(method = "getAvailableEnchantmentResults", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean antarchy$applyItemTagPrimary(EnchantmentCategory category, Item item) {
        Enchantment enchantment = ANTARCHY_CURRENT.get();
        ANTARCHY_CURRENT.remove();
        Boolean primary = AntarchyEnchantability.primary(enchantment, item);
        return primary != null ? primary : category.canEnchant(item);
    }
}
