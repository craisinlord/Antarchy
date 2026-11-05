package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.joml.Matrix4f;

@Mixin(GameRenderer.class)
public abstract class AntTransitShakeMixin {
    @Inject(method = "bobHurt", at = @At("TAIL"))
    private void antarchy$transitShake(CameraRenderState state, PoseStack poseStack, CallbackInfo ci) {
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float shake = AntTransitClientState.shake(partialTick);
        if (shake != 0.0F) {
            float time = AntTransitClientState.time(partialTick);
            poseStack.mulPose(new Matrix4f().rotation(Axis.ZP.rotationDegrees(shake * Mth.sin(time * 2.3F))));
            poseStack.mulPose(new Matrix4f().rotation(Axis.XP.rotationDegrees(shake * 0.6F * Mth.sin(time * 3.1F + 1.0F))));
        }
    }
}
