package com.craisinlord.antarchy.content.horde;

import com.craisinlord.antarchy.Antarchy;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;

public final class CavarynHordeDefinitions {
    private static final String DIRECTORY = "cavaryn_hordes";
    private static ResourceManager cachedManager;
    private static Map<ResourceLocation, Definition> definitions = Map.of();
    private static Set<EntityType<?>> configuredTypes = Set.of();

    private CavarynHordeDefinitions() {
    }

    public static Definition get(ServerLevel level, ResourceLocation id) {
        reloadIfNeeded(level.getServer().getResourceManager());
        return definitions.getOrDefault(id, Definition.EMPTY);
    }

    public static boolean isConfiguredHordeType(ServerLevel level, EntityType<?> type) {
        reloadIfNeeded(level.getServer().getResourceManager());
        return configuredTypes.contains(type);
    }

    private static void reloadIfNeeded(ResourceManager manager) {
        if (cachedManager == manager) {
            return;
        }

        Map<ResourceLocation, Definition> loaded = new HashMap<>();
        manager.listResources(DIRECTORY, location -> location.getPath().endsWith(".json")).forEach((location, resource) -> {
            String path = location.getPath();
            String idPath = path.substring((DIRECTORY + "/").length(), path.length() - ".json".length());
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(location.getNamespace(), idPath);
            try {
                loaded.put(id, Definition.fromJson(resource));
            } catch (Exception exception) {
                Antarchy.LOGGER.warn("Failed to load Cavaryn horde definition {}", location, exception);
            }
        });
        Set<EntityType<?>> types = new HashSet<>();
        for (Definition definition : loaded.values()) {
            definition.addTypesTo(types);
        }
        definitions = Map.copyOf(loaded);
        configuredTypes = Set.copyOf(types);
        cachedManager = manager;
    }

    public record Definition(int maxActive, List<Entry> entries) {
        private static final Definition EMPTY = new Definition(0, List.of());

        private static Definition fromJson(Resource resource) throws Exception {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                int maxActive = getInt(json, "max_active", 0);
                JsonArray rawEntries = json.getAsJsonArray("entries");
                if (rawEntries == null) {
                    return new Definition(maxActive, List.of());
                }

                List<Entry> entries = new ArrayList<>();
                for (JsonElement rawEntry : rawEntries) {
                    if (rawEntry.isJsonObject()) {
                        Entry entry = Entry.fromJson(rawEntry.getAsJsonObject());
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }
                }
                return new Definition(maxActive, List.copyOf(entries));
            }
        }

        public int resolvedMaxActive(ServerLevel level, int fallback) {
            return this.maxActive > 0 ? scaleSpawnCount(level, this.maxActive) : fallback;
        }

        public List<SpawnChoice> buildSpawnList(ServerLevel level, int players, boolean highAttention, RandomSource random) {
            if (level.getDifficulty() == Difficulty.PEACEFUL || this.entries.isEmpty()) {
                return List.of();
            }

            List<SpawnChoice> choices = new ArrayList<>();
            for (Entry entry : this.entries) {
                int count = entry.count(players, highAttention);
                count = scaleSpawnCount(level, count);
                for (int i = 0; i < count; i++) {
                    EntityType<?> type = entry.randomType(random);
                    if (type != null) {
                        choices.add(new SpawnChoice(type, entry.trackCompletion, entry.heavyArrivalEffect, entry.jerryStage));
                    }
                }
            }
            Collections.shuffle(choices);
            return choices;
        }

        public boolean shouldWarn() {
            for (Entry entry : this.entries) {
                if (entry.warning) {
                    return true;
                }
            }
            return false;
        }

