package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.worldgen.thoraxis.ThoraxisUndersideManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jetbrains.annotations.Nullable;

public final class ThoraxisUndersideSkyRenderer {
    public static final ResourceKey<Level> THORAXIS = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "thoraxis")
    );
    private static final Identifier SUN_TEXTURE = Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/eye_moon.png");
    private static final Identifier[] EYE_STAR_TEXTURES = {
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/eye_star1.png"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/eye_star2.png"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/eye_star3.png"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/eye_star4.png"),
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "textures/environment/eye_star5.png")
    };
    private static final float[] EYE_STAR_SCALES = {1.0F, 0.86F, 0.72F, 0.58F, 0.46F};
    private static final double STAR_FOOTPRINT = Math.sqrt(2.0D);
    private static final double STAR_GAP = 1.0D;
    private static final float SKY_RADIUS = 100.0F;
    private static final float BODY_SIZE = 32.0F;
    private static final float SUN_RADIUS = 27.0F;
    private static final float SUN_GLOW_RED = 1.0F;
    private static final float SUN_GLOW_GREEN = 0x1F / 255.0F;
    private static final float SUN_GLOW_BLUE = 0x18 / 255.0F;
    private static final float SUN_GLOW_CENTER_ALPHA = 0.04F;
    private static final float SUN_GLOW_PEAK_RADIUS = 15.5F;
    private static final float SUN_GLOW_PEAK_ALPHA = 0.12F;
    private static final float SUN_GLOW_KNEE_RADIUS = 19.0F;
    private static final float SUN_GLOW_KNEE_ALPHA = 0.03F;
    private static final float SUN_GLOW_RADIUS = 25.0F;
    private static final float[][] EYE_SUNS = {
            {259.2F, -10.0F, 245.5F},
            {200.0F, 20.0F, 30.0F},
            {150.0F, -35.0F, 200.0F},
            {120.0F, 40.0F, 300.0F},
            {215.0F, -60.0F, 120.0F}
    };
    private static final float SKY_RED = 0.015F;
    private static final float SKY_GREEN = 0.0F;
    private static final float SKY_BLUE = 0.035F;
    private static final Map<SkyRenderState, Boolean> STATES = new WeakHashMap<>();

    @Nullable
    private static AntarchySkyDraw.StaticMesh[] eyeStarMeshes = null;

    private ThoraxisUndersideSkyRenderer() {
    }

    public static boolean shouldRender(ClientLevel level, Camera camera) {
        return level.dimension().equals(THORAXIS) && camera.position().y < ThoraxisUndersideManager.GRAVITY_FLIP_Y;
    }

    public static boolean shouldRender(SkyRenderState skyState) {
        return STATES.containsKey(skyState);
    }

    public static void extract(ClientLevel level, Camera camera, float partialTick, SkyRenderState skyState) {
        if (!shouldRender(level, camera)) {
            STATES.remove(skyState);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        boolean skip = minecraft.options.hideLightningFlash().get() || minecraft.gameRenderer.getMainCamera() != camera;
        STATES.put(skyState, skip);
    }

    public static void render(SkyRenderState skyState) {
        Boolean skip = STATES.get(skyState);
        if (skip == null || skip) {
            return;
        }

        Matrix4f modelView = AntarchySkyDraw.modelView();
        AntarchySkyDraw.draw("Antarchy Thoraxis sky disc", RenderPipelines.SKY, AntarchySkyDraw.topSkyDisc(), modelView,
                new Vector4f(SKY_RED, SKY_GREEN, SKY_BLUE, 1.0F), null);

        renderEyeStars(modelView);
        renderEyeSuns(modelView);
    }

    private static void renderEyeStars(Matrix4f modelView) {
        if (eyeStarMeshes == null) {
            eyeStarMeshes = buildEyeStarMeshes();
        }

        for (int i = 0; i < eyeStarMeshes.length; i++) {
            AntarchySkyDraw.StaticMesh mesh = eyeStarMeshes[i];
            if (mesh != null) {
                AntarchySkyDraw.draw("Antarchy Thoraxis eye stars", RenderPipelines.END_SKY, mesh, modelView,
                        AntarchySkyDraw.WHITE, EYE_STAR_TEXTURES[i]);
            }
        }
    }

    private static AntarchySkyDraw.StaticMesh[] buildEyeStarMeshes() {
        Vector3f[] sunDirections = new Vector3f[EYE_SUNS.length];
        for (int sun = 0; sun < EYE_SUNS.length; sun++) {
            sunDirections[sun] = bodyDirection(EYE_SUNS[sun][0], EYE_SUNS[sun][1]);
        }
        double sunLimit = Math.cos(bodyAngularRadius(SUN_RADIUS));
        List<double[]> stars = new ArrayList<>();
        Random random = new Random(552031L);
        for (int i = 0; i < 1800; i++) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            int textureIndex = i % EYE_STAR_TEXTURES.length;
            double size = (0.52F + random.nextFloat() * 1.24F) * EYE_STAR_SCALES[textureIndex];
            double rot = random.nextDouble() * Math.PI * 2.0D;
            double lenSq = x * x + y * y + z * z;
            if (lenSq >= 1.0D || lenSq <= 0.01D) {
                continue;
            }

            double len = 1.0D / Math.sqrt(lenSq);
            x *= len;
            y *= len;
            z *= len;
            if (behindEyeSun(sunDirections, sunLimit, x, y, z) || crowdsExistingStar(stars, x, y, z, size)) {
                continue;
            }
            stars.add(new double[]{x, y, z, size, rot, textureIndex});
        }

        AntarchySkyDraw.StaticMesh[] meshes = new AntarchySkyDraw.StaticMesh[EYE_STAR_TEXTURES.length];
        for (int textureIndex = 0; textureIndex < EYE_STAR_TEXTURES.length; textureIndex++) {
            BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            boolean any = false;

            for (double[] star : stars) {
                if ((int) star[5] != textureIndex) {
                    continue;
                }
                any = true;
                double x = star[0];
                double y = star[1];
                double z = star[2];
                double size = star[3];
                double rot = star[4];
                double sx = x * 100.0D;
                double sy = y * 100.0D;
                double sz = z * 100.0D;
                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double sinRot = Math.sin(rot);
                double cosRot = Math.cos(rot);

                for (int j = 0; j < 4; j++) {
                    double u = (j & 2) - 1;
                    double v = (j + 1 & 2) - 1;
                    double px = u * cosRot - v * sinRot;
                    double py = v * cosRot + u * sinRot;
                    double qx = px * sinPitch;
                    double qy = -px * cosPitch;
                    double rx = qy * sinYaw - py * cosYaw;
                    double ry = py * sinYaw + qy * cosYaw;
                    float quadU = (j == 0 || j == 1) ? 0.0F : 1.0F;
                    float quadV = (j == 0 || j == 3) ? 0.0F : 1.0F;
                    builder.addVertex((float) (sx + rx * size), (float) (sy + qx * size), (float) (sz + ry * size))
                            .setUv(quadU, quadV)
                            .setColor(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }

            if (any) {
                meshes[textureIndex] = AntarchySkyDraw.upload("Antarchy Thoraxis eye star buffer", builder.buildOrThrow());
            } else {
                builder.build();
            }
        }
        return meshes;
    }

    private static boolean behindEyeSun(Vector3f[] sunDirections, double limit, double x, double y, double z) {
        for (Vector3f direction : sunDirections) {
            if (x * direction.x + y * direction.y + z * direction.z > limit) {
                return true;
            }
        }
        return false;
    }

    private static boolean crowdsExistingStar(List<double[]> stars, double x, double y, double z, double size) {
        for (double[] other : stars) {
            double dx = (x - other[0]) * SKY_RADIUS;
            double dy = (y - other[1]) * SKY_RADIUS;
            double dz = (z - other[2]) * SKY_RADIUS;
            double minDistance = (size + other[3]) * STAR_FOOTPRINT + STAR_GAP;
            if (dx * dx + dy * dy + dz * dz < minDistance * minDistance) {
                return true;
            }
        }
        return false;
    }

    private static Vector3f bodyDirection(float orbitDegrees, float yawDegrees) {
        return new Matrix4f()
                .rotateX((float) Math.toRadians(orbitDegrees))
                .rotateZ((float) Math.toRadians(yawDegrees))
                .transformDirection(new Vector3f(0.0F, 1.0F, 0.0F))
                .normalize();
    }

    private static double bodyAngularRadius(float radius) {
        return Math.atan(BODY_SIZE * 0.5F * (radius / 22.0F) / SKY_RADIUS) + 0.02D;
    }

    private static void renderEyeSuns(Matrix4f modelView) {
        for (float[] sun : EYE_SUNS) {
            renderSunGlow(modelView, sun[0], sun[1]);
            renderSunBody(modelView, sun[0], sun[1], sun[2]);
        }
    }

    private static void renderSunGlow(Matrix4f modelView, float orbitDegrees, float yawDegrees) {
        Matrix4f glowModelView = new Matrix4f(modelView)
                .rotateX((float) Math.toRadians(orbitDegrees))
                .rotateZ((float) Math.toRadians(yawDegrees));

        drawGlowBand(glowModelView, 0.0F, SUN_GLOW_CENTER_ALPHA, SUN_GLOW_PEAK_RADIUS, SUN_GLOW_PEAK_ALPHA);
        drawGlowBand(glowModelView, SUN_GLOW_PEAK_RADIUS, SUN_GLOW_PEAK_ALPHA, SUN_GLOW_KNEE_RADIUS, SUN_GLOW_KNEE_ALPHA);
        drawGlowBand(glowModelView, SUN_GLOW_KNEE_RADIUS, SUN_GLOW_KNEE_ALPHA, SUN_GLOW_RADIUS, 0.0F);
    }

    private static void drawGlowBand(Matrix4f modelView, float fromRadius, float fromAlpha, float toRadius, float toAlpha) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 32; i++) {
            float angle = i * Mth.TWO_PI / 32.0F;
            float nextAngle = (i + 1) * Mth.TWO_PI / 32.0F;
            float sin = Mth.sin(angle);
            float cos = Mth.cos(angle);
            float nextSin = Mth.sin(nextAngle);
            float nextCos = Mth.cos(nextAngle);
            buffer.addVertex(sin * fromRadius, SKY_RADIUS, cos * fromRadius).setColor(SUN_GLOW_RED, SUN_GLOW_GREEN, SUN_GLOW_BLUE, fromAlpha);
            buffer.addVertex(sin * toRadius, SKY_RADIUS, cos * toRadius).setColor(SUN_GLOW_RED, SUN_GLOW_GREEN, SUN_GLOW_BLUE, toAlpha);
            buffer.addVertex(nextSin * toRadius, SKY_RADIUS, nextCos * toRadius).setColor(SUN_GLOW_RED, SUN_GLOW_GREEN, SUN_GLOW_BLUE, toAlpha);
            buffer.addVertex(nextSin * fromRadius, SKY_RADIUS, nextCos * fromRadius).setColor(SUN_GLOW_RED, SUN_GLOW_GREEN, SUN_GLOW_BLUE, fromAlpha);
        }
        AntarchySkyDraw.draw("Antarchy Thoraxis sun glow", RenderPipelines.DEBUG_QUADS, buffer.buildOrThrow(), modelView,
                AntarchySkyDraw.WHITE, null);
    }

    private static void renderSunBody(Matrix4f modelView, float orbitDegrees, float yawDegrees, float selfRotationDegrees) {
        Matrix4f bodyModelView = new Matrix4f(modelView)
                .rotateX((float) Math.toRadians(orbitDegrees))
                .rotateZ((float) Math.toRadians(yawDegrees))
                .rotateY((float) Math.toRadians(selfRotationDegrees));

        float halfSize = BODY_SIZE * 0.5F * (SUN_RADIUS / 22.0F);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(-halfSize, SKY_RADIUS, -halfSize).setUv(0.0F, 0.0F);
        buffer.addVertex(halfSize, SKY_RADIUS, -halfSize).setUv(1.0F, 0.0F);
        buffer.addVertex(halfSize, SKY_RADIUS, halfSize).setUv(1.0F, 1.0F);
        buffer.addVertex(-halfSize, SKY_RADIUS, halfSize).setUv(0.0F, 1.0F);
        AntarchySkyDraw.draw("Antarchy Thoraxis sun", RenderPipelines.CELESTIAL, buffer.buildOrThrow(), bodyModelView,
                AntarchySkyDraw.WHITE, SUN_TEXTURE);
    }
}
