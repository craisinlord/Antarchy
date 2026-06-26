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
    private static final ResourceKey<Biome> ELYTHIA_OCEAN = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_ocean")
    );
    private static final ResourceKey<Biome> ELYTHIA_BEACH = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_beach")
    );
    private static final int[] SURFACE_FALLBACK_BLOCK_YS = new int[]{192, 160, 128, 96, 64, 32, 0};

    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters),
            Codec.INT.optionalFieldOf("moleworm_caves_max_y", 72).forGetter(ElythiaBiomeSource::molewormCavesMaxY),
            Codec.INT.optionalFieldOf("surface_biome_sample_y", 160).forGetter(ElythiaBiomeSource::surfaceBiomeSampleY),
            Codec.INT.optionalFieldOf("ocean_max_y", 85).forGetter(ElythiaBiomeSource::oceanMaxY)
    ).apply(instance, ElythiaBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int molewormCavesMaxY;
    private final int surfaceBiomeSampleY;
    private final int oceanMaxY;
    private final int molewormCavesMaxQuartY;
    private final int surfaceBiomeSampleQuartY;
    private final int oceanMaxQuartY;

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int molewormCavesMaxY, int surfaceBiomeSampleY, int oceanMaxY) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.molewormCavesMaxY = molewormCavesMaxY;
        this.surfaceBiomeSampleY = surfaceBiomeSampleY;
        this.oceanMaxY = oceanMaxY;
        this.molewormCavesMaxQuartY = QuartPos.fromBlock(molewormCavesMaxY);
        this.surfaceBiomeSampleQuartY = QuartPos.fromBlock(surfaceBiomeSampleY);
        this.oceanMaxQuartY = QuartPos.fromBlock(oceanMaxY);
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

    private int oceanMaxY() {
        return this.oceanMaxY;
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

        // Cap moleworm caves below their max Y
        if (biome.is(MOLEWORM_CAVES) && y > this.molewormCavesMaxQuartY) {
            return resolveSurfaceFallback(x, y, z, sampler, MOLEWORM_CAVES);
        }

        // Cap ocean/beach biomes above sea level — if terrain noise puts these biomes
        // too high, replace with whatever surface biome would naturally be there
        if ((biome.is(ELYTHIA_OCEAN) || biome.is(ELYTHIA_BEACH)) && y > this.oceanMaxQuartY) {
            return resolveSurfaceFallback(x, y, z, sampler, ELYTHIA_OCEAN, ELYTHIA_BEACH);
        }

        return biome;
    }

    @SafeVarargs
    private Holder<Biome> resolveSurfaceFallback(int x, int y, int z, Climate.Sampler sampler, ResourceKey<Biome>... excluded) {
        Holder<Biome> fallback = this.delegate.getNoiseBiome(x, this.surfaceBiomeSampleQuartY, z, sampler);
        if (!isExcluded(fallback, excluded)) {
            return fallback;
        }

        for (int sampleBlockY : SURFACE_FALLBACK_BLOCK_YS) {
            Holder<Biome> candidate = this.delegate.getNoiseBiome(x, QuartPos.fromBlock(sampleBlockY), z, sampler);
            if (!isExcluded(candidate, excluded)) {
                return candidate;
            }
        }

        return fallback;
    }

    @SafeVarargs
    private static boolean isExcluded(Holder<Biome> biome, ResourceKey<Biome>... excluded) {
        for (ResourceKey<Biome> key : excluded) {
            if (biome.is(key)) return true;
        }
        return false;
    }

    @Override
    public void addDebugInfo(java.util.List<String> debug, net.minecraft.core.BlockPos pos, Climate.Sampler sampler) {
        this.delegate.addDebugInfo(debug, pos, sampler);
        debug.add("Elythia mole cave cap: y<=" + this.molewormCavesMaxY);
        debug.add("Elythia ocean cap: y<=" + this.oceanMaxY);
    }
}
