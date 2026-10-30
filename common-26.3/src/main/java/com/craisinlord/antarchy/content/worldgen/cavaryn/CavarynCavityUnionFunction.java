package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.densityfunction.*;

import java.util.List;

public record CavarynCavityUnionFunction(List<DensityFunction> arguments) implements DensityFunction {
    public static final MapCodec<CavarynCavityUnionFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(DensityFunction.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("arguments").forGetter(CavarynCavityUnionFunction::arguments))
                    .apply(instance, CavarynCavityUnionFunction::new));

    public CavarynCavityUnionFunction {
        arguments = List.copyOf(arguments);
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        DensitySampler[] samplers = this.arguments.stream().map(argument -> argument.compileSampler(context)).toArray(DensitySampler[]::new);
        return new Sampler(samplers, this.range().min());
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new CavarynCavityUnionFunction(this.arguments.stream().map(rule::rewrite).toList());
    }

    @Override
    public Interval range() {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.POSITIVE_INFINITY;
        for (DensityFunction argument : this.arguments) {
            min = Math.min(min, argument.range().min());
            max = Math.min(max, argument.range().max());
        }
        return Interval.of(min, max);
    }

    @Override
    public int domainAxes() {
        int axes = DensityFunction.NO_AXES;
        for (DensityFunction argument : this.arguments) {
            axes |= argument.domainAxes();
        }
        return axes;
    }

    @Override
    public MapCodec<CavarynCavityUnionFunction> codec() {
        return CODEC;
    }

    private record Sampler(DensitySampler[] arguments, float floor) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float best = Float.POSITIVE_INFINITY;
            for (DensitySampler argument : this.arguments) {
                best = Math.min(best, argument.sampleValue(context, blockX, blockY, blockZ));
                if (best <= this.floor) {
                    return best;
                }
            }
            return best;
        }
    }
}
