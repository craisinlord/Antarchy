package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class ElythiaShared implements DensityFunction {
    public static final MapCodec<ElythiaShared> CODEC = DensityFunction.HOLDER_HELPER_CODEC
            .fieldOf("argument")
            .xmap(ElythiaShared::new, ElythiaShared::argument);
    public static final KeyDispatchDataCodec<ElythiaShared> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private final DensityFunction argument;
    private final Map<Visitor, DensityFunction> mapped = Collections.synchronizedMap(new WeakHashMap<>());

    public ElythiaShared(DensityFunction argument) {
        this.argument = argument;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    @Override
    public double compute(FunctionContext context) {
        return this.argument.compute(context);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        this.argument.fillArray(values, contextProvider);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        DensityFunction previous = this.mapped.get(visitor);
        if (previous != null) {
            return previous;
        }
        DensityFunction result = visitor.apply(new ElythiaShared(this.argument.mapAll(visitor)));
        this.mapped.put(visitor, result);
        return result;
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
}
