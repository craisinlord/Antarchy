package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.HordeClientState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class CavarynHordeFogMixin {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath("antarchy", "cavaryn")
    );

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void antarchy$hordeFogDistance(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
                                           ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        float intensity = HordeClientState.intensity();
        if (intensity <= 0.01F || camera.entity() == null || !camera.entity().level().dimension().equals(CAVARYN) || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        FogData fog = cir.getReturnValue();
        float renderDistance = renderDistanceInChunks * 16.0F;
        float fogStart = Mth.lerp(intensity, Math.min(renderDistance * 0.55F, 48.0F), 3.5F);
        float fogEnd = Mth.lerp(intensity, Math.min(renderDistance, 128.0F), 36.0F);
        fog.environmentalStart = Math.min(fogStart, fogEnd * 0.65F);
        fog.environmentalEnd = fogEnd;
        fog.skyEnd = Math.min(fog.skyEnd, fogEnd);
        fog.cloudEnd = Math.min(fog.cloudEnd, fogEnd);
    }
}
