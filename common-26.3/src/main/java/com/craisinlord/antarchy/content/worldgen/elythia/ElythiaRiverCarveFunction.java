package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;

public record ElythiaRiverCarveFunction(
        DensityFunction erosion,
        DensityFunction ridges
) implements DensityFunction {

    public static final MapCodec<ElythiaRiverCarveFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("erosion").forGetter(ElythiaRiverCarveFunction::erosion),
                    DensityFunction.CODEC.fieldOf("ridges").forGetter(ElythiaRiverCarveFunction::ridges)
            ).apply(instance, ElythiaRiverCarveFunction::new)
    );

    private static final double EROSION_THRESHOLD = 0.70;
    private static final double CHANNEL_HALF_WIDTH = 0.08;
    private static final double CHANNEL_DEPTH = 0.35;
    private static final double BASE_EROSION_FACTOR = -0.06;

    private static double carve(double erosionVal, double ridgesVal) {
        double base = BASE_EROSION_FACTOR * erosionVal;

        if (erosionVal > EROSION_THRESHOLD) {
            double dist = Math.abs(ridgesVal);
            if (dist < CHANNEL_HALF_WIDTH) {
                double t = 1.0 - (dist / CHANNEL_HALF_WIDTH);
                double channelness = t * t;
                return base - channelness * CHANNEL_DEPTH;
            }
        }

        return base;
    }

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        return new Sampler(this.erosion.compileSampler(context), this.ridges.compileSampler(context));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction rewrittenErosion = rule.rewrite(this.erosion);
        DensityFunction rewrittenRidges = rule.rewrite(this.ridges);
        return rewrittenErosion == this.erosion && rewrittenRidges == this.ridges
                ? this
                : new ElythiaRiverCarveFunction(rewrittenErosion, rewrittenRidges);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (BASE_EROSION_FACTOR - CHANNEL_DEPTH), (float) -BASE_EROSION_FACTOR);
    }

    @DensityFunction.Axes
    @Override
    public int domainAxes() {
        return this.erosion.domainAxes() | this.ridges.domainAxes();
    }

    @Override
    public MapCodec<ElythiaRiverCarveFunction> codec() {
        return CODEC;
    }

    private record Sampler(DensitySampler erosion, DensitySampler ridges) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            this.erosion.sampleVolume(context, outputBuffer, volume);

            try (ScopedDensityBuffer ridgesBuffer = context.acquireBuffer(volume)) {
                this.ridges.sampleVolume(context, ridgesBuffer, volume);

                for (int i = 0; i < outputBuffer.size(); i++) {
                    outputBuffer.set(i, (float) carve(outputBuffer.get(i), ridgesBuffer.get(i)));
                }
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            double erosionVal = this.erosion.sampleValue(context, blockX, blockY, blockZ);
            if (erosionVal > EROSION_THRESHOLD) {
                return (float) carve(erosionVal, this.ridges.sampleValue(context, blockX, blockY, blockZ));
            }
            return (float) (BASE_EROSION_FACTOR * erosionVal);
        }
    }
}
