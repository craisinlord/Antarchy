package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.ThoraxisEventClient;
import com.craisinlord.antarchy.content.weather.ThoraxisBubble;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
public final class ThoraxisBubbleRenderer {
    private static final Identifier SHELL_TEXTURE = texture("shell");
    private static final Identifier FRAME_TEXTURE = texture("frame");
    private static final Identifier CORE_TEXTURE = texture("core");
    private static final int SIDES = 9;
    private static final int BANDS = 6;
    private static final int GLOW_STEPS = 4;
    private static final float EDGE_WIDTH = 0.125F;
    private static final float EDGE_UV = 0.06F;
    private static final double MAX_DISTANCE = 144.0D;
    private static final double DISTANCE_FADE = 24.0D;
    private static final float SQUASH_AMOUNT = 0.22F;
    private static final Vector3f[] CUBE_NORMALS = {
            new Vector3f(0, 1, 0), new Vector3f(0, -1, 0), new Vector3f(1, 0, 0),
            new Vector3f(-1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, 0, -1)
    };
    private static final Style SLOW = new Style(
            new float[]{0.62F, 0.16F, 0.3F},
            new float[]{0.75F, 0.25F, 0.27F},
            new float[]{0.57F, 0.15F, 0.14F},
            0.004F, 0.35F, 0.035F);
    private static final Style FAST = new Style(
            new float[]{0.87F, 0.22F, 0.24F},
            new float[]{1.0F, 0.58F, 0.4F},
            new float[]{0.96F, 0.67F, 0.35F},
            0.06F, 1.0F, 0.09F);

    private record Style(float[] body, float[] edge, float[] core, float spin, float tumble, float pulse) {
    }

    private record Shape(ThoraxisBubble bubble, Style style, Vector3f center, float alpha, float seed, Vector3f axis,
                         Vector3f[][] points, float squash) {
    }

    private static final Vector3f[][] UNIT_SHELL = unitShell();
    private static final Vector3f X_AXIS = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final Vector3f Y_AXIS = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final List<Vector3f[][]> POINT_POOL = new ArrayList<>();
    private static final Quaternionf ROTATION = new Quaternionf();
    private static final Quaternionf ORBIT_TILT = new Quaternionf();
    private static final Vector3f TILT_AXIS = new Vector3f();
    private static final Vector3f FRAME_CENTER = new Vector3f();
    private static final Vector3f SHARD_CENTER = new Vector3f();
    private static final Vector3f OFFSET = new Vector3f();
    private static final Vector3f MIDDLE = new Vector3f();
    private static final Vector3f DIRECTION = new Vector3f();
    private static final Vector3f SIDE = new Vector3f();
    private static final Vector3f NORMAL = new Vector3f();
    private static final Vector3f TANGENT = new Vector3f();
    private static final Vector3f BITANGENT = new Vector3f();
    private static final Vector3f FACE_CENTER = new Vector3f();
    private static final Vector3f U = new Vector3f();
    private static final Vector3f V = new Vector3f();
    private static final float[] FACE_COLOR = new float[3];

    private ThoraxisBubbleRenderer() {
    }

    private static Identifier texture(String name) {
        return Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/entity/temporal_bubble/" + name + ".png");
    }

    private static Vector3f[][] unitShell() {
        Vector3f[][] points = new Vector3f[BANDS + 1][SIDES];
        for (int i = 0; i <= BANDS; i++) {
            float theta = Mth.PI * i / BANDS;
            for (int j = 0; j < SIDES; j++) {
                float phi = Mth.TWO_PI * (j + (i % 2) * 0.5F) / SIDES;
                points[i][j] = new Vector3f(Mth.sin(theta) * Mth.cos(phi), Mth.cos(theta), Mth.sin(theta) * Mth.sin(phi));
            }
        }
        return points;
    }

