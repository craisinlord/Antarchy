package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record PeachTreeConfiguration(
        BlockStateProvider trunkProvider,
        BlockStateProvider foliageProvider,
        IntProvider height,
        IntProvider canopyRadius,
        IntProvider branchCount,
        int trunkWidth,
        float fruitChance,
        float topCanopyScale
) implements FeatureConfiguration {
    public static final Codec<PeachTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(PeachTreeConfiguration::trunkProvider),
            BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(PeachTreeConfiguration::foliageProvider),
            IntProvider.codec(4, 64).fieldOf("height").forGetter(PeachTreeConfiguration::height),
            IntProvider.codec(2, 16).fieldOf("canopy_radius").forGetter(PeachTreeConfiguration::canopyRadius),
            IntProvider.codec(1, 12).fieldOf("branch_count").forGetter(PeachTreeConfiguration::branchCount),
            Codec.intRange(1, 2).optionalFieldOf("trunk_width", 1).forGetter(PeachTreeConfiguration::trunkWidth),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("fruit_chance", 0.45F).forGetter(PeachTreeConfiguration::fruitChance),
            Codec.floatRange(0.2F, 1.2F).optionalFieldOf("top_canopy_scale", 0.8F).forGetter(PeachTreeConfiguration::topCanopyScale)
    ).apply(instance, PeachTreeConfiguration::new));
}
