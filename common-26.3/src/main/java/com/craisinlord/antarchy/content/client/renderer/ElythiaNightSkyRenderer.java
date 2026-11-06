package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.ElythiaEventClient;
import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ElythiaNightSkyRenderer {
    private static final float RADIUS = 99.0F;
    private static final Vector3f UP = new Vector3f(0.0F, 1.0F, 0.0F);
    private static final Vector3f EAST = new Vector3f(1.0F, 0.0F, 0.0F);
    private static final double METEOR_SLOT_TICKS = 30.0D;
    private static final double METEOR_LIFE_TICKS = 18.0D;
    private static final float METEOR_CHANCE = 0.03F;
    private static final float SHOWER_METEOR_CHANCE = 0.3F;
    private static final double SHOWER_SLOT_TICKS = 26.0D;
    private static final int SHOWER_LANES = 2;
    private static final float STARFALL_METEOR_CHANCE = 0.45F;
    private static final double STARFALL_SLOT_TICKS = 20.0D;
    private static final int STARFALL_LANES = 4;
    private static final float BOLIDE_CHANCE = 0.12F;
    private static final int AURORA_COLUMNS = 96;
    private static final int AURORA_ROWS = 4;
    private static final float[] AURORA_ROW_OFFSETS = new float[AURORA_ROWS];
    private static final Vector3f[][] AURORA_POINTS = new Vector3f[AURORA_COLUMNS][AURORA_ROWS];
    private static final float[][][] AURORA_COLORS = new float[AURORA_COLUMNS][AURORA_ROWS][4];

    static {
        for (Vector3f[] column : AURORA_POINTS) {
            for (int row = 0; row < AURORA_ROWS; row++) {
                column[row] = new Vector3f();
            }
        }
    }
    private static final float[] METEOR_HEAD = {1.0F, 0.95F, 1.0F};
    private static final float[] METEOR_TAIL = {0.8F, 0.6F, 1.0F};
    private static final float[][] SHOWER_PALETTE = {
            {1.0F, 0.95F, 1.0F}, {0.85F, 0.75F, 1.0F}, {0.75F, 0.9F, 1.0F}
    };
    private static final float[][] STARFALL_PALETTE = {
            {0.4F, 1.0F, 1.0F}, {1.0F, 0.5F, 0.85F}, {1.0F, 0.85F, 0.4F}, {0.7F, 0.5F, 1.0F}, {0.5F, 1.0F, 0.6F}, {1.0F, 1.0F, 1.0F}
    };

    private record AuroraStyle(int curtains, float baseElevation, float elevationStep, float height, float heightStep,
                               float span, float spanStep, float azimuthStep, float speed, float[][] rowColors, boolean prism) {
    }

    private static final AuroraStyle VEIL = new AuroraStyle(2, 7.0F, 5.0F, 22.0F, 6.0F, 150.0F, 40.0F, 35.0F, 1.0F,
            new float[][]{
                    {0.3F, 1.0F, 0.5F, 0.0F},
                    {0.3F, 1.0F, 0.5F, 1.0F},
                    {0.3F, 0.85F, 0.75F, 0.55F},
                    {0.65F, 0.35F, 1.0F, 0.0F}
            }, false);
    private static final AuroraStyle TIDE = new AuroraStyle(3, 8.0F, 7.0F, 36.0F, 6.0F, 230.0F, 35.0F, 70.0F, 1.6F,
            new float[][]{
                    {0.2F, 0.9F, 1.0F, 0.0F},
                    {0.2F, 0.95F, 0.9F, 1.0F},
                    {0.35F, 0.55F, 1.0F, 0.7F},
                    {1.0F, 0.35F, 0.85F, 0.0F}
            }, false);
    private static final AuroraStyle PRISM = new AuroraStyle(4, 10.0F, 9.0F, 42.0F, 5.0F, 170.0F, 10.0F, 90.0F, 2.4F,
            new float[][]{
                    {1.0F, 1.0F, 1.0F, 0.0F},
                    {1.0F, 1.0F, 1.0F, 1.0F},
                    {1.0F, 1.0F, 1.0F, 0.75F},
                    {1.0F, 1.0F, 1.0F, 0.0F}
            }, true);

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
    private static AntarchySkyDraw.StaticMesh constellationStars;

    private ElythiaNightSkyRenderer() {
    }

    public static void render(Matrix4f modelViewMatrix, long dayTime, double time, float partialTick, float starBrightness) {
        float night = Mth.clamp(starBrightness * 2.0F, 0.0F, 1.0F);
        if (night <= 0.05F) {
            return;
        }
        float timeOfDay = Math.floorMod(dayTime, 24000L) + partialTick;
        if (constellationStars == null) {
            constellationStars = buildConstellationStars();
        }
        float constellationBrightness = Math.min(1.0F, starBrightness * 2.0F);
        AntarchySkyDraw.draw("Antarchy Elythia constellations", CONSTELLATION_PIPELINE, constellationStars, modelViewMatrix,
                new org.joml.Vector4f(constellationBrightness, constellationBrightness, constellationBrightness, constellationBrightness), null);
        BufferBuilder buffer = AntarchySkyDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderAurora(buffer, timeOfDay, time, night, partialTick);
        renderMeteors(buffer, time, night, partialTick);
        MeshData mesh = buffer.build();
        if (mesh != null) {
            AntarchySkyDraw.draw("Antarchy Elythia aurora and meteors", ADDITIVE_PIPELINE, mesh, modelViewMatrix, AntarchySkyDraw.WHITE, null);
        }
    }

    private static boolean renderAurora(BufferBuilder buffer, float timeOfDay, double time, float night, float partialTick) {
        ElythiaEvent event = ElythiaEventClient.shown();
        AuroraStyle style = switch (event) {
            case AURORA -> VEIL;
            case GRAND_AURORA -> TIDE;
            case PRISM_AURORA -> PRISM;
            default -> null;
        };
        if (style == null) {
            return false;
        }
        long seed = ElythiaEventClient.seed();
        float window = smooth(1.0F - Math.abs(timeOfDay - 18000.0F) / 6500.0F);
        float strength = 0.6F + 0.4F * hash01(seed, 0xA1L);
        float intensity = night * window * strength * ElythiaEventClient.strength(partialTick);
        if (intensity <= 0.01F) {
            return false;
        }

        float centerAzimuth = hash01(seed, 0xA2L) * 360.0F;
        float speed = style.speed();
        float[][] rowColors = style.rowColors();
        for (int curtain = 0; curtain < style.curtains(); curtain++) {
            float baseElevation = style.baseElevation() + curtain * style.elevationStep();
            float height = style.height() - curtain * style.heightStep();
            float span = style.span() - curtain * style.spanStep();
            float alphaScale = curtain == 0 ? 1.0F : 0.55F + 0.3F * hash01(seed + curtain, 0xA3L);
            float azimuthStart = centerAzimuth + curtain * style.azimuthStep() - span * 0.5F;
            AURORA_ROW_OFFSETS[0] = -1.5F;
            AURORA_ROW_OFFSETS[1] = 0.0F;
            AURORA_ROW_OFFSETS[2] = height * 0.45F;
            AURORA_ROW_OFFSETS[3] = height;

            for (int j = 0; j < AURORA_COLUMNS; j++) {
                float u = j / (float) (AURORA_COLUMNS - 1);
                float azimuth = azimuthStart + u * span + 6.0F * Mth.sin((float) (u * 9.0F + time * 0.004D * speed + curtain));
                float bottom = baseElevation + 2.5F * Mth.sin((float) (u * 7.0F + time * 0.006D * speed));
                float shimmer = 0.55F + 0.45F * Mth.sin((float) (j * 0.9F + time * 0.05D * speed + curtain))
                        * Mth.sin((float) (j * 0.23F - time * 0.021D * speed));
                float alpha = intensity * alphaScale * Mth.sin(Mth.PI * u) * shimmer;
                float hue = style.prism() ? (float) (u * 1.3F + curtain * 0.27F + time * 0.0015D) : 0.0F;
                for (int row = 0; row < AURORA_ROWS; row++) {
                    skyPoint(azimuth, bottom + AURORA_ROW_OFFSETS[row], AURORA_POINTS[j][row]).mul(RADIUS);
                    float[] color = AURORA_COLORS[j][row];
                    float[] rowColor = rowColors[row];
                    if (style.prism()) {
                        int rgb = Mth.hsvToRgb(Mth.frac(hue + row * 0.06F), 0.75F - row * 0.1F, 1.0F);
                        color[0] = (rgb >> 16 & 0xFF) / 255.0F;
                        color[1] = (rgb >> 8 & 0xFF) / 255.0F;
                        color[2] = (rgb & 0xFF) / 255.0F;
                    } else {
                        color[0] = rowColor[0];
                        color[1] = rowColor[1];
                        color[2] = rowColor[2];
                    }
                    color[3] = rowColor[3] * alpha;
                }
            }

            for (int j = 0; j < AURORA_COLUMNS - 1; j++) {
                for (int row = 0; row < AURORA_ROWS - 1; row++) {
                    auroraVertex(buffer, j, row);
                    auroraVertex(buffer, j + 1, row);
                    auroraVertex(buffer, j + 1, row + 1);
                    auroraVertex(buffer, j, row + 1);
                }
            }
        }
        return true;
    }

    private static void auroraVertex(BufferBuilder buffer, int column, int row) {
        Vector3f point = AURORA_POINTS[column][row];
        float[] color = AURORA_COLORS[column][row];
        buffer.addVertex(point.x, point.y, point.z).setColor(color[0], color[1], color[2], color[3]);
    }

    private static boolean renderMeteors(BufferBuilder buffer, double time, float night, float partialTick) {
        if (night < 0.3F) {
            return false;
        }
        ElythiaEvent event = ElythiaEventClient.shown();
        float strength = ElythiaEventClient.strength(partialTick);
        long seed = ElythiaEventClient.seed();
        boolean drew = false;
        if (event == ElythiaEvent.METEOR_SHOWER) {
            for (int lane = 0; lane < SHOWER_LANES; lane++) {
                float chance = Mth.lerp(strength, lane == 0 ? METEOR_CHANCE : 0.0F, SHOWER_METEOR_CHANCE);
                drew |= renderMeteor(buffer, seed, lane, time, night, chance, SHOWER_SLOT_TICKS, SHOWER_PALETTE, false);
            }
        } else if (event == ElythiaEvent.STARFALL) {
            for (int lane = 0; lane < STARFALL_LANES; lane++) {
                float chance = Mth.lerp(strength, lane == 0 ? METEOR_CHANCE : 0.0F, STARFALL_METEOR_CHANCE);
                drew |= renderMeteor(buffer, seed, lane, time, night, chance, STARFALL_SLOT_TICKS, STARFALL_PALETTE, true);
            }
        } else {
            drew = renderMeteor(buffer, 0L, 0, time, night, METEOR_CHANCE, METEOR_SLOT_TICKS, null, false);
        }
        return drew;
    }

    private static boolean renderMeteor(BufferBuilder buffer, long seed, int lane, double time, float night, float chance,
                                        double slotTicks, float[][] palette, boolean bolides) {
        double laneTime = time + lane * slotTicks * 0.37D;
        long slot = (long) Math.floor(laneTime / slotTicks);
        long key = palette == null ? slot : slot * 31L + lane;
        if (hash01(key, 0xB1L) >= chance) {
            return false;
        }
        boolean bolide = bolides && hash01(key, 0xB6L) < BOLIDE_CHANCE;
        double life = bolide ? METEOR_LIFE_TICKS * 1.6D : METEOR_LIFE_TICKS;
        float progress = (float) ((laneTime - slot * slotTicks) / life);
        if (progress >= 1.0F) {
            return false;
        }

        float azimuth;
        float azimuthTravel;
        float elevation;
        if (palette == null) {
            azimuth = hash01(key, 0xB2L) * 360.0F;
            azimuthTravel = (hash01(key, 0xB4L) - 0.5F) * 50.0F;
            elevation = 25.0F + hash01(key, 0xB3L) * 45.0F;
        } else {
            float radiant = hash01(seed, 0xB7L) * 360.0F;
            float offset = (hash01(key, 0xB2L) - 0.5F) * 140.0F;
            azimuth = radiant + offset;
            azimuthTravel = Math.signum(offset) * (10.0F + hash01(key, 0xB4L) * 30.0F);
            elevation = 35.0F + hash01(key, 0xB3L) * 40.0F;
        }
        float elevationTravel = 10.0F + hash01(key, 0xB5L) * 15.0F;
        if (bolide) {
            azimuthTravel *= 1.5F;
            elevationTravel *= 1.5F;
        }
        Vector3f start = skyPoint(azimuth, elevation);
        Vector3f end = skyPoint(azimuth + azimuthTravel, elevation - elevationTravel);
        float tailLength = bolide ? 0.5F : palette == null ? 0.35F : 0.42F;
        Vector3f head = new Vector3f(start).lerp(end, progress).normalize(RADIUS);
        Vector3f tail = new Vector3f(start).lerp(end, Math.max(0.0F, progress - tailLength)).normalize(RADIUS);
        Vector3f side = new Vector3f(head).sub(tail).cross(head).normalize(bolide ? 0.75F : 0.3F);
        float alpha = night * Mth.sin(Mth.PI * progress);

        float[] headColor = palette == null ? METEOR_HEAD : palette[Math.min(palette.length - 1, (int) (hash01(key, 0xB8L) * palette.length))];
        float[] tailColor = palette == null ? METEOR_TAIL : headColor;
        buffer.addVertex(head.x + side.x, head.y + side.y, head.z + side.z).setColor(headColor[0], headColor[1], headColor[2], alpha);
        buffer.addVertex(head.x - side.x, head.y - side.y, head.z - side.z).setColor(headColor[0], headColor[1], headColor[2], alpha);
        buffer.addVertex(tail.x, tail.y, tail.z).setColor(tailColor[0], tailColor[1], tailColor[2], 0.0F);
        buffer.addVertex(tail.x, tail.y, tail.z).setColor(tailColor[0], tailColor[1], tailColor[2], 0.0F);
        if (bolide) {
            skyQuad(buffer, head, 1.4F * Mth.sin(Mth.PI * progress), (float) time * 0.2F, 1.0F, 1.0F, 1.0F, alpha * 0.8F);
        }
        return true;
    }

    private static AntarchySkyDraw.StaticMesh buildConstellationStars() {
        BufferBuilder buffer = AntarchySkyDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR);
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

    private static final com.mojang.renderpearl.api.pipeline.RenderPipeline CONSTELLATION_PIPELINE = pipeline(false);
    private static final com.mojang.renderpearl.api.pipeline.RenderPipeline ADDITIVE_PIPELINE = pipeline(true);

    private static com.mojang.renderpearl.api.pipeline.RenderPipeline pipeline(boolean additive) {
        var original = net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS;
        var blend = additive
                ? new com.mojang.renderpearl.api.pipeline.BlendFunction(com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_ALPHA, com.mojang.renderpearl.api.pipeline.BlendFactor.ONE,
                        com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_ALPHA, com.mojang.renderpearl.api.pipeline.BlendFactor.ONE)
                : new com.mojang.renderpearl.api.pipeline.BlendFunction(com.mojang.renderpearl.api.pipeline.BlendFactor.SRC_ALPHA, com.mojang.renderpearl.api.pipeline.BlendFactor.ONE_MINUS_SRC_ALPHA,
                        com.mojang.renderpearl.api.pipeline.BlendFactor.ONE, com.mojang.renderpearl.api.pipeline.BlendFactor.ZERO);
        var builder = com.mojang.renderpearl.api.pipeline.RenderPipeline.builder()
                .withLocation(net.minecraft.resources.Identifier.fromNamespaceAndPath("antarchy", additive ? "pipeline/elythia_night_additive" : "pipeline/elythia_constellations"))
                .withCull(false)
                .withPolygonMode(original.getPolygonMode())
                .withDepthStencilState(java.util.Optional.ofNullable(original.getDepthStencilState()).map(state ->
                        new com.mojang.renderpearl.api.pipeline.DepthStencilState(state.depthTest(), false, state.depthBiasScaleFactor(), state.depthBiasConstant())));
        builder.withVertexShader(original.getShaders().get(com.mojang.renderpearl.api.pipeline.ShaderType.VERTEX))
                .withFragmentShader(original.getShaders().get(com.mojang.renderpearl.api.pipeline.ShaderType.FRAGMENT))
                .withPrimitiveTopology(original.getPrimitiveTopology()).withPushConstantSize(original.pushConstantSize());
        for (int index = 0; index < original.getVertexFormatBindings().size(); index++) {
            var format = original.getVertexFormatBinding(index);
            if (format != null) {
                builder.withVertexBinding(index, format);
            }
        }
        for (int index = 0; index < original.getColorTargetStates().size(); index++) {
            var state = original.getColorTargetStates().get(index);
            builder.withColorTargetState(index, new com.mojang.renderpearl.api.pipeline.ColorTargetState(java.util.Optional.of(blend), state.format(), state.writeMask()));
        }
        for (var layout : original.getBindGroupLayouts()) {
            builder.withBindGroupLayout(layout);
        }
        return builder.build();
    }

    private static AntarchySkyDraw.StaticMesh upload(MeshData mesh) {
        return AntarchySkyDraw.upload("Antarchy Elythia constellations", mesh);
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
        return skyPoint(azimuthDegrees, elevationDegrees, new Vector3f());
    }

    private static Vector3f skyPoint(float azimuthDegrees, float elevationDegrees, Vector3f dest) {
        float azimuth = (float) Math.toRadians(azimuthDegrees);
        float elevation = (float) Math.toRadians(elevationDegrees);
        float horizontal = Mth.cos(elevation);
        return dest.set(horizontal * Mth.sin(azimuth), Mth.sin(elevation), horizontal * Mth.cos(azimuth));
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
