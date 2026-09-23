package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.infinity.InfinityCompatBridge;
import com.craisinlord.antarchy.compat.infinity.InfinityWarpResult;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.util.InfinityMethods;
import net.lerariemann.infinity.util.teleport.PortalCreator;
import net.lerariemann.infinity.util.teleport.WarpLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.lerariemann.infinity.access.MinecraftServerAccess;
import com.craisinlord.antarchy.neoforge.mixins.infinity.PortalCreatorInvoker;
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
    public InfinityWarpResult requestWarp(ServerPlayer player, ResourceLocation dimensionId) {
        try {
            WarpLogic.requestWarp(player, dimensionId, false);
            return InfinityWarpResult.PENDING;
        } catch (Throwable throwable) {
            Antarchy.LOGGER.error("[Antarchy] Failed to request an Infinity warp to {}", dimensionId, throwable);
            return InfinityGenerationFailure.consume(dimensionId)
                    ? InfinityWarpResult.FAILED_GENERATION
                    : InfinityWarpResult.REJECTED;
        }
    }

    @Override
    public InfinityWarpResult requestDimensionCreation(MinecraftServer server, ResourceLocation dimensionId) {
        if (!isAvailable() || server == null || dimensionId == null || !"infinity".equals(dimensionId.getNamespace())) {
            return InfinityWarpResult.REJECTED;
        }
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, dimensionId);
        if (server.getLevel(key) != null) {
            return InfinityWarpResult.READY;
        }
        if (!(server instanceof MinecraftServerAccess access)) {
            return InfinityWarpResult.REJECTED;
        }
        try {
            if (access.infinity$hasToAdd(key)) {
                return InfinityWarpResult.PENDING;
            }
            return PortalCreatorInvoker.antarchy$invokeTryAddInfinityDimension(server, dimensionId)
                    ? InfinityWarpResult.PENDING : InfinityWarpResult.REJECTED;
        } catch (Throwable throwable) {
            Antarchy.LOGGER.error("[Antarchy] Failed to request creation of Infinity dimension {}", dimensionId, throwable);
            return InfinityWarpResult.FAILED_GENERATION;
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
