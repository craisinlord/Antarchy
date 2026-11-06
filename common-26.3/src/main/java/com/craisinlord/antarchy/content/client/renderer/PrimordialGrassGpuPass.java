package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.PrimordialGrassWind;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
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
    private static final int TIMER_SLOTS = 4;
    private static final double TIMER_SMOOTHING = 0.1D;
    private static final VertexFormat MESH_FORMAT = VertexFormat.builder(0)
            .addAttribute("Offset", GpuFormat.RG32_FLOAT)
            .addAttribute("Shape", GpuFormat.RG32_FLOAT)
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)
            .addAttribute("Flippable", GpuFormat.R32_FLOAT)
            .build();
    private static final VertexFormat INSTANCE_FORMAT = VertexFormat.builder(1)
            .addAttribute("Root", GpuFormat.RGB32_FLOAT)
            .addAttribute("Meta", GpuFormat.RG8_UNORM)
            .addAttribute("UV2", GpuFormat.RG8_UINT)
            .build();
    private static final RenderPipeline PIPELINE = createPipeline();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector4f WHITE = new Vector4f(1.0F);
    private static final Vector3f SECTION_OFFSET = new Vector3f();
    private static final boolean[] TIMER_PENDING = new boolean[TIMER_SLOTS];

    private static boolean failed;
    private static GpuBuffer meshBuffer;
    private static GpuBuffer frameBuffer;
    private static ByteBuffer frameBytes;
    private static GpuQueryPool timerPool;
    private static int timerSlot;
    private static boolean timerActive;
    private static double gpuNanos;
    @Nullable
    private static Method irisInstance;
    @Nullable
    private static Method irisShaderPackInUse;
    private static boolean irisChecked;

    private PrimordialGrassGpuPass() {
    }

    private static RenderPipeline createPipeline() {
        RenderPipeline cutout = RenderPipelines.CUTOUT_BLOCK;
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/primordial_grass"))
                .withVertexShader(Identifier.fromNamespaceAndPath("antarchy", "core/primordial_grass"))
                .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/primordial_grass"))
                .withVertexBinding(0, MESH_FORMAT)
                .withVertexBinding(1, INSTANCE_FORMAT)
                .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_STRIP)
                .withBindGroupLayout(BindGroupLayouts.PROJECTION)
                .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
                .withBindGroupLayout(BindGroupLayouts.FOG)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
                .withBindGroupLayout(BindGroupLayout.builder().withUniform("GrassFrame", UniformType.UNIFORM_BUFFER).build())
                .withCull(false)
                .withDepthStencilState(Optional.ofNullable(cutout.getDepthStencilState()));
        for (int index = 0; index < cutout.getColorTargetStates().size(); index++) {
            builder.withColorTargetState(index, cutout.getColorTargetStates().get(index));
        }
        return builder.build();
    }

    static boolean available() {
        if (failed) return false;
        try {
            if (RenderSystem.getCompiledPipelineNullable(PIPELINE) != null) return true;
            fail(new IllegalStateException("Pipeline did not compile"));
            return false;
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
        ByteBuffer data = BufferUtils.createByteBuffer(PrimordialGrassLod.shapeVertices() * MESH_STRIDE);
        for (int bands : PrimordialGrassLod.shapeBands()) {
            for (int faces = 1; faces <= PrimordialGrassLod.MAX_FACES; faces++) {
                writeShape(data, sprite, bands, faces);
            }
        }
        data.flip();
        if (meshBuffer != null) meshBuffer.close();
        meshBuffer = RenderSystem.getDevice().createBuffer(() -> "Antarchy primordial grass mesh", GpuBuffer.USAGE_VERTEX, data);
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
        data.putFloat(x).putFloat(z);
        data.putFloat(h).putFloat((float) PrimordialGrassWind.flexibility(h));
        data.putFloat(u == 0.0F ? sprite.getU0() : sprite.getU1()).putFloat(sprite.getV(1.0F - h));
        data.putFloat(flippable);
    }

    static GpuBuffer uploadInstances(@Nullable GpuBuffer current, ByteBuffer data) {
        if (current != null && current.size() == data.remaining()) {
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(current.slice(), data);
            return current;
        }
        if (current != null) current.close();
        return RenderSystem.getDevice().createBuffer(
                () -> "Antarchy primordial grass instances",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                data);
    }

    static GpuBufferSlice writeSectionTransform(float offsetX, float offsetY, float offsetZ) {
        return RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewStack(), WHITE, SECTION_OFFSET.set(offsetX, offsetY, offsetZ), IDENTITY);
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

    static void begin(RenderPass pass, boolean timed) {
        if (timed) beginTimer(pass);
        Minecraft minecraft = Minecraft.getInstance();
        AbstractTexture atlas = minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        pass.setPipeline(RenderSystem.getCompiledPipeline(PIPELINE));
        pass.setUniform("Sampler0", atlas.getTextureView(), atlas.getSampler());
        pass.setUniform("Sampler2", minecraft.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        pass.setUniform("GrassFrame", frameBuffer.slice());
        pass.setVertexBuffer(0, meshBuffer.slice());
    }

    static void draw(RenderPass pass, GpuBuffer instances, GpuBufferSlice transform, int first, int vertices, int instanceCount) {
        pass.setUniform("DynamicTransforms", transform);
        pass.setVertexBuffer(1, instances.slice());
        pass.draw(vertices, instanceCount, first, 0);
    }

    static void end(RenderPass pass) {
        endTimer(pass);
    }

    static double gpuMillis() {
        return gpuNanos / 1_000_000.0D;
    }

    private static void beginTimer(RenderPass pass) {
        if (timerPool == null) timerPool = RenderSystem.getDevice().createTimestampQueryPool(TIMER_SLOTS * 2);
        int slot = timerSlot;
        if (TIMER_PENDING[slot]) {
            OptionalLong[] stamps = timerPool.getValues(slot * 2, 2);
            if (stamps[0].isEmpty() || stamps[1].isEmpty()) return;
            double elapsed = (stamps[1].getAsLong() - stamps[0].getAsLong()) * (double) RenderSystem.getDevice().getDeviceInfo().timestampPeriod();
            gpuNanos += (elapsed - gpuNanos) * TIMER_SMOOTHING;
            TIMER_PENDING[slot] = false;
        }
        pass.writeTimestamp(timerPool, slot * 2);
        timerActive = true;
    }

    private static void endTimer(RenderPass pass) {
        if (!timerActive) return;
        timerActive = false;
        pass.writeTimestamp(timerPool, timerSlot * 2 + 1);
        TIMER_PENDING[timerSlot] = true;
        timerSlot = (timerSlot + 1) % TIMER_SLOTS;
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
