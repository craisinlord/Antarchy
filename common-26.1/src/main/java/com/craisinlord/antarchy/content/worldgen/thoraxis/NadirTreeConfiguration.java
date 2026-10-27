package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record NadirTreeConfiguration(
        BlockStateProvider trunkProvider,
        BlockStateProvider foliageProvider,
        IntProvider height,
        IntProvider bottomRadius,
        IntProvider branchCount,
        IntProvider branchLength,
        IntProvider veilRadius
) implements FeatureConfiguration {
    public static final Codec<NadirTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(NadirTreeConfiguration::trunkProvider),
            BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(NadirTreeConfiguration::foliageProvider),
            IntProviders.codec(6, 48).fieldOf("height").forGetter(NadirTreeConfiguration::height),
            IntProviders.codec(1, 6).fieldOf("bottom_radius").forGetter(NadirTreeConfiguration::bottomRadius),
            IntProviders.codec(1, 12).fieldOf("branch_count").forGetter(NadirTreeConfiguration::branchCount),
            IntProviders.codec(2, 16).fieldOf("branch_length").forGetter(NadirTreeConfiguration::branchLength),
            IntProviders.codec(1, 8).fieldOf("veil_radius").forGetter(NadirTreeConfiguration::veilRadius)
    ).apply(instance, NadirTreeConfiguration::new));
}