    private static Vector3f[][] pooledPoints(int index) {
        while (POINT_POOL.size() <= index) {
            Vector3f[][] points = new Vector3f[BANDS + 1][SIDES];
            for (Vector3f[] band : points) {
                for (int j = 0; j < SIDES; j++) {
                    band[j] = new Vector3f();
                }
            }
            POINT_POOL.add(points);
        }
        return POINT_POOL.get(index);
    }

    public static void render(PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, Vec3 cameraPos, float partialTick) {
        List<Shape> shapes = shapes(cameraPos, partialTick);
        if (shapes.isEmpty()) return;
        double time = Minecraft.getInstance().level.getGameTime() + partialTick;
        collector.submitCustomGeometry(poseStack, AntarchySkyDraw.additiveTexture(SHELL_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            for (Shape shape : shapes) drawShell(consumer, matrix, shape);
        });
        collector.submitCustomGeometry(poseStack, AntarchySkyDraw.additiveTexture(FRAME_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            for (Shape shape : shapes) {
                drawShellEdges(consumer, matrix, shape);
                drawFrames(consumer, matrix, shape, time);
            }
        });
        collector.submitCustomGeometry(poseStack, AntarchySkyDraw.additiveTexture(CORE_TEXTURE), (pose, consumer) -> {
            Matrix4f matrix = pose.pose();
            for (Shape shape : shapes) drawCore(consumer, matrix, shape, time);
        });
    }
    private static List<Shape> shapes(Vec3 cameraPos, float partialTick) {
        List<ThoraxisBubble> bubbles = ThoraxisEventClient.bubbles();
        Minecraft minecraft = Minecraft.getInstance();
        if (bubbles.isEmpty() || minecraft.level == null) return List.of();
        float visibility = ThoraxisEventClient.bubbleVisibility(partialTick);
        if (visibility <= 0.01F) return List.of();
        float solidity = ThoraxisEventClient.strength(partialTick);
        double time = minecraft.level.getGameTime() + partialTick;
        List<Shape> shapes = new ArrayList<>();
        for (ThoraxisBubble bubble : bubbles) {
            Vec3 center = ThoraxisEventClient.bubblePosition(bubble, time);
            double distance = center.distanceTo(cameraPos);
            if (distance > MAX_DISTANCE) continue;
            float alpha = visibility * bubble.presence(time) * Mth.lerp(solidity, 0.4F, 1.0F)
                    * (float) Mth.clamp((MAX_DISTANCE - distance) / DISTANCE_FADE, 0.0D, 1.0D);
            if (alpha <= 0.01F) continue;
            float squash = ThoraxisEventClient.bubbleSquash(bubble, time) * SQUASH_AMOUNT;
            center = center.add(0.0D, bubble.down() * bubble.radius() * squash, 0.0D);
            float seed = (bubble.id().hashCode() & 0xFFFF) / 2048.0F;
            Shape shape = new Shape(bubble, bubble.slow() ? SLOW : FAST,
                    new Vector3f((float) (center.x - cameraPos.x), (float) (center.y - cameraPos.y), (float) (center.z - cameraPos.z)),
                    alpha, seed, new Vector3f(Mth.sin(seed), 1.6F, Mth.cos(seed)).normalize(), pooledPoints(shapes.size()), squash);
            shellPoints(shape, time);
            shapes.add(shape);
        }
        return shapes;
    }
    private static void shellPoints(Shape shape, double time) {
        Quaternionf spin = ROTATION.rotationAxis((float) (time * shape.style().spin()) + shape.seed(), shape.axis());
        float radius = shape.bubble().radius();
        float spread = radius * (1.0F + shape.squash() * 0.5F);
        float height = radius * (1.0F - shape.squash());
        Vector3f[][] points = shape.points();
        for (int i = 0; i <= BANDS; i++) {
            for (int j = 0; j < SIDES; j++) {
                spin.transform(UNIT_SHELL[i][j], points[i][j]).mul(spread, height, spread).add(shape.center());
            }
        }
    }

