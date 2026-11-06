package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record CavarynSurfaceMask(DensityFunction temperature, DensityFunction erosion,
                                 float temperatureMin, float temperatureMax, float erosionMin, float erosionMax) {
    public static final Codec<CavarynSurfaceMask> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DensityFunction.CODEC.fieldOf("temperature").forGetter(CavarynSurfaceMask::temperature),
            DensityFunction.CODEC.fieldOf("erosion").forGetter(CavarynSurfaceMask::erosion),
            Codec.FLOAT.fieldOf("temperature_min").forGetter(CavarynSurfaceMask::temperatureMin),
            Codec.FLOAT.fieldOf("temperature_max").forGetter(CavarynSurfaceMask::temperatureMax),
            Codec.FLOAT.fieldOf("erosion_min").forGetter(CavarynSurfaceMask::erosionMin),
            Codec.FLOAT.fieldOf("erosion_max").forGetter(CavarynSurfaceMask::erosionMax)
    ).apply(instance, CavarynSurfaceMask::new));

    private static final int SAMPLE_Y = 300;

    public CavarynSurfaceMask rewriteChildren(DfRewriteRule rule) {
        return new CavarynSurfaceMask(rule.rewrite(this.temperature), rule.rewrite(this.erosion),
                this.temperatureMin, this.temperatureMax, this.erosionMin, this.erosionMax);
    }

    public Filter compile(DensityFunction.CompileContext context) {
        DensitySampler temperatureSampler = this.temperature.compileSampler(context);
        DensitySampler erosionSampler = this.erosion.compileSampler(context);
        return (x, z) -> {
            int blockX = Mth.floor(x);
            int blockZ = Mth.floor(z);
            SamplerContext samplerContext = SamplerContext.EMPTY_UNCACHED;
            double t = temperatureSampler.sampleValue(samplerContext, blockX, SAMPLE_Y, blockZ);
            if (t < this.temperatureMin || t > this.temperatureMax) {
                return false;
            }
            double e = erosionSampler.sampleValue(samplerContext, blockX, SAMPLE_Y, blockZ);
            return e >= this.erosionMin && e <= this.erosionMax;
        };
    }

    public interface Filter {
        boolean accepts(double x, double z);
    }
}
