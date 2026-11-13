package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

public record ElythiaShelfLip(DensityFunction argument, DensityFunction mask, DensityFunction lift, DensityFunction phase, DensityFunction step, DensityFunction riser, DensityFunction warp, double tilt, double minOffset, double maxOffset, double minOverhang, double maxOverhang, double minThickness, double maxThickness, double minMask, double maxRiser) implements DensityFunction {
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
                    Codec.doubleRange(0.0D, 32.0D).fieldOf("min_overhang").forGetter(ElythiaShelfLip::minOverhang),
                    Codec.doubleRange(0.0D, 32.0D).fieldOf("max_overhang").forGetter(ElythiaShelfLip::maxOverhang),
                    Codec.doubleRange(0.0D, 64.0D).fieldOf("min_thickness").forGetter(ElythiaShelfLip::minThickness),
                    Codec.doubleRange(0.0D, 64.0D).fieldOf("max_thickness").forGetter(ElythiaShelfLip::maxThickness),
                    Codec.doubleRange(0.0D, 0.99D).fieldOf("min_mask").forGetter(ElythiaShelfLip::minMask),
                    Codec.doubleRange(0.01D, 1.0D).fieldOf("max_riser").forGetter(ElythiaShelfLip::maxRiser)
            ).apply(instance, ElythiaShelfLip::new)
    );
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

    private record Column(long key, double topY, double gap, double thickness) {
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        ElythiaField argument = ElythiaField.bind(this.argument, context);
        ElythiaField mask = ElythiaField.bind(this.mask, context);
        ElythiaField lift = ElythiaField.bind(this.lift, context);
        ElythiaField phase = ElythiaField.bind(this.phase, context);
        ElythiaField step = ElythiaField.bind(this.step, context);
        ElythiaField riser = ElythiaField.bind(this.riser, context);
        ElythiaField warp = ElythiaField.bind(this.warp, context);
        return ElythiaField.sampler(new Runtime(argument, mask, lift, phase, step, riser, warp, this.tilt, this.minOffset, this.maxOffset, this.minOverhang, this.maxOverhang, this.minThickness, this.maxThickness, this.minMask, this.maxRiser));
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new ElythiaShelfLip(rule.rewrite(this.argument), rule.rewrite(this.mask), rule.rewrite(this.lift), rule.rewrite(this.phase), rule.rewrite(this.step), rule.rewrite(this.riser), rule.rewrite(this.warp), this.tilt, this.minOffset, this.maxOffset, this.minOverhang, this.maxOverhang, this.minThickness, this.maxThickness, this.minMask, this.maxRiser);
    }

    @Override
    public Interval range() {
        return Interval.of((float) (0.0D), (float) (Math.max(0.0D, this.maxOffset - this.minOffset) + 4.0D));
    }

    @Override
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<ElythiaShelfLip> codec() {
        return CODEC;
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
        private final double minOverhang;
        private final double maxOverhang;
        private final double minThickness;
        private final double maxThickness;
        private final double minMask;
        private final double maxRiser;
        private final Column[] columns = new Column[SIZE];

        private Runtime(ElythiaField argument, ElythiaField mask, ElythiaField lift, ElythiaField phase, ElythiaField step, ElythiaField riser, ElythiaField warp, double tilt, double minOffset, double maxOffset, double minOverhang, double maxOverhang, double minThickness, double maxThickness, double minMask, double maxRiser) {
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
            this.minOverhang = minOverhang;
            this.maxOverhang = maxOverhang;
            this.minThickness = minThickness;
            this.maxThickness = maxThickness;
            this.minMask = minMask;
            this.maxRiser = maxRiser;
        }

        public double compute(ElythiaField.Point context) {
            Column column = this.column(context);
            if (column.gap() <= 0.0D) {
                return 0.0D;
            }
            double weight = Mth.clamp((context.blockY() - (column.topY() - column.thickness())) / RAMP, 0.0D, 1.0D);
            return weight * column.gap();
        }

        private Column column(ElythiaField.Point context) {
            int x = context.blockX();
            int z = context.blockZ();
            long key = ChunkPos.pack(x, z);
            int index = (x & MASK) << BITS | (z & MASK);
            Column column = this.columns[index];
            if (column != null && column.key() == key) {
                return column;
            }
            column = this.buildColumn(context, key);
            this.columns[index] = column;
            return column;
        }

        private Column buildColumn(ElythiaField.Point context, long key) {
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
            double fade = Mth.smoothstep((float) ((mask - this.minMask) / (1.0D - this.minMask))) * cliff;
            double stepScale = Mth.clamp((this.step.compute(context) - SHORT_STEP) / (TALL_STEP - SHORT_STEP), 0.0D, 1.0D);
            double overhang = (this.minOverhang + (this.maxOverhang - this.minOverhang) * stepScale) * fade;
            double anchor = top - ANCHOR_DROP / BLOCKS_PER_OFFSET;
            double distance = shelf.blended() >= anchor ? 0.0D : this.anchorDistance(context, overhang, anchor);
            if (distance < 0.0D) {
                return new Column(key, 0.0D, 0.0D, 0.0D);
            }
            double extent = overhang <= 0.0D ? 1.0D : 1.0D - distance / overhang;
            double taper = Mth.smoothstep((float) extent);
            double raised = gap * taper;
            double thickness = this.minThickness + (this.maxThickness - this.minThickness) * taper;
            return new Column(key, ZERO_OFFSET_Y + BLOCKS_PER_OFFSET * (shelf.blended() + raised), raised, thickness);
        }

        private double anchorDistance(ElythiaField.Point context, double overhang, double underside) {
            int x = context.blockX();
            int z = context.blockZ();
            for (int distance = SEARCH_STRIDE; distance <= overhang; distance += SEARCH_STRIDE) {
                int diagonal = (int) Math.round(distance * DIAGONAL);
                if (this.reaches(context.at(x + distance, 0, z), underside) || this.reaches(context.at(x - distance, 0, z), underside)
                        || this.reaches(context.at(x, 0, z + distance), underside) || this.reaches(context.at(x, 0, z - distance), underside)
                        || this.reaches(context.at(x + diagonal, 0, z + diagonal), underside) || this.reaches(context.at(x + diagonal, 0, z - diagonal), underside)
                        || this.reaches(context.at(x - diagonal, 0, z + diagonal), underside) || this.reaches(context.at(x - diagonal, 0, z - diagonal), underside)) return distance;
            }
            return -1.0D;
        }

        private boolean reaches(ElythiaField.Point context, double underside) {
            double offset = this.argument.compute(context);
            double mask = this.mask.compute(context);
            if (mask <= 0.0D) {
                return offset >= underside;
            }
            return ElythiaShelfTerrace.sample(context, offset, mask, this.lift, this.phase, this.step, this.riser, this.warp,
                    this.tilt, this.minOffset, this.maxOffset).blended() >= underside;
        }
    }
}
