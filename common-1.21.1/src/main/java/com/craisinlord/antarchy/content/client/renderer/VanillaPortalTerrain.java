package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.mixins.client.LevelRendererPortalViewAreaAccessor;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

final class VanillaPortalTerrain implements PortalTerrain {
    private static final int MAX_REBUILDS_PER_SCENE = 32;

    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> sections;

    private VanillaPortalTerrain(ObjectArrayList<SectionRenderDispatcher.RenderSection> sections) {
        this.sections = sections;
    }

    static VanillaPortalTerrain prepare(Minecraft minecraft, UUID exitId, Vec3 exitCenter, Vec3 exitNormal, Frustum frustum) {
        PortalGunPortalSectionViews.View view = PortalGunPortalSectionViews.prepare(minecraft, exitId, exitCenter, exitNormal, frustum);
        view.visibleSections().removeIf(section -> !PortalGunPortalRenderState.shouldRenderBounds(section.getBoundingBox()));
        queueDirtySections(minecraft, view.visibleSections());
        return new VanillaPortalTerrain(view.visibleSections());
    }

    @Override
    public int sectionCount() {
        return this.sections.size();
    }

    @Override
    public void begin(Minecraft minecraft) {
    }

    @Override
    public void end(Minecraft minecraft) {
    }

    @Override
    public void drawLayer(Minecraft minecraft, RenderType renderType, Vec3 cameraPosition, Matrix4f viewMatrix, Matrix4f projection) {
        renderType.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        if (shader == null) {
            renderType.clearRenderState();
            return;
        }
        shader.setDefaultUniforms(VertexFormat.Mode.QUADS, viewMatrix, projection, minecraft.getWindow());
        shader.apply();
        Uniform chunkOffset = shader.CHUNK_OFFSET;
        boolean frontToBack = renderType != RenderType.translucent();
        ObjectListIterator<SectionRenderDispatcher.RenderSection> iterator =
                this.sections.listIterator(frontToBack ? 0 : this.sections.size());
        try {
            while (frontToBack ? iterator.hasNext() : iterator.hasPrevious()) {
                SectionRenderDispatcher.RenderSection section = frontToBack ? iterator.next() : iterator.previous();
                if (section.getCompiled().isEmpty(renderType)) {
                    continue;
                }
                VertexBuffer buffer = section.getBuffer(renderType);
                BlockPos origin = section.getOrigin();
                if (chunkOffset != null) {
                    chunkOffset.set((float) (origin.getX() - cameraPosition.x), (float) (origin.getY() - cameraPosition.y),
                            (float) (origin.getZ() - cameraPosition.z));
                    chunkOffset.upload();
                }
                buffer.bind();
                buffer.draw();
            }
        } finally {
            if (chunkOffset != null) {
                chunkOffset.set(0.0F, 0.0F, 0.0F);
            }
            shader.clear();
            VertexBuffer.unbind();
            renderType.clearRenderState();
        }
    }

    @Override
    public void forEachBlockEntity(Minecraft minecraft, Frustum frustum, Consumer<BlockEntity> consumer) {
        for (SectionRenderDispatcher.RenderSection section : this.sections) {
            for (BlockEntity blockEntity : section.getCompiled().getRenderableBlockEntities()) {
                consumer.accept(blockEntity);
            }
        }
        LevelRendererPortalViewAreaAccessor state = (LevelRendererPortalViewAreaAccessor) minecraft.levelRenderer;
        synchronized (state.antarchy$getGlobalBlockEntities()) {
            for (BlockEntity blockEntity : state.antarchy$getGlobalBlockEntities()) {
                if (frustum.isVisible(new AABB(blockEntity.getBlockPos()).inflate(1.0D))) {
                    consumer.accept(blockEntity);
                }
            }
        }
    }

    private static void queueDirtySections(Minecraft minecraft, ObjectArrayList<SectionRenderDispatcher.RenderSection> sections) {
        SectionRenderDispatcher dispatcher = minecraft.levelRenderer.getSectionRenderDispatcher();
        if (dispatcher == null) {
            return;
        }
        RenderRegionCache cache = null;
        int queued = 0;
        for (SectionRenderDispatcher.RenderSection section : sections) {
            if (queued >= MAX_REBUILDS_PER_SCENE) {
                break;
            }
            if (!section.isDirty() || !minecraft.level.getLightEngine().lightOnInSection(SectionPos.of(section.getOrigin()))) {
                continue;
            }
            if (cache == null) {
                cache = new RenderRegionCache();
            }
            section.rebuildSectionAsync(dispatcher, cache);
            section.setNotDirty();
            queued++;
        }
    }
}
