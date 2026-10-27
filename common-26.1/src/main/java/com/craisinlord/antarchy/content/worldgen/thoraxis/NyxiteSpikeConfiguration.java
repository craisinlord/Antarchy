package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record NyxiteSpikeConfiguration(
        int floorToCeilingSearchRange,
        int radius,
        int maxColumnHeight,
        int heightDeviation,
        float chanceOfColumnAtMaxDistanceFromCenter,
        int maxDistanceFromCenterAffectingChanceOfColumn,
        int maxDistanceFromCenterAffectingHeightBias,
        int maxHeightDifference
) implements FeatureConfiguration {
    public static final Codec<NyxiteSpikeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 128).fieldOf("floor_to_ceiling_search_range").forGetter(NyxiteSpikeConfiguration::floorToCeilingSearchRange),
            Codec.intRange(1, 32).fieldOf("radius").forGetter(NyxiteSpikeConfiguration::radius),
            Codec.intRange(1, 32).fieldOf("max_column_height").forGetter(NyxiteSpikeConfiguration::maxColumnHeight),
            Codec.intRange(1, 16).fieldOf("height_deviation").forGetter(NyxiteSpikeConfiguration::heightDeviation),
            Codec.floatRange(0.0F, 1.0F).fieldOf("chance_of_column_at_max_distance_from_center").forGetter(NyxiteSpikeConfiguration::chanceOfColumnAtMaxDistanceFromCenter),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_chance_of_column").forGetter(NyxiteSpikeConfiguration::maxDistanceFromCenterAffectingChanceOfColumn),
            Codec.intRange(1, 64).fieldOf("max_distance_from_center_affecting_height_bias").forGetter(NyxiteSpikeConfiguration::maxDistanceFromCenterAffectingHeightBias),
            Codec.intRange(0, 32).fieldOf("max_height_difference").forGetter(NyxiteSpikeConfiguration::maxHeightDifference)
    ).apply(instance, NyxiteSpikeConfiguration::new));
}
