package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.recipe.CustomBrewingRecipes;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.BrewingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingRecipe.class)
public abstract class PotionBrewingMixin {
    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/BrewingInput;)Z", at = @At("RETURN"), cancellable = true)
    private void antarchy$rejectRedundantGlimmerAugment(BrewingInput brewingInput, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && CustomBrewingRecipes.isRedundantGlimmerAugment(brewingInput.input(), brewingInput.reagent())) {
            cir.setReturnValue(false);
        }
    }
}
