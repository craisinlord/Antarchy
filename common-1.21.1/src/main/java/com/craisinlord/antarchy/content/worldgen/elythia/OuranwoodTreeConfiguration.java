package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record OuranwoodTreeConfiguration(
        BlockStateProvider trunkProvider,
        BlockStateProvider foliageProvider,
        IntProvider height,
        IntProvider trunkRadius,
        IntProvider canopyRadius,
        IntProvider canopyDepth,
        IntProvider buttressHeight,
        IntProvider trunkVineLength,
        boolean juvenile,
        float flyingSquirrelNestChance,
        float flyingSquirrelHollowChance,
        float waspNestChance,
        float caterpillarChrysalisChance
) implements FeatureConfiguration {
    public static final Codec<OuranwoodTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(OuranwoodTreeConfiguration::trunkProvider),
            BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(OuranwoodTreeConfiguration::foliageProvider),
            IntProvider.codec(8, 255).fieldOf("height").forGetter(OuranwoodTreeConfiguration::height),
            IntProvider.codec(1, 6).fieldOf("trunk_radius").forGetter(OuranwoodTreeConfiguration::trunkRadius),
            IntProvider.codec(2, 32).fieldOf("canopy_radius").forGetter(OuranwoodTreeConfiguration::canopyRadius),
            IntProvider.codec(3, 64).fieldOf("canopy_depth").forGetter(OuranwoodTreeConfiguration::canopyDepth),
            IntProvider.codec(0, 32).fieldOf("buttress_height").forGetter(OuranwoodTreeConfiguration::buttressHeight),
            IntProvider.codec(1, 16).fieldOf("trunk_vine_length").forGetter(OuranwoodTreeConfiguration::trunkVineLength),
            Codec.BOOL.optionalFieldOf("juvenile", false).forGetter(OuranwoodTreeConfiguration::juvenile),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("flying_squirrel_nest_chance", 0.0F).forGetter(OuranwoodTreeConfiguration::flyingSquirrelNestChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("flying_squirrel_hollow_chance", 1.0F).forGetter(OuranwoodTreeConfiguration::flyingSquirrelHollowChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("wasp_nest_chance", 0.0F).forGetter(OuranwoodTreeConfiguration::waspNestChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("caterpillar_chrysalis_chance", 0.0F).forGetter(OuranwoodTreeConfiguration::caterpillarChrysalisChance)
    ).apply(instance, OuranwoodTreeConfiguration::new));
}
