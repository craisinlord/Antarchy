package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

public final class ElythiaArrival {
    public static final int MIN_ARRIVAL_Y = 101;
    private static final int SEARCH_RADIUS = 192;
    private static final int SEARCH_STEP = 16;
    private static final int DIRECTIONS = 8;

    private ElythiaArrival() {
    }

    public static BlockPos nearestPlateau(ServerLevel level, BlockPos preferredPos) {
        if (!level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            return preferredPos;
        }
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        int height = surfaceHeight(generator, randomState, level, preferredPos.getX(), preferredPos.getZ());
        if (height > MIN_ARRIVAL_Y) {
            return preferredPos;
        }
        for (int radius = SEARCH_STEP; radius <= SEARCH_RADIUS; radius += SEARCH_STEP) {
            double offset = (radius / SEARCH_STEP & 1) * 0.5D;
            for (int direction = 0; direction < DIRECTIONS; direction++) {
                double angle = (direction + offset) * Math.PI * 2.0D / DIRECTIONS;
                int x = preferredPos.getX() + Mth.floor(Math.cos(angle) * radius);
                int z = preferredPos.getZ() + Mth.floor(Math.sin(angle) * radius);
                int candidateHeight = surfaceHeight(generator, randomState, level, x, z);
                if (candidateHeight > MIN_ARRIVAL_Y) {
                    return new BlockPos(x, candidateHeight, z);
                }
            }
        }
        return preferredPos;
    }

    private static int surfaceHeight(ChunkGenerator generator, RandomState randomState, ServerLevel level, int x, int z) {
        return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
    }
}
