package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.AntDescentClientState;
import com.craisinlord.antarchy.content.client.ThrowbackClientState;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ThrowbackDreamRenderer {
    private static final float ACTIVE_THRESHOLD = 0.01F;
    private static final float BASELINE_SATURATION = 0.12F;
    private static final float ZONE_SATURATION = 0.4F;
    private static final float AFTERIMAGE_WEIGHT = 0.1F;
    private static final long TIME_WRAP_TICKS = 72000L;
    private static final int IRIS_RECHECK_FRAMES = 60;

    private static final Chain TINT = new Chain(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "shaders/post/throwback_tint.json"));
    private static final Chain DREAM = new Chain(ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "shaders/post/throwback_dream.json"));

    private static Boolean irisPresent;
    private static boolean shaderPackActive;
    private static int framesUntilIrisCheck;

    private ThrowbackDreamRenderer() {
    }

    public static void render(DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        boolean elythia = AntarchySettings.throwbackDreamHaze() && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
        float baseline = elythia ? ThrowbackClientState.baseline(partialTick) : 0.0F;
        float zone = elythia ? ThrowbackClientState.zone(partialTick) * dreamStrength() : 0.0F;
        float wake = AntDescentClientState.saturation(partialTick);
        boolean waking = wake < 1.0F - ACTIVE_THRESHOLD;
        if ((baseline < ACTIVE_THRESHOLD && zone < ACTIVE_THRESHOLD && !waking) || shaderPackActive()) {
            return;
        }
        boolean dreaming = zone >= ACTIVE_THRESHOLD;
        PostChain postChain = (dreaming ? DREAM : TINT).get(minecraft);
        if (postChain == null) {
            return;
        }
        postChain.setUniform("Baseline", baseline);
        postChain.setUniform("Wake", wake);
        if (dreaming) {
            float distortion = minecraft.options.screenEffectScale().get().floatValue();
            float time = ((minecraft.level.getGameTime() % TIME_WRAP_TICKS) + partialTick) / 20.0F;
            float ghost = AntarchySettings.throwbackAfterimages() ? AFTERIMAGE_WEIGHT * zone * distortion : 0.0F;
            postChain.setUniform("Zone", zone);
            postChain.setUniform("Distortion", distortion);
            postChain.setUniform("DreamTime", time);
            postChain.setUniform("Ghost", ghost);
            postChain.setUniform("Saturation", 1.0F + BASELINE_SATURATION * baseline + ZONE_SATURATION * zone);
        }
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.resetTextureMatrix();
        postChain.process(deltaTracker.getGameTimeDeltaTicks());
        minecraft.getMainRenderTarget().bindWrite(true);
    }

    public static void resize(int width, int height) {
        TINT.resize(width, height);
        DREAM.resize(width, height);
    }

    public static void reset() {
        TINT.close();
        DREAM.close();
        irisPresent = null;
        framesUntilIrisCheck = 0;
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

    private static final class Chain {
        private final ResourceLocation location;
        private PostChain chain;
        private boolean failed;
        private int width = -1;
        private int height = -1;

        private Chain(ResourceLocation location) {
            this.location = location;
        }

        private PostChain get(Minecraft minecraft) {
            if (this.failed) {
                return null;
            }
            RenderTarget main = minecraft.getMainRenderTarget();
            if (this.chain == null) {
                try {
                    this.chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), main, this.location);
                } catch (Exception exception) {
                    this.failed = true;
                    Antarchy.LOGGER.warn("Failed to load Throwback Zone shader {}", this.location, exception);
                    return null;
                }
                this.width = -1;
            }
            if (main.width != this.width || main.height != this.height) {
                this.resize(main.width, main.height);
            }
            return this.chain;
        }

        private void resize(int width, int height) {
            if (this.chain != null) {
                this.chain.resize(width, height);
                this.width = width;
                this.height = height;
            }
        }

        private void close() {
            if (this.chain != null) {
                this.chain.close();
                this.chain = null;
            }
            this.failed = false;
            this.width = -1;
            this.height = -1;
        }
    }
}
