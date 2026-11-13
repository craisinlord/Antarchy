package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public final class AntarchySkyDraw {
    static final Vector4fc WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
    private static final RenderPipeline.Snippet ADDITIVE_TEXTURE_SNIPPET = RenderPipeline.builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
            .withVertexShader(Identifier.fromNamespaceAndPath("antarchy", "core/antarchy_additive_texture"))
            .withFragmentShader(Identifier.fromNamespaceAndPath("antarchy", "core/antarchy_additive_texture"))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withCull(false)
            .buildSnippet();
    private static final RenderPipeline ADDITIVE_TEXTURE_PIPELINE = RenderPipeline.builder(ADDITIVE_TEXTURE_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("antarchy", "pipeline/additive_texture"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .build();
    private static final OitPipelineSet ADDITIVE_TEXTURE_OIT = OitPipelineSet.builder("antarchy_additive_texture",
            RenderPipeline.builder(ADDITIVE_TEXTURE_SNIPPET).withShaderDefine("OIT_ADDITIVE")).build();
    private static final Map<Identifier, RenderType> ADDITIVE_TEXTURE_TYPES = new HashMap<>();
    private static final int INITIAL_BUFFER_SIZE = 786432;
    private static final long VERTEX_ALIGNMENT = 16L;

    @Nullable
    private static ByteBufferBuilder byteBuffer;
    @Nullable
    private static StaticMesh topSkyDisc;
    @Nullable
    private static StaticMesh bottomSkyDisc;

    private AntarchySkyDraw() {
    }

    public record StaticMesh(GpuBuffer vertices, int indexCount, PrimitiveTopology topology) {
    }

    public static BufferBuilder begin(PrimitiveTopology topology, VertexFormat format) {
        if (byteBuffer == null) {
            byteBuffer = new ByteBufferBuilder(INITIAL_BUFFER_SIZE);
        }
        return new BufferBuilder(byteBuffer, topology, format);
    }

    public static RenderType additiveTexture(Identifier texture) {
        return ADDITIVE_TEXTURE_TYPES.computeIfAbsent(texture, id -> RenderType.create(
                "antarchy_additive_texture",
                RenderSetup.builder(ADDITIVE_TEXTURE_PIPELINE).setOitPipelines(ADDITIVE_TEXTURE_OIT).withTexture("Sampler0", id).createRenderSetup()));
    }

    public static StaticMesh upload(String label, MeshData mesh) {
        try (mesh) {
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            return new StaticMesh(buffer, mesh.drawState().indexCount(), mesh.drawState().primitiveTopology());
        }
    }

    static StaticMesh topSkyDisc() {
        if (topSkyDisc == null) {
            topSkyDisc = buildSkyDisc("Antarchy top sky disc", 16.0F);
        }
        return topSkyDisc;
    }

    static StaticMesh bottomSkyDisc() {
        if (bottomSkyDisc == null) {
            bottomSkyDisc = buildSkyDisc("Antarchy bottom sky disc", -16.0F);
        }
        return bottomSkyDisc;
    }

    private static StaticMesh buildSkyDisc(String label, float yy) {
        BufferBuilder builder = begin(PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        float x = Math.signum(yy) * 512.0F;
        builder.addVertex(0.0F, yy, 0.0F);
        for (int i = -180; i <= 180; i += 45) {
            builder.addVertex(x * Mth.cos(i * Mth.DEG_TO_RAD), yy, 512.0F * Mth.sin(i * Mth.DEG_TO_RAD));
        }
        return upload(label, builder.buildOrThrow());
    }

    public static Matrix4f modelView() {
        return RenderSystem.getModelViewMatrixCopy();
    }

    public static void draw(String label, RenderPipeline pipeline, MeshData mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture) {
        try (mesh) {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBufferSlice vertices = encoder.transientMemory().uploadGpu(mesh.vertexBuffer(), VERTEX_ALIGNMENT, GpuBuffer.USAGE_VERTEX);
            draw(label, pipeline, vertices, mesh.drawState().indexCount(), mesh.drawState().primitiveTopology(), modelView, color, texture, null);
        }
    }

    public static void draw(String label, RenderPipeline pipeline, StaticMesh mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture) {
        draw(label, pipeline, mesh.vertices().slice(), mesh.indexCount(), mesh.topology(), modelView, color, texture, null);
    }

    public static void draw(String label, RenderPipeline pipeline, StaticMesh mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture, @Nullable RenderTarget target) {
        draw(label, pipeline, mesh.vertices().slice(), mesh.indexCount(), mesh.topology(), modelView, color, texture, target);
    }

    public static void draw(String label, RenderPipeline pipeline, StaticMesh mesh, Matrix4fc modelView, Vector4fc color,
                            @Nullable Identifier texture, RenderPass renderPass) {
        if (mesh.indexCount() <= 0) {
            return;
        }
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(mesh.topology());
        GpuBuffer indexBuffer = indices.getBuffer(mesh.indexCount());
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f(modelView), new Vector4f(color), new Vector3f(), new Matrix4f());
        renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
        RenderSystem.bindDefaultUniforms(renderPass);
        renderPass.setUniform("DynamicTransforms", transforms);
        if (texture != null) {
            AbstractTexture abstractTexture = Minecraft.getInstance().getTextureManager().getTexture(texture);
            renderPass.setUniform("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
        }
        renderPass.setVertexBuffer(0, mesh.vertices().slice());
        renderPass.setIndexBuffer(indexBuffer, indices.type());
        renderPass.drawIndexed(mesh.indexCount(), 1, 0, 0, 0);
    }

    public static void draw(String label, RenderPipeline pipeline, StaticMesh mesh, Matrix4fc modelView, Vector4fc color, Vector3f modelOffset, @Nullable RenderTarget target) {
        draw(label, pipeline, mesh.vertices().slice(), mesh.indexCount(), mesh.topology(), modelView, color, modelOffset, null, target);
    }

    private static void draw(
            String label,
            RenderPipeline pipeline,
            GpuBufferSlice vertices,
            int indexCount,
            PrimitiveTopology topology,
            Matrix4fc modelView,
            Vector4fc color,
            @Nullable Identifier texture,
            @Nullable RenderTarget target
    ) {
        draw(label, pipeline, vertices, indexCount, topology, modelView, color, new Vector3f(), texture, target);
    }

    private static void draw(
            String label,
            RenderPipeline pipeline,
            GpuBufferSlice vertices,
            int indexCount,
            PrimitiveTopology topology,
            Matrix4fc modelView,
            Vector4fc color,
            Vector3f modelOffset,
            @Nullable Identifier texture,
            @Nullable RenderTarget target
    ) {
        if (indexCount <= 0) {
            return;
        }
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(topology);
        GpuBuffer indexBuffer = indices.getBuffer(indexCount);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f(modelView), new Vector4f(color), modelOffset, new Matrix4f());
        AbstractTexture abstractTexture = texture != null ? Minecraft.getInstance().getTextureManager().getTexture(texture) : null;
        if (target == null) {
            target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        }
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> label, target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", transforms);
            if (abstractTexture != null) {
                renderPass.setUniform("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
            }
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indexBuffer, indices.type());
            renderPass.drawIndexed(indexCount, 1, 0, 0, 0);
        }
    }
}
