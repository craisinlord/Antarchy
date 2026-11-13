package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.content.effect.SaucedMeals;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodProperties.class)
public abstract class SaucedMealMixin {
    @Inject(
            method = "onConsume(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/component/Consumable;)V",
            at = @At("RETURN")
    )
    private void antarchy$applySaucedBonus(Level level, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo ci) {
        if (user instanceof Player player && !level.isClientSide()) {
            SaucedMeals.onMealEaten(player, stack, (FoodProperties) (Object) this);
        }
    }
}
