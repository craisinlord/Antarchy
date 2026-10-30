package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaRiverSpan(DensityFunction argument, double scale) implements DensityFunction {
    public static final MapCodec<ElythiaRiverSpan> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("argument").forGetter(ElythiaRiverSpan::argument),
                    Codec.DOUBLE.fieldOf("scale").forGetter(ElythiaRiverSpan::scale)
            ).apply(instance, ElythiaRiverSpan::new)
    );
    private static final int STEP = 4;
    private static final double MIN_GRADIENT = 1.0E-9D;
    private static final double MAX_VALUE = 1000.0D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        return ElythiaField.sampler(new Runtime(argument, this.scale));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaRiverSpan(rule.rewrite(this.argument), this.scale);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (0.0D), (float) (MAX_VALUE));
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaRiverSpan> codec() { return CODEC; }



    private static final class Runtime implements ElythiaField {
        private final ElythiaField argument;
        private final double scale;
        private Runtime(ElythiaField argument, double scale) {
            this.argument = argument;
            this.scale = scale;
        }

        public double compute(ElythiaField.Point context) {
            int x = context.blockX();
            int z = context.blockZ();
            double value = this.sample(x, z);
            double gradientX = (this.sample(x + STEP, z) - this.sample(x - STEP, z)) / (2.0D * STEP);
            double gradientZ = (this.sample(x, z + STEP) - this.sample(x, z - STEP)) / (2.0D * STEP);
            double gradient = Math.sqrt(gradientX * gradientX + gradientZ * gradientZ);
            if (gradient < MIN_GRADIENT) {
                return MAX_VALUE;
            }
            return Math.min(MAX_VALUE, Math.abs(value) / gradient * this.scale);
        }

        private double sample(int x, int z) {
            return this.argument.compute(new ElythiaField.Point(x, 0, z));
        }
    }
}