    private static void drawShell(VertexConsumer buffer, Matrix4f matrix, Shape shape) {
        Vector3f[][] points = shape.points();
        float[] color = shape.style().body();
        for (int i = 0; i < BANDS; i++) {
            for (int j = 0; j < SIDES; j++) {
                int next = (j + 1) % SIDES;
                Vector3f a = points[i][j];
                Vector3f b = points[i + 1][j];
                Vector3f c = points[i + 1][next];
                Vector3f d = points[i][next];
                Vector3f middle = MIDDLE.set(a).add(b).add(c).add(d).mul(0.25F);
                Vector3f normal = NORMAL.set(middle).sub(shape.center()).normalize();
                float middleLengthSqr = middle.lengthSquared();
                float facing = middleLengthSqr > 1.0E-4F ? Math.abs(normal.dot(middle) / Mth.sqrt(middleLengthSqr)) : 1.0F;
                float step = Math.round((1.0F - facing) * GLOW_STEPS) / (float) GLOW_STEPS;
                float alpha = shape.alpha() * (0.12F + 0.78F * step * step);
                float top = a.distance(d);
                float bottom = b.distance(c);
                float height = a.distance(b);
                vertex(buffer, matrix, a.x, a.y, a.z, 0.0F, 0.0F, color, alpha);
                vertex(buffer, matrix, b.x, b.y, b.z, 0.0F, height, color, alpha);
                vertex(buffer, matrix, c.x, c.y, c.z, bottom, height, color, alpha);
                vertex(buffer, matrix, d.x, d.y, d.z, top, 0.0F, color, alpha);
            }
        }
    }

    private static void drawShellEdges(VertexConsumer buffer, Matrix4f matrix, Shape shape) {
        Vector3f[][] points = shape.points();
        float[] color = shape.style().edge();
        float alpha = shape.alpha();
        for (int i = 1; i < BANDS; i++) {
            for (int j = 0; j < SIDES; j++) {
                edge(buffer, matrix, points[i][j], points[i][(j + 1) % SIDES], color, alpha);
            }
        }
        for (int i = 0; i < BANDS; i++) {
            for (int j = 0; j < SIDES; j++) {
                edge(buffer, matrix, points[i][j], points[i + 1][j], color, alpha * 0.7F);
            }
        }
    }

    private static void drawFrames(VertexConsumer buffer, Matrix4f matrix, Shape shape, double time) {
        Style style = shape.style();
        float t = (float) time;
        float tumble = style.tumble();
        float bob = 0.15F * Mth.sin(t * 0.05F + shape.seed());
        Vector3f center = FRAME_CENTER.set(shape.center()).add(0.0F, bob, 0.0F);
        cube(buffer, matrix, center, 0.85F, ROTATION.rotationXYZ(t * 0.031F * tumble + shape.seed(), t * 0.047F * tumble, t * 0.023F * tumble),
                style.edge(), shape.alpha() * 0.95F, false);
        cube(buffer, matrix, center, 0.6F, ROTATION.rotationXYZ(-t * 0.043F * tumble, t * 0.029F * tumble + shape.seed(), -t * 0.053F * tumble),
                style.edge(), shape.alpha() * 0.85F, false);

        Quaternionf orbitTilt = ORBIT_TILT.rotationAxis(0.5F, TILT_AXIS.set(shape.axis().z, 0.0F, -shape.axis().x).normalize());
        float orbit = t * 0.02F * tumble * 3.0F + shape.seed();
        for (int k = 0; k < SIDES; k++) {
            float angle = orbit + Mth.TWO_PI * k / SIDES;
            orbitTilt.transform(Mth.cos(angle) * 1.6F, 0.0F, Mth.sin(angle) * 1.6F, OFFSET);
            cube(buffer, matrix, SHARD_CENTER.set(center).add(OFFSET), 0.14F, ROTATION.rotationXYZ(angle * 2.0F, t * 0.07F * tumble, angle),
                    style.edge(), shape.alpha() * 0.9F, true);
        }
    }

