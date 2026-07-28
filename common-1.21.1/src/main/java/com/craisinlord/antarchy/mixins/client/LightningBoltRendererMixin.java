package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.content.client.ThoraxisWeatherClientState;
import com.craisinlord.antarchy.content.weather.ThoraxisWeatherKind;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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

    @ModifyArgs(
            method = "quad",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;setColor(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private static void antarchy$tintLightning(Args args) {
        if (!Boolean.TRUE.equals(antarchy$redLightning.get())) {
            return;
        }

        args.set(0, 0.95F);
        args.set(1, 0.12F);
        args.set(2, 0.12F);
        args.set(3, 0.35F);
    }
}
