package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ElythiaRiverCarveFunction(
        DensityFunction erosion,
        DensityFunction ridges
) implements DensityFunction {

    public static final MapCodec<ElythiaRiverCarveFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("erosion").forGetter(ElythiaRiverCarveFunction::erosion),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("ridges").forGetter(ElythiaRiverCarveFunction::ridges)
            ).apply(instance, ElythiaRiverCarveFunction::new)
    );

    public static final KeyDispatchDataCodec<ElythiaRiverCarveFunction> KEY_CODEC =
            KeyDispatchDataCodec.of(CODEC);

    private static final double EROSION_THRESHOLD = 0.70;
    private static final double CHANNEL_HALF_WIDTH = 0.08;
    private static final double CHANNEL_DEPTH = 0.35;
    private static final double BASE_EROSION_FACTOR = -0.06;

    @Override
    public double compute(FunctionContext context) {
        double erosionVal = erosion.compute(context);
        double base = BASE_EROSION_FACTOR * erosionVal;

        if (erosionVal > EROSION_THRESHOLD) {
            double ridgesVal = ridges.compute(context);
            double dist = Math.abs(ridgesVal);
            if (dist < CHANNEL_HALF_WIDTH) {
                double t = 1.0 - (dist / CHANNEL_HALF_WIDTH);
                double channelness = t * t;
                return base - channelness * CHANNEL_DEPTH;
            }
        }

        return base;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public double minValue() {
        return BASE_EROSION_FACTOR - CHANNEL_DEPTH;
    }

    @Override
    public double maxValue() {
        return -BASE_EROSION_FACTOR;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaRiverCarveFunction(erosion.mapAll(visitor), ridges.mapAll(visitor)));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
