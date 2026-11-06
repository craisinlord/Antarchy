package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class CranberryTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRANBERRY_TREE = ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation("antarchy", "cranberry_tree"));
    public static final AbstractTreeGrower CRANBERRY = new AbstractTreeGrower() {
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            return CRANBERRY_TREE;
        }
    };

    private CranberryTreeGrowers() {
    }
}
