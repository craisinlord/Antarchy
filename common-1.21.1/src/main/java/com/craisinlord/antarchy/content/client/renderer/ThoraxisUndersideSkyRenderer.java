package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import java.util.Random;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class ThoraxisUndersideSkyRenderer {
    public static final ResourceKey<Level> THORAXIS = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis")
    );
    private static final ResourceLocation SUN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final float SKY_RADIUS = 100.0F;
    private static final float BODY_SIZE = 32.0F;
    private static final float SKY_RED = 0.015F;
    private static final float SKY_GREEN = 0.0F;
    private static final float SKY_BLUE = 0.035F;

    @org.jetbrains.annotations.Nullable
    private static VertexBuffer redStarBuffer = null;

    private ThoraxisUndersideSkyRenderer() {
    }

    public static boolean shouldRender(ClientLevel level, Camera camera) {
        return level.dimension().equals(THORAXIS) && camera.getPosition().y < ThoraxisUndersideManager.GRAVITY_FLIP_Y;
    }

    public static void render(
            Minecraft minecraft,
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            float partialTick,
            Camera camera,
            boolean isFoggy,
            Runnable setupFog,
            VertexBuffer skyBuffer
    ) {
        setupFog.run();
        if (minecraft.options.hideLightningFlash().get() || minecraft.gameRenderer.getMainCamera() != camera) {
            return;
        }

        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0F);
        skyBuffer.bind();
        skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float timeOfDay = 0.72F + partialTick * 0.0F;
        renderCelestialBodies(poseStack.last().pose(), projectionMatrix, timeOfDay);
        renderRedStars(poseStack.last().pose(), projectionMatrix);

        RenderSystem.depthMask(true);
    }

    private static void renderRedStars(Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        if (redStarBuffer == null) {
            redStarBuffer = buildRedStarBuffer();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(0.95F, 0.95F, 0.95F, 0.95F);
        redStarBuffer.bind();
        redStarBuffer.drawWithShader(modelViewMatrix, projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static VertexBuffer buildRedStarBuffer() {
        Random random = new Random(552031L);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < 1800; i++) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double size = 0.13F + random.nextFloat() * 0.12F;
            double lenSq = x * x + y * y + z * z;
            if (lenSq < 1.0D && lenSq > 0.01D) {
                double len = 1.0D / Math.sqrt(lenSq);
                x *= len;
                y *= len;
                z *= len;
                double sx = x * 100.0D;
                double sy = y * 100.0D;
                double sz = z * 100.0D;
                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double rot = random.nextDouble() * Math.PI * 2.0D;
                double sinRot = Math.sin(rot);
                double cosRot = Math.cos(rot);
                float red = 0.75F + random.nextFloat() * 0.25F;
                float green = 0.02F + random.nextFloat() * 0.05F;
                float blue = 0.04F + random.nextFloat() * 0.08F;

                for (int j = 0; j < 4; j++) {
                    double u = (j & 2) - 1;
                    double v = (j + 1 & 2) - 1;
                    double px = u * cosRot - v * sinRot;
                    double py = v * cosRot + u * sinRot;
                    double qx = px * sinPitch;
                    double qy = -px * cosPitch;
                    double rx = qy * sinYaw - py * cosYaw;
                    double ry = py * sinYaw + qy * cosYaw;
                    builder.addVertex((float) (sx + rx * size), (float) (sy + qx * size), (float) (sz + ry * size))
                            .setColor(red, green, blue, 1.0F);
                }
            }
        }

        MeshData mesh = builder.buildOrThrow();
        VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffer.bind();
        buffer.upload(mesh);
        VertexBuffer.unbind();
        return buffer;
    }

    private static void renderCelestialBodies(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float timeOfDay) {
        float t = timeOfDay * 360.0F;
        renderBodyGlow(modelViewMatrix, projectionMatrix, t, -10.0F, 0xFF1F18, 86.0F);
        renderTexturedBody(modelViewMatrix, projectionMatrix, t, -10.0F, 42.0F, 0xFF1F18, SUN_TEXTURE, false, 0, t * 0.6F);

        float moonOrbit = t + 180.0F;
        renderBodyGlow(modelViewMatrix, projectionMatrix, moonOrbit, 24.0F, 0x8F30FF, 76.0F);
        renderTexturedBody(modelViewMatrix, projectionMatrix, moonOrbit, 24.0F, 26.0F, 0x8F30FF, MOON_TEXTURE, true, 0, -t * 0.35F);
    }

    private static void renderBodyGlow(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float orbitDegrees, float yawDegrees, int rgb, float glowRadius) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelViewMatrix);
        modelViewStack.rotateX((float) Math.toRadians(orbitDegrees));
        modelViewStack.rotateZ((float) Math.toRadians(yawDegrees));
        RenderSystem.applyModelViewMatrix();

        float red = ((rgb >> 16) & 0xFF) / 255.0F;
        float green = ((rgb >> 8) & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(0.0F, SKY_RADIUS, 0.0F).setColor(red, green, blue, 0.55F);
        for (int i = 0; i <= 24; i++) {
            float angle = i * Mth.TWO_PI / 24.0F;
            buffer.addVertex(Mth.sin(angle) * glowRadius, SKY_RADIUS, Mth.cos(angle) * glowRadius).setColor(red, green, blue, 0.0F);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderTexturedBody(
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            float orbitDegrees,
            float yawDegrees,
            float radius,
            int rgb,
            ResourceLocation texture,
            boolean usePhaseSheet,
            int phaseIndex,
            float selfRotationDegrees
    ) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelViewMatrix);
        modelViewStack.rotateX((float) Math.toRadians(orbitDegrees));
        modelViewStack.rotateZ((float) Math.toRadians(yawDegrees));
        modelViewStack.rotateY((float) Math.toRadians(selfRotationDegrees));
        RenderSystem.applyModelViewMatrix();

        float halfSize = BODY_SIZE * 0.5F * (radius / 22.0F);
        float minU = 0.0F;
        float maxU = 1.0F;
        float minV = 0.0F;
        float maxV = 1.0F;
        if (usePhaseSheet) {
            int phaseX = phaseIndex % 4;
            int phaseY = phaseIndex / 4;
            minU = phaseX / 4.0F;
            maxU = (phaseX + 1) / 4.0F;
            minV = phaseY / 2.0F;
            maxV = (phaseY + 1) / 2.0F;
        }

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(-halfSize, SKY_RADIUS, -halfSize).setUv(minU, minV);
        buffer.addVertex(-halfSize, SKY_RADIUS, halfSize).setUv(minU, maxV);
        buffer.addVertex(halfSize, SKY_RADIUS, halfSize).setUv(maxU, maxV);
        buffer.addVertex(halfSize, SKY_RADIUS, -halfSize).setUv(maxU, minV);
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
