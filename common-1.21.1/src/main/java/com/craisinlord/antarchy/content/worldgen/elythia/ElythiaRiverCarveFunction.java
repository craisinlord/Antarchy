package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * Custom density function that carves narrow river channels in Elythia.
 *
 * Logic:
 *  - Outside river-erosion territory (erosion <= EROSION_THRESHOLD): behaves identically
 *    to the original "-0.06 * erosion" term in elythia_surface.json.
 *  - Inside river-erosion territory (erosion > EROSION_THRESHOLD): additionally applies
 *    a strong negative carve when the ridges value is near 0, producing a narrow channel.
 *    The carve tapers quadratically from full depth at ridges=0 to zero at |ridges|=CHANNEL_HALF_WIDTH.
 *
 * Tuning:
 *  - EROSION_THRESHOLD: raise to make rivers rarer, lower to make them appear in more terrain
 *  - CHANNEL_HALF_WIDTH: controls channel width in blocks (wider noise band = wider river)
 *  - CHANNEL_DEPTH: controls how far below sea level the river floor sits
 */
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

    // Erosion must exceed this for river carving to engage
    private static final double EROSION_THRESHOLD = 0.70;
    // Half-width of the channel in ridges-space. Ridges ranges roughly -1 to 1 across
    // the landscape; this controls the physical width of the carved channel.
    private static final double CHANNEL_HALF_WIDTH = 0.08;
    // Maximum density reduction applied at the center of the channel.
    // ~0.35 pushes the terrain floor roughly 25 blocks below its normal level,
    // landing below sea level (63) so the channel fills with water naturally.
    private static final double CHANNEL_DEPTH = 0.35;
    // Matches the original elythia_surface.json erosion multiplier so non-river areas are unchanged
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
