package com.craisinlord.antarchy.content.worldgen.elythia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ElythiaSlopeField {
    private static final int HEIGHT_GRID = 4;
    private static final int GRID_POINTS = 16 / HEIGHT_GRID + 1;
    private static final int PATCH_GRID = 16;

    private ElythiaSlopeField() {
    }

    @FunctionalInterface
    public interface Heights {
        int at(int x, int z);
    }

    public record Column(int x, int z, int height, int slope, int rise) {
        public boolean outcrop() {
            return rise > 0;
        }
    }

    public static List<Column> chunk(long seed, int chunkX, int chunkZ, Heights heights) {
        int startX = chunkX * 16;
        int startZ = chunkZ * 16;
        int[] cache = new int[GRID_POINTS * GRID_POINTS];
        Arrays.fill(cache, Integer.MIN_VALUE);
        Heights cached = (x, z) -> {
            int index = (x - startX) / HEIGHT_GRID * GRID_POINTS + (z - startZ) / HEIGHT_GRID;
            if (cache[index] == Integer.MIN_VALUE) {
                cache[index] = heights.at(x, z);
            }
            return cache[index];
        };
        List<Column> columns = new ArrayList<>();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                Column column = sample(seed, x, z, cached);
                if (column != null) {
                    columns.add(column);
                }
            }
        }
        return List.copyOf(columns);
    }

    public static Column sample(long seed, int x, int z, Heights heights) {
        int gridX = Math.floorDiv(x, HEIGHT_GRID) * HEIGHT_GRID;
        int gridZ = Math.floorDiv(z, HEIGHT_GRID) * HEIGHT_GRID;
        int h00 = heights.at(gridX, gridZ);
        int h10 = heights.at(gridX + HEIGHT_GRID, gridZ);
        int h01 = heights.at(gridX, gridZ + HEIGHT_GRID);
        int h11 = heights.at(gridX + HEIGHT_GRID, gridZ + HEIGHT_GRID);
        int slope = Math.max(Math.max(h00, h10), Math.max(h01, h11)) - Math.min(Math.min(h00, h10), Math.min(h01, h11));
        if (slope < 3) {
            return null;
        }
        double tx = (x - gridX) / (double) HEIGHT_GRID;
        double tz = (z - gridZ) / (double) HEIGHT_GRID;
        int height = (int) Math.round((h00 * (1 - tx) + h10 * tx) * (1 - tz) + (h01 * (1 - tx) + h11 * tx) * tz);
        double patch = patch(seed, x, z);
        long grain = hash(seed ^ 0x32D1A09BL, x, z);
        if (patch >= 1 || unit(grain) > 0.66D) {
            return null;
        }
        int rise = slope >= 6 && patch < 0.45D ? (unit(mix(grain + 1)) < 0.2D ? 2 : 1) : 0;
        return new Column(x, z, height, slope, rise);
    }

    private static double patch(long seed, int x, int z) {
        int cellX = Math.floorDiv(x, PATCH_GRID);
        int cellZ = Math.floorDiv(z, PATCH_GRID);
        double best = Double.POSITIVE_INFINITY;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long key = hash(seed ^ 0x57A3C821L, cellX + dx, cellZ + dz);
                if (unit(key) > 0.7D) {
                    continue;
                }
                double centerX = (cellX + dx) * PATCH_GRID + unit(mix(key + 1)) * PATCH_GRID;
                double centerZ = (cellZ + dz) * PATCH_GRID + unit(mix(key + 2)) * PATCH_GRID;
                double radius = 4 + unit(mix(key + 3)) * 5;
                double distanceX = (x - centerX) / radius;
                double distanceZ = (z - centerZ) / radius;
                best = Math.min(best, distanceX * distanceX + distanceZ * distanceZ);
            }
        }
        return best;
    }

    private static long hash(long seed, int x, int z) {
        return mix(seed ^ x * 0x632BE59BD9B4E019L ^ z * 0x85157AF5D66D3EABL);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }
}
