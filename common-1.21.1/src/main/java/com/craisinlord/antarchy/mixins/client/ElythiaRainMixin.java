package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LevelRenderer.class)
public abstract class ElythiaRainMixin {
    @Unique
    private static final float ANTARCHY$RAIN_SPEED = 0.3F;
    @Unique
    private static final float[] ANTARCHY$RAIN_TINT = {0.82F, 0.5F, 1.0F};

    @ModifyExpressionValue(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F", ordinal = 0)
    )
    private float antarchy$slowRain(float random) {
        if (!antarchy$purpleRain()) {
            return random;
        }
        return (3.0F + random) * ANTARCHY$RAIN_SPEED - 3.0F;
    }

    @WrapOperation(
            method = "renderSnowAndRain",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private VertexConsumer antarchy$purpleRainColor(VertexConsumer consumer, float red, float green, float blue, float alpha, Operation<VertexConsumer> original) {
        if (!antarchy$purpleRain()) {
            return original.call(consumer, red, green, blue, alpha);
        }
        return original.call(consumer, red * ANTARCHY$RAIN_TINT[0], green * ANTARCHY$RAIN_TINT[1], blue * ANTARCHY$RAIN_TINT[2], alpha);
    }

    @Unique
    private static boolean antarchy$purpleRain() {
        Minecraft minecraft = Minecraft.getInstance();
        return AntarchySettings.elythiaPurpleRain()
                && minecraft.level != null
                && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
    }
}
