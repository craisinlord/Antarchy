package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ThoraxisTransitionThroatConfiguration(boolean large) implements FeatureConfiguration {
    public static final Codec<ThoraxisTransitionThroatConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("large").forGetter(ThoraxisTransitionThroatConfiguration::large)
    ).apply(instance, ThoraxisTransitionThroatConfiguration::new));
}
