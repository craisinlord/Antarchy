package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.AntTransitClientState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class AntTransitGroundRenderer {
    private static final float GROW_TICKS = 20.0F;
    private static final float CLOSE_TICKS = 15.0F;
    private static final float LIFT = 0.02F;
    private static final float GOLD_TILE = 0.5F;
    private static final int GOLD_REACH = 6;
    private static final float CRACK_TILE = 0.25F;
    private static final int CRACK_BRANCHES = 6;
    private static final int CRACK_LENGTH = 14;
    private static final float PRISM_TILE = 0.25F;
    private static final int PRISM_REACH = 10;
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    private AntTransitGroundRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource buffers, Camera camera, float partialTick) {
        if (AntTransitClientState.discs().isEmpty()) {
            return;
        }
        Vec3 cameraPos = camera.getPosition();
        RenderType renderType = RenderType.debugQuads();
        VertexConsumer consumer = buffers.getBuffer(renderType);
        Matrix4f pose = poseStack.last().pose();
        for (AntTransitClientState.Disc disc : AntTransitClientState.discs()) {
            if (disc.style().cavaryn()) {
                continue;
            }
            float age = disc.age() + partialTick;
            float grow = Mth.clamp(age / GROW_TICKS, 0.0F, 1.0F);
            float close = Mth.clamp((AntTransitClientState.DISC_LIFETIME_TICKS - age) / CLOSE_TICKS, 0.0F, 1.0F);
            float reveal = (1.0F - (1.0F - grow) * (1.0F - grow)) * close;
            if (reveal <= 0.01F) {
                continue;
            }
            Vec3 center = disc.center();
            float y = (float) (center.y - cameraPos.y) + LIFT;
            float time = age / 20.0F;
            if (disc.style().thoraxis()) {
                drawCracks(consumer, pose, center, cameraPos, y, reveal, time);
            } else if (disc.style().prism()) {
                drawMosaic(consumer, pose, center, cameraPos, y, reveal, time);
            } else {
                drawTiles(consumer, pose, center, cameraPos, y, reveal, time);
            }
        }
        buffers.endBatch(renderType);
    }

    private static void drawTiles(VertexConsumer consumer, Matrix4f pose, Vec3 center, Vec3 cameraPos, float y, float reveal, float time) {
        int cx = Mth.floor(center.x / GOLD_TILE);
        int cz = Mth.floor(center.z / GOLD_TILE);
        float reach = GOLD_REACH * reveal;
        for (int dx = -GOLD_REACH; dx <= GOLD_REACH; dx++) {
            for (int dz = -GOLD_REACH; dz <= GOLD_REACH; dz++) {
                int distance = Math.abs(dx) + Math.abs(dz);
                if (distance > reach) {
                    continue;
                }
                float edge = 1.0F - distance / (float) (GOLD_REACH + 1);
                float sparkle = 0.65F + 0.35F * Mth.sin(time * 6.0F + hash(cx + dx, cz + dz) * 6.283F);
                float white = distance <= 1 ? 0.9F : 0.0F;
                float r = 1.0F;
                float g = Mth.lerp(white, 0.82F, 0.97F) * sparkle;
                float b = Mth.lerp(white, 0.4F, 0.86F) * sparkle;
                float alpha = 0.85F * edge * Mth.clamp(reach - distance + 1.0F, 0.0F, 1.0F);
                tile(consumer, pose, (cx + dx) * GOLD_TILE - cameraPos.x, y, (cz + dz) * GOLD_TILE - cameraPos.z,
                        GOLD_TILE, 0.04F, r * sparkle, g, b, alpha);
            }
        }
    }

    private static void drawCracks(VertexConsumer consumer, Matrix4f pose, Vec3 center, Vec3 cameraPos, float y, float reveal, float time) {
        int cx = Mth.floor(center.x / CRACK_TILE);
        int cz = Mth.floor(center.z / CRACK_TILE);
        int seed = cx * 73856093 ^ cz * 19349663;
        int steps = Math.round(CRACK_LENGTH * reveal);
        float pulse = 0.75F + 0.25F * Mth.sin(time * 5.0F);
        tile(consumer, pose, cx * CRACK_TILE - cameraPos.x, y, cz * CRACK_TILE - cameraPos.z, CRACK_TILE, -0.08F, 0.78F * pulse, 0.08F, 0.2F, 0.5F);
        tile(consumer, pose, cx * CRACK_TILE - cameraPos.x, y + 0.001F, cz * CRACK_TILE - cameraPos.z, CRACK_TILE, 0.0F, 0.04F, 0.0F, 0.02F, 0.95F);
        for (int branch = 0; branch < CRACK_BRANCHES; branch++) {
            int heading = Math.floorMod(seed + branch * 7, 4);
            int x = cx;
            int z = cz;
            for (int step = 0; step < steps; step++) {
                float roll = hash(seed + branch * 31, step);
                int direction = roll < 0.18F ? (heading + 1) % 4 : roll < 0.36F ? (heading + 3) % 4 : heading;
                x += DIRECTIONS[direction][0];
                z += DIRECTIONS[direction][1];
                float fade = 1.0F - step / (float) (CRACK_LENGTH + 2);
                float px = x * CRACK_TILE - (float) cameraPos.x;
                float pz = z * CRACK_TILE - (float) cameraPos.z;
                tile(consumer, pose, px, y, pz, CRACK_TILE, -0.06F, 0.78F * pulse, 0.08F, 0.22F, 0.55F * fade);
                tile(consumer, pose, px, y + 0.001F, pz, CRACK_TILE, 0.03F, 0.05F, 0.0F, 0.02F, 0.95F * fade);
            }
        }
    }

    private static void drawMosaic(VertexConsumer consumer, Matrix4f pose, Vec3 center, Vec3 cameraPos, float y, float reveal, float time) {
        int cx = Mth.floor(center.x / PRISM_TILE);
        int cz = Mth.floor(center.z / PRISM_TILE);
        float reach = PRISM_REACH * reveal;
        for (int dx = -PRISM_REACH; dx <= PRISM_REACH; dx++) {
            for (int dz = -PRISM_REACH; dz <= PRISM_REACH; dz++) {
                int ring = Math.max(Math.abs(dx), Math.abs(dz));
                if (ring > reach) {
                    continue;
                }
                float noise = hash(cx + dx, cz + dz);
                if (ring > 2 && noise < 0.22F) {
                    continue;
                }
                float hue = ring * 0.07F - time * 0.6F + noise * 0.08F;
                int rgb = Mth.hsvToRgb(hue - (float) Math.floor(hue), 0.8F, 1.0F);
                float edge = 1.0F - ring / (float) (PRISM_REACH + 1);
                float alpha = 0.8F * edge * Mth.clamp(reach - ring + 1.0F, 0.0F, 1.0F);
                tile(consumer, pose, (cx + dx) * PRISM_TILE - cameraPos.x, y, (cz + dz) * PRISM_TILE - cameraPos.z,
                        PRISM_TILE, 0.015F, (rgb >> 16 & 0xFF) / 255.0F, (rgb >> 8 & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, alpha);
            }
        }
    }

    private static void tile(VertexConsumer consumer, Matrix4f pose, double x, float y, double z, float size, float inset,
                             float r, float g, float b, float alpha) {
        if (alpha <= 0.01F) {
            return;
        }
        float x0 = (float) x + inset;
        float z0 = (float) z + inset;
        float x1 = (float) x + size - inset;
        float z1 = (float) z + size - inset;
        consumer.addVertex(pose, x0, y, z0).setColor(r, g, b, alpha);
        consumer.addVertex(pose, x0, y, z1).setColor(r, g, b, alpha);
        consumer.addVertex(pose, x1, y, z1).setColor(r, g, b, alpha);
        consumer.addVertex(pose, x1, y, z0).setColor(r, g, b, alpha);
    }

    private static float hash(int a, int b) {
        int h = a * 374761393 + b * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFF) / 65535.0F;
    }
}
