package com.craisinlord.antarchy.content.worldgen.royal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record RoyalTreeConfiguration(
        BlockStateProvider trunkProvider,
        BlockStateProvider leavesProvider,
        IntProvider height,
        IntProvider trunkDrift,
        IntProvider canopyRadius,
        IntProvider canopyLayers,
        IntProvider branchCount,
        IntProvider strutCount
) implements FeatureConfiguration {
    public static final Codec<RoyalTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(RoyalTreeConfiguration::trunkProvider),
            BlockStateProvider.CODEC.fieldOf("leaves_provider").forGetter(RoyalTreeConfiguration::leavesProvider),
            IntProviders.codec(6, 48).fieldOf("height").forGetter(RoyalTreeConfiguration::height),
            IntProviders.codec(0, 12).fieldOf("trunk_drift").forGetter(RoyalTreeConfiguration::trunkDrift),
            IntProviders.codec(2, 14).fieldOf("canopy_radius").forGetter(RoyalTreeConfiguration::canopyRadius),
            IntProviders.codec(1, 8).fieldOf("canopy_layers").forGetter(RoyalTreeConfiguration::canopyLayers),
            IntProviders.codec(0, 8).fieldOf("branch_count").forGetter(RoyalTreeConfiguration::branchCount),
            IntProviders.codec(0, 4).fieldOf("strut_count").forGetter(RoyalTreeConfiguration::strutCount)
    ).apply(instance, RoyalTreeConfiguration::new));
}
