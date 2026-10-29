package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class CavarynCavityFunction implements DensityFunction {
    public static final MapCodec<CavarynCavityFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    CavarynCavityGrid.Kind.CODEC.fieldOf("kind").forGetter(CavarynCavityFunction::kind),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("humidity").forGetter(CavarynCavityFunction::humidity),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("weirdness").forGetter(CavarynCavityFunction::weirdness),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("depth").forGetter(CavarynCavityFunction::depth),
                    NoiseHolder.CODEC.fieldOf("noise").forGetter(CavarynCavityFunction::noise)
            ).apply(instance, CavarynCavityFunction::new)
    );
    public static final KeyDispatchDataCodec<CavarynCavityFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double SCALE = 0.35;

    private final CavarynCavityGrid.Kind kind;
    private final DensityFunction humidity;
    private final DensityFunction weirdness;
    private final DensityFunction depth;
    private final NoiseHolder noise;
    @Nullable
    private final CavarynCavityGrid grid;

    public CavarynCavityFunction(CavarynCavityGrid.Kind kind, DensityFunction humidity, DensityFunction weirdness, DensityFunction depth, NoiseHolder noise) {
        this(kind, humidity, weirdness, depth, noise, null);
    }

    private CavarynCavityFunction(CavarynCavityGrid.Kind kind, DensityFunction humidity, DensityFunction weirdness, DensityFunction depth, NoiseHolder noise, @Nullable CavarynCavityGrid grid) {
        this.kind = kind;
        this.humidity = humidity;
        this.weirdness = weirdness;
        this.depth = depth;
        this.noise = noise;
        this.grid = grid;
    }

    public CavarynCavityGrid.Kind kind() {
        return this.kind;
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

    @Override
    public double compute(FunctionContext context) {
        if (this.grid == null) {
            return 1.0;
        }
        double distance = this.grid.signedDistance(context.blockX(), context.blockY(), context.blockZ());
        return Mth.clamp(distance * SCALE, -1.0, 1.0);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        DensityFunction mappedHumidity = this.humidity.mapAll(visitor);
        DensityFunction mappedWeirdness = this.weirdness.mapAll(visitor);
        DensityFunction mappedDepth = this.depth.mapAll(visitor);
        NoiseHolder mappedNoise = visitor.visitNoise(this.noise);
        CavarynCavityGrid mappedGrid = this.grid;
        if (mappedGrid == null && mappedNoise.noise() != null) {
            long salt = CavarynWormBoreFunction.saltFor(mappedNoise.getValue(1234.567, 89.123, -456.789), mappedNoise.getValue(-987.654, 12.345, 654.321));
            String biome = this.kind.biome();
            boolean strict = this.kind.strict();
            mappedGrid = CavarynCavityGrid.create(this.kind, salt, (x, y, z) -> {
                SinglePointContext point = new SinglePointContext(Mth.floor(x), Mth.floor(y), Mth.floor(z));
                double humidity = mappedHumidity.compute(point);
                double weirdness = mappedWeirdness.compute(point);
                double depth = mappedDepth.compute(point);
                return strict
                        ? CavarynBiomeTable.isOnly(biome, humidity, weirdness, depth)
                        : CavarynBiomeTable.isAmongBest(biome, humidity, weirdness, depth);
            });
        }
        return visitor.apply(new CavarynCavityFunction(this.kind, mappedHumidity, mappedWeirdness, mappedDepth, mappedNoise, mappedGrid));
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
