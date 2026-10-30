package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record TruffaloTreeConfiguration(
        net.minecraft.core.Holder<BlockStateProvider> trunkProvider,
        IntProvider height,
        int tuftRadius,
        int tuftHeight
) {
    public static final MapCodec<TruffaloTreeConfiguration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(TruffaloTreeConfiguration::trunkProvider),
            IntProviders.codec(4, 32).fieldOf("height").forGetter(TruffaloTreeConfiguration::height),
            Codec.INT.optionalFieldOf("tuft_radius", 1).forGetter(TruffaloTreeConfiguration::tuftRadius),
            Codec.INT.optionalFieldOf("tuft_height", 3).forGetter(TruffaloTreeConfiguration::tuftHeight)
    ).apply(instance, TruffaloTreeConfiguration::new));
}
