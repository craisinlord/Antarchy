package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class OuranwoodTreeGrowers {
    public static final ResourceKey<Feature> OURANWOOD_YOUNG_TREE = featureKey("ouranwood_young_tree");
    public static final ResourceKey<Feature> OURANWOOD_MEDIUM_TREE = featureKey("ouranwood_medium_tree");
    public static final ResourceKey<Feature> OURANWOOD_LARGE_TREE = featureKey("ouranwood_large_tree");
    public static final TreeGrower OURANWOOD = new TreeGrower(
            "ouranwood",
            WeightedList.of(OURANWOOD_YOUNG_TREE),
            WeightedList.of(),
            WeightedList.of(),
            OURANWOOD_YOUNG_TREE
    );

    private OuranwoodTreeGrowers() {
    }

    private static ResourceKey<Feature> featureKey(String path) {
        return ResourceKey.create(
                Registries.FEATURE,
                Identifier.fromNamespaceAndPath(Antarchy.MODID, path)
        );
    }
}
