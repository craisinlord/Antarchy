package com.craisinlord.antarchy.mixins.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class ThoraxisFogMixin {
    @Shadow private static float fogRed;
    @Shadow private static float fogGreen;
    @Shadow private static float fogBlue;

    private static final ResourceLocation THORAXIS_DIMENSION =
            ResourceLocation.fromNamespaceAndPath("antarchy", "thoraxis");
    private static final float FOG_RED = 0.18F;
    private static final float FOG_GREEN = 0.02F;
    private static final float FOG_BLUE = 0.02F;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void modifyThoraxisFogColor(
            net.minecraft.client.Camera camera,
            float partialTick,
            net.minecraft.client.multiplayer.ClientLevel level,
            int renderDistance,
            float darkenWorldAmount,
            CallbackInfo ci) {
        if (!isThoraxis() || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        fogRed = FOG_RED;
        fogGreen = FOG_GREEN;
        fogBlue = FOG_BLUE;
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void modifyThoraxisFog(
            net.minecraft.client.Camera camera,
            FogRenderer.FogMode fogMode,
            float renderDistance,
            boolean isFoggy,
            float partialTick,
            CallbackInfo ci) {
        if (!isThoraxis() || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        RenderSystem.setShaderFogStart(0.5F);
        RenderSystem.setShaderFogEnd(Math.max(renderDistance, 1024.0F));
        RenderSystem.setShaderFogShape(FogShape.SPHERE);
    }

    private static boolean isThoraxis() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.dimension().location().equals(THORAXIS_DIMENSION);
    }
}
