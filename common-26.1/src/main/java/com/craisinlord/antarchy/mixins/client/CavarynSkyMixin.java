package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.CavarynSkyRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class CavarynSkyMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "addCloudsPass*", at = @At("HEAD"), cancellable = true)
    private void antarchy$skipCavarynClouds(CallbackInfo ci) {
        if (this.level != null && CavarynSkyRenderer.shouldRender(this.level)) {
            ci.cancel();
        }
    }
}
