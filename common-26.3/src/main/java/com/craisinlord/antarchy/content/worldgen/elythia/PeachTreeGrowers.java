package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class PeachTreeGrowers {
    public static final ResourceKey<Feature> PEACH_LARGE_TREE = featureKey("peach_large_tree");
    public static final ResourceKey<Feature> PEACH_TREE = featureKey("peach_tree");
    public static final TreeGrower PEACH = new TreeGrower(
            "peach",
            WeightedList.of(PEACH_TREE),
            WeightedList.of(PEACH_LARGE_TREE),
            WeightedList.of(),
            PEACH_TREE
    );

    private PeachTreeGrowers() {
    }

    private static ResourceKey<Feature> featureKey(String path) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
