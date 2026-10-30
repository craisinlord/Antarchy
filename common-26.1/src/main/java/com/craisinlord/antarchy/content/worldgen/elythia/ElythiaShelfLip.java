package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

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
        double reach,
        double minThickness,
        double maxThickness,
        double minMask,
        double maxRiser
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
                    Codec.doubleRange(0.0D, 0.9D).fieldOf("reach").forGetter(ElythiaShelfLip::reach),
                    Codec.doubleRange(0.0D, 64.0D).fieldOf("min_thickness").forGetter(ElythiaShelfLip::minThickness),
                    Codec.doubleRange(0.0D, 64.0D).fieldOf("max_thickness").forGetter(ElythiaShelfLip::maxThickness),
                    Codec.doubleRange(0.0D, 0.99D).fieldOf("min_mask").forGetter(ElythiaShelfLip::minMask),
                    Codec.doubleRange(0.01D, 1.0D).fieldOf("max_riser").forGetter(ElythiaShelfLip::maxRiser)
            ).apply(instance, ElythiaShelfLip::new)
    );

    public static final KeyDispatchDataCodec<ElythiaShelfLip> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    private static final double BLOCKS_PER_OFFSET = 63.4325D;
    private static final double ZERO_OFFSET_Y = 131.727D;
    private static final double RAMP = 3.0D;

    @Override
    public double compute(FunctionContext context) {
        double mask = this.mask.compute(context);
        if (mask <= this.minMask) {
            return 0.0D;
        }
        ElythiaShelfTerrace.Shelf shelf = ElythiaShelfTerrace.sample(context, this.argument.compute(context), mask,
                this.lift, this.phase, this.step, this.riser, this.warp, this.tilt, this.minOffset, this.maxOffset);
        double cliff = Mth.clamp((this.maxRiser - shelf.riser()) / (this.maxRiser * 0.4D), 0.0D, 1.0D);
        if (cliff <= 0.0D) {
            return 0.0D;
        }
        double edge = 1.0D - shelf.riser();
        double start = edge - this.reach;
        if (shelf.fraction() < start) {
            return 0.0D;
        }
        double top = shelf.blendedTop();
        double gap = top - shelf.blended();
        if (gap <= 0.0D) {
            return 0.0D;
        }
        double extent = shelf.fraction() >= edge || this.reach <= 0.0D ? 1.0D : (shelf.fraction() - start) / this.reach;
        double fade = Mth.smoothstep((mask - this.minMask) / (1.0D - this.minMask)) * cliff;
        double thickness = (this.minThickness + (this.maxThickness - this.minThickness) * Mth.smoothstep(extent)) * fade;
        double topY = ZERO_OFFSET_Y + BLOCKS_PER_OFFSET * top;
        double weight = Mth.clamp((context.blockY() - (topY - thickness)) / RAMP, 0.0D, 1.0D);
        return weight * gap;
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
                this.reach,
                this.minThickness,
                this.maxThickness,
                this.minMask,
                this.maxRiser
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
