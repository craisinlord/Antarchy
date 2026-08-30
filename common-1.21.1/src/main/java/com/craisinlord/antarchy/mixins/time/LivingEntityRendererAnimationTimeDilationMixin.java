package com.craisinlord.antarchy.mixins.time;

import com.craisinlord.antarchy.content.client.ClientTimeDilationTicker;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererAnimationTimeDilationMixin {
    @ModifyReturnValue(method = "getBob", at = @At("RETURN"))
    private float antarchy$slowIdleAnimation(float bob, LivingEntity entity, float partialTick) {
        return ClientTimeDilationTicker.dilateAnimationTime(entity, bob);
    }
}
