package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StoneTorConfiguration(ResourceLocation block, Optional<ResourceLocation> accent, ResourceLocation slab,
                                    int minHeight, int maxHeight, boolean submerged) implements FeatureConfiguration {
    public static final Codec<StoneTorConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("block").forGetter(StoneTorConfiguration::block),
            ResourceLocation.CODEC.optionalFieldOf("accent").forGetter(StoneTorConfiguration::accent),
            ResourceLocation.CODEC.fieldOf("slab").forGetter(StoneTorConfiguration::slab),
            Codec.intRange(2, 32).fieldOf("min_height").forGetter(StoneTorConfiguration::minHeight),
            Codec.intRange(2, 32).fieldOf("max_height").forGetter(StoneTorConfiguration::maxHeight),
            Codec.BOOL.optionalFieldOf("submerged", false).forGetter(StoneTorConfiguration::submerged)
    ).apply(instance, StoneTorConfiguration::new));

    public static Block resolve(ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(() -> new IllegalStateException("Missing Elythia stone block: " + id));
    }
}
