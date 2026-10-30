package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaColumnCache(DensityFunction argument) implements DensityFunction {
    public static final MapCodec<ElythiaColumnCache> CODEC = DensityFunction.CODEC
            .fieldOf("argument")
            .xmap(ElythiaColumnCache::new, ElythiaColumnCache::argument);
    private static final int BITS = 5;
    private static final int MASK = (1 << BITS) - 1;
    private static final int SIZE = 1 << (BITS * 2);

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        return ElythiaField.sampler(new Runtime(argument, true));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaColumnCache(rule.rewrite(this.argument));
    }

    @Override
    public Interval range() {
        return Interval.of((float) (this.argument.range().min()), (float) (this.argument.range().max()));
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaColumnCache> codec() { return CODEC; }

    @Override
    public boolean equals(Object other) { return this == other; }

    @Override
    public int hashCode() { return System.identityHashCode(this); }



    private static final class Runtime implements ElythiaField {
        private final ElythiaField argument;
        private final boolean active;
        private volatile Entry[] entries;

        private Runtime(ElythiaField argument, boolean active) {
            this.argument = argument;
            this.active = active;
        }

        public double compute(ElythiaField.Point context) {
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

        private record Entry(long key, double value) {
        }
    }
}
