package com.craisinlord.antarchy.content.client.renderer;

import java.lang.reflect.Method;

final class PrimordialGrassSodiumVisibility {
    private static final String RENDERER = "me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer";

    private PrimordialGrassSodiumVisibility() {
    }

    static boolean visible(int sectionX, int sectionY, int sectionZ) {
        try {
            Class<?> type = Class.forName(RENDERER, false, PrimordialGrassSodiumVisibility.class.getClassLoader());
            Object renderer;
            try {
                renderer = type.getMethod("instanceNullable").invoke(null);
            } catch (NoSuchMethodException ignored) {
                renderer = type.getMethod("instance").invoke(null);
            }
            if (renderer == null) {
                return true;
            }
            Method visible = type.getMethod("isBoxVisible", double.class, double.class, double.class, double.class, double.class, double.class);
            double x = sectionX * 16.0D;
            double y = sectionY * 16.0D;
            double z = sectionZ * 16.0D;
            return (boolean) visible.invoke(renderer, x, y, z, x + 15.9D, y + 15.9D, z + 15.9D);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return true;
        }
    }
}