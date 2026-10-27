package com.craisinlord.antarchy.fabric.mixin;

import com.craisinlord.antarchy.content.recipe.CustomBrewingRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandCustomIngredientMixin {
    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void antarchy$canPlaceCustomBrewingItem(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot == 3) {
            if (CustomBrewingRecipes.isCustomIngredient(stack)) {
                cir.setReturnValue(true);
            }
        } else if (slot >= 0 && slot <= 2) {
            if (CustomBrewingRecipes.isCustomContainer(stack)) {
                cir.setReturnValue(true);
            }
        }
    }
}
