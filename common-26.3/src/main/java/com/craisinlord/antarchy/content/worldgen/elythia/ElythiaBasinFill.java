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
    private static final double NONE = -1.0D;
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
            if (value >= CEILING || value < DEEP) {
                return value;
            }
            int x = context.blockX();
            int z = context.blockZ();
            int cellX = x >> GRID_SHIFT;
            int cellZ = z >> GRID_SHIFT;
            double tx = (double) (x - (cellX << GRID_SHIFT)) / GRID;
            double tz = (double) (z - (cellZ << GRID_SHIFT)) / GRID;
            FloorCache cache = this.floors.get();
            double floor = Mth.lerp2(tx, tz,
                    this.floor(cache, cellX, cellZ),
                    this.floor(cache, cellX + 1, cellZ),
                    this.floor(cache, cellX, cellZ + 1),
                    this.floor(cache, cellX + 1, cellZ + 1));
            return Math.max(value, floor);
        }

        private double floor(FloorCache cache, int cellX, int cellZ) {
            long key = (long) cellX << 32 | (cellZ & 0xFFFFFFFFL);
            int slot = (int) ((cellX * 31L + cellZ) & (CACHE_SLOTS - 1));
            if (cache.filled[slot] && cache.keys[slot] == key) {
                return cache.values[slot];
            }
            double floor = this.computeFloor(cellX << GRID_SHIFT, cellZ << GRID_SHIFT);
            cache.keys[slot] = key;
            cache.values[slot] = floor;
            cache.filled[slot] = true;
            return floor;
        }

        private double computeFloor(int x, int z) {
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
                    return CEILING;
                }
            }
            double rim = smooth((fill - RIM_LOW) / (RIM_HIGH - RIM_LOW));
            double enclosedFloor = Mth.lerp(rim, NONE, Math.min(fill, CEILING));
            double open = smooth((lowest - OPEN_LOW) / (OPEN_HIGH - OPEN_LOW));
            double inlandFloor = Mth.lerp(open, NONE, CEILING);
            return Math.max(enclosedFloor, inlandFloor);
        }
    }

    private static final class FloorCache {
        private final long[] keys = new long[CACHE_SLOTS];
        private final double[] values = new double[CACHE_SLOTS];
        private final boolean[] filled = new boolean[CACHE_SLOTS];
    }
}
