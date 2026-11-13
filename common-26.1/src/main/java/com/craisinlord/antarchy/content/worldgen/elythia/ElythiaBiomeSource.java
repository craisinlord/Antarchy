package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.datafixers.util.Pair;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class ElythiaBiomeSource extends BiomeSource {
    public static final MapCodec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters)
    ).apply(instance, ElythiaBiomeSource::new));

    private static final int COLUMN_SLOTS = 16;
    private static final int NO_BIOME = -1;
    private static final long OVERRIDE_CONTINENTALNESS = Climate.quantizeCoord(1.5F);

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Climate.ParameterList<Holder<Biome>> climate;
    private final long[] overrideMin;
    private final long[] overrideMax;
    private final Holder<Biome>[] overrides;
    private final boolean columnCached;
    private final Holder<Biome>[] biomeTable;
    private final Reference2IntMap<Holder<Biome>> biomeIds = new Reference2IntOpenHashMap<>();
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    @SuppressWarnings("unchecked")
    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters) {
        this.parameters = parameters;
        this.biomeTable = parameters.values().stream().map(Pair::getSecond).distinct().toArray(Holder[]::new);
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
        this.columnCached = sharesDepth(parameters);
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
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (!this.columnCached) {
            return this.pick(x, y, z, sampler);
        }
        ColumnCache cache = this.columns.get();
        cache.select(sampler);
        int slot = (x & 3) << 2 | (z & 3);
        long key = ChunkPos.pack(x, z);
        if (cache.ids[slot] != NO_BIOME && cache.keys[slot] == key) {
            return this.biomeTable[cache.ids[slot]];
        }
        Holder<Biome> biome = this.pick(x, y, z, sampler);
        cache.keys[slot] = key;
        cache.ids[slot] = this.biomeIds.getInt(biome);
        return biome;
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
        return this.climate.findValue(target);
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
        private WeakReference<Climate.Sampler> sampler = new WeakReference<>(null);

        private void select(Climate.Sampler current) {
            if (this.sampler.get() != current) {
                this.sampler = new WeakReference<>(current);
                Arrays.fill(this.ids, NO_BIOME);
            }
        }
    }
}
