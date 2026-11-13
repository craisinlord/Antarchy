package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWormBoreFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.Nullable;

public record ElythiaButteFunction(Output output, DensityFunction argument, DensityFunction mask, Holder<NormalNoise> noise) implements DensityFunction {
    public static final MapCodec<ElythiaButteFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Output.CODEC.fieldOf("output").forGetter(ElythiaButteFunction::output),
                    DensityFunction.CODEC.fieldOf("argument").forGetter(ElythiaButteFunction::argument),
                    DensityFunction.CODEC.fieldOf("mask").forGetter(ElythiaButteFunction::mask),
                    NormalNoise.CODEC.fieldOf("noise").forGetter(ElythiaButteFunction::noise)
            ).apply(instance, ElythiaButteFunction::new)
    );
    private static final double MAX_RISE = 2.6D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        ElythiaField mask = ElythiaField.bind(this.mask, context);
        Noise noise = context.createNoiseSampler(this.noise);
        long salt = CavarynWormBoreFunction.saltFor(noise.get(311.713D, 47.219D, -829.531D), noise.get(-613.887D, 23.441D, 157.093D));
        return ElythiaField.sampler(new Runtime(this.output, argument, mask, noise, ElythiaButteGrid.create(salt, argument, mask)));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaButteFunction(this.output, rule.rewrite(this.argument), rule.rewrite(this.mask), this.noise);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (this.output != Output.OFFSET ? 0.0D : this.argument.range().min()), (float) (this.output != Output.OFFSET ? 1.0D : this.argument.range().max() + MAX_RISE));
    }

    @Override
    public int domainAxes() {
        return ElythiaField.planarAxes(this.argument, this.mask);
    }

    @Override
    public MapCodec<ElythiaButteFunction> codec() {
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

    public enum Output implements StringRepresentable {
        OFFSET("offset"),
        PRESENCE("presence"),
        PROXIMITY("proximity");

        public static final Codec<Output> CODEC = StringRepresentable.fromEnum(Output::values);

        private final String name;

        Output(String name) {
            this.name = name;
        }

            public String getSerializedName() {
            return this.name;
        }
    }

    private static final class Runtime implements ElythiaField {
        private final Output output;
        private final ElythiaField argument;
        private final ElythiaField mask;
        private final Noise noise;
        @Nullable
        private final ElythiaButteGrid grid;

        private Runtime(Output output, ElythiaField argument, ElythiaField mask, Noise noise, @Nullable ElythiaButteGrid grid) {
            this.output = output;
            this.argument = argument;
            this.mask = mask;
            this.noise = noise;
            this.grid = grid;
        }

        public double compute(ElythiaField.Point context) {
            if (this.output == Output.PRESENCE) {
                return this.grid == null ? 0.0D : this.grid.presence(context.blockX(), context.blockZ());
            }
            if (this.output == Output.PROXIMITY) {
                return this.grid == null ? 0.0D : this.grid.proximity(context.blockX(), context.blockZ());
            }
            double offset = this.argument.compute(context);
            return this.grid == null ? offset : this.grid.offset(context.blockX(), context.blockZ(), offset);
        }
    }
}
