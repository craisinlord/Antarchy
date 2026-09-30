package com.craisinlord.antarchy.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererPortalSceneInvoker {
    @Invoker("setupRender")
    void antarchy$setupPortalScene(Camera camera, Frustum frustum, boolean capturedFrustum, boolean spectator);

    @Invoker("compileSections")
    void antarchy$compilePortalSections(Camera camera);

    @Invoker("renderSectionLayer")
    void antarchy$renderPortalSectionLayer(RenderType renderType, double cameraX, double cameraY, double cameraZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix);

    @Invoker("renderEntity")
    void antarchy$renderPortalEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource);

    @Invoker("renderSnowAndRain")
    void antarchy$renderPortalWeather(LightTexture lightTexture, float partialTick, double cameraX, double cameraY, double cameraZ);

    @Invoker("renderWorldBorder")
    void antarchy$renderPortalWorldBorder(Camera camera);
}
