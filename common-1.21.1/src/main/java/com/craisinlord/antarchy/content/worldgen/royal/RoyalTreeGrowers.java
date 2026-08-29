package com.craisinlord.antarchy.content.worldgen.royal;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class RoyalTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROYAL_TREE_SMALL = featureKey("royal_tree_small");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROYAL_TREE_MEDIUM = featureKey("royal_tree_medium");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ROYAL_TREE_LARGE = featureKey("royal_tree_large");

    private RoyalTreeGrowers() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
