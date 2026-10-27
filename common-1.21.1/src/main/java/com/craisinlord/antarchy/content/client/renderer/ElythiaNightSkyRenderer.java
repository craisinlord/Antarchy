package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

public final class ElythiaNightSkyRenderer {
    private static final float RADIUS = 99.0F;
    private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final Vector3f EAST = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final double METEOR_SLOT_TICKS = 30.0D;
    private static final double METEOR_LIFE_TICKS = 18.0D;
    private static final float METEOR_CHANCE = 0.03F;
    private static final float SHOWER_METEOR_CHANCE = 0.3F;
    private static final float SHOWER_NIGHT_CHANCE = 1.0F / 7.0F;
    private static final float AURORA_NIGHT_CHANCE = 0.4F;
    private static final int AURORA_COLUMNS = 96;

    private record Constellation(float azimuth, float elevation, float scale, float[][] points) {
    }

    private static final Constellation[] CONSTELLATIONS = {
            new Constellation(20.0F, 52.0F, 1.5F,
                    new float[][]{{0, 3}, {0, 0}, {0, -4}, {-2, 6}, {2, 6}, {-6, 5}, {-7, 0}, {-5, -4}, {6, 5}, {7, 0}, {5, -4}}),
            new Constellation(95.0F, 40.0F, 1.4F,
                    new float[][]{{6, 1}, {8, 4}, {2, 0}, {-1, 0}, {-6, -1}, {1, 3}, {1, -3}, {4, 3}, {4, -3}, {-1, -3}}),
            new Constellation(170.0F, 58.0F, 1.5F,
                    new float[][]{{0, 4}, {2.8F, 2.8F}, {4, 0}, {2.8F, -2.8F}, {0, -4}, {-2.8F, -2.8F}, {-4, 0}, {-2.8F, 2.8F}, {0, 6}, {2.5F, 7}}),
            new Constellation(245.0F, 46.0F, 1.6F,
                    new float[][]{{-5, 0}, {-5, 5}, {-2.5F, 2}, {0, 6}, {2.5F, 2}, {5, 5}, {5, 0}}),
            new Constellation(320.0F, 36.0F, 1.5F,
                    new float[][]{{-2, 4}, {2, 4}, {0, 2}, {-4, -1}, {4, -1}, {-3, -4}, {3, -4}, {-6, -4}, {6, -4}})
    };

    @Nullable
    private static VertexBuffer constellationStars;

    private ElythiaNightSkyRenderer() {
    }

