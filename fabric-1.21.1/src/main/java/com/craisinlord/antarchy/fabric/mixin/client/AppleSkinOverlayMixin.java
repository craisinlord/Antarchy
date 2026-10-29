package com.craisinlord.antarchy.fabric.mixin.client;

import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler", remap = false)
public abstract class AppleSkinOverlayMixin {
    @Inject(method = {"onPreRenderFood", "onRenderFood", "onRenderHealth"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void antarchy$hideWithRemembrance(CallbackInfo ci) {
        if (ThrowbackClientState.foodOverlaysHidden()) {
            ci.cancel();
        }
    }
}
