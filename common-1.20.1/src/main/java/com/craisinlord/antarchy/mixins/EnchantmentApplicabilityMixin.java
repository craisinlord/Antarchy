package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.enchantment.AntarchyEnchantability;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentApplicabilityMixin {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void antarchy$applyItemTagSupport(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Boolean supported = AntarchyEnchantability.supports((Enchantment) (Object) this, stack.getItem());
        if (supported != null) {
            cir.setReturnValue(supported);
        }
    }

    @Inject(method = "canApplyAtEnchantingTable", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void antarchy$applyItemTagPrimary(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Boolean primary = AntarchyEnchantability.primary((Enchantment) (Object) this, stack.getItem());
        if (primary != null) {
            cir.setReturnValue(primary);
        }
    }
}
