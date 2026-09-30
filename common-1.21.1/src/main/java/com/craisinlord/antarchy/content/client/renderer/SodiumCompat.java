package com.craisinlord.antarchy.content.client.renderer;

import com.craisinlord.antarchy.Antarchy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.Camera;

public final class SodiumCompat {
    private static final String SODIUM_RENDERER_CLASS = "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer";
    private static final ThreadLocal<RenderState> RENDER_STATE = ThreadLocal.withInitial(RenderState::new);
    private static boolean restoreWarningLogged;

    private SodiumCompat() {
    }

    public static boolean isLoaded() {
        String resource = SODIUM_RENDERER_CLASS.replace('.', '/') + ".class";
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && contextLoader.getResource(resource) != null) {
            return true;
        }
        ClassLoader ownLoader = SodiumCompat.class.getClassLoader();
        return ownLoader != null && ownLoader.getResource(resource) != null;
    }

    public static void captureTerrainSetup(Camera camera, Object viewport, boolean spectator, boolean updateChunks) {
        RENDER_STATE.get().activeSetup = new TerrainSetup(camera, viewport, spectator, updateChunks);
    }

    public static TerrainScope beginPortalView(Camera portalCamera) {
        RenderState state = RENDER_STATE.get();
        TerrainSetup previousSetup = state.activeSetup;
        state.previousSetups.push(new PreviousSetup(previousSetup));
        if (previousSetup != null) {
            restoreTerrainSetup(previousSetup.withCamera(portalCamera), state);
        }
        return () -> {
            PreviousSetup previous = state.previousSetups.pop();
            if (previous.setup() == null) {
                state.activeSetup = null;
                return;
            }
            restoreTerrainSetup(previous.setup(), state);
        };
    }

    public static void clear() {
        RENDER_STATE.remove();
    }

    private static void restoreTerrainSetup(TerrainSetup setup, RenderState state) {
        try {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            Class<?> rendererClass = Class.forName(SODIUM_RENDERER_CLASS, false,
                    contextLoader == null ? SodiumCompat.class.getClassLoader() : contextLoader);
            Object renderer = rendererClass.getMethod("instanceNullable").invoke(null);
            if (renderer == null) {
                return;
            }
            Method terrainSetup = null;
            for (Method method : rendererClass.getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (method.getName().equals("setupTerrain")
                        && parameters.length == 4
                        && parameters[0].isInstance(setup.camera())
                        && parameters[1].isInstance(setup.viewport())
                        && parameters[2] == boolean.class
                        && parameters[3] == boolean.class) {
                    terrainSetup = method;
                    break;
                }
            }
            if (terrainSetup == null) {
                logRestoreFailure("Sodium terrain setup method was not found", null);
                state.activeSetup = setup;
                return;
            }
            terrainSetup.invoke(renderer, setup.camera(), setup.viewport(), setup.spectator(), setup.updateChunks());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | LinkageError exception) {
            logRestoreFailure("Could not restore Sodium terrain after a portal view", exception);
            state.activeSetup = setup;
        }
    }

    private static void logRestoreFailure(String message, Throwable exception) {
        if (!restoreWarningLogged) {
            restoreWarningLogged = true;
            if (exception == null) {
                Antarchy.LOGGER.warn(message);
            } else {
                Antarchy.LOGGER.warn(message, exception);
            }
        }
    }

    @FunctionalInterface
    public interface TerrainScope extends AutoCloseable {
        @Override
        void close();
    }

    private static final class RenderState {
        private final Deque<PreviousSetup> previousSetups = new ArrayDeque<>();
        private TerrainSetup activeSetup;
    }

    private record PreviousSetup(TerrainSetup setup) {
    }

    private record TerrainSetup(Camera camera, Object viewport, boolean spectator, boolean updateChunks) {
        private TerrainSetup withCamera(Camera camera) {
            return new TerrainSetup(camera, this.viewport, this.spectator, this.updateChunks);
        }
    }
}
