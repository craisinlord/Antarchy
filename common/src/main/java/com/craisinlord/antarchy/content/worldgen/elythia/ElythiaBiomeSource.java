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

public class ElythiaBiomeSource extends BiomeSource {
    private static final ResourceKey<Biome> MOLEWORM_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "moleworm_caves")
    );
    private static final ResourceKey<Biome> ELYTHIA_LUSH_CAVES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_lush_caves")
    );
    private static final ResourceKey<Biome> ELYTHIA_OCEAN = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_ocean")
    );
    private static final ResourceKey<Biome> ELYTHIA_CORAL_SPIKES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia_coral_spikes")
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
            Codec.INT.optionalFieldOf("ocean_max_y", 85).forGetter(ElythiaBiomeSource::oceanMaxY),
            Codec.INT.optionalFieldOf("sea_level", 78).forGetter(ElythiaBiomeSource::seaLevel)
    ).apply(instance, ElythiaBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int molewormCavesMaxY;
    private final int surfaceBiomeSampleY;
    private final int oceanMaxY;
    private final int seaLevel;
    private final int molewormCavesMaxQuartY;
    private final int surfaceBiomeSampleQuartY;
    private final int seaLevelQuartY;
    private final Holder<Biome> oceanHolder;
    private final Holder<Biome> defaultLandHolder;

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int molewormCavesMaxY, int surfaceBiomeSampleY, int oceanMaxY, int seaLevel) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.molewormCavesMaxY = molewormCavesMaxY;
        this.surfaceBiomeSampleY = surfaceBiomeSampleY;
        this.oceanMaxY = oceanMaxY;
        this.seaLevel = seaLevel;
        this.molewormCavesMaxQuartY = QuartPos.fromBlock(molewormCavesMaxY);
        this.surfaceBiomeSampleQuartY = QuartPos.fromBlock(surfaceBiomeSampleY);
        this.seaLevelQuartY = QuartPos.fromBlock(seaLevel);
        this.oceanHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> h.is(ELYTHIA_OCEAN))
                .findFirst()
                .orElse(null);
        this.defaultLandHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> !isOceanBiome(h) && !h.is(ELYTHIA_BEACH)
                          && !h.is(MOLEWORM_CAVES) && !h.is(ELYTHIA_LUSH_CAVES))
                .findFirst()
                .orElse(null);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() { return this.parameters; }
    private int molewormCavesMaxY() { return this.molewormCavesMaxY; }
    private int surfaceBiomeSampleY() { return this.surfaceBiomeSampleY; }
    private int oceanMaxY() { return this.oceanMaxY; }
    private int seaLevel() { return this.seaLevel; }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.delegate.possibleBiomes().stream();
    }

    // Conservative threshold well inside the ocean entry range (-0.85 to -0.92).
    // At continentalness < -0.80 the terrain is reliably below sea level.
    // Transition-zone columns (-0.80 to -0.85) are treated as land to avoid
    // ocean biome appearing over above-sea-level terrain.
    private static final long OCEAN_CONTINENTALNESS_THRESHOLD = Climate.quantizeCoord(-0.80f);

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        Holder<Biome> biome = this.delegate.getNoiseBiome(x, y, z, sampler);

        // Cap moleworm caves below their max Y — replace with surface biome above it
        if (biome.is(MOLEWORM_CAVES) && y > this.molewormCavesMaxQuartY) {
            return resolveSurfaceFallback(x, y, z, sampler, MOLEWORM_CAVES);
        }

        // Use continentalness (XZ-only noise) to classify the column.
        // Ocean columns get the ocean biome at every Y so structures, particles,
        // and ambient effects work correctly above the water surface.
        Climate.TargetPoint target = sampler.sample(x, this.seaLevelQuartY, z);
        if (target.continentalness() < OCEAN_CONTINENTALNESS_THRESHOLD) {
            return this.delegate.getNoiseBiome(x, this.seaLevelQuartY, z, sampler);
        }

        // Land column below or at sea level: force ocean for any biome not allowed there.
        // Use <= so quart 19 (blocks 76-79, partly underwater) is covered.
        if (y <= this.seaLevelQuartY && oceanHolder != null && !allowedBelowSeaLevel(biome)) {
            return oceanHolder;
        }

        // Land column: prevent ocean biomes from bleeding in at any Y
        if (isOceanBiome(biome)) {
            return resolveLandFallback(x, z, sampler);
        }

        return biome;
    }

    private static boolean allowedBelowSeaLevel(Holder<Biome> biome) {
        return biome.is(MOLEWORM_CAVES) || biome.is(ELYTHIA_LUSH_CAVES)
                || biome.is(ELYTHIA_OCEAN) || biome.is(ELYTHIA_CORAL_SPIKES);
    }

    private static boolean isOceanBiome(Holder<Biome> biome) {
        return biome.is(ELYTHIA_OCEAN) || biome.is(ELYTHIA_CORAL_SPIKES);
    }

    // Used for moleworm caves — returns best Y-sample, falls back to whatever the delegate gives
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

    // Used when ocean bleeds into a land column — guaranteed to never return an ocean/cave biome
    private Holder<Biome> resolveLandFallback(int x, int z, Climate.Sampler sampler) {
        Holder<Biome> fallback = this.delegate.getNoiseBiome(x, this.surfaceBiomeSampleQuartY, z, sampler);
        if (!isOceanOrCave(fallback)) return fallback;

        for (int sampleBlockY : SURFACE_FALLBACK_BLOCK_YS) {
            Holder<Biome> candidate = this.delegate.getNoiseBiome(x, QuartPos.fromBlock(sampleBlockY), z, sampler);
            if (!isOceanOrCave(candidate)) return candidate;
        }

        return defaultLandHolder != null ? defaultLandHolder : fallback;
    }

    @SafeVarargs
    private static boolean isExcluded(Holder<Biome> biome, ResourceKey<Biome>... excluded) {
        for (ResourceKey<Biome> key : excluded) {
            if (biome.is(key)) return true;
        }
        return false;
    }

    private static boolean isOceanOrCave(Holder<Biome> biome) {
        return isOceanBiome(biome) || biome.is(ELYTHIA_BEACH)
                || biome.is(MOLEWORM_CAVES) || biome.is(ELYTHIA_LUSH_CAVES);
    }

    @Override
    public void addDebugInfo(java.util.List<String> debug, net.minecraft.core.BlockPos pos, Climate.Sampler sampler) {
        this.delegate.addDebugInfo(debug, pos, sampler);
        debug.add("Elythia mole cave cap: y<=" + this.molewormCavesMaxY);
        debug.add("Elythia sea level: " + this.seaLevel);
    }
}
