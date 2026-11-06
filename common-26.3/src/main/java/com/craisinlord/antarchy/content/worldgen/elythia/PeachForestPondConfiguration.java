package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PeachForestPondConfiguration(
        int minRadius,
        int maxRadius,
        int minDepth,
        int maxDepth
) {
    public static final MapCodec<PeachForestPondConfiguration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, 32).fieldOf("min_radius").forGetter(PeachForestPondConfiguration::minRadius),
            Codec.intRange(1, 32).fieldOf("max_radius").forGetter(PeachForestPondConfiguration::maxRadius),
            Codec.intRange(1, 16).fieldOf("min_depth").forGetter(PeachForestPondConfiguration::minDepth),
            Codec.intRange(1, 16).fieldOf("max_depth").forGetter(PeachForestPondConfiguration::maxDepth)
    ).apply(instance, PeachForestPondConfiguration::new));
}
