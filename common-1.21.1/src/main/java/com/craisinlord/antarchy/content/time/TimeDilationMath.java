package com.craisinlord.antarchy.content.time;

public final class TimeDilationMath {
    public static final double MIN_RATE = 0.05D;
    public static final double NORMAL_RATE = 1.0D;
    private static final double GAUSSIAN_EDGE_EXPONENT = -4.5D;

    private TimeDilationMath() {
    }

    public static double clampRate(double rate) {
        if (Double.isNaN(rate) || Double.isInfinite(rate)) {
            return NORMAL_RATE;
        }
        return Math.max(MIN_RATE, Math.min(NORMAL_RATE, rate));
    }

    public static double gaussianFalloff(double distance, double radius) {
        if (distance < 0.0D || distance >= radius || radius <= 0.0D) {
            return 0.0D;
        }
        double normalized = distance / radius;
        return Math.exp(GAUSSIAN_EDGE_EXPONENT * normalized * normalized);
    }

    public static double localFieldRate(double fieldRate, double falloff) {
        double clampedRate = clampRate(fieldRate);
        double clampedFalloff = Math.max(0.0D, Math.min(1.0D, falloff));
        if (clampedRate <= 0.0D) {
            return 0.0D;
        }
        return Math.pow(clampedRate, clampedFalloff);
    }
}
