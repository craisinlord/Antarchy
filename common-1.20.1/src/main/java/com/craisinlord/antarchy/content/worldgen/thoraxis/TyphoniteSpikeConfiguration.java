package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record TyphoniteSpikeConfiguration(
        int floorToCeilingSearchRange,
        int radius,
        int maxColumnHeight,
        int heightDeviation,
        float chanceOfColumnAtMaxDistanceFromCenter,
        int maxDistanceFromCenterAffectingChanceOfColumn,
        int maxDistanceFromCenterAffectingHeightBias,
        int maxHeightDifference
) implements FeatureConfiguration {
    public static final Codec<TyphoniteSpikeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 128).fieldOf("floor_to_ceiling_search_range").forGetter(TyphoniteSpikeConfiguration::floorToCeilingSearchRange),
            Codec.intRange(1, 32).fieldOf("radius").forGetter(TyphoniteSpikeConfiguration::radius),
            Codec.intRange(1, 32).fieldOf("max_column_height").forGetter(TyphoniteSpikeConfiguration::maxColumnHeight),
            Codec.intRange(1, 16).fieldOf("height_deviation").forGetter(TyphoniteSpikeConfiguration::heightDeviation),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_column_at_max_distance_from_center").forGetter(TyphoniteSpikeConfiguration::chanceOfColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_chance_of_column").forGetter(TyphoniteSpikeConfiguration::maxDistanceFromCenterAffectingChanceOfColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(TyphoniteSpikeConfiguration::maxDistanceFromCenterAffectingHeightBias),
            Codec.intRange(0, 32).fieldOf("max_height_difference").forGetter(TyphoniteSpikeConfiguration::maxHeightDifference)
    ).apply(instance, TyphoniteSpikeConfiguration::new));
}
