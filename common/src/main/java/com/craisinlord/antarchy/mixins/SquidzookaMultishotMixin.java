package com.craisinlord.antarchy.mixins;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class SquidzookaMultishotMixin {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowSquidzookaForMultishot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (antarchy$isMultishot() && antarchy$isSquidzooka(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isPrimaryItem", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowSquidzookaAsPrimaryItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (antarchy$isMultishot() && antarchy$isSquidzooka(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean antarchy$isMultishot() {
        return "Multishot".equals(((Enchantment) (Object) this).description().getString());
    }

    @Unique
    private boolean antarchy$isSquidzooka(ItemStack stack) {
        return "squidzooka".equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
    }
}
