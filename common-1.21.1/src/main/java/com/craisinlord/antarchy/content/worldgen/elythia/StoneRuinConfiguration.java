package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StoneRuinConfiguration(ResourceLocation bricks, Optional<ResourceLocation> cracked, Optional<ResourceLocation> mossy,
                                     ResourceLocation pillar, ResourceLocation chiseled, ResourceLocation slab, ResourceLocation wall,
                                     boolean submerged, Optional<ResourceLocation> lootTable) implements FeatureConfiguration {
    public static final Codec<StoneRuinConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("bricks").forGetter(StoneRuinConfiguration::bricks),
            ResourceLocation.CODEC.optionalFieldOf("cracked").forGetter(StoneRuinConfiguration::cracked),
            ResourceLocation.CODEC.optionalFieldOf("mossy").forGetter(StoneRuinConfiguration::mossy),
            ResourceLocation.CODEC.fieldOf("pillar").forGetter(StoneRuinConfiguration::pillar),
            ResourceLocation.CODEC.fieldOf("chiseled").forGetter(StoneRuinConfiguration::chiseled),
            ResourceLocation.CODEC.fieldOf("slab").forGetter(StoneRuinConfiguration::slab),
            ResourceLocation.CODEC.fieldOf("wall").forGetter(StoneRuinConfiguration::wall),
            Codec.BOOL.optionalFieldOf("submerged", false).forGetter(StoneRuinConfiguration::submerged),
            ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(StoneRuinConfiguration::lootTable)
    ).apply(instance, StoneRuinConfiguration::new));
}
