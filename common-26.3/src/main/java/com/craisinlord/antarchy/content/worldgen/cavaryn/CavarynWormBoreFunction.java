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

public record CavarynWormBoreFunction(DensityFunction humidity, DensityFunction weirdness, DensityFunction depth, Holder<NormalNoise> noise) implements DensityFunction {
    public static final MapCodec<CavarynWormBoreFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("humidity").forGetter(CavarynWormBoreFunction::humidity),
                    DensityFunction.CODEC.fieldOf("weirdness").forGetter(CavarynWormBoreFunction::weirdness),
                    DensityFunction.CODEC.fieldOf("depth").forGetter(CavarynWormBoreFunction::depth),
                    NormalNoise.CODEC.fieldOf("noise").forGetter(CavarynWormBoreFunction::noise)
            ).apply(instance, CavarynWormBoreFunction::new));

    public static long saltFor(double first, double second) {
        return CavarynWormBoreGrid.salt(first, second);
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        DensitySampler humiditySampler = this.humidity.compileSampler(context);
        DensitySampler weirdnessSampler = this.weirdness.compileSampler(context);
        DensitySampler depthSampler = this.depth.compileSampler(context);
        Noise noiseSampler = context.createNoiseSampler(this.noise);
        long salt = saltFor(noiseSampler.get(1234.567D, 89.123D, -456.789D), noiseSampler.get(-987.654D, 12.345D, 654.321D));
        CavarynWormBoreGrid grid = CavarynWormBoreGrid.create(salt, (x, y, z) -> {
            int bx = Mth.floor(x);
            int by = Mth.floor(y);
            int bz = Mth.floor(z);
            SamplerContext samplerContext = SamplerContext.EMPTY_UNCACHED;
            double h = humiditySampler.sampleValue(samplerContext, bx, by, bz);
            double w = weirdnessSampler.sampleValue(samplerContext, bx, by, bz);
            if (CavarynBiomeTable.excludedByClimate(CavarynBiomeTable.WORMY_CAVES, h, w)) {
                return false;
            }
            double d = depthSampler.sampleValue(samplerContext, bx, by, bz);
            return CavarynBiomeTable.WORMY_CAVES.equals(CavarynBiomeTable.winner(h, w, d));
        });
        return new Sampler(grid);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new CavarynWormBoreFunction(rule.rewrite(this.humidity), rule.rewrite(this.weirdness), rule.rewrite(this.depth), this.noise);
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
    public MapCodec<CavarynWormBoreFunction> codec() {
        return CODEC;
    }

    private record Sampler(CavarynWormBoreGrid grid) implements DensitySampler, CavarynColumn.Source {
        @Override
        public CavarynColumn newColumn() {
            return this.grid.newColumn();
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (float) CavarynColumn.density(this.grid.signedDistance(blockX, blockY, blockZ, CavarynColumn.FLOOR));
        }
    }
}
