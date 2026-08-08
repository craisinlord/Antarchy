package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatBridge;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.PortalCreator;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class FabricInfinityCompat implements InfinityCompatBridge {
    public static boolean isAvailableOnClasspath() {
        try {
            Class.forName("net.lerariemann.infinity.util.InfinityMethods");
            Class.forName("net.lerariemann.infinity.util.teleport.WarpLogic");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ResourceLocation getRandomDimensionId(RandomSource random) {
        try {
            return InfinityMethods.getRandomId(random);
        } catch (Throwable throwable) {
            Antarchy.LOGGER.error("[Antarchy] Failed to get a random Infinity dimension id", throwable);
            return null;
        }
    }

    @Override
    public boolean requestWarp(ServerPlayer player, ResourceLocation dimensionId) {
        try {
            WarpLogic.requestWarp(player, dimensionId, false);
            return true;
        } catch (Throwable throwable) {
            Antarchy.LOGGER.error("[Antarchy] Failed to request an Infinity warp to {}", dimensionId, throwable);
            return false;
        }
    }

    @Override
    public boolean createPhysicalPortal(ServerLevel level, BlockPos anyPortalBlockPos, ResourceLocation dimensionId) {
        try {
            return PortalCreator.modifyOnInitialCollision(dimensionId, level, anyPortalBlockPos);
        } catch (Throwable throwable) {
            Antarchy.LOGGER.error("[Antarchy] Failed to convert a portal frame into an Infinity portal to {}", dimensionId, throwable);
            return false;
        }
    }
}