    private static void drawCore(VertexConsumer buffer, Matrix4f matrix, Shape shape, double time) {
        Style style = shape.style();
        float t = (float) time;
        float pulse = 1.0F + 0.12F * Mth.sin(t * style.pulse() * 2.0F + shape.seed());
        Quaternionf spin = ROTATION.rotationXYZ(0.6F, t * 0.04F * style.tumble() + shape.seed(), 0.6F);
        float bob = 0.15F * Mth.sin(t * 0.05F + shape.seed());
        cube(buffer, matrix, FRAME_CENTER.set(shape.center()).add(0.0F, bob, 0.0F), 0.32F * pulse, spin, style.core(), shape.alpha(), true);
    }

    private static void cube(VertexConsumer buffer, Matrix4f matrix, Vector3f center, float half, Quaternionf rotation, float[] color,
                             float alpha, boolean shaded) {
        for (Vector3f baseNormal : CUBE_NORMALS) {
            Vector3f normal = rotation.transform(baseNormal, NORMAL);
            Vector3f tangent = rotation.transform(Math.abs(baseNormal.y) > 0.5F ? X_AXIS : Y_AXIS, TANGENT);
            Vector3f bitangent = normal.cross(tangent, BITANGENT);
            float shade = shaded ? 0.55F + 0.45F * (normal.y * 0.5F + 0.5F) : 1.0F;
            FACE_COLOR[0] = color[0] * shade;
            FACE_COLOR[1] = color[1] * shade;
            FACE_COLOR[2] = color[2] * shade;
            Vector3f faceCenter = FACE_CENTER.set(normal).mul(half).add(center);
            Vector3f u = U.set(tangent).mul(half);
            Vector3f v = V.set(bitangent).mul(half);
            vertex(buffer, matrix, faceCenter.x - u.x - v.x, faceCenter.y - u.y - v.y, faceCenter.z - u.z - v.z, 0.0F, 1.0F, FACE_COLOR, alpha);
            vertex(buffer, matrix, faceCenter.x + u.x - v.x, faceCenter.y + u.y - v.y, faceCenter.z + u.z - v.z, 1.0F, 1.0F, FACE_COLOR, alpha);
            vertex(buffer, matrix, faceCenter.x + u.x + v.x, faceCenter.y + u.y + v.y, faceCenter.z + u.z + v.z, 1.0F, 0.0F, FACE_COLOR, alpha);
            vertex(buffer, matrix, faceCenter.x - u.x + v.x, faceCenter.y - u.y + v.y, faceCenter.z - u.z + v.z, 0.0F, 0.0F, FACE_COLOR, alpha);
        }
    }

    private static void edge(VertexConsumer buffer, Matrix4f matrix, Vector3f from, Vector3f to, float[] color, float alpha) {
        Vector3f middle = MIDDLE.set(from).add(to).mul(0.5F);
        Vector3f side = DIRECTION.set(to).sub(from).cross(middle, SIDE);
        if (side.lengthSquared() < 1.0E-6F) {
            return;
        }
        side.normalize(EDGE_WIDTH * 0.5F);
        vertex(buffer, matrix, from.x + side.x, from.y + side.y, from.z + side.z, 0.0F, 0.0F, color, alpha);
        vertex(buffer, matrix, from.x - side.x, from.y - side.y, from.z - side.z, EDGE_UV, 0.0F, color, alpha);
        vertex(buffer, matrix, to.x - side.x, to.y - side.y, to.z - side.z, EDGE_UV, EDGE_UV, color, alpha);
        vertex(buffer, matrix, to.x + side.x, to.y + side.y, to.z + side.z, 0.0F, EDGE_UV, color, alpha);
    }

    private static void vertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float u, float v, float[] color, float alpha) {
        buffer.addVertex(matrix, x, y, z).setUv(u, v).setColor(color[0], color[1], color[2], alpha);
    }
}
