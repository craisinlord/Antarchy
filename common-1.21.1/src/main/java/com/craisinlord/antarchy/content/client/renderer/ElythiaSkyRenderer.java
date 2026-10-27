package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.ElythiaFog;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.GlStateManager;
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
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public final class ElythiaSkyRenderer {
    public static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    private static final ResourceLocation SUN_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON_TEXTURE = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final float SKY_RADIUS = 100.0F;
    private static final float BODY_SIZE = 32.0F;
    private static final float[] HORIZON_FOG_ELEVATIONS = {
            -60.0F, 0.0F, 1.0F, 2.0F, 3.5F, 5.0F, 7.0F, 9.5F, 12.5F, 16.0F, 20.0F, 25.0F, 31.0F, 38.0F, 46.0F, 55.0F, 65.0F, 78.0F
    };
    private static final float SKY_FOG_CEILING = 24.0F;

    @org.jetbrains.annotations.Nullable
    private static VertexBuffer coloredStarBuffer = null;

    private ElythiaSkyRenderer() {
    }

    public static boolean shouldRender(ClientLevel level) {
        return level.dimension().equals(ELYTHIA);
    }

    public static void render(
            Minecraft minecraft,
            ClientLevel level,
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            float partialTick,
            Camera camera,
            boolean isFoggy,
            Runnable setupFog,
            VertexBuffer skyBuffer,
            VertexBuffer darkBuffer,
            VertexBuffer starBuffer
    ) {
        setupFog.run();
        if (isFoggy || minecraft.options.hideLightningFlash().get() || minecraft.gameRenderer.getMainCamera() != camera) {
            return;
        }

        Vec3 skyColor = level.getSkyColor(camera.getPosition(), partialTick);
        float red = (float) skyColor.x;
        float green = (float) skyColor.y;
        float blue = (float) skyColor.z;
        float rainLevel = level.getRainLevel(partialTick);
        float starBrightness = level.getStarBrightness(partialTick) * (1.0F - rainLevel);
        double timeDegrees = (level.getDayTime() + partialTick) * (360.0D / 24000.0D);

        float nightPurpleR = 0.055F;
        float nightPurpleG = 0.010F;
        float nightPurpleB = 0.120F;
        red   = red   + nightPurpleR * starBrightness;
        green = green + nightPurpleG * starBrightness;
        blue  = blue  + nightPurpleB * starBrightness;

        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(red, green, blue, 1.0F);
        skyBuffer.bind();
        skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float renderDistance = Math.max(minecraft.gameRenderer.getRenderDistance(), 32.0F);
        renderHorizonFog(poseStack.last().pose(), projectionMatrix,
                ElythiaFog.start(level, renderDistance, partialTick), ElythiaFog.end(level, renderDistance, partialTick));
        renderSunrise(poseStack, projectionMatrix, level, partialTick, rainLevel);
        renderCelestialBodies(poseStack.last().pose(), projectionMatrix, timeDegrees, rainLevel);

        if (starBrightness > 0.1F) {
            if (coloredStarBuffer == null) {
                coloredStarBuffer = buildColoredStarBuffer();
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.setShaderColor(starBrightness, starBrightness, starBrightness, starBrightness);
            coloredStarBuffer.bind();
            coloredStarBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionColorShader());
            VertexBuffer.unbind();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        }

        ElythiaNightSkyRenderer.render(level, poseStack.last().pose(), projectionMatrix, partialTick, starBrightness);

        if (camera.getEntity() != null) {
            double horizon = camera.getEntity().getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level);
            if (horizon < 0.0D) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 12.0F, 0.0F);
                darkBuffer.bind();
                darkBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, GameRenderer.getPositionShader());
                VertexBuffer.unbind();
                poseStack.popPose();
            }
        }

        RenderSystem.depthMask(true);
    }

    private static float skyFogAlpha(float elevationDegrees, float fogStart, float fogEnd) {
        if (elevationDegrees <= 0.0F) {
            return 1.0F;
        }
        float tangent = (float) Math.tan(Math.toRadians(elevationDegrees));
        float distance = Math.max(SKY_FOG_CEILING / tangent, SKY_FOG_CEILING);
        return Mth.clamp((distance - fogStart) / Math.max(1.0F, fogEnd - fogStart), 0.0F, 1.0F);
    }

    private static void renderHorizonFog(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float fogStart, float fogEnd) {
        float[] fogColor = RenderSystem.getShaderFogColor();
        float r = fogColor[0];
        float g = fogColor[1];
        float b = fogColor[2];

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelViewMatrix);
        RenderSystem.applyModelViewMatrix();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int segments = 48;
        for (int ring = 0; ring < HORIZON_FOG_ELEVATIONS.length - 1; ring++) {
            float lowElevation = (float) Math.toRadians(HORIZON_FOG_ELEVATIONS[ring]);
            float highElevation = (float) Math.toRadians(HORIZON_FOG_ELEVATIONS[ring + 1]);
            float lowAlpha = skyFogAlpha(HORIZON_FOG_ELEVATIONS[ring], fogStart, fogEnd);
            float highAlpha = skyFogAlpha(HORIZON_FOG_ELEVATIONS[ring + 1], fogStart, fogEnd);
            if (lowAlpha <= 0.0F && highAlpha <= 0.0F) {
                continue;
            }
            float lowRadius = Mth.cos(lowElevation) * SKY_RADIUS;
            float highRadius = Mth.cos(highElevation) * SKY_RADIUS;
            float lowY = Mth.sin(lowElevation) * SKY_RADIUS;
            float highY = Mth.sin(highElevation) * SKY_RADIUS;
            for (int i = 0; i < segments; i++) {
                float a0 = i * Mth.TWO_PI / segments;
                float a1 = (i + 1) * Mth.TWO_PI / segments;
                float c0 = Mth.cos(a0);
                float s0 = Mth.sin(a0);
                float c1 = Mth.cos(a1);
                float s1 = Mth.sin(a1);
                buffer.addVertex(c0 * lowRadius, lowY, s0 * lowRadius).setColor(r, g, b, lowAlpha);
                buffer.addVertex(c1 * lowRadius, lowY, s1 * lowRadius).setColor(r, g, b, lowAlpha);
                buffer.addVertex(c1 * highRadius, highY, s1 * highRadius).setColor(r, g, b, highAlpha);
                buffer.addVertex(c0 * highRadius, highY, s0 * highRadius).setColor(r, g, b, highAlpha);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static VertexBuffer buildColoredStarBuffer() {
        Random random = new Random(10842L);
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i < 1500; i++) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double size = 0.15F + random.nextFloat() * 0.1F;
            double lenSq = x * x + y * y + z * z;
            if (lenSq < 1.0D && lenSq > 0.01D) {
                double len = 1.0D / Math.sqrt(lenSq);
                x *= len; y *= len; z *= len;
                double sx = x * 100.0D, sy = y * 100.0D, sz = z * 100.0D;
                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw), cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch), cosPitch = Math.cos(pitch);
                double rot = random.nextDouble() * Math.PI * 2.0D;
                double sinRot = Math.sin(rot), cosRot = Math.cos(rot);

                float[] color = pickStarColor(random);

                for (int j = 0; j < 4; j++) {
                    double u = (j & 2) - 1;
                    double v = (j + 1 & 2) - 1;
                    double px = u * cosRot - v * sinRot;
                    double py = v * cosRot + u * sinRot;
                    double qx = px * sinPitch;
                    double qy = -px * cosPitch;
                    double rx = qy * sinYaw - py * cosYaw;
                    double ry = py * sinYaw + qy * cosYaw;
                    builder.addVertex((float)(sx + rx * size), (float)(sy + qx * size), (float)(sz + ry * size))
                            .setColor(color[0], color[1], color[2], 1.0F);
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

    private static float[] pickStarColor(Random random) {
        float roll = random.nextFloat();
        if (roll < 0.72F) return new float[]{1.0F, 0.97F, 1.0F};
        if (roll < 0.82F) return new float[]{0.82F, 0.62F, 1.0F};
        if (roll < 0.89F) return new float[]{1.0F, 0.74F, 0.45F};
        if (roll < 0.96F) return new float[]{0.72F, 1.0F, 0.7F};
        return new float[]{1.0F, 0.56F, 0.62F};
    }

    private static void renderSunrise(PoseStack poseStack, Matrix4f projectionMatrix, ClientLevel level, float partialTick, float rainLevel) {
        float[] sunriseColor = level.effects().getSunriseColor(level.getTimeOfDay(partialTick), partialTick);
        if (sunriseColor == null || rainLevel >= 1.0F) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(level.getSunAngle(partialTick)) < 0.0F ? 180.0F : 0.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(matrix, 0.0F, 100.0F, 0.0F).setColor(sunriseColor[0], sunriseColor[1], sunriseColor[2], sunriseColor[3] * (1.0F - rainLevel));
        for (int i = 0; i <= 32; i++) {
            float angle = i * Mth.TWO_PI / 32.0F;
            float x = Mth.sin(angle) * 120.0F;
            float y = Mth.cos(angle) * 120.0F;
            buffer.addVertex(matrix, x, y, -5.0F).setColor(sunriseColor[0], sunriseColor[1], sunriseColor[2], 0.0F);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());

        poseStack.popPose();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderCelestialBodies(Matrix4f modelViewMatrix, Matrix4f projectionMatrix, double timeDegrees, float rainLevel) {

        float sun1Orbit = orbitAngle(timeDegrees, 1.0D, 0.0D);
        renderBodyGlow(modelViewMatrix, projectionMatrix, sun1Orbit, 0.0F, 0xD06BFF, rainLevel, 70.0F);
        renderTexturedBody(modelViewMatrix, projectionMatrix, sun1Orbit, 0.0F, 38.0F, 0xD06BFF, 1.0F, SUN_TEXTURE, false, 0, orbitAngle(timeDegrees, 0.8D, 0.0D));

        float sun2Orbit = orbitAngle(timeDegrees, 0.73D, 54.0D);
        renderBodyGlow(modelViewMatrix, projectionMatrix, sun2Orbit, -14.0F, 0xFF9A33, rainLevel, 70.0F);
        renderTexturedBody(modelViewMatrix, projectionMatrix, sun2Orbit, -14.0F, 16.0F, 0xFF9A33, 1.0F, SUN_TEXTURE, false, 0, orbitAngle(timeDegrees, -0.5D, 0.0D));

        float moon2Orbit = orbitAngle(timeDegrees, 1.27D, 228.0D);
        renderBodyGlow(modelViewMatrix, projectionMatrix, moon2Orbit, 36.0F, 0x4FD84D, rainLevel, 78.0F);
        renderTexturedBody(modelViewMatrix, projectionMatrix, moon2Orbit, 36.0F, 14.0F, 0x4FD84D, 1.0F, MOON_TEXTURE, true, 1, orbitAngle(timeDegrees, 0.3D, 0.0D));

        float moon3Orbit = orbitAngle(timeDegrees, 0.38D, 122.0D);
        renderBodyGlow(modelViewMatrix, projectionMatrix, moon3Orbit, -34.0F, 0xA3FF8F, rainLevel, 78.0F);
        renderTexturedBody(modelViewMatrix, projectionMatrix, moon3Orbit, -34.0F, 12.0F, 0xA3FF8F, 1.0F, MOON_TEXTURE, true, 2, orbitAngle(timeDegrees, -0.6D, 0.0D));
    }

    private static float orbitAngle(double timeDegrees, double speed, double offset) {
        return (float) ((timeDegrees * speed + offset) % 360.0D);
    }

    private static void renderBodyGlow(
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            float orbitDegrees,
            float yawDegrees,
            int rgb,
            float rainLevel,
            float glowRadius
    ) {
        float orbitRad = (float) Math.toRadians(orbitDegrees);
        float yawRad = (float) Math.toRadians(yawDegrees);

        float bodyHeight = Mth.cos(yawRad) * Mth.cos(orbitRad);

        float horizonGlow = 1.0F - Mth.clamp(Math.abs(bodyHeight) * 2.0F, 0.0F, 1.0F);
        float visibility = Mth.clamp((bodyHeight + 0.4F) * 2.5F, 0.0F, 1.0F);
        float alpha = horizonGlow * visibility * 0.65F * (1.0F - rainLevel);

        if (alpha <= 0.01F) {
            return;
        }

        float r = ((rgb >> 16) & 0xFF) / 255.0F;
        float g = ((rgb >> 8) & 0xFF) / 255.0F;
        float b = (rgb & 0xFF) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelViewMatrix);
        modelViewStack.rotateX(orbitRad);
        modelViewStack.rotateZ(yawRad);
        RenderSystem.applyModelViewMatrix();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(0.0F, SKY_RADIUS, 0.0F).setColor(r, g, b, alpha);
        for (int i = 0; i <= 16; i++) {
            float angle = i * Mth.TWO_PI / 16.0F;
            buffer.addVertex(Mth.sin(angle) * glowRadius, SKY_RADIUS, Mth.cos(angle) * glowRadius).setColor(r, g, b, 0.0F);
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
            float alpha,
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
        RenderSystem.setShaderColor(
                ((rgb >> 16) & 0xFF) / 255.0F,
                ((rgb >> 8) & 0xFF) / 255.0F,
                (rgb & 0xFF) / 255.0F,
                alpha
        );
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

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
