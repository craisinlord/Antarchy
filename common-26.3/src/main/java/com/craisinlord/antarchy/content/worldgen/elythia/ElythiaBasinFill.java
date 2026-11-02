package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaBasinFill(DensityFunction argument) implements DensityFunction {
    public static final MapCodec<ElythiaBasinFill> CODEC = DensityFunction.CODEC
            .fieldOf("argument")
            .xmap(ElythiaBasinFill::new, ElythiaBasinFill::argument);
    private static final double CEILING = -0.28D;
    private static final double DEEP = -0.7D;
    private static final double RIM_LOW = -0.43D;
    private static final double RIM_HIGH = -0.39D;
    private static final double OPEN_LOW = -0.45D;
    private static final double OPEN_HIGH = -0.39D;
    private static final double SOFTNESS = 0.02D;
    private static final int[] RADII = {48, 96, 144, 192};
    private static final double[][] DIRECTIONS = directions(8);
    private static final int GRID_SHIFT = 4;
    private static final int GRID = 1 << GRID_SHIFT;
    private static final int CACHE_SLOTS = 256;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return ElythiaField.sampler(new Runtime(ElythiaField.bind(this.argument, context)));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaBasinFill(rule.rewrite(this.argument));
    }

    @Override
    public Interval range() {
        return this.argument.range();
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaBasinFill> codec() { return CODEC; }

    private static double spline(double t, int index) {
        return switch (index) {
            case 0 -> 0.5D * (1.0D - t) * (1.0D - t);
            case 1 -> 0.5D + t - t * t;
            default -> 0.5D * t * t;
        };
    }

    private static double lift(double gap) {
        if (gap <= -SOFTNESS) {
            return 0.0D;
        }
        if (gap >= SOFTNESS) {
            return gap;
        }
        double shifted = gap + SOFTNESS;
        return shifted * shifted / (4.0D * SOFTNESS);
    }

    private static double smooth(double t) {
        double clamped = Mth.clamp(t, 0.0D, 1.0D);
        return clamped * clamped * (3.0D - 2.0D * clamped);
    }

    private static double[][] directions(int count) {
        double[][] directions = new double[count][];
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            directions[i] = new double[]{Math.cos(angle), Math.sin(angle)};
        }
        return directions;
    }

    private static final class Runtime implements ElythiaField {
        private final ElythiaField argument;
        private final ThreadLocal<FloorCache> floors = ThreadLocal.withInitial(FloorCache::new);

        private Runtime(ElythiaField argument) {
            this.argument = argument;
        }

        public double compute(ElythiaField.Point context) {
            double value = this.argument.compute(context);
            if (value >= CEILING + SOFTNESS || value < DEEP) {
                return value;
            }
            double u = (double) context.blockX() / GRID - 0.5D;
            double v = (double) context.blockZ() / GRID - 0.5D;
            int cellX = Mth.floor(u);
            int cellZ = Mth.floor(v);
            double tx = u - cellX;
            double tz = v - cellZ;
            FloorCache cache = this.floors.get();
            double rim = 0.0D;
            double target = 0.0D;
            double open = 0.0D;
            for (int i = 0; i < 3; i++) {
                double weightX = spline(tx, i);
                for (int j = 0; j < 3; j++) {
                    double weight = weightX * spline(tz, j);
                    int slot = this.point(cache, cellX + i, cellZ + j);
                    rim += weight * cache.rims[slot];
                    target += weight * cache.targets[slot];
                    open += weight * cache.opens[slot];
                }
            }
            double enclosed = value + rim * lift(target - value);
            double inland = value + open * lift(CEILING - value);
            return Math.max(enclosed, inland);
        }

        private int point(FloorCache cache, int cellX, int cellZ) {
            long key = (long) cellX << 32 | (cellZ & 0xFFFFFFFFL);
            int slot = (int) ((cellX * 31L + cellZ) & (CACHE_SLOTS - 1));
            if (cache.filled[slot] && cache.keys[slot] == key) {
                return slot;
            }
            this.computePoint(cache, slot, cellX << GRID_SHIFT, cellZ << GRID_SHIFT);
            cache.keys[slot] = key;
            cache.filled[slot] = true;
            return slot;
        }

        private void computePoint(FloorCache cache, int slot, int x, int z) {
            double fill = Double.NEGATIVE_INFINITY;
            double lowest = Double.POSITIVE_INFINITY;
            for (int radius : RADII) {
                double ring = Double.POSITIVE_INFINITY;
                for (double[] direction : DIRECTIONS) {
                    int sampleX = x + (int) Math.round(direction[0] * radius);
                    int sampleZ = z + (int) Math.round(direction[1] * radius);
                    ring = Math.min(ring, this.argument.compute(new ElythiaField.Point(sampleX, 0, sampleZ)));
                }
                fill = Math.max(fill, ring);
                lowest = Math.min(lowest, ring);
                if (fill >= CEILING) {
                    cache.rims[slot] = 1.0D;
                    cache.targets[slot] = CEILING;
                    cache.opens[slot] = 0.0D;
                    return;
                }
            }
            cache.rims[slot] = smooth((fill - RIM_LOW) / (RIM_HIGH - RIM_LOW));
            cache.targets[slot] = Math.min(fill, CEILING);
            cache.opens[slot] = smooth((lowest - OPEN_LOW) / (OPEN_HIGH - OPEN_LOW));
        }
    }

    private static final class FloorCache {
        private final long[] keys = new long[CACHE_SLOTS];
        private final double[] rims = new double[CACHE_SLOTS];
        private final double[] targets = new double[CACHE_SLOTS];
        private final double[] opens = new double[CACHE_SLOTS];
        private final boolean[] filled = new boolean[CACHE_SLOTS];
    }
}
