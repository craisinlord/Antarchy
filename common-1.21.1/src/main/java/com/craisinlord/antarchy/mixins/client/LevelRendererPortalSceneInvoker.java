package com.craisinlord.antarchy.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface LevelRendererPortalSceneInvoker {
    @Invoker("renderEntity")
    void antarchy$renderPortalEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource);

    @Invoker("renderSnowAndRain")
    void antarchy$renderPortalWeather(LightTexture lightTexture, float partialTick, double cameraX, double cameraY, double cameraZ);

    @Invoker("renderWorldBorder")
    void antarchy$renderPortalWorldBorder(Camera camera);
}
