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
    private static final TagKey<net.minecraft.world.level.biome.Biome> MISSILE_SQUID_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "missile_squid_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> OCTOPUS_BOMB_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "octopus_bomb_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> MANTIS_OVERWORLD_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "mantis_overworld_spawn_biomes")
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

    private static final TagKey<net.minecraft.world.level.biome.Biome> CAVARYN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "is_cavaryn")
    );

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
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(MISSILE_SQUID_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricContent.MISSILE_SQUID.get(),
                2, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(OCTOPUS_BOMB_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricContent.OCTOPUS_BOMB.get(),
                2, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(MANTIS_OVERWORLD_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricContent.MANTIS.get(),
                8, 1, 1
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(MANTIS_OVERWORLD_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricContent.ALPHA_MANTIS.get(),
                1, 1, 1
        );
    }
}
