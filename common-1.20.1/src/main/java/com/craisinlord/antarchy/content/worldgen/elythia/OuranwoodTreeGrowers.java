package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class OuranwoodTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OURANWOOD_YOUNG_TREE = featureKey("ouranwood_young_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OURANWOOD_MEDIUM_TREE = featureKey("ouranwood_medium_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OURANWOOD_LARGE_TREE = featureKey("ouranwood_large_tree");

    private OuranwoodTreeGrowers() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String path) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                new ResourceLocation(Antarchy.MODID, path)
        );
    }
}
