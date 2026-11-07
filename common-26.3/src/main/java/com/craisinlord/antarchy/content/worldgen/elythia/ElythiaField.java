package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

@FunctionalInterface
interface ElythiaField {
    double compute(Point point);

    record Point(int blockX, int blockY, int blockZ, SamplerContext context) {
        Point(int blockX, int blockY, int blockZ) {
            this(blockX, blockY, blockZ, SamplerContext.EMPTY_UNCACHED);
        }

        Point at(int x, int y, int z) {
            return new Point(x, y, z, this.context);
        }
    }

    static int planarAxes(DensityFunction... inputs) {
        int axes = DensityFunction.AXIS_X | DensityFunction.AXIS_Z;
        for (DensityFunction input : inputs) {
            axes |= input.domainAxes();
        }
        return axes;
    }

    static double smoothstep(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    static ElythiaField bind(DensityFunction function, DensityFunction.CompileContext context) {
        DensitySampler sampler = function.compileSampler(context);
        return point -> sampler.sampleValue(point.context(), point.blockX(), point.blockY(), point.blockZ());
    }

    static DensitySampler sampler(ElythiaField field) {
        return new DensitySampler() {
            @Override
            public void sampleVolume(SamplerContext context, DensityBuffer output, DensityVolume volume) {
                DensitySampler.sampleVolumeNaive(context, output, volume, this);
            }

            @Override
            public float sampleValue(SamplerContext context, int x, int y, int z) {
                return (float) field.compute(new Point(x, y, z, context));
            }
        };
    }
}
