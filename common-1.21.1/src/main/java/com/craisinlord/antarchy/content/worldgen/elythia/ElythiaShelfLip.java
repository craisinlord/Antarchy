package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.jetbrains.annotations.Nullable;

public record ElythiaShelfLip(
        DensityFunction argument,
        DensityFunction mask,
        DensityFunction lift,
        DensityFunction phase,
        DensityFunction step,
        DensityFunction riser,
        DensityFunction warp,
        double tilt,
        double minOffset,
        double maxOffset,
        double minOverhang,
        double maxOverhang,
        double minThickness,
        double maxThickness,
        double minMask,
        double maxRiser,
        @Nullable Column[] columns
) implements DensityFunction {

    public static final MapCodec<ElythiaShelfLip> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaShelfLip::argument),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("mask").forGetter(ElythiaShelfLip::mask),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("lift").forGetter(ElythiaShelfLip::lift),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("phase").forGetter(ElythiaShelfLip::phase),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("step").forGetter(ElythiaShelfLip::step),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("riser").forGetter(ElythiaShelfLip::riser),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("warp").forGetter(ElythiaShelfLip::warp),
                    Codec.doubleRange(0.0D, 0.9D).fieldOf("tilt").forGetter(ElythiaShelfLip::tilt),
                    Codec.DOUBLE.fieldOf("min_offset").forGetter(ElythiaShelfLip::minOffset),
                    Codec.DOUBLE.fieldOf("max_offset").forGetter(ElythiaShelfLip::maxOffset),
                    Codec.doubleRange(0.0D, 32.0D).fieldOf("min_overhang").forGetter(ElythiaShelfLip::minOverhang),
                    Codec.doubleRange(0.0D, 32.0D).fieldOf("max_overhang").forGetter(ElythiaShelfLip::maxOverhang),
                    Codec.doubleRange(0.0D, 64.0D).fieldOf("min_thickness").forGetter(ElythiaShelfLip::minThickness),
                    Codec.doubleRange(0.0D, 64.0D).fieldOf("max_thickness").forGetter(ElythiaShelfLip::maxThickness),
                    Codec.doubleRange(0.0D, 0.99D).fieldOf("min_mask").forGetter(ElythiaShelfLip::minMask),
                    Codec.doubleRange(0.01D, 1.0D).fieldOf("max_riser").forGetter(ElythiaShelfLip::maxRiser)
            ).apply(instance, ElythiaShelfLip::new)
    );

    public static final KeyDispatchDataCodec<ElythiaShelfLip> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double BLOCKS_PER_OFFSET = 63.4325D;
    private static final double ZERO_OFFSET_Y = 131.727D;
    private static final double RAMP = 4.0D;
    private static final double ANCHOR_DROP = 4.0D;
    private static final double CANDIDATE_BAND = 0.3D;
    private static final double SHORT_STEP = 0.25D;
    private static final double TALL_STEP = 0.51D;
    private static final int SEARCH_STRIDE = 2;
    private static final double DIAGONAL = Math.sqrt(0.5D);
    private static final int BITS = 6;
    private static final int MASK = (1 << BITS) - 1;
    private static final int SIZE = 1 << (BITS * 2);

    public record Column(long key, double topY, double gap, double thickness) {
    }

    public ElythiaShelfLip(DensityFunction argument, DensityFunction mask, DensityFunction lift, DensityFunction phase,
                           DensityFunction step, DensityFunction riser, DensityFunction warp, double tilt,
                           double minOffset, double maxOffset, double minOverhang, double maxOverhang,
                           double minThickness, double maxThickness, double minMask, double maxRiser) {
        this(argument, mask, lift, phase, step, riser, warp, tilt, minOffset, maxOffset, minOverhang, maxOverhang,
                minThickness, maxThickness, minMask, maxRiser, null);
    }

    @Override
    public double compute(FunctionContext context) {
        Column column = this.column(context);
        if (column.gap() <= 0.0D) {
            return 0.0D;
        }
        double weight = Mth.clamp((context.blockY() - (column.topY() - column.thickness())) / RAMP, 0.0D, 1.0D);
        return weight * column.gap();
    }

    private Column column(FunctionContext context) {
        Column[] table = this.columns;
        if (table == null) {
            return this.buildColumn(context, 0L);
        }
        int x = context.blockX();
        int z = context.blockZ();
        long key = ChunkPos.asLong(x, z);
        int index = (x & MASK) << BITS | (z & MASK);
        Column column = table[index];
        if (column != null && column.key() == key) {
            return column;
        }
        column = this.buildColumn(context, key);
        table[index] = column;
        return column;
    }

    private Column buildColumn(FunctionContext context, long key) {
        double mask = this.mask.compute(context);
        if (mask <= this.minMask) {
            return new Column(key, 0.0D, 0.0D, 0.0D);
        }
        ElythiaShelfTerrace.Shelf shelf = ElythiaShelfTerrace.sample(context, this.argument.compute(context), mask,
                this.lift, this.phase, this.step, this.riser, this.warp, this.tilt, this.minOffset, this.maxOffset);
        double cliff = Mth.clamp((this.maxRiser - shelf.riser()) / (this.maxRiser * 0.4D), 0.0D, 1.0D);
        if (cliff <= 0.0D || shelf.fraction() < 1.0D - shelf.riser() - CANDIDATE_BAND) {
            return new Column(key, 0.0D, 0.0D, 0.0D);
        }
        double top = shelf.blendedTop();
        double gap = top - shelf.blended();
        if (gap <= 0.0D) {
            return new Column(key, 0.0D, 0.0D, 0.0D);
        }
        double fade = Mth.smoothstep((mask - this.minMask) / (1.0D - this.minMask)) * cliff;
        double stepScale = Mth.clamp((this.step.compute(context) - SHORT_STEP) / (TALL_STEP - SHORT_STEP), 0.0D, 1.0D);
        double overhang = (this.minOverhang + (this.maxOverhang - this.minOverhang) * stepScale) * fade;
        double anchor = top - ANCHOR_DROP / BLOCKS_PER_OFFSET;
        double distance = shelf.blended() >= anchor ? 0.0D : this.anchorDistance(context.blockX(), context.blockZ(), overhang, anchor);
        if (distance < 0.0D) {
            return new Column(key, 0.0D, 0.0D, 0.0D);
        }
        double extent = overhang <= 0.0D ? 1.0D : 1.0D - distance / overhang;
        double taper = Mth.smoothstep(extent);
        double raised = gap * taper;
        double thickness = this.minThickness + (this.maxThickness - this.minThickness) * taper;
        return new Column(key, ZERO_OFFSET_Y + BLOCKS_PER_OFFSET * (shelf.blended() + raised), raised, thickness);
    }

    private double anchorDistance(int x, int z, double overhang, double underside) {
        for (int distance = SEARCH_STRIDE; distance <= overhang; distance += SEARCH_STRIDE) {
            int diagonal = (int) Math.round(distance * DIAGONAL);
            if (this.reaches(x + distance, z, underside) || this.reaches(x - distance, z, underside)
                    || this.reaches(x, z + distance, underside) || this.reaches(x, z - distance, underside)
                    || this.reaches(x + diagonal, z + diagonal, underside) || this.reaches(x + diagonal, z - diagonal, underside)
                    || this.reaches(x - diagonal, z + diagonal, underside) || this.reaches(x - diagonal, z - diagonal, underside)) {
                return distance;
            }
        }
        return -1.0D;
    }

    private boolean reaches(int x, int z, double underside) {
        DensityFunction.SinglePointContext context = new DensityFunction.SinglePointContext(x, 0, z);
        double offset = this.argument.compute(context);
        double mask = this.mask.compute(context);
        if (mask <= 0.0D) {
            return offset >= underside;
        }
        return ElythiaShelfTerrace.sample(context, offset, mask, this.lift, this.phase, this.step, this.riser, this.warp,
                this.tilt, this.minOffset, this.maxOffset).blended() >= underside;
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaShelfLip(
                this.argument.mapAll(visitor),
                this.mask.mapAll(visitor),
                this.lift.mapAll(visitor),
                this.phase.mapAll(visitor),
                this.step.mapAll(visitor),
                this.riser.mapAll(visitor),
                this.warp.mapAll(visitor),
                this.tilt,
                this.minOffset,
                this.maxOffset,
                this.minOverhang,
                this.maxOverhang,
                this.minThickness,
                this.maxThickness,
                this.minMask,
                this.maxRiser,
                this.columns != null ? this.columns : new Column[SIZE]
        ));
    }

    @Override
    public double minValue() {
        return 0.0D;
    }

    @Override
    public double maxValue() {
        return Math.max(0.0D, this.maxOffset - this.minOffset) + 4.0D;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
