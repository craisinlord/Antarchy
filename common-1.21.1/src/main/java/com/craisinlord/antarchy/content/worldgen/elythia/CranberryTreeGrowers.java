package com.craisinlord.antarchy.content.worldgen.elythia;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class CranberryTreeGrowers {
    public static final ResourceKey<ConfiguredFeature<?, ?>> CRANBERRY_TREE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("antarchy", "cranberry_tree"));
    public static final TreeGrower CRANBERRY = new TreeGrower("cranberry", Optional.empty(), Optional.of(CRANBERRY_TREE), Optional.empty());

    private CranberryTreeGrowers() {
    }
}
