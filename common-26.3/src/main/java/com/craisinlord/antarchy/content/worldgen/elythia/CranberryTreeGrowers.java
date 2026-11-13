package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class CranberryTreeGrowers {
    public static final ResourceKey<Feature> CRANBERRY_TREE = featureKey("cranberry_tree");
    public static final TreeGrower CRANBERRY = new TreeGrower(
            "cranberry",
            WeightedList.of(CRANBERRY_TREE),
            WeightedList.of(),
            WeightedList.of(),
            CRANBERRY_TREE
    );

    private CranberryTreeGrowers() {
    }

    private static ResourceKey<Feature> featureKey(String path) {
        return ResourceKey.create(Registries.FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
