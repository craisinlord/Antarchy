package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ElythiaEventClient;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class ElythiaRainMixin {
    @Unique
    private static final float ANTARCHY$RAIN_SPEED = 0.3F;
    @Unique
    private static final float[] ANTARCHY$RAIN_TINT = {0.9F, 0.62F, 1.0F};
    @Unique
    private static final float ANTARCHY$RAIN_ALPHA = 0.5F;
    @Unique
    private static final float[] ANTARCHY$LUMEN_TINT = {0.35F, 1.0F, 1.0F};
    @Unique
    private static final float ANTARCHY$LUMEN_ALPHA = 0.8F;
    @Unique
    private static boolean antarchy$purpleRainFrame;
    @Unique
    private static final float[] ANTARCHY$FRAME_TINT = new float[4];

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void antarchy$prepareRain(CallbackInfo ci) {
        antarchy$purpleRainFrame = antarchy$purpleRain();
        if (!antarchy$purpleRainFrame) {
            return;
        }
        float lumen = ElythiaEventClient.strength(ElythiaEvent.LUMEN_RAIN, 1.0F);
        ANTARCHY$FRAME_TINT[0] = Mth.lerp(lumen, ANTARCHY$RAIN_TINT[0], ANTARCHY$LUMEN_TINT[0]);
        ANTARCHY$FRAME_TINT[1] = Mth.lerp(lumen, ANTARCHY$RAIN_TINT[1], ANTARCHY$LUMEN_TINT[1]);
        ANTARCHY$FRAME_TINT[2] = Mth.lerp(lumen, ANTARCHY$RAIN_TINT[2], ANTARCHY$LUMEN_TINT[2]);
        ANTARCHY$FRAME_TINT[3] = Mth.lerp(lumen, ANTARCHY$RAIN_ALPHA, ANTARCHY$LUMEN_ALPHA);
    }

    @ModifyExpressionValue(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 0)
    )
    private float antarchy$slowRain(float random) {
        if (!antarchy$purpleRainFrame) {
            return random;
        }
        return (3.0F + random) * ANTARCHY$RAIN_SPEED - 3.0F;
    }

    @WrapOperation(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer antarchy$purpleRainColor(VertexConsumer consumer, float red, float green, float blue, float alpha, Operation<VertexConsumer> original) {
        if (!antarchy$purpleRainFrame) {
            return original.call(consumer, red, green, blue, alpha);
        }
        return original.call(consumer, red * ANTARCHY$FRAME_TINT[0], green * ANTARCHY$FRAME_TINT[1],
                blue * ANTARCHY$FRAME_TINT[2], alpha * ANTARCHY$FRAME_TINT[3]);
    }

    @WrapOperation(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setLight(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer antarchy$glowingRain(VertexConsumer consumer, int light, Operation<VertexConsumer> original) {
        return original.call(consumer, antarchy$purpleRainFrame ? LightTexture.FULL_BRIGHT : light);
    }

    @ModifyArg(
            method = "tickRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"),
            index = 0
    )
    private ParticleOptions antarchy$lumenSplash(ParticleOptions particle) {
        if (particle == ParticleTypes.RAIN && ElythiaEventClient.isShown(ElythiaEvent.LUMEN_RAIN) && antarchy$inElythia()) {
            return ParticleTypes.GLOW;
        }
        return particle;
    }

    @Unique
    private static boolean antarchy$purpleRain() {
        return (AntarchySettings.elythiaPurpleRain() || ElythiaEventClient.isShown(ElythiaEvent.LUMEN_RAIN)) && antarchy$inElythia();
    }

    @Unique
    private static boolean antarchy$inElythia() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
    }
}
