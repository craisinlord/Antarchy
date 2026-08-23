package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record TruffaloTreeConfiguration(
        BlockStateProvider trunkProvider,
        IntProvider height
) implements FeatureConfiguration {
    public static final Codec<TruffaloTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(TruffaloTreeConfiguration::trunkProvider),
            IntProvider.codec(4, 9).fieldOf("height").forGetter(TruffaloTreeConfiguration::height)
    ).apply(instance, TruffaloTreeConfiguration::new));
}
