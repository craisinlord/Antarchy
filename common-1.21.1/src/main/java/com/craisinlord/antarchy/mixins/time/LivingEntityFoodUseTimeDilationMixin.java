package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.time.TimeDilationApi;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFoodUseTimeDilationMixin {
    @Shadow
    protected abstract void updatingUsingItem();

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;updatingUsingItem()V"))
    private void antarchy$advanceFoodUse(LivingEntity entity) {
        ItemStack stack = entity.getUseItem();
        if (stack.isEmpty() || stack.get(DataComponents.FOOD) == null) {
            this.updatingUsingItem();
            return;
        }

        int ticks = TimeDilationApi.consumeTicks(entity, "living_food_use");
        for (int i = 0; i < ticks && entity.isUsingItem(); i++) {
            this.updatingUsingItem();
        }
    }
}
