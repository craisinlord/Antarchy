package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.worldgen.OptionalBiomeOverlay;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.DensityFunction;

public final class ElythiaBiomeSource extends BiomeSource {
    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters),
            OptionalBiomeOverlay.CODEC.listOf().optionalFieldOf("overlays", List.of()).forGetter(ElythiaBiomeSource::overlays),
            ElythiaCaveOverlay.CODEC.listOf().optionalFieldOf("cave_overlays", List.of()).forGetter(ElythiaBiomeSource::caveOverlays),
            Codec.INT.optionalFieldOf("cave_overlay_max_y", 96).forGetter(ElythiaBiomeSource::caveOverlayMaxY),
            RegistryOps.<Biome, ElythiaBiomeSource>retrieveGetter(Registries.BIOME)
    ).apply(instance, ElythiaBiomeSource::new));

    private static final int COLUMN_SLOTS = 16;
    private static final int NO_BIOME = -1;
    private static final long OVERRIDE_CONTINENTALNESS = Climate.quantizeCoord(1.5F);

    private static final int[] SALT_POINTS = {1048576, 524288, -786432, 917504, 655360, -1179648, -983040, -851968, 1441792, 262144, -327680, 1310720};

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final List<OptionalBiomeOverlay> overlays;
    private final List<ElythiaCaveOverlay> caveOverlays;
    private final int caveOverlayMaxY;
    private final int caveOverlayMaxQuart;
    private final List<OptionalBiomeOverlay.Resolved> resolvedOverlays;
    private final List<ElythiaCaveOverlay.Resolved> resolvedCaveOverlays;
    private final Climate.ParameterList<Holder<Biome>> climate;
    private final long[] overrideMin;
    private final long[] overrideMax;
    private final Holder<Biome>[] overrides;
    private final boolean columnCached;
    private final Holder<Biome>[] biomeTable;
    private final Reference2IntMap<Holder<Biome>> biomeIds = new Reference2IntOpenHashMap<>();
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    @SuppressWarnings("unchecked")
    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, List<OptionalBiomeOverlay> overlays,
                              List<ElythiaCaveOverlay> caveOverlays, int caveOverlayMaxY, HolderGetter<Biome> biomes) {
        this.parameters = parameters;
        this.overlays = overlays;
        this.caveOverlays = caveOverlays;
        this.caveOverlayMaxY = caveOverlayMaxY;
        this.caveOverlayMaxQuart = QuartPos.fromBlock(caveOverlayMaxY);
        this.resolvedOverlays = OptionalBiomeOverlay.resolve(overlays, biomes);
        this.resolvedCaveOverlays = ElythiaCaveOverlay.resolve(caveOverlays, biomes);
        this.biomeTable = this.allBiomes().toArray(Holder[]::new);
        this.biomeIds.defaultReturnValue(NO_BIOME);
        for (int id = 0; id < this.biomeTable.length; id++) {
            this.biomeIds.put(this.biomeTable[id], id);
        }
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> regular = new ArrayList<>();
        List<Pair<Climate.ParameterPoint, Holder<Biome>>> special = new ArrayList<>();
        for (Pair<Climate.ParameterPoint, Holder<Biome>> entry : parameters.values()) {
            if (entry.getFirst().continentalness().min() >= OVERRIDE_CONTINENTALNESS) {
                special.add(entry);
            } else {
                regular.add(entry);
            }
        }
        this.climate = new Climate.ParameterList<>(regular);
        this.overrideMin = new long[special.size()];
        this.overrideMax = new long[special.size()];
        this.overrides = new Holder[special.size()];
        for (int i = 0; i < special.size(); i++) {
            this.overrideMin[i] = special.get(i).getFirst().continentalness().min();
            this.overrideMax[i] = special.get(i).getFirst().continentalness().max();
            this.overrides[i] = special.get(i).getSecond();
        }
        this.columnCached = sharesDepth(parameters) && overlays.stream().allMatch(overlay -> overlay.depth().isEmpty());
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    private List<OptionalBiomeOverlay> overlays() {
        return this.overlays;
    }

    private List<ElythiaCaveOverlay> caveOverlays() {
        return this.caveOverlays;
    }

    private int caveOverlayMaxY() {
        return this.caveOverlayMaxY;
    }

    private Stream<Holder<Biome>> allBiomes() {
        return Stream.of(
                this.parameters.values().stream().map(Pair::getSecond),
                this.resolvedOverlays.stream().map(OptionalBiomeOverlay.Resolved::holder),
                this.resolvedCaveOverlays.stream().map(ElythiaCaveOverlay.Resolved::holder)
        ).flatMap(stream -> stream).distinct();
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return this.allBiomes();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (!this.columnCached) {
            Holder<Biome> surface = this.pick(x, y, z, sampler);
            return y < this.caveOverlayMaxQuart ? this.caveBiome(x, z, surface, sampler) : surface;
        }
        ColumnCache cache = this.columns.get();
        cache.select(sampler);
        int slot = (x & 3) << 2 | (z & 3);
        long key = ChunkPos.asLong(x, z);
        if (cache.ids[slot] != NO_BIOME && cache.keys[slot] == key) {
            return this.biomeTable[y < this.caveOverlayMaxQuart ? cache.caveIds[slot] : cache.ids[slot]];
        }
        Holder<Biome> surface = this.pick(x, y, z, sampler);
        Holder<Biome> cave = this.caveBiome(x, z, surface, sampler);
        cache.keys[slot] = key;
        cache.ids[slot] = this.biomeIds.getInt(surface);
        cache.caveIds[slot] = this.biomeIds.getInt(cave);
        return y < this.caveOverlayMaxQuart ? cave : surface;
    }

    private Holder<Biome> pick(int x, int y, int z, Climate.Sampler sampler) {
        Climate.TargetPoint target = sampler.sample(x, y, z);
        long continentalness = target.continentalness();
        if (continentalness >= OVERRIDE_CONTINENTALNESS && this.overrides.length > 0) {
            Holder<Biome> nearest = this.overrides[0];
            long nearestDistance = Long.MAX_VALUE;
            for (int i = 0; i < this.overrides.length; i++) {
                long distance = continentalness < this.overrideMin[i] ? this.overrideMin[i] - continentalness
                        : continentalness > this.overrideMax[i] ? continentalness - this.overrideMax[i] : 0L;
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = this.overrides[i];
                }
            }
            return nearest;
        }
        return OptionalBiomeOverlay.apply(this.resolvedOverlays, this.climate.findValue(target), target);
    }

    private Holder<Biome> caveBiome(int x, int z, Holder<Biome> surface, Climate.Sampler sampler) {
        if (this.resolvedCaveOverlays.isEmpty()) {
            return surface;
        }
        double region = Double.NaN;
        for (ElythiaCaveOverlay.Resolved overlay : this.resolvedCaveOverlays) {
            if (!overlay.allowedUnder(surface)) {
                continue;
            }
            if (Double.isNaN(region)) {
                region = ElythiaRegionNoise.sample(this.columns.get().salt(sampler), x, z);
            }
            if (overlay.inRegion(region)) {
                return overlay.holder();
            }
        }
        return surface;
    }

    private static long computeSalt(Climate.Sampler sampler) {
        long salt = 0L;
        for (int i = 0; i < SALT_POINTS.length; i += 2) {
            long value = Climate.quantizeCoord((float) sampler.temperature().compute(
                    new DensityFunction.SinglePointContext(SALT_POINTS[i], 0, SALT_POINTS[i + 1])));
            salt = ElythiaRegionNoise.mix(salt * 31L + value + 0x9E3779B97F4A7C15L);
        }
        return salt;
    }

    private static boolean sharesDepth(Climate.ParameterList<Holder<Biome>> parameters) {
        Climate.Parameter depth = null;
        for (Pair<Climate.ParameterPoint, Holder<Biome>> entry : parameters.values()) {
            Climate.Parameter current = entry.getFirst().depth();
            if (depth == null) {
                depth = current;
            } else if (!depth.equals(current)) {
                return false;
            }
        }
        return true;
    }

    private static final class ColumnCache {
        private final long[] keys = new long[COLUMN_SLOTS];
        private final int[] ids = new int[COLUMN_SLOTS];
        private final int[] caveIds = new int[COLUMN_SLOTS];
        private long salt;
        private boolean salted;
        private WeakReference<Climate.Sampler> sampler = new WeakReference<>(null);

        private void select(Climate.Sampler current) {
            if (this.sampler.get() != current) {
                this.sampler = new WeakReference<>(current);
                Arrays.fill(this.ids, NO_BIOME);
                this.salted = false;
            }
        }

        private long salt(Climate.Sampler current) {
            this.select(current);
            if (!this.salted) {
                this.salt = computeSalt(current);
                this.salted = true;
            }
            return this.salt;
        }
    }
}
