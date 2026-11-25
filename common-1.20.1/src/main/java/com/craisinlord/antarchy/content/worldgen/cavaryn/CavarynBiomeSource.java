package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.content.worldgen.OptionalBiomeOverlay;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

public final class CavarynBiomeSource extends BiomeSource {
    public static final Codec<CavarynBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MultiNoiseBiomeSource.DIRECT_CODEC.forGetter(CavarynBiomeSource::parameters),
            Climate.ParameterList.codec(Biome.CODEC.fieldOf("biome")).optionalFieldOf("surface_biomes").forGetter(CavarynBiomeSource::surfaceParameters),
            Codec.INT.optionalFieldOf("surface_min_y", 320).forGetter(CavarynBiomeSource::surfaceMinY),
            OptionalBiomeOverlay.CODEC.listOf().optionalFieldOf("overlays", List.of()).forGetter(CavarynBiomeSource::overlays),
            RegistryOps.<Biome, CavarynBiomeSource>retrieveGetter(Registries.BIOME)
    ).apply(instance, CavarynBiomeSource::new));

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Optional<Climate.ParameterList<Holder<Biome>>> surfaceParameters;
    private final int surfaceMinY;
    private final int surfaceMinQuart;
    private final List<OptionalBiomeOverlay> overlays;
    private final List<OptionalBiomeOverlay.Resolved> resolvedOverlays;

    public CavarynBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, Optional<Climate.ParameterList<Holder<Biome>>> surfaceParameters,
                              int surfaceMinY, List<OptionalBiomeOverlay> overlays, HolderGetter<Biome> biomes) {
        this.parameters = parameters;
        this.surfaceParameters = surfaceParameters;
        this.surfaceMinY = surfaceMinY;
        this.surfaceMinQuart = surfaceParameters.isPresent() ? QuartPos.fromBlock(surfaceMinY) : Integer.MAX_VALUE;
        this.overlays = overlays;
        this.resolvedOverlays = OptionalBiomeOverlay.resolve(overlays, biomes);
    }

    public Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    public Optional<Climate.ParameterList<Holder<Biome>>> surfaceParameters() {
        return this.surfaceParameters;
    }

    public int surfaceMinY() {
        return this.surfaceMinY;
    }

    private List<OptionalBiomeOverlay> overlays() {
        return this.overlays;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(
                this.parameters.values().stream().map(Pair::getSecond),
                this.surfaceParameters.stream().flatMap(surface -> surface.values().stream()).map(Pair::getSecond),
                this.resolvedOverlays.stream().map(OptionalBiomeOverlay.Resolved::holder)
        ).flatMap(stream -> stream).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        return this.pick(y, sampler.sample(x, y, z));
    }

    private Holder<Biome> pick(int quartY, Climate.TargetPoint target) {
        if (quartY >= this.surfaceMinQuart) {
            return this.surfaceParameters.orElse(this.parameters).findValue(target);
        }
        return OptionalBiomeOverlay.apply(this.resolvedOverlays, this.parameters.findValue(target), target);
    }
}
