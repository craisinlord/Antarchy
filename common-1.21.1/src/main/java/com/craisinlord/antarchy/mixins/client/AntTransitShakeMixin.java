package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class AntTransitShakeMixin {
    @Inject(method = "bobHurt", at = @At("TAIL"))
    private void antarchy$transitShake(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        float shake = AntTransitClientState.shake(partialTick);
        if (shake == 0.0F) {
            return;
        }
        float time = AntTransitClientState.time(partialTick);
        poseStack.mulPose(Axis.ZP.rotationDegrees(shake * Mth.sin(time * 2.3F)));
        poseStack.mulPose(Axis.XP.rotationDegrees(shake * 0.6F * Mth.sin(time * 3.1F + 1.0F)));
    }
}
