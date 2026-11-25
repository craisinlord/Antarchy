package com.craisinlord.antarchy.content.worldgen.elythia;

public final class ElythiaRegionNoise {
    private static final int BROAD_SCALE = 84;
    private static final int DETAIL_SCALE = 31;
    private static final int DETAIL_OFFSET_X = 197;
    private static final int DETAIL_OFFSET_Z = -113;
    private static final double BROAD_WEIGHT = 0.78D;

    private ElythiaRegionNoise() {
    }

    public static double sample(long salt, int quartX, int quartZ) {
        double broad = valueNoise(salt, quartX, quartZ, BROAD_SCALE);
        double detail = valueNoise(salt, quartX + DETAIL_OFFSET_X, quartZ + DETAIL_OFFSET_Z, DETAIL_SCALE);
        return broad * BROAD_WEIGHT + detail * (1.0D - BROAD_WEIGHT);
    }

    public static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double valueNoise(long salt, int x, int z, int scale) {
        int cellX = Math.floorDiv(x, scale);
        int cellZ = Math.floorDiv(z, scale);
        double fracX = smoothstep(Math.floorMod(x, scale) / (double) scale);
        double fracZ = smoothstep(Math.floorMod(z, scale) / (double) scale);
        double near = lerp(fracX, lattice(salt, cellX, cellZ), lattice(salt, cellX + 1, cellZ));
        double far = lerp(fracX, lattice(salt, cellX, cellZ + 1), lattice(salt, cellX + 1, cellZ + 1));
        return lerp(fracZ, near, far);
    }

    private static double lattice(long salt, int x, int z) {
        long hash = mix(salt ^ (x * 341873128712L + z * 132897987541L));
        return (hash >>> 11) * 0x1.0p-53D;
    }

    private static double smoothstep(double value) {
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerp(double delta, double start, double end) {
        return start + (end - start) * delta;
    }
}
