package com.craisinlord.antarchy.content.client.renderer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.RenderRegionCache;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

public final class PortalGunPortalRendererPool {
    private static final int MAX_RENDERERS = 8;
    private static final long PORTAL_SECTION_BUDGET_NANOS = 4_166_666L;
    private static final LinkedHashMap<UUID, PortalRenderer> RENDERERS = new LinkedHashMap<>(8, 0.75F, true);
    private static final Set<LevelRenderer> PROXY_RENDERERS = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private static final ThreadLocal<Long> SECTION_BUDGET_DEADLINE = ThreadLocal.withInitial(() -> 0L);
    private static final ThreadLocal<Integer> SCHEDULED_SECTION_REBUILDS = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> SKIP_SET_NOT_DIRTY = ThreadLocal.withInitial(() -> false);
    private static ClientLevel ownerLevel;

    private PortalGunPortalRendererPool() {
    }

    public static LevelRenderer acquire(Minecraft minecraft, UUID destinationId, int width, int height) {
        if (ownerLevel != minecraft.level) {
            clear();
            ownerLevel = minecraft.level;
        }
        PortalRenderer state = RENDERERS.get(destinationId);
        if (state == null) {
            LevelRenderer renderer = new LevelRenderer(
                    minecraft,
                    minecraft.getEntityRenderDispatcher(),
                    minecraft.getBlockEntityRenderDispatcher(),
                    minecraft.renderBuffers()
            );
            PROXY_RENDERERS.add(renderer);
            renderer.setLevel(minecraft.level);
            renderer.resize(width, height);
            state = new PortalRenderer(renderer, width, height);
            RENDERERS.put(destinationId, state);
            trim();
        } else if (state.width() != width || state.height() != height) {
            state.renderer().resize(width, height);
            RENDERERS.put(destinationId, new PortalRenderer(state.renderer(), width, height));
        }
        return state.renderer();
    }

    public static boolean isProxy(LevelRenderer renderer) {
        return PROXY_RENDERERS.contains(renderer);
    }

    public static void beginFrame() {
        SECTION_BUDGET_DEADLINE.set(System.nanoTime() + PORTAL_SECTION_BUDGET_NANOS);
        SCHEDULED_SECTION_REBUILDS.set(0);
        SKIP_SET_NOT_DIRTY.set(false);
    }

    public static void beginSceneBudget() {
        SECTION_BUDGET_DEADLINE.set(System.nanoTime() + PORTAL_SECTION_BUDGET_NANOS);
        SCHEDULED_SECTION_REBUILDS.set(0);
        SKIP_SET_NOT_DIRTY.set(false);
    }

    public static int scheduledSectionRebuilds() {
        return SCHEDULED_SECTION_REBUILDS.get();
    }

    public static long remainingSectionBudgetNanos() {
        return Math.max(0L, SECTION_BUDGET_DEADLINE.get() - System.nanoTime());
    }

    public static void rebuildSection(LevelRenderer renderer, SectionRenderDispatcher dispatcher, SectionRenderDispatcher.RenderSection section, RenderRegionCache cache) {
        if (!isProxy(renderer)) {
            dispatcher.rebuildSectionSync(section, cache);
            SKIP_SET_NOT_DIRTY.set(false);
            return;
        }
        if (remainingSectionBudgetNanos() <= 0L) {
            SKIP_SET_NOT_DIRTY.set(true);
            return;
        }
        section.rebuildSectionAsync(dispatcher, cache);
        SCHEDULED_SECTION_REBUILDS.set(SCHEDULED_SECTION_REBUILDS.get() + 1);
        SKIP_SET_NOT_DIRTY.set(false);
    }

    public static void rebuildSectionAsync(LevelRenderer renderer, SectionRenderDispatcher dispatcher, SectionRenderDispatcher.RenderSection section, RenderRegionCache cache) {
        if (!isProxy(renderer)) {
            section.rebuildSectionAsync(dispatcher, cache);
            SKIP_SET_NOT_DIRTY.set(false);
            return;
        }
        if (remainingSectionBudgetNanos() <= 0L) {
            SKIP_SET_NOT_DIRTY.set(true);
            return;
        }
        section.rebuildSectionAsync(dispatcher, cache);
        SCHEDULED_SECTION_REBUILDS.set(SCHEDULED_SECTION_REBUILDS.get() + 1);
        SKIP_SET_NOT_DIRTY.set(false);
    }

    public static void setNotDirty(SectionRenderDispatcher.RenderSection section) {
        boolean leaveDirty = SKIP_SET_NOT_DIRTY.get();
        SKIP_SET_NOT_DIRTY.set(false);
        if (!leaveDirty) {
            section.setNotDirty();
        }
    }

    public static void clear() {
        for (PortalRenderer state : new ArrayList<>(RENDERERS.values())) {
            LevelRenderer renderer = state.renderer();
            PortalGunPortalViewAreaManager.releaseRenderer(renderer);
            try {
                renderer.close();
            } finally {
                PROXY_RENDERERS.remove(renderer);
            }
        }
        RENDERERS.clear();
        ownerLevel = null;
    }

    private static void trim() {
        Iterator<Map.Entry<UUID, PortalRenderer>> iterator = RENDERERS.entrySet().iterator();
        while (RENDERERS.size() > MAX_RENDERERS && iterator.hasNext()) {
            PortalRenderer state = iterator.next().getValue();
            iterator.remove();
            LevelRenderer renderer = state.renderer();
            PortalGunPortalViewAreaManager.releaseRenderer(renderer);
            try {
                renderer.close();
            } finally {
                PROXY_RENDERERS.remove(renderer);
            }
        }
    }

    private record PortalRenderer(LevelRenderer renderer, int width, int height) {
    }
}
