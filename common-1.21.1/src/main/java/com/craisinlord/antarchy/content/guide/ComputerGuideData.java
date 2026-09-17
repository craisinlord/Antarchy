package com.craisinlord.antarchy.content.guide;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComputerGuideData extends SimplePreparableReloadListener<ComputerGuideData.LoadedData> {
    private static final ComputerGuideData INSTANCE = new ComputerGuideData();
    private static final ResourceLocation INTRODUCTION = ResourceLocation.fromNamespaceAndPath("antarchy", "introduction");
    private static volatile Map<ResourceLocation, Entry> entries = Map.of();
    private static volatile Map<ResourceLocation, Disk> disks = Map.of();

    private ComputerGuideData() {
    }

    public static ComputerGuideData instance() {
        return INSTANCE;
    }

    public static Entry entry(ResourceLocation id) {
        return entries.get(id);
    }

    public static Disk disk(ResourceLocation id) {
        return disks.get(id);
    }

    public static List<Entry> entriesFor(List<ResourceLocation> diskIds) {
        if (AntarchySettings.unlockAllArchives()) {
            Map<ResourceLocation, Entry> result = new LinkedHashMap<>();
            result.put(INTRODUCTION, introduction());
            result.putAll(entries);
            return result.values().stream().sorted(Comparator.comparing(entry -> entry.titleKey().toString())).toList();
        }
        Map<ResourceLocation, Entry> result = new LinkedHashMap<>();
        for (ResourceLocation diskId : diskIds) {
            if (INTRODUCTION.equals(diskId)) {
                result.put(INTRODUCTION, introduction());
                continue;
            }
            Disk disk = disks.get(diskId);
            if (disk == null) {
                continue;
            }
            for (ResourceLocation entryId : disk.entries()) {
                Entry entry = entries.get(entryId);
                if (entry != null) {
                    result.putIfAbsent(entryId, entry);
                }
            }
        }
        return result.values().stream().sorted(Comparator.comparing(entry -> entry.titleKey().toString())).toList();
    }

    private static Entry introduction() {
        return new Entry(INTRODUCTION, "article", "general", "guide.antarchy.entry.introduction.title",
                "guide.antarchy.entry.introduction.subtitle", List.of("guide.antarchy.entry.introduction.description"),
                "", "", "", "", "", "", "", 0, "", "", "", 0.0F, 1.0F);
    }

    /** Encodes the server data-pack view so remote clients do not need a duplicate asset copy. */
    public static String encodeNetworkSnapshot() {
        JsonObject root = new JsonObject();
        JsonArray entryArray = new JsonArray();
        entries.values().stream().sorted(Comparator.comparing(entry -> entry.id().toString())).forEach(entry -> {
            JsonObject object = new JsonObject();
            object.addProperty("id", entry.id().toString());
            object.addProperty("type", entry.type());
            object.addProperty("category", entry.category());
            object.addProperty("title", entry.titleKey());
            object.addProperty("subtitle", entry.subtitleKey());
            JsonArray descriptions = new JsonArray();
            entry.descriptionKeys().forEach(descriptions::add);
            object.add("description", descriptions);
            object.addProperty("item", entry.itemId());
            object.addProperty("entity", entry.entityId());
            object.addProperty("enchantment", entry.enchantmentId());
            object.addProperty("recipe", entry.recipeId());
            object.addProperty("structure", entry.structureId());
            object.addProperty("structure_tag", entry.structureTagId());
            object.addProperty("dimension", entry.dimensionId());
            object.addProperty("search_radius", entry.searchRadius());
            object.addProperty("cover_item", entry.coverItemId());
            object.addProperty("cover_entity", entry.coverEntityId());
            object.addProperty("cover_potion", entry.coverPotionId());
            object.addProperty("rotation", entry.rotation());
            object.addProperty("render_scale", entry.renderScale());
            entryArray.add(object);
        });
        root.add("entries", entryArray);

        JsonArray diskArray = new JsonArray();
        disks.values().stream().sorted(Comparator.comparing(disk -> disk.id().toString())).forEach(disk -> {
            JsonObject object = new JsonObject();
            object.addProperty("id", disk.id().toString());
            object.addProperty("category", disk.category());
            object.addProperty("title", disk.titleKey());
            JsonArray diskEntries = new JsonArray();
            disk.entries().forEach(id -> diskEntries.add(id.toString()));
            object.add("entries", diskEntries);
            object.addProperty("wallpaper", disk.wallpaper());
            diskArray.add(object);
        });
        root.add("disks", diskArray);
        return root.toString();
    }

    /** Applies the authoritative server snapshot on a multiplayer client. */
    public static void applyNetworkSnapshot(String json) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            Map<ResourceLocation, Entry> loadedEntries = new HashMap<>();
            Map<ResourceLocation, Disk> loadedDisks = new HashMap<>();
            JsonArray entryArray = root.getAsJsonArray("entries");
            if (entryArray != null) {
                for (JsonElement element : entryArray) {
                    JsonObject object = element.getAsJsonObject();
                    parseEntry(ResourceLocation.parse(string(object, "id", "")), object, loadedEntries);
                }
            }
            JsonArray diskArray = root.getAsJsonArray("disks");
            if (diskArray != null) {
                for (JsonElement element : diskArray) {
                    JsonObject object = element.getAsJsonObject();
                    parseDisk(ResourceLocation.parse(string(object, "id", "")), object, loadedDisks);
                }
            }
            entries = Map.copyOf(loadedEntries);
            disks = Map.copyOf(loadedDisks);
            Antarchy.LOGGER.info("Loaded {} computer archive entries and {} disks from the server", entries.size(), disks.size());
        } catch (RuntimeException exception) {
            Antarchy.LOGGER.error("Failed to decode computer archive data from the server", exception);
        }
    }

    @Override
    protected LoadedData prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Entry> loadedEntries = new HashMap<>();
        Map<ResourceLocation, Disk> loadedDisks = new HashMap<>();
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> resource : resourceManager.listResources("computer/entry", path -> path.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = resourceId(resource.getKey(), "computer/entry");
            try {
                JsonElement json = JsonParser.parseReader(resource.getValue().openAsReader());
                parseEntry(id, json, loadedEntries);
            } catch (Exception exception) {
                Antarchy.LOGGER.error("Failed to load computer archive entry {}", resource.getKey(), exception);
            }
        }
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> resource : resourceManager.listResources("computer/disk", path -> path.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = resourceId(resource.getKey(), "computer/disk");
            try {
                JsonElement json = JsonParser.parseReader(resource.getValue().openAsReader());
                parseDisk(id, json, loadedDisks);
            } catch (Exception exception) {
                Antarchy.LOGGER.error("Failed to load computer archive disk {}", resource.getKey(), exception);
            }
        }
        return new LoadedData(Map.copyOf(loadedEntries), Map.copyOf(loadedDisks));
    }

    @Override
    protected void apply(LoadedData data, ResourceManager resourceManager, ProfilerFiller profiler) {
        entries = data.entries();
        disks = data.disks();
    }

    private static ResourceLocation resourceId(ResourceLocation path, String directory) {
        String prefix = directory + "/";
        String value = path.getPath().startsWith(prefix) ? path.getPath().substring(prefix.length()) : path.getPath();
        if (value.endsWith(".json")) {
            value = value.substring(0, value.length() - 5);
        }
        return ResourceLocation.fromNamespaceAndPath(path.getNamespace(), value);
    }

    private static void parseEntry(ResourceLocation id, JsonElement element, Map<ResourceLocation, Entry> target) {
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        String type = string(object, "type", "article");
        String category = string(object, "category", "general");
        String title = string(object, "title", "guide.antarchy.missing_title");
        String subtitle = string(object, "subtitle", "");
        List<String> description = strings(object.get("description"));
        String item = string(object, "item", "");
        String entity = string(object, "entity", "");
        String enchantment = string(object, "enchantment", "");
        String recipe = string(object, "recipe", "");
        String structure = string(object, "structure", "");
        String structureTag = string(object, "structure_tag", "");
        String dimension = string(object, "dimension", "");
        int searchRadius = object.has("search_radius") && object.get("search_radius").isJsonPrimitive()
                ? Math.max(0, Math.min(1000, object.get("search_radius").getAsInt())) : 0;
        String coverItem = string(object, "cover_item", "");
        String coverEntity = string(object, "cover_entity", "");
        String coverPotion = string(object, "cover_potion", "");
        float rotation = object.has("rotation") && object.get("rotation").isJsonPrimitive()
                ? object.get("rotation").getAsFloat() : 0.0F;
        float renderScale = object.has("render_scale") && object.get("render_scale").isJsonPrimitive()
                ? object.get("render_scale").getAsFloat() : 1.0F;
        renderScale = Math.max(0.1F, Math.min(3.0F, renderScale));
        target.put(id, new Entry(id, type, category, title, subtitle, description, item, entity, enchantment, recipe, structure, structureTag, dimension, searchRadius,
                coverItem, coverEntity, coverPotion, rotation, renderScale));
    }

    private static void parseDisk(ResourceLocation id, JsonElement element, Map<ResourceLocation, Disk> target) {
        if (!element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        String category = string(object, "category", "general");
        String title = string(object, "title", "guide.antarchy.missing_disk_title");
        List<ResourceLocation> entryIds = new ArrayList<>();
        JsonElement entriesElement = object.get("entries");
        if (entriesElement != null && entriesElement.isJsonArray()) {
            for (JsonElement entry : entriesElement.getAsJsonArray()) {
                if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                    try {
                        entryIds.add(ResourceLocation.parse(entry.getAsString()));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        String wallpaper = string(object, "wallpaper", "");
        target.put(id, new Disk(id, category, title, List.copyOf(entryIds), wallpaper));
    }

    private static String string(JsonObject object, String key, String fallback) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() ? element.getAsString() : fallback;
    }

    private static List<String> strings(JsonElement element) {
        if (element == null) {
            return List.of();
        }
        if (element.isJsonPrimitive()) {
            return List.of(element.getAsString());
        }
        if (!element.isJsonArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (value.isJsonPrimitive()) {
                values.add(value.getAsString());
            } else if (value.isJsonObject()) {
                JsonObject render = value.getAsJsonObject();
                String item = string(render, "item", "");
                String entity = string(render, "entity", "");
                String enchantment = string(render, "enchantment", "");
                String recipe = string(render, "recipe", "");
                if (!item.isBlank()) values.add("@item:" + item);
                else if (!entity.isBlank()) values.add("@entity:" + entity);
                else if (!enchantment.isBlank()) values.add("@enchantment:" + enchantment);
                else if (!recipe.isBlank()) values.add("@recipe:" + recipe);
            }
        }
        return List.copyOf(values);
    }

    public record Entry(ResourceLocation id, String type, String category, String titleKey, String subtitleKey,
                         List<String> descriptionKeys, String itemId, String entityId, String enchantmentId,
                         String recipeId, String structureId, String structureTagId, String dimensionId, int searchRadius, String coverItemId,
                         String coverEntityId, String coverPotionId, float rotation, float renderScale) {
    }

    public record Disk(ResourceLocation id, String category, String titleKey, List<ResourceLocation> entries, String wallpaper) {
    }

    record LoadedData(Map<ResourceLocation, Entry> entries, Map<ResourceLocation, Disk> disks) {
    }
}
