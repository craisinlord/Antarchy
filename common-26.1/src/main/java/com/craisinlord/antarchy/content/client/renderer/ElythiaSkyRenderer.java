package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.ElythiaFog;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jetbrains.annotations.Nullable;

public final class ElythiaSkyRenderer {
    public static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );
    private static final Identifier SUN_TEXTURE = Identifier.withDefaultNamespace("textures/environment/celestial/sun.png");
    private static final Identifier[] MOON_PHASE_TEXTURES = {
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/full_moon.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/waning_gibbous.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/third_quarter.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/waning_crescent.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/new_moon.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/waxing_crescent.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/first_quarter.png"),
            Identifier.withDefaultNamespace("textures/environment/celestial/moon/waxing_gibbous.png")
    };
    private static final float SKY_RADIUS = 100.0F;
    private static final float BODY_SIZE = 32.0F;
    private static final float[] HORIZON_FOG_ELEVATIONS = {
            -60.0F, 0.0F, 1.0F, 2.0F, 3.5F, 5.0F, 7.0F, 9.5F, 12.5F, 16.0F, 20.0F, 25.0F, 31.0F, 38.0F, 46.0F, 55.0F, 65.0F, 78.0F, 90.0F
    };
    private static final float SKY_FOG_CEILING = 24.0F;
    private static final float HORIZON_FOG_REBUILD_TOLERANCE = 0.5F;
    private static final int HORIZON_FOG_SEGMENTS = 48;
    private static final int GLOW_SEGMENTS = 16;
    private static final Map<SkyRenderState, State> STATES = new WeakHashMap<>();

    @Nullable
    private static AntarchySkyDraw.StaticMesh coloredStarMesh = null;
    @Nullable
    private static AntarchySkyDraw.StaticMesh horizonFogMesh = null;
    private static float horizonFogStart = Float.NaN;
    private static float horizonFogEnd = Float.NaN;
    private static final Map<Float, AntarchySkyDraw.StaticMesh> GLOW_MESHES = new HashMap<>();
    private static final AntarchySkyDraw.StaticMesh[] BODY_MESHES = new AntarchySkyDraw.StaticMesh[4];
    @Nullable
    private static FogData currentFog;

    private ElythiaSkyRenderer() {
    }

    public static boolean shouldRender(ClientLevel level) {
        return level.dimension().equals(ELYTHIA);
    }

    @Nullable
    public static FogData currentFog() {
        return currentFog;
    }

    public static void captureFog(FogData fog) {
        currentFog = fog;
    }

    public static boolean shouldRender(SkyRenderState skyState) {
        return STATES.containsKey(skyState);
    }

    public static void extract(ClientLevel level, Camera camera, float partialTick, SkyRenderState skyState) {
        if (!shouldRender(level)) {
            STATES.remove(skyState);
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        State state = new State();
        state.skip = minecraft.gui.getBossOverlay().shouldCreateWorldFog()
                || minecraft.gameRenderer.getMainCamera() != camera;

        long dayTime = level.getDefaultClockTime();
        float timeOfDay = timeOfDay(dayTime);
        float red = ARGB.redFloat(skyState.skyColor);
        float green = ARGB.greenFloat(skyState.skyColor);
        float blue = ARGB.blueFloat(skyState.skyColor);
        float rainLevel = level.getRainLevel(partialTick);
        float starBrightness = starBrightness(timeOfDay) * (1.0F - rainLevel);

        float nightPurpleR = 0.055F;
        float nightPurpleG = 0.010F;
        float nightPurpleB = 0.120F;
        state.red = red + nightPurpleR * starBrightness;
        state.green = green + nightPurpleG * starBrightness;
        state.blue = blue + nightPurpleB * starBrightness;
        state.rainLevel = rainLevel;
        state.starBrightness = starBrightness;
        state.dayTime = level.getOverworldClockTime();
        state.gameTime = level.getGameTime() + partialTick;
        state.partialTick = partialTick;
        state.timeDegrees = (dayTime + partialTick) * (360.0D / 24000.0D);
        state.sunAngle = timeOfDay * Mth.TWO_PI;
        state.sunriseColor = sunriseColor(timeOfDay);
        float renderDistance = Math.max(minecraft.options.getEffectiveRenderDistance() * 16.0F, 32.0F);
        state.fogStart = ElythiaFog.start(level, renderDistance, partialTick);
        state.fogEnd = ElythiaFog.end(level, renderDistance, partialTick);

        Entity cameraEntity = camera.entity();
        state.darkDisc = cameraEntity != null
                && cameraEntity.getEyePosition(partialTick).y - level.getLevelData().getHorizonHeight(level) < 0.0D;
        STATES.put(skyState, state);
    }

    public static void render(SkyRenderState skyState) {
        State state = STATES.get(skyState);
        if (state == null || state.skip) {
            return;
        }

        Matrix4f modelView = AntarchySkyDraw.modelView();
        AntarchySkyDraw.draw("Antarchy Elythia sky disc", RenderPipelines.SKY, AntarchySkyDraw.topSkyDisc(), modelView,
                new Vector4f(state.red, state.green, state.blue, 1.0F), null);

        renderHorizonFog(modelView, state.fogStart, state.fogEnd);
        renderSunrise(modelView, state);
        renderCelestialBodies(modelView, state.timeDegrees, state.rainLevel);

        if (state.starBrightness > 0.1F) {
            if (coloredStarMesh == null) {
                coloredStarMesh = buildColoredStarMesh();
            }
            float starBrightness = state.starBrightness;
            AntarchySkyDraw.draw("Antarchy Elythia stars", RenderPipelines.DEBUG_QUADS, coloredStarMesh, modelView,
                    new Vector4f(starBrightness, starBrightness, starBrightness, starBrightness), null);
        }

        ElythiaNightSkyRenderer.render(modelView, state.dayTime, state.gameTime, state.partialTick, state.starBrightness);

        if (state.darkDisc) {
            Matrix4f darkModelView = new Matrix4f(modelView).translate(0.0F, 12.0F, 0.0F);
            AntarchySkyDraw.draw("Antarchy Elythia dark disc", RenderPipelines.SKY, AntarchySkyDraw.bottomSkyDisc(), darkModelView,
                    AntarchySkyDraw.WHITE, null);
        }
    }

    private static float skyFogAlpha(float elevationDegrees, float fogStart, float fogEnd) {
        if (elevationDegrees <= 0.0F) {
            return 1.0F;
        }
        float tangent = (float) Math.tan(Math.toRadians(elevationDegrees));
        float distance = Math.max(SKY_FOG_CEILING / tangent, SKY_FOG_CEILING);
        return Mth.clamp((distance - fogStart) / Math.max(1.0F, fogEnd - fogStart), 0.0F, 1.0F);
    }

    private static void renderHorizonFog(Matrix4f modelView, float fogStart, float fogEnd) {
        if (Float.isNaN(horizonFogStart)
                || Math.abs(fogStart - horizonFogStart) > HORIZON_FOG_REBUILD_TOLERANCE
                || Math.abs(fogEnd - horizonFogEnd) > HORIZON_FOG_REBUILD_TOLERANCE) {
            rebuildHorizonFog(fogStart, fogEnd);
        }
        FogData fog = currentFog;
        if (horizonFogMesh == null || fog == null) {
            return;
        }
        AntarchySkyDraw.draw("Antarchy Elythia horizon fog", RenderPipelines.DEBUG_QUADS, horizonFogMesh, modelView,
                new Vector4f(fog.color.x, fog.color.y, fog.color.z, 1.0F), null);
    }

    private static void rebuildHorizonFog(float fogStart, float fogEnd) {
        horizonFogStart = fogStart;
        horizonFogEnd = fogEnd;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        int segments = HORIZON_FOG_SEGMENTS;
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
                buffer.addVertex(c0 * lowRadius, lowY, s0 * lowRadius).setColor(1.0F, 1.0F, 1.0F, lowAlpha);
                buffer.addVertex(c1 * lowRadius, lowY, s1 * lowRadius).setColor(1.0F, 1.0F, 1.0F, lowAlpha);
                buffer.addVertex(c1 * highRadius, highY, s1 * highRadius).setColor(1.0F, 1.0F, 1.0F, highAlpha);
                buffer.addVertex(c0 * highRadius, highY, s0 * highRadius).setColor(1.0F, 1.0F, 1.0F, highAlpha);
            }
        }
        MeshData mesh = buffer.build();
        if (horizonFogMesh != null) {
            horizonFogMesh.vertices().close();
            horizonFogMesh = null;
        }
        if (mesh != null) {
            horizonFogMesh = AntarchySkyDraw.upload("Antarchy Elythia horizon fog", mesh);
        }
    }

    public static float timeOfDay(long dayTime) {
        double fraction = Mth.frac(dayTime / 24000.0D - 0.25D);
        double eased = 0.5D - Math.cos(fraction * Math.PI) / 2.0D;
        return (float) (fraction * 2.0D + eased) / 3.0F;
    }

    private static float starBrightness(float timeOfDay) {
        float brightness = 1.0F - (Mth.cos(timeOfDay * Mth.TWO_PI) * 2.0F + 0.25F);
        brightness = Mth.clamp(brightness, 0.0F, 1.0F);
        return brightness * brightness * 0.5F;
    }

    private static float @Nullable [] sunriseColor(float timeOfDay) {
        float cos = Mth.cos(timeOfDay * Mth.TWO_PI);
        if (cos < -0.4F || cos > 0.4F) {
            return null;
        }
        float progress = cos / 0.4F * 0.5F + 0.5F;
        float alpha = 1.0F - (1.0F - Mth.sin(progress * Mth.PI)) * 0.99F;
        alpha *= alpha;
        return new float[]{progress * 0.3F + 0.7F, progress * progress * 0.7F + 0.2F, 0.2F, alpha};
    }

    private static AntarchySkyDraw.StaticMesh buildColoredStarMesh() {
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

        return AntarchySkyDraw.upload("Antarchy Elythia star buffer", builder.buildOrThrow());
    }

    private static float[] pickStarColor(Random random) {
        float roll = random.nextFloat();
        if (roll < 0.72F) return new float[]{1.0F, 0.97F, 1.0F};
        if (roll < 0.82F) return new float[]{0.82F, 0.62F, 1.0F};
        if (roll < 0.89F) return new float[]{1.0F, 0.74F, 0.45F};
        if (roll < 0.96F) return new float[]{0.72F, 1.0F, 0.7F};
        return new float[]{1.0F, 0.56F, 0.62F};
    }

    private static void renderSunrise(Matrix4f modelView, State state) {
        float[] sunriseColor = state.sunriseColor;
        float rainLevel = state.rainLevel;
        if (sunriseColor == null || rainLevel >= 1.0F) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(state.sunAngle) < 0.0F ? 180.0F : 0.0F));
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
        AntarchySkyDraw.draw("Antarchy Elythia sunrise", RenderPipelines.DEBUG_TRIANGLE_FAN, buffer.buildOrThrow(), modelView,
                AntarchySkyDraw.WHITE, null);
    }

    private static void renderCelestialBodies(Matrix4f modelView, double timeDegrees, float rainLevel) {

        float sun1Orbit = orbitAngle(timeDegrees, 1.0D, 0.0D);
        renderBodyGlow(modelView, sun1Orbit, 0.0F, 0xD06BFF, rainLevel, 70.0F);
        renderTexturedBody(modelView, 0, sun1Orbit, 0.0F, 38.0F, 0xD06BFF, 1.0F, SUN_TEXTURE, orbitAngle(timeDegrees, 0.8D, 0.0D));

        float sun2Orbit = orbitAngle(timeDegrees, 0.73D, 54.0D);
        renderBodyGlow(modelView, sun2Orbit, -14.0F, 0xFF9A33, rainLevel, 70.0F);
        renderTexturedBody(modelView, 1, sun2Orbit, -14.0F, 16.0F, 0xFF9A33, 1.0F, SUN_TEXTURE, orbitAngle(timeDegrees, -0.5D, 0.0D));

        float moon2Orbit = orbitAngle(timeDegrees, 1.27D, 228.0D);
        renderBodyGlow(modelView, moon2Orbit, 36.0F, 0x4FD84D, rainLevel, 78.0F);
        renderTexturedBody(modelView, 2, moon2Orbit, 36.0F, 14.0F, 0x4FD84D, 1.0F, MOON_PHASE_TEXTURES[1], orbitAngle(timeDegrees, 0.3D, 0.0D));

        float moon3Orbit = orbitAngle(timeDegrees, 0.38D, 122.0D);
        renderBodyGlow(modelView, moon3Orbit, -34.0F, 0xA3FF8F, rainLevel, 78.0F);
        renderTexturedBody(modelView, 3, moon3Orbit, -34.0F, 12.0F, 0xA3FF8F, 1.0F, MOON_PHASE_TEXTURES[2], orbitAngle(timeDegrees, -0.6D, 0.0D));
    }

    private static float orbitAngle(double timeDegrees, double speed, double offset) {
        return (float) ((timeDegrees * speed + offset) % 360.0D);
    }

    private static void renderBodyGlow(
            Matrix4f modelView,
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

        AntarchySkyDraw.StaticMesh glow = GLOW_MESHES.computeIfAbsent(glowRadius, ElythiaSkyRenderer::buildGlowMesh);
        Matrix4f bodyModelView = new Matrix4f(modelView).rotateX(orbitRad).rotateZ(yawRad);
        AntarchySkyDraw.draw("Antarchy Elythia body glow", RenderPipelines.DEBUG_TRIANGLE_FAN, glow, bodyModelView,
                new Vector4f(r, g, b, alpha), null);
    }

    private static AntarchySkyDraw.StaticMesh buildGlowMesh(float glowRadius) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(0.0F, SKY_RADIUS, 0.0F).setColor(1.0F, 1.0F, 1.0F, 1.0F);
        for (int i = 0; i <= GLOW_SEGMENTS; i++) {
            float angle = i * Mth.TWO_PI / GLOW_SEGMENTS;
            buffer.addVertex(Mth.sin(angle) * glowRadius, SKY_RADIUS, Mth.cos(angle) * glowRadius).setColor(1.0F, 1.0F, 1.0F, 0.0F);
        }
        return AntarchySkyDraw.upload("Antarchy Elythia body glow", buffer.buildOrThrow());
    }

    private static void renderTexturedBody(
            Matrix4f modelView,
            int bodyIndex,
            float orbitDegrees,
            float yawDegrees,
            float radius,
            int rgb,
            float alpha,
            Identifier texture,
            float selfRotationDegrees
    ) {
        Matrix4f bodyModelView = new Matrix4f(modelView)
                .rotateX((float) Math.toRadians(orbitDegrees))
                .rotateZ((float) Math.toRadians(yawDegrees))
                .rotateY((float) Math.toRadians(selfRotationDegrees));

        AntarchySkyDraw.StaticMesh body = BODY_MESHES[bodyIndex];
        if (body == null) {
            body = buildBodyMesh(radius);
            BODY_MESHES[bodyIndex] = body;
        }
        AntarchySkyDraw.draw("Antarchy Elythia body", RenderPipelines.CELESTIAL, body, bodyModelView,
                new Vector4f(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, alpha), texture);
    }

    private static AntarchySkyDraw.StaticMesh buildBodyMesh(float radius) {
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float halfSize = BODY_SIZE * 0.5F * (radius / 22.0F);
        buffer.addVertex(-halfSize, SKY_RADIUS, -halfSize).setUv(0.0F, 0.0F);
        buffer.addVertex(halfSize, SKY_RADIUS, -halfSize).setUv(1.0F, 0.0F);
        buffer.addVertex(halfSize, SKY_RADIUS, halfSize).setUv(1.0F, 1.0F);
        buffer.addVertex(-halfSize, SKY_RADIUS, halfSize).setUv(0.0F, 1.0F);
        return AntarchySkyDraw.upload("Antarchy Elythia body", buffer.buildOrThrow());
    }

    private static final class State {
        boolean skip;
        float red;
        float green;
        float blue;
        float rainLevel;
        float starBrightness;
        long dayTime;
        double gameTime;
        float partialTick;
        double timeDegrees;
        float sunAngle;
        float @Nullable [] sunriseColor;
        boolean darkDisc;
        float fogStart;
        float fogEnd;
    }
}
