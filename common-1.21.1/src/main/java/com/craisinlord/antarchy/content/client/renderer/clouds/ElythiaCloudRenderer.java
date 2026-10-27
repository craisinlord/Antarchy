package com.craisinlord.antarchy.content.client.renderer.clouds;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class ElythiaCloudRenderer {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final double DRIFT_PER_TICK = 0.03D;
    private static final float MIN_CLOUD_RANGE = 512.0F;
    private static final float FOG_START_FRACTION = 0.65F;

    private static final Layer[] LAYERS = {
            new Layer(ElythiaCloudField.LOW, 200.0F, 1.0D, 0.8F),
            new Layer(ElythiaCloudField.MID, 280.0F, 0.7D, 0.8F),
            new Layer(ElythiaCloudField.HIGH, 470.0F, 1.35D, 0.7F)
    };

    private ElythiaCloudRenderer() {
    }

    public static void render(
            Minecraft minecraft,
            ClientLevel level,
            PoseStack poseStack,
            Matrix4f modelViewMatrix,
            Matrix4f projectionMatrix,
            float partialTick,
            double camX,
            double camY,
            double camZ
    ) {
        CloudStatus status = minecraft.options.getCloudsType();
        if (status == CloudStatus.OFF) {
            return;
        }

        boolean anyReady = false;
        for (Layer layer : LAYERS) {
            anyReady |= layer.buffer() != null;
        }
        if (!anyReady) {
            return;
        }

        Vec3 cloudColor = level.getCloudColor(partialTick);
        double time = level.getGameTime() + partialTick;
        float terrainFogStart = RenderSystem.getShaderFogStart();
        float terrainFogEnd = RenderSystem.getShaderFogEnd();
        float cloudRange = Math.max(terrainFogEnd, MIN_CLOUD_RANGE);

        FogRenderer.levelFogColor();
        poseStack.pushPose();
        poseStack.mulPose(modelViewMatrix);
        RenderSystem.setShaderColor((float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, 1.0F);

        for (int pass = status == CloudStatus.FANCY ? 0 : 1; pass < 2; pass++) {
            RenderType renderType = pass == 0 ? RenderType.cloudsDepthOnly() : RenderType.clouds();
            renderType.setupRenderState();
            RenderSystem.disableCull();
            RenderSystem.setShaderTexture(0, WHITE_TEXTURE);
            ShaderInstance shader = RenderSystem.getShader();
            if (shader != null) {
                for (Layer layer : LAYERS) {
                    VertexBuffer buffer = layer.buffer();
                    if (buffer == null) {
                        continue;
                    }
                    float vertical = (float) Math.abs(layer.baseY + layer.thickness() * 0.5F - camY);
                    float fogEnd = Math.max(terrainFogEnd, vertical + cloudRange);
                    RenderSystem.setShaderFogStart(fogEnd * FOG_START_FRACTION);
                    RenderSystem.setShaderFogEnd(fogEnd);
                    drawLayer(layer, buffer, poseStack, projectionMatrix, shader, time, camX, camY, camZ, fogEnd);
                }
            }
            renderType.clearRenderState();
        }

        RenderSystem.setShaderFogStart(terrainFogStart);
        RenderSystem.setShaderFogEnd(terrainFogEnd);
        VertexBuffer.unbind();
        RenderSystem.enableCull();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static void drawLayer(
            Layer layer,
            VertexBuffer buffer,
            PoseStack poseStack,
            Matrix4f projectionMatrix,
            ShaderInstance shader,
            double time,
            double camX,
            double camY,
            double camZ,
            float range
    ) {
        double span = layer.span();
        double drift = (time * DRIFT_PER_TICK * layer.speed) % span;
        double patternX = camX + drift;
        int tileX = Mth.floor(patternX / span);
        int tileZ = Mth.floor(camZ / span);
        float offsetY = (float) (layer.baseY - camY);
        double maxDistance = range + layer.spec.cellWidth() * 2.0D;

        buffer.bind();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                double originX = (tileX + dx) * span - patternX;
                double originZ = (tileZ + dz) * span - camZ;
                double nearX = Mth.clamp(0.0D, originX, originX + span);
                double nearZ = Mth.clamp(0.0D, originZ, originZ + span);
                if (nearX * nearX + nearZ * nearZ > maxDistance * maxDistance) {
                    continue;
                }

                poseStack.pushPose();
                poseStack.translate((float) originX, offsetY, (float) originZ);
                buffer.drawWithShader(poseStack.last().pose(), projectionMatrix, shader);
                poseStack.popPose();
            }
        }
    }

    @Nullable
    private static VertexBuffer buildBuffer(ElythiaCloudField field, float alpha) {
        int n = field.size();
        boolean wisp = field.spec().kind() == ElythiaCloudField.Kind.WISP;
        float cellWidth = field.spec().cellWidth();
        float cellHeight = field.spec().cellHeight();
        ByteBufferBuilder bytes = new ByteBufferBuilder(1 << 20);
        try {
            BufferBuilder builder = new BufferBuilder(bytes, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

            for (int z = 0; z < n; z++) {
                int x = 0;
                while (x < n) {
                    int height = field.height(x, z);
                    if (height == 0) {
                        x++;
                        continue;
                    }
                    int end = x;
                    while (end + 1 < n && field.height(end + 1, z) == height) {
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
                for (int z = 0; z < n; z++) {
                    for (int x = 0; x < n; x++) {
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
            VertexBuffer buffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            buffer.bind();
            buffer.upload(mesh);
            VertexBuffer.unbind();
            return buffer;
        } finally {
            bytes.close();
        }
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
        private final float alpha;
        @Nullable
        private CompletableFuture<ElythiaCloudField> pending;
        @Nullable
        private VertexBuffer buffer;
        private boolean built;

        private Layer(ElythiaCloudField.Spec spec, float baseY, double speed, float alpha) {
            this.spec = spec;
            this.baseY = baseY;
            this.speed = speed;
            this.alpha = alpha;
        }

        private double span() {
            return (double) this.spec.size() * this.spec.cellWidth();
        }

        private float thickness() {
            return this.spec.kind() == ElythiaCloudField.Kind.WISP ? 0.0F : this.spec.maxHeight() * this.spec.cellHeight();
        }

        @Nullable
        private VertexBuffer buffer() {
            if (this.built) {
                return this.buffer;
            }
            if (this.pending == null) {
                ElythiaCloudField.Spec spec = this.spec;
                this.pending = CompletableFuture.supplyAsync(() -> ElythiaCloudField.generate(spec));
            }
            if (!this.pending.isDone()) {
                return null;
            }
            this.built = true;
            try {
                this.buffer = buildBuffer(this.pending.join(), this.alpha);
            } catch (RuntimeException exception) {
                Antarchy.LOGGER.error("Failed to build Elythia cloud layer {}", this.spec.name(), exception);
                this.buffer = null;
            }
            this.pending = null;
            return this.buffer;
        }
    }
}
