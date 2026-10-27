package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.renderer.ThrowbackDreamRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class ThrowbackGameRendererMixin {
    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void antarchy$throwbackDreamPass(DeltaTracker deltaTracker, CallbackInfo ci) {
        ThrowbackDreamRenderer.render(deltaTracker);
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void antarchy$throwbackDreamResize(int width, int height, CallbackInfo ci) {
        ThrowbackDreamRenderer.resize(width, height);
    }

    @Inject(method = "reloadShaders", at = @At("TAIL"))
    private void antarchy$throwbackDreamReload(ResourceProvider resourceProvider, CallbackInfo ci) {
        ThrowbackDreamRenderer.reset();
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void antarchy$throwbackDreamClose(CallbackInfo ci) {
        ThrowbackDreamRenderer.reset();
    }
}