        private void addTypesTo(Set<EntityType<?>> types) {
            for (Entry entry : this.entries) {
                entry.addTypesTo(types);
            }
        }
    }

    public record SpawnChoice(EntityType<?> type, boolean trackCompletion, boolean heavyArrivalEffect, String jerryStage) {
    }

    private static final class Entry {
        private final TagKey<EntityType<?>> entityTag;
        private final int weight;
        private final int baseCount;
        private final int highAttentionBaseCount;
        private final int perPlayerCount;
        private final int perPlayers;
        private final int perPlayersCount;
        private final int minCount;
        private final int maxCount;
        private final boolean warning;
        private final boolean trackCompletion;
        private final boolean heavyArrivalEffect;
        private final String jerryStage;

        private Entry(TagKey<EntityType<?>> entityTag, int weight, int baseCount, int highAttentionBaseCount, int perPlayerCount,
                      int perPlayers, int perPlayersCount, int minCount, int maxCount, boolean warning,
                      boolean trackCompletion, boolean heavyArrivalEffect, String jerryStage) {
            this.entityTag = entityTag;
            this.weight = Math.max(1, weight);
            this.baseCount = baseCount;
            this.highAttentionBaseCount = highAttentionBaseCount;
            this.perPlayerCount = perPlayerCount;
            this.perPlayers = perPlayers;
            this.perPlayersCount = perPlayersCount;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.warning = warning;
            this.trackCompletion = trackCompletion;
            this.heavyArrivalEffect = heavyArrivalEffect;
            this.jerryStage = jerryStage;
        }

        private static Entry fromJson(JsonObject json) {
            String tagId = getString(json, "entity_tag", "");
            ResourceLocation tagLocation = ResourceLocation.tryParse(tagId);
            if (tagLocation == null) {
                Antarchy.LOGGER.warn("Skipping Cavaryn horde entry with invalid entity_tag '{}'", tagId);
                return null;
            }

            return new Entry(
                    TagKey.create(Registries.ENTITY_TYPE, tagLocation),
                    getInt(json, "weight", 1),
                    getInt(json, "base_count", 0),
                    getInt(json, "high_attention_base_count", -1),
                    getInt(json, "per_player_count", 0),
                    getInt(json, "per_players", 0),
                    getInt(json, "per_players_count", 1),
                    getInt(json, "min_count", 0),
                    getInt(json, "max_count", Integer.MAX_VALUE),
                    getBoolean(json, "warning", false),
                    getBoolean(json, "track_completion", false),
                    getBoolean(json, "heavy_arrival_effect", false),
                    getString(json, "jerry_stage", "")
            );
        }

        private int count(int players, boolean highAttention) {
            int count = highAttention && this.highAttentionBaseCount >= 0 ? this.highAttentionBaseCount : this.baseCount;
            count += this.perPlayerCount * Math.max(0, players);
            if (this.perPlayers > 0) {
                count += (Math.max(0, players) / this.perPlayers) * this.perPlayersCount;
            }
            count = Math.max(this.minCount, count);
            return Math.min(this.maxCount, count);
        }

        private EntityType<?> randomType(RandomSource random) {
            Optional<HolderSet.Named<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTag(this.entityTag);
            if (tag.isEmpty()) {
                return null;
            }

            List<EntityType<?>> weightedTypes = new ArrayList<>();
            for (Holder<EntityType<?>> holder : tag.get()) {
                EntityType<?> type = holder.value();
                for (int i = 0; i < this.weight; i++) {
                    weightedTypes.add(type);
                }
            }
            if (weightedTypes.isEmpty()) {
                return null;
            }
            return weightedTypes.get(random.nextInt(weightedTypes.size()));
        }

        private void addTypesTo(Set<EntityType<?>> types) {
            Optional<HolderSet.Named<EntityType<?>>> tag = BuiltInRegistries.ENTITY_TYPE.getTag(this.entityTag);
            tag.ifPresent(entityTypes -> {
                for (Holder<EntityType<?>> holder : entityTypes) {
                    types.add(holder.value());
                }
            });
        }
    }

    private static int scaleSpawnCount(ServerLevel level, int baseCount) {
        if (baseCount <= 0 || level.getDifficulty() == Difficulty.PEACEFUL) {
            return 0;
        }
        float multiplier = switch (level.getDifficulty()) {
            case PEACEFUL -> 0.0F;
            case EASY -> 0.65F;
            case NORMAL -> 1.0F;
            case HARD -> 1.35F;
        };
        return Math.max(1, Mth.ceil(baseCount * multiplier));
    }

    private static String getString(JsonObject json, String key, String fallback) {
        return json.has(key) ? json.get(key).getAsString() : fallback;
    }

    private static int getInt(JsonObject json, String key, int fallback) {
        return json.has(key) ? json.get(key).getAsInt() : fallback;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }
}
