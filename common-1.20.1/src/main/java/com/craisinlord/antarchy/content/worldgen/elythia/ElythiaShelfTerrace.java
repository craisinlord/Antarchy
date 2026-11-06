package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;

public record ElythiaShelfTerrace(
        DensityFunction argument,
        DensityFunction mask,
        DensityFunction lift,
        DensityFunction phase,
        DensityFunction step,
        DensityFunction riser,
        DensityFunction warp,
        double tilt,
        double minOffset,
        double maxOffset
) implements DensityFunction {

    public static final MapCodec<ElythiaShelfTerrace> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("argument").forGetter(ElythiaShelfTerrace::argument),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("mask").forGetter(ElythiaShelfTerrace::mask),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("lift").forGetter(ElythiaShelfTerrace::lift),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("phase").forGetter(ElythiaShelfTerrace::phase),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("step").forGetter(ElythiaShelfTerrace::step),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("riser").forGetter(ElythiaShelfTerrace::riser),
                    DensityFunction.HOLDER_HELPER_CODEC.fieldOf("warp").forGetter(ElythiaShelfTerrace::warp),
                    Codec.doubleRange(0.0D, 0.9D).fieldOf("tilt").forGetter(ElythiaShelfTerrace::tilt),
                    Codec.DOUBLE.fieldOf("min_offset").forGetter(ElythiaShelfTerrace::minOffset),
                    Codec.DOUBLE.fieldOf("max_offset").forGetter(ElythiaShelfTerrace::maxOffset)
            ).apply(instance, ElythiaShelfTerrace::new)
    );

    public static final KeyDispatchDataCodec<ElythiaShelfTerrace> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    static final double MIN_STEP = 0.15D;
    static final double MIN_RISER = 0.04D;
    static final double MAX_RISER = 0.6D;

    record Shelf(double offset, double weight, double fraction, double riser, double current, double top) {
        double blended() {
            return this.offset + this.weight * (this.current - this.offset);
        }

        double blendedTop() {
            return this.offset + this.weight * (this.top - this.offset);
        }
    }

    @Override
    public double compute(FunctionContext context) {
        double offset = this.argument.compute(context);
        double mask = this.mask.compute(context);
        if (mask <= 0.0D) {
            return offset;
        }
        return sample(context, offset, mask, this.lift, this.phase, this.step, this.riser, this.warp, this.tilt, this.minOffset, this.maxOffset).blended();
    }

    static Shelf sample(FunctionContext context, double offset, double mask, DensityFunction lift, DensityFunction phase,
                        DensityFunction step, DensityFunction riser, DensityFunction warp,
                        double tilt, double minOffset, double maxOffset) {
        double base = phase.compute(context);
        double height = Math.max(step.compute(context), MIN_STEP);
        double edge = Mth.clamp(riser.compute(context), MIN_RISER, MAX_RISER);
        double t = (offset + mask * lift.compute(context) + warp.compute(context) - base) / height;
        double level = Math.floor(t);
        double fraction = t - level;
        double floor = Math.min(offset, minOffset);
        double cap = Math.max(maxOffset, offset);
        double current = Mth.clamp(base + (level + profile(fraction, edge, tilt)) * height, floor, cap);
        double top = Mth.clamp(base + (level + 1.0D) * height, floor, cap);
        return new Shelf(offset, Mth.smoothstep(mask), fraction, edge, current, top);
    }

    static double profile(double fraction, double riser, double tilt) {
        double edge = 1.0D - riser;
        if (fraction < edge) {
            return tilt * fraction / edge;
        }
        return tilt + (1.0D - tilt) * Mth.smoothstep((fraction - edge) / riser);
    }

    @Override
    public void fillArray(double[] values, ContextProvider contextProvider) {
        for (int i = 0; i < values.length; i++) {
            values[i] = this.compute(contextProvider.forIndex(i));
        }
    }

    @Override
    public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new ElythiaShelfTerrace(
                this.argument.mapAll(visitor),
                this.mask.mapAll(visitor),
                this.lift.mapAll(visitor),
                this.phase.mapAll(visitor),
                this.step.mapAll(visitor),
                this.riser.mapAll(visitor),
                this.warp.mapAll(visitor),
                this.tilt,
                this.minOffset,
                this.maxOffset
        ));
    }

    @Override
    public double minValue() {
        return Math.min(this.argument.minValue(), this.minOffset);
    }

    @Override
    public double maxValue() {
        return Math.max(this.argument.maxValue(), this.maxOffset);
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
