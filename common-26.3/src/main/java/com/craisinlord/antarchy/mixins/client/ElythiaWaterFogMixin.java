package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class ElythiaWaterFogMixin {
    @Inject(method = "setupFog", at = @At("RETURN"))
    private void antarchy$elythiaWater(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker,
                                      float darkenWorldAmount, ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        if (!AntarchySettings.elythiaClearWater() || camera.getFluidInCamera() != FogType.WATER
                || !level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return;
        }
        FogData fog = cir.getReturnValue();
        fog.color.x = Mth.lerp(0.65F, fog.color.x, 0.16F);
        fog.color.y = Mth.lerp(0.65F, fog.color.y, 0.62F);
        fog.color.z = Mth.lerp(0.65F, fog.color.z, 0.66F);
        fog.environmentalEnd = Math.min(fog.environmentalEnd * 2.2F, renderDistanceInChunks * 16.0F);
    }
}
