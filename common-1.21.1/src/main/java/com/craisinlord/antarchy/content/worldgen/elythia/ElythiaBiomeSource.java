package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.lang.ref.WeakReference;
import java.util.Arrays;
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

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
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
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
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
            return this.delegate.getNoiseBiome(x, y, z, sampler);
        }
        ColumnCache cache = this.columns.get();
        cache.select(sampler);
        int slot = (x & 3) << 2 | (z & 3);
        long key = ChunkPos.asLong(x, z);
        if (cache.ids[slot] != NO_BIOME && cache.keys[slot] == key) {
            return this.biomeTable[cache.ids[slot]];
        }
        Holder<Biome> biome = this.delegate.getNoiseBiome(x, y, z, sampler);
        cache.keys[slot] = key;
        cache.ids[slot] = this.biomeIds.getInt(biome);
        return biome;
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
