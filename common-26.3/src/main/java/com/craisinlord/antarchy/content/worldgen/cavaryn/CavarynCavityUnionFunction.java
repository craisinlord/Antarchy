package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

public record CavarynCavityUnionFunction(List<DensityFunction> arguments) implements DensityFunction {
    public static final MapCodec<CavarynCavityUnionFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(DensityFunction.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("arguments").forGetter(CavarynCavityUnionFunction::arguments))
                    .apply(instance, CavarynCavityUnionFunction::new));

    public CavarynCavityUnionFunction {
        arguments = List.copyOf(arguments);
    }

    @Override
    public DensitySampler compileSampler(CompileContext context) {
        DensitySampler[] samplers = this.arguments.stream().map(argument -> argument.compileSampler(context)).toArray(DensitySampler[]::new);
        float floor = this.range().min();
        CavarynColumn.Source[] sources = new CavarynColumn.Source[samplers.length];
        for (int i = 0; i < samplers.length; i++) {
            if (!(samplers[i] instanceof CavarynColumn.Source source)) {
                return new Sampler(samplers, floor);
            }
            sources[i] = source;
        }
        return new ColumnSampler(sources, floor);
    }

    @Override
    public DensityFunction rewriteChildren(DfRewriteRule rule) {
        return new CavarynCavityUnionFunction(this.arguments.stream().map(rule::rewrite).toList());
    }

    @Override
    public Interval range() {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.POSITIVE_INFINITY;
        for (DensityFunction argument : this.arguments) {
            min = Math.min(min, argument.range().min());
            max = Math.min(max, argument.range().max());
        }
        return Interval.of(min, max);
    }

    @Override
    public int domainAxes() {
        int axes = DensityFunction.NO_AXES;
        for (DensityFunction argument : this.arguments) {
            axes |= argument.domainAxes();
        }
        return axes;
    }

    @Override
    public MapCodec<CavarynCavityUnionFunction> codec() {
        return CODEC;
    }

    private record Sampler(DensitySampler[] arguments, float floor) implements DensitySampler {
        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            float best = Float.POSITIVE_INFINITY;
            for (DensitySampler argument : this.arguments) {
                best = Math.min(best, argument.sampleValue(context, blockX, blockY, blockZ));
                if (best <= this.floor) {
                    return best;
                }
            }
            return best;
        }
    }

    private static final class ColumnSampler implements DensitySampler {
        private final ThreadLocal<ColumnSet> columns;

        ColumnSampler(CavarynColumn.Source[] sources, float floor) {
            this.columns = ThreadLocal.withInitial(() -> new ColumnSet(sources, floor));
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this.columns.get());
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            return this.columns.get().sampleValue(context, blockX, blockY, blockZ);
        }
    }

    private static final class ColumnSet implements DensitySampler {
        private final CavarynColumn[] columns;
        private final CavarynColumn[] active;
        private final float floor;
        private int activeCount;
        private boolean loaded;
        private int qx;
        private int qz;
        private double activeMinY;
        private double activeMaxY;

        ColumnSet(CavarynColumn.Source[] sources, float floor) {
            this.columns = new CavarynColumn[sources.length];
            for (int i = 0; i < sources.length; i++) {
                this.columns[i] = sources[i].newColumn();
            }
            this.active = new CavarynColumn[sources.length];
            this.floor = floor;
        }

        @Override
        public void sampleVolume(SamplerContext context, DensityBuffer outputBuffer, DensityVolume volume) {
            DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
        }

        @Override
        public float sampleValue(SamplerContext context, int blockX, int blockY, int blockZ) {
            int currentQx = blockX >> 2;
            int currentQz = blockZ >> 2;
            if (!this.loaded || this.qx != currentQx || this.qz != currentQz) {
                this.load(currentQx, currentQz);
            }
            double py = blockY + 0.5D;
            if (py < this.activeMinY || py > this.activeMaxY) {
                return 1.0F;
            }
            float best = 1.0F;
            for (int i = 0; i < this.activeCount; i++) {
                best = Math.min(best, (float) CavarynColumn.density(this.active[i].signedDistance(blockX, blockY, blockZ, CavarynColumn.FLOOR)));
                if (best <= this.floor) {
                    return best;
                }
            }
            return best;
        }

        private void load(int currentQx, int currentQz) {
            int count = 0;
            double min = Double.MAX_VALUE;
            double max = -Double.MAX_VALUE;
            for (CavarynColumn column : this.columns) {
                column.load(currentQx, currentQz);
                if (!column.isEmpty()) {
                    this.active[count++] = column;
                    min = Math.min(min, column.minY());
                    max = Math.max(max, column.maxY());
                }
            }
            this.activeCount = count;
            this.activeMinY = min;
            this.activeMaxY = max;
            this.qx = currentQx;
            this.qz = currentQz;
            this.loaded = true;
        }
    }
}
