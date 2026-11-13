package com.craisinlord.antarchy.content.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;

public final class LoadedChunkGuard {
    private LoadedChunkGuard() {
    }

    public static boolean isNeighborhoodUnloaded(LevelAccessor level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) {
            return false;
        }
        int minX = (pos.getX() - 1) >> 4;
        int maxX = (pos.getX() + 1) >> 4;
        int minZ = (pos.getZ() - 1) >> 4;
        int maxZ = (pos.getZ() + 1) >> 4;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (server.getChunkSource().getChunkNow(x, z) == null) {
                    return true;
                }
            }
        }
        return false;
    }
}
