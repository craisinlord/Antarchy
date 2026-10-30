package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaShelfTerrace(DensityFunction argument, DensityFunction mask, DensityFunction lift, DensityFunction phase, DensityFunction step, DensityFunction riser, DensityFunction warp, double tilt, double minOffset, double maxOffset) implements DensityFunction {
    public static final MapCodec<ElythiaShelfTerrace> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("argument").forGetter(ElythiaShelfTerrace::argument),
                    DensityFunction.CODEC.fieldOf("mask").forGetter(ElythiaShelfTerrace::mask),
                    DensityFunction.CODEC.fieldOf("lift").forGetter(ElythiaShelfTerrace::lift),
                    DensityFunction.CODEC.fieldOf("phase").forGetter(ElythiaShelfTerrace::phase),
                    DensityFunction.CODEC.fieldOf("step").forGetter(ElythiaShelfTerrace::step),
                    DensityFunction.CODEC.fieldOf("riser").forGetter(ElythiaShelfTerrace::riser),
                    DensityFunction.CODEC.fieldOf("warp").forGetter(ElythiaShelfTerrace::warp),
                    Codec.doubleRange(0.0D, 0.9D).fieldOf("tilt").forGetter(ElythiaShelfTerrace::tilt),
                    Codec.DOUBLE.fieldOf("min_offset").forGetter(ElythiaShelfTerrace::minOffset),
                    Codec.DOUBLE.fieldOf("max_offset").forGetter(ElythiaShelfTerrace::maxOffset)
            ).apply(instance, ElythiaShelfTerrace::new)
    );
    static final double MIN_STEP = 0.15D;
    static final double MIN_RISER = 0.04D;
    static final double MAX_RISER = 0.6D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        ElythiaField mask = ElythiaField.bind(this.mask, context);
        ElythiaField lift = ElythiaField.bind(this.lift, context);
        ElythiaField phase = ElythiaField.bind(this.phase, context);
        ElythiaField step = ElythiaField.bind(this.step, context);
        ElythiaField riser = ElythiaField.bind(this.riser, context);
        ElythiaField warp = ElythiaField.bind(this.warp, context);
        return ElythiaField.sampler(new Runtime(argument, mask, lift, phase, step, riser, warp, this.tilt, this.minOffset, this.maxOffset));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaShelfTerrace(rule.rewrite(this.argument), rule.rewrite(this.mask), rule.rewrite(this.lift), rule.rewrite(this.phase), rule.rewrite(this.step), rule.rewrite(this.riser), rule.rewrite(this.warp), this.tilt, this.minOffset, this.maxOffset);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (Math.min(this.argument.range().min(), this.minOffset)), (float) (Math.max(this.argument.range().max(), this.maxOffset)));
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaShelfTerrace> codec() { return CODEC; }

    static Shelf sample(ElythiaField.Point context, double offset, double mask, ElythiaField lift, ElythiaField phase,
                        ElythiaField step, ElythiaField riser, ElythiaField warp,
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
        return new Shelf(offset, ElythiaField.smoothstep(mask), fraction, edge, current, top);
    }

    static double profile(double fraction, double riser, double tilt) {
        double edge = 1.0D - riser;
        if (fraction < edge) {
            return tilt * fraction / edge;
        }
        return tilt + (1.0D - tilt) * ElythiaField.smoothstep((fraction - edge) / riser);
    }

    record Shelf(double offset, double weight, double fraction, double riser, double current, double top) {
        double blended() {
            return this.offset + this.weight * (this.current - this.offset);
        }

        double blendedTop() {
            return this.offset + this.weight * (this.top - this.offset);
        }
    }

    private static final class Runtime implements ElythiaField {
        private final ElythiaField argument;
        private final ElythiaField mask;
        private final ElythiaField lift;
        private final ElythiaField phase;
        private final ElythiaField step;
        private final ElythiaField riser;
        private final ElythiaField warp;
        private final double tilt;
        private final double minOffset;
        private final double maxOffset;
        private Runtime(ElythiaField argument, ElythiaField mask, ElythiaField lift, ElythiaField phase, ElythiaField step, ElythiaField riser, ElythiaField warp, double tilt, double minOffset, double maxOffset) {
            this.argument = argument;
            this.mask = mask;
            this.lift = lift;
            this.phase = phase;
            this.step = step;
            this.riser = riser;
            this.warp = warp;
            this.tilt = tilt;
            this.minOffset = minOffset;
            this.maxOffset = maxOffset;
        }

        public double compute(ElythiaField.Point context) {
            double offset = this.argument.compute(context);
            double mask = this.mask.compute(context);
            if (mask <= 0.0D) {
                return offset;
            }
            return sample(context, offset, mask, this.lift, this.phase, this.step, this.riser, this.warp, this.tilt, this.minOffset, this.maxOffset).blended();
        }
    }
}
