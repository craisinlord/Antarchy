package com.craisinlord.antarchy.content.worldgen.royal;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class RoyalTreeGrowers {
    public static final ResourceKey<Feature> ROYAL_TREE_SMALL = featureKey("royal_tree_small");
    public static final ResourceKey<Feature> ROYAL_TREE_MEDIUM = featureKey("royal_tree_medium");
    public static final ResourceKey<Feature> ROYAL_TREE_LARGE = featureKey("royal_tree_large");

    private RoyalTreeGrowers() {
    }

    private static ResourceKey<Feature> featureKey(String path) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
