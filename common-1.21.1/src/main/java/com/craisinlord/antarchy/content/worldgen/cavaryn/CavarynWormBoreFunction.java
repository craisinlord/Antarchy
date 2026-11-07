package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class CavarynWormBoreFunction implements DensityFunction, CavarynColumn.Source {
    public static final MapCodec<CavarynWormBoreFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("humidity").forGetter(CavarynWormBoreFunction::humidity),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("weirdness").forGetter(CavarynWormBoreFunction::weirdness),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("depth").forGetter(CavarynWormBoreFunction::depth),
                    NoiseHolder.CODEC.fieldOf("noise").forGetter(CavarynWormBoreFunction::noise)
            ).apply(instance, CavarynWormBoreFunction::new)
    );
    public static final KeyDispatchDataCodec<CavarynWormBoreFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private final DensityFunction humidity;
    private final DensityFunction weirdness;
    private final DensityFunction depth;
    private final NoiseHolder noise;
    @Nullable
    private final CavarynWormBoreGrid grid;

    public CavarynWormBoreFunction(DensityFunction humidity, DensityFunction weirdness, DensityFunction depth, NoiseHolder noise) {
        this(humidity, weirdness, depth, noise, null);
    }

    private CavarynWormBoreFunction(DensityFunction humidity, DensityFunction weirdness, DensityFunction depth, NoiseHolder noise, @Nullable CavarynWormBoreGrid grid) {
        this.humidity = humidity;
        this.weirdness = weirdness;
        this.depth = depth;
        this.noise = noise;
        this.grid = grid;
    }

    public DensityFunction humidity() {
        return this.humidity;
    }

    public DensityFunction weirdness() {
        return this.weirdness;
    }

    public DensityFunction depth() {
        return this.depth;
    }

    public NoiseHolder noise() {
        return this.noise;
    }

    public static long saltFor(double first, double second) {
        return CavarynWormBoreGrid.salt(first, second);
    }

    @Override
    public double compute(FunctionContext context) {
        if (this.grid == null) {
            return 1.0D;
        }
        return CavarynColumn.density(this.grid.signedDistance(context.blockX(), context.blockY(), context.blockZ(), CavarynColumn.FLOOR));
    }

    @Override
    public boolean bound() {
        return this.grid != null;
    }

    @Override
    public CavarynColumn newColumn() {
        return this.grid == null ? null : this.grid.newColumn();
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        if (this.grid != null) {
            return visitor.apply(this);
        }
        DensityFunction mappedHumidity = this.humidity.mapAll(visitor);
        DensityFunction mappedWeirdness = this.weirdness.mapAll(visitor);
        DensityFunction mappedDepth = this.depth.mapAll(visitor);
        NoiseHolder mappedNoise = visitor.visitNoise(this.noise);
        CavarynWormBoreGrid mappedGrid = null;
        if (mappedNoise.noise() != null) {
            long salt = saltFor(mappedNoise.getValue(1234.567D, 89.123D, -456.789D), mappedNoise.getValue(-987.654D, 12.345D, 654.321D));
            mappedGrid = CavarynWormBoreGrid.create(salt, (x, y, z) -> {
                SinglePointContext point = new SinglePointContext(Mth.floor(x), Mth.floor(y), Mth.floor(z));
                double humidity = mappedHumidity.compute(point);
                double weirdness = mappedWeirdness.compute(point);
                if (CavarynBiomeTable.excludedByClimate(CavarynBiomeTable.WORMY_CAVES, humidity, weirdness)) {
                    return false;
                }
                return CavarynBiomeTable.WORMY_CAVES.equals(CavarynBiomeTable.winner(humidity, weirdness, mappedDepth.compute(point)));
            });
        }
        return visitor.apply(new CavarynWormBoreFunction(mappedHumidity, mappedWeirdness, mappedDepth, mappedNoise, mappedGrid));
    }

    @Override
    public double minValue() {
        return -1.0D;
    }

    @Override
    public double maxValue() {
        return 1.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
