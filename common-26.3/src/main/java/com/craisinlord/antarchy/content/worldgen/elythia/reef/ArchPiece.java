package com.craisinlord.antarchy.content.worldgen.elythia.reef;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

record ArchPiece(
        int centerX,
        int centerZ,
        int radius,
        double startX,
        double startY,
        double startZ,
        double endX,
        double endY,
        double endZ,
        double height,
        double perpX,
        double perpZ,
        double sway,
        double startLeg,
        double endLeg,
        double crestRadius,
        double skew,
        Wave side,
        Wave rise,
        Wave lumps,
        Wave patches,
        double breakStart,
        double breakEnd,
        ReefCoral primary,
        ReefCoral secondary
) implements ReefPiece {

    private static final int CEILING = 6;
    private static final double FLARE = 0.12D;

    record Wave(double amplitude, double frequencyA, double phaseA, double frequencyB, double phaseB) {

        static Wave random(RandomSource random, double amplitude, double minFrequency, double frequencyRange) {
            return new Wave(amplitude,
                    minFrequency + random.nextDouble() * frequencyRange, random.nextDouble() * Math.PI * 2.0D,
                    minFrequency * 2.1D + random.nextDouble() * frequencyRange * 2.0D, random.nextDouble() * Math.PI * 2.0D);
        }

        double at(double t) {
            return this.amplitude * (0.65D * Math.sin(t * this.frequencyA * Math.PI * 2.0D + this.phaseA)
                    + 0.35D * Math.sin(t * this.frequencyB * Math.PI * 2.0D + this.phaseB));
        }
    }

    static ArchPiece between(ReefSite site, double startX, double startZ, double endX, double endZ,
                             double height, double sway, double leg, double crest,
                             ReefCoral primary, ReefCoral secondary, boolean mayBreak) {
        RandomSource random = site.random();
        double dx = endX - startX;
        double dz = endZ - startZ;
        double length = Math.max(1.0D, Math.sqrt(dx * dx + dz * dz));
        double perpX = -dz / length;
        double perpZ = dx / length;
        double startY = site.grid().at(startX, startZ) - 1;
        double endY = site.grid().at(endX, endZ) - 1;
        double ceiling = site.seaLevel() + CEILING - Math.max(startY, endY);
        double varied = height * (0.8D + random.nextDouble() * 0.4D);
        double clamped = Math.max(8.0D, Math.min(varied, ceiling));
        double startLeg = leg * (0.8D + random.nextDouble() * 0.45D);
        double endLeg = leg * (0.8D + random.nextDouble() * 0.45D);
        double crestRadius = crest * (0.8D + random.nextDouble() * 0.4D);
        double skew = (random.nextDouble() * 2.0D - 1.0D) * 0.35D;
        double wander = 1.0D + length * 0.04D;
        Wave side = Wave.random(random, wander, 0.6D, 0.9D);
        Wave rise = Wave.random(random, wander * 0.7D, 0.7D, 0.9D);
        Wave lumps = Wave.random(random, 0.22D, 2.0D, 3.0D);
        Wave patches = Wave.random(random, 1.0D, 0.8D, 1.6D);
        double breakStart = 2.0D;
        double breakEnd = 2.0D;
        if (mayBreak && random.nextFloat() < 0.14F) {
            breakStart = 0.3D + random.nextDouble() * 0.3D;
            breakEnd = random.nextBoolean() ? 2.0D : breakStart + 0.15D + random.nextDouble() * 0.25D;
        }
        double reach = Math.abs(sway) + side.amplitude() + Math.max(startLeg, endLeg) * 1.5D * (1.0D + lumps.amplitude());
        int centerX = Mth.floor((startX + endX) / 2.0D + perpX * sway * 0.5D);
        int centerZ = Mth.floor((startZ + endZ) / 2.0D + perpZ * sway * 0.5D);
        int radius = Mth.ceil(length / 2.0D + reach + 3.0D);
        return new ArchPiece(centerX, centerZ, radius, startX, startY, startZ, endX, endY, endZ, clamped,
                perpX, perpZ, sway, startLeg, endLeg, crestRadius, skew, side, rise, lumps, patches,
                breakStart, breakEnd, primary, secondary);
    }

    @Override
    public boolean covers(int x, int z, double margin) {
        double midX = (this.startX + this.endX) / 2.0D + this.perpX * this.sway;
        double midZ = (this.startZ + this.endZ) / 2.0D + this.perpZ * this.sway;
        double reach = Math.max(this.startLeg, this.endLeg) * 1.5D * (1.0D + this.lumps.amplitude())
                + this.side.amplitude() + margin;
        return ReefPiece.segmentDistance(x, z, this.startX, this.startZ, midX, midZ) <= reach
                || ReefPiece.segmentDistance(x, z, midX, midZ, this.endX, this.endZ) <= reach;
    }

    @Override
    public void render(ReefCanvas canvas) {
        double length = Math.sqrt((this.endX - this.startX) * (this.endX - this.startX)
                + (this.endZ - this.startZ) * (this.endZ - this.startZ));
        int samples = Mth.ceil((length + this.height * 2.0D) * 2.0D);
        double lastX = Double.NaN;
        double lastY = 0.0D;
        double lastZ = 0.0D;
        ReefCoral lastCoral = null;
        for (int i = 0; i <= samples; i++) {
            double t = (double) i / samples;
            double taper = breakTaper(t);
            if (taper <= 0.0D) {
                lastX = Double.NaN;
                continue;
            }
            double warped = t + this.skew * t * (1.0D - t);
            double lift = Math.sin(Math.PI * warped);
            double envelope = Math.sin(Math.PI * t);
            double offset = this.sway * lift + this.side.at(t) * envelope;
            double x = Mth.lerp(t, this.startX, this.endX) + this.perpX * offset;
            double z = Mth.lerp(t, this.startZ, this.endZ) + this.perpZ * offset;
            double leg = Mth.lerp(t, this.startLeg, this.endLeg);
            double r = Mth.lerp(lift, leg, this.crestRadius) * (1.0D + this.lumps.at(t)) * flare(t) * taper;
            if (r < 0.6D || !canvas.intersects(x, z, r + 1.0D)) {
                lastX = Double.NaN;
                continue;
            }
            double y = Mth.lerp(t, this.startY, this.endY) + this.height * lift + this.rise.at(t) * envelope;
            ReefCoral coral = this.patches.at(t) > 0.3D ? this.secondary : this.primary;
            if (coral == lastCoral && i < samples && !Double.isNaN(lastX)) {
                double dx = x - lastX;
                double dy = y - lastY;
                double dz = z - lastZ;
                double spacing = Math.max(0.5D, r * 0.45D);
                if (dx * dx + dy * dy + dz * dz < spacing * spacing) {
                    continue;
                }
            }
            canvas.blob(x, y, z, r, coral, t < 0.12D || t > 0.88D);
            lastX = x;
            lastY = y;
            lastZ = z;
            lastCoral = coral;
        }
    }

    private static double flare(double t) {
        double edge = Math.min(t, 1.0D - t);
        return edge < FLARE ? 1.0D + 0.5D * (1.0D - edge / FLARE) : 1.0D;
    }

    private double breakTaper(double t) {
        if (this.breakStart > 1.0D) {
            return 1.0D;
        }
        if (t > this.breakStart && t < this.breakEnd) {
            return 0.0D;
        }
        double distance = t <= this.breakStart ? this.breakStart - t : t - this.breakEnd;
        return Mth.clamp(0.45D + distance / 0.12D, 0.45D, 1.0D);
    }
}
