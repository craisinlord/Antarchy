package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ThoraxisDuneConfiguration(
        BlockStateProvider topState,
        BlockStateProvider underState,
        int minRadius,
        int maxRadius,
        int minHeight,
        int maxHeight,
        int underDepth,
        float irregularity
) implements FeatureConfiguration {
    public static final Codec<ThoraxisDuneConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("top_state").forGetter(ThoraxisDuneConfiguration::topState),
            BlockStateProvider.CODEC.fieldOf("under_state").forGetter(ThoraxisDuneConfiguration::underState),
            Codec.intRange(1, 64).fieldOf("min_radius").forGetter(ThoraxisDuneConfiguration::minRadius),
            Codec.intRange(1, 64).fieldOf("max_radius").forGetter(ThoraxisDuneConfiguration::maxRadius),
            Codec.intRange(1, 64).fieldOf("min_height").forGetter(ThoraxisDuneConfiguration::minHeight),
            Codec.intRange(1, 64).fieldOf("max_height").forGetter(ThoraxisDuneConfiguration::maxHeight),
            Codec.intRange(1, 16).fieldOf("under_depth").forGetter(ThoraxisDuneConfiguration::underDepth),
            Codec.floatRange(0.0F, 1.0F).fieldOf("irregularity").forGetter(ThoraxisDuneConfiguration::irregularity)
    ).apply(instance, ThoraxisDuneConfiguration::new));
}
