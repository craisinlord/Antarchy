package com.craisinlord.antarchy.content.time;

public final class TimeDilationFieldSnapshotSampler {
    private TimeDilationFieldSnapshotSampler() {
    }

    public static double sample(Iterable<TimeDilationFieldSnapshot> fields, double x, double y, double z) {
        double combinedRate = TimeDilationMath.NORMAL_RATE;
        for (TimeDilationFieldSnapshot field : fields) {
            double dx = field.x() - x;
            double dy = field.y() - y;
            double dz = field.z() - z;
            double distanceSqr = dx * dx + dy * dy + dz * dz;
            if (distanceSqr >= field.radiusSquared()) {
                continue;
            }
            double falloff = TimeDilationMath.gaussianFalloff(Math.sqrt(distanceSqr), field.radius());
            combinedRate *= TimeDilationMath.localFieldRate(field.effectiveRate(), falloff);
        }
        return TimeDilationMath.clampRate(combinedRate);
    }
}
