package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.InnateEnchantmentHelper;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {
    @Inject(method = "removeNonCursesFrom", at = @At("HEAD"), cancellable = true)
    private void antarchy$preserveInnateEnchantments(ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (InnateEnchantmentHelper.hasInnateNonCurseEnchantments(stack)) {
            cir.setReturnValue(InnateEnchantmentHelper.stripOnlyRemovableNonCurseEnchantments(stack));
        }
    }

    @Inject(method = "computeResult", at = @At("RETURN"), cancellable = true)
    private void antarchy$blockInnateOnlyNoOp(ItemStack firstInput, ItemStack secondInput, CallbackInfoReturnable<ItemStack> cir) {
        if (!firstInput.isEmpty() && secondInput.isEmpty()
                && InnateEnchantmentHelper.hasInnateNonCurseEnchantments(firstInput)
                && !InnateEnchantmentHelper.hasRemovableNonCurseEnchantments(firstInput)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        if (firstInput.isEmpty() && !secondInput.isEmpty()
                && InnateEnchantmentHelper.hasInnateNonCurseEnchantments(secondInput)
                && !InnateEnchantmentHelper.hasRemovableNonCurseEnchantments(secondInput)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
