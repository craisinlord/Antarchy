package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class PortalGunZoomFovMixin {
    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double antarchy$applyPortalGunZoom(double fov, Camera camera, float partialTick, boolean useFovSetting) {
        return PortalGunZoomClientState.applyFov(fov, partialTick, Minecraft.getInstance().options);
    }
}
