package com.craisinlord.antarchy.mixins.compat.stellarview;

import com.craisinlord.antarchy.content.client.MoonSkyTracker;
import com.craisinlord.anthology.portal.client.PortalSceneRenderer;
import com.mojang.blaze3d.vertex.Tesselator;
import java.lang.reflect.Field;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.povstalec.stellarview.client.render.space_objects.distinct.LunaRenderer", remap = false)
public abstract class StellarViewLunaDirectionMixin {
    @Unique
    private static Field antarchy$thetaField;
    @Unique
    private static Field antarchy$phiField;
    @Unique
    private static boolean antarchy$fieldsUnavailable;

    @Inject(method = "renderTextureLayers", at = @At("HEAD"), remap = false, require = 0)
    private void antarchy$recordMoonDirection(@Coerce Object viewCenter, ClientLevel level, Camera camera, Tesselator tesselator,
                                              Matrix4f lastMatrix, @Coerce Object sphericalCoords, long ticks, double distance,
                                              float partialTicks, CallbackInfo ci) {
        if (PortalSceneRenderer.isRenderingScene() || sphericalCoords == null || camera == null || lastMatrix == null) {
            return;
        }
        if (antarchy$fieldsUnavailable) {
            return;
        }
        try {
            if (antarchy$thetaField == null || antarchy$phiField == null) {
                antarchy$thetaField = sphericalCoords.getClass().getField("theta");
                antarchy$phiField = sphericalCoords.getClass().getField("phi");
            }
            MoonSkyTracker.recordSphericalMoon(level, camera, lastMatrix,
                    antarchy$thetaField.getDouble(sphericalCoords), antarchy$phiField.getDouble(sphericalCoords));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            antarchy$fieldsUnavailable = true;
        }
    }
}
