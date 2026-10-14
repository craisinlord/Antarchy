package com.craisinlord.antarchy.fabric;

import net.minecraft.resources.ResourceLocation;

public final class InfinityGenerationFailure {
    private static final ThreadLocal<ResourceLocation> FAILURE = new ThreadLocal<>();

    private InfinityGenerationFailure() {
    }

    public static void mark(ResourceLocation dimensionId) {
        FAILURE.set(dimensionId);
    }

    public static boolean consume(ResourceLocation dimensionId) {
        ResourceLocation failed = FAILURE.get();
        FAILURE.remove();
        return dimensionId.equals(failed);
    }
}
