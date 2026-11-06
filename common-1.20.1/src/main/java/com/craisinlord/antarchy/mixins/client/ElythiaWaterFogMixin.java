package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class ElythiaWaterFogMixin {
    @Unique
    private static final float ANTARCHY$WATER_FOG_DISTANCE = 2.2F;
    @Unique
    private static final float ANTARCHY$TURQUOISE_WEIGHT = 0.65F;
    @Unique
    private static final float[] ANTARCHY$TURQUOISE = {0.16F, 0.62F, 0.66F};

    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void antarchy$elythiaWaterColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float bossColorModifier, CallbackInfo ci) {
        if (!antarchy$clearWater(camera)) {
            return;
        }
        fogRed = Mth.lerp(ANTARCHY$TURQUOISE_WEIGHT, fogRed, ANTARCHY$TURQUOISE[0]);
        fogGreen = Mth.lerp(ANTARCHY$TURQUOISE_WEIGHT, fogGreen, ANTARCHY$TURQUOISE[1]);
        fogBlue = Mth.lerp(ANTARCHY$TURQUOISE_WEIGHT, fogBlue, ANTARCHY$TURQUOISE[2]);
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void antarchy$elythiaWaterDistance(Camera camera, FogRenderer.FogMode mode, float renderDistance, boolean thickFog, float partialTick, CallbackInfo ci) {
        if (mode != FogRenderer.FogMode.FOG_TERRAIN || !antarchy$clearWater(camera)) {
            return;
        }
        RenderSystem.setShaderFogEnd(Math.min(RenderSystem.getShaderFogEnd() * ANTARCHY$WATER_FOG_DISTANCE, renderDistance));
    }

    @Unique
    private static boolean antarchy$clearWater(Camera camera) {
        if (!AntarchySettings.elythiaClearWater() || camera.getFluidInCamera() != FogType.WATER) {
            return false;
        }
        Entity entity = camera.getEntity();
        return entity != null && entity.level().dimension().equals(ThrowbackZones.ELYTHIA);
    }
}
