package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ElythiaRiverCoastFade(
        DensityFunction argument,
        DensityFunction continents
) implements DensityFunction {

    public static final MapCodec<ElythiaRiverCoastFade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaRiverCoastFade::argument),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("continents").forGetter(ElythiaRiverCoastFade::continents)
            ).apply(instance, ElythiaRiverCoastFade::new)
    );

    public static final KeyDispatchDataCodec<ElythiaRiverCoastFade> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final int STEP = 24;
    private static final double MIN_GRADIENT = 1.0E-12D;
    private static final double MIN_ALIGNMENT = 0.7D;
    private static final double MAX_ALIGNMENT = 0.9D;

    @Override
    public double compute(FunctionContext context) {
        int x = context.blockX();
        int z = context.blockZ();
        double riverX = sample(this.argument, x + STEP, z) - sample(this.argument, x - STEP, z);
        double riverZ = sample(this.argument, x, z + STEP) - sample(this.argument, x, z - STEP);
        double coastX = sample(this.continents, x + STEP, z) - sample(this.continents, x - STEP, z);
        double coastZ = sample(this.continents, x, z + STEP) - sample(this.continents, x, z - STEP);
        double river = riverX * riverX + riverZ * riverZ;
        double coast = coastX * coastX + coastZ * coastZ;
        if (river < MIN_GRADIENT || coast < MIN_GRADIENT) {
            return 0.0D;
        }
        double alignment = Math.abs(riverX * coastX + riverZ * coastZ) / Math.sqrt(river * coast);
        double t = Mth.clamp((alignment - MIN_ALIGNMENT) / (MAX_ALIGNMENT - MIN_ALIGNMENT), 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private static double sample(DensityFunction function, int x, int z) {
        return function.compute(new DensityFunction.SinglePointContext(x, 0, z));
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaRiverCoastFade(this.argument.mapAll(visitor), this.continents.mapAll(visitor)));
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return 1.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
