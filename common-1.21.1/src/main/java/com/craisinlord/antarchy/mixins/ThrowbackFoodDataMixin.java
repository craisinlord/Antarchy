package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class ThrowbackFoodDataMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void antarchy$pauseHungerInThrowbackZone(Player player, CallbackInfo ci) {
        if (AntarchySettings.throwbackClassicFood() && player.hasEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get())) {
            ci.cancel();
        }
    }
}
