package com.craisinlord.antarchy.content.client.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.LevelRenderer;

final class IrisPortalSceneCompat {
    private static final Field PIPELINE_FIELD;
    private static final Method GET_PIPELINE_MANAGER;
    private static final Method GET_PIPELINE;

    static {
        Field field = null;
        Method manager = null;
        Method pipeline = null;
        try {
            Class<?> iris = Class.forName("net.irisshaders.iris.Iris");
            manager = iris.getMethod("getPipelineManager");
            pipeline = manager.getReturnType().getMethod("getPipelineNullable");
            field = LevelRenderer.class.getDeclaredField("pipeline");
            field.setAccessible(true);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            field = null;
        }
        PIPELINE_FIELD = field;
        GET_PIPELINE_MANAGER = field == null ? null : manager;
        GET_PIPELINE = field == null ? null : pipeline;
    }

    private IrisPortalSceneCompat() {
    }

    static boolean begin(LevelRenderer renderer) {
        if (PIPELINE_FIELD == null) {
            return false;
        }
        try {
            if (PIPELINE_FIELD.get(renderer) != null) {
                return false;
            }
            Object pipeline = GET_PIPELINE.invoke(GET_PIPELINE_MANAGER.invoke(null));
            if (pipeline == null) {
                return false;
            }
            PIPELINE_FIELD.set(renderer, pipeline);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    static void end(LevelRenderer renderer, boolean installed) {
        if (!installed) {
            return;
        }
        try {
            PIPELINE_FIELD.set(renderer, null);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
        }
    }
}
