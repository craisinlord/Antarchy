package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class SquidzookaMultishotMixin {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    private void antarchy$allowSquidzookaForMultishot(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (antarchy$isMultishot() && antarchy$isMultishotEnchantable(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean antarchy$isMultishot() {
        return (Enchantment) (Object) this == Enchantments.MULTISHOT;
    }

    @Unique
    private boolean antarchy$isMultishotEnchantable(ItemStack stack) {
        return stack.is(AntarchyTags.Items.MULTISHOT_ENCHANTABLE);
    }
}
