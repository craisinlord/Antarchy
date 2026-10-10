package com.craisinlord.antarchy.mixins.compat.stellarview;

import com.craisinlord.antarchy.content.client.MoonSkyTracker;
import com.craisinlord.antarchy.content.client.renderer.PortalGunPortalSceneRenderer;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import net.povstalec.stellarview.common.util.SphericalCoords;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.povstalec.stellarview.client.render.space_objects.distinct.LunaRenderer", remap = false)
public abstract class StellarViewLunaDirectionMixin {
    @Inject(method = "renderTextureLayers", at = @At("HEAD"), remap = false)
    private void antarchy$recordMoonDirection(ViewCenter viewCenter, ClientLevel level, Camera camera, Tesselator tesselator,
                                              Matrix4f lastMatrix, SphericalCoords sphericalCoords, long ticks, double distance,
                                              float partialTicks, CallbackInfo ci) {
        if (PortalGunPortalSceneRenderer.isRenderingScene() || sphericalCoords == null || camera == null || lastMatrix == null) {
            return;
        }
        MoonSkyTracker.recordSphericalMoon(level, camera, lastMatrix, sphericalCoords.theta, sphericalCoords.phi);
    }
}
