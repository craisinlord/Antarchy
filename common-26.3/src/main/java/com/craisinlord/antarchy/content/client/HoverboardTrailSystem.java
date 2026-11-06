package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.entity.HoverboardEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class HoverboardTrailSystem {
    private static final int MAX_TRAIL_POINTS = 18;
    private static final int TRAIL_LIFETIME_TICKS = 12;
    private static final float CORE_HALF_WIDTH = 0.0225f;
    private static final float GLOW_HALF_WIDTH = 0.055f;
    private static final double MIN_DISTANCE_BETWEEN_POINTS = 0.04D;
    private static final double LATERAL_OFFSET = 0.5275D;
    private static final double REAR_OFFSET = 0.77D;
    private static final double HEIGHT_OFFSET = 0.14D;
    private static final float[] DEFAULT_COLOR = rgb(0xC9A0FF);

    private static final RenderType TRAIL_RENDER_TYPE = RenderType.create(
            "antarchy_hoverboard_trail",
            RenderSetup.builder(RenderPipelines.DEBUG_QUADS).setOitPipelines(RenderPipelines.OIT_DEBUG_QUADS).createRenderSetup()
    );

    private static final Map<Integer, BoardTrails> TRAILS = new HashMap<>();

    private HoverboardTrailSystem() {
    }

    private static final class TrailPoint {
        final Vec3 pos;
        int age;

        TrailPoint(Vec3 pos) {
            this.pos = pos;
        }

        float progress() {
            return (float) this.age / (float) TRAIL_LIFETIME_TICKS;
        }

        boolean expired() {
            return this.age >= TRAIL_LIFETIME_TICKS;
        }
    }

    private static final class BoardTrails {
        final ArrayDeque<TrailPoint> left = new ArrayDeque<>();
        final ArrayDeque<TrailPoint> right = new ArrayDeque<>();
        float[] color = DEFAULT_COLOR;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            TRAILS.clear();
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        Set<Integer> activeIds = new HashSet<>();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof HoverboardEntity hoverboard)) {
                continue;
            }

            int id = hoverboard.getId();
            activeIds.add(id);
            BoardTrails boardTrails = TRAILS.computeIfAbsent(id, ignored -> new BoardTrails());
            boardTrails.color = colorFor(hoverboard.getColor());

            if (hoverboard.isVehicle() && hoverboard.isTrailActive()) {
                TrailAnchors anchors = trailAnchors(hoverboard);
                addPoint(boardTrails.left, anchors.left());
                addPoint(boardTrails.right, anchors.right());
            }

            age(boardTrails.left);
            age(boardTrails.right);
            if (boardTrails.left.isEmpty() && boardTrails.right.isEmpty() && !hoverboard.isTrailActive()) {
                TRAILS.remove(id);
            }
        }

        TRAILS.keySet().removeIf(id -> !activeIds.contains(id));
    }

    public static void render(PoseStack poseStack, SubmitNodeCollector collector, Vec3 camPos) {
        if (TRAILS.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        List<TrailSnapshot> snapshots = new ArrayList<>(TRAILS.size() * 2);
        for (BoardTrails boardTrails : TRAILS.values()) {
            if (boardTrails.left.size() >= 2) {
                snapshots.add(new TrailSnapshot(copy(boardTrails.left), boardTrails.color));
            }
            if (boardTrails.right.size() >= 2) {
                snapshots.add(new TrailSnapshot(copy(boardTrails.right), boardTrails.color));
            }
        }

        if (snapshots.isEmpty()) {
            return;
        }

        collector.submitCustomGeometry(poseStack, TRAIL_RENDER_TYPE, (pose, buf) -> {
            for (TrailSnapshot snapshot : snapshots) {
                buildPass(buf, pose, snapshot.points(), snapshot.color(), camPos, true);
            }
            for (TrailSnapshot snapshot : snapshots) {
                buildPass(buf, pose, snapshot.points(), snapshot.color(), camPos, false);
            }
        });
    }

    private static List<PointSnapshot> copy(ArrayDeque<TrailPoint> points) {
        List<PointSnapshot> out = new ArrayList<>(points.size());
        for (TrailPoint point : points) {
            out.add(new PointSnapshot(point.pos, point.progress()));
        }
        return out;
    }

    private static void addPoint(ArrayDeque<TrailPoint> points, Vec3 pos) {
        if (points.isEmpty()) {
            points.addFirst(new TrailPoint(pos));
            return;
        }
        if (points.peekFirst().pos.distanceToSqr(pos) < MIN_DISTANCE_BETWEEN_POINTS * MIN_DISTANCE_BETWEEN_POINTS) {
            return;
        }
        points.addFirst(new TrailPoint(pos));
        while (points.size() > MAX_TRAIL_POINTS) {
            points.pollLast();
        }
    }

    private static void age(ArrayDeque<TrailPoint> points) {
        Iterator<TrailPoint> iterator = points.iterator();
        while (iterator.hasNext()) {
            TrailPoint point = iterator.next();
            point.age++;
            if (point.expired()) {
                iterator.remove();
            }
        }
    }

    private static TrailAnchors trailAnchors(HoverboardEntity hoverboard) {
        float yawRad = hoverboard.getYRot() * Mth.DEG_TO_RAD;
        Vec3 forward = new Vec3(-Mth.sin(yawRad), 0.0D, Mth.cos(yawRad));
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(forward.z, 0.0D, -forward.x);
        Vec3 center = hoverboard.position().add(0.0D, HEIGHT_OFFSET, 0.0D).subtract(forward.scale(REAR_OFFSET));
        return new TrailAnchors(center.subtract(right.scale(LATERAL_OFFSET)), center.add(right.scale(LATERAL_OFFSET)));
    }

    private static void buildPass(VertexConsumer buf, PoseStack.Pose pose, List<PointSnapshot> points, float[] color, Vec3 camPos, boolean glow) {
        int n = points.size();
        for (int i = 0; i < n - 1; i++) {
            PointSnapshot newer = points.get(i);
            PointSnapshot older = points.get(i + 1);

            float alphaNearer = (glow ? 0.34f : 0.78f) * indexAlpha(i, n) * (1.0f - newer.progress());
            float alphaFarther = (glow ? 0.34f : 0.78f) * indexAlpha(i + 1, n) * (1.0f - older.progress());
            if (alphaNearer < 0.005f && alphaFarther < 0.005f) {
                continue;
            }

            Vec3 right = cameraFacingRight(newer.pos(), older.pos(), camPos);
            if (right == null) {
                continue;
            }

            right = right.scale(glow ? GLOW_HALF_WIDTH : CORE_HALF_WIDTH);
            Vec3 na = newer.pos().subtract(camPos);
            Vec3 nb = older.pos().subtract(camPos);

            buf.addVertex(pose, (float) (na.x + right.x), (float) (na.y + right.y), (float) (na.z + right.z)).setColor(color[0], color[1], color[2], alphaNearer);
            buf.addVertex(pose, (float) (na.x - right.x), (float) (na.y - right.y), (float) (na.z - right.z)).setColor(color[0], color[1], color[2], alphaNearer);
            buf.addVertex(pose, (float) (nb.x - right.x), (float) (nb.y - right.y), (float) (nb.z - right.z)).setColor(color[0], color[1], color[2], alphaFarther);
            buf.addVertex(pose, (float) (nb.x + right.x), (float) (nb.y + right.y), (float) (nb.z + right.z)).setColor(color[0], color[1], color[2], alphaFarther);
        }
    }

    private static Vec3 cameraFacingRight(Vec3 a, Vec3 b, Vec3 camPos) {
        Vec3 segDir = b.subtract(a);
        if (segDir.lengthSqr() < 1.0E-12D) {
            return null;
        }
        segDir = segDir.normalize();
        Vec3 mid = a.add(b).scale(0.5D);
        Vec3 toCamera = camPos.subtract(mid);
        if (toCamera.lengthSqr() < 1.0E-12D) {
            return null;
        }
        toCamera = toCamera.normalize();
        Vec3 right = segDir.cross(toCamera);
        if (right.lengthSqr() < 1.0E-10D) {
            right = segDir.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (right.lengthSqr() < 1.0E-10D) {
                return null;
            }
        }
        return right.normalize();
    }

    private static float indexAlpha(int i, int n) {
        if (n <= 1) {
            return 1.0f;
        }
        return 1.0f - (float) i / (float) (n - 1);
    }

    private static float[] colorFor(DyeColor color) {
        if (color == null) {
            return DEFAULT_COLOR;
        }
        return switch (color) {
            case PURPLE -> rgb(0x00FF3C);
            case WHITE -> rgb(0xFF1919);
            case ORANGE -> rgb(0x33CCFF);
            case LIGHT_BLUE -> rgb(0xFF8A00);
            case YELLOW -> rgb(0xB026FF);
            case PINK -> rgb(0x66E0FF);
            case LIME -> rgb(0xFF00E6);
            case GRAY -> rgb(0xFF9F4D);
            case LIGHT_GRAY -> rgb(0x9BFF1A);
            case BLUE -> rgb(0x2ED9A0);
            case BROWN -> rgb(0x66C6FF);
            case GREEN -> rgb(0xFF3B1F);
            case RED -> rgb(0xFF7A00);
            case BLACK -> rgb(0xFFFFFF);
            case MAGENTA -> rgb(0x00E5FF);
            case CYAN -> rgb(0xFF3DDB);
        };
    }

    private static float[] rgb(int color) {
        return new float[] {
                ((color >> 16) & 255) / 255.0f,
                ((color >> 8) & 255) / 255.0f,
                (color & 255) / 255.0f
        };
    }

    private record TrailAnchors(Vec3 left, Vec3 right) {
    }

    private record PointSnapshot(Vec3 pos, float progress) {
    }

    private record TrailSnapshot(List<PointSnapshot> points, float[] color) {
    }
}
