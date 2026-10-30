package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaRiverFlatten(DensityFunction argument, DensityFunction gradientX, DensityFunction gradientZ, double minGradient) implements DensityFunction {
    public static final MapCodec<ElythiaRiverFlatten> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("argument").forGetter(ElythiaRiverFlatten::argument),
                    DensityFunction.CODEC.fieldOf("gradient_x").forGetter(ElythiaRiverFlatten::gradientX),
                    DensityFunction.CODEC.fieldOf("gradient_z").forGetter(ElythiaRiverFlatten::gradientZ),
                    Codec.DOUBLE.fieldOf("min_gradient").forGetter(ElythiaRiverFlatten::minGradient)
            ).apply(instance, ElythiaRiverFlatten::new)
    );
    private static final double MAX_VALUE = 1000.0D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        ElythiaField gradientX = ElythiaField.bind(this.gradientX, context);
        ElythiaField gradientZ = ElythiaField.bind(this.gradientZ, context);
        return ElythiaField.sampler(new Runtime(argument, gradientX, gradientZ, this.minGradient));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaRiverFlatten(rule.rewrite(this.argument), rule.rewrite(this.gradientX), rule.rewrite(this.gradientZ), this.minGradient);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (0.0D), (float) (MAX_VALUE));
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaRiverFlatten> codec() { return CODEC; }



    private static final class Runtime implements ElythiaField {
        private final ElythiaField argument;
        private final ElythiaField gradientX;
        private final ElythiaField gradientZ;
        private final double minGradient;
        private Runtime(ElythiaField argument, ElythiaField gradientX, ElythiaField gradientZ, double minGradient) {
            this.argument = argument;
            this.gradientX = gradientX;
            this.gradientZ = gradientZ;
            this.minGradient = minGradient;
        }

        public double compute(ElythiaField.Point context) {
            double value = Math.abs(this.argument.compute(context));
            double gradientX = this.gradientX.compute(context);
            double gradientZ = this.gradientZ.compute(context);
            double gradient = Math.sqrt(gradientX * gradientX + gradientZ * gradientZ);
            if (gradient >= this.minGradient) {
                return value;
            }
            return Math.min(MAX_VALUE, value * this.minGradient / Math.max(gradient, this.minGradient / MAX_VALUE));
        }
    }
}
