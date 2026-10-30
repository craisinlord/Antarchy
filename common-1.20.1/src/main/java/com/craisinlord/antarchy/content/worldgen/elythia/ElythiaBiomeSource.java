package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class ElythiaBiomeSource extends BiomeSource {
    public static final Codec<ElythiaBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(ElythiaBiomeSource::parameters)
    ).apply(instance, ElythiaBiomeSource::new));

    private static final int COLUMN_SLOTS = 16;

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final MultiNoiseBiomeSource delegate;
    private final boolean columnCached;
    private final ThreadLocal<ColumnCache> columns = ThreadLocal.withInitial(ColumnCache::new);

    public ElythiaBiomeSource(Climate.ParameterList<Holder<Biome>> parameters) {
        this.parameters = parameters;
        this.delegate = MultiNoiseBiomeSource.createFromList(parameters);
        this.columnCached = sharesDepth(parameters);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
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
        int slot = (x & 3) << 2 | (z & 3);
        long key = ChunkPos.asLong(x, z);
        if (cache.samplers[slot] == sampler && cache.keys[slot] == key) {
            return cache.biomes[slot];
        }
        Holder<Biome> biome = this.delegate.getNoiseBiome(x, y, z, sampler);
        cache.samplers[slot] = sampler;
        cache.keys[slot] = key;
        cache.biomes[slot] = biome;
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
        private final Climate.Sampler[] samplers = new Climate.Sampler[COLUMN_SLOTS];
        @SuppressWarnings("unchecked")
        private final Holder<Biome>[] biomes = new Holder[COLUMN_SLOTS];
    }
}
