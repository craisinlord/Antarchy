package com.craisinlord.antarchy.mixins.client;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class ThoraxisFogMixin {
    private static final Identifier THORAXIS_DIMENSION =
            Identifier.fromNamespaceAndPath("antarchy", "thoraxis");
    private static final float FOG_RED = 0.18F;
    private static final float FOG_GREEN = 0.02F;
    private static final float FOG_BLUE = 0.02F;

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void modifyThoraxisFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
                                   ClientLevel level, CallbackInfoReturnable<FogData> cir) {
        if (!isThoraxis() || camera.getFluidInCamera() != FogType.NONE) {
            return;
        }

        FogData fog = cir.getReturnValue();
        fog.color.set(FOG_RED, FOG_GREEN, FOG_BLUE, 1.0F);
        float fogEnd = Math.max(renderDistanceInChunks * 16.0F, 1024.0F);
        fog.environmentalStart = 0.5F;
        fog.environmentalEnd = fogEnd;
        fog.renderDistanceStart = fogEnd;
        fog.renderDistanceEnd = fogEnd;
    }

    private static boolean isThoraxis() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.dimension().identifier().equals(THORAXIS_DIMENSION);
    }
}
