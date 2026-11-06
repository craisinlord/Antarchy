package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StoneTorConfiguration(Identifier block, Optional<Identifier> accent, Identifier slab,
                                    int minHeight, int maxHeight, boolean submerged, boolean breach) implements FeatureConfiguration {
    public static final Codec<StoneTorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("block").forGetter(StoneTorConfiguration::block),
            Identifier.CODEC.optionalFieldOf("accent").forGetter(StoneTorConfiguration::accent),
            Identifier.CODEC.fieldOf("slab").forGetter(StoneTorConfiguration::slab),
            Codec.intRange(2, 32).fieldOf("min_height").forGetter(StoneTorConfiguration::minHeight),
            Codec.intRange(2, 32).fieldOf("max_height").forGetter(StoneTorConfiguration::maxHeight),
            Codec.BOOL.optionalFieldOf("submerged", false).forGetter(StoneTorConfiguration::submerged),
            Codec.BOOL.optionalFieldOf("breach", false).forGetter(StoneTorConfiguration::breach)
    ).apply(instance, StoneTorConfiguration::new));

    public static Block resolve(Identifier id) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(() -> new IllegalStateException("Missing Elythia stone block: " + id));
    }
}
