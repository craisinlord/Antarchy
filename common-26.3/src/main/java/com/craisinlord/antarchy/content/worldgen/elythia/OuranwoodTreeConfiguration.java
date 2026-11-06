package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record OuranwoodTreeConfiguration(
        net.minecraft.core.Holder<BlockStateProvider> trunkProvider,
        net.minecraft.core.Holder<BlockStateProvider> foliageProvider,
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
) {
    public static final MapCodec<OuranwoodTreeConfiguration> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(OuranwoodTreeConfiguration::trunkProvider),
            BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(OuranwoodTreeConfiguration::foliageProvider),
            IntProviders.codec(8, 255).fieldOf("height").forGetter(OuranwoodTreeConfiguration::height),
            IntProviders.codec(1, 6).fieldOf("trunk_radius").forGetter(OuranwoodTreeConfiguration::trunkRadius),
            IntProviders.codec(2, 32).fieldOf("canopy_radius").forGetter(OuranwoodTreeConfiguration::canopyRadius),
            IntProviders.codec(3, 64).fieldOf("canopy_depth").forGetter(OuranwoodTreeConfiguration::canopyDepth),
            IntProviders.codec(0, 32).fieldOf("buttress_height").forGetter(OuranwoodTreeConfiguration::buttressHeight),
            IntProviders.codec(1, 16).fieldOf("trunk_vine_length").forGetter(OuranwoodTreeConfiguration::trunkVineLength),
            Codec.BOOL.optionalFieldOf("juvenile", false).forGetter(OuranwoodTreeConfiguration::juvenile),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("flying_squirrel_nest_chance", 0.0F).forGetter(OuranwoodTreeConfiguration::flyingSquirrelNestChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("flying_squirrel_hollow_chance", 1.0F).forGetter(OuranwoodTreeConfiguration::flyingSquirrelHollowChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("wasp_nest_chance", 0.0F).forGetter(OuranwoodTreeConfiguration::waspNestChance),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("caterpillar_chrysalis_chance", 0.0F).forGetter(OuranwoodTreeConfiguration::caterpillarChrysalisChance)
    ).apply(instance, OuranwoodTreeConfiguration::new));
}
