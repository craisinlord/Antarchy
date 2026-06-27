package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.CavarynSkyRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class CavarynSkyMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private ClientLevel level;

    @Shadow
    @Final
    private VertexBuffer skyBuffer;

    @Shadow
    @Final
    private VertexBuffer darkBuffer;

    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void antarchy$renderCavarynSky(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
        if (this.level == null || !CavarynSkyRenderer.shouldRender(this.level)) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(modelViewMatrix);
        CavarynSkyRenderer.render(
                this.minecraft,
                this.level,
                poseStack,
                projectionMatrix,
                partialTick,
                camera,
                isFoggy,
                setupFog,
                this.skyBuffer,
                this.darkBuffer
        );
        ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    private void antarchy$skipCavarynClouds(PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        if (this.level != null && CavarynSkyRenderer.shouldRender(this.level)) {
            ci.cancel();
        }
    }
}
