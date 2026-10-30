package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class ThoraxisBiomeSource extends BiomeSource {
    private static final int VERTICAL_NOISE_SCALE = 8;
    private static final double REGION_SPREAD = 0.64D;
    private static final double VERTICAL_SPREAD = 0.75D;
    private static final double DREAM_DUNES_SELECTOR_CUTOFF = 0.42D;
    private static final double HILLS_SELECTOR_CUTOFF = 0.31D;
    private static final double NADIR_FOREST_SELECTOR_MIN = 0.51D;
    private static final double NADIR_FOREST_SELECTOR_MAX = 0.57D;
    private static final double DREAM_PEAKS_MASK = 0.22D;
    private static final ResourceKey<Biome> NIGHTMARE_WASTES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_wastes")
    );
    private static final ResourceKey<Biome> DREAM_DUNES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_dunes")
    );
    private static final ResourceKey<Biome> DREAM_PEAKS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_peaks")
    );
    private static final ResourceKey<Biome> VORTEX_EXPANSE = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "vortex_expanse")
    );
    private static final ResourceKey<Biome> UMBRAL_HILLS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "umbral_hills")
    );
    private static final ResourceKey<Biome> LUCID_POOLS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lucid_pools")
    );
    private static final ResourceKey<Biome> NADIR_FOREST = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nadir_forest")
    );

    public static final MapCodec<ThoraxisBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ThoraxisBiomeSource::parameters),
            Codec.INT.optionalFieldOf("dream_dunes_max_y", 160).forGetter(ThoraxisBiomeSource::dreamDunesMaxY),
            Codec.INT.optionalFieldOf("umbral_hills_max_y", 160).forGetter(ThoraxisBiomeSource::umbralHillsMaxY),
            Codec.INT.optionalFieldOf("lucid_pools_min_y", 116).forGetter(ThoraxisBiomeSource::lucidPoolsMinY),
            Codec.INT.optionalFieldOf("nadir_forest_min_y", 128).forGetter(ThoraxisBiomeSource::nadirForestMinY),
            RegionalBiomeOverlay.CODEC.listOf().optionalFieldOf("regional_overlays", List.of()).forGetter(ThoraxisBiomeSource::regionalOverlays)
    ).apply(instance, ThoraxisBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int dreamDunesMaxY;
    private final int umbralHillsMaxY;
    private final int lucidPoolsMinY;
    private final int nadirForestMinY;
    private final List<RegionalBiomeOverlay> regionalOverlays;
    private final int dreamDunesMaxQuartY;
    private final int umbralHillsMaxQuartY;
    private final int lucidPoolsMinQuartY;
    private final int nadirForestMinQuartY;
    private volatile boolean biomesResolved;
    private Holder<Biome> nightmareWastesBiome;
    private Holder<Biome> dreamDunesBiome;
    private Holder<Biome> dreamPeaksBiome;
    private Holder<Biome> vortexExpanseBiome;
    private Holder<Biome> umbralHillsBiome;
    private Holder<Biome> lucidPoolsBiome;
    private Holder<Biome> nadirForestBiome;
    private List<ResolvedRegionalBiomeOverlay> resolvedRegionalOverlays = List.of();

    public ThoraxisBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int dreamDunesMaxY, int umbralHillsMaxY, int lucidPoolsMinY, int nadirForestMinY, List<RegionalBiomeOverlay> regionalOverlays) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.dreamDunesMaxY = dreamDunesMaxY;
        this.umbralHillsMaxY = umbralHillsMaxY;
        this.lucidPoolsMinY = lucidPoolsMinY;
        this.nadirForestMinY = nadirForestMinY;
        this.regionalOverlays = regionalOverlays;
        this.dreamDunesMaxQuartY = QuartPos.fromBlock(dreamDunesMaxY);
        this.umbralHillsMaxQuartY = QuartPos.fromBlock(umbralHillsMaxY);
        this.lucidPoolsMinQuartY = QuartPos.fromBlock(lucidPoolsMinY);
        this.nadirForestMinQuartY = QuartPos.fromBlock(nadirForestMinY);
    }

    private void resolveBiomesIfNeeded() {
        if (this.biomesResolved) {
            return;
        }
        synchronized (this) {
            if (this.biomesResolved) {
                return;
            }
            this.nightmareWastesBiome = this.findBiome(NIGHTMARE_WASTES);
            this.dreamDunesBiome = this.findBiome(DREAM_DUNES);
            this.dreamPeaksBiome = this.findBiome(DREAM_PEAKS);
            this.vortexExpanseBiome = this.findBiome(VORTEX_EXPANSE);
            this.umbralHillsBiome = this.findBiome(UMBRAL_HILLS);
            this.lucidPoolsBiome = this.findBiome(LUCID_POOLS);
            this.nadirForestBiome = this.findBiome(NADIR_FOREST);
            this.resolvedRegionalOverlays = this.resolveRegionalOverlays();
            this.biomesResolved = true;
        }
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    private int lucidPoolsMinY() {
        return this.lucidPoolsMinY;
    }

    private int nadirForestMinY() {
        return this.nadirForestMinY;
    }

    private int dreamDunesMaxY() {
        return this.dreamDunesMaxY;
    }

    private int umbralHillsMaxY() {
        return this.umbralHillsMaxY;
    }

    private List<RegionalBiomeOverlay> regionalOverlays() {
        return this.regionalOverlays;
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
        this.resolveBiomesIfNeeded();
        DensityFunction.SinglePointContext point = new DensityFunction.SinglePointContext(
                QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z));
        if (y < 0 && this.dreamDunesBiome != null) {
            if (this.dreamPeaksBiome != null && sampler.temperature().compute(point) >= DREAM_PEAKS_MASK) {
                return this.dreamPeaksBiome;
            }
            return this.dreamDunesBiome;
        }

        double region = Mth.clamp(0.5D + sampler.continentalness().compute(point) * REGION_SPREAD, 0.0D, 1.0D);
        double vertical = sampleVertical(y, sampler.depth().compute(point));
        double dreamDunesSelector = region * 0.65D + (1.0D - vertical) * 0.35D;
        double umbralHillsSelector = region * 0.55D + vertical * 0.45D;
        double nadirForestSelector = region * 0.78D + vertical * 0.22D;

        if (this.nadirForestBiome != null
                && y >= this.nadirForestMinQuartY
                && nadirForestSelector >= NADIR_FOREST_SELECTOR_MIN
                && nadirForestSelector <= NADIR_FOREST_SELECTOR_MAX) {
            return this.nadirForestBiome;
        }

        if (y >= this.lucidPoolsMinQuartY && this.lucidPoolsBiome != null) {
            return this.lucidPoolsBiome;
        }

        if (this.dreamDunesBiome != null && y <= this.dreamDunesMaxQuartY && dreamDunesSelector <= DREAM_DUNES_SELECTOR_CUTOFF) {
            return this.dreamDunesBiome;
        }

        if (this.vortexExpanseBiome != null
                && ThoraxisTerrainRegimes.dominant(sampler.erosion().compute(point), sampler.weirdness().compute(point)) == ThoraxisTerrainRegimes.SPIRES) {
            return this.vortexExpanseBiome;
        }

        if (this.umbralHillsBiome != null && y <= this.umbralHillsMaxQuartY && umbralHillsSelector <= HILLS_SELECTOR_CUTOFF) {
            return this.umbralHillsBiome;
        }

        for (ResolvedRegionalBiomeOverlay overlay : this.resolvedRegionalOverlays) {
            if (region >= overlay.minNoise() && region < overlay.maxNoise()) {
                return overlay.biome();
            }
        }

        return this.nightmareWastesBiome != null ? this.nightmareWastesBiome : this.delegate.getNoiseBiome(x, y, z, sampler);
    }

    @Override
    public void addDebugInfo(java.util.List<String> debug, BlockPos pos, Climate.Sampler sampler) {
        this.delegate.addDebugInfo(debug, pos, sampler);
        debug.add("Thoraxis selector: seeded vertical/region noise");
        debug.add("Thoraxis Lucid Pools: y>=" + this.lucidPoolsMinY);
        debug.add("Thoraxis Nadir Forest: y>=" + this.nadirForestMinY);
    }

    private Holder<Biome> findBiome(ResourceKey<Biome> key) {
        return this.delegate.possibleBiomes().stream()
                .filter(holder -> holder.is(key))
                .findFirst()
                .orElse(null);
    }

    private Holder<Biome> findBiome(ResourceLocation location) {
        ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, location);
        return this.findBiome(key);
    }

    private List<ResolvedRegionalBiomeOverlay> resolveRegionalOverlays() {
        return this.regionalOverlays.stream().map(overlay -> {
            Holder<Biome> biome = this.findBiome(overlay.biome());
            if (biome == null) {
                throw new IllegalStateException("Thoraxis regional overlay biome " + overlay.biome() + " must be listed in the biome source parameters");
            }
            return new ResolvedRegionalBiomeOverlay(biome, overlay.minNoise(), overlay.maxNoise());
        }).toList();
    }

    private record RegionalBiomeOverlay(ResourceLocation biome, double minNoise, double maxNoise) {
        private static final Codec<RegionalBiomeOverlay> CODEC = RecordCodecBuilder.<RegionalBiomeOverlay>create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("biome").forGetter(RegionalBiomeOverlay::biome),
                Codec.DOUBLE.fieldOf("min_noise").forGetter(RegionalBiomeOverlay::minNoise),
                Codec.DOUBLE.fieldOf("max_noise").forGetter(RegionalBiomeOverlay::maxNoise)
        ).apply(instance, RegionalBiomeOverlay::new)).flatXmap(
                overlay -> overlay.minNoise() >= 0.0D && overlay.maxNoise() <= 1.0D && overlay.minNoise() < overlay.maxNoise()
                        ? DataResult.success(overlay)
                        : DataResult.error(() -> "Regional biome overlay noise bounds must satisfy 0 <= min_noise < max_noise <= 1"),
                DataResult::success
        );
    }

    private record ResolvedRegionalBiomeOverlay(Holder<Biome> biome, double minNoise, double maxNoise) {
    }

    private static double sampleVertical(int quartY, double noise) {
        double scaled = positiveMod((double) quartY / (double) VERTICAL_NOISE_SCALE, 1.0D);
        double detail = Mth.clamp(0.5D + noise * VERTICAL_SPREAD, 0.0D, 1.0D);
        return scaled * 0.6D + detail * 0.4D;
    }

    private static double positiveMod(double value, double mod) {
        double result = value % mod;
        return result < 0.0D ? result + mod : result;
    }
}
