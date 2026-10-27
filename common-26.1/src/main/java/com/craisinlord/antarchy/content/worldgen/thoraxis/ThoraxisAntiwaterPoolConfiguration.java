package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ThoraxisAntiwaterPoolConfiguration(
        BlockStateProvider state,
        int minRadius,
        int maxRadius,
        int minDepth,
        int maxDepth,
        float irregularity
) implements FeatureConfiguration {
    public static final Codec<ThoraxisAntiwaterPoolConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("state").forGetter(ThoraxisAntiwaterPoolConfiguration::state),
            Codec.intRange(1, 64).fieldOf("min_radius").forGetter(ThoraxisAntiwaterPoolConfiguration::minRadius),
            Codec.intRange(1, 64).fieldOf("max_radius").forGetter(ThoraxisAntiwaterPoolConfiguration::maxRadius),
            Codec.intRange(1, 32).fieldOf("min_depth").forGetter(ThoraxisAntiwaterPoolConfiguration::minDepth),
            Codec.intRange(1, 32).fieldOf("max_depth").forGetter(ThoraxisAntiwaterPoolConfiguration::maxDepth),
            Codec.floatRange(0.0F, 1.0F).fieldOf("irregularity").forGetter(ThoraxisAntiwaterPoolConfiguration::irregularity)
    ).apply(instance, ThoraxisAntiwaterPoolConfiguration::new));
}
