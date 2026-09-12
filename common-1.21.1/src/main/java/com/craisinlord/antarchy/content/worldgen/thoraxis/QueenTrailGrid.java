package com.craisinlord.antarchy.content.worldgen.thoraxis;

import net.minecraft.util.RandomSource;

public final class QueenTrailGrid {
    public static final int SPACING = 1_536;
    private static final int JITTER_MARGIN = 256;

    private QueenTrailGrid() {
    }

    public static Site site(long worldSeed, int cellX, int cellZ) {
        long seed = mix(worldSeed ^ 0x51A7E4D39B20C16DL, cellX, cellZ);
        RandomSource random = RandomSource.create(seed);
        int usable = SPACING - JITTER_MARGIN * 2;
        int x = cellX * SPACING + JITTER_MARGIN + random.nextInt(usable);
        int z = cellZ * SPACING + JITTER_MARGIN + random.nextInt(usable);
        double angle = random.nextDouble() * Math.PI * 2.0D;
        int footprints = 16 + random.nextInt(7);
        int stride = 6 + random.nextInt(2);
        return new Site(seed, x, z, Math.cos(angle), Math.sin(angle), footprints, stride);
    }

    private static long mix(long seed, int x, int z) {
        long value = seed ^ (long) x * 0x9E3779B97F4A7C15L ^ (long) z * 0xC2B2AE3D27D4EB4FL;
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public record Site(long id, int terminalX, int terminalZ, double directionX, double directionZ, int footprints, int stride) {
        public int length() {
            return this.footprints * this.stride + 18;
        }
    }
}
