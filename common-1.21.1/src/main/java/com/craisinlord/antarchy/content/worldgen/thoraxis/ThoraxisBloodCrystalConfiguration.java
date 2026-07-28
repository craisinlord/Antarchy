package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record ThoraxisBloodCrystalConfiguration(
        BlockStateProvider baseState,
        BlockStateProvider crystalState,
        int minHeight,
        int maxHeight,
        int minBaseRadius,
        int maxBaseRadius,
        int tipRadius,
        int sway,
        float sideCrystalChance,
        float branchChance
) implements FeatureConfiguration {
    public static final Codec<ThoraxisBloodCrystalConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("base_state").forGetter(ThoraxisBloodCrystalConfiguration::baseState),
            BlockStateProvider.CODEC.fieldOf("crystal_state").forGetter(ThoraxisBloodCrystalConfiguration::crystalState),
            Codec.intRange(1, 256).fieldOf("min_height").forGetter(ThoraxisBloodCrystalConfiguration::minHeight),
            Codec.intRange(1, 256).fieldOf("max_height").forGetter(ThoraxisBloodCrystalConfiguration::maxHeight),
            Codec.intRange(1, 24).fieldOf("min_base_radius").forGetter(ThoraxisBloodCrystalConfiguration::minBaseRadius),
            Codec.intRange(1, 24).fieldOf("max_base_radius").forGetter(ThoraxisBloodCrystalConfiguration::maxBaseRadius),
            Codec.intRange(1, 24).fieldOf("tip_radius").forGetter(ThoraxisBloodCrystalConfiguration::tipRadius),
            Codec.intRange(0, 32).fieldOf("sway").forGetter(ThoraxisBloodCrystalConfiguration::sway),
            Codec.floatRange(0.0F, 1.0F).fieldOf("side_crystal_chance").forGetter(ThoraxisBloodCrystalConfiguration::sideCrystalChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("branch_chance").forGetter(ThoraxisBloodCrystalConfiguration::branchChance)
    ).apply(instance, ThoraxisBloodCrystalConfiguration::new));
}

