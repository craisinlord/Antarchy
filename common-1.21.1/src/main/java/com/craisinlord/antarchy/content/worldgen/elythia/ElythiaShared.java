package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class ElythiaShared implements DensityFunction {
    public static final MapCodec<ElythiaShared> CODEC = DensityFunction.HOLDER_HELPER_CODEC
            .fieldOf("argument")
            .xmap(ElythiaShared::new, ElythiaShared::argument);
    public static final KeyDispatchDataCodec<ElythiaShared> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private final DensityFunction argument;
    @Nullable
    private final Object origin;
    private final Map<Visitor, WeakReference<DensityFunction>> mapped = Collections.synchronizedMap(new WeakHashMap<>());

    public ElythiaShared(DensityFunction argument) {
        this(argument, null);
    }

    private ElythiaShared(DensityFunction argument, @Nullable Object origin) {
        this.argument = argument;
        this.origin = origin;
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
        WeakReference<DensityFunction> reference = this.mapped.get(visitor);
        DensityFunction previous = reference == null ? null : reference.get();
        if (previous != null) {
            return previous;
        }
        Object mappedOrigin = this.origin != null ? this.origin : new Object();
        DensityFunction result = visitor.apply(new ElythiaShared(this.argument.mapAll(visitor), mappedOrigin));
        this.mapped.put(visitor, new WeakReference<>(result));
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
        return this == other || (this.origin != null && other instanceof ElythiaShared shared && shared.origin == this.origin);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this.origin != null ? this.origin : this);
    }
}
