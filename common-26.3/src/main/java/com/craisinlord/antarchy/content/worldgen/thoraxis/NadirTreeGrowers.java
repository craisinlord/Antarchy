package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class NadirTreeGrowers {
    public static final ResourceKey<Feature> NADIR_TREE = featureKey("nadir_tree");

    private NadirTreeGrowers() {
    }

    private static ResourceKey<Feature> featureKey(String path) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
