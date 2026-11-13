package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class SkyIsleBiomeFunction implements DensityFunction {
    public static final double SKY_ISLE_VALUE = 1.9D;
    public static final double ROYAL_ISLE_VALUE = 1.65D;
    public static final MapCodec<SkyIsleBiomeFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(SkyIsleBiomeFunction::argument),
                    NoiseHolder.CODEC.fieldOf("noise").forGetter(SkyIsleBiomeFunction::noise)
            ).apply(instance, SkyIsleBiomeFunction::new)
    );
    public static final KeyDispatchDataCodec<SkyIsleBiomeFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private final DensityFunction argument;
    private final NoiseHolder noise;
    @Nullable
    private final SkyIsleGrid grid;

    public SkyIsleBiomeFunction(DensityFunction argument, NoiseHolder noise) {
        this(argument, noise, null);
    }

    private SkyIsleBiomeFunction(DensityFunction argument, NoiseHolder noise, @Nullable SkyIsleGrid grid) {
        this.argument = argument;
        this.noise = noise;
        this.grid = grid;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    public NoiseHolder noise() {
        return this.noise;
    }

    @Override
    public double compute(FunctionContext context) {
        if (this.grid != null) {
            int footprint = this.grid.footprint(context.blockX(), context.blockZ());
            if (footprint == SkyIsleGrid.FOOTPRINT_ROYAL) {
                return ROYAL_ISLE_VALUE;
            }
            if (footprint == SkyIsleGrid.FOOTPRINT_SKY) {
                return SKY_ISLE_VALUE;
            }
        }
        return this.argument.compute(context);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        DensityFunction mappedArgument = this.argument.mapAll(visitor);
        NoiseHolder mappedNoise = visitor.visitNoise(this.noise);
        SkyIsleGrid mappedGrid = this.grid;
        if (mappedGrid == null && mappedNoise.noise() != null) {
            mappedGrid = SkyIsleGrid.forNoise(mappedNoise.noise());
        }
        return visitor.apply(new SkyIsleBiomeFunction(mappedArgument, mappedNoise, mappedGrid));
    }

    @Override
    public double minValue() {
        return Math.min(this.argument.minValue(), ROYAL_ISLE_VALUE);
    }

    @Override
    public double maxValue() {
        return Math.max(this.argument.maxValue(), SKY_ISLE_VALUE);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
