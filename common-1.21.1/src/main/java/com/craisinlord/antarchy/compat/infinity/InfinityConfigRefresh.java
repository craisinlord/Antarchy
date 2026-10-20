package com.craisinlord.antarchy.compat.infinity;

import com.craisinlord.antarchy.Antarchy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class InfinityConfigRefresh {
    private static final Map<String, Registry<?>> CHECKED_FILES = Map.of(
            "mobs.json", BuiltInRegistries.ENTITY_TYPE,
            "items.json", BuiltInRegistries.ITEM,
            "effects.json", BuiltInRegistries.MOB_EFFECT
    );
    private static final Set<String> DISABLED_IDS = Set.of("antarchy:paralyzed");

    private InfinityConfigRefresh() {
    }

    public static void refreshIfStale(Path configDir) {
        Path modularDir = configDir.resolve("infinity").resolve("modular");
        Path invocationLock = modularDir.resolve("invocation.lock");
        if (!Files.exists(invocationLock)) {
            return;
        }

        Path antarchyDir = modularDir.resolve(Antarchy.MODID);
        List<String> staleIds = new ArrayList<>();
        CHECKED_FILES.forEach((fileName, registry) -> collectStaleIds(antarchyDir.resolve(fileName), registry, staleIds));
        if (staleIds.isEmpty()) {
            return;
        }

        try {
            Files.delete(invocationLock);
            Antarchy.LOGGER.info(
                    "[Antarchy] Infinite Dimensions config lists unregistered or disabled Antarchy content {}; its config will regenerate on next world join",
                    staleIds
            );
        } catch (Exception exception) {
            Antarchy.LOGGER.warn("[Antarchy] Failed to reset stale Infinite Dimensions config at {}", invocationLock, exception);
        }
    }

    private static void collectStaleIds(Path file, Registry<?> registry, List<String> staleIds) {
        if (!Files.exists(file)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject() || !root.getAsJsonObject().has("elements")) {
                return;
            }
            for (JsonElement element : root.getAsJsonObject().getAsJsonArray("elements")) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject entry = element.getAsJsonObject();
                if (!entry.has("key")) {
                    continue;
                }
                String key = entry.get("key").getAsString();
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null && Antarchy.MODID.equals(id.getNamespace()) && !registry.containsKey(id)) {
                    staleIds.add(key);
                } else if (DISABLED_IDS.contains(key) && (!entry.has("weight") || entry.get("weight").getAsDouble() > 0.0D)) {
                    staleIds.add(key);
                }
            }
        } catch (Exception exception) {
            Antarchy.LOGGER.warn("[Antarchy] Failed to read Infinite Dimensions config {}", file, exception);
        }
    }
}
