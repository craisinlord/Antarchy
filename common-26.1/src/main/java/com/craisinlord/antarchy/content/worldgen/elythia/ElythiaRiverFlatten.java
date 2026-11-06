package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ElythiaRiverFlatten(
        DensityFunction argument,
        DensityFunction gradientX,
        DensityFunction gradientZ,
        double minGradient
) implements DensityFunction {

    public static final MapCodec<ElythiaRiverFlatten> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaRiverFlatten::argument),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("gradient_x").forGetter(ElythiaRiverFlatten::gradientX),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("gradient_z").forGetter(ElythiaRiverFlatten::gradientZ),
                    Codec.DOUBLE.fieldOf("min_gradient").forGetter(ElythiaRiverFlatten::minGradient)
            ).apply(instance, ElythiaRiverFlatten::new)
    );

    public static final KeyDispatchDataCodec<ElythiaRiverFlatten> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double MAX_VALUE = 1000.0D;

    @Override
    public double compute(FunctionContext context) {
        double value = Math.abs(this.argument.compute(context));
        double gradientX = this.gradientX.compute(context);
        double gradientZ = this.gradientZ.compute(context);
        double gradient = Math.sqrt(gradientX * gradientX + gradientZ * gradientZ);
        if (gradient >= this.minGradient) {
            return value;
        }
        return Math.min(MAX_VALUE, value * this.minGradient / Math.max(gradient, this.minGradient / MAX_VALUE));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaRiverFlatten(
                this.argument.mapAll(visitor),
                this.gradientX.mapAll(visitor),
                this.gradientZ.mapAll(visitor),
                this.minGradient
        ));
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
