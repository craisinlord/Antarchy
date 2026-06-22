package com.craisinlord.antarchy.fabric;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
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
import java.util.Arrays;
import java.util.Comparator;
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
        Map<String, Method> getters = new LinkedHashMap<>();
        for (Method method : AntarchySettings.class.getMethods()) {
            if (!isPublicStatic(method) || method.getParameterCount() != 0 || method.getReturnType() == Void.TYPE) {
                continue;
            }
            getters.put(method.getName(), method);
        }

        return Arrays.stream(AntarchySettings.class.getMethods())
                .filter(AntarchyConfigModuleFabric::isPublicStatic)
                .filter(method -> method.getName().startsWith("set"))
                .filter(method -> method.getParameterCount() == 1)
                .sorted(Comparator.comparing(Method::getName))
                .collect(LinkedHashMap::new, (bindings, setter) -> {
                    String propertyName = decapitalize(setter.getName().substring(3));
                    Method getter = getters.get(propertyName);
                    if (getter == null) {
                        return;
                    }
                    if (!isSupported(setter.getParameterTypes()[0], getter.getReturnType())) {
                        return;
                    }
                    bindings.put(propertyName, new SettingBinding(propertyName, getter, setter, ConfigSection.forProperty(propertyName)));
                }, Map::putAll);
    }

    private static boolean isPublicStatic(Method method) {
        int modifiers = method.getModifiers();
        return java.lang.reflect.Modifier.isPublic(modifiers) && java.lang.reflect.Modifier.isStatic(modifiers);
    }

    private static boolean isSupported(Class<?> setterType, Class<?> getterType) {
        if (setterType == boolean.class || setterType == int.class || setterType == double.class || setterType == float.class) {
            return getterType == setterType;
        }
        return setterType == String.class && ResourceKey.class.isAssignableFrom(getterType);
    }

    private static String decapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private enum ConfigSection {
        MOBS("antarchy_mobs.json"),
        TOOLS("antarchy_tools.json"),
        MISC("antarchy_misc.json");

        private final Path path;

        ConfigSection(String fileName) {
            this.path = CONFIG_DIR.resolve(fileName);
        }

        private static ConfigSection forProperty(String name) {
            if (isMiscSetting(name)) {
                return MISC;
            }
            if (isToolSetting(name)) {
                return TOOLS;
            }
            return MOBS;
        }

        private static boolean isMiscSetting(String name) {
            return name.equals("disableInfinityBookPortalCreation")
                    || name.equals("rainbowAntsLeadToInfinityDimensions")
                    || name.equals("hushweedSporeLifetimeSeconds")
                    || name.equals("elythiaFireflyParticlesEnabled")
                    || name.equals("duplicatorTreeEnabled")
                    || name.equals("glowingTorchflowers")
                    || name.equals("glowVinesUnderLeaves")
                    || name.equals("entitySpecificFireOverlayEnabled")
                    || name.equals("dreamSandEnabled")
                    || name.startsWith("dreamSand")
                    || name.equals("ichorWitherEnabled")
                    || name.startsWith("diamondMinecart");
        }

        private static boolean isToolSetting(String name) {
            return name.equals("sizeChangingRaysEnabled")
                    || name.startsWith("sizeRay")
                    || name.equals("shrinkingPotionDelta")
                    || name.equals("growthPotionDelta")
                    || name.startsWith("ultimate")
                    || name.startsWith("battleAxe")
                    || name.startsWith("bigBertha")
                    || name.startsWith("scorpionWhip")
                    || name.startsWith("bloodCrystal")
                    || name.startsWith("nightmareHelmet")
                    || name.startsWith("nightmareChestplate")
                    || name.startsWith("nightmareLeggings")
                    || name.startsWith("nightmareBoots")
                    || name.startsWith("nightmareArmorDreadAura")
                    || name.startsWith("nightmareSword")
                    || name.startsWith("basiliskDagger")
                    || name.startsWith("squidzooka")
                    || name.equals("invertProjectilesFromInvertedPlayers")
                    || name.startsWith("gravityGun")
                    || name.equals("potentNyxiteInvertedDurationSeconds")
                    || name.equals("corneaEarNightVisionSeconds")
                    || name.startsWith("mogglesVision")
                    || name.equals("ductTapeRepairPercentPerUse")
                    || name.startsWith("fallenKingCrown");
        }
    }

    private static final class SettingBinding {
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
