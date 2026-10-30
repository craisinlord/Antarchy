package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaRiverCoastFade(DensityFunction argument, DensityFunction continents) implements DensityFunction {
    public static final MapCodec<ElythiaRiverCoastFade> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("argument").forGetter(ElythiaRiverCoastFade::argument),
                    DensityFunction.CODEC.fieldOf("continents").forGetter(ElythiaRiverCoastFade::continents)
            ).apply(instance, ElythiaRiverCoastFade::new)
    );
    private static final int STEP = 24;
    private static final double MIN_GRADIENT = 1.0E-12D;
    private static final double MIN_ALIGNMENT = 0.7D;
    private static final double MAX_ALIGNMENT = 0.9D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        ElythiaField continents = ElythiaField.bind(this.continents, context);
        return ElythiaField.sampler(new Runtime(argument, continents));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaRiverCoastFade(rule.rewrite(this.argument), rule.rewrite(this.continents));
    }

    @Override
    public Interval range() {
        return Interval.of((float) (0.0D), (float) (1.0D));
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaRiverCoastFade> codec() { return CODEC; }



    private static final class Runtime implements ElythiaField {
        private final ElythiaField argument;
        private final ElythiaField continents;
        private Runtime(ElythiaField argument, ElythiaField continents) {
            this.argument = argument;
            this.continents = continents;
        }

        public double compute(ElythiaField.Point context) {
            int x = context.blockX();
            int z = context.blockZ();
            double riverX = sample(this.argument, x + STEP, z) - sample(this.argument, x - STEP, z);
            double riverZ = sample(this.argument, x, z + STEP) - sample(this.argument, x, z - STEP);
            double coastX = sample(this.continents, x + STEP, z) - sample(this.continents, x - STEP, z);
            double coastZ = sample(this.continents, x, z + STEP) - sample(this.continents, x, z - STEP);
            double river = riverX * riverX + riverZ * riverZ;
            double coast = coastX * coastX + coastZ * coastZ;
            if (river < MIN_GRADIENT || coast < MIN_GRADIENT) {
                return 0.0D;
            }
            double alignment = Math.abs(riverX * coastX + riverZ * coastZ) / Math.sqrt(river * coast);
            double t = Mth.clamp((alignment - MIN_ALIGNMENT) / (MAX_ALIGNMENT - MIN_ALIGNMENT), 0.0D, 1.0D);
            return t * t * (3.0D - 2.0D * t);
        }

        private static double sample(ElythiaField function, int x, int z) {
            return function.compute(new ElythiaField.Point(x, 0, z));
        }
    }
}
