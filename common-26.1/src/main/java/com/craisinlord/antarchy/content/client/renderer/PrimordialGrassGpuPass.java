package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.PrimordialGrassWind;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

final class PrimordialGrassGpuPass {
    static final int INSTANCE_BYTES = 16;

    private static final int MAX_TRAMPLES = 32;
    private static final int GUST_ENTRIES = 8;
    private static final int FRAME_BYTES = (1 + GUST_ENTRIES + MAX_TRAMPLES + MAX_TRAMPLES + 1) * 16;
    private static final int MESH_STRIDE = 28;
    private static final float HALF_WIDTH = 0.45F;
    private static final double TIMER_SMOOTHING = 0.1D;
    private static final VertexFormat MESH_FORMAT = VertexFormat.builder()
            .add("MeshShape", VertexFormatElement.POSITION)
            .add("UV0", VertexFormatElement.UV0)
            .add("Flex", VertexFormatElement.LINE_WIDTH)
            .add("Flags", VertexFormatElement.COLOR)
            .build();
    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/primordial_grass"))
            .withVertexShader(Identifier.fromNamespaceAndPath("antarchy", "core/primordial_grass"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/primordial_grass"))
            .withVertexFormat(MESH_FORMAT, VertexFormat.Mode.TRIANGLE_STRIP)
            .withSampler("Sampler0")
            .withSampler("Sampler2")
            .withUniform("GrassFrame", UniformType.UNIFORM_BUFFER)
            .withUniform("GrassInstances", UniformType.TEXEL_BUFFER, TextureFormat.RGBA8)
            .withCull(false)
            .withDepthStencilState(RenderPipelines.CUTOUT_BLOCK.getDepthStencilState())
            .withColorTargetState(RenderPipelines.CUTOUT_BLOCK.getColorTargetState())
            .build();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector4f WHITE = new Vector4f(1.0F);
    private static final Vector3f SECTION_OFFSET = new Vector3f();

    private static boolean failed;
    private static GpuBuffer meshBuffer;
    private static GpuBuffer indexBuffer;
    private static GpuBuffer frameBuffer;
    private static ByteBuffer frameBytes;
    private static GpuQuery activeQuery;
    private static GpuQuery pendingQuery;
    private static double gpuNanos;
    @Nullable
    private static Method irisInstance;
    @Nullable
    private static Method irisShaderPackInUse;
    private static boolean irisChecked;

    private PrimordialGrassGpuPass() {
    }

    static boolean available() {
        if (failed) {
            return false;
        }
        try {
            return RenderSystem.getDevice().precompilePipeline(PIPELINE).isValid();
        } catch (RuntimeException | LinkageError exception) {
            fail(exception);
            return false;
        }
    }

    static void fail(Throwable exception) {
        Antarchy.LOGGER.error("Failed to run Primordial grass GPU pass, using static grass", exception);
        failed = true;
    }

    static void uploadMesh(TextureAtlasSprite sprite) {
        int vertices = PrimordialGrassLod.shapeVertices();
        ByteBuffer data = BufferUtils.createByteBuffer(vertices * MESH_STRIDE);
        for (int bands : PrimordialGrassLod.shapeBands()) {
            for (int faces = 1; faces <= PrimordialGrassLod.MAX_FACES; faces++) {
                writeShape(data, sprite, bands, faces);
            }
        }
        data.flip();
        if (meshBuffer != null) meshBuffer.close();
        meshBuffer = RenderSystem.getDevice().createBuffer(() -> "Antarchy primordial grass mesh", GpuBuffer.USAGE_VERTEX, data);
        if (indexBuffer == null) {
            ByteBuffer indices = BufferUtils.createByteBuffer(vertices * Short.BYTES);
            for (int i = 0; i < vertices; i++) indices.putShort((short) i);
            indices.flip();
            indexBuffer = RenderSystem.getDevice().createBuffer(() -> "Antarchy primordial grass mesh indices", GpuBuffer.USAGE_INDEX, indices);
        }
    }

    private static void writeShape(ByteBuffer data, TextureAtlasSprite sprite, int bands, int faces) {
        float flippable = faces == 1 ? 1.0F : 0.0F;
        for (int face = 0; face < faces; face++) {
            float x = face == 0 ? HALF_WIDTH : -HALF_WIDTH;
            if (face > 0) writeMeshVertex(data, sprite, -x, -HALF_WIDTH, 0.0F, 0.0F, flippable);
            for (int level = 0; level <= bands; level++) {
                float h = level / (float) bands;
                writeMeshVertex(data, sprite, -x, -HALF_WIDTH, h, 0.0F, flippable);
                writeMeshVertex(data, sprite, x, HALF_WIDTH, h, 1.0F, flippable);
            }
            if (face < faces - 1) writeMeshVertex(data, sprite, x, HALF_WIDTH, 1.0F, 1.0F, flippable);
        }
    }

    private static void writeMeshVertex(ByteBuffer data, TextureAtlasSprite sprite, float x, float z, float h, float u, float flippable) {
        data.putFloat(x).putFloat(z).putFloat(h);
        data.putFloat(u == 0.0F ? sprite.getU0() : sprite.getU1()).putFloat(sprite.getV(1.0F - h));
        data.putFloat((float) PrimordialGrassWind.flexibility(h));
        data.put((byte) (flippable > 0.5F ? 255 : 0)).put((byte) 0).put((byte) 0).put((byte) 0);
    }

    static GpuBuffer uploadInstances(@Nullable GpuBuffer current, ByteBuffer data) {
        if (current != null && current.size() == data.remaining()) {
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(current.slice(), data);
            return current;
        }
        if (current != null) current.close();
        return RenderSystem.getDevice().createBuffer(
                () -> "Antarchy primordial grass instances",
                GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER | GpuBuffer.USAGE_COPY_DST,
                data);
    }

    static GpuBufferSlice writeSectionTransform(float offsetX, float offsetY, float offsetZ) {
        return RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), WHITE, SECTION_OFFSET.set(offsetX, offsetY, offsetZ), IDENTITY);
    }

