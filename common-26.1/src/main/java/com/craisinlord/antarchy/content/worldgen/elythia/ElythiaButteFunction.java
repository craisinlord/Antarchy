package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.worldgen.cavaryn.CavarynWormBoreFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class ElythiaButteFunction implements DensityFunction {
    public static final MapCodec<ElythiaButteFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Output.CODEC.fieldOf("output").forGetter(ElythiaButteFunction::output),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaButteFunction::argument),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("mask").forGetter(ElythiaButteFunction::mask),
                    NoiseHolder.CODEC.fieldOf("noise").forGetter(ElythiaButteFunction::noise)
            ).apply(instance, ElythiaButteFunction::new)
    );
    public static final KeyDispatchDataCodec<ElythiaButteFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double MAX_RISE = 2.6D;

    private final Output output;
    private final DensityFunction argument;
    private final DensityFunction mask;
    private final NoiseHolder noise;
    @Nullable
    private final ElythiaButteGrid grid;

    public ElythiaButteFunction(Output output, DensityFunction argument, DensityFunction mask, NoiseHolder noise) {
        this(output, argument, mask, noise, null);
    }

    private ElythiaButteFunction(Output output, DensityFunction argument, DensityFunction mask, NoiseHolder noise, @Nullable ElythiaButteGrid grid) {
        this.output = output;
        this.argument = argument;
        this.mask = mask;
        this.noise = noise;
        this.grid = grid;
    }

    public Output output() {
        return this.output;
    }

    public DensityFunction argument() {
        return this.argument;
    }

    public DensityFunction mask() {
        return this.mask;
    }

    public NoiseHolder noise() {
        return this.noise;
    }

    @Override
    public double compute(FunctionContext context) {
        if (this.output == Output.PRESENCE) {
            return this.grid == null ? 0.0D : this.grid.presence(context.blockX(), context.blockZ());
        }
        if (this.output == Output.PROXIMITY) {
            return this.grid == null ? 0.0D : this.grid.proximity(context.blockX(), context.blockZ());
        }
        double offset = this.argument.compute(context);
        return this.grid == null ? offset : this.grid.offset(context.blockX(), context.blockZ(), offset);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        DensityFunction mappedArgument = this.argument.mapAll(visitor);
        DensityFunction mappedMask = this.mask.mapAll(visitor);
        NoiseHolder mappedNoise = visitor.visitNoise(this.noise);
        ElythiaButteGrid mappedGrid = this.grid;
        if (mappedGrid == null && mappedNoise.noise() != null) {
            long salt = CavarynWormBoreFunction.saltFor(mappedNoise.getValue(311.713, 47.219, -829.531), mappedNoise.getValue(-613.887, 23.441, 157.093));
            mappedGrid = ElythiaButteGrid.create(salt, mappedArgument, mappedMask);
        }
        return visitor.apply(new ElythiaButteFunction(this.output, mappedArgument, mappedMask, mappedNoise, mappedGrid));
    }

    @Override
    public double minValue() {
        return this.output != Output.OFFSET ? 0.0D : this.argument.minValue();
    }

    @Override
    public double maxValue() {
        return this.output != Output.OFFSET ? 1.0D : this.argument.maxValue() + MAX_RISE;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
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

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
