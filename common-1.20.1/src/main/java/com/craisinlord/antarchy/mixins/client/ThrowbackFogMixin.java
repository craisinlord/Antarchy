package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class ThrowbackFogMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void antarchy$throwbackDenseFog(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
        if (!AntarchySettings.throwbackDenseFog()) {
            return;
        }
        float zone = ThrowbackClientState.zone(partialTick);
        if (zone <= 0.01F
                || camera.getEntity() == null
                || !camera.getEntity().level().dimension().equals(ThrowbackZones.ELYTHIA)
                || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }
        float originalStart = RenderSystem.getShaderFogStart();
        float originalEnd = RenderSystem.getShaderFogEnd();
        float denseEnd = Math.min(originalEnd, Mth.clamp(renderDistance * 0.4F, 48.0F, 96.0F));
        float fogEnd = Mth.lerp(zone, originalEnd, denseEnd);
        float fogStart = Mth.lerp(zone, originalStart, denseEnd * 0.25F);
        RenderSystem.setShaderFogStart(Math.min(fogStart, fogEnd * 0.6F));
        RenderSystem.setShaderFogEnd(fogEnd);
    }
}
