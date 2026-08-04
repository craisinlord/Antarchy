package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatBridge;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

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
            Antarchy.LOGGER.error("[Antarchy] Failed to get a random Infinity dimension id", throwable);
            return null;
        }
    }

    @Override
    public ResourceLocation getRandomDimensionId(RandomSource random, double easterEggChance) {
        try {
            if (easterEggChance > 0.0D && random.nextDouble() < easterEggChance) {
                ResourceLocation easterDimensionId = getRandomEasterDimensionId(random);
                if (easterDimensionId != null) {
                    return easterDimensionId;
                }
            }

            return InfinityMethods.getRandomId(random);
        } catch (Throwable throwable) {
            Antarchy.LOGGER.error("[Antarchy] Failed to get a random Infinity dimension id", throwable);
            return null;
        }
    }

    private static ResourceLocation getRandomEasterDimensionId(RandomSource random) {
        if (InfinityMod.provider == null || InfinityMod.provider.easterizer == null) {
            return null;
        }

        List<String> easterNames = new ArrayList<>();
        for (String easterName : InfinityMod.provider.easterizer.map.keySet()) {
            if (InfinityMod.provider.easterizer.isEaster(easterName)) {
                easterNames.add(easterName);
            }
        }

        if (easterNames.isEmpty()) {
            return null;
        }

        String chosenEaster = easterNames.get(random.nextInt(easterNames.size()));
        return InfinityMod.provider.easterizer.getAsEaster(chosenEaster);
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
}
