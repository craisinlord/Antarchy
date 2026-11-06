package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public abstract class CavarynDuskSkyMixin {
    private static final ResourceKey<Level> CAVARYN = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "cavaryn")
    );
    private static final float DUSK_SKY_DARKEN = 0.45F;

    @Inject(method = "extract", at = @At("TAIL"))
    private void antarchy$cavarynDuskSky(LightmapRenderState renderState, float partialTicks, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (renderState.needsUpdate && minecraft.level != null && minecraft.level.dimension().equals(CAVARYN)) {
            renderState.skyFactor = Math.max(renderState.skyFactor, DUSK_SKY_DARKEN);
        }
    }
}
