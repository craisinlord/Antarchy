package com.craisinlord.antarchy.content.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;

public final class WorldgenWriteZone {
    private WorldgenWriteZone() {
    }

    public static boolean contains(WorldGenLevel level, BlockPos pos) {
        return !(level instanceof WorldGenRegion region) || region.isWithinWriteZone(pos);
    }

    public static boolean contains(WorldGenLevel level, int x, int z) {
        return contains(level, new BlockPos(x, 0, z));
    }
}
