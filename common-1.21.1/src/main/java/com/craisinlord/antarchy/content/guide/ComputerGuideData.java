package com.craisinlord.antarchy.content.guide;

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
        Map<ResourceLocation, Entry> result = new LinkedHashMap<>();
        for (ResourceLocation diskId : diskIds) {
            if (INTRODUCTION.equals(diskId)) {
                result.put(INTRODUCTION, new Entry(INTRODUCTION, "article", "general", "guide.antarchy.entry.introduction.title", "guide.antarchy.entry.introduction.subtitle", List.of("guide.antarchy.entry.introduction.description"), "", "", ""));
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

    @Override
    protected LoadedData prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Entry> loadedEntries = new HashMap<>();
        Map<ResourceLocation, Disk> loadedDisks = new HashMap<>();
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> resource : resourceManager.listResources("computer/entry", path -> path.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = resourceId(resource.getKey(), "computer/entry");
            try {
                JsonElement json = JsonParser.parseReader(resource.getValue().openAsReader());
                parseEntry(id, json, loadedEntries);
            } catch (Exception ignored) {
            }
        }
        for (Map.Entry<ResourceLocation, net.minecraft.server.packs.resources.Resource> resource : resourceManager.listResources("computer/disk", path -> path.getPath().endsWith(".json")).entrySet()) {
            ResourceLocation id = resourceId(resource.getKey(), "computer/disk");
            try {
                JsonElement json = JsonParser.parseReader(resource.getValue().openAsReader());
                parseDisk(id, json, loadedDisks);
            } catch (Exception ignored) {
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
        String structure = string(object, "structure", "");
        String dimension = string(object, "dimension", "");
        target.put(id, new Entry(id, type, category, title, subtitle, description, item, structure, dimension));
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
        target.put(id, new Disk(id, category, title, List.copyOf(entryIds)));
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
            }
        }
        return List.copyOf(values);
    }

    public record Entry(ResourceLocation id, String type, String category, String titleKey, String subtitleKey,
                         List<String> descriptionKeys, String itemId, String structureId, String dimensionId) {
    }

    public record Disk(ResourceLocation id, String category, String titleKey, List<ResourceLocation> entries) {
    }

    record LoadedData(Map<ResourceLocation, Entry> entries, Map<ResourceLocation, Disk> disks) {
    }
}
