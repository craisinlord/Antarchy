package com.craisinlord.antarchy.mixins.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class ElythiaLightTextureMixin {
    @Unique
    private RenderPipeline antarchy$dreamPipeline;
    @Unique
    private RenderPipeline antarchy$dreamSource;
    @Unique
    private MappableRingBuffer antarchy$dreamInfo;
    @Unique
    private boolean antarchy$dreamFrame;

    @Inject(method = "render", at = @At("HEAD"))
    private void antarchy$prepareDreamShadows(LightmapRenderState state, CallbackInfo ci) {
        antarchy$dreamFrame = false;
        if (!state.needsUpdate) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (!AntarchySettings.elythiaDreamShadowsEnabled() || minecraft.level == null
                || !minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return;
        }
        float strength = (float) Mth.clamp(AntarchySettings.elythiaDreamShadowsStrength(), 0.0D, 1.0D);
        if (strength <= 0.0F) {
            return;
        }
        if (antarchy$dreamInfo == null) {
            antarchy$dreamInfo = new MappableRingBuffer(() -> "Antarchy Elythia dream shadows",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE, 16);
        }
        try (var mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(antarchy$dreamInfo.currentBuffer(), false, true)) {
            mapped.data().order(java.nio.ByteOrder.nativeOrder()).putFloat(0, strength);
        }
        antarchy$dreamFrame = true;
    }

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/renderer/RenderPipelines;LIGHTMAP:Lcom/mojang/blaze3d/pipeline/RenderPipeline;"))
    private RenderPipeline antarchy$elythiaDreamShadows(RenderPipeline original) {
        if (!antarchy$dreamFrame) {
            return original;
        }
        if (antarchy$dreamPipeline == null || antarchy$dreamSource != original) {
            RenderPipeline.Builder builder = RenderPipeline.builder()
                    .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/elythia_dream_lightmap"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/elythia_lightmap"))
                    .withCull(original.isCull())
                    .withPolygonMode(original.getPolygonMode())
                    .withDepthStencilState(java.util.Optional.ofNullable(original.getDepthStencilState()));
            builder.withVertexShader(original.getVertexShader())
                    .withVertexFormat(original.getVertexFormat(), original.getVertexFormatMode())
                    .withColorTargetState(original.getColorTargetState())
                    .withUniform("AntarchyDreamInfo", com.mojang.blaze3d.shaders.UniformType.UNIFORM_BUFFER);
            for (RenderPipeline.UniformDescription uniform : original.getUniforms()) {
                if (uniform.textureFormat() == null) {
                    builder.withUniform(uniform.name(), uniform.type());
                } else {
                    builder.withUniform(uniform.name(), uniform.type(), uniform.textureFormat());
                }
            }
            for (String sampler : original.getSamplers()) {
                builder.withSampler(sampler);
            }
            antarchy$dreamPipeline = builder.build();
            antarchy$dreamSource = original;
        }
        return antarchy$dreamPipeline;
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lcom/mojang/blaze3d/systems/RenderPass;setUniform(Ljava/lang/String;Lcom/mojang/blaze3d/buffers/GpuBuffer;)V"))
    private void antarchy$bindDreamShadows(RenderPass pass, String name, GpuBuffer buffer, Operation<Void> original) {
        original.call(pass, name, buffer);
        if (antarchy$dreamFrame) {
            pass.setUniform("AntarchyDreamInfo", antarchy$dreamInfo.currentBuffer());
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void antarchy$rotateDreamShadows(LightmapRenderState state, CallbackInfo ci) {
        if (antarchy$dreamFrame) {
            antarchy$dreamInfo.rotate();
        }
    }

    @Inject(method = "close", at = @At("TAIL"))
    private void antarchy$closeDreamShadows(CallbackInfo ci) {
        if (antarchy$dreamInfo != null) {
            antarchy$dreamInfo.close();
            antarchy$dreamInfo = null;
        }
    }
}
