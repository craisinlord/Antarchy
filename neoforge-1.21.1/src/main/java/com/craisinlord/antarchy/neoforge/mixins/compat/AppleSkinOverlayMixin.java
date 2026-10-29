package com.craisinlord.antarchy.neoforge.mixins.compat;

import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "squeek.appleskin.client.HUDOverlayHandler$Overlay", remap = false)
public abstract class AppleSkinOverlayMixin {
    @Inject(method = "shouldRenderOverlay", at = @At("HEAD"), cancellable = true, remap = false)
    private void antarchy$hideWithRemembrance(CallbackInfoReturnable<Boolean> cir) {
        if (ThrowbackClientState.foodOverlaysHidden()) {
            cir.setReturnValue(false);
        }
    }
}
