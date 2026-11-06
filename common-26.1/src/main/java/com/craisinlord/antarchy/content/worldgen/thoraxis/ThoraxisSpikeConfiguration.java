package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ThoraxisSpikeConfiguration(
        Orientation orientation,
        int minLength,
        int maxLength,
        int baseRadius,
        int tipRadius,
        int sway,
        float branchChance
) implements FeatureConfiguration {
    public enum Orientation {
        UP,
        DOWN;

        public static Orientation fromName(String name) {
            for (Orientation value : values()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown Thoraxis spike orientation: " + name);
        }
    }

    public static final Codec<Orientation> ORIENTATION_CODEC = Codec.STRING.xmap(Orientation::fromName, Orientation::name);
    public static final Codec<ThoraxisSpikeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ORIENTATION_CODEC.fieldOf("orientation").forGetter(ThoraxisSpikeConfiguration::orientation),
            Codec.intRange(1, 256).fieldOf("min_length").forGetter(ThoraxisSpikeConfiguration::minLength),
            Codec.intRange(1, 256).fieldOf("max_length").forGetter(ThoraxisSpikeConfiguration::maxLength),
            Codec.intRange(1, 24).fieldOf("base_radius").forGetter(ThoraxisSpikeConfiguration::baseRadius),
            Codec.intRange(1, 24).fieldOf("tip_radius").forGetter(ThoraxisSpikeConfiguration::tipRadius),
            Codec.intRange(0, 32).fieldOf("sway").forGetter(ThoraxisSpikeConfiguration::sway),
            Codec.floatRange(0.0F, 1.0F).fieldOf("branch_chance").forGetter(ThoraxisSpikeConfiguration::branchChance)
    ).apply(instance, ThoraxisSpikeConfiguration::new));
}
