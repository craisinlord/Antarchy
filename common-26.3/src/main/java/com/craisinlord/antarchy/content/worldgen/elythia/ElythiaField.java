package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.levelgen.densityfunction.*;

@FunctionalInterface
interface ElythiaField {
    double compute(Point point);

    record Point(int blockX, int blockY, int blockZ) {}

    static double smoothstep(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    static ElythiaField bind(DensityFunction function, DensityFunction.CompileContext context) {
        DensitySampler.Bound sampler = function.compileSampler(context).bind(SamplerContext.EMPTY_UNCACHED);
        return point -> sampler.sampleValue(point.blockX(), point.blockY(), point.blockZ());
    }

    static DensitySampler sampler(ElythiaField field) {
        return new DensitySampler() {
            @Override
            public void sampleVolume(SamplerContext context, DensityBuffer output, DensityVolume volume) {
                DensitySampler.sampleVolumeNaive(context, output, volume, this);
            }

            @Override
            public float sampleValue(SamplerContext context, int x, int y, int z) {
                return (float) field.compute(new Point(x, y, z));
            }
        };
    }
}
