package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.compat.infinity.InfinityCompatBridge;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

public final class NeoForgeInfinityCompat implements InfinityCompatBridge {
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ResourceLocation getRandomDimensionId(RandomSource random) {
        try {
            return InfinityMethods.getRandomId(random);
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Override
    public boolean requestWarp(ServerPlayer player, ResourceLocation dimensionId) {
        try {
            WarpLogic.requestWarp(player, dimensionId, false);
            return true;
        } catch (Throwable throwable) {
            return false;
        }
    }
}
