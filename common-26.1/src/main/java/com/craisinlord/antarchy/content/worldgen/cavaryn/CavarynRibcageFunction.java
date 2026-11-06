package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public final class CavarynRibcageFunction implements DensityFunction {
    public static final MapCodec<CavarynRibcageFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Part.CODEC.fieldOf("part").forGetter(CavarynRibcageFunction::part),
                    NoiseHolder.CODEC.fieldOf("noise").forGetter(CavarynRibcageFunction::noise),
                    CavarynSurfaceMask.CODEC.fieldOf("mask").forGetter(CavarynRibcageFunction::mask)
            ).apply(instance, CavarynRibcageFunction::new)
    );
    public static final KeyDispatchDataCodec<CavarynRibcageFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double SCALE = 0.35;

    private final Part part;
    private final NoiseHolder noise;
    private final CavarynSurfaceMask mask;
    @Nullable
    private final CavarynRibcageGrid grid;

    public CavarynRibcageFunction(Part part, NoiseHolder noise, CavarynSurfaceMask mask) {
        this(part, noise, mask, null);
    }

    private CavarynRibcageFunction(Part part, NoiseHolder noise, CavarynSurfaceMask mask, @Nullable CavarynRibcageGrid grid) {
        this.part = part;
        this.noise = noise;
        this.mask = mask;
        this.grid = grid;
    }

    public CavarynSurfaceMask mask() {
        return this.mask;
    }

    public Part part() {
        return this.part;
    }

    public NoiseHolder noise() {
        return this.noise;
    }

    @Override
    public double compute(FunctionContext context) {
        if (this.grid == null) {
            return this.part == Part.BONES ? -1.0 : 1.0;
        }
        if (this.part == Part.BONES) {
            double distance = this.grid.boneDistance(context.blockX(), context.blockY(), context.blockZ());
            return distance == Double.MAX_VALUE ? -1.0 : Mth.clamp(-distance * SCALE, -1.0, 1.0);
        }
        double distance = this.grid.trenchDistance(context.blockX(), context.blockY(), context.blockZ());
        return distance == Double.MAX_VALUE ? 1.0 : Mth.clamp(distance * SCALE, -1.0, 1.0);
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
        NoiseHolder mappedNoise = visitor.visitNoise(this.noise);
        CavarynSurfaceMask mappedMask = this.mask.mapAll(visitor);
        CavarynRibcageGrid mappedGrid = null;
        if (mappedNoise.noise() != null) {
            mappedGrid = CavarynRibcageGrid.create(CavarynWormBoreFunction.saltFor(
                    mappedNoise.getValue(1234.567, 89.123, -456.789),
                    mappedNoise.getValue(-987.654, 12.345, 654.321)), mappedMask::accepts);
        }
        return visitor.apply(new CavarynRibcageFunction(this.part, mappedNoise, mappedMask, mappedGrid));
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

    public enum Part implements StringRepresentable {
        BONES("bones"),
        TRENCH("trench");

        public static final Codec<Part> CODEC = StringRepresentable.fromEnum(Part::values);

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
