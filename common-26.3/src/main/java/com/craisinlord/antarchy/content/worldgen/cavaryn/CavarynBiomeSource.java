package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.content.worldgen.OptionalBiomeOverlay;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;

public final class CavarynBiomeSource extends BiomeSource {
    public static final MapCodec<CavarynBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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
    protected MapCodec<? extends BiomeSource> codec() {
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
    public BiomeResolver createResolver(Climate.Sampler sampler) {
        return (x, y, z) -> this.pick(y, sampler.sample(x, y, z));
    }

    @Override
    public BiomeResolver createResolverForChunk(Climate.Sampler sampler, int minQuartX, int minQuartY,
                                               int minQuartZ, int sizeX, int sizeY, int sizeZ) {
        DensityVolume volume = new DensityVolume(sizeX, sizeY, sizeZ, QuartPos.toBlock(minQuartX), QuartPos.toBlock(minQuartY),
                QuartPos.toBlock(minQuartZ), 4, 4, 4);
        DensityBuffer temperature = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer humidity = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer continentalness = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer erosion = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer depth = DensityBuffer.createUnpooled(volume.size());
        DensityBuffer weirdness = DensityBuffer.createUnpooled(volume.size());
        sampler.temperature().sampleVolume(temperature, volume);
        sampler.humidity().sampleVolume(humidity, volume);
        sampler.continentalness().sampleVolume(continentalness, volume);
        sampler.erosion().sampleVolume(erosion, volume);
        sampler.depth().sampleVolume(depth, volume);
        sampler.weirdness().sampleVolume(weirdness, volume);
        return (x, y, z) -> {
            int index = volume.indexUnchecked(x - minQuartX, y - minQuartY, z - minQuartZ);
            return this.pick(y, Climate.target(temperature.get(index), humidity.get(index), continentalness.get(index),
                    erosion.get(index), depth.get(index), weirdness.get(index)));
        };
    }

    private Holder<Biome> pick(int quartY, Climate.TargetPoint target) {
        if (quartY >= this.surfaceMinQuart) {
            return this.surfaceParameters.orElse(this.parameters).findValue(target);
        }
        return OptionalBiomeOverlay.apply(this.resolvedOverlays, this.parameters.findValue(target), target);
    }
}
