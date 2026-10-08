package com.craisinlord.antarchy.content.client.renderer;

public final class SodiumCompat {
    private static final String SODIUM_RENDERER_CLASS = "net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer";
    private static Boolean loaded;

    private SodiumCompat() {
    }

    public static boolean isLoaded() {
        if (loaded == null) {
            loaded = detect();
        }
        return loaded;
    }

    private static boolean detect() {
        String resource = SODIUM_RENDERER_CLASS.replace('.', '/') + ".class";
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null && contextLoader.getResource(resource) != null) {
            return true;
        }
        ClassLoader ownLoader = SodiumCompat.class.getClassLoader();
        return ownLoader != null && ownLoader.getResource(resource) != null;
    }
}
