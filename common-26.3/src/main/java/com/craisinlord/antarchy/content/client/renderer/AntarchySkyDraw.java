package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jetbrains.annotations.Nullable;

final class AntarchySkyDraw {
    static final Vector4fc WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
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

    record StaticMesh(GpuBuffer vertices, int indexCount, PrimitiveTopology topology) {
    }

    static BufferBuilder begin(PrimitiveTopology topology, VertexFormat format) {
        if (byteBuffer == null) {
            byteBuffer = new ByteBufferBuilder(INITIAL_BUFFER_SIZE);
        }
        return new BufferBuilder(byteBuffer, topology, format);
    }

    static StaticMesh upload(String label, MeshData mesh) {
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

    static Matrix4f modelView() {
        return RenderSystem.getModelViewMatrixCopy();
    }

    static void draw(String label, RenderPipeline pipeline, MeshData mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture) {
        try (mesh) {
            CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
            GpuBufferSlice vertices = encoder.transientMemory().uploadGpu(mesh.vertexBuffer(), VERTEX_ALIGNMENT, GpuBuffer.USAGE_VERTEX);
            draw(label, pipeline, vertices, mesh.drawState().indexCount(), mesh.drawState().primitiveTopology(), modelView, color, texture);
        }
    }

    static void draw(String label, RenderPipeline pipeline, StaticMesh mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture) {
        draw(label, pipeline, mesh.vertices().slice(), mesh.indexCount(), mesh.topology(), modelView, color, texture);
    }

    private static void draw(
            String label,
            RenderPipeline pipeline,
            GpuBufferSlice vertices,
            int indexCount,
            PrimitiveTopology topology,
            Matrix4fc modelView,
            Vector4fc color,
            @Nullable Identifier texture
    ) {
        if (indexCount <= 0) {
            return;
        }
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(topology);
        GpuBuffer indexBuffer = indices.getBuffer(indexCount);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(new Matrix4f(modelView), new Vector4f(color), new Vector3f(), new Matrix4f());
        AbstractTexture abstractTexture = texture != null ? Minecraft.getInstance().getTextureManager().getTexture(texture) : null;
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
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
