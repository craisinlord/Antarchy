package com.craisinlord.antarchy.forge.mixins.compat;

import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler", remap = false)
public abstract class AppleSkinOverlayMixin {
    @Inject(method = {"renderExhaustion", "renderFoodOrHealthOverlay"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private static void antarchy$hideWithRemembrance(CallbackInfo ci) {
        if (ThrowbackClientState.foodOverlaysHidden()) {
            ci.cancel();
        }
    }
}
