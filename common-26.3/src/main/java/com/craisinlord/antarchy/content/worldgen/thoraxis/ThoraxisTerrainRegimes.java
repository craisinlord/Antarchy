package com.craisinlord.antarchy.content.worldgen.thoraxis;

public final class ThoraxisTerrainRegimes {
    public static final int STRATA = 0;
    public static final int RIBCAGE = 1;
    public static final int MIRROR = 2;
    public static final int SPIRES = 3;
    public static final int COUNT = 4;

    private static final double SPLIT_A = 0.04D;
    private static final double SPLIT_B = 0.0D;
    private static final double BLEND_WIDTH = 0.14D;

    private ThoraxisTerrainRegimes() {
    }

    public static void weights(double regimeA, double regimeB, double[] out) {
        double a = blend(regimeA, SPLIT_A);
        double b = blend(regimeB, SPLIT_B);
        out[STRATA] = (1.0D - a) * (1.0D - b);
        out[RIBCAGE] = a * (1.0D - b);
        out[MIRROR] = (1.0D - a) * b;
        out[SPIRES] = a * b;
    }

    public static int dominant(double regimeA, double regimeB) {
        boolean a = regimeA >= SPLIT_A;
        boolean b = regimeB >= SPLIT_B;
        if (a) {
            return b ? SPIRES : RIBCAGE;
        }
        return b ? MIRROR : STRATA;
    }

    private static double blend(double value, double split) {
        double t = (value - split) / BLEND_WIDTH + 0.5D;
        if (t <= 0.0D) {
            return 0.0D;
        }
        if (t >= 1.0D) {
            return 1.0D;
        }
        return t * t * (3.0D - 2.0D * t);
    }

    static double hash01(long x, long z, long salt) {
        long value = salt + 0x9E3779B97F4A7C15L;
        value ^= x * 0x632BE59BD9B4E019L;
        value ^= z * 0xC6BC279692B5CC83L;
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53;
    }
}
