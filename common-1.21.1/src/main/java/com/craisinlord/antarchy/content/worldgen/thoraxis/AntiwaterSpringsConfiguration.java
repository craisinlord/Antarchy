package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record AntiwaterSpringsConfiguration(
        BlockStateProvider state,
        int horizontalSearchRadius,
        int floorSearchBelow,
        int floorSearchAbove,
        int ventOffsetRadius,
        int baseClusters,
        int extraClusterChance,
        int minSmallVentCount,
        int maxSmallVentCount,
        int minLargeVentCount,
        int maxLargeVentCount,
        int largeVentClusterChance,
        int minDepth,
        int maxDepth,
        int largeClusterMinDepth,
        int largeClusterMaxDepth
) implements FeatureConfiguration {
    public static final Codec<AntiwaterSpringsConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state").forGetter(AntiwaterSpringsConfiguration::state),
            Codec.intRange(0, 64).fieldOf("horizontal_search_radius").forGetter(AntiwaterSpringsConfiguration::horizontalSearchRadius),
            Codec.intRange(1, 64).fieldOf("floor_search_below").forGetter(AntiwaterSpringsConfiguration::floorSearchBelow),
            Codec.intRange(1, 64).fieldOf("floor_search_above").forGetter(AntiwaterSpringsConfiguration::floorSearchAbove),
            Codec.intRange(0, 32).fieldOf("vent_offset_radius").forGetter(AntiwaterSpringsConfiguration::ventOffsetRadius),
            Codec.intRange(1, 16).fieldOf("base_clusters").forGetter(AntiwaterSpringsConfiguration::baseClusters),
            Codec.intRange(1, 64).fieldOf("extra_cluster_chance").forGetter(AntiwaterSpringsConfiguration::extraClusterChance),
            Codec.intRange(1, 16).fieldOf("min_small_vent_count").forGetter(AntiwaterSpringsConfiguration::minSmallVentCount),
            Codec.intRange(1, 16).fieldOf("max_small_vent_count").forGetter(AntiwaterSpringsConfiguration::maxSmallVentCount),
            Codec.intRange(1, 16).fieldOf("min_large_vent_count").forGetter(AntiwaterSpringsConfiguration::minLargeVentCount),
            Codec.intRange(1, 16).fieldOf("max_large_vent_count").forGetter(AntiwaterSpringsConfiguration::maxLargeVentCount),
            Codec.intRange(1, 64).fieldOf("large_vent_cluster_chance").forGetter(AntiwaterSpringsConfiguration::largeVentClusterChance),
            Codec.intRange(1, 32).fieldOf("min_depth").forGetter(AntiwaterSpringsConfiguration::minDepth),
            Codec.intRange(1, 32).fieldOf("max_depth").forGetter(AntiwaterSpringsConfiguration::maxDepth),
            Codec.intRange(1, 32).fieldOf("large_cluster_min_depth").forGetter(AntiwaterSpringsConfiguration::largeClusterMinDepth),
            Codec.intRange(1, 32).fieldOf("large_cluster_max_depth").forGetter(AntiwaterSpringsConfiguration::largeClusterMaxDepth)
    ).apply(instance, AntiwaterSpringsConfiguration::new));
}
