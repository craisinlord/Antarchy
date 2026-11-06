package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class CranberryTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRANBERRY_TREE = featureKey("cranberry_tree");
    public static final TreeGrower CRANBERRY = new TreeGrower(
            "cranberry",
            Optional.empty(),
            Optional.of(CRANBERRY_TREE),
            Optional.empty()
    );

    private CranberryTreeGrowers() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
