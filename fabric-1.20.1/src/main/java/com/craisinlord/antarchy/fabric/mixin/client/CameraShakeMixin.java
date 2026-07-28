package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public abstract class CameraShakeMixin {
    private static final int TORETERROR_SHAKE_TICKS = 25;

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "bobView", at = @At("TAIL"))
    private void applyCameraShake(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (minecraft.level == null || minecraft.player == null) {
            CameraShakeClientState.clear();
            return;
        }

        float shakeStrength = CameraShakeClientState.getStrength(minecraft.gameRenderer.getMainCamera().getPosition());

        int beetleShakeTicks = HerculesBeetleImpactShakeClientState.getTicks();
        if (beetleShakeTicks > 0) {
            shakeStrength += (float) beetleShakeTicks / TORETERROR_SHAKE_TICKS * 2.0F;
        }

        int bigBerthaShakeTicks = BigBerthaItem.clientShakeTicks;
        if (bigBerthaShakeTicks > 0) {
            shakeStrength += (float) bigBerthaShakeTicks / TORETERROR_SHAKE_TICKS * 2.0F;
        }

        if (shakeStrength <= 0.0F) return;

        float time = (float) (minecraft.player.tickCount + partialTick);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(time * 1.5F) * shakeStrength * 2.5F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(time * 1.8F) * shakeStrength * 2.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(time * 2.2F) * shakeStrength * 1.5F));
    }
}
