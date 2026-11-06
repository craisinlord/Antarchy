package com.craisinlord.antarchy.mixins.compat.stellarview;

import com.craisinlord.antarchy.Antarchy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.povstalec.stellarview.client.render.space_objects.distinct.LunaRenderer", remap = false)
public abstract class StellarViewLunaDirectionMixin {
    @Unique
    private static boolean antarchy$reported;

    @Inject(method = "renderTextureLayers", at = @At("HEAD"), remap = false)
    private void antarchy$recordMoonDirection(CallbackInfo ci) {
        if (!antarchy$reported) {
            antarchy$reported = true;
            Antarchy.LOGGER.error("Antarchy Stellar View moon tracking is not ported to 26.1: no 26.1 Stellar View API was available at build time, so portal gun moon aiming ignores the Stellar View moon");
        }
    }
}
