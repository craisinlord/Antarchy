package com.craisinlord.antarchy.content.client.renderer.clouds;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.renderer.AntarchySkyDraw;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

final class ElythiaSoftCloudPass {
    static final int MAX_CLEARINGS = 8;
    static final float NO_GROWTH_LIMIT = 1.0E9F;
    private static final float RESOLUTION_SCALE = 0.5F;
    private static final float COVERAGE_STEP = 1.0F / 24.0F;
    private static final float SURFACE_OPACITY = 0.22F;
    private static final float MAX_ALPHA = 0.93F;
    private static final float HAZE_STRENGTH = 0.55F;
    private static final int FRAME_BYTES = (MAX_CLEARINGS + 2) * 16;
    private static final int COMPOSITE_BYTES = 4 * 16;
    private static final RenderPipeline COVERAGE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/elythia_soft_cloud_coverage"))
            .withVertexShader(Identifier.fromNamespaceAndPath("antarchy", "core/elythia_soft_cloud_coverage"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/elythia_soft_cloud_coverage"))
            .withSampler("InSampler")
            .withUniform("ElythiaSoftCloud", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(Optional.empty())
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL, VertexFormat.Mode.QUADS)
            .build();
    private static final RenderPipeline COMPOSITE_PIPELINE = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/elythia_soft_cloud_composite"))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/elythia_soft_cloud_composite"))
            .withSampler("InSampler")
            .withSampler("Sampler0")
            .withUniform("ElythiaSoftComposite", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(Optional.empty())
            .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
            .build();

    record Draw(AntarchySkyDraw.StaticMesh buffer, float offsetX, float offsetY, float offsetZ, float fogEnd, float opacity,
                float growthTop, boolean clearable) {
    }

    record Frame(Vec3 cloudColor, float[] fogColor, float sunEast, float sunStrength,
                 float[] clearings, int clearingCount, float clearingStrength) {
    }

    private static boolean failed;
    @Nullable
    private static RenderTarget coverageTarget;
    @Nullable
    private static GpuBuffer frameBuffer;
    @Nullable
    private static GpuBuffer compositeBuffer;
    private static final ByteBuffer FRAME_DATA = BufferUtils.createByteBuffer(FRAME_BYTES);
    private static final ByteBuffer COMPOSITE_DATA = BufferUtils.createByteBuffer(COMPOSITE_BYTES);
    @Nullable
    private static Method irisInstance;
    @Nullable
    private static Method irisShaderPackInUse;
    private static boolean irisChecked;

    private ElythiaSoftCloudPass() {
    }

    static boolean supported() {
        return !failed && !irisShaderPackActive();
    }

    static void render(List<Draw> draws, Matrix4fc viewMatrix, Frame frame, @Nullable RenderTarget compositeTarget) {
        if (draws.isEmpty() || failed) {
            return;
        }
        try {
            renderPasses(draws, viewMatrix, frame, compositeTarget);
        } catch (RuntimeException exception) {
            Antarchy.LOGGER.error("Failed to render Elythia soft clouds, falling back to solid clouds", exception);
            failed = true;
        }
    }

    private static void renderPasses(List<Draw> draws, Matrix4fc viewMatrix, Frame frame, @Nullable RenderTarget compositeTarget) {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        int width = Math.max(1, Math.round(main.width * RESOLUTION_SCALE));
        int height = Math.max(1, Math.round(main.height * RESOLUTION_SCALE));
        if (!ensureTarget(width, height)) {
            return;
        }
        RenderTarget coverage = coverageTarget;
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        int clearingCount = Math.min(MAX_CLEARINGS, frame.clearingCount());
        ByteBuffer data = FRAME_DATA.clear();
        for (int index = 0; index < MAX_CLEARINGS * 4; index++) {
            data.putFloat(index < clearingCount * 4 ? frame.clearings()[index] : 0.0F);
        }
        data.putFloat(clearingCount).putFloat(frame.clearingStrength()).putFloat(frame.sunEast()).putFloat(frame.sunStrength());
        data.putFloat(main.width / (float) width).putFloat(main.height / (float) height).putFloat(0.0F).putFloat(0.0F);
        data.flip();
        if (frameBuffer == null) {
            frameBuffer = RenderSystem.getDevice().createBuffer(() -> "Antarchy Elythia soft cloud frame", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, FRAME_BYTES);
        }
        encoder.writeToBuffer(frameBuffer.slice(), data);

        float r = (float) frame.cloudColor().x;
        float g = (float) frame.cloudColor().y;
        float b = (float) frame.cloudColor().z;
        float[] fog = frame.fogColor();
        ByteBuffer composite = COMPOSITE_DATA.clear();
        composite.putFloat(1.0F / width).putFloat(1.0F / height).putFloat(SURFACE_OPACITY).putFloat(MAX_ALPHA);
        composite.putFloat(r * 0.94F).putFloat(g * 0.94F).putFloat(b * 0.95F).putFloat(1.0F / COVERAGE_STEP);
        composite.putFloat(r * 0.6F * 0.7F + fog[0] * 0.3F).putFloat(g * 0.58F * 0.7F + fog[1] * 0.3F).putFloat(b * 0.72F * 0.7F + fog[2] * 0.3F).putFloat(HAZE_STRENGTH);
        composite.putFloat(fog[0]).putFloat(fog[1]).putFloat(fog[2]).putFloat(1.0F);
        composite.flip();
        if (compositeBuffer == null) {
            compositeBuffer = RenderSystem.getDevice().createBuffer(() -> "Antarchy Elythia soft cloud composite", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, COMPOSITE_BYTES);
        }
        encoder.writeToBuffer(compositeBuffer.slice(), composite);

        List<GpuBufferSlice> transforms = new ArrayList<>(draws.size());
        int maxIndices = 0;
        for (Draw draw : draws) {
            float step = COVERAGE_STEP * 2.0F * draw.opacity();
            transforms.add(RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(viewMatrix),
                    new Vector4f(draw.fogEnd(), step, draw.growthTop(), draw.clearable() ? 1.0F : 0.0F),
                    new Vector3f(draw.offsetX(), draw.offsetY(), draw.offsetZ()), new Matrix4f()));
            maxIndices = Math.max(maxIndices, draw.buffer().indexCount());
        }
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(maxIndices);

        try (RenderPass pass = encoder.createRenderPass(() -> "Antarchy Elythia soft cloud coverage", coverage.getColorTextureView(), OptionalInt.of(0))) {
            pass.setPipeline(COVERAGE_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindTexture("InSampler", main.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.setUniform("ElythiaSoftCloud", frameBuffer);
            pass.setIndexBuffer(indexBuffer, indices.type());
            for (int index = 0; index < draws.size(); index++) {
                Draw draw = draws.get(index);
                if (draw.buffer().indexCount() <= 0) {
                    continue;
                }
                pass.setUniform("DynamicTransforms", transforms.get(index));
                pass.setVertexBuffer(0, draw.buffer().vertices());
                pass.drawIndexed(0, 0, draw.buffer().indexCount(), 1);
            }
        }

        RenderTarget output = compositeTarget != null ? compositeTarget : main;
        try (RenderPass pass = encoder.createRenderPass(() -> "Antarchy Elythia soft cloud composite", output.getColorTextureView(), OptionalInt.empty())) {
            pass.setPipeline(COMPOSITE_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.bindTexture("InSampler", coverage.getColorTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
            pass.bindTexture("Sampler0", main.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
            pass.setUniform("ElythiaSoftComposite", compositeBuffer);
            pass.draw(0, 3);
        }
    }

    private static boolean ensureTarget(int width, int height) {
        if (coverageTarget != null && coverageTarget.width == width && coverageTarget.height == height) {
            return true;
        }
        if (coverageTarget != null) {
            coverageTarget.destroyBuffers();
        }
        TextureTarget target = new TextureTarget("Antarchy Elythia soft cloud coverage", width, height, false);
        if (!(target.getColorTexture() instanceof GlTexture texture)) {
            target.destroyBuffers();
            Antarchy.LOGGER.error("Elythia soft clouds need a float color target, falling back to solid clouds");
            failed = true;
            return false;
        }
        GlStateManager._bindTexture(texture.glId());
        GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, null);
        GlStateManager._bindTexture(0);
        coverageTarget = target;
        return true;
    }

    private static boolean irisShaderPackActive() {
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
