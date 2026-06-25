package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.BloodCrystalShardItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class AntarchyFabric implements ModInitializer {
    private static final TagKey<net.minecraft.world.level.biome.Biome> SHELLSTONE_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "has_shellstone")
    );
    private static final ResourceKey<PlacedFeature> SHELLSTONE_UPPER = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "shellstone_upper")
    );
    private static final ResourceKey<PlacedFeature> SHELLSTONE_LOWER = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "shellstone_lower")
    );

    @Override
    public void onInitialize() {
        AntarchyFabricNetworking.register();
        AntarchyConfigModuleFabric.init();
        AntarchyFabricContent.register();
        registerWorldgenFeatures();
        AntarchyFabricEvents.register();
        BloodglassManager.register();
        Antarchy.init();
        BloodCrystalShardItem.SYNC_BLOODGLASS = BloodglassManager::syncBloodglass;
    }

    private static void registerWorldgenFeatures() {
        BiomeModifications.addFeature(
                BiomeSelectors.tag(SHELLSTONE_BIOMES),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                SHELLSTONE_UPPER
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(SHELLSTONE_BIOMES),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                SHELLSTONE_LOWER
        );
    }
}
