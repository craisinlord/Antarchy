package com.craisinlord.antarchy.content.worldgen.elythia;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

public final class ElythiaMassifFinder {
    public static final ResourceKey<Biome> PRIMORDIAL_MASSIF = ResourceKey.create(Registries.BIOME,
            Identifier.fromNamespaceAndPath("antarchy", "primordial_massif"));
    private static final int SEARCH_STEP = 64;
    private static final int SEARCH_RADIUS = 8192;
    private static final int SAMPLE_Y = 160;
    private static final int[] CLIMB_STEPS = {32, 16, 8, 4, 2, 1};
    private static final int MAX_CLIMB_MOVES = 400;

    private ElythiaMassifFinder() {
    }

    public record Peak(BlockPos pos, int distance) {
    }

    public static Peak find(ServerLevel level, BlockPos origin, List<BlockPos> excluded, int excludeRadius) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState randomState = level.getChunkSource().randomState();
        BiomeSource biomes = generator.getBiomeSource();
        long excludeSq = (long) excludeRadius * excludeRadius;
        for (int radius = 0; radius <= SEARCH_RADIUS; radius += SEARCH_STEP) {
            int samples = radius == 0 ? 1 : Math.max(8, Mth.ceil(Math.PI * 2.0D * radius / SEARCH_STEP));
            for (int i = 0; i < samples; i++) {
                double angle = Math.PI * 2.0D * i / samples;
                int x = origin.getX() + Mth.floor(Math.cos(angle) * radius);
                int z = origin.getZ() + Mth.floor(Math.sin(angle) * radius);
                if (!isMassif(biomes, randomState, x, z) || isExcluded(x, z, excluded, excludeSq)) {
                    continue;
                }
                BlockPos peak = climb(generator, randomState, level, x, z);
                if (isExcluded(peak.getX(), peak.getZ(), excluded, excludeSq)) {
                    continue;
                }
                int distance = Mth.floor(Math.sqrt(peak.distSqr(new BlockPos(origin.getX(), peak.getY(), origin.getZ()))));
                return new Peak(peak, distance);
            }
        }
        return null;
    }

    private static boolean isMassif(BiomeSource biomes, RandomState randomState, int x, int z) {
        Holder<Biome> biome = biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(SAMPLE_Y), QuartPos.fromBlock(z), randomState.sampler());
        return biome.is(PRIMORDIAL_MASSIF);
    }

    private static boolean isExcluded(int x, int z, List<BlockPos> excluded, long excludeSq) {
        for (BlockPos pos : excluded) {
            long dx = x - pos.getX();
            long dz = z - pos.getZ();
            if (dx * dx + dz * dz < excludeSq) {
                return true;
            }
        }
        return false;
    }

    private static BlockPos climb(ChunkGenerator generator, RandomState randomState, ServerLevel level, int x, int z) {
        int height = height(generator, randomState, level, x, z);
        int moves = 0;
        for (int step : CLIMB_STEPS) {
            boolean moved = true;
            while (moved && moves < MAX_CLIMB_MOVES) {
                moved = false;
                int bestX = x;
                int bestZ = z;
                int bestHeight = height;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) {
                            continue;
                        }
                        int candidate = height(generator, randomState, level, x + dx * step, z + dz * step);
                        if (candidate > bestHeight) {
                            bestHeight = candidate;
                            bestX = x + dx * step;
                            bestZ = z + dz * step;
                        }
                    }
                }
                if (bestHeight > height) {
                    x = bestX;
                    z = bestZ;
                    height = bestHeight;
                    moved = true;
                    moves++;
                }
            }
        }
        return new BlockPos(x, height, z);
    }

    private static int height(ChunkGenerator generator, RandomState randomState, ServerLevel level, int x, int z) {
        return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
    }
}
