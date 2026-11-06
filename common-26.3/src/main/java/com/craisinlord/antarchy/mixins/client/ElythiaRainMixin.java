package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WeatherEffectRenderer.class)
public abstract class ElythiaRainMixin {
    @Unique
    private static final float ANTARCHY$RAIN_SPEED = 0.3F;
    @Unique
    private static final float[] ANTARCHY$RAIN_TINT = {0.78F, 0.32F, 1.0F};
    @Unique
    private static final float[] ANTARCHY$SPLASH_TINT = {0.8F, 0.4F, 1.0F};
    @Unique
    private static final float ANTARCHY$RAIN_ALPHA = 0.8F;
    @Unique
    private static boolean antarchy$purpleRainFrame;

    @Inject(method = {"extractRenderState", "prepare"}, at = @At("HEAD"))
    private void antarchy$prepareRain(CallbackInfo ci) {
        antarchy$purpleRainFrame = antarchy$inElythia();
    }

    @ModifyExpressionValue(
            method = "createRainColumnInstance",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 0)
    )
    private float antarchy$slowRain(float random) {
        if (!antarchy$purpleRainFrame) {
            return random;
        }
        return (3.0F + random) * ANTARCHY$RAIN_SPEED - 3.0F;
    }

    @WrapOperation(
            method = "prepareInstances",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer antarchy$purpleRainColor(VertexConsumer consumer, int color, Operation<VertexConsumer> original) {
        if (!antarchy$purpleRainFrame) {
            return original.call(consumer, color);
        }
        int alpha = (int) ((color >>> 24 & 255) * ANTARCHY$RAIN_ALPHA);
        int red = (int) ((color >>> 16 & 255) * ANTARCHY$RAIN_TINT[0]);
        int green = (int) ((color >>> 8 & 255) * ANTARCHY$RAIN_TINT[1]);
        int blue = (int) ((color & 255) * ANTARCHY$RAIN_TINT[2]);
        return original.call(consumer, alpha << 24 | red << 16 | green << 8 | blue);
    }

    @WrapOperation(
            method = "prepareInstances",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setLight(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer antarchy$glowingRain(VertexConsumer consumer, int light, Operation<VertexConsumer> original) {
        return original.call(consumer, antarchy$purpleRainFrame ? LightCoordsUtil.FULL_BRIGHT : light);
    }

    @Unique
    private static boolean antarchy$inElythia() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
    }
}
