package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.Interval;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.synth.Noise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public record CavarynRibcageFunction(Part part, Holder<NormalNoise> noise, CavarynSurfaceMask mask) implements DensityFunction {
    public static final MapCodec<CavarynRibcageFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Part.CODEC.fieldOf("part").forGetter(CavarynRibcageFunction::part),
                    NormalNoise.CODEC.fieldOf("noise").forGetter(CavarynRibcageFunction::noise),
                    CavarynSurfaceMask.CODEC.fieldOf("mask").forGetter(CavarynRibcageFunction::mask)
            ).apply(instance, CavarynRibcageFunction::new)
    );

    private static final double SCALE = 0.35D;

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        Noise noiseSampler = context.createNoiseSampler(this.noise);
        CavarynRibcageGrid grid = CavarynRibcageGrid.create(CavarynWormBoreFunction.saltFor(
                noiseSampler.get(1234.567D, 89.123D, -456.789D),
                noiseSampler.get(-987.654D, 12.345D, 654.321D)), this.mask.compile(context));
        return new Sampler(this.part, grid);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new CavarynRibcageFunction(this.part, this.noise, this.mask.rewriteChildren(rule));
    }

    @Override
    public Interval range() {
        return Interval.of(-1.0F, 1.0F);
    }

    @Override
    public int domainAxes() {
        return DensityFunction.ALL_AXES;
    }

    @Override
    public MapCodec<CavarynRibcageFunction> codec() {
        return CODEC;
    }

    private record Sampler(Part part, CavarynRibcageGrid grid) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            if (this.part == Part.BONES) {
                double distance = this.grid.boneDistance(blockX, blockY, blockZ);
                return distance == Double.MAX_VALUE ? -1.0F : (float) Mth.clamp(-distance * SCALE, -1.0D, 1.0D);
            }
            double distance = this.grid.trenchDistance(blockX, blockY, blockZ);
            return distance == Double.MAX_VALUE ? 1.0F : (float) Mth.clamp(distance * SCALE, -1.0D, 1.0D);
        }
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
