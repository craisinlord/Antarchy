package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
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

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"))
    private void antarchy$prepareRain(CallbackInfo ci) {
        antarchy$purpleRainFrame = antarchy$inElythia();
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
        return original.call(consumer, red * ANTARCHY$RAIN_TINT[0], green * ANTARCHY$RAIN_TINT[1],
                blue * ANTARCHY$RAIN_TINT[2], alpha * ANTARCHY$RAIN_ALPHA);
    }

    @WrapOperation(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setLight(I)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer antarchy$glowingRain(VertexConsumer consumer, int light, Operation<VertexConsumer> original) {
        return original.call(consumer, antarchy$purpleRainFrame ? LightTexture.FULL_BRIGHT : light);
    }

    @WrapOperation(
            method = "tickRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    )
    private void antarchy$purpleSplash(ClientLevel level, ParticleOptions particle, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, Operation<Void> original) {
        if (particle != ParticleTypes.RAIN || !antarchy$inElythia()) {
            original.call(level, particle, x, y, z, xSpeed, ySpeed, zSpeed);
            return;
        }
        Particle splash = Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.GLOW, x, y, z, xSpeed, ySpeed, zSpeed);
        if (splash != null) {
            splash.setColor(ANTARCHY$SPLASH_TINT[0], ANTARCHY$SPLASH_TINT[1], ANTARCHY$SPLASH_TINT[2]);
        }
    }

    @Unique
    private static boolean antarchy$inElythia() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
    }
}
