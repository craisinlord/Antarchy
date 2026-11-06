package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class PortalGunZoomFovMixin {
    @ModifyReturnValue(method = {"calculateFov", "calculateHudFov"}, at = @At("RETURN"))
    private float antarchy$applyPortalGunZoom(float fov, float partialTick) {
        return (float) PortalGunZoomClientState.applyFov(fov, partialTick, Minecraft.getInstance().options);
    }
}
