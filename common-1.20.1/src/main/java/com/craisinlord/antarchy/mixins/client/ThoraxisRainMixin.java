package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.ThoraxisRainRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ThoraxisRainMixin {
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void antarchy$renderUpwardRain(LightTexture lightTexture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        ThoraxisRainRenderer.render(partialTick, camX, camY, camZ);
    }
}
