package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class ElythiaColumnCache implements DensityFunction {
    public static final MapCodec<ElythiaColumnCache> CODEC = DensityFunction.HOLDER_HELPER_CODEC
            .fieldOf("argument")
            .xmap(ElythiaColumnCache::new, ElythiaColumnCache::argument);
    public static final KeyDispatchDataCodec<ElythiaColumnCache> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final int BITS = 5;
    private static final int MASK = (1 << BITS) - 1;
    private static final int SIZE = 1 << (BITS * 2);

    private final DensityFunction argument;
    private final boolean active;
    private volatile Entry[] entries;

    public ElythiaColumnCache(DensityFunction argument) {
        this(argument, false);
    }

    private ElythiaColumnCache(DensityFunction argument, boolean active) {
        this.argument = argument;
        this.active = active;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    @Override
    public double compute(FunctionContext context) {
        if (!this.active) {
            return this.argument.compute(context);
        }
        int x = context.blockX();
        int z = context.blockZ();
        long key = ChunkPos.pack(x, z);
        Entry[] table = this.entries;
        if (table == null) {
            table = new Entry[SIZE];
            this.entries = table;
        }
        int index = (x & MASK) << BITS | (z & MASK);
        Entry entry = table[index];
        if (entry != null && entry.key() == key) {
            return entry.value();
        }
        double value = this.argument.compute(context);
        table[index] = new Entry(key, value);
        return value;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaColumnCache(this.argument.mapAll(visitor), true));
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
