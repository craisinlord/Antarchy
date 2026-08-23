package com.craisinlord.antarchy.content.worldgen.truffalo;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class TruffaloTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> TRUFFALO_TREE = featureKey("truffalo_tree");

    private TruffaloTreeGrowers() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, path));
    }
}
