package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class ElythiaColumnCache implements DensityFunction {
    public static final MapCodec<ElythiaColumnCache> CODEC = DensityFunction.HOLDER_HELPER_CODEC
            .fieldOf("argument")
            .xmap(ElythiaColumnCache::new, ElythiaColumnCache::argument);
    public static final KeyDispatchDataCodec<ElythiaColumnCache> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final int BITS = 6;
    private static final int MASK = (1 << BITS) - 1;
    private static final int SIZE = 1 << (BITS * 2);

    private final DensityFunction argument;
    @Nullable
    private final Entry[] entries;

    public ElythiaColumnCache(DensityFunction argument) {
        this(argument, null);
    }

    private ElythiaColumnCache(DensityFunction argument, @Nullable Entry[] entries) {
        this.argument = argument;
        this.entries = entries;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    @Override
    public double compute(FunctionContext context) {
        Entry[] table = this.entries;
        if (table == null) {
            return this.argument.compute(context);
        }
        int x = context.blockX();
        int z = context.blockZ();
        long key = ChunkPos.asLong(x, z);
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
        Entry[] table = this.entries != null ? this.entries : new Entry[SIZE];
        return visitor.apply(new ElythiaColumnCache(this.argument.mapAll(visitor), table));
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
        return this == other || (this.entries != null && other instanceof ElythiaColumnCache cache && cache.entries == this.entries);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.entries != null ? this.entries : this);
    }

    private record Entry(long key, double value) {
    }
}
