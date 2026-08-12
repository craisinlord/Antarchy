package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ThoraxisFissureConfiguration(int minSpan, int maxSpan, int minRadius, int maxRadius, int maxBend, float branchChance) implements FeatureConfiguration {
    public static final Codec<ThoraxisFissureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 256).fieldOf("min_span").forGetter(ThoraxisFissureConfiguration::minSpan),
            Codec.intRange(1, 256).fieldOf("max_span").forGetter(ThoraxisFissureConfiguration::maxSpan),
            Codec.intRange(1, 8).fieldOf("min_radius").forGetter(ThoraxisFissureConfiguration::minRadius),
            Codec.intRange(1, 8).fieldOf("max_radius").forGetter(ThoraxisFissureConfiguration::maxRadius),
            Codec.intRange(0, 32).fieldOf("max_bend").forGetter(ThoraxisFissureConfiguration::maxBend),
            Codec.floatRange(0.0F, 1.0F).fieldOf("branch_chance").forGetter(ThoraxisFissureConfiguration::branchChance)
    ).apply(instance, ThoraxisFissureConfiguration::new));
}
