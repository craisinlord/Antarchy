package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.ThoraxisWeatherClientState;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightningBoltRenderer.class)
public abstract class LightningBoltRendererMixin {
    @Unique
    private static final ThreadLocal<Boolean> antarchy$redLightning = ThreadLocal.withInitial(() -> false);

    @Inject(method = "render", at = @At("HEAD"))
    private void antarchy$markRedLightning(LightningBolt bolt, float entityYaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        ThoraxisWeatherClientState.ThoraxisWeatherState weather = ThoraxisWeatherClientState.current(bolt.level());
        antarchy$redLightning.set(weather != null
                && weather.kind() == ThoraxisWeatherKind.INVERSION_STORM
                && weather.anchor().closerToCenterThan(bolt.position(), 64.0D));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void antarchy$clearRedLightning(LightningBolt bolt, float entityYaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        antarchy$redLightning.remove();
    }

    @Redirect(
            method = "quad",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private static VertexConsumer antarchy$tintLightning(VertexConsumer consumer, float red, float green, float blue, float alpha) {
        if (Boolean.TRUE.equals(antarchy$redLightning.get())) {
            return consumer.color(0.95F, 0.12F, 0.12F, 0.35F);
        }

        return consumer.color(red, green, blue, alpha);
    }
}
