package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public record ElythiaCaveOverlay(
        ResourceKey<Biome> biome,
        Optional<ResourceKey<Biome>> requires,
        Optional<List<ResourceKey<Biome>>> under,
        List<ResourceKey<Biome>> notUnder,
        double minRegion,
        double maxRegion
) {
    public static final Codec<ElythiaCaveOverlay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(ElythiaCaveOverlay::biome),
            ResourceKey.codec(Registries.BIOME).optionalFieldOf("requires").forGetter(ElythiaCaveOverlay::requires),
            ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("under").forGetter(ElythiaCaveOverlay::under),
            ResourceKey.codec(Registries.BIOME).listOf().optionalFieldOf("not_under", List.of()).forGetter(ElythiaCaveOverlay::notUnder),
            Codec.DOUBLE.optionalFieldOf("min_region", 0.0D).forGetter(ElythiaCaveOverlay::minRegion),
            Codec.DOUBLE.optionalFieldOf("max_region", 1.0D).forGetter(ElythiaCaveOverlay::maxRegion)
    ).apply(instance, ElythiaCaveOverlay::new));

    public static List<Resolved> resolve(List<ElythiaCaveOverlay> overlays, HolderGetter<Biome> biomes) {
        return overlays.stream().filter(overlay -> overlay.requires().map(key -> biomes.get(key).isPresent()).orElse(true))
                .flatMap(overlay -> biomes.get(overlay.biome()).map(holder -> new Resolved(holder, overlay.under().map(Set::copyOf), Set.copyOf(overlay.notUnder()),
                        overlay.minRegion(), overlay.maxRegion())).stream()).toList();
    }

    public record Resolved(Holder<Biome> holder, Optional<Set<ResourceKey<Biome>>> under, Set<ResourceKey<Biome>> notUnder,
                           double minRegion, double maxRegion) {
        public boolean allowedUnder(Holder<Biome> surface) {
            return (this.under.isEmpty() || surface.is(this.under.get()::contains)) && !surface.is(this.notUnder::contains);
        }

        public boolean inRegion(double region) {
            return region >= this.minRegion && region <= this.maxRegion;
        }
    }
}
