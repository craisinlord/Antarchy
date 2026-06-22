package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class ThoraxisBiomeSource extends BiomeSource {
    private static final int REGION_NOISE_SCALE = 96;
    private static final int REGION_DETAIL_SCALE = 41;
    private static final int VERTICAL_NOISE_SCALE = 8;
    private static final double DUNES_SELECTOR_CUTOFF = 0.38D;
    private static final double HILLS_SELECTOR_CUTOFF = 0.24D;
    private static final ResourceKey<Biome> NIGHTMARE_WASTES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "nightmare_wastes")
    );
    private static final ResourceKey<Biome> DREAM_DUNES = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dream_dunes")
    );
    private static final ResourceKey<Biome> UMBRAL_HILLS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "umbral_hills")
    );
    private static final ResourceKey<Biome> LUCID_POOLS = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "lucid_pools")
    );
    private static final ResourceKey<Biome> CLOUD_SEA = ResourceKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "cloud_sea")
    );

    public static final MapCodec<ThoraxisBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ThoraxisBiomeSource::parameters),
            Codec.INT.optionalFieldOf("dream_dunes_max_y", 96).forGetter(ThoraxisBiomeSource::dreamDunesMaxY),
            Codec.INT.optionalFieldOf("umbral_hills_max_y", 100).forGetter(ThoraxisBiomeSource::umbralHillsMaxY),
            Codec.INT.optionalFieldOf("lucid_pools_min_y", 116).forGetter(ThoraxisBiomeSource::lucidPoolsMinY)
    ).apply(instance, ThoraxisBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final int dreamDunesMaxY;
    private final int umbralHillsMaxY;
    private final int lucidPoolsMinY;
    private final int dreamDunesMaxQuartY;
    private final int umbralHillsMaxQuartY;
    private final int lucidPoolsMinQuartY;
    private final Holder<Biome> nightmareWastesBiome;
    private final Holder<Biome> dreamDunesBiome;
    private final Holder<Biome> umbralHillsBiome;
    private final Holder<Biome> lucidPoolsBiome;
    private final Holder<Biome> cloudSeaBiome;

    private static final ThreadLocal<Long2DoubleOpenHashMap> CLOUD_SEA_CACHE =
            ThreadLocal.withInitial(() -> new Long2DoubleOpenHashMap(512));
    private static final ThreadLocal<Long2DoubleOpenHashMap> REGION_CACHE =
            ThreadLocal.withInitial(() -> new Long2DoubleOpenHashMap(512));
    private static final ThreadLocal<Long2DoubleOpenHashMap> VERTICAL_CACHE =
            ThreadLocal.withInitial(() -> new Long2DoubleOpenHashMap(64));

    public ThoraxisBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, int dreamDunesMaxY, int umbralHillsMaxY, int lucidPoolsMinY) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.dreamDunesMaxY = dreamDunesMaxY;
        this.umbralHillsMaxY = umbralHillsMaxY;
        this.lucidPoolsMinY = lucidPoolsMinY;
        this.dreamDunesMaxQuartY = QuartPos.fromBlock(dreamDunesMaxY);
        this.umbralHillsMaxQuartY = QuartPos.fromBlock(umbralHillsMaxY);
        this.lucidPoolsMinQuartY = QuartPos.fromBlock(lucidPoolsMinY);
        this.nightmareWastesBiome = this.findBiome(NIGHTMARE_WASTES);
        this.dreamDunesBiome = this.findBiome(DREAM_DUNES);
        this.umbralHillsBiome = this.findBiome(UMBRAL_HILLS);
        this.lucidPoolsBiome = this.findBiome(LUCID_POOLS);
        this.cloudSeaBiome = this.findBiome(CLOUD_SEA);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    private int lucidPoolsMinY() {
        return this.lucidPoolsMinY;
    }

    private int dreamDunesMaxY() {
        return this.dreamDunesMaxY;
    }

    private int umbralHillsMaxY() {
        return this.umbralHillsMaxY;
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
        if (this.cloudSeaBiome != null && cachedIsCloudSeaZone(x, z)) {
            return this.cloudSeaBiome;
        }

        if (y >= this.lucidPoolsMinQuartY && this.lucidPoolsBiome != null) {
            return this.lucidPoolsBiome;
        }

        double region = cachedRegionNoise(x, z);
        double vertical = cachedVerticalNoise(y);
        double dreamDunesSelector = region * 0.65D + (1.0D - vertical) * 0.35D;
        double umbralHillsSelector = region * 0.55D + vertical * 0.45D;

        if (this.dreamDunesBiome != null && y <= this.dreamDunesMaxQuartY && dreamDunesSelector <= DUNES_SELECTOR_CUTOFF) {
            return this.dreamDunesBiome;
        }

        if (this.umbralHillsBiome != null && y <= this.umbralHillsMaxQuartY && umbralHillsSelector <= HILLS_SELECTOR_CUTOFF) {
            return this.umbralHillsBiome;
        }

        Holder<Biome> delegateBiome = this.delegate.getNoiseBiome(x, y, z, sampler);
        return this.nightmareWastesBiome != null ? this.nightmareWastesBiome : delegateBiome;
    }

    @Override
    public void addDebugInfo(java.util.List<String> debug, BlockPos pos, Climate.Sampler sampler) {
        this.delegate.addDebugInfo(debug, pos, sampler);
        debug.add("Thoraxis selector: mixed vertical/region noise");
        debug.add("Thoraxis Lucid Pools: y>=" + this.lucidPoolsMinY);
        debug.add("Thoraxis Cloud Sea: widened noise-selected");
    }

    private Holder<Biome> findBiome(ResourceKey<Biome> key) {
        return this.delegate.possibleBiomes().stream()
                .filter(holder -> holder.is(key))
                .findFirst()
                .orElse(null);
    }

    private boolean cachedIsCloudSeaZone(int quartX, int quartZ) {
        long key = net.minecraft.world.level.ChunkPos.asLong(quartX, quartZ);
        Long2DoubleOpenHashMap cache = CLOUD_SEA_CACHE.get();
        if (cache.containsKey(key)) {
            return cache.get(key) > 0.0D;
        }
        boolean result = isCloudSeaZone(quartX, quartZ);
        if (cache.size() >= 512) cache.clear();
        cache.put(key, result ? 1.0D : -1.0D);
        return result;
    }

    private double cachedRegionNoise(int quartX, int quartZ) {
        long key = net.minecraft.world.level.ChunkPos.asLong(quartX, quartZ);
        Long2DoubleOpenHashMap cache = REGION_CACHE.get();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        double result = sampleRegionNoise(quartX, quartZ);
        if (cache.size() >= 512) cache.clear();
        cache.put(key, result);
        return result;
    }

    private double cachedVerticalNoise(int quartY) {
        long key = (long) quartY;
        Long2DoubleOpenHashMap cache = VERTICAL_CACHE.get();
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        double result = sampleVerticalNoise(quartY);
        if (cache.size() >= 64) cache.clear();
        cache.put(key, result);
        return result;
    }

    private static boolean isCloudSeaZone(int quartX, int quartZ) {
        double primary = sampleNoise(quartX, quartZ, 56);
        double secondary = sampleNoise(quartX + 137, quartZ - 211, 24);
        double combined = primary * 0.72D + secondary * 0.28D;
        return (combined >= 0.892D && combined <= 0.952D) || (combined >= 0.962D && combined <= 0.992D);
    }

    private static double sampleRegionNoise(int quartX, int quartZ) {
        double broad = sampleNoise(quartX, quartZ, REGION_NOISE_SCALE);
        double detail = sampleNoise(quartX + 311, quartZ - 197, REGION_DETAIL_SCALE);
        return broad * 0.82D + detail * 0.18D;
    }

    private double sampleVerticalNoise(int quartY) {
        double scaled = positiveMod((double) quartY / (double) VERTICAL_NOISE_SCALE, 1.0D);
        double detail = sampleNoise(quartY + 73, quartY - 29, 5);
        return scaled * 0.6D + detail * 0.4D;
    }

    private static double sampleNoise(int x, int z, int scale) {
        int cellX = Math.floorDiv(x, scale);
        int cellZ = Math.floorDiv(z, scale);
        double fracX = positiveMod((double) x / (double) scale, 1.0D);
        double fracZ = positiveMod((double) z / (double) scale, 1.0D);

        double n00 = valueNoise(cellX, cellZ);
        double n10 = valueNoise(cellX + 1, cellZ);
        double n01 = valueNoise(cellX, cellZ + 1);
        double n11 = valueNoise(cellX + 1, cellZ + 1);

        double ix0 = lerp(smoothstep(fracX), n00, n10);
        double ix1 = lerp(smoothstep(fracX), n01, n11);
        return lerp(smoothstep(fracZ), ix0, ix1);
    }

    private static double valueNoise(int x, int z) {
        long seed = x * 341873128712L + z * 132897987541L;
        seed ^= seed >>> 33;
        seed *= 0xff51afd7ed558ccdL;
        seed ^= seed >>> 33;
        seed *= 0xc4ceb9fe1a85ec53L;
        seed ^= seed >>> 33;
        return (double) (seed & 0xFFFFFFFFL) / (double) 0xFFFFFFFFL;
    }

    private static double smoothstep(double value) {
        return value * value * (3.0D - 2.0D * value);
    }

    private static double lerp(double delta, double start, double end) {
        return start + (end - start) * delta;
    }

    private static double positiveMod(double value, double mod) {
        double result = value % mod;
        return result < 0.0D ? result + mod : result;
    }
}
