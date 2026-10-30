package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ElythiaHydrology(
        DensityFunction reserve,
        DensityFunction riverLevel,
        DensityFunction riverChannel,
        DensityFunction lumen,
        DensityFunction floor,
        DensityFunction valley,
        int seaLevel
) implements DensityFunction {

    public static final MapCodec<ElythiaHydrology> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("reserve").forGetter(ElythiaHydrology::reserve),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("river_level").forGetter(ElythiaHydrology::riverLevel),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("river_channel").forGetter(ElythiaHydrology::riverChannel),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("lumen").forGetter(ElythiaHydrology::lumen),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("floor").forGetter(ElythiaHydrology::floor),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("valley").forGetter(ElythiaHydrology::valley),
                    Codec.INT.fieldOf("sea_level").forGetter(ElythiaHydrology::seaLevel)
            ).apply(instance, ElythiaHydrology::new)
    );

    public static final KeyDispatchDataCodec<ElythiaHydrology> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    @Override
    public double compute(FunctionContext context) {
        return 0.0D;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        Arrays.fill(values, 0.0D);
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaHydrology(
                this.reserve.mapAll(visitor),
                this.riverLevel.mapAll(visitor),
                this.riverChannel.mapAll(visitor),
                this.lumen.mapAll(visitor),
                this.floor.mapAll(visitor),
                this.valley.mapAll(visitor),
                this.seaLevel
        ));
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return 0.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
