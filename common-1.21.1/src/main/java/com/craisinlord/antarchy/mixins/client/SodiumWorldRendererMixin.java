package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.SodiumCompat;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer", remap = false)
public abstract class SodiumWorldRendererMixin {
    @Inject(method = "setupTerrain", at = @At("HEAD"), remap = false)
    private void antarchy$captureTerrainSetup(Camera camera, @Coerce Object viewport, boolean spectator, boolean updateChunks, CallbackInfo ci) {
        SodiumCompat.captureTerrainSetup(camera, viewport, spectator, updateChunks);
    }
}
