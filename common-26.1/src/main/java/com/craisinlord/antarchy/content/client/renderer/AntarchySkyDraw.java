package com.craisinlord.antarchy.content.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
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

    @Nullable
    private static StaticMesh topSkyDisc;
    @Nullable
    private static StaticMesh bottomSkyDisc;

    private AntarchySkyDraw() {
    }

    record StaticMesh(GpuBuffer vertices, int indexCount, VertexFormat.Mode mode) {
    }

    static StaticMesh upload(String label, MeshData mesh) {
        try (mesh) {
            GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> label, GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
            return new StaticMesh(buffer, mesh.drawState().indexCount(), mesh.drawState().mode());
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
        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
        float x = Math.signum(yy) * 512.0F;
        builder.addVertex(0.0F, yy, 0.0F);
        for (int i = -180; i <= 180; i += 45) {
            builder.addVertex(x * Mth.cos(i * Mth.DEG_TO_RAD), yy, 512.0F * Mth.sin(i * Mth.DEG_TO_RAD));
        }
        return upload(label, builder.buildOrThrow());
    }

    static Matrix4f modelView() {
        return new Matrix4f(RenderSystem.getModelViewMatrix());
    }

    static void draw(String label, RenderPipeline pipeline, MeshData mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture) {
        try (mesh) {
            GpuBuffer vertices = pipeline.getVertexFormat().uploadImmediateVertexBuffer(mesh.vertexBuffer());
            draw(label, pipeline, vertices, mesh.drawState().indexCount(), mesh.drawState().mode(), modelView, color, texture);
        }
    }

    static void draw(String label, RenderPipeline pipeline, StaticMesh mesh, Matrix4fc modelView, Vector4fc color, @Nullable Identifier texture) {
        draw(label, pipeline, mesh.vertices(), mesh.indexCount(), mesh.mode(), modelView, color, texture);
    }

    private static void draw(
            String label,
            RenderPipeline pipeline,
            GpuBuffer vertices,
            int indexCount,
            VertexFormat.Mode mode,
            Matrix4fc modelView,
            Vector4fc color,
            @Nullable Identifier texture
    ) {
        if (indexCount <= 0) {
            return;
        }
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(mode);
        GpuBuffer indexBuffer = indices.getBuffer(indexCount);
        GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(modelView, color, new Vector3f(), new Matrix4f());
        AbstractTexture abstractTexture = texture != null ? Minecraft.getInstance().getTextureManager().getTexture(texture) : null;
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> label, target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", transforms);
            if (abstractTexture != null) {
                renderPass.bindTexture("Sampler0", abstractTexture.getTextureView(), abstractTexture.getSampler());
            }
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indexBuffer, indices.type());
            renderPass.drawIndexed(0, 0, indexCount, 1);
        }
    }
}
