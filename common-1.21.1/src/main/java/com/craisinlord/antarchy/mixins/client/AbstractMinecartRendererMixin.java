package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecartRenderer.class)
public abstract class AbstractMinecartRendererMixin<T extends AbstractMinecart> {
    private static final double ANTARCHY$FLIP_PIVOT_Y = AntimetalRailHelper.CART_MODEL_PIVOT_Y;
    private static final double ANTARCHY$MODEL_DROP = 0.1875D;

    @Inject(method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void antarchy$flipHead(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof AntimetalMinecartAccess access && access.antarchy$isOnAntimetalRail()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -ANTARCHY$MODEL_DROP, 0.0D);
            poseStack.translate(0.0D, ANTARCHY$FLIP_PIVOT_Y, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(0.0D, -ANTARCHY$FLIP_PIVOT_Y, 0.0D);
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/vehicle/AbstractMinecart;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void antarchy$flipTail(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity instanceof AntimetalMinecartAccess access && access.antarchy$isOnAntimetalRail()) {
            poseStack.popPose();
        }
    }
}
