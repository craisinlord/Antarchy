package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.HordeClientState;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class CavarynHordeFogMixin {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath("antarchy", "cavaryn")
    );

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void antarchy$hordeFogDistance(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
        float intensity = HordeClientState.intensity();
        if (intensity <= 0.01F || camera.getEntity() == null || !camera.getEntity().level().dimension().equals(CAVARYN) || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        float fogStart = Mth.lerp(intensity, Math.min(renderDistance * 0.55F, 48.0F), 3.5F);
        float fogEnd = Mth.lerp(intensity, Math.min(renderDistance, 128.0F), 36.0F);
        RenderSystem.setShaderFogStart(Math.min(fogStart, fogEnd * 0.65F));
        RenderSystem.setShaderFogEnd(fogEnd);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);
    }
}
