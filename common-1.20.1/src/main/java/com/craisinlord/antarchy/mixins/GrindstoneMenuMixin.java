package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.item.InnateEnchantmentHelper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin {
    @Shadow @Final
    private Container repairSlots;

    @Shadow @Final
    private Container resultSlots;

    @Inject(method = "removeNonCurses(Lnet/minecraft/world/item/ItemStack;II)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void antarchy$preserveInnateEnchantments(ItemStack stack, int damage, int count, CallbackInfoReturnable<ItemStack> cir) {
        if (InnateEnchantmentHelper.hasInnateNonCurseEnchantments(stack)) {
            cir.setReturnValue(InnateEnchantmentHelper.stripOnlyRemovableNonCurseEnchantments(stack));
        }
    }

    @Inject(method = "createResult()V", at = @At("RETURN"))
    private void antarchy$blockInnateOnlyNoOp(CallbackInfo ci) {
        ItemStack firstInput = this.repairSlots.getItem(0);
        ItemStack secondInput = this.repairSlots.getItem(1);
        if (!firstInput.isEmpty() && secondInput.isEmpty()
                && InnateEnchantmentHelper.hasInnateNonCurseEnchantments(firstInput)
                && !InnateEnchantmentHelper.hasRemovableNonCurseEnchantments(firstInput)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }

        if (firstInput.isEmpty() && !secondInput.isEmpty()
                && InnateEnchantmentHelper.hasInnateNonCurseEnchantments(secondInput)
                && !InnateEnchantmentHelper.hasRemovableNonCurseEnchantments(secondInput)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }
}
