package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record SkyIsleBiomeFunction(DensityFunction argument, Holder<NormalNoise> noise) implements DensityFunction {
    public static final double SKY_ISLE_VALUE = 1.9D;
    public static final double ROYAL_ISLE_VALUE = 1.65D;
    public static final MapCodec<SkyIsleBiomeFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("argument").forGetter(SkyIsleBiomeFunction::argument),
                    NormalNoise.CODEC.fieldOf("noise").forGetter(SkyIsleBiomeFunction::noise)
            ).apply(instance, SkyIsleBiomeFunction::new)
    );

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        SkyIsleGrid grid = SkyIsleGrid.forNoise(context.createNoiseSampler(this.noise));
        return ElythiaField.sampler(point -> {
            int footprint = grid.footprint(point.blockX(), point.blockZ());
            if (footprint == SkyIsleGrid.FOOTPRINT_ROYAL) {
                return ROYAL_ISLE_VALUE;
            }
            if (footprint == SkyIsleGrid.FOOTPRINT_SKY) {
                return SKY_ISLE_VALUE;
            }
            return argument.compute(point);
        });
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new SkyIsleBiomeFunction(rule.rewrite(this.argument), this.noise);
    }

    @Override
    public Interval range() {
        Interval argument = this.argument.range();
        return Interval.of(Math.min(argument.min(), (float) ROYAL_ISLE_VALUE), Math.max(argument.max(), (float) SKY_ISLE_VALUE));
    }

    @Override
    public int domainAxes() {
        return ElythiaField.planarAxes(this.argument);
    }

    @Override
    public MapCodec<SkyIsleBiomeFunction> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
