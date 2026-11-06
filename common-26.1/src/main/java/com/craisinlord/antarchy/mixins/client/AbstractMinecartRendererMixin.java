package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.minecart.AntimetalMinecartAccess;
import com.craisinlord.antarchy.content.minecart.AntimetalRailHelper;
import com.geckolib.constant.dataticket.DataTicket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecartRenderer.class)
public abstract class AbstractMinecartRendererMixin {
    private static final double ANTARCHY$FLIP_PIVOT_Y = AntimetalRailHelper.CART_MODEL_PIVOT_Y;
    private static final double ANTARCHY$MODEL_DROP = 0.1875D;
    @Unique
    private static final DataTicket<Boolean> ANTARCHY$ON_ANTIMETAL_RAIL = DataTicket.create("antarchy_minecart_on_antimetal_rail", Boolean.class);

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/vehicle/minecart/AbstractMinecart;Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;F)V",
            at = @At("TAIL")
    )
    private void antarchy$captureAntimetalRail(AbstractMinecart entity, MinecartRenderState state, float partialTicks, CallbackInfo ci) {
        state.addGeckolibData(ANTARCHY$ON_ANTIMETAL_RAIL, entity instanceof AntimetalMinecartAccess access && access.antarchy$isOnAntimetalRail());
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD")
    )
    private void antarchy$flipHead(MinecartRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (antarchy$isOnAntimetalRail(state)) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -ANTARCHY$MODEL_DROP, 0.0D);
            poseStack.translate(0.0D, ANTARCHY$FLIP_PIVOT_Y, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(0.0D, -ANTARCHY$FLIP_PIVOT_Y, 0.0D);
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/MinecartRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void antarchy$flipTail(MinecartRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
        if (antarchy$isOnAntimetalRail(state)) {
            poseStack.popPose();
        }
    }

    @Unique
    private static boolean antarchy$isOnAntimetalRail(MinecartRenderState state) {
        return state.getOrDefaultGeckolibData(ANTARCHY$ON_ANTIMETAL_RAIL, Boolean.FALSE);
    }
}
