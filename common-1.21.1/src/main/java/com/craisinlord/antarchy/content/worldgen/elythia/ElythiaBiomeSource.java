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
    private static final ResourceKey<Biome> OURANWOOD_FOREST = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "ouranwood_forest")
    );
    private static final ResourceKey<Biome> SPARSE_OURANWOOD_FOREST = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "sparse_ouranwood_forest")
    );
    private static final ResourceKey<Biome> PEACH_FOREST = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "peach_forest")
    );
    private static final ResourceKey<Biome> FUNGAL_OURANWOOD_FOREST = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "fungal_ouranwood_forest")
    );
    private static final ResourceKey<Biome> GLIMMERING_POOLS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "glimmering_pools")
    );
    private static final ResourceKey<Biome> CLOUD_SEA = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cloud_sea")
    );
    private static final int[] SURFACE_FALLBACK_BLOCK_YS = new int[]{192, 160, 128, 96, 64, 32, 0};

    private static final class ColumnBiomeCache {
        long key = Long.MIN_VALUE;
        Holder<Biome> value;
    }

    private final ThreadLocal<ColumnBiomeCache> molewormSurfaceFallbackCache = ThreadLocal.withInitial(ColumnBiomeCache::new);
    private final ThreadLocal<ColumnBiomeCache> peachForestSurfaceFallbackCache = ThreadLocal.withInitial(ColumnBiomeCache::new);
    private final ThreadLocal<ColumnBiomeCache> landFallbackCache = ThreadLocal.withInitial(ColumnBiomeCache::new);

    private static long packColumnKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
    }

    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters),
            Codec.INT.optionalFieldOf("moleworm_caves_max_y", 72).forGetter(ElythiaBiomeSource::molewormCavesMaxY),
            Codec.INT.optionalFieldOf("surface_biome_sample_y", 160).forGetter(ElythiaBiomeSource::surfaceBiomeSampleY),
            Codec.INT.optionalFieldOf("ocean_max_y", 85).forGetter(ElythiaBiomeSource::oceanMaxY),
            Codec.INT.optionalFieldOf("sea_level", 78).forGetter(ElythiaBiomeSource::seaLevel),
            Codec.INT.optionalFieldOf("cloud_sea_min_y", 360).forGetter(ElythiaBiomeSource::cloudSeaMinY)
    ).apply(instance, ElythiaBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int molewormCavesMaxY;
    private final int surfaceBiomeSampleY;
    private final int oceanMaxY;
    private final int seaLevel;
    private final int cloudSeaMinY;
    private final int molewormCavesMaxQuartY;
    private final int surfaceBiomeSampleQuartY;
    private final int oceanMaxQuartY;
    private final int seaLevelQuartY;
    private final int undergroundGuardMaxQuartY;
    private final int cloudSeaMinQuartY;
    private final Holder<Biome> oceanHolder;
    private final Holder<Biome> defaultLandHolder;
    private final Holder<Biome> glimmeringPoolsHolder;
    private final Holder<Biome> ouranwoodForestHolder;
    private final Holder<Biome> cloudSeaHolder;

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int molewormCavesMaxY, int surfaceBiomeSampleY, int oceanMaxY, int seaLevel, int cloudSeaMinY) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.molewormCavesMaxY = molewormCavesMaxY;
        this.surfaceBiomeSampleY = surfaceBiomeSampleY;
        this.oceanMaxY = oceanMaxY;
        this.seaLevel = seaLevel;
        this.cloudSeaMinY = cloudSeaMinY;
        this.molewormCavesMaxQuartY = QuartPos.fromBlock(molewormCavesMaxY);
        this.surfaceBiomeSampleQuartY = QuartPos.fromBlock(surfaceBiomeSampleY);
        this.oceanMaxQuartY = QuartPos.fromBlock(oceanMaxY);
        this.seaLevelQuartY = QuartPos.fromBlock(seaLevel);
        this.undergroundGuardMaxQuartY = QuartPos.fromBlock(molewormCavesMaxY + 40);
        this.cloudSeaMinQuartY = QuartPos.fromBlock(cloudSeaMinY);
        this.oceanHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> h.is(ELYTHIA_OCEAN))
                .findFirst()
                .orElse(null);
        this.glimmeringPoolsHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> h.is(GLIMMERING_POOLS))
                .findFirst()
                .orElse(null);
        this.ouranwoodForestHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> h.is(OURANWOOD_FOREST))
                .findFirst()
                .orElse(null);
        this.defaultLandHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> !isOceanBiome(h) && !h.is(ELYTHIA_BEACH)
                          && !h.is(MOLEWORM_CAVES) && !h.is(ELYTHIA_LUSH_CAVES)
                          && !h.is(GLIMMERING_POOLS))
                .findFirst()
                .orElse(null);
        this.cloudSeaHolder = parameters.values().stream()
                .map(Pair::getSecond)
                .filter(h -> h.is(CLOUD_SEA))
                .findFirst()
                .orElse(null);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() { return this.parameters; }
    private int molewormCavesMaxY() { return this.molewormCavesMaxY; }
    private int surfaceBiomeSampleY() { return this.surfaceBiomeSampleY; }
    private int oceanMaxY() { return this.oceanMaxY; }
    private int seaLevel() { return this.seaLevel; }
    private int cloudSeaMinY() { return this.cloudSeaMinY; }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.delegate.possibleBiomes().stream();
    }
    private static final long OCEAN_CONTINENTALNESS_THRESHOLD = Climate.quantizeCoord(-0.87f);
    private static final int CLOUD_SEA_CELL_SIZE = 96;
    private static final long CLOUD_SEA_CHANCE_DENOMINATOR = 5L; // 1-in-5 columns == ~20%

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        // Only some sky columns are the cloud sea — everywhere else, altitude above the cloud
        // floor just falls through to whatever biome would normally be there. Decided before
        // touching the (expensive) delegate at all, both for correctness (multi-noise has no
        // real notion of raw Y to gate a biome on) and as a speed win for the common case.
        if (this.cloudSeaHolder != null && y >= this.cloudSeaMinQuartY && isCloudSeaColumn(x, z)) {
            return this.cloudSeaHolder;
        }

        Holder<Biome> biome = this.delegate.getNoiseBiome(x, y, z, sampler);

        // Defensive: cloud_sea's multi-noise parameter point is reserved far off in "offset"
        // so it shouldn't win nearest-neighbor on its own, but if it ever does for a query
        // below the cloud floor, treat it like any other biome that has no business at that
        // altitude and fall back to a real land biome instead of leaking clouds into terrain.
        if (biome.is(CLOUD_SEA)) {
            return resolveLandFallback(x, z, sampler);
        }

        if (biome.is(MOLEWORM_CAVES) && y > this.molewormCavesMaxQuartY) {
            return resolveSurfaceFallback(x, z, sampler, this.molewormSurfaceFallbackCache, MOLEWORM_CAVES, GLIMMERING_POOLS);
        }

        Climate.TargetPoint target = sampler.sample(x, this.seaLevelQuartY, z);
        if (target.continentalness() < OCEAN_CONTINENTALNESS_THRESHOLD && y <= this.oceanMaxQuartY) {
            if (biome.is(MOLEWORM_CAVES) || biome.is(ELYTHIA_LUSH_CAVES)) return biome;
            Holder<Biome> seaLevelBiome = this.delegate.getNoiseBiome(x, this.seaLevelQuartY, z, sampler);
            if (isOceanBiome(seaLevelBiome)) return seaLevelBiome;
            return oceanHolder != null ? oceanHolder : seaLevelBiome;
        }
        if (y <= this.seaLevelQuartY && oceanHolder != null && !allowedBelowSeaLevel(biome)) {
            return oceanHolder;
        }

        if (isOceanBiome(biome)) {
            return resolveLandFallback(x, z, sampler);
        }

        if (biome.is(SPARSE_OURANWOOD_FOREST) && !isSparseOuranwoodCandidate(target, x, z)) {
            biome = this.ouranwoodForestHolder != null ? this.ouranwoodForestHolder : biome;
        }

        if (isGlimmeringPoolsCandidate(biome, target, x, z)) {
            biome = this.glimmeringPoolsHolder != null ? this.glimmeringPoolsHolder : biome;
        }
        if ((biome.is(PEACH_FOREST) || biome.is(GLIMMERING_POOLS)) && y <= this.undergroundGuardMaxQuartY) {
            return resolveSurfaceFallback(x, z, sampler, this.peachForestSurfaceFallbackCache, PEACH_FOREST, GLIMMERING_POOLS);
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

    private static boolean isOuranwoodBiome(Holder<Biome> biome) {
        return biome.is(OURANWOOD_FOREST) || biome.is(SPARSE_OURANWOOD_FOREST)
                || biome.is(FUNGAL_OURANWOOD_FOREST);
    }

    private static boolean isGlimmeringPoolsCandidate(Holder<Biome> biome, Climate.TargetPoint target, int x, int z) {
        if (!isOuranwoodBiome(biome)) {
            return false;
        }

        long humidity = target.humidity();
        long continentalness = target.continentalness();
        long weirdness = target.weirdness();
        long depth = target.depth();

        if (humidity < Climate.quantizeCoord(0.64F) || humidity > Climate.quantizeCoord(1.0F)) {
            return false;
        }
        if (continentalness < Climate.quantizeCoord(0.29F) || continentalness > Climate.quantizeCoord(0.90F)) {
            return false;
        }
        if (weirdness < Climate.quantizeCoord(-0.88F) || weirdness > Climate.quantizeCoord(0.33F)) {
            return false;
        }
        if (depth < Climate.quantizeCoord(0.0F) || depth > Climate.quantizeCoord(0.80F)) {
            return false;
        }

        long cellX = Math.floorDiv(x, 11);
        long cellZ = Math.floorDiv(z, 11);
        long gate = Math.floorMod(cellX * 73428767L + cellZ * 912931L, 5L);
        return gate <= 3L;
    }

    private static boolean isCloudSeaColumn(int x, int z) {
        long cellX = Math.floorDiv(x, CLOUD_SEA_CELL_SIZE);
        long cellZ = Math.floorDiv(z, CLOUD_SEA_CELL_SIZE);
        long gate = Math.floorMod(cellX * 668265263L + cellZ * 2246822519L, CLOUD_SEA_CHANCE_DENOMINATOR);
        return gate == 0L;
    }

    private static boolean isSparseOuranwoodCandidate(Climate.TargetPoint target, int x, int z) {
        if (target.temperature() > Climate.quantizeCoord(0.24F)) {
            return false;
        }
        if (target.humidity() > Climate.quantizeCoord(0.70F)) {
            return false;
        }
        if (target.continentalness() < Climate.quantizeCoord(0.90F)) {
            return false;
        }
        if (target.erosion() > Climate.quantizeCoord(-0.22F)) {
            return false;
        }
        if (target.weirdness() < Climate.quantizeCoord(0.92F)) {
            return false;
        }

        long cellX = Math.floorDiv(x, 24);
        long cellZ = Math.floorDiv(z, 24);
        long gate = Math.floorMod(cellX * 1103515245L + cellZ * 2147483647L, 12L);
        return gate == 0L;
    }

    @SafeVarargs
    private Holder<Biome> resolveSurfaceFallback(int x, int z, Climate.Sampler sampler, ThreadLocal<ColumnBiomeCache> cacheHolder, ResourceKey<Biome>... excluded) {
        ColumnBiomeCache cache = cacheHolder.get();
        long key = packColumnKey(x, z);
        if (cache.key == key) {
            return cache.value;
        }

        Holder<Biome> result = this.delegate.getNoiseBiome(x, this.surfaceBiomeSampleQuartY, z, sampler);
        if (isExcluded(result, excluded)) {
            for (int sampleBlockY : SURFACE_FALLBACK_BLOCK_YS) {
                Holder<Biome> candidate = this.delegate.getNoiseBiome(x, QuartPos.fromBlock(sampleBlockY), z, sampler);
                if (!isExcluded(candidate, excluded)) {
                    result = candidate;
                    break;
                }
            }
        }

        cache.key = key;
        cache.value = result;
        return result;
    }

    private Holder<Biome> resolveLandFallback(int x, int z, Climate.Sampler sampler) {
        ColumnBiomeCache cache = this.landFallbackCache.get();
        long key = packColumnKey(x, z);
        if (cache.key == key) {
            return cache.value;
        }

        Holder<Biome> result = this.delegate.getNoiseBiome(x, this.surfaceBiomeSampleQuartY, z, sampler);
        if (isOceanOrCave(result)) {
            Holder<Biome> resolved = null;
            for (int sampleBlockY : SURFACE_FALLBACK_BLOCK_YS) {
                Holder<Biome> candidate = this.delegate.getNoiseBiome(x, QuartPos.fromBlock(sampleBlockY), z, sampler);
                if (!isOceanOrCave(candidate)) {
                    resolved = candidate;
                    break;
                }
            }
            result = resolved != null ? resolved : (defaultLandHolder != null ? defaultLandHolder : result);
        }

        cache.key = key;
        cache.value = result;
        return result;
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
                || biome.is(MOLEWORM_CAVES) || biome.is(ELYTHIA_LUSH_CAVES)
                || biome.is(GLIMMERING_POOLS) || biome.is(CLOUD_SEA);
    }

    @Override
    public void addDebugInfo(java.util.List<String> debug, net.minecraft.core.BlockPos pos, Climate.Sampler sampler) {
        try {
            this.delegate.addDebugInfo(debug, pos, sampler);
        } catch (NullPointerException ignored) {
        }
        debug.add("Elythia mole cave cap: y<=" + this.molewormCavesMaxY);
        debug.add("Elythia sea level: " + this.seaLevel);
        debug.add("Elythia cloud sea: y>=" + this.cloudSeaMinY + " in ~1/" + CLOUD_SEA_CHANCE_DENOMINATOR + " columns");
    }
}
