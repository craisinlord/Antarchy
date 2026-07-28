package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class OuranwoodTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> OURANWOOD_YOUNG_TREE = featureKey("ouranwood_young_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OURANWOOD_MEDIUM_TREE = featureKey("ouranwood_medium_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OURANWOOD_LARGE_TREE = featureKey("ouranwood_large_tree");
    public static final TreeGrower OURANWOOD = new TreeGrower(
            "ouranwood",
            Optional.empty(),
            Optional.of(OURANWOOD_YOUNG_TREE),
            Optional.empty()
    );

    private OuranwoodTreeGrowers() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String path) {
        return ResourceKey.create(
                Registries.CONFIGURED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path)
        );
    }
}
