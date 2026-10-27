package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class TruffaloTreeGrowers {
    public static final ResourceKey<Feature> TRUFFALO_TREE = featureKey("truffalo_tree");

    private TruffaloTreeGrowers() {
    }

    private static ResourceKey<Feature> featureKey(String path) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
