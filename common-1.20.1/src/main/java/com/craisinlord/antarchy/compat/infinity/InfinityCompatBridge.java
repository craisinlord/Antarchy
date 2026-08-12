package com.craisinlord.antarchy.compat.infinity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

    default ResourceLocation getRandomDimensionId(RandomSource random, double easterEggChance) {
        return getRandomDimensionId(random);
    }

    default boolean requestWarp(ServerPlayer player, ResourceLocation dimensionId) {
        return false;
    }

    /**
     * Converts an already-ignited vanilla Nether portal frame at {@code anyPortalBlockPos} into a real, permanent
     * Infinite Dimensions portal to {@code dimensionId}, generating the dimension first if it doesn't exist yet.
     * Mirrors what Infinity itself does when a written book is thrown into a lit portal.
     */
    default boolean createPhysicalPortal(ServerLevel level, BlockPos anyPortalBlockPos, ResourceLocation dimensionId) {
        return false;
    }
}
