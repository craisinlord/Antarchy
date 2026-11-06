package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ElythiaRiverSpan(
        DensityFunction argument,
        double scale
) implements DensityFunction {

    public static final MapCodec<ElythiaRiverSpan> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaRiverSpan::argument),
                    Codec.DOUBLE.fieldOf("scale").forGetter(ElythiaRiverSpan::scale)
            ).apply(instance, ElythiaRiverSpan::new)
    );

    public static final KeyDispatchDataCodec<ElythiaRiverSpan> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final int STEP = 4;
    private static final double MIN_GRADIENT = 1.0E-9D;
    private static final double MAX_VALUE = 1000.0D;

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        double value = this.sample(x, z);
        double gradientX = (this.sample(x + STEP, z) - this.sample(x - STEP, z)) / (2.0D * STEP);
        double gradientZ = (this.sample(x, z + STEP) - this.sample(x, z - STEP)) / (2.0D * STEP);
        double gradient = Math.sqrt(gradientX * gradientX + gradientZ * gradientZ);
        if (gradient < MIN_GRADIENT) {
            return MAX_VALUE;
        }
        return Math.min(MAX_VALUE, Math.abs(value) / gradient * this.scale);
    }

    private double sample(int x, int z) {
        return this.argument.compute(new DensityFunction.SinglePointContext(x, 0, z));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaRiverSpan(this.argument.mapAll(visitor), this.scale));
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return MAX_VALUE;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
