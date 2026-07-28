package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.entity.ReverieEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReverieTrailHandler {

    private static final int MAX_TRAIL_POINTS = 20;
    private static final int IDLE_LIFETIME_TICKS = 8;
    private static final int NORMAL_LIFETIME_TICKS = 14;
    private static final int DASH_LIFETIME_TICKS = 22;
    private static final float CORE_LINE_WIDTH = 0.035f;
    private static final float GLOW_LINE_WIDTH = 0.10f;
    private static final double MIN_DISTANCE_BETWEEN_POINTS = 0.05;
    private static final double DASH_DISTANCE_THRESHOLD = 0.35;

    private static final Map<Integer, ArrayDeque<TrailPoint>> TRAILS = new HashMap<>();

    private ReverieTrailHandler() {}

    private static final class TrailPoint {
        final Vec3 pos;
        final ReverieEntity.Mood mood;
        int age;
        final int lifetime;

        TrailPoint(Vec3 pos, ReverieEntity.Mood mood, int lifetime) {
            this.pos = pos;
            this.mood = mood;
            this.age = 0;
            this.lifetime = lifetime;
        }

        float progress() { return lifetime <= 0 ? 1f : (float) age / (float) lifetime; }
        boolean expired() { return age >= lifetime; }
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) { TRAILS.clear(); return; }
            if (client.isPaused()) return;

            Set<Integer> activeIds = new HashSet<>();

            for (Entity entity : client.level.entitiesForRendering()) {
                if (!(entity instanceof ReverieEntity reverie)) continue;

                int id = reverie.getId();
                activeIds.add(id);
                ReverieEntity.Mood mood = reverie.getMood();
                Vec3 pos = reverie.position().add(0.0, reverie.getBbHeight() * 0.5, 0.0);

                ArrayDeque<TrailPoint> points = TRAILS.computeIfAbsent(id, k -> new ArrayDeque<>());

                if (points.isEmpty()) {
                    points.addFirst(new TrailPoint(pos, mood, lifetimeFor(mood, false)));
                } else {
                    Vec3 lastPos = points.peekFirst().pos;
                    double distSq = pos.distanceToSqr(lastPos);
                    if (distSq >= MIN_DISTANCE_BETWEEN_POINTS * MIN_DISTANCE_BETWEEN_POINTS) {
                        boolean isDash = distSq >= DASH_DISTANCE_THRESHOLD * DASH_DISTANCE_THRESHOLD;
                        points.addFirst(new TrailPoint(pos, mood, lifetimeFor(mood, isDash)));
                        while (points.size() > MAX_TRAIL_POINTS) points.pollLast();
                    }
                }

                Iterator<TrailPoint> it = points.iterator();
                while (it.hasNext()) {
                    TrailPoint p = it.next();
                    p.age++;
                    if (p.expired()) it.remove();
                }
            }

            TRAILS.keySet().removeIf(id -> !activeIds.contains(id));
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (TRAILS.isEmpty()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            Camera camera = context.camera();
            Vec3 camPos = camera.getPosition();
            Matrix4f mat = context.matrixStack().last().pose();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            Tesselator tesselator = Tesselator.getInstance();

            List<List<TrailPoint>> snapshots = new ArrayList<>(TRAILS.size());
            for (ArrayDeque<TrailPoint> deque : TRAILS.values()) {
                if (deque.size() >= 2) snapshots.add(new ArrayList<>(deque));
            }

            if (!snapshots.isEmpty()) {
                BufferBuilder glowBuf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (List<TrailPoint> points : snapshots) buildPass(glowBuf, mat, points, camPos, true);
                MeshData glowMesh = glowBuf.build();
                if (glowMesh != null) BufferUploader.drawWithShader(glowMesh);

                BufferBuilder coreBuf = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                for (List<TrailPoint> points : snapshots) buildPass(coreBuf, mat, points, camPos, false);
                MeshData coreMesh = coreBuf.build();
                if (coreMesh != null) BufferUploader.drawWithShader(coreMesh);
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        });
    }

    private static int lifetimeFor(ReverieEntity.Mood mood, boolean isDash) {
        if (isDash || mood == ReverieEntity.Mood.PURPLE) return DASH_LIFETIME_TICKS;
        if (mood == ReverieEntity.Mood.WHITE) return IDLE_LIFETIME_TICKS;
        return NORMAL_LIFETIME_TICKS;
    }

    private static void buildPass(BufferBuilder buf, Matrix4f mat, List<TrailPoint> points, Vec3 camPos, boolean glow) {
        int n = points.size();
        for (int i = 0; i < n - 1; i++) {
            TrailPoint newer = points.get(i);
            TrailPoint older = points.get(i + 1);

            float alphaNearer  = (glow ? glowAlpha(newer.mood) : coreAlpha(newer.mood)) * indexAlpha(i, n)       * (1f - newer.progress());
            float alphaFarther = (glow ? glowAlpha(older.mood) : coreAlpha(older.mood)) * indexAlpha(i + 1, n)   * (1f - older.progress());
            if (alphaNearer < 0.005f && alphaFarther < 0.005f) continue;

            Vec3 right = cameraFacingRight(newer.pos, older.pos, camPos);
            if (right == null) continue;

            float halfWidth = glow ? glowHalfWidth(newer.mood) : coreHalfWidth(newer.mood);
            right = right.scale(halfWidth);

            float[] cn = color(newer.mood);
            float[] co = color(older.mood);
            Vec3 na = newer.pos.subtract(camPos);
            Vec3 nb = older.pos.subtract(camPos);

            buf.addVertex(mat, (float)(na.x + right.x), (float)(na.y + right.y), (float)(na.z + right.z)).setColor(cn[0], cn[1], cn[2], alphaNearer);
            buf.addVertex(mat, (float)(na.x - right.x), (float)(na.y - right.y), (float)(na.z - right.z)).setColor(cn[0], cn[1], cn[2], alphaNearer);
            buf.addVertex(mat, (float)(nb.x - right.x), (float)(nb.y - right.y), (float)(nb.z - right.z)).setColor(co[0], co[1], co[2], alphaFarther);
            buf.addVertex(mat, (float)(nb.x + right.x), (float)(nb.y + right.y), (float)(nb.z + right.z)).setColor(co[0], co[1], co[2], alphaFarther);
        }
    }

    private static Vec3 cameraFacingRight(Vec3 a, Vec3 b, Vec3 camPos) {
        Vec3 segDir = b.subtract(a);
        if (segDir.lengthSqr() < 1e-12) return null;
        segDir = segDir.normalize();
        Vec3 mid = a.add(b).scale(0.5);
        Vec3 toCamera = camPos.subtract(mid);
        if (toCamera.lengthSqr() < 1e-12) return null;
        toCamera = toCamera.normalize();
        Vec3 right = segDir.cross(toCamera);
        if (right.lengthSqr() < 1e-10) {
            right = segDir.cross(new Vec3(0.0, 1.0, 0.0));
            if (right.lengthSqr() < 1e-10) return null;
        }
        return right.normalize();
    }

    private static float indexAlpha(int i, int n) {
        if (n <= 1) return 1f;
        return 1f - (float) i / (float) (n - 1);
    }

    private static final float[] COLOR_WHITE  = { 1.0f, 1.0f, 1.0f };
    private static final float[] COLOR_YELLOW = { 1.0f, 0.85f, 0.1f };
    private static final float[] COLOR_RED    = { 1.0f, 0.1f, 0.05f };
    private static final float[] COLOR_PURPLE = { 0.7f, 0.1f, 1.0f };
    private static final float[] COLOR_BLUE   = { 0.1f, 0.5f, 1.0f };

    private static float[] color(ReverieEntity.Mood mood) {
        return switch (mood) {
            case WHITE  -> COLOR_WHITE;
            case YELLOW -> COLOR_YELLOW;
            case RED    -> COLOR_RED;
            case PURPLE -> COLOR_PURPLE;
            case BLUE   -> COLOR_BLUE;
        };
    }

    private static float glowAlpha(ReverieEntity.Mood mood) {
        return switch (mood) {
            case WHITE  -> 0.15f;
            case YELLOW -> 0.30f;
            case RED    -> 0.55f;
            case PURPLE -> 0.65f;
            case BLUE   -> 0.35f;
        };
    }

    private static float coreAlpha(ReverieEntity.Mood mood) {
        return switch (mood) {
            case WHITE  -> 0.35f;
            case YELLOW -> 0.55f;
            case RED    -> 0.85f;
            case PURPLE -> 0.95f;
            case BLUE   -> 0.60f;
        };
    }

    private static float glowHalfWidth(ReverieEntity.Mood mood) {
        float base = GLOW_LINE_WIDTH * 0.5f;
        return switch (mood) {
            case WHITE  -> base * 0.60f;
            case YELLOW -> base * 0.80f;
            case RED    -> base * 1.00f;
            case PURPLE -> base * 1.25f;
            case BLUE   -> base * 0.90f;
        };
    }

    private static float coreHalfWidth(ReverieEntity.Mood mood) {
        float base = CORE_LINE_WIDTH * 0.5f;
        return switch (mood) {
            case WHITE  -> base * 0.60f;
            case YELLOW -> base * 0.80f;
            case RED    -> base * 1.20f;
            case PURPLE -> base * 1.50f;
            case BLUE   -> base * 1.00f;
        };
    }
}
