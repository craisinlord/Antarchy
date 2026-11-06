package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public abstract class ThoraxisBlindSkyLightMixin {
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private static void antarchy$blindSkyDarkness(DimensionType dimensionType, int lightLevel, CallbackInfoReturnable<Float> cir) {
        float darkness = ThoraxisEventClient.blindSkyDarkness();
        if (darkness <= 0.0F) {
            return;
        }
        float curve = lightLevel / 15.0F;
        curve = curve / (4.0F - 3.0F * curve);
        float ambient = dimensionType.ambientLight() * (1.0F - darkness);
        cir.setReturnValue(Math.min(cir.getReturnValueF(), Mth.lerp(ambient, curve, 1.0F)));
    }
}
