package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ThoraxisUndersideMountainsFunction(DensityFunction mask, DensityFunction style, Holder<NormalNoise> shape) implements DensityFunction {
    public static final MapCodec<ThoraxisUndersideMountainsFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("mask").forGetter(ThoraxisUndersideMountainsFunction::mask),
                    DensityFunction.CODEC.fieldOf("style").forGetter(ThoraxisUndersideMountainsFunction::style),
                    NormalNoise.CODEC.fieldOf("shape").forGetter(ThoraxisUndersideMountainsFunction::shape)
            ).apply(instance, ThoraxisUndersideMountainsFunction::new));

    public static final double MAX_HEIGHT = 130.0D;
    public static final double MASK_START = 0.17D;
    public static final double MASK_FULL = 0.34D;
    public static final double MESA_LIMIT = -0.08D;
    public static final double PEAK_START = 0.36D;
    private static final double STYLE_BLEND = 0.08D;
    private static final double TERRACE_HEIGHT = 20.0D;


    @Override
    public DensitySampler compileSampler(CompileContext context) {
        return new RuntimeSampler(this.mask.compileSampler(context), this.style.compileSampler(context), context.createNoiseSampler(this.shape));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ThoraxisUndersideMountainsFunction(rule.rewrite(this.mask), rule.rewrite(this.style), this.shape);
    }

    @Override
    public Interval range() {
        return Interval.of(0.0F, (float) MAX_HEIGHT);
    }

    @Override
    public int domainAxes() {
        return this.mask.domainAxes() | this.style.domainAxes() | DensityFunction.AXIS_X | DensityFunction.AXIS_Z;
    }

    @Override
    public MapCodec<ThoraxisUndersideMountainsFunction> codec() {
        return CODEC;
    }

    private static final class RuntimeSampler implements DensitySampler {
        private final DensitySampler mask;
        private final DensitySampler style;
        private final Noise shape;

        private RuntimeSampler(DensitySampler mask, DensitySampler style, Noise shape) {
            this.mask = mask;
            this.style = style;
            this.shape = shape;
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int x, int y, int z) {
            return (float) this.compute(context, x, y, z);
        }

        private double compute(SamplerContext context, int blockX, int blockY, int blockZ) {
            double coverage = smoothstep((this.mask.sampleValue(context, blockX, blockY, blockZ) - MASK_START) / (MASK_FULL - MASK_START));
            if (coverage <= 0.0D) {
                return 0.0D;
            }
            int x = blockX;
            int z = blockZ;
            double styleValue = this.style.sampleValue(context, blockX, blockY, blockZ);
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
            return this.shape.get(x, 0.0D, z);
        }

    }
}
