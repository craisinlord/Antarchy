package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class ElythiaBiomeSource extends BiomeSource {
    private static final ResourceKey<Biome> CLOUD_SEA = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cloud_sea")
    );

    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters),
            Codec.INT.optionalFieldOf("cloud_sea_min_y", 250).forGetter(ElythiaBiomeSource::cloudSeaMinY)
    ).apply(instance, ElythiaBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int cloudSeaMinQuartY;
    private final Holder<Biome> cloudSeaHolder;
    private final Holder<Biome> fallbackHolder;

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int cloudSeaMinY) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.cloudSeaMinQuartY = QuartPos.fromBlock(cloudSeaMinY);
        this.cloudSeaHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(holder -> holder.is(CLOUD_SEA))
                .findFirst()
                .orElse(null);
        this.fallbackHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(holder -> !holder.is(CLOUD_SEA))
                .findFirst()
                .orElse(null);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    private int cloudSeaMinY() {
        return this.cloudSeaMinQuartY * 4;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.delegate.possibleBiomes().stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (this.cloudSeaHolder != null && y >= this.cloudSeaMinQuartY) {
            return this.cloudSeaHolder;
        }

        Holder<Biome> biome = this.delegate.getNoiseBiome(x, y, z, sampler);
        if (biome != null && !biome.is(CLOUD_SEA)) {
            return biome;
        }
        return this.fallbackHolder != null ? this.fallbackHolder : biome;
    }
}
