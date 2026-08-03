package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ChitinSpikeConfiguration(
        int floorToCeilingSearchRange,
        int radius,
        int maxColumnHeight,
        int heightDeviation,
        float chanceOfColumnAtMaxDistanceFromCenter,
        int maxDistanceFromCenterAffectingChanceOfColumn,
        int maxDistanceFromCenterAffectingHeightBias,
        int maxHeightDifference,
        float largeVariantChance,
        int largeVariantExtraRadius,
        int largeVariantExtraHeight,
        int largeVariantExtraHeightDeviation,
        float largeVariantSurfaceSpikeMultiplier
) implements FeatureConfiguration {
    public static final Codec<ChitinSpikeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 128).fieldOf("floor_to_ceiling_search_range").forGetter(ChitinSpikeConfiguration::floorToCeilingSearchRange),
            Codec.intRange(1, 32).fieldOf("radius").forGetter(ChitinSpikeConfiguration::radius),
            Codec.intRange(1, 32).fieldOf("max_column_height").forGetter(ChitinSpikeConfiguration::maxColumnHeight),
            Codec.intRange(1, 16).fieldOf("height_deviation").forGetter(ChitinSpikeConfiguration::heightDeviation),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_column_at_max_distance_from_center").forGetter(ChitinSpikeConfiguration::chanceOfColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_chance_of_column").forGetter(ChitinSpikeConfiguration::maxDistanceFromCenterAffectingChanceOfColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(ChitinSpikeConfiguration::maxDistanceFromCenterAffectingHeightBias),
            Codec.intRange(0, 32).fieldOf("max_height_difference").forGetter(ChitinSpikeConfiguration::maxHeightDifference),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("large_variant_chance", 0.0F).forGetter(ChitinSpikeConfiguration::largeVariantChance),
            Codec.intRange(0, 24).optionalFieldOf("large_variant_extra_radius", 0).forGetter(ChitinSpikeConfiguration::largeVariantExtraRadius),
            Codec.intRange(0, 32).optionalFieldOf("large_variant_extra_height", 0).forGetter(ChitinSpikeConfiguration::largeVariantExtraHeight),
            Codec.intRange(0, 16).optionalFieldOf("large_variant_extra_height_deviation", 0).forGetter(ChitinSpikeConfiguration::largeVariantExtraHeightDeviation),
            Codec.floatRange(1.0F, 8.0F).optionalFieldOf("large_variant_surface_spike_multiplier", 1.0F).forGetter(ChitinSpikeConfiguration::largeVariantSurfaceSpikeMultiplier)
    ).apply(instance, ChitinSpikeConfiguration::new));
}
