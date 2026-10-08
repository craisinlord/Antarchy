package com.craisinlord.antarchy.content.client.renderer.sodium;

import com.craisinlord.antarchy.content.client.PortalGunPortalRenderState;
import com.craisinlord.antarchy.content.client.renderer.PortalTerrain;
import com.craisinlord.antarchy.mixins.compat.sodium.RenderSectionManagerAccessor;
import com.craisinlord.antarchy.mixins.compat.sodium.SodiumWorldRendererAccessor;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import net.caffeinemc.mods.sodium.client.SodiumClientMod;
import net.caffeinemc.mods.sodium.client.gl.device.RenderDevice;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.TaskQueueType;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.CoordinateSectionVisitor;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import net.caffeinemc.mods.sodium.client.render.chunk.lists.TreeSectionCollector;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SodiumPortalTerrain implements PortalTerrain {
    private static final int FIRST_PORTAL_FRAME = -1_000_000;
    private static final int MAX_PENDING_TASKS_PER_QUEUE = 256;
    private static final Map<TaskQueueType, ArrayDeque<RenderSection>> PENDING_TASKS = new EnumMap<>(TaskQueueType.class);
    private static int nextFrame = FIRST_PORTAL_FRAME;

    private final SodiumWorldRenderer worldRenderer;
    private final RenderSectionManager sectionManager;
    private final SortedRenderLists renderLists;
    private final int sectionCount;
    private SortedRenderLists previousRenderLists;
    private boolean previousEntityCulling;

    private SodiumPortalTerrain(SodiumWorldRenderer worldRenderer, RenderSectionManager sectionManager, SortedRenderLists renderLists, int sectionCount) {
        this.worldRenderer = worldRenderer;
        this.sectionManager = sectionManager;
        this.renderLists = renderLists;
        this.sectionCount = sectionCount;
    }

    public static boolean canView(Minecraft minecraft, Vec3 exitCenter) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        if (worldRenderer == null || minecraft.player == null
                || ((SodiumWorldRendererAccessor) worldRenderer).antarchy$getRenderSectionManager() == null) {
            return false;
        }
        int limit = minecraft.options.getEffectiveRenderDistance();
        return Math.abs(SectionPos.posToSectionCoord(exitCenter.x) - minecraft.player.chunkPosition().x) <= limit
                && Math.abs(SectionPos.posToSectionCoord(exitCenter.z) - minecraft.player.chunkPosition().z) <= limit;
    }

    public static PortalTerrain prepare(Minecraft minecraft, Frustum frustum) {
        SodiumWorldRenderer worldRenderer = SodiumWorldRenderer.instanceNullable();
        RenderSectionManager sectionManager = worldRenderer == null ? null
                : ((SodiumWorldRendererAccessor) worldRenderer).antarchy$getRenderSectionManager();
        if (sectionManager == null) {
            return EmptyTerrain.INSTANCE;
        }
        RenderSectionManagerAccessor manager = (RenderSectionManagerAccessor) sectionManager;
        int frame = nextFrame--;
        if (nextFrame > FIRST_PORTAL_FRAME) {
            nextFrame = FIRST_PORTAL_FRAME;
        }
        Viewport viewport = ((ViewportProvider) frustum).sodium$createViewport();
        TreeSectionCollector collector = new TreeSectionCollector(frame,
                SodiumClientMod.options().performance.chunkBuildDeferMode.getImportantRebuildQueueType(),
                manager.antarchy$getSortBehavior().getDeferMode().getImportantRebuildQueueType(),
                manager.antarchy$getSectionByPosition());
        int[] visited = {0};
        CoordinateSectionVisitor apertureFilter = (x, y, z) -> {
            int minX = SectionPos.sectionToBlockCoord(x);
            int minY = SectionPos.sectionToBlockCoord(y);
            int minZ = SectionPos.sectionToBlockCoord(z);
            if (PortalGunPortalRenderState.shouldRenderBounds(new AABB(minX, minY, minZ, minX + 16, minY + 16, minZ + 16))) {
                collector.visit(x, y, z);
                visited[0]++;
            }
        };
        manager.antarchy$getRenderableSectionTree().prepareForTraversal();
        manager.antarchy$getRenderableSectionTree().traverse(apertureFilter, viewport, manager.antarchy$getSearchDistance());
        SortedRenderLists renderLists = collector.createRenderLists(viewport);
        stashPendingTasks(collector.getTaskLists());
        sectionManager.markGraphDirty();
        return new SodiumPortalTerrain(worldRenderer, sectionManager, renderLists, visited[0]);
    }

    public static void drainPendingTasks(Map<TaskQueueType, ArrayDeque<RenderSection>> taskLists) {
        if (PENDING_TASKS.isEmpty() || taskLists == null) {
            return;
        }
        for (Map.Entry<TaskQueueType, ArrayDeque<RenderSection>> entry : PENDING_TASKS.entrySet()) {
            ArrayDeque<RenderSection> queue = taskLists.get(entry.getKey());
            if (queue == null) {
                continue;
            }
            int limit = entry.getKey().queueSizeLimit();
            for (RenderSection section : entry.getValue()) {
                if (queue.size() >= limit) {
                    break;
                }
                if (!section.isDisposed() && section.getPendingUpdate() != 0 && section.getRunningJob() == null) {
                    queue.add(section);
                }
            }
        }
        PENDING_TASKS.clear();
    }

    private static void stashPendingTasks(Map<TaskQueueType, ArrayDeque<RenderSection>> taskLists) {
        for (Map.Entry<TaskQueueType, ArrayDeque<RenderSection>> entry : taskLists.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            ArrayDeque<RenderSection> pending = PENDING_TASKS.computeIfAbsent(entry.getKey(), type -> new ArrayDeque<>());
            for (RenderSection section : entry.getValue()) {
                if (pending.size() >= MAX_PENDING_TASKS_PER_QUEUE) {
                    break;
                }
                pending.add(section);
            }
        }
    }

    @Override
    public int sectionCount() {
        return this.sectionCount;
    }

    @Override
    public void begin(Minecraft minecraft) {
        RenderSectionManagerAccessor manager = (RenderSectionManagerAccessor) this.sectionManager;
        SodiumWorldRendererAccessor renderer = (SodiumWorldRendererAccessor) this.worldRenderer;
        this.previousRenderLists = manager.antarchy$getRenderLists();
        this.previousEntityCulling = renderer.antarchy$getUseEntityCulling();
        manager.antarchy$setRenderLists(this.renderLists);
        renderer.antarchy$setUseEntityCulling(false);
    }

    @Override
    public void end(Minecraft minecraft) {
        ((RenderSectionManagerAccessor) this.sectionManager).antarchy$setRenderLists(this.previousRenderLists);
        ((SodiumWorldRendererAccessor) this.worldRenderer).antarchy$setUseEntityCulling(this.previousEntityCulling);
    }

    @Override
    public void forEachBlockEntity(Minecraft minecraft, Frustum frustum, Consumer<BlockEntity> consumer) {
        this.worldRenderer.iterateVisibleBlockEntities(consumer);
    }

    @Override
    public void drawLayer(Minecraft minecraft, RenderType renderType, Vec3 cameraPosition, Matrix4f viewMatrix, Matrix4f projection) {
        TerrainRenderPass[] passes;
        if (renderType == RenderType.solid()) {
            passes = new TerrainRenderPass[] {DefaultTerrainRenderPasses.SOLID, DefaultTerrainRenderPasses.CUTOUT};
        } else if (renderType == RenderType.translucent()) {
            passes = new TerrainRenderPass[] {DefaultTerrainRenderPasses.TRANSLUCENT};
        } else {
            return;
        }
        ChunkRenderMatrices matrices = new ChunkRenderMatrices(projection, viewMatrix);
        RenderDevice.enterManagedCode();
        try {
            for (TerrainRenderPass pass : passes) {
                this.sectionManager.renderLayer(matrices, pass, cameraPosition.x, cameraPosition.y, cameraPosition.z);
            }
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    private enum EmptyTerrain implements PortalTerrain {
        INSTANCE;

        @Override
        public int sectionCount() {
            return 0;
        }

        @Override
        public void begin(Minecraft minecraft) {
        }

        @Override
        public void end(Minecraft minecraft) {
        }

        @Override
        public void drawLayer(Minecraft minecraft, RenderType renderType, Vec3 cameraPosition, Matrix4f viewMatrix, Matrix4f projection) {
        }

        @Override
        public void forEachBlockEntity(Minecraft minecraft, Frustum frustum, Consumer<BlockEntity> consumer) {
        }
    }
}
