package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class PeachTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> PEACH_LARGE_TREE = featureKey("peach_large_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PEACH_TREE = featureKey("peach_tree");

    private PeachTreeGrowers() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> featureKey(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Antarchy.MODID, path));
    }
}
