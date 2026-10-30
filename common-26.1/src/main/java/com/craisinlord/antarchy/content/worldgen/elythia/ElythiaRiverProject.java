package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class ElythiaRiverProject implements DensityFunction {

    public static final MapCodec<ElythiaRiverProject> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaRiverProject::argument),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("guide").forGetter(ElythiaRiverProject::guide)
            ).apply(instance, ElythiaRiverProject::new)
    );

    public static final KeyDispatchDataCodec<ElythiaRiverProject> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final int STEP = 4;
    private static final double MAX_SHIFT = 64.0D;
    private static final double MIN_GRADIENT = 1.0E-12D;
    private static final int SMOOTH_SAMPLES = 2;
    private static final double SMOOTH_SPACING = 12.0D;
    private static final int GRID_SHIFT = 2;
    private static final int GRID_SIZE = 1 << GRID_SHIFT;
    private static final int GRID_MASK = ~(GRID_SIZE - 1);
    private static final int SLOT_BITS = 5;
    private static final int SLOT_MASK = (1 << SLOT_BITS) - 1;
    private static final int SLOTS = 1 << (SLOT_BITS * 2);

    private final DensityFunction argument;
    private final DensityFunction guide;
    private final boolean active;
    private volatile Entry[] lattice;

    public ElythiaRiverProject(DensityFunction argument, DensityFunction guide) {
        this(argument, guide, false);
    }

    private ElythiaRiverProject(DensityFunction argument, DensityFunction guide, boolean active) {
        this.argument = argument;
        this.guide = guide;
        this.active = active;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    public DensityFunction guide() {
        return this.guide;
    }

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();
        if (!this.active) {
            return this.project(x, y, z, context);
        }
        int x0 = x & GRID_MASK;
        int z0 = z & GRID_MASK;
        double fx = (x - x0) / (double) GRID_SIZE;
        double fz = (z - z0) / (double) GRID_SIZE;
        double v00 = this.lattice(x0, y, z0);
        if (fx == 0.0D && fz == 0.0D) {
            return v00;
        }
        double v10 = this.lattice(x0 + GRID_SIZE, y, z0);
        double v01 = this.lattice(x0, y, z0 + GRID_SIZE);
        double v11 = this.lattice(x0 + GRID_SIZE, y, z0 + GRID_SIZE);
        double north = v00 + (v10 - v00) * fx;
        double south = v01 + (v11 - v01) * fx;
        return north + (south - north) * fz;
    }

    private double lattice(int x, int y, int z) {
        int gridX = x >> GRID_SHIFT;
        int gridZ = z >> GRID_SHIFT;
        long key = ChunkPos.pack(gridX, gridZ);
        Entry[] table = this.lattice;
        if (table == null) {
            table = new Entry[SLOTS];
            this.lattice = table;
        }
        int index = (gridX & SLOT_MASK) << SLOT_BITS | (gridZ & SLOT_MASK);
        Entry entry = table[index];
        if (entry != null && entry.key() == key) {
            return entry.value();
        }
        double value = this.project(x, y, z, null);
        table[index] = new Entry(key, value);
        return value;
    }

    private double project(int x, int y, int z, FunctionContext context) {
        double value = this.guide(x, z);
        double gradientX = (this.guide(x + STEP, z) - this.guide(x - STEP, z)) / (2.0D * STEP);
        double gradientZ = (this.guide(x, z + STEP) - this.guide(x, z - STEP)) / (2.0D * STEP);
        double gradient = gradientX * gradientX + gradientZ * gradientZ;
        if (gradient < MIN_GRADIENT) {
            return this.argument.compute(context != null ? context : new DensityFunction.SinglePointContext(x, y, z));
        }
        double shift = value / gradient;
        double distance = Math.abs(value) / Math.sqrt(gradient);
        if (distance > MAX_SHIFT) {
            shift *= MAX_SHIFT / distance;
        }
        double centerX = x - shift * gradientX;
        double centerZ = z - shift * gradientZ;
        double length = Math.sqrt(gradient);
        double tangentX = -gradientZ / length;
        double tangentZ = gradientX / length;
        double total = 0.0D;
        for (int i = -SMOOTH_SAMPLES; i <= SMOOTH_SAMPLES; i++) {
            double offset = i * SMOOTH_SPACING;
            int sampleX = (int) Math.round(centerX + tangentX * offset);
            int sampleZ = (int) Math.round(centerZ + tangentZ * offset);
            total += this.argument.compute(new DensityFunction.SinglePointContext(sampleX, y, sampleZ));
        }
        return total / (2 * SMOOTH_SAMPLES + 1);
    }

    private double guide(int x, int z) {
        return this.guide.compute(new DensityFunction.SinglePointContext(x, 0, z));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaRiverProject(this.argument.mapAll(visitor), this.guide.mapAll(visitor), true));
    }

    @Override
    public double minValue() {
        return this.argument.minValue();
    }

    @Override
    public double maxValue() {
        return this.argument.maxValue();
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    private record Entry(long key, double value) {
    }
}
