package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ThoraxisRibColumnsConfiguration(int minRibs, int maxRibs, int minLength, int maxLength, int minThickness, int maxThickness, int curveStrength, float clawGapChance) implements FeatureConfiguration {
    public static final Codec<ThoraxisRibColumnsConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 24).fieldOf("min_ribs").forGetter(ThoraxisRibColumnsConfiguration::minRibs),
            Codec.intRange(1, 24).fieldOf("max_ribs").forGetter(ThoraxisRibColumnsConfiguration::maxRibs),
            Codec.intRange(1, 64).fieldOf("min_length").forGetter(ThoraxisRibColumnsConfiguration::minLength),
            Codec.intRange(1, 64).fieldOf("max_length").forGetter(ThoraxisRibColumnsConfiguration::maxLength),
            Codec.intRange(1, 8).fieldOf("min_thickness").forGetter(ThoraxisRibColumnsConfiguration::minThickness),
            Codec.intRange(1, 8).fieldOf("max_thickness").forGetter(ThoraxisRibColumnsConfiguration::maxThickness),
            Codec.intRange(0, 16).fieldOf("curve_strength").forGetter(ThoraxisRibColumnsConfiguration::curveStrength),
            Codec.floatRange(0.0F, 1.0F).fieldOf("claw_gap_chance").forGetter(ThoraxisRibColumnsConfiguration::clawGapChance)
    ).apply(instance, ThoraxisRibColumnsConfiguration::new));
}