    public static void render(ClientLevel level, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, float partialTick, float starBrightness) {
        float night = Mth.clamp(starBrightness * 2.0F, 0.0F, 1.0F);
        if (night <= 0.05F) {
            return;
        }

        long dayTime = level.getDayTime();
        long nightIndex = Math.floorDiv(dayTime + 12000L, 24000L);
        float timeOfDay = Math.floorMod(dayTime, 24000L) + partialTick;
        double time = level.getGameTime() + partialTick;

        if (constellationStars == null) {
            constellationStars = buildConstellationStars();
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        float constellationBrightness = Math.min(1.0F, starBrightness * 2.0F);
        RenderSystem.setShaderColor(constellationBrightness, constellationBrightness, constellationBrightness, constellationBrightness);
        constellationStars.bind();
        constellationStars.drawWithShader(modelViewMatrix, projectionMatrix, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.DISTANCE_TO_ORIGIN);
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(modelViewMatrix);
        RenderSystem.applyModelViewMatrix();

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderAurora(buffer, nightIndex, timeOfDay, time, night);
        renderMeteors(buffer, nightIndex, time, night);
        MeshData mesh = buffer.build();
        if (mesh != null) {
            BufferUploader.drawWithShader(mesh);
        }

        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static boolean renderAurora(BufferBuilder buffer, long nightIndex, float timeOfDay, double time, float night) {
        if (hash01(nightIndex, 0xA0L) > AURORA_NIGHT_CHANCE) {
            return false;
        }
        float window = smooth(1.0F - Math.abs(timeOfDay - 18000.0F) / 6500.0F);
        float strength = 0.6F + 0.4F * hash01(nightIndex, 0xA1L);
        float intensity = night * window * strength;
        if (intensity <= 0.01F) {
            return false;
        }

        float centerAzimuth = hash01(nightIndex, 0xA2L) * 360.0F;
        float[][] columns = new float[AURORA_COLUMNS][];
        for (int curtain = 0; curtain < 2; curtain++) {
            float baseElevation = 7.0F + curtain * 5.0F;
            float height = 22.0F - curtain * 6.0F;
            float span = 150.0F - curtain * 40.0F;
            float alphaScale = curtain == 0 ? 1.0F : 0.55F;
            float azimuthStart = centerAzimuth + curtain * 35.0F - span * 0.5F;

            for (int j = 0; j < AURORA_COLUMNS; j++) {
                float u = j / (float) (AURORA_COLUMNS - 1);
                float azimuth = azimuthStart + u * span + 6.0F * Mth.sin((float) (u * 9.0F + time * 0.004D + curtain));
                float bottom = baseElevation + 2.5F * Mth.sin((float) (u * 7.0F + time * 0.006D));
                float shimmer = 0.55F + 0.45F * Mth.sin((float) (j * 0.9F + time * 0.05D + curtain))
                        * Mth.sin((float) (j * 0.23F - time * 0.021D));
                float alpha = intensity * alphaScale * Mth.sin(Mth.PI * u) * shimmer;
                columns[j] = new float[]{azimuth, bottom, alpha};
            }

            float[] rowOffsets = {-1.5F, 0.0F, height * 0.45F, height};
            float[][] rowColors = {
                    {0.3F, 1.0F, 0.5F, 0.0F},
                    {0.3F, 1.0F, 0.5F, 1.0F},
                    {0.3F, 0.85F, 0.75F, 0.55F},
                    {0.65F, 0.35F, 1.0F, 0.0F}
            };
            for (int j = 0; j < AURORA_COLUMNS - 1; j++) {
                float[] left = columns[j];
                float[] right = columns[j + 1];
                for (int row = 0; row < rowOffsets.length - 1; row++) {
                    auroraVertex(buffer, left, rowOffsets[row], rowColors[row]);
                    auroraVertex(buffer, right, rowOffsets[row], rowColors[row]);
                    auroraVertex(buffer, right, rowOffsets[row + 1], rowColors[row + 1]);
                    auroraVertex(buffer, left, rowOffsets[row + 1], rowColors[row + 1]);
                }
            }
        }
        return true;
    }

    private static void auroraVertex(BufferBuilder buffer, float[] column, float elevationOffset, float[] color) {
        Vector3f point = skyPoint(column[0], column[1] + elevationOffset).mul(RADIUS);
        buffer.addVertex(point.x, point.y, point.z).setColor(color[0], color[1], color[2], color[3] * column[2]);
    }

    private static boolean renderMeteors(BufferBuilder buffer, long nightIndex, double time, float night) {
        if (night < 0.3F) {
            return false;
        }
        boolean shower = hash01(nightIndex, 0xB0L) < SHOWER_NIGHT_CHANCE;
        float chance = shower ? SHOWER_METEOR_CHANCE : METEOR_CHANCE;
        long slot = (long) Math.floor(time / METEOR_SLOT_TICKS);
        if (hash01(slot, 0xB1L) >= chance) {
            return false;
        }
        float progress = (float) ((time - slot * METEOR_SLOT_TICKS) / METEOR_LIFE_TICKS);
        if (progress >= 1.0F) {
            return false;
        }

        float azimuth = hash01(slot, 0xB2L) * 360.0F;
        float elevation = 25.0F + hash01(slot, 0xB3L) * 45.0F;
        float azimuthTravel = (hash01(slot, 0xB4L) - 0.5F) * 50.0F;
        float elevationTravel = 10.0F + hash01(slot, 0xB5L) * 15.0F;
        Vector3f start = skyPoint(azimuth, elevation);
        Vector3f end = skyPoint(azimuth + azimuthTravel, elevation - elevationTravel);
        Vector3f head = new Vector3f(start).lerp(end, progress).normalize(RADIUS);
        Vector3f tail = new Vector3f(start).lerp(end, Math.max(0.0F, progress - 0.35F)).normalize(RADIUS);
        Vector3f side = new Vector3f(head).sub(tail).cross(head).normalize(0.3F);
        float alpha = night * Mth.sin(Mth.PI * progress);

        buffer.addVertex(head.x + side.x, head.y + side.y, head.z + side.z).setColor(1.0F, 0.95F, 1.0F, alpha);
        buffer.addVertex(head.x - side.x, head.y - side.y, head.z - side.z).setColor(1.0F, 0.95F, 1.0F, alpha);
        buffer.addVertex(tail.x, tail.y, tail.z).setColor(0.8F, 0.6F, 1.0F, 0.0F);
        buffer.addVertex(tail.x, tail.y, tail.z).setColor(0.8F, 0.6F, 1.0F, 0.0F);
        return true;
    }

    private static VertexBuffer buildConstellationStars() {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int index = 0;
        for (Constellation constellation : CONSTELLATIONS) {
            for (float[] point : constellation.points()) {
                Vector3f position = constellationPoint(constellation, point);
                float rotation = hash01(index++, 0xC0L) * Mth.TWO_PI;
                skyQuad(buffer, position, 0.3F, rotation, 1.0F, 0.96F, 1.0F, 1.0F);
            }
        }
        return upload(buffer.buildOrThrow());
    }

    private static VertexBuffer upload(MeshData mesh) {
        VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        vertexBuffer.bind();
        vertexBuffer.upload(mesh);
        VertexBuffer.unbind();
        return vertexBuffer;
    }

    private static Vector3f constellationPoint(Constellation constellation, float[] point) {
        Vector3f center = skyPoint(constellation.azimuth(), constellation.elevation());
        Vector3f east = new Vector3f(UP).cross(center).normalize();
        Vector3f north = new Vector3f(center).cross(east);
        float u = (float) Math.tan(Math.toRadians(point[0] * constellation.scale()));
        float v = (float) Math.tan(Math.toRadians(point[1] * constellation.scale()));
        return center.fma(u, east).fma(v, north).normalize(RADIUS);
    }

    private static void skyQuad(BufferBuilder buffer, Vector3f position, float size, float rotation, float r, float g, float b, float a) {
        Vector3f normal = new Vector3f(position).normalize();
        Vector3f reference = Math.abs(normal.y) > 0.95F ? EAST : UP;
        Vector3f tangent = new Vector3f(reference).cross(normal).normalize();
        Vector3f bitangent = new Vector3f(normal).cross(tangent);
        float cos = Mth.cos(rotation);
        float sin = Mth.sin(rotation);
        Vector3f e1 = new Vector3f(tangent).mul(cos).fma(sin, bitangent).mul(size);
        Vector3f e2 = new Vector3f(bitangent).mul(cos).fma(-sin, tangent).mul(size);
        buffer.addVertex(position.x - e1.x - e2.x, position.y - e1.y - e2.y, position.z - e1.z - e2.z).setColor(r, g, b, a);
        buffer.addVertex(position.x + e1.x - e2.x, position.y + e1.y - e2.y, position.z + e1.z - e2.z).setColor(r, g, b, a);
        buffer.addVertex(position.x + e1.x + e2.x, position.y + e1.y + e2.y, position.z + e1.z + e2.z).setColor(r, g, b, a);
        buffer.addVertex(position.x - e1.x + e2.x, position.y - e1.y + e2.y, position.z - e1.z + e2.z).setColor(r, g, b, a);
    }

    private static Vector3f skyPoint(float azimuthDegrees, float elevationDegrees) {
        float azimuth = (float) Math.toRadians(azimuthDegrees);
        float elevation = (float) Math.toRadians(elevationDegrees);
        float horizontal = Mth.cos(elevation);
        return new Vector3f(horizontal * Mth.sin(azimuth), Mth.sin(elevation), horizontal * Mth.cos(azimuth));
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float hash01(long value, long salt) {
        long h = value * 0x9E3779B97F4A7C15L ^ salt * 0xC2B2AE3D27D4EB4FL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) * 0x1.0p-24F;
    }
}
