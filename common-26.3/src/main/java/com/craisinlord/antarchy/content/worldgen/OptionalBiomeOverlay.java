package com.craisinlord.antarchy.content.worldgen;

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
import net.minecraft.world.level.biome.Climate;

public record OptionalBiomeOverlay(
        ResourceKey<Biome> biome,
        List<ResourceKey<Biome>> replaces,
        Optional<Climate.Parameter> temperature,
        Optional<Climate.Parameter> humidity,
        Optional<Climate.Parameter> continentalness,
        Optional<Climate.Parameter> erosion,
        Optional<Climate.Parameter> depth,
        Optional<Climate.Parameter> weirdness
) {
    public static final Codec<OptionalBiomeOverlay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(OptionalBiomeOverlay::biome),
            ResourceKey.codec(Registries.BIOME).listOf().fieldOf("replaces").forGetter(OptionalBiomeOverlay::replaces),
            Climate.Parameter.CODEC.optionalFieldOf("temperature").forGetter(OptionalBiomeOverlay::temperature),
            Climate.Parameter.CODEC.optionalFieldOf("humidity").forGetter(OptionalBiomeOverlay::humidity),
            Climate.Parameter.CODEC.optionalFieldOf("continentalness").forGetter(OptionalBiomeOverlay::continentalness),
            Climate.Parameter.CODEC.optionalFieldOf("erosion").forGetter(OptionalBiomeOverlay::erosion),
            Climate.Parameter.CODEC.optionalFieldOf("depth").forGetter(OptionalBiomeOverlay::depth),
            Climate.Parameter.CODEC.optionalFieldOf("weirdness").forGetter(OptionalBiomeOverlay::weirdness)
    ).apply(instance, OptionalBiomeOverlay::new));

    public static List<Resolved> resolve(List<OptionalBiomeOverlay> overlays, HolderGetter<Biome> biomes) {
        return overlays.stream().flatMap(overlay -> biomes.get(overlay.biome())
                .map(holder -> new Resolved(holder, Set.copyOf(overlay.replaces()), overlay)).stream()).toList();
    }

    public static Holder<Biome> apply(List<Resolved> overlays, Holder<Biome> base, Climate.TargetPoint target) {
        for (Resolved overlay : overlays) {
            if (overlay.matches(base, target)) {
                return overlay.holder();
            }
        }
        return base;
    }

    private boolean contains(Climate.TargetPoint target) {
        return contains(this.temperature, target.temperature())
                && contains(this.humidity, target.humidity())
                && contains(this.continentalness, target.continentalness())
                && contains(this.erosion, target.erosion())
                && contains(this.depth, target.depth())
                && contains(this.weirdness, target.weirdness());
    }

    private static boolean contains(Optional<Climate.Parameter> range, long value) {
        return range.isEmpty() || range.get().distance(value) == 0L;
    }

    public record Resolved(Holder<Biome> holder, Set<ResourceKey<Biome>> replaces, OptionalBiomeOverlay overlay) {
        private boolean matches(Holder<Biome> base, Climate.TargetPoint target) {
            return base.is(this.replaces::contains) && this.overlay.contains(target);
        }
    }
}
