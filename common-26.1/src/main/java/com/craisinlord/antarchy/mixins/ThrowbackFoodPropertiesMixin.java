package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.throwback.ThrowbackFoodHealing;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.class)
public abstract class ThrowbackFoodPropertiesMixin {
    @WrapOperation(method = "onConsume", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"))
    private void antarchy$throwbackFoodHeals(FoodData foodData, FoodProperties food, Operation<Void> original, @Local(argsOnly = true) LivingEntity user) {
        if (AntarchySettings.throwbackClassicFood() && user.hasEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get())) {
            if (!user.level().isClientSide() && user instanceof ThrowbackFoodHealing healing) {
                healing.antarchy$addPendingFoodHealing(food.nutrition());
            }
            return;
        }
        original.call(foodData, food);
    }
}
