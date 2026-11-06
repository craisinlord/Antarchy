package com.craisinlord.antarchy.content.client;

public final class ElythiaWind {
    private static final double TWO_PI = Math.PI * 2.0D;
    private static final double DAY_TICKS = 24000.0D;
    private static final double DIRECTION_PERIOD_DAYS = 4.0D;
    private static final double STRENGTH_PERIOD_DAYS = 3.0D;
    private static final int SAMPLES_PER_DAY = 24;

    private static long cachedDays;
    private static double cachedX;
    private static double cachedZ;

    private ElythiaWind() {
    }

    public static double angle(double ticks) {
        return TWO_PI * noise(ticks / DAY_TICKS / DIRECTION_PERIOD_DAYS, 0x57L);
    }

    public static double strength(double ticks) {
        return 0.8D + 0.4D * noise(ticks / DAY_TICKS / STRENGTH_PERIOD_DAYS, 0x58L);
    }

    public static double[] displacement(double ticks) {
        double clamped = Math.max(0.0D, ticks);
        long day = (long) Math.floor(clamped / DAY_TICKS);
        if (day < cachedDays) {
            cachedDays = 0L;
            cachedX = 0.0D;
            cachedZ = 0.0D;
        }
        while (cachedDays < day) {
            double[] full = integrate(cachedDays * DAY_TICKS, DAY_TICKS);
            cachedX += full[0];
            cachedZ += full[1];
            cachedDays++;
        }
        double[] partial = integrate(day * DAY_TICKS, clamped - day * DAY_TICKS);
        return new double[]{cachedX + partial[0], cachedZ + partial[1]};
    }

    private static double[] integrate(double start, double length) {
        if (length <= 0.0D) {
            return new double[]{0.0D, 0.0D};
        }
        int steps = Math.max(1, (int) Math.ceil(SAMPLES_PER_DAY * length / DAY_TICKS));
        double step = length / steps;
        double x = 0.0D;
        double z = 0.0D;
        for (int i = 0; i < steps; i++) {
            double t = start + (i + 0.5D) * step;
            double a = angle(t);
            double s = strength(t);
            x += Math.cos(a) * s * step;
            z += Math.sin(a) * s * step;
        }
        return new double[]{x, z};
    }

    private static double noise(double x, long salt) {
        long cell = (long) Math.floor(x);
        double f = x - cell;
        double smooth = f * f * (3.0D - 2.0D * f);
        double a = hash01(cell, salt);
        double b = hash01(cell + 1L, salt);
        return a + (b - a) * smooth;
    }

    private static double hash01(long value, long salt) {
        long h = value * 0x9E3779B97F4A7C15L ^ salt * 0xC2B2AE3D27D4EB4FL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 11) * 0x1.0p-53;
    }
}
