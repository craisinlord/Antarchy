package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record StoneRuinConfiguration(Identifier bricks, Optional<Identifier> cracked, Optional<Identifier> mossy,
                                     Identifier pillar, Identifier chiseled, Identifier slab, Identifier wall,
                                     boolean submerged, Optional<Identifier> lootTable) implements FeatureConfiguration {
    public static final Codec<StoneRuinConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("bricks").forGetter(StoneRuinConfiguration::bricks),
            Identifier.CODEC.optionalFieldOf("cracked").forGetter(StoneRuinConfiguration::cracked),
            Identifier.CODEC.optionalFieldOf("mossy").forGetter(StoneRuinConfiguration::mossy),
            Identifier.CODEC.fieldOf("pillar").forGetter(StoneRuinConfiguration::pillar),
            Identifier.CODEC.fieldOf("chiseled").forGetter(StoneRuinConfiguration::chiseled),
            Identifier.CODEC.fieldOf("slab").forGetter(StoneRuinConfiguration::slab),
            Identifier.CODEC.fieldOf("wall").forGetter(StoneRuinConfiguration::wall),
            Codec.BOOL.optionalFieldOf("submerged", false).forGetter(StoneRuinConfiguration::submerged),
            Identifier.CODEC.optionalFieldOf("loot_table").forGetter(StoneRuinConfiguration::lootTable)
    ).apply(instance, StoneRuinConfiguration::new));
}
