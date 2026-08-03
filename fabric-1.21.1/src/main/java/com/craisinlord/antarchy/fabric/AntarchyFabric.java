package com.craisinlord.antarchy.fabric;
import com.craisinlord.antarchy.fabric.registry.AntarchyFabricEntities;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyGameRules;
import com.craisinlord.antarchy.content.entity.trades.DrTrayaurusTradeManager;
import com.craisinlord.antarchy.content.item.BloodCrystalShardItem;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricNetworking;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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
    private static final TagKey<net.minecraft.world.level.biome.Biome> OVERWORLD_ANT_NEST_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "overworld_ant_nest_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> RED_ANT_NEST_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "red_ant_nest_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> IS_ELYTHIA = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "is_elythia")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> APPLE_COW_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "apple_cow_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> GOLDEN_APPLE_COW_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "golden_apple_cow_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> FLYING_SQUIRREL_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flying_squirrel_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> FLOWER_FOREST_BUTTERFLY_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flower_forest_butterfly_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> FLOWER_FOREST_CATERPILLAR_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flower_forest_caterpillar_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> MILKWEED_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "milkweed_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> WILD_CORN_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "wild_corn_spawn_biomes")
    );
    private static final ResourceKey<PlacedFeature> BROWN_ANT_NEST = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "brown_ant_nest")
    );
    private static final ResourceKey<PlacedFeature> RAINBOW_ANT_NEST = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "rainbow_ant_nest")
    );
    private static final ResourceKey<PlacedFeature> TERMITE_NEST = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "termite_nest")
    );
    private static final ResourceKey<PlacedFeature> RED_ANT_NEST = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "red_ant_nest")
    );
    private static final ResourceKey<PlacedFeature> URANIUM_ORE = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "uranium_ore")
    );
    private static final ResourceKey<PlacedFeature> TITANIUM_ORE = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "titanium_ore")
    );
    private static final ResourceKey<PlacedFeature> FLOWER_FOREST_MILKWEED = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "flower_forest_milkweed")
    );
    private static final ResourceKey<PlacedFeature> CORN_PATCH = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "corn_patch")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> HAS_SEASHELL = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "has_seashell")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> HAS_RARE_SEASHELL = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "has_rare_seashell")
    );
    private static final ResourceKey<PlacedFeature> SEASHELL_PATCH = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "seashell_patch")
    );
    private static final ResourceKey<PlacedFeature> SEASHELL_PATCH_RARE = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "seashell_patch_rare")
    );
    @Override
    public void onInitialize() {
        AntarchyGameRules.bootstrap((name, category, defaultValue) ->
                GameRuleRegistry.register(name, category, GameRuleFactory.createBooleanRule(defaultValue))
        );
        AntarchyFabricNetworking.register();
        AntarchyFabricNetworking.bootstrapMultipartCommon();
        AntarchyConfigModuleFabric.init();
        AntarchyFabricContent.register();
        registerWorldgenFeatures();
        AntarchyFabricEvents.register();
        BloodglassManager.register();
        TigerEyeFabricManager.register();
        Antarchy.init();
        BloodCrystalShardItem.SYNC_BLOODGLASS = BloodglassManager::syncBloodglass;
        registerTradeReloadListener();
    }

    private static void registerTradeReloadListener() {
        DrTrayaurusTradeManager delegate = new DrTrayaurusTradeManager();
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "trayaurus_trades");
            }

            @Override
            public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
                                                    ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                                    Executor prepareExecutor, Executor applyExecutor) {
                return delegate.reload(barrier, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
            }
        });
    }

    private static final TagKey<net.minecraft.world.level.biome.Biome> CAVARYN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "is_cavaryn")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> DORRIE_OVERWORLD_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dorrie_overworld_spawn_biomes")
    );
    private static final TagKey<net.minecraft.world.level.biome.Biome> DORRIE_ELYTHIA_SPAWN_BIOMES = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "dorrie_elythia_spawn_biomes")
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
                AntarchyFabricEntities.MISSILE_SQUID.get(),
                2, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(OCTOPUS_BOMB_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricEntities.OCTOPUS_BOMB.get(),
                2, 1, 2
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(MANTIS_OVERWORLD_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricEntities.MANTIS.get(),
                8, 1, 1
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(MANTIS_OVERWORLD_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.MONSTER,
                AntarchyFabricEntities.ALPHA_MANTIS.get(),
                1, 1, 1
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(OVERWORLD_ANT_NEST_SPAWN_BIOMES),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                BROWN_ANT_NEST
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(OVERWORLD_ANT_NEST_SPAWN_BIOMES),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                RAINBOW_ANT_NEST
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(OVERWORLD_ANT_NEST_SPAWN_BIOMES),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                TERMITE_NEST
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(RED_ANT_NEST_SPAWN_BIOMES),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                RED_ANT_NEST
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                URANIUM_ORE
        );
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                TITANIUM_ORE
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(IS_ELYTHIA),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                URANIUM_ORE
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(IS_ELYTHIA),
                GenerationStep.Decoration.UNDERGROUND_ORES,
                TITANIUM_ORE
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(MILKWEED_SPAWN_BIOMES),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                FLOWER_FOREST_MILKWEED
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(WILD_CORN_SPAWN_BIOMES),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                CORN_PATCH
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(HAS_SEASHELL),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                SEASHELL_PATCH
        );
        BiomeModifications.addFeature(
                BiomeSelectors.tag(HAS_RARE_SEASHELL),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                SEASHELL_PATCH_RARE
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(APPLE_COW_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.CREATURE,
                AntarchyFabricEntities.APPLE_COW.get(),
                10, 2, 4
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(GOLDEN_APPLE_COW_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.CREATURE,
                AntarchyFabricEntities.GOLDEN_APPLE_COW.get(),
                1, 1, 3
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(FLYING_SQUIRREL_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.CREATURE,
                AntarchyFabricEntities.FLYING_SQUIRREL.get(),
                8, 1, 3
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(FLOWER_FOREST_BUTTERFLY_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.AMBIENT,
                AntarchyFabricEntities.BUTTERFLY.get(),
                28, 2, 5
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(FLOWER_FOREST_CATERPILLAR_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.CREATURE,
                AntarchyFabricEntities.CATERPILLAR.get(),
                24, 2, 4
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(DORRIE_OVERWORLD_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.CREATURE,
                AntarchyFabricEntities.DORRIE.get(),
                1, 1, 1
        );
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(DORRIE_ELYTHIA_SPAWN_BIOMES),
                net.minecraft.world.entity.MobCategory.CREATURE,
                AntarchyFabricEntities.DORRIE.get(),
                10, 1, 2
        );
    }
}
