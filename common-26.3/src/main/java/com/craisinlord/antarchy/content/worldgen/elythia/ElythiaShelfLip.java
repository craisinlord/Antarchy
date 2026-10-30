package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.*;

public record ElythiaShelfLip(DensityFunction argument, DensityFunction mask, DensityFunction lift, DensityFunction phase, DensityFunction step, DensityFunction riser, DensityFunction warp, double tilt, double minOffset, double maxOffset, double reach, double minThickness, double maxThickness, double minMask, double maxRiser) implements DensityFunction {
    public static final MapCodec<ElythiaShelfLip> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("argument").forGetter(ElythiaShelfLip::argument),
                    DensityFunction.CODEC.fieldOf("mask").forGetter(ElythiaShelfLip::mask),
                    DensityFunction.CODEC.fieldOf("lift").forGetter(ElythiaShelfLip::lift),
                    DensityFunction.CODEC.fieldOf("phase").forGetter(ElythiaShelfLip::phase),
                    DensityFunction.CODEC.fieldOf("step").forGetter(ElythiaShelfLip::step),
                    DensityFunction.CODEC.fieldOf("riser").forGetter(ElythiaShelfLip::riser),
                    DensityFunction.CODEC.fieldOf("warp").forGetter(ElythiaShelfLip::warp),
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
    private static final double BLOCKS_PER_OFFSET = 63.4325D;
    private static final double ZERO_OFFSET_Y = 131.727D;
    private static final double RAMP = 3.0D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        ElythiaField mask = ElythiaField.bind(this.mask, context);
        ElythiaField lift = ElythiaField.bind(this.lift, context);
        ElythiaField phase = ElythiaField.bind(this.phase, context);
        ElythiaField step = ElythiaField.bind(this.step, context);
        ElythiaField riser = ElythiaField.bind(this.riser, context);
        ElythiaField warp = ElythiaField.bind(this.warp, context);
        return ElythiaField.sampler(new Runtime(argument, mask, lift, phase, step, riser, warp, this.tilt, this.minOffset, this.maxOffset, this.reach, this.minThickness, this.maxThickness, this.minMask, this.maxRiser));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaShelfLip(rule.rewrite(this.argument), rule.rewrite(this.mask), rule.rewrite(this.lift), rule.rewrite(this.phase), rule.rewrite(this.step), rule.rewrite(this.riser), rule.rewrite(this.warp), this.tilt, this.minOffset, this.maxOffset, this.reach, this.minThickness, this.maxThickness, this.minMask, this.maxRiser);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (0.0D), (float) (Math.max(0.0D, this.maxOffset - this.minOffset) + 4.0D));
    }

    @Override
    public int domainAxes() { return DensityFunction.ALL_AXES; }

    @Override
    public MapCodec<ElythiaShelfLip> codec() { return CODEC; }



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
        private final double reach;
        private final double minThickness;
        private final double maxThickness;
        private final double minMask;
        private final double maxRiser;
        private Runtime(ElythiaField argument, ElythiaField mask, ElythiaField lift, ElythiaField phase, ElythiaField step, ElythiaField riser, ElythiaField warp, double tilt, double minOffset, double maxOffset, double reach, double minThickness, double maxThickness, double minMask, double maxRiser) {
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
            this.reach = reach;
            this.minThickness = minThickness;
            this.maxThickness = maxThickness;
            this.minMask = minMask;
            this.maxRiser = maxRiser;
        }

        public double compute(ElythiaField.Point context) {
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
            double fade = ElythiaField.smoothstep((mask - this.minMask) / (1.0D - this.minMask)) * cliff;
            double thickness = (this.minThickness + (this.maxThickness - this.minThickness) * ElythiaField.smoothstep(extent)) * fade;
            double topY = ZERO_OFFSET_Y + BLOCKS_PER_OFFSET * top;
            double weight = Mth.clamp((context.blockY() - (topY - thickness)) / RAMP, 0.0D, 1.0D);
            return weight * gap;
        }
    }
}
