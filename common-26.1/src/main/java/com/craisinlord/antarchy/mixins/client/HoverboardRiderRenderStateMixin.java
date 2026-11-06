package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidMobRenderer.class)
public abstract class HoverboardRiderRenderStateMixin {
    @Inject(method = "extractHumanoidRenderState", at = @At("TAIL"))
    private static void antarchy$standOnHoverboard(LivingEntity entity, HumanoidRenderState state, float partialTick, ItemModelResolver itemModelResolver, CallbackInfo ci) {
        if (entity.getVehicle() instanceof HoverboardEntity) {
            state.isPassenger = false;
        }
    }
}
