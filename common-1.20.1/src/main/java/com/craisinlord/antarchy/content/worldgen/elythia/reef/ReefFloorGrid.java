package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import java.util.Arrays;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

final class ReefFloorGrid {
    static final int STEP = 16;
    static final byte OCEAN = 1;
    static final byte SPIKES = 2;
    static final byte OTHER = 3;

    record Sampler(ChunkGenerator generator, LevelHeightAccessor heightAccessor, RandomState randomState) {
    }

    private static final int UNKNOWN = Integer.MIN_VALUE;

    private final int originX;
    private final int originZ;
    private final int size;
    private final int biomeY;
    private final int[] heights;
    private final byte[] biomes;

    ReefFloorGrid(int cellMinX, int cellMinZ, int cellSize, int seaLevel) {
        this.originX = cellMinX - STEP;
        this.originZ = cellMinZ - STEP;
        this.size = cellSize / STEP + 3;
        this.biomeY = seaLevel - 4;
        this.heights = new int[this.size * this.size];
        this.biomes = new byte[this.size * this.size];
        Arrays.fill(this.heights, UNKNOWN);
    }

    int size() {
        return this.size;
    }

    int pointX(int i) {
        return this.originX + i * STEP;
    }

    int pointZ(int j) {
        return this.originZ + j * STEP;
    }

    int indexX(double x) {
        return Mth.clamp(Mth.floor((x - this.originX) / STEP + 0.5D), 0, this.size - 1);
    }

    int indexZ(double z) {
        return Mth.clamp(Mth.floor((z - this.originZ) / STEP + 0.5D), 0, this.size - 1);
    }

    int height(Sampler sampler, int i, int j) {
        int index = i * this.size + j;
        int value = this.heights[index];
        if (value == UNKNOWN) {
            value = sampler.generator().getBaseHeight(pointX(i), pointZ(j), Heightmap.Types.OCEAN_FLOOR_WG,
                    sampler.heightAccessor(), sampler.randomState());
            this.heights[index] = value;
        }
        return value;
    }

    byte biome(Sampler sampler, int i, int j, ResourceKey<Biome> ocean, ResourceKey<Biome> spikes) {
        int index = i * this.size + j;
        byte value = this.biomes[index];
        if (value == 0) {
            Holder<Biome> biome = sampler.generator().getBiomeSource().getNoiseBiome(
                    QuartPos.fromBlock(pointX(i)), QuartPos.fromBlock(this.biomeY), QuartPos.fromBlock(pointZ(j)),
                    sampler.randomState().sampler());
            value = biome.is(ocean) ? OCEAN : biome.is(spikes) ? SPIKES : OTHER;
            this.biomes[index] = value;
        }
        return value;
    }

    void prefetch(Sampler sampler, int centerX, int centerZ, int radius) {
        int i0 = Math.max(0, indexX(centerX - radius) - 1);
        int i1 = Math.min(this.size - 1, indexX(centerX + radius) + 1);
        int j0 = Math.max(0, indexZ(centerZ - radius) - 1);
        int j1 = Math.min(this.size - 1, indexZ(centerZ + radius) + 1);
        for (int i = i0; i <= i1; i++) {
            for (int j = j0; j <= j1; j++) {
                height(sampler, i, j);
            }
        }
    }

    int at(double x, double z) {
        double fx = Mth.clamp((x - this.originX) / STEP, 0.0D, this.size - 1.0D);
        double fz = Mth.clamp((z - this.originZ) / STEP, 0.0D, this.size - 1.0D);
        int ix = Math.min((int) fx, this.size - 2);
        int iz = Math.min((int) fz, this.size - 2);
        double tx = fx - ix;
        double tz = fz - iz;
        double value = Mth.lerp2(tx, tz,
                known(ix, iz), known(ix + 1, iz),
                known(ix, iz + 1), known(ix + 1, iz + 1));
        return Mth.floor(value + 0.5D);
    }

    private int known(int i, int j) {
        int value = this.heights[i * this.size + j];
        return value == UNKNOWN ? this.biomeY - 20 : value;
    }
}
