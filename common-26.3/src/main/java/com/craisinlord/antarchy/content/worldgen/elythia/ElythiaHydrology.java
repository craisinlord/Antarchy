package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record ElythiaHydrology(
        DensityFunction reserve,
        DensityFunction riverLevel,
        DensityFunction riverChannel,
        DensityFunction lumen,
        int seaLevel
) implements DensityFunction {

    public static final MapCodec<ElythiaHydrology> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    DensityFunction.CODEC.fieldOf("reserve").forGetter(ElythiaHydrology::reserve),
                    DensityFunction.CODEC.fieldOf("river_level").forGetter(ElythiaHydrology::riverLevel),
                    DensityFunction.CODEC.fieldOf("river_channel").forGetter(ElythiaHydrology::riverChannel),
                    DensityFunction.CODEC.fieldOf("lumen").forGetter(ElythiaHydrology::lumen),
                    Codec.INT.fieldOf("sea_level").forGetter(ElythiaHydrology::seaLevel)
            ).apply(instance, ElythiaHydrology::new)
    );

    @Override
    public DensitySampler compileSampler(DensityFunction.CompileContext context) {
        return Sampler.INSTANCE;
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        DensityFunction rewrittenReserve = rule.rewrite(this.reserve);
        DensityFunction rewrittenRiverLevel = rule.rewrite(this.riverLevel);
        DensityFunction rewrittenRiverChannel = rule.rewrite(this.riverChannel);
        DensityFunction rewrittenLumen = rule.rewrite(this.lumen);
        return rewrittenReserve == this.reserve
                && rewrittenRiverLevel == this.riverLevel
                && rewrittenRiverChannel == this.riverChannel
                && rewrittenLumen == this.lumen
                ? this
                : new ElythiaHydrology(rewrittenReserve, rewrittenRiverLevel, rewrittenRiverChannel, rewrittenLumen, this.seaLevel);
    }

    @Override
    public Interval range() {
        return Interval.of(0.0F, 0.0F);
    }

    @DensityFunction.Axes
    @Override
    public int domainAxes() {
        return DensityFunction.NO_AXES;
    }

    @Override
    public MapCodec<ElythiaHydrology> codec() {
        return CODEC;
    }

    private enum Sampler implements DensitySampler {
        INSTANCE;

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            for (int i = 0; i < outputBuffer.size(); i++) {
                outputBuffer.set(i, 0.0F);
            }
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return 0.0F;
        }
    }
}
