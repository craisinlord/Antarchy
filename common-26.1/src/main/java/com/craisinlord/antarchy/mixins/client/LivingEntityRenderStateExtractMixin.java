package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.AntarchyLivingRenderStateHooks;
import com.craisinlord.antarchy.content.client.renderer.GlimmeringLivingLayer;
import com.craisinlord.antarchy.content.client.renderer.RoyalIndicatorRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRenderStateExtractMixin {
    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void antarchy$extractAntarchyState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        AntarchyLivingRenderStateHooks.extract((LivingEntityRenderer) (Object) this, entity, state, partialTick);
        RoyalIndicatorRenderer.extractIndicatorState(entity, state);
        GlimmeringLivingLayer.extractRenderData(entity, state);
    }
}
