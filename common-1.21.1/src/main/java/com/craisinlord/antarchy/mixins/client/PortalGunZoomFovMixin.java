package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class PortalGunZoomFovMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void antarchy$applyPortalGunZoom(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        cir.setReturnValue(PortalGunZoomClientState.applyFov(cir.getReturnValue(), partialTick, Minecraft.getInstance().options));
    }
}
