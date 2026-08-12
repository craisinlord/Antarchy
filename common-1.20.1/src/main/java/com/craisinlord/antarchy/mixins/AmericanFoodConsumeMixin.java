package com.craisinlord.antarchy.mixins;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class AmericanFoodConsumeMixin {
    @Inject(
            method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN")
    )
    private void antarchy$applyAmericanBonus(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide || !AntarchyObjects.AMERICAN_COMPONENT.has(stack)) {
            return;
        }

        Player self = (Player) (Object) this;
        self.getFoodData().eat(AntarchySettings.americanBonusNutrition(), (float) AntarchySettings.americanBonusSaturation());

        int duration = AntarchySettings.americanRegenerationDurationTicks();
        if (duration > 0) {
            self.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, AntarchySettings.americanRegenerationAmplifier()));
        }
    }
}
