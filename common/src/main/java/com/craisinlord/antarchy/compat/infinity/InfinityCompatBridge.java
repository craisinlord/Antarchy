package com.craisinlord.antarchy.compat.infinity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public interface InfinityCompatBridge {
    InfinityCompatBridge NOOP = new InfinityCompatBridge() {
    };

    default boolean isAvailable() {
        return false;
    }

    default ResourceLocation getRandomDimensionId(RandomSource random) {
        return null;
    }

    default boolean requestWarp(ServerPlayer player, ResourceLocation dimensionId) {
        return false;
    }
}
