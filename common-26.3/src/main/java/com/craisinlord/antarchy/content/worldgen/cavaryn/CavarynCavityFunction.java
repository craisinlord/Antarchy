package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record CavarynCavityFunction(CavarynCavityGrid.Kind kind, DensityFunction humidity, DensityFunction weirdness, DensityFunction depth, Holder<NormalNoise> noise) implements DensityFunction {
    public static final MapCodec<CavarynCavityFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    CavarynCavityGrid.Kind.CODEC.fieldOf("kind").forGetter(CavarynCavityFunction::kind),
                    DensityFunction.CODEC.fieldOf("humidity").forGetter(CavarynCavityFunction::humidity),
                    DensityFunction.CODEC.fieldOf("weirdness").forGetter(CavarynCavityFunction::weirdness),
                    DensityFunction.CODEC.fieldOf("depth").forGetter(CavarynCavityFunction::depth),
                    NormalNoise.CODEC.fieldOf("noise").forGetter(CavarynCavityFunction::noise)
            ).apply(instance, CavarynCavityFunction::new));
    private static final double SCALE = 0.35;
    private static final double FLOOR = -1.0 / SCALE - 1.0E-9;

    public static long saltFor(double first, double second) {
        return CavarynWormBoreGrid.salt(first, second);
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        DensitySampler humiditySampler = this.humidity.compileSampler(context);
        DensitySampler weirdnessSampler = this.weirdness.compileSampler(context);
        DensitySampler depthSampler = this.depth.compileSampler(context);
        Noise noiseSampler = context.createNoiseSampler(this.noise);
        long salt = saltFor(noiseSampler.get(1234.567, 89.123, -456.789), noiseSampler.get(-987.654, 12.345, 654.321));
        CavarynCavityGrid grid = CavarynCavityGrid.create(this.kind, salt, (x, y, z) -> {
            int bx = Mth.floor(x);
            int by = Mth.floor(y);
            int bz = Mth.floor(z);
            SamplerContext samplerContext = SamplerContext.EMPTY_UNCACHED;
            double h = humiditySampler.sampleValue(samplerContext, bx, by, bz);
            double w = weirdnessSampler.sampleValue(samplerContext, bx, by, bz);
            double d = depthSampler.sampleValue(samplerContext, bx, by, bz);
            String biome = this.kind.biome();
            return this.kind.strict()
                    ? CavarynBiomeTable.isOnly(biome, h, w, d)
                    : CavarynBiomeTable.isAmongBest(biome, h, w, d);
        });
        return new Sampler(grid);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new CavarynCavityFunction(this.kind, rule.rewrite(this.humidity), rule.rewrite(this.weirdness), rule.rewrite(this.depth), this.noise);
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
    public MapCodec<CavarynCavityFunction> codec() {
        return CODEC;
    }

    private record Sampler(CavarynCavityGrid grid) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return (float) Mth.clamp(this.grid.signedDistance(blockX, blockY, blockZ, FLOOR) * SCALE, -1.0, 1.0);
        }
    }
}
