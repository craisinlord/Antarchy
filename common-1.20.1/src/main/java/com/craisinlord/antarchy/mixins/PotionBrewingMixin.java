package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.recipe.CustomBrewingRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "hasMix", at = @At("HEAD"), cancellable = true)
    private static void antarchy$hasCustomMix(ItemStack input, ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        if (CustomBrewingRecipes.hasMix(input, ingredient)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mix", at = @At("HEAD"), cancellable = true)
    private static void antarchy$mixCustomRecipe(ItemStack ingredient, ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = CustomBrewingRecipes.getOutput(input, ingredient);
        if (!output.isEmpty()) {
            cir.setReturnValue(output);
        }
    }
}
