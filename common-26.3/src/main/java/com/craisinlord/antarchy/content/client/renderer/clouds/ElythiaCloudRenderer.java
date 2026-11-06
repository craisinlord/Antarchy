package com.craisinlord.antarchy.content.client.renderer.clouds;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import com.craisinlord.antarchy.content.client.renderer.AntarchySkyDraw;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.ElythiaWind;
import com.craisinlord.antarchy.content.client.renderer.ElythiaSkyRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class ElythiaCloudRenderer {
    private static BufferBuilder beginBuilder() {
        return AntarchySkyDraw.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
    }

    private static RenderPipeline createCloudPipeline() {
        RenderPipeline original = RenderPipelines.DEBUG_QUADS;
        BlendFunction blend = new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA, BlendFactor.ONE, BlendFactor.ZERO);
        var builder = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/elythia_clouds"))
                .withCull(false)
                .withPolygonMode(original.getPolygonMode())
                .withDepthStencilState(Optional.ofNullable(original.getDepthStencilState()).map(state ->
                        new com.mojang.renderpearl.api.pipeline.DepthStencilState(state.depthTest(), false, state.depthBiasScaleFactor(), state.depthBiasConstant())))
                .withVertexShader(Identifier.fromNamespaceAndPath("antarchy", "core/elythia_cloud"))
                .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/elythia_cloud"))
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL)
                .withColorTargetState(new ColorTargetState(Optional.of(blend), original.getColorTargetStates().get(0).format(), original.getColorTargetStates().get(0).writeMask()));
        return builder.build();
    }

        private static final double DRIFT_PER_TICK = 0.03D;
    static final RenderPipeline CLOUD_PIPELINE = createCloudPipeline();
    private static final float MIN_CLOUD_RANGE = 512.0F;
    private static final float FOG_START_FRACTION = 0.65F;
    private static final float SOFT_PUFF_WIDTH = 13.0F;
    private static final float SOFT_PUFF_HEIGHT = 7.0F;
    private static final float SOFT_VERTICAL_SPREAD = 1.5F;
    private static final int SECTION_CELLS = 32;
    private static final float CLEARING_DIM = 0.3F;

    private static final Layer[] LAYERS = {
            new Layer(ElythiaCloudField.LOW, 240.0F, 0.8D, 0.0D, 0.8F, true),
            new Layer(ElythiaCloudField.MID, 320.0F, 1.2D, 20.0D, 0.8F, true),
            new Layer(ElythiaCloudField.HIGH, 510.0F, 2.0D, 40.0D, 0.7F, false)
    };

    private ElythiaCloudRenderer() {
    }

    public static void render(Minecraft minecraft, ClientLevel level, CloudStatus status, Vec3 cameraPosition,
                              long gameTime, float partialTick, int packedCloudColor, int cloudRangeChunks) {
        if (status == CloudStatus.OFF) {
            return;
        }
        boolean anyReady = false;
        for (Layer layer : LAYERS) {
            anyReady |= layer.sections() != null;
        }
        if (!anyReady) {
            return;
        }
        Vec3 cloudColor = new Vec3((packedCloudColor >> 16 & 255) / 255.0D, (packedCloudColor >> 8 & 255) / 255.0D,
                (packedCloudColor & 255) / 255.0D);
        double[] wind = ElythiaWind.displacement(gameTime + partialTick);
        FogData fog = ElythiaSkyRenderer.currentFog();
        float terrainFogEnd = fog != null ? fog.environmentalEnd : cloudRangeChunks * 16.0F;
        float cloudRange = Math.max(terrainFogEnd, MIN_CLOUD_RANGE);
        CameraRenderState camera = minecraft.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        float depthFar = camera.depthFar;
        Matrix4f modelView = AntarchySkyDraw.modelView();
        Frustum frustum = new Frustum(modelView, camera.projectionMatrix);
        frustum.prepare(0.0D, 0.0D, 0.0D);
        RenderTarget target = null;
        double camX = cameraPosition.x;
        double camY = cameraPosition.y;
        double camZ = cameraPosition.z;

        List<ElythiaSoftCloudPass.Draw> softDraws = new ArrayList<>();
        boolean softEnabled = AntarchySettings.elythiaSoftCloudsEnabled() && ElythiaSoftCloudPass.supported();
        ElythiaGiantCloudRenderer.render(minecraft, level, cloudColor, status, wind, camX, camY, camZ, partialTick,
                terrainFogEnd, softEnabled, softDraws, modelView, target);
        float clearing = ElythiaGiantCloudRenderer.clearing();

        for (Layer layer : LAYERS) {
            float visibility = layer.visibility(clearing);
            if (!layer.soft() && visibility > 0.0F) {
                Vector4f color = new Vector4f((float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, visibility);
                drawLayer(layer, modelView, color, frustum, wind, camX, camY, camZ, terrainFogEnd, cloudRange, depthFar, target);
            }
        }

        for (Layer layer : LAYERS) {
            float visibility = layer.visibility(clearing);
            if (layer.soft() && visibility > 0.0F) {
                collectSoftDraws(layer, softDraws, visibility, frustum, wind, camX, camY, camZ, terrainFogEnd, cloudRange, depthFar);
            }
        }
        if (!softDraws.isEmpty()) {
            float[] fogColor = fog != null ? new float[]{fog.color.x, fog.color.y, fog.color.z} : new float[]{0.0F, 0.0F, 0.0F};
            double sunAngle = ElythiaSkyRenderer.timeOfDay(level.getDefaultClockTime()) * Math.PI * 2.0D;
            float sunEast = (float) (0.5D - 0.5D * Math.sin(sunAngle));
            float sunStrength = Mth.clamp((float) Math.cos(sunAngle) * 3.0F + 0.3F, 0.0F, 0.85F);
            ElythiaSoftCloudPass.render(softDraws, modelView, new ElythiaSoftCloudPass.Frame(
                    cloudColor, fogColor, sunEast, sunStrength,
                    ElythiaGiantCloudRenderer.clearingData(), ElythiaGiantCloudRenderer.clearingCount(), clearing), target);
        }
    }

    private static void drawLayer(Layer layer, Matrix4f modelView, Vector4f color, Frustum frustum, double[] wind,
                                  double camX, double camY, double camZ, float terrainFogEnd, float cloudRange, float depthFar,
                                  @Nullable RenderTarget target) {
        List<Section> sections = layer.sections();
        if (sections == null) {
            return;
        }
        float vertical = (float) Math.abs(layer.baseY + layer.thickness() * 0.5F - camY);
        float fogEnd = Math.max(terrainFogEnd, vertical + cloudRange);
        Vector3f fogRange = new Vector3f(fogEnd * FOG_START_FRACTION, fogEnd, 0.0F);
        forEachSection(layer, sections, frustum, wind, camX, camY, camZ, Math.min(fogEnd, depthFar),
                (section, originX, originY, originZ) -> {
                    Matrix4f transform = new Matrix4f(modelView).translate(originX, originY, originZ);
                    AntarchySkyDraw.draw("Antarchy Elythia cloud layer", CLOUD_PIPELINE, section.buffer(), transform, color, fogRange, target);
                });
    }

    private static void collectSoftDraws(
            Layer layer,
            List<ElythiaSoftCloudPass.Draw> draws,
            float visibility,
            Frustum frustum,
            double[] wind,
            double camX,
            double camY,
            double camZ,
            float terrainFogEnd,
            float cloudRange,
            float depthFar
    ) {
        List<Section> sections = layer.sections();
        if (sections == null) {
            return;
        }
        float vertical = (float) Math.abs(layer.baseY + layer.thickness() * 0.5F - camY);
        float fogEnd = Math.max(terrainFogEnd, vertical + cloudRange);
        forEachSection(layer, sections, frustum, wind, camX, camY, camZ, Math.min(fogEnd, depthFar),
                (section, originX, originY, originZ) -> draws.add(
                        new ElythiaSoftCloudPass.Draw(section.buffer(), originX, originY, originZ, fogEnd, visibility,
                                ElythiaSoftCloudPass.NO_GROWTH_LIMIT, true)));
    }

    private interface SectionConsumer {
        void accept(Section section, float originX, float originY, float originZ);
    }

    private record Section(AntarchySkyDraw.StaticMesh buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    }

    private static void forEachSection(Layer layer, List<Section> sections, Frustum frustum, double[] wind,
                                       double camX, double camY, double camZ, float range, SectionConsumer consumer) {
        double span = layer.span();
        double cos = Math.cos(layer.windOffset);
        double sin = Math.sin(layer.windOffset);
        double scale = DRIFT_PER_TICK * layer.speed;
        double driftX = ((wind[0] * cos - wind[1] * sin) * scale) % span;
        double driftZ = ((wind[0] * sin + wind[1] * cos) * scale) % span;
        double patternX = camX - driftX;
        double patternZ = camZ - driftZ;
        int tileX = Mth.floor(patternX / span);
        int tileZ = Mth.floor(patternZ / span);
        float offsetY = (float) (layer.baseY - camY);
        double maxDistanceSq = (double) range * range;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                double originX = (tileX + dx) * span - patternX;
                double originZ = (tileZ + dz) * span - patternZ;
                double tileNearX = Mth.clamp(0.0D, originX, originX + span);
                double tileNearZ = Mth.clamp(0.0D, originZ, originZ + span);
                if (tileNearX * tileNearX + tileNearZ * tileNearZ > maxDistanceSq) {
                    continue;
                }
                for (Section section : sections) {
                    double minX = originX + section.minX();
                    double maxX = originX + section.maxX();
                    double minZ = originZ + section.minZ();
                    double maxZ = originZ + section.maxZ();
                    double nearX = Mth.clamp(0.0D, minX, maxX);
                    double nearZ = Mth.clamp(0.0D, minZ, maxZ);
                    if (nearX * nearX + nearZ * nearZ > maxDistanceSq) {
                        continue;
                    }
                    if (!frustum.isVisible(new AABB(minX, offsetY + section.minY(), minZ, maxX, offsetY + section.maxY(), maxZ))) {
                        continue;
                    }
                    consumer.accept(section, (float) originX, offsetY, (float) originZ);
                }
            }
        }
    }

    @Nullable
    private static List<Section> buildSections(ElythiaCloudField field, float alpha, boolean soft) {
        int n = field.size();
        float cellWidth = field.spec().cellWidth();
        float cellHeight = field.spec().cellHeight();
        float horizontalPad;
        float minY;
        float maxY;
        if (soft) {
            horizontalPad = SOFT_PUFF_WIDTH * (cellWidth / 6.0F) + cellWidth;
            minY = -SOFT_PUFF_HEIGHT;
            maxY = Math.max(1, field.spec().maxHeight()) * cellHeight * SOFT_VERTICAL_SPREAD + SOFT_PUFF_HEIGHT;
        } else {
            horizontalPad = 0.0F;
            minY = -1.0F;
            maxY = field.spec().kind() == ElythiaCloudField.Kind.WISP ? 1.0F : field.spec().maxHeight() * cellHeight + 1.0F;
        }
        List<Section> sections = new ArrayList<>();
        try {
            for (int z0 = 0; z0 < n; z0 += SECTION_CELLS) {
                for (int x0 = 0; x0 < n; x0 += SECTION_CELLS) {
                    int x1 = Math.min(n, x0 + SECTION_CELLS);
                    int z1 = Math.min(n, z0 + SECTION_CELLS);
                    AntarchySkyDraw.StaticMesh buffer = soft ? buildSoftBuffer(field, x0, z0, x1, z1) : buildBuffer(field, alpha, x0, z0, x1, z1);
                    if (buffer != null) {
                        sections.add(new Section(buffer,
                                x0 * cellWidth - horizontalPad, minY, z0 * cellWidth - horizontalPad,
                                x1 * cellWidth + horizontalPad, maxY, z1 * cellWidth + horizontalPad));
                    }
                }
            }
        } catch (RuntimeException exception) {
            closeSections(sections);
            throw exception;
        }
        return sections;
    }

    private static void closeSections(@Nullable List<Section> sections) {
        if (sections == null) {
            return;
        }
        for (Section section : sections) {
            section.buffer().vertices().close();
        }
    }

    @Nullable
    private static AntarchySkyDraw.StaticMesh buildBuffer(ElythiaCloudField field, float alpha, int startX, int startZ, int endX, int endZ) {
        boolean wisp = field.spec().kind() == ElythiaCloudField.Kind.WISP;
        float cellWidth = field.spec().cellWidth();
        float cellHeight = field.spec().cellHeight();
        BufferBuilder builder = beginBuilder();

            for (int z = startZ; z < endZ; z++) {
                int x = startX;
                while (x < endX) {
                    int height = field.height(x, z);
                    if (height == 0) {
                        x++;
                        continue;
                    }
                    int end = x;
                    while (end + 1 < endX && field.height(end + 1, z) == height) {
                        end++;
                    }
                    float x0 = x * cellWidth;
                    float x1 = (end + 1) * cellWidth;
                    float z0 = z * cellWidth;
                    float z1 = z0 + cellWidth;
                    if (wisp) {
                        quad(builder, 0.9F, alpha, 0.0F, -1.0F, 0.0F,
                                x0, 0.0F, z0, x1, 0.0F, z0, x1, 0.0F, z1, x0, 0.0F, z1);
                    } else {
                        float top = height * cellHeight;
                        quad(builder, 1.0F, alpha, 0.0F, 1.0F, 0.0F,
                                x0, top, z0, x0, top, z1, x1, top, z1, x1, top, z0);
                        quad(builder, 0.7F, alpha, 0.0F, -1.0F, 0.0F,
                                x0, 0.0F, z0, x1, 0.0F, z0, x1, 0.0F, z1, x0, 0.0F, z1);
                    }
                    x = end + 1;
                }
            }

            if (!wisp) {
                for (int z = startZ; z < endZ; z++) {
                    for (int x = startX; x < endX; x++) {
                        int height = field.height(x, z);
                        if (height == 0) {
                            continue;
                        }
                        float x0 = x * cellWidth;
                        float x1 = x0 + cellWidth;
                        float z0 = z * cellWidth;
                        float z1 = z0 + cellWidth;
                        float top = height * cellHeight;

                        float west = field.height(x - 1, z) * cellHeight;
                        if (west < top) {
                            quad(builder, 0.9F, alpha, -1.0F, 0.0F, 0.0F,
                                    x0, west, z0, x0, west, z1, x0, top, z1, x0, top, z0);
                        }
                        float east = field.height(x + 1, z) * cellHeight;
                        if (east < top) {
                            quad(builder, 0.9F, alpha, 1.0F, 0.0F, 0.0F,
                                    x1, east, z0, x1, top, z0, x1, top, z1, x1, east, z1);
                        }
                        float north = field.height(x, z - 1) * cellHeight;
                        if (north < top) {
                            quad(builder, 0.8F, alpha, 0.0F, 0.0F, -1.0F,
                                    x0, north, z0, x0, top, z0, x1, top, z0, x1, north, z0);
                        }
                        float south = field.height(x, z + 1) * cellHeight;
                        if (south < top) {
                            quad(builder, 0.8F, alpha, 0.0F, 0.0F, 1.0F,
                                    x0, south, z1, x1, south, z1, x1, top, z1, x0, top, z1);
                        }
                    }
                }
            }

            MeshData mesh = builder.build();
            if (mesh == null) {
                return null;
            }
            return AntarchySkyDraw.upload("Antarchy Elythia cloud section", mesh);
    }

    @Nullable
    private static AntarchySkyDraw.StaticMesh buildSoftBuffer(ElythiaCloudField field, int startX, int startZ, int endX, int endZ) {
        float cellWidth = field.spec().cellWidth();
        float cellHeight = field.spec().cellHeight();
        float maxThickness = Math.max(1, field.spec().maxHeight()) * cellHeight * SOFT_VERTICAL_SPREAD;
        float puffScale = cellWidth / 6.0F;
        BufferBuilder builder = beginBuilder();
            for (int z = startZ; z < endZ; z++) {
                for (int x = startX; x < endX; x++) {
                    int height = field.height(x, z);
                    if (height == 0) {
                        continue;
                    }
                    float density = neighbourhoodDensity(field, x, z);
                    float shapeScale = 0.45F + 0.75F * smoothstep(density);
                    float px = (x + cellHash(x, z, 0x51L)) * cellWidth;
                    float pz = (z + cellHash(x, z, 0x52L)) * cellWidth;
                    float column = height * cellHeight * SOFT_VERTICAL_SPREAD;
                    float py = cellHash(x, z, 0x53L) * column * (0.4F + 0.6F * density);
                    float sizeVariation = 0.75F + 0.5F * cellHash(x, z, 0x54L);
                    float halfWidth = SOFT_PUFF_WIDTH * 0.5F * shapeScale * sizeVariation * puffScale;
                    float halfHeight = SOFT_PUFF_HEIGHT * 0.5F * (0.6F + 0.4F * shapeScale) * sizeVariation;
                    float heightFactor = Mth.clamp((py + halfHeight * 0.5F) / maxThickness, 0.0F, 1.0F);
                    softCube(builder, px - halfWidth, py - halfHeight, pz - halfWidth,
                            px + halfWidth, py + halfHeight, pz + halfWidth, heightFactor);
                }
            }
            MeshData mesh = builder.build();
            if (mesh == null) {
                return null;
            }
            return AntarchySkyDraw.upload("Antarchy Elythia cloud section", mesh);
    }

    private static void softCube(BufferBuilder builder, float x0, float y0, float z0, float x1, float y1, float z1, float height) {
        float ambient = 0.5F + 0.5F * height;
        softQuad(builder, ambient, 0.0F, 1.0F, 0.0F, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        softQuad(builder, ambient, 0.0F, -1.0F, 0.0F, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        softQuad(builder, ambient, -1.0F, 0.0F, 0.0F, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        softQuad(builder, ambient, 1.0F, 0.0F, 0.0F, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        softQuad(builder, ambient, 0.0F, 0.0F, -1.0F, x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        softQuad(builder, ambient, 0.0F, 0.0F, 1.0F, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
    }

    private static void softQuad(BufferBuilder builder, float ambient, float normalX, float normalY, float normalZ,
                                 float ax, float ay, float az, float bx, float by, float bz,
                                 float cx, float cy, float cz, float dx, float dy, float dz) {
        builder.addVertex(ax, ay, az).setUv(0.5F, 0.5F).setColor(ambient, 1.0F, 1.0F, 1.0F).setNormal(normalX, normalY, normalZ);
        builder.addVertex(bx, by, bz).setUv(0.5F, 0.5F).setColor(ambient, 1.0F, 1.0F, 1.0F).setNormal(normalX, normalY, normalZ);
        builder.addVertex(cx, cy, cz).setUv(0.5F, 0.5F).setColor(ambient, 1.0F, 1.0F, 1.0F).setNormal(normalX, normalY, normalZ);
        builder.addVertex(dx, dy, dz).setUv(0.5F, 0.5F).setColor(ambient, 1.0F, 1.0F, 1.0F).setNormal(normalX, normalY, normalZ);
    }

    private static float neighbourhoodDensity(ElythiaCloudField field, int x, int z) {
        int filled = 0;
        int total = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz > 5) {
                    continue;
                }
                total++;
                if (field.height(x + dx, z + dz) > 0) {
                    filled++;
                }
            }
        }
        return filled / (float) total;
    }

    private static float smoothstep(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float cellHash(int x, int z, long salt) {
        long h = x * 0x9E3779B97F4A7C15L ^ z * 0xC2B2AE3D27D4EB4FL ^ salt * 0x165667B19E3779F9L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) * 0x1.0p-24F;
    }

    private static void quad(
            BufferBuilder builder,
            float shade,
            float alpha,
            float normalX,
            float normalY,
            float normalZ,
            float ax, float ay, float az,
            float bx, float by, float bz,
            float cx, float cy, float cz,
            float dx, float dy, float dz
    ) {
        vertex(builder, ax, ay, az, shade, alpha, normalX, normalY, normalZ);
        vertex(builder, bx, by, bz, shade, alpha, normalX, normalY, normalZ);
        vertex(builder, cx, cy, cz, shade, alpha, normalX, normalY, normalZ);
        vertex(builder, dx, dy, dz, shade, alpha, normalX, normalY, normalZ);
    }

    private static void vertex(BufferBuilder builder, float x, float y, float z, float shade, float alpha,
                               float normalX, float normalY, float normalZ) {
        builder.addVertex(x, y, z)
                .setUv(0.5F, 0.5F)
                .setColor(shade, shade, shade, alpha)
                .setNormal(normalX, normalY, normalZ);
    }

    private static final class Layer {
        private final ElythiaCloudField.Spec spec;
        private final float baseY;
        private final double speed;
        private final double windOffset;
        private final float alpha;
        private final boolean softCapable;
        @Nullable
        private CompletableFuture<ElythiaCloudField> pending;
        @Nullable
        private ElythiaCloudField field;
        @Nullable
        private List<Section> sections;
        private boolean built;
        private boolean builtSoft;

        private Layer(ElythiaCloudField.Spec spec, float baseY, double speed, double windOffsetDegrees, float alpha, boolean softCapable) {
            this.spec = spec;
            this.baseY = baseY;
            this.speed = speed;
            this.windOffset = Math.toRadians(windOffsetDegrees);
            this.alpha = alpha;
            this.softCapable = softCapable;
        }

        private double span() {
            return (double) this.spec.size() * this.spec.cellWidth();
        }

        private float thickness() {
            return this.spec.kind() == ElythiaCloudField.Kind.WISP ? 0.0F : this.spec.maxHeight() * this.spec.cellHeight();
        }

        private float visibility(float clearing) {
            return 1.0F - CLEARING_DIM * clearing;
        }

        private boolean soft() {
            return this.built && this.builtSoft;
        }

        @Nullable
        private List<Section> sections() {
            boolean wantSoft = this.softCapable && AntarchySettings.elythiaSoftCloudsEnabled() && ElythiaSoftCloudPass.supported();
            if (this.built && this.builtSoft == wantSoft) {
                return this.sections;
            }
            if (this.field == null) {
                if (this.pending == null) {
                    ElythiaCloudField.Spec spec = this.spec;
                    this.pending = CompletableFuture.supplyAsync(() -> ElythiaCloudField.generate(spec));
                }
                if (!this.pending.isDone()) {
                    return null;
                }
                try {
                    this.field = this.pending.join();
                } catch (RuntimeException exception) {
                    Antarchy.LOGGER.error("Failed to generate Elythia cloud layer {}", this.spec.name(), exception);
                    this.built = true;
                    this.builtSoft = wantSoft;
                    this.pending = null;
                    return null;
                }
                this.pending = null;
            }
            closeSections(this.sections);
            this.sections = null;
            this.built = true;
            this.builtSoft = wantSoft;
            try {
                this.sections = buildSections(this.field, this.alpha, wantSoft);
            } catch (RuntimeException exception) {
                Antarchy.LOGGER.error("Failed to build Elythia cloud layer {}", this.spec.name(), exception);
                this.sections = null;
            }
            return this.sections;
        }
    }
}
