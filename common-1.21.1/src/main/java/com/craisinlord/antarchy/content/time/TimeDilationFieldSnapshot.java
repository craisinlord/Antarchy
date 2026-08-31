package com.craisinlord.antarchy.content.time;

public record TimeDilationFieldSnapshot(
        double x,
        double y,
        double z,
        double radius,
        double rate,
        int age,
        int durationTicks
) {
    public double radiusSquared() {
        return this.radius * this.radius;
    }

    public double effectiveRate() {
        return TimeDilationMath.effectiveFieldRate(this.rate, this.age, this.durationTicks);
    }
}
