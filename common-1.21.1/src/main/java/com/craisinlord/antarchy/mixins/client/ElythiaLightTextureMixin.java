package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.FungalReserveAtmosphere;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightTexture.class)
public abstract class ElythiaLightTextureMixin {
    @Unique
    private static final float[] ANTARCHY$SHADOW_HUE = {1.30F, 0.78F, 2.05F};
    @Unique
    private static final float[] ANTARCHY$SHADOW_FLOOR = {0.070F, 0.035F, 0.140F};
    @Unique
    private static final float ANTARCHY$HUE_WEIGHT = 1.0F;

    @WrapOperation(
            method = "updateLightTexture",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V")
    )
    private void antarchy$elythiaDreamShadows(NativeImage image, int x, int y, int color, Operation<Void> original) {
        original.call(image, x, y, antarchy$tint(color, x));
    }

    @Unique
    private static int antarchy$tint(int color, int blockLight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return color;
        }
        int shaded = AntarchySettings.elythiaDreamShadowsEnabled() ? antarchy$dreamShadow(color, blockLight) : color;
        return antarchy$dim(shaded, FungalReserveAtmosphere.lightScale(blockLight));
    }

    @Unique
    private static int antarchy$dim(int color, float scale) {
        if (scale >= 1.0F) {
            return color;
        }
        int packed = color & 0xFF000000;
        for (int channel = 0; channel < 3; channel++) {
            int value = color >> (channel * 8) & 0xFF;
            packed |= Math.round(value * scale) << (channel * 8);
        }
        return packed;
    }

    @Unique
    private static int antarchy$dreamShadow(int color, int blockLight) {
        float strength = (float) Mth.clamp(AntarchySettings.elythiaDreamShadowsStrength(), 0.0D, 1.0D);
        if (strength <= 0.0F) {
            return color;
        }
        float[] rgb = {(color & 0xFF) / 255.0F, (color >> 8 & 0xFF) / 255.0F, (color >> 16 & 0xFF) / 255.0F};
        float luminance = 0.2126F * rgb[0] + 0.7152F * rgb[1] + 0.0722F * rgb[2];
        float darkness = 1.0F - luminance;
        float torchlight = 1.0F - blockLight / 15.0F;
        float hue = Math.min(1.0F, darkness * darkness * ANTARCHY$HUE_WEIGHT) * torchlight * strength;
        float floor = darkness * darkness * darkness * strength;
        int packed = color & 0xFF000000;
        for (int channel = 0; channel < 3; channel++) {
            float shifted = Mth.lerp(hue, rgb[channel], luminance * ANTARCHY$SHADOW_HUE[channel]);
            float value = Mth.clamp(shifted + ANTARCHY$SHADOW_FLOOR[channel] * floor, 0.0F, 1.0F);
            packed |= Math.round(value * 255.0F) << (channel * 8);
        }
        return packed;
    }
}
