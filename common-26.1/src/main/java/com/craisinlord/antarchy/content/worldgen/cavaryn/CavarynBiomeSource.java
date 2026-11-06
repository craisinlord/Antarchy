package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class CavarynBiomeSource extends BiomeSource {
    public static final MapCodec<CavarynBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(CavarynBiomeSource::parameters),
            Climate.ParameterList.codec(Biome.CODEC.fieldOf("biome")).fieldOf("surface_biomes").forGetter(CavarynBiomeSource::surfaceParameters),
            Codec.INT.fieldOf("surface_min_y").forGetter(CavarynBiomeSource::surfaceMinY)
    ).apply(instance, CavarynBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Climate.ParameterList<Holder<Biome>> surfaceParameters;
    private final MultiNoiseBiomeSource caves;
    private final int surfaceMinY;
    private final int surfaceMinQuart;

    public CavarynBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, Climate.ParameterList<Holder<Biome>> surfaceParameters, int surfaceMinY) {
        this.parameters = parameters;
        this.surfaceParameters = surfaceParameters;
        this.caves = MultiNoiseBiomeSource.createFromList(parameters);
        this.surfaceMinY = surfaceMinY;
        this.surfaceMinQuart = QuartPos.fromBlock(surfaceMinY);
    }

    public Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    public Climate.ParameterList<Holder<Biome>> surfaceParameters() {
        return this.surfaceParameters;
    }

    public int surfaceMinY() {
        return this.surfaceMinY;
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                this.caves.possibleBiomes().stream(),
                this.surfaceParameters.values().stream().map(pair -> pair.getSecond())
        ).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        if (y >= this.surfaceMinQuart) {
            return this.surfaceParameters.findValue(sampler.sample(x, y, z));
        }
        return this.caves.getNoiseBiome(x, y, z, sampler);
    }
}
