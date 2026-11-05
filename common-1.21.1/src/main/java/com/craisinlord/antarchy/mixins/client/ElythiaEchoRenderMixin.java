package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.ElythiaEchoes;
import com.craisinlord.antarchy.content.client.renderer.AntTransitGroundRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ElythiaEchoRenderMixin {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 0)
    )
    private void antarchy$renderEchoes(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                                       LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        net.minecraft.client.renderer.culling.Frustum grassFrustum = new net.minecraft.client.renderer.culling.Frustum(frustumMatrix, projectionMatrix);
        net.minecraft.world.phys.Vec3 grassCamera = camera.getPosition();
        grassFrustum.prepare(grassCamera.x, grassCamera.y, grassCamera.z);
        com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer.render(this.renderBuffers.bufferSource(), camera, grassFrustum, partialTick);
        ElythiaEchoes.render(new PoseStack(), this.renderBuffers.bufferSource(), camera, partialTick);
        AntTransitGroundRenderer.render(new PoseStack(), this.renderBuffers.bufferSource(), camera, partialTick);
    }
}
