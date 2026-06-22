package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
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

public class ElythiaBiomeSource extends BiomeSource {
    private static final ResourceKey<Biome> MOLEWORM_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "moleworm_caves")
    );
    private static final int[] SURFACE_FALLBACK_BLOCK_YS = new int[]{192, 160, 128, 96, 64, 32, 0};

    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters),
            Codec.INT.optionalFieldOf("moleworm_caves_max_y", 72).forGetter(ElythiaBiomeSource::molewormCavesMaxY),
            Codec.INT.optionalFieldOf("surface_biome_sample_y", 160).forGetter(ElythiaBiomeSource::surfaceBiomeSampleY)
    ).apply(instance, ElythiaBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int molewormCavesMaxY;
    private final int surfaceBiomeSampleY;
    private final int molewormCavesMaxQuartY;
    private final int surfaceBiomeSampleQuartY;

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int molewormCavesMaxY, int surfaceBiomeSampleY) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.molewormCavesMaxY = molewormCavesMaxY;
        this.surfaceBiomeSampleY = surfaceBiomeSampleY;
        this.molewormCavesMaxQuartY = QuartPos.fromBlock(molewormCavesMaxY);
        this.surfaceBiomeSampleQuartY = QuartPos.fromBlock(surfaceBiomeSampleY);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    private int molewormCavesMaxY() {
        return this.molewormCavesMaxY;
    }

    private int surfaceBiomeSampleY() {
        return this.surfaceBiomeSampleY;
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
        Holder<Biome> biome = this.delegate.getNoiseBiome(x, y, z, sampler);
        if (!biome.is(MOLEWORM_CAVES) || y <= this.molewormCavesMaxQuartY) {
            return biome;
        }

        Holder<Biome> fallback = this.delegate.getNoiseBiome(x, this.surfaceBiomeSampleQuartY, z, sampler);
        if (!fallback.is(MOLEWORM_CAVES)) {
            return fallback;
        }

        for (int sampleBlockY : SURFACE_FALLBACK_BLOCK_YS) {
            Holder<Biome> candidate = this.delegate.getNoiseBiome(x, QuartPos.fromBlock(sampleBlockY), z, sampler);
            if (!candidate.is(MOLEWORM_CAVES)) {
                return candidate;
            }
        }

        return fallback;
    }

    @Override
    public void addDebugInfo(java.util.List<String> debug, net.minecraft.core.BlockPos pos, Climate.Sampler sampler) {
        this.delegate.addDebugInfo(debug, pos, sampler);
        debug.add("Elythia mole cave cap: y<=" + this.molewormCavesMaxY);
    }
}
