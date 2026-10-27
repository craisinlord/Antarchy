package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import com.craisinlord.antarchy.content.util.AntarchyLivingRenderStateAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class HoverboardRiderPoseMixin {
    private static final float HOVERBOARD_RIDER_RENDER_YAW = -90.0F;

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void antarchy$captureHoverboardRider(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        ((AntarchyLivingRenderStateAccess) state).antarchy$setOnHoverboard(entity.getVehicle() instanceof HoverboardEntity);
    }

    @Inject(
            method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V",
            at = @At("TAIL")
    )
    private void antarchy$rotateHoverboardRider(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float scale, CallbackInfo ci) {
        if (((AntarchyLivingRenderStateAccess) state).antarchy$isOnHoverboard()) {
            poseStack.rotate(Axis.YP.rotationDegrees(HOVERBOARD_RIDER_RENDER_YAW));
        }
    }
}
