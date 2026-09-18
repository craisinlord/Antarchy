package com.craisinlord.antarchy.content.worldgen;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.mixins.PoiTypesAccessor;
import com.craisinlord.integrated_api.mixins.structures.StructurePoolAccessor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Applies Antarchy-owned additions to existing vanilla jigsaw pools without replacing them. */
public final class VillagePoolAdditions {
    private static final Gson GSON = new Gson();
    private static final String DIRECTORY = "structure_pool_additions";

    private VillagePoolAdditions() {
    }

    public static void apply(MinecraftServer server) {
        registerComputerPoiStates(server);
        Registry<StructureTemplatePool> pools = server.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
        Registry<StructureProcessorList> processors = server.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);

        for (Map.Entry<ResourceLocation, Resource> entry : server.getResourceManager()
                .listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).entrySet()) {
            try (java.io.Reader reader = entry.getValue().openAsReader()) {
                JsonObject definition = GSON.fromJson(reader, JsonObject.class);
                addToPool(pools, processors, definition, entry.getKey());
            } catch (IOException | RuntimeException exception) {
                Antarchy.LOGGER.error("Failed to apply structure pool additions from {}", entry.getKey(), exception);
            }
        }
    }

    private static void registerComputerPoiStates(MinecraftServer server) {
        ResourceLocation poiId = ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "computer");
        Registry<net.minecraft.world.entity.ai.village.poi.PoiType> poiRegistry =
                server.registryAccess().registryOrThrow(Registries.POINT_OF_INTEREST_TYPE);
        Holder<net.minecraft.world.entity.ai.village.poi.PoiType> poi = poiRegistry.getHolderOrThrow(
                ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, poiId));
        Map<BlockState, Holder<net.minecraft.world.entity.ai.village.poi.PoiType>> stateMap =
                PoiTypesAccessor.antarchy$getTypeByState();
        net.minecraft.world.level.block.Block computer = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("antos", "computer"));
        for (BlockState state : computer.getStateDefinition().getPossibleStates()) {
            stateMap.put(state, poi);
        }
    }

    private static void addToPool(
            Registry<StructureTemplatePool> pools,
            Registry<StructureProcessorList> processors,
            JsonObject definition,
            ResourceLocation source
    ) {
        ResourceLocation poolId = ResourceLocation.parse(definition.get("pool").getAsString());
        StructureTemplatePool pool = pools.get(poolId);
        if (pool == null) {
            Antarchy.LOGGER.warn("Skipping structure pool addition {} because pool {} does not exist", source, poolId);
            return;
        }

        JsonObject elementDefinition = definition.getAsJsonObject("element");
        ResourceLocation location = ResourceLocation.parse(elementDefinition.get("location").getAsString());
        ResourceLocation processorId = ResourceLocation.parse(stringValue(
                elementDefinition, "processors", "minecraft:empty"));
        Holder<StructureProcessorList> processor = processors.getHolderOrThrow(
                ResourceKey.create(Registries.PROCESSOR_LIST, processorId));
        StructureTemplatePool.Projection projection = projection(stringValue(
                elementDefinition, "projection", "rigid"));
        int weight = definition.has("weight") ? definition.get("weight").getAsInt() : 1;
        if (weight < 1) {
            throw new IllegalArgumentException("weight must be positive");
        }

        StructurePoolElement poolElement = StructurePoolElement
                .legacy(location.toString(), processor)
                .apply(projection);
        StructurePoolAccessor accessor = (StructurePoolAccessor) pool;
        List<Pair<StructurePoolElement, Integer>> templates = new ArrayList<>(accessor.integratedapi_getRawTemplates());
        templates.add(Pair.of(poolElement, weight));
        accessor.integratedapi_setRawTemplates(templates);

        // StructureTemplatePool caches its weighted list in addition to the raw entries.
        // Keep both views synchronized so the new house is immediately eligible.
        ObjectArrayList<StructurePoolElement> expandedTemplates = new ObjectArrayList<>(accessor.integratedapi_getTemplates());
        for (int i = 0; i < weight; i++) {
            expandedTemplates.add(poolElement);
        }
        accessor.integratedapi_setTemplates(expandedTemplates);
    }

    private static StructureTemplatePool.Projection projection(String value) {
        return switch (value) {
            case "rigid" -> StructureTemplatePool.Projection.RIGID;
            case "terrain_matching" -> StructureTemplatePool.Projection.TERRAIN_MATCHING;
            default -> throw new IllegalArgumentException("Unknown structure pool projection: " + value);
        };
    }

    private static JsonElement primitive(String value) {
        return new com.google.gson.JsonPrimitive(value);
    }

    private static String stringValue(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }
}