    static void writeFrame(PrimordialGrassWind.ShaderFrame frame, float[] gustTable, boolean windEnabled,
                           float[] tramplePositions, float[] trampleMotions, int trampleCount) {
        if (frameBytes == null) frameBytes = BufferUtils.createByteBuffer(FRAME_BYTES);
        int count = Math.max(0, Math.min(MAX_TRAMPLES, trampleCount));
        ByteBuffer data = frameBytes;
        data.clear();
        data.putFloat(frame.directionX()).putFloat(frame.directionZ()).putFloat(frame.gustLocal()).putFloat(frame.calmPhase());
        for (int i = 0; i < GUST_ENTRIES; i++) {
            data.putFloat(gustTable[i * 2]).putFloat(gustTable[i * 2 + 1]).putFloat(0.0F).putFloat(0.0F);
        }
        for (int i = 0; i < MAX_TRAMPLES; i++) {
            if (i < count) data.putFloat(tramplePositions[i * 4]).putFloat(tramplePositions[i * 4 + 1]).putFloat(tramplePositions[i * 4 + 2]).putFloat(tramplePositions[i * 4 + 3]);
            else data.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
        }
        for (int i = 0; i < MAX_TRAMPLES; i++) {
            if (i < count) data.putFloat(trampleMotions[i * 3]).putFloat(trampleMotions[i * 3 + 1]).putFloat(trampleMotions[i * 3 + 2]).putFloat(0.0F);
            else data.putFloat(0.0F).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
        }
        data.putFloat(windEnabled ? 1.0F : 0.0F).putFloat(count).putFloat(0.0F).putFloat(0.0F);
        data.flip();
        if (frameBuffer == null) {
            frameBuffer = RenderSystem.getDevice().createBuffer(() -> "Antarchy primordial grass frame", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, FRAME_BYTES);
        }
        RenderSystem.getDevice().createCommandEncoder().writeToBuffer(frameBuffer.slice(), data);
    }

    static RenderPass begin(boolean timed) {
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        if (timed) beginTimer(encoder);
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        AbstractTexture atlas = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        RenderPass pass = encoder.createRenderPass(
                () -> "Antarchy primordial grass",
                target.getColorTextureView(),
                OptionalInt.empty(),
                target.getDepthTextureView(),
                OptionalDouble.empty());
        pass.setPipeline(PIPELINE);
        RenderSystem.bindDefaultUniforms(pass);
        pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
        pass.bindTexture("Sampler2", minecraft.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        pass.setUniform("GrassFrame", frameBuffer);
        pass.setVertexBuffer(0, meshBuffer);
        pass.setIndexBuffer(indexBuffer, VertexFormat.IndexType.SHORT);
        return pass;
    }

    static void draw(RenderPass pass, GpuBuffer instances, GpuBufferSlice transform, int first, int vertices, int instanceCount) {
        pass.setUniform("DynamicTransforms", transform);
        pass.setUniform("GrassInstances", instances);
        pass.drawIndexed(0, first, vertices, instanceCount);
    }

    static void end() {
        endTimer();
    }

    static double gpuMillis() {
        return gpuNanos / 1_000_000.0D;
    }

    private static void beginTimer(CommandEncoder encoder) {
        if (pendingQuery != null) {
            OptionalLong elapsed = pendingQuery.getValue();
            if (elapsed.isEmpty()) {
                return;
            }
            gpuNanos += (elapsed.getAsLong() - gpuNanos) * TIMER_SMOOTHING;
            pendingQuery.close();
            pendingQuery = null;
        }
        try {
            activeQuery = encoder.timerQueryBegin();
        } catch (IllegalStateException ignored) {
            activeQuery = null;
        }
    }

    private static void endTimer() {
        if (activeQuery == null) {
            return;
        }
        RenderSystem.getDevice().createCommandEncoder().timerQueryEnd(activeQuery);
        pendingQuery = activeQuery;
        activeQuery = null;
    }

    static boolean shaderPackActive() {
        if (!irisChecked) {
            irisChecked = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                irisInstance = api.getMethod("getInstance");
                irisShaderPackInUse = api.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                irisInstance = null;
                irisShaderPackInUse = null;
            }
        }
        if (irisInstance == null || irisShaderPackInUse == null) {
            return false;
        }
        try {
            Object instance = irisInstance.invoke(null);
            return Boolean.TRUE.equals(irisShaderPackInUse.invoke(instance));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }
}
