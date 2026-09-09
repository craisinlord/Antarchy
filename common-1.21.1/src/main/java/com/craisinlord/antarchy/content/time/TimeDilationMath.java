package com.craisinlord.antarchy.content.time;

public final class TimeDilationMath {
    public static final double MIN_RATE = 0.05D;
    public static final double NORMAL_RATE = 1.0D;
    public static final double MAX_RATE = 3.0D;
    public static final int FIELD_TRANSITION_TICKS = 20;
    public static final int EFFECT_TRANSITION_TICKS = 20;
    private static final double GAUSSIAN_EDGE_EXPONENT = -4.5D;

    private TimeDilationMath() {
    }

    public static double clampRate(double rate) {
        if (Double.isNaN(rate) || Double.isInfinite(rate)) {
            return NORMAL_RATE;
        }
        return Math.max(MIN_RATE, Math.min(MAX_RATE, rate));
    }

    /** Moves an entity's effective rate toward a new effect/field target without a hard step. */
    public static double transitionRate(double currentRate, double targetRate) {
        double current = clampRate(currentRate);
        double target = clampRate(targetRate);
        double maximumStep = (MAX_RATE - MIN_RATE) / EFFECT_TRANSITION_TICKS;
        double delta = Math.max(-maximumStep, Math.min(maximumStep, target - current));
        return clampRate(current + delta);
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

    public static double smoothStep(double value) {
        double clamped = Math.max(0.0D, Math.min(1.0D, value));
        return clamped * clamped * (3.0D - 2.0D * clamped);
    }

    public static double fieldStrength(int age, int durationTicks) {
        double fadeIn = smoothStep((double) age / FIELD_TRANSITION_TICKS);
        if (durationTicks < 0) {
            return fadeIn;
        }

        double remaining = durationTicks - age;
        double fadeOut = smoothStep(remaining / FIELD_TRANSITION_TICKS);
        return Math.min(fadeIn, fadeOut);
    }

    public static double effectiveFieldRate(double fieldRate, int age, int durationTicks) {
        double strength = fieldStrength(age, durationTicks);
        return NORMAL_RATE + (clampRate(fieldRate) - NORMAL_RATE) * strength;
    }
}
