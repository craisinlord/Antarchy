package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record CavarynSpireFunction(Holder<NormalNoise> noise, CavarynSurfaceMask mask) implements DensityFunction {
    public static final MapCodec<CavarynSpireFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    NormalNoise.CODEC.fieldOf("noise").forGetter(CavarynSpireFunction::noise),
                    CavarynSurfaceMask.CODEC.fieldOf("mask").forGetter(CavarynSpireFunction::mask)
            ).apply(instance, CavarynSpireFunction::new)
    );

    private static final double SCALE = 0.35;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        Noise noiseSampler = context.createNoiseSampler(this.noise);
        CavarynSpireGrid grid = CavarynSpireGrid.create(CavarynWormBoreFunction.saltFor(
                noiseSampler.get(1234.567, 89.123, -456.789),
                noiseSampler.get(-987.654, 12.345, 654.321)), this.mask.compile(context));
        return new Sampler(grid);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new CavarynSpireFunction(this.noise, this.mask.rewriteChildren(rule));
    }

    @Override
    public Interval range() {
        return Interval.of(-1.0F, 1.0F);
    }

    @Override
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<CavarynSpireFunction> codec() {
        return CODEC;
    }

    private record Sampler(CavarynSpireGrid grid) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            double distance = this.grid.signedDistance(blockX, blockY, blockZ);
            if (distance == Double.MAX_VALUE) {
                return -1.0F;
            }
            return (float) Mth.clamp(-distance * SCALE, -1.0, 1.0);
        }
    }
}
