package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class CavarynSpireFunction implements DensityFunction {
    public static final MapCodec<CavarynSpireFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    NoiseHolder.CODEC.fieldOf("noise").forGetter(CavarynSpireFunction::noise),
                    CavarynSurfaceMask.CODEC.fieldOf("mask").forGetter(CavarynSpireFunction::mask)
            ).apply(instance, CavarynSpireFunction::new)
    );
    public static final KeyDispatchDataCodec<CavarynSpireFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double SCALE = 0.35;

    private final NoiseHolder noise;
    private final CavarynSurfaceMask mask;
    @Nullable
    private final CavarynSpireGrid grid;

    public CavarynSpireFunction(NoiseHolder noise, CavarynSurfaceMask mask) {
        this(noise, mask, null);
    }

    private CavarynSpireFunction(NoiseHolder noise, CavarynSurfaceMask mask, @Nullable CavarynSpireGrid grid) {
        this.noise = noise;
        this.mask = mask;
        this.grid = grid;
    }

    public NoiseHolder noise() {
        return this.noise;
    }

    public CavarynSurfaceMask mask() {
        return this.mask;
    }

    @Override
    public double compute(FunctionContext context) {
        if (this.grid == null) {
            return -1.0;
        }
        double distance = this.grid.signedDistance(context.blockX(), context.blockY(), context.blockZ());
        if (distance == Double.MAX_VALUE) {
            return -1.0;
        }
        return Mth.clamp(-distance * SCALE, -1.0, 1.0);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        if (this.grid != null) {
            return visitor.apply(this);
        }
        NoiseHolder mappedNoise = visitor.visitNoise(this.noise);
        CavarynSurfaceMask mappedMask = this.mask.mapAll(visitor);
        CavarynSpireGrid mappedGrid = null;
        if (mappedNoise.noise() != null) {
            mappedGrid = CavarynSpireGrid.create(CavarynWormBoreFunction.saltFor(
                    mappedNoise.getValue(1234.567, 89.123, -456.789),
                    mappedNoise.getValue(-987.654, 12.345, 654.321)), mappedMask::accepts);
        }
        return visitor.apply(new CavarynSpireFunction(mappedNoise, mappedMask, mappedGrid));
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
