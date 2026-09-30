package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.mixins.client.LevelRendererPortalViewAreaAccessor;
import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.ViewArea;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class PortalGunPortalViewAreaManager {
    private static final int MAX_VIEW_AREAS = 8;
    private static final LinkedHashMap<ViewKey, ViewState> VIEW_AREAS = new LinkedHashMap<>(8, 0.75F, true);
    private static final Map<SectionRenderDispatcher.RenderSection, ViewState> SECTION_OWNERS = Collections.synchronizedMap(new IdentityHashMap<>());
    private static ClientLevel ownerLevel;
    private static int ownerViewDistance = -1;

    private PortalGunPortalViewAreaManager() {
    }

    public static Scope enter(Minecraft minecraft, LevelRenderer renderer, UUID destinationId, Vec3 cameraPos, net.minecraft.client.Camera portalCamera) {
        LevelRendererPortalViewAreaAccessor accessor = (LevelRendererPortalViewAreaAccessor) renderer;
        int viewDistance = minecraft.options.getEffectiveRenderDistance();
        if (ownerLevel != minecraft.level || ownerViewDistance != viewDistance) {
            clear();
            ownerLevel = minecraft.level;
            ownerViewDistance = viewDistance;
        }
        Scope sodiumScope = SodiumCompat.isLoaded() ? new SodiumScope(SodiumCompat.beginPortalView(portalCamera)) : null;
        ViewKey viewKey = new ViewKey(renderer, destinationId);
        ViewState state = VIEW_AREAS.get(viewKey);
        if (state == null) {
            ViewArea area = accessor.antarchy$getViewArea();
            SectionOcclusionGraph graph = accessor.antarchy$getSectionOcclusionGraph();
            ObjectArrayList<SectionRenderDispatcher.RenderSection> sections = accessor.antarchy$getVisibleSections();
            state = new ViewState(area, graph, sections, accessor.antarchy$getGlobalBlockEntities());
            state.cloudBuffer = accessor.antarchy$getCloudBuffer();
            state.generateClouds = accessor.antarchy$getGenerateClouds();
            state.prevCloudX = accessor.antarchy$getPrevCloudX();
            state.prevCloudY = accessor.antarchy$getPrevCloudY();
            state.prevCloudZ = accessor.antarchy$getPrevCloudZ();
            state.prevCloudColor = accessor.antarchy$getPrevCloudColor();
            state.prevCloudsType = accessor.antarchy$getPrevCloudsType();
            for (SectionRenderDispatcher.RenderSection section : area.sections) {
                ((PortalViewSectionMarker) section).antarchy$markPortalViewSection();
                SECTION_OWNERS.put(section, state);
            }
            VIEW_AREAS.put(viewKey, state);
            trimOldViews();
        }
        state.area.repositionCamera(cameraPos.x, cameraPos.z);
        int sectionX = SectionPos.posToSectionCoord(cameraPos.x);
        int sectionY = SectionPos.posToSectionCoord(cameraPos.y);
        int sectionZ = SectionPos.posToSectionCoord(cameraPos.z);
        state.cameraPos = cameraPos;
        state.lastSectionX = sectionX;
        state.lastSectionY = sectionY;
        state.lastSectionZ = sectionZ;
        Scope scope = new VanillaScope(renderer, accessor, state, minecraft.getBlockEntityRenderDispatcher());
        accessor.antarchy$setViewArea(state.area);
        accessor.antarchy$setSectionOcclusionGraph(state.graph);
        accessor.antarchy$setVisibleSections(state.visibleSections);
        accessor.antarchy$setGlobalBlockEntities(state.globalBlockEntities);
        accessor.antarchy$setLastCameraSectionX(sectionX);
        accessor.antarchy$setLastCameraSectionY(sectionY);
        accessor.antarchy$setLastCameraSectionZ(sectionZ);
        return sodiumScope == null ? scope : new CombinedScope(scope, sodiumScope);
    }

    public static boolean routeSectionCompiled(SectionRenderDispatcher.RenderSection section) {
        ViewState state = SECTION_OWNERS.get(section);
        if (state == null) {
            return ((PortalViewSectionMarker) section).antarchy$isPortalViewSection();
        }
        state.graph.onSectionCompiled(section);
        return true;
    }

    public static Vec3 cameraForSection(SectionRenderDispatcher.RenderSection section, Vec3 fallback) {
        ViewState state = SECTION_OWNERS.get(section);
        return state == null || state.cameraPos == null ? fallback : state.cameraPos;
    }

    public static boolean updateGlobalBlockEntities(SectionRenderDispatcher.RenderSection section, Iterable<BlockEntity> removed, Iterable<BlockEntity> added) {
        ViewState state = SECTION_OWNERS.get(section);
        if (state == null) {
            return ((PortalViewSectionMarker) section).antarchy$isPortalViewSection();
        }
        synchronized (state.globalBlockEntities) {
            for (BlockEntity blockEntity : removed) {
                state.globalBlockEntities.remove(blockEntity);
            }
            for (BlockEntity blockEntity : added) {
                state.globalBlockEntities.add(blockEntity);
            }
        }
        return true;
    }

    public static void onSectionDirty(int sectionX, int sectionY, int sectionZ, boolean playerChanged) {
        for (ViewState state : VIEW_AREAS.values()) {
            int sectionRange = state.area.getViewDistance() + 1;
            if (Math.abs((long) sectionX - state.lastSectionX) > sectionRange
                    || Math.abs((long) sectionZ - state.lastSectionZ) > sectionRange
                    || sectionY < ownerLevel.getMinSection()
                    || sectionY > ownerLevel.getMaxSection()) {
                continue;
            }
            state.area.setDirty(sectionX, sectionY, sectionZ, playerChanged);
        }
    }

    public static void onChunkLoaded(net.minecraft.world.level.ChunkPos pos) {
        for (ViewState state : VIEW_AREAS.values()) {
            state.graph.onChunkLoaded(pos);
            markChunkDirty(state, pos);
        }
    }

    public static void onChunkDropped(net.minecraft.world.level.ChunkPos pos) {
        for (ViewState state : VIEW_AREAS.values()) {
            markChunkDirty(state, pos);
            state.graph.invalidate();
        }
    }

    private static void markChunkDirty(ViewState state, net.minecraft.world.level.ChunkPos pos) {
        int sectionRange = state.area.getViewDistance() + 1;
        if (Math.abs((long) pos.x - state.lastSectionX) <= sectionRange
                && Math.abs((long) pos.z - state.lastSectionZ) <= sectionRange) {
            for (int sectionY = ownerLevel.getMinSection(); sectionY <= ownerLevel.getMaxSection(); sectionY++) {
                state.area.setDirty(pos.x, sectionY, pos.z, true);
            }
        }
    }

    public static void clear() {
        SodiumCompat.clear();
        for (ViewState state : VIEW_AREAS.values()) {
            state.graph.waitAndReset(state.area);
        }
        VIEW_AREAS.clear();
        SECTION_OWNERS.clear();
        ownerLevel = null;
        ownerViewDistance = -1;
    }

    public static void releaseRenderer(LevelRenderer renderer) {
        Iterator<Map.Entry<ViewKey, ViewState>> iterator = VIEW_AREAS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ViewKey, ViewState> entry = iterator.next();
            if (entry.getKey().renderer() == renderer) {
                ViewState state = entry.getValue();
                state.graph.waitAndReset(state.area);
                removeOwners(state);
                iterator.remove();
            }
        }
    }

    private static void trimOldViews() {
        Iterator<Map.Entry<ViewKey, ViewState>> iterator = VIEW_AREAS.entrySet().iterator();
        while (VIEW_AREAS.size() > MAX_VIEW_AREAS && iterator.hasNext()) {
            ViewState state = iterator.next().getValue();
            iterator.remove();
            state.graph.waitAndReset(state.area);
            removeOwners(state);
        }
    }

    private static void removeOwners(ViewState state) {
        for (SectionRenderDispatcher.RenderSection section : state.area.sections) {
            SECTION_OWNERS.remove(section);
        }
    }

    private static final class ViewState {
        private final ViewArea area;
        private final SectionOcclusionGraph graph;
        private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;
        private final Set<BlockEntity> globalBlockEntities;
        private VertexBuffer cloudBuffer;
        private boolean generateClouds = true;
        private int prevCloudX = Integer.MIN_VALUE;
        private int prevCloudY = Integer.MIN_VALUE;
        private int prevCloudZ = Integer.MIN_VALUE;
        private Vec3 prevCloudColor = Vec3.ZERO;
        private CloudStatus prevCloudsType;
        private int lastSectionX;
        private int lastSectionY;
        private int lastSectionZ;
        private volatile Vec3 cameraPos;
        private double transparentSortX = Double.NaN;
        private double transparentSortY = Double.NaN;
        private double transparentSortZ = Double.NaN;
        private double prevCamX = Double.NaN;
        private double prevCamY = Double.NaN;
        private double prevCamZ = Double.NaN;
        private double prevCamRotX = Double.NaN;
        private double prevCamRotY = Double.NaN;

        private ViewState(ViewArea area, SectionOcclusionGraph graph, ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections, Set<BlockEntity> globalBlockEntities) {
            this.area = area;
            this.graph = graph;
            this.visibleSections = visibleSections;
            this.globalBlockEntities = globalBlockEntities;
        }
    }

    private record ViewKey(LevelRenderer renderer, UUID destinationId) {
    }

    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }

    private static final class CombinedScope implements Scope {
        private final Scope viewAreaScope;
        private final Scope sodiumScope;

        private CombinedScope(Scope viewAreaScope, Scope sodiumScope) {
            this.viewAreaScope = viewAreaScope;
            this.sodiumScope = sodiumScope;
        }

        @Override
        public void close() {
            try {
                viewAreaScope.close();
            } finally {
                sodiumScope.close();
            }
        }
    }

    private static final class SodiumScope implements Scope {
        private final SodiumCompat.TerrainScope terrainScope;

        private SodiumScope(SodiumCompat.TerrainScope terrainScope) {
            this.terrainScope = terrainScope;
        }

        @Override
        public void close() {
            terrainScope.close();
        }
    }

    private static final class VanillaScope implements Scope {
        private final LevelRendererPortalViewAreaAccessor accessor;
        private final ViewArea previousArea;
        private final SectionOcclusionGraph previousGraph;
        private final ObjectArrayList<SectionRenderDispatcher.RenderSection> previousVisibleSections;
        private final Set<BlockEntity> previousGlobalBlockEntities;
        private final VertexBuffer previousCloudBuffer;
        private final boolean previousGenerateClouds;
        private final int previousCloudX;
        private final int previousCloudY;
        private final int previousCloudZ;
        private final Vec3 previousCloudColor;
        private final CloudStatus previousCloudsType;
        private final int previousRainSoundTime;
        private final int previousSectionX;
        private final int previousSectionY;
        private final int previousSectionZ;
        private final double previousPrevCamX;
        private final double previousPrevCamY;
        private final double previousPrevCamZ;
        private final double previousPrevCamRotX;
        private final double previousPrevCamRotY;
        private final double previousTransparentSortX;
        private final double previousTransparentSortY;
        private final double previousTransparentSortZ;
        private final Vec3 previousDispatcherCamera;
        private final SectionRenderDispatcher dispatcher;
        private final BlockEntityRenderDispatcher blockEntityDispatcher;
        private final Level previousBlockEntityLevel;
        private final net.minecraft.client.Camera previousBlockEntityCamera;
        private final HitResult previousBlockEntityHitResult;
        private final ViewState state;
        private boolean closed;

        private VanillaScope(LevelRenderer renderer, LevelRendererPortalViewAreaAccessor accessor, ViewState state, BlockEntityRenderDispatcher blockEntityDispatcher) {
            this.accessor = accessor;
            this.state = state;
            this.blockEntityDispatcher = blockEntityDispatcher;
            this.previousBlockEntityLevel = blockEntityDispatcher.level;
            this.previousBlockEntityCamera = blockEntityDispatcher.camera;
            this.previousBlockEntityHitResult = blockEntityDispatcher.cameraHitResult;
            this.previousArea = accessor.antarchy$getViewArea();
            this.previousGraph = accessor.antarchy$getSectionOcclusionGraph();
            this.previousVisibleSections = accessor.antarchy$getVisibleSections();
            this.previousGlobalBlockEntities = accessor.antarchy$getGlobalBlockEntities();
            this.previousCloudBuffer = accessor.antarchy$getCloudBuffer();
            this.previousGenerateClouds = accessor.antarchy$getGenerateClouds();
            this.previousCloudX = accessor.antarchy$getPrevCloudX();
            this.previousCloudY = accessor.antarchy$getPrevCloudY();
            this.previousCloudZ = accessor.antarchy$getPrevCloudZ();
            this.previousCloudColor = accessor.antarchy$getPrevCloudColor();
            this.previousCloudsType = accessor.antarchy$getPrevCloudsType();
            this.previousRainSoundTime = accessor.antarchy$getRainSoundTime();
            this.previousSectionX = accessor.antarchy$getLastCameraSectionX();
            this.previousSectionY = accessor.antarchy$getLastCameraSectionY();
            this.previousSectionZ = accessor.antarchy$getLastCameraSectionZ();
            this.previousPrevCamX = accessor.antarchy$getPrevCamX();
            this.previousPrevCamY = accessor.antarchy$getPrevCamY();
            this.previousPrevCamZ = accessor.antarchy$getPrevCamZ();
            this.previousPrevCamRotX = accessor.antarchy$getPrevCamRotX();
            this.previousPrevCamRotY = accessor.antarchy$getPrevCamRotY();
            this.previousTransparentSortX = accessor.antarchy$getTransparentSortX();
            this.previousTransparentSortY = accessor.antarchy$getTransparentSortY();
            this.previousTransparentSortZ = accessor.antarchy$getTransparentSortZ();
            this.dispatcher = renderer.getSectionRenderDispatcher();
            this.previousDispatcherCamera = dispatcher.getCameraPosition();
            accessor.antarchy$setLastCameraSectionX(state.lastSectionX);
            accessor.antarchy$setLastCameraSectionY(state.lastSectionY);
            accessor.antarchy$setLastCameraSectionZ(state.lastSectionZ);
            accessor.antarchy$setPrevCamX(state.prevCamX);
            accessor.antarchy$setPrevCamY(state.prevCamY);
            accessor.antarchy$setPrevCamZ(state.prevCamZ);
            accessor.antarchy$setPrevCamRotX(state.prevCamRotX);
            accessor.antarchy$setPrevCamRotY(state.prevCamRotY);
            accessor.antarchy$setTransparentSortX(state.transparentSortX);
            accessor.antarchy$setTransparentSortY(state.transparentSortY);
            accessor.antarchy$setTransparentSortZ(state.transparentSortZ);
            accessor.antarchy$setCloudBuffer(state.cloudBuffer);
            accessor.antarchy$setGenerateClouds(state.generateClouds);
            accessor.antarchy$setPrevCloudX(state.prevCloudX);
            accessor.antarchy$setPrevCloudY(state.prevCloudY);
            accessor.antarchy$setPrevCloudZ(state.prevCloudZ);
            accessor.antarchy$setPrevCloudColor(state.prevCloudColor);
            accessor.antarchy$setPrevCloudsType(state.prevCloudsType);
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            state.lastSectionX = accessor.antarchy$getLastCameraSectionX();
            state.lastSectionY = accessor.antarchy$getLastCameraSectionY();
            state.lastSectionZ = accessor.antarchy$getLastCameraSectionZ();
            state.prevCamX = accessor.antarchy$getPrevCamX();
            state.prevCamY = accessor.antarchy$getPrevCamY();
            state.prevCamZ = accessor.antarchy$getPrevCamZ();
            state.prevCamRotX = accessor.antarchy$getPrevCamRotX();
            state.prevCamRotY = accessor.antarchy$getPrevCamRotY();
            state.transparentSortX = accessor.antarchy$getTransparentSortX();
            state.transparentSortY = accessor.antarchy$getTransparentSortY();
            state.transparentSortZ = accessor.antarchy$getTransparentSortZ();
            state.cloudBuffer = accessor.antarchy$getCloudBuffer();
            state.generateClouds = accessor.antarchy$getGenerateClouds();
            state.prevCloudX = accessor.antarchy$getPrevCloudX();
            state.prevCloudY = accessor.antarchy$getPrevCloudY();
            state.prevCloudZ = accessor.antarchy$getPrevCloudZ();
            state.prevCloudColor = accessor.antarchy$getPrevCloudColor();
            state.prevCloudsType = accessor.antarchy$getPrevCloudsType();
            accessor.antarchy$setViewArea(previousArea);
            accessor.antarchy$setSectionOcclusionGraph(previousGraph);
            accessor.antarchy$setVisibleSections(previousVisibleSections);
            accessor.antarchy$setGlobalBlockEntities(previousGlobalBlockEntities);
            accessor.antarchy$setCloudBuffer(previousCloudBuffer);
            accessor.antarchy$setGenerateClouds(previousGenerateClouds);
            accessor.antarchy$setPrevCloudX(previousCloudX);
            accessor.antarchy$setPrevCloudY(previousCloudY);
            accessor.antarchy$setPrevCloudZ(previousCloudZ);
            accessor.antarchy$setPrevCloudColor(previousCloudColor);
            accessor.antarchy$setPrevCloudsType(previousCloudsType);
            accessor.antarchy$setRainSoundTime(previousRainSoundTime);
            accessor.antarchy$setLastCameraSectionX(previousSectionX);
            accessor.antarchy$setLastCameraSectionY(previousSectionY);
            accessor.antarchy$setLastCameraSectionZ(previousSectionZ);
            accessor.antarchy$setPrevCamX(previousPrevCamX);
            accessor.antarchy$setPrevCamY(previousPrevCamY);
            accessor.antarchy$setPrevCamZ(previousPrevCamZ);
            accessor.antarchy$setPrevCamRotX(previousPrevCamRotX);
            accessor.antarchy$setPrevCamRotY(previousPrevCamRotY);
            accessor.antarchy$setTransparentSortX(previousTransparentSortX);
            accessor.antarchy$setTransparentSortY(previousTransparentSortY);
            accessor.antarchy$setTransparentSortZ(previousTransparentSortZ);
            dispatcher.setCamera(previousDispatcherCamera);
            blockEntityDispatcher.prepare(previousBlockEntityLevel, previousBlockEntityCamera, previousBlockEntityHitResult);
        }
    }
}
