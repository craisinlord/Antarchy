package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchyConfigCatalog;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.config.ConfigResetGuard;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AntarchyConfigModuleFabric {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Antarchy.MODID);
    private static final Path LEGACY_CONFIG_PATH = CONFIG_DIR.resolve("antarchy.json");
    private static final Map<String, SettingBinding> SETTINGS = buildBindings();

    private AntarchyConfigModuleFabric() {
    }

    public static void init() {
        ConfigResetGuard.migrateIfNeeded(
                CONFIG_DIR,
                ConfigSection.MOBS.path,
                ConfigSection.TOOLS.path,
                ConfigSection.MISC.path,
                LEGACY_CONFIG_PATH
        );
        JsonObject legacy = readConfig(LEGACY_CONFIG_PATH);
        Map<ConfigSection, JsonObject> loaded = new LinkedHashMap<>();
        Map<ConfigSection, JsonObject> normalized = new LinkedHashMap<>();

        for (ConfigSection section : ConfigSection.values()) {
            loaded.put(section, readConfig(section.path));
            normalized.put(section, new JsonObject());
        }

        for (Map.Entry<String, SettingBinding> entry : SETTINGS.entrySet()) {
            String key = entry.getKey();
            SettingBinding binding = entry.getValue();
            JsonObject loadedSection = loaded.get(binding.section);
            JsonElement element = loadedSection != null ? loadedSection.get(key) : null;
            if (element == null) {
                for (JsonObject otherSection : loaded.values()) {
                    if (otherSection != loadedSection && otherSection.has(key)) {
                        element = otherSection.get(key);
                        break;
                    }
                }
            }
            if (element == null && legacy != null) {
                element = legacy.get(key);
            }
            Object value = binding.readValue(element);
            binding.apply(value);
            normalized.get(binding.section).add(key, binding.toJson(value));
        }

        for (Map.Entry<ConfigSection, JsonObject> entry : normalized.entrySet()) {
            writeConfig(entry.getKey().path, entry.getValue());
        }
    }

    /**
     * Exposes the reflection-based getter/setter binding map (see {@link #buildBindings()}) so
     * other Fabric-side code — namely the optional Cloth Config screen builder — can reuse the
     * exact same set of persisted settings the JSON files already round-trip, instead of
     * hand-authoring a separate list. Read-only: callers get/set individual values through
     * {@link SettingBinding#getValue()} / {@link SettingBinding#setValue(Object)}.
     */
    public static Map<String, SettingBinding> getBindings() {
        return SETTINGS;
    }

    /**
     * Re-serializes the current live value of every binding back to its JSON file. Intended for
     * the optional Cloth Config screen's save handler: entries there update the live
     * {@link AntarchySettings} fields immediately through their own save consumers (same as
     * {@link SettingBinding#apply(Object)} does below), but nothing writes those changes to disk
     * until this runs — mirrors the persistence tail of {@link #init()} above.
     */
    public static void persist() {
        Map<ConfigSection, JsonObject> normalized = new LinkedHashMap<>();
        for (ConfigSection section : ConfigSection.values()) {
            normalized.put(section, new JsonObject());
        }

        for (Map.Entry<String, SettingBinding> entry : SETTINGS.entrySet()) {
            SettingBinding binding = entry.getValue();
            normalized.get(binding.section).add(entry.getKey(), binding.toJson(binding.getCurrentValue()));
        }

        for (Map.Entry<ConfigSection, JsonObject> entry : normalized.entrySet()) {
            writeConfig(entry.getKey().path, entry.getValue());
        }
    }

    private static JsonObject readConfig(Path path) {
        if (!Files.exists(path)) {
            return null;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed.isJsonObject()) {
                return parsed.getAsJsonObject();
            }
            Antarchy.LOGGER.warn("Fabric config at {} is not a JSON object, using defaults", path);
        } catch (IOException | JsonParseException e) {
            Antarchy.LOGGER.warn("Failed to read Fabric config at {}, using defaults", path, e);
        }

        return null;
    }

    private static void writeConfig(Path path, JsonObject root) {
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException e) {
            Antarchy.LOGGER.warn("Failed to write Fabric config at {}", path, e);
        }
    }

    private static Map<String, SettingBinding> buildBindings() {
        return AntarchyConfigCatalog.definitions().stream()
                .collect(LinkedHashMap::new, (bindings, definition) -> bindings.put(
                        definition.name(),
                        new SettingBinding(
                                definition.name(),
                                definition.getter(),
                                definition.setter(),
                                toConfigSection(definition.section()))), Map::putAll);
    }

    private static ConfigSection toConfigSection(AntarchyConfigCatalog.Section section) {
        return switch (section) {
            case MOBS -> ConfigSection.MOBS;
            case TOOLS -> ConfigSection.TOOLS;
            case MISC -> ConfigSection.MISC;
        };
    }

    /**
     * Public so the optional Cloth Config screen builder (fabric.client package) can group
     * entries into the same Mobs/Tools/Misc categories the JSON files already use.
     */
    public enum ConfigSection {
        MOBS("antarchy_mobs.json"),
        TOOLS("antarchy_tools.json"),
        MISC("antarchy_misc.json");

        private final Path path;

        ConfigSection(String fileName) {
            this.path = CONFIG_DIR.resolve(fileName);
        }

    }

    /**
     * Public so the optional Cloth Config screen builder (fabric.client package) can reuse this
     * exact binding — the same getter/setter pair and JSON section the persistence layer above
     * already drives — instead of a separate hand-authored entry list. The JSON-specific
     * machinery (readValue/apply/toJson/getCurrentValue) stays private and is reused internally
     * by the public getValue()/setValue() wrappers below, so there's exactly one code path for
     * "what is this setting's current value" and "how do I change it".
     */
    public static final class SettingBinding {
        private final String name;
        private final Method getter;
        private final Method setter;
        private final Class<?> setterType;
        private final ConfigSection section;

        private SettingBinding(String name, Method getter, Method setter, ConfigSection section) {
            this.name = name;
            this.getter = getter;
            this.setter = setter;
            this.setterType = setter.getParameterTypes()[0];
            this.section = section;
        }

        /** The AntarchySettings property name, e.g. {@code krakenHealth}. */
        public String getName() {
            return name;
        }

        /** Which of the three JSON files (Mobs/Tools/Misc) this setting persists to. */
        public ConfigSection getSection() {
            return section;
        }

        /** The primitive/String type of the setter's single parameter (boolean/int/double/float/String). */
        public Class<?> getType() {
            return setterType;
        }

        /** The setting's current live value, boxed (Boolean/Integer/Double/Float/String). */
        public Object getValue() {
            return getCurrentValue();
        }

        /** Applies a new value immediately via reflection, same as the JSON loader does at startup. */
        public void setValue(Object value) {
            apply(value);
        }

        private Object readValue(JsonElement element) {
            Object defaultValue = getCurrentValue();
            if (element == null || element.isJsonNull()) {
                return defaultValue;
            }

            try {
                if (setterType == boolean.class) {
                    return element.getAsBoolean();
                }
                if (setterType == int.class) {
                    return element.getAsInt();
                }
                if (setterType == double.class) {
                    return element.getAsDouble();
                }
                if (setterType == float.class) {
                    return element.getAsFloat();
                }
                if (setterType == String.class) {
                    return element.getAsString();
                }
            } catch (RuntimeException e) {
                Antarchy.LOGGER.warn("Invalid Fabric config value for {}, using default", name, e);
            }

            return defaultValue;
        }

        private void apply(Object value) {
            try {
                setter.invoke(null, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to apply config value for " + name, e);
            }
        }

        private JsonElement toJson(Object value) {
            return GSON.toJsonTree(value);
        }

        private Object getCurrentValue() {
            try {
                Object value = getter.invoke(null);
                if (setterType == String.class && value instanceof ResourceKey<?> resourceKey) {
                    return resourceKey.location().toString();
                }
                return value;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Failed to read config default for " + name, e);
            }
        }
    }
}
