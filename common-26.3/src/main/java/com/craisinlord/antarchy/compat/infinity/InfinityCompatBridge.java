package com.craisinlord.antarchy.compat.infinity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public interface InfinityCompatBridge {
    InfinityCompatBridge NOOP = new InfinityCompatBridge() {
    };

    default boolean isAvailable() {
        return false;
    }

    default Identifier getRandomDimensionId(RandomSource random) {
        return null;
    }

    default Identifier getRandomDimensionId(RandomSource random, double easterEggChance) {
        return getRandomDimensionId(random);
    }

    default InfinityWarpResult requestWarp(ServerPlayer player, Identifier dimensionId) {
        return InfinityWarpResult.REJECTED;
    }

    default InfinityWarpResult requestDimensionCreation(MinecraftServer server, Identifier dimensionId) {
        return InfinityWarpResult.REJECTED;
    }

    default boolean createPhysicalPortal(ServerLevel level, BlockPos anyPortalBlockPos, Identifier dimensionId) {
        return false;
    }
}
