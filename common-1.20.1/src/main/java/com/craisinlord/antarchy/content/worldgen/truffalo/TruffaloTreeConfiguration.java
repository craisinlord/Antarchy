package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record TruffaloTreeConfiguration(
        BlockStateProvider trunkProvider,
        IntProvider height,
        int tuftRadius,
        int tuftHeight
) implements FeatureConfiguration {
    public static final Codec<TruffaloTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(TruffaloTreeConfiguration::trunkProvider),
            IntProvider.codec(4, 18).fieldOf("height").forGetter(TruffaloTreeConfiguration::height),
            Codec.INT.optionalFieldOf("tuft_radius", 1).forGetter(TruffaloTreeConfiguration::tuftRadius),
            Codec.INT.optionalFieldOf("tuft_height", 3).forGetter(TruffaloTreeConfiguration::tuftHeight)
    ).apply(instance, TruffaloTreeConfiguration::new));
}
