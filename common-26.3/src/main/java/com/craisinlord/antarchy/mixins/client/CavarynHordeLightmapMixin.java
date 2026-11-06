package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.HordeClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class CavarynHordeLightmapMixin {
    @Unique
    private static final ResourceKey<Level> ANTARCHY$CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );

    @Inject(method = "extract", at = @At("TAIL"))
    private void antarchy$hordeAmbientLight(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        if (!renderState.needsUpdate) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float intensity = HordeClientState.intensity();
        if (intensity <= 0.01F || minecraft.level == null || !minecraft.level.dimension().equals(ANTARCHY$CAVARYN)) {
            return;
        }

        float hordeAmbient = 0.015F + 0.55F * intensity;
        Vector3fc ambient = renderState.ambientColor;
        renderState.ambientColor = new Vector3f(
                Mth.clamp(ambient.x() + hordeAmbient * (1.0F - ambient.x()), 0.0F, 1.0F),
                Mth.clamp(ambient.y() + hordeAmbient * (1.0F - ambient.y()), 0.0F, 1.0F),
                Mth.clamp(ambient.z() + hordeAmbient * (1.0F - ambient.z()), 0.0F, 1.0F)
        );
    }
}
