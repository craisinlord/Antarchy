package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class CavarynCavityUnionFunction implements DensityFunction {
    public static final MapCodec<CavarynCavityUnionFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ExtraCodecs.nonEmptyList(DensityFunction.HOLDER_HELPER_CODEC.listOf()).fieldOf("arguments").forGetter(CavarynCavityUnionFunction::arguments)
            ).apply(instance, CavarynCavityUnionFunction::new)
    );
    public static final KeyDispatchDataCodec<CavarynCavityUnionFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private final List<DensityFunction> arguments;
    private final DensityFunction[] array;
    private final double minValue;
    private final double maxValue;

    public CavarynCavityUnionFunction(List<DensityFunction> arguments) {
        this.arguments = List.copyOf(arguments);
        this.array = this.arguments.toArray(new DensityFunction[0]);
        double min = Double.MAX_VALUE;
        double max = Double.MAX_VALUE;
        for (DensityFunction argument : this.array) {
            min = Math.min(min, argument.minValue());
            max = Math.min(max, argument.maxValue());
        }
        this.minValue = min;
        this.maxValue = max;
    }

    public List<DensityFunction> arguments() {
        return this.arguments;
    }

    @Override
    public double compute(FunctionContext context) {
        double floor = this.minValue;
        double best = Double.MAX_VALUE;
        for (DensityFunction argument : this.array) {
            double value = argument.compute(context);
            if (value < best) {
                best = value;
                if (best <= floor) {
                    return best;
                }
            }
        }
        return best;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        DensityFunction[] mapped = new DensityFunction[this.array.length];
        for (int i = 0; i < mapped.length; i++) {
            mapped[i] = this.array[i].mapAll(visitor);
        }
        return visitor.apply(new CavarynCavityUnionFunction(List.of(mapped)));
    }

    @Override
    public double minValue() {
        return this.minValue;
    }

    @Override
    public double maxValue() {
        return this.maxValue;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
