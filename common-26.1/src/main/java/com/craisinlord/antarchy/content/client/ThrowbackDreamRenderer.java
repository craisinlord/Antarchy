package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.mixins.client.PostChainAccessor;
import com.craisinlord.antarchy.mixins.client.PostPassUniformAccessor;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.nio.ByteOrder;
import java.util.Map;

public final class ThrowbackDreamRenderer {
    private static final Identifier DREAM_EFFECT = Identifier.fromNamespaceAndPath("antarchy", "throwback_dream");
    private static final Identifier DREAM_NO_AFTERIMAGE_EFFECT = Identifier.fromNamespaceAndPath("antarchy", "throwback_dream_no_afterimage");
    private static final Identifier TINT_EFFECT = Identifier.fromNamespaceAndPath("antarchy", "throwback_tint");
    private static final float ACTIVE_THRESHOLD = 0.01F;
    private static final float BASELINE_SATURATION = 0.12F;
    private static final float ZONE_SATURATION = 0.4F;
    private static final float AFTERIMAGE_WEIGHT = 0.1F;
    private static final long TIME_WRAP_TICKS = 72000L;
    private static final int IRIS_RECHECK_FRAMES = 60;
    private static Boolean irisPresent;
    private static boolean shaderPackActive;
    private static int framesUntilIrisCheck;

    private ThrowbackDreamRenderer() {
    }

    public static void process(RenderTarget target, CrossFrameResourcePool resourcePool) {
        Selection selection = selection();
        if (selection == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        PostChain chain = minecraft.getShaderManager().getPostChain(selection.effect(), LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return;
        }
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float zone = selection.zone();
        float baseline = selection.baseline();
        float wake = selection.wake();
        float prism = selection.prism();
        if (selection.dreaming()) {
            float distortion = minecraft.options.screenEffectScale().get().floatValue();
            float time = ((minecraft.level.getGameTime() % TIME_WRAP_TICKS) + partialTick) / 20.0F;
            float ghost = AntarchySettings.throwbackAfterimages() && distortion > 0.0F ? AFTERIMAGE_WEIGHT * zone * distortion : 0.0F;
            updateUniform(chain, "ThrowbackDreamConfig", baseline, wake, zone, distortion, time, ghost,
                    1.0F + BASELINE_SATURATION * baseline + ZONE_SATURATION * zone);
        } else {
            updateUniform(chain, "ThrowbackTintConfig", baseline, wake, prism);
        }
        chain.process(target, resourcePool);
    }

    private static void updateUniform(PostChain chain, String block, float... values) {
        for (PostPass pass : ((PostChainAccessor) chain).antarchy$passes()) {
            Map<String, GpuBuffer> uniforms = ((PostPassUniformAccessor) pass).antarchy$customUniforms();
            GpuBuffer buffer = uniforms.get(block);
            if (buffer == null) {
                continue;
            }
            try (var mapped = RenderSystem.getDevice().createCommandEncoder().mapBuffer(buffer, false, true)) {
                var data = mapped.data().order(ByteOrder.nativeOrder());
                for (int index = 0; index < values.length; index++) {
                    data.putFloat(index * Float.BYTES, values[index]);
                }
            }
        }
    }

    private static Selection selection() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean haze = AntarchySettings.throwbackDreamHaze();
        float zone = haze ? ThrowbackClientState.zone(partialTick) * dreamStrength() : 0.0F;
        float baseline = haze ? ThrowbackClientState.baseline(partialTick) * Mth.clamp(zone, 0.0F, 1.0F) : 0.0F;
        float wake = AntTransitClientState.saturation(partialTick);
        float prism = AntTransitClientState.prism(partialTick);
        boolean transit = Math.abs(wake - 1.0F) > ACTIVE_THRESHOLD || prism > ACTIVE_THRESHOLD;
        if ((zone < ACTIVE_THRESHOLD && !transit) || shaderPackActive()) {
            return null;
        }
        boolean dreaming = zone >= ACTIVE_THRESHOLD && prism <= ACTIVE_THRESHOLD;
        float distortion = dreaming ? minecraft.options.screenEffectScale().get().floatValue() : 0.0F;
        boolean afterimages = AntarchySettings.throwbackAfterimages() && distortion > 0.0F;
        Identifier effect = dreaming ? (afterimages ? DREAM_EFFECT : DREAM_NO_AFTERIMAGE_EFFECT) : TINT_EFFECT;
        return new Selection(effect, dreaming, zone, baseline, wake, prism);
    }

    private static float dreamStrength() {
        return (float) Mth.clamp(AntarchySettings.throwbackDreamStrength(), 0.0D, 1.0D);
    }

    private static boolean shaderPackActive() {
        if (framesUntilIrisCheck-- > 0) {
            return shaderPackActive;
        }
        framesUntilIrisCheck = IRIS_RECHECK_FRAMES;
        shaderPackActive = queryIris();
        return shaderPackActive;
    }

    private static boolean queryIris() {
        try {
            if (irisPresent == null) {
                irisPresent = classExists("net.irisshaders.iris.api.v0.IrisApi");
            }
            if (!irisPresent) {
                return false;
            }
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            return (Boolean) api.getMethod("isShaderPackInUse").invoke(instance);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            return false;
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, ThrowbackDreamRenderer.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException exception) {
            return false;
        }
    }

    private record Selection(Identifier effect, boolean dreaming, float zone, float baseline, float wake, float prism) {
    }
}
