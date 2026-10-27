package com.craisinlord.antarchy.fabric.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import org.joml.Matrix4f;

public final class BloodCrystalKatanaTrailHandler {
    private static final int MAX_TRAIL_POINTS = 16;
    private static final int POINT_LIFETIME_TICKS = 10;
    private static final double MIN_DISTANCE_BETWEEN_POINTS = 0.05D;
    private static final float CORE_WIDTH = 0.06F;
    private static final float GLOW_WIDTH = 0.16F;
    private static final Map<Integer, ArrayDeque<TrailPoint>> TRAILS = new HashMap<>();

    private BloodCrystalKatanaTrailHandler() {}

    private static final class TrailPoint {
        private final Vec3 position;
        private int age;

        private TrailPoint(Vec3 position) {
            this.position = position;
        }

        private float alpha() {
            return 1.0F - ((float) age / POINT_LIFETIME_TICKS);
        }

        private boolean expired() {
            return age >= POINT_LIFETIME_TICKS || alpha() <= 0.01F;
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            TRAILS.clear();
            BloodCrystalKatanaTrailClientState.clear();
            return;
        }
        if (mc.isPaused()) {
            return;
        }

        BloodCrystalKatanaTrailClientState.tick();
        Set<Integer> activeIds = new HashSet<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            int remaining = BloodCrystalKatanaTrailClientState.getRemainingTicks(entity.getId());
            if (remaining <= 0) {
                continue;
            }

            activeIds.add(entity.getId());
            Vec3 position = entity.position().add(0.0D, entity.getBbHeight() * 0.45D, 0.0D);
            ArrayDeque<TrailPoint> points = TRAILS.computeIfAbsent(entity.getId(), id -> new ArrayDeque<>());

            if (points.isEmpty() || points.peekFirst().position.distanceToSqr(position) >= MIN_DISTANCE_BETWEEN_POINTS * MIN_DISTANCE_BETWEEN_POINTS) {
                points.addFirst(new TrailPoint(position));
                while (points.size() > MAX_TRAIL_POINTS) {
                    points.pollLast();
                }
            }
        }

        TRAILS.keySet().removeIf(id -> !activeIds.contains(id));
        for (ArrayDeque<TrailPoint> points : TRAILS.values()) {
            Iterator<TrailPoint> iterator = points.iterator();
            while (iterator.hasNext()) {
                TrailPoint point = iterator.next();
                point.age++;
                if (point.expired()) {
                    iterator.remove();
                }
            }
        }
        TRAILS.values().removeIf(ArrayDeque::isEmpty);
    }

    public static void render(LevelRenderContext context) {
        if (TRAILS.isEmpty()) {
            return;
        }

        if (context.poseStack() == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Vec3 cameraPos = context.levelState().cameraRenderState.pos;
        SubmitNodeCollector collector = context.submitNodeCollector();
        PoseStack poseStack = context.poseStack();

        List<List<TrailPoint>> snapshots = new ArrayList<>(TRAILS.size());
        for (ArrayDeque<TrailPoint> deque : TRAILS.values()) {
            if (deque.size() >= 2) {
                snapshots.add(new ArrayList<>(deque));
            }
        }

        if (snapshots.isEmpty()) {
            return;
        }

        collector.submitCustomGeometry(poseStack, AntarchyFabricRenderTypes.TRANSLUCENT_QUADS_NO_CULL, (customPose, buffer) -> {
            Matrix4f matrix = customPose.pose();
            for (List<TrailPoint> points : snapshots) {
                buildTrail(buffer, matrix, points, cameraPos, GLOW_WIDTH, 0.95F, 0.14F, 0.18F, 0.38F);
            }
            for (List<TrailPoint> points : snapshots) {
                buildTrail(buffer, matrix, points, cameraPos, CORE_WIDTH, 1.0F, 0.32F, 0.36F, 0.92F);
            }
        });
    }

    private static void buildTrail(VertexConsumer builder, Matrix4f matrix, List<TrailPoint> points, Vec3 cameraPos,
                                   float width, float red, float green, float blue, float alphaScale) {
        for (int i = 0; i < points.size() - 1; i++) {
            TrailPoint newer = points.get(i);
            TrailPoint older = points.get(i + 1);
            Vec3 side = cameraFacingRight(newer.position, older.position, cameraPos);
            if (side == null) {
                continue;
            }

            float nearAlpha = newer.alpha() * alphaScale;
            float farAlpha = older.alpha() * alphaScale * 0.85F;
            Vec3 nearOffset = side.scale(width);
            Vec3 farOffset = side.scale(width);
            Vec3 nearPos = newer.position.subtract(cameraPos);
            Vec3 farPos = older.position.subtract(cameraPos);

            builder.addVertex(matrix, (float) (nearPos.x + nearOffset.x), (float) (nearPos.y + nearOffset.y), (float) (nearPos.z + nearOffset.z)).setColor(red, green, blue, nearAlpha);
            builder.addVertex(matrix, (float) (nearPos.x - nearOffset.x), (float) (nearPos.y - nearOffset.y), (float) (nearPos.z - nearOffset.z)).setColor(red, green, blue, nearAlpha);
            builder.addVertex(matrix, (float) (farPos.x - farOffset.x), (float) (farPos.y - farOffset.y), (float) (farPos.z - farOffset.z)).setColor(red, green, blue, farAlpha);
            builder.addVertex(matrix, (float) (farPos.x + farOffset.x), (float) (farPos.y + farOffset.y), (float) (farPos.z + farOffset.z)).setColor(red, green, blue, farAlpha);
        }
    }

    private static Vec3 cameraFacingRight(Vec3 current, Vec3 next, Vec3 cameraPos) {
        Vec3 segmentDirection = next.subtract(current);
        if (segmentDirection.lengthSqr() < 1.0E-8D) {
            return null;
        }
        segmentDirection = segmentDirection.normalize();

        Vec3 midpoint = current.add(next).scale(0.5D);
        Vec3 cameraDirection = cameraPos.subtract(midpoint);
        if (cameraDirection.lengthSqr() < 1.0E-8D) {
            return null;
        }
        cameraDirection = cameraDirection.normalize();

        Vec3 side = segmentDirection.cross(cameraDirection);
        if (side.lengthSqr() < 1.0E-8D) {
            side = segmentDirection.cross(new Vec3(0.0D, 1.0D, 0.0D));
            if (side.lengthSqr() < 1.0E-8D) {
                return null;
            }
        }
        return side.normalize();
    }
}
