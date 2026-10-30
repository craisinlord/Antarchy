package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class ThoraxisUndersideMountainsFunction implements DensityFunction {
    public static final MapCodec<ThoraxisUndersideMountainsFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("mask").forGetter(ThoraxisUndersideMountainsFunction::mask),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("style").forGetter(ThoraxisUndersideMountainsFunction::style),
                    DensityFunction.NoiseHolder.CODEC.fieldOf("shape").forGetter(ThoraxisUndersideMountainsFunction::shape)
            ).apply(instance, ThoraxisUndersideMountainsFunction::new)
    );
    public static final KeyDispatchDataCodec<ThoraxisUndersideMountainsFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    public static final double MAX_HEIGHT = 130.0D;
    public static final double MASK_START = 0.17D;
    public static final double MASK_FULL = 0.34D;
    public static final double MESA_LIMIT = -0.08D;
    public static final double PEAK_START = 0.36D;
    private static final double STYLE_BLEND = 0.08D;
    private static final double TERRACE_HEIGHT = 20.0D;

    private final DensityFunction mask;
    private final DensityFunction style;
    private final DensityFunction.NoiseHolder shape;

    public ThoraxisUndersideMountainsFunction(DensityFunction mask, DensityFunction style, DensityFunction.NoiseHolder shape) {
        this.mask = mask;
        this.style = style;
        this.shape = shape;
    }

    public DensityFunction mask() {
        return this.mask;
    }

    public DensityFunction style() {
        return this.style;
    }

    public DensityFunction.NoiseHolder shape() {
        return this.shape;
    }

    @Override
    public double compute(FunctionContext context) {
        double coverage = smoothstep((this.mask.compute(context) - MASK_START) / (MASK_FULL - MASK_START));
        if (coverage <= 0.0D) {
            return 0.0D;
        }
        int x = context.blockX();
        int z = context.blockZ();
        double styleValue = this.style.compute(context);
        double mesaWeight = 1.0D - smoothstep((styleValue - MESA_LIMIT) / STYLE_BLEND + 0.5D);
        double peakWeight = smoothstep((styleValue - PEAK_START) / STYLE_BLEND + 0.5D);
        double duneWeight = Math.max(0.0D, 1.0D - mesaWeight - peakWeight);
        double height = 0.0D;
        if (mesaWeight > 0.0D) {
            height += mesaWeight * this.mesa(x, z, coverage);
        }
        if (duneWeight > 0.0D) {
            height += duneWeight * this.dunes(x, z, coverage);
        }
        if (peakWeight > 0.0D) {
            height += peakWeight * this.peaks(x, z, coverage);
        }
        return Math.max(0.0D, Math.min(MAX_HEIGHT, height));
    }

    private double mesa(int x, int z, double coverage) {
        double lift = clamp01(0.62D + 1.4D * this.shape(x * 0.35D + 1200.0D, z * 0.35D - 3400.0D));
        double plateau = MAX_HEIGHT * smoothstep(coverage * 1.7D) * (0.55D + 0.45D * lift);
        double canyon = Math.abs(this.shape(x * 0.8D - 6100.0D, z * 0.8D + 2300.0D));
        if (canyon < 0.07D) {
            plateau *= 0.35D + 0.65D * smoothstep(canyon / 0.07D);
        }
        double level = plateau / TERRACE_HEIGHT;
        double step = Math.floor(level);
        double riser = smoothstep((level - step - 0.8D) / 0.2D);
        return (step + riser) * TERRACE_HEIGHT;
    }

    private double dunes(int x, int z, double coverage) {
        double first = ridge(this.shape(x * 1.1D + 4700.0D, z * 1.1D - 900.0D), 2.4D);
        double second = ridge(this.shape(z * 1.1D - 3100.0D, x * 1.1D + 5500.0D), 2.4D);
        double star = Math.max(first, second);
        double swell = clamp01(0.6D + 1.2D * this.shape(x * 0.4D - 2700.0D, z * 0.4D + 800.0D));
        return MAX_HEIGHT * coverage * (0.18D + 0.82D * star * star) * (0.65D + 0.35D * swell);
    }

    private double peaks(int x, int z, double coverage) {
        double broad = ridge(this.shape(x * 1.6D - 800.0D, z * 1.6D + 7300.0D), 2.0D);
        double jag = ridge(this.shape(x * 4.2D + 3900.0D, z * 4.2D - 5100.0D), 1.6D);
        double crest = broad * broad * broad * 0.8D + jag * jag * 0.2D;
        return MAX_HEIGHT * coverage * (0.12D + 0.88D * crest);
    }

    private static double ridge(double value, double sharpness) {
        return clamp01(1.0D - Math.abs(value) * sharpness);
    }

    private static double clamp01(double value) {
        return value < 0.0D ? 0.0D : value > 1.0D ? 1.0D : value;
    }

    private static double smoothstep(double t) {
        if (t <= 0.0D) {
            return 0.0D;
        }
        if (t >= 1.0D) {
            return 1.0D;
        }
        return t * t * (3.0D - 2.0D * t);
    }

    private double shape(double x, double z) {
        return this.shape.getValue(x, 0.0D, z);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return MAX_HEIGHT;
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ThoraxisUndersideMountainsFunction(
                this.mask.mapAll(visitor),
                this.style.mapAll(visitor),
                visitor.visitNoise(this.shape)));
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
