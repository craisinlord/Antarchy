package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class ElythiaBiomeSource extends BiomeSource {
    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters)
    ).apply(instance, ElythiaBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.parameters.values().stream().map(Pair::getSecond).distinct();
    }

    @Override
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return this.delegate.createResolver(sampler);
    }

    @Override
    public BiomeResolver createResolverForChunk(
            Climate.Sampler sampler,
            int minQuartX,
            int minQuartY,
            int minQuartZ,
            int quartSizeX,
            int quartSizeY,
            int quartSizeZ
    ) {
        return this.delegate.createResolverForChunk(sampler, minQuartX, minQuartY, minQuartZ, quartSizeX, quartSizeY, quartSizeZ);
    }
}
