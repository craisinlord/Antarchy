package com.craisinlord.antarchy.config;

import net.minecraft.resources.ResourceKey;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AntarchyConfigCatalog {
    public enum Section {
        MOBS,
        TOOLS,
        MISC
    }

    public enum ValueType {
        BOOLEAN,
        INTEGER,
        DOUBLE,
        FLOAT,
        STRING
    }

    public record Definition(
            String name,
            Section section,
            ValueType valueType,
            Class<?> setterType,
            Method getter,
            Method setter
    ) {
    }

    private static final List<Definition> DEFINITIONS = buildDefinitions();

    private AntarchyConfigCatalog() {
    }

    public static List<Definition> definitions() {
        return DEFINITIONS;
    }

    public static Map<String, Definition> definitionsByName() {
        Map<String, Definition> result = new LinkedHashMap<>();
        for (Definition definition : DEFINITIONS) {
            result.put(definition.name(), definition);
        }
        return Map.copyOf(result);
    }

    private static List<Definition> buildDefinitions() {
        Map<String, Method> getters = Arrays.stream(AntarchySettings.class.getMethods())
                .filter(AntarchyConfigCatalog::isPublicStatic)
                .filter(method -> method.getParameterCount() == 0)
                .filter(method -> method.getReturnType() != Void.TYPE)
                .collect(java.util.stream.Collectors.toMap(
                        Method::getName,
                        method -> method,
                        (first, ignored) -> first,
                        LinkedHashMap::new));

        return Arrays.stream(AntarchySettings.class.getMethods())
                .filter(AntarchyConfigCatalog::isPublicStatic)
                .filter(method -> method.getName().startsWith("set"))
                .filter(method -> method.getParameterCount() == 1)
                .sorted(java.util.Comparator.comparing(Method::getName))
                .map(setter -> definitionFor(setter, getters))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private static Definition definitionFor(Method setter, Map<String, Method> getters) {
        String name = decapitalize(setter.getName().substring(3));
        Method getter = getters.get(name);
        if (getter == null) {
            return null;
        }

        Class<?> setterType = setter.getParameterTypes()[0];
        ValueType valueType = valueTypeFor(setterType, getter.getReturnType());
        if (valueType == null) {
            return null;
        }

        return new Definition(name, sectionFor(name), valueType, setterType, getter, setter);
    }

    private static ValueType valueTypeFor(Class<?> setterType, Class<?> getterType) {
        if (setterType == boolean.class && getterType == boolean.class) {
            return ValueType.BOOLEAN;
        }
        if (setterType == int.class && getterType == int.class) {
            return ValueType.INTEGER;
        }
        if (setterType == double.class && getterType == double.class) {
            return ValueType.DOUBLE;
        }
        if (setterType == float.class && getterType == float.class) {
            return ValueType.FLOAT;
        }
        if (setterType == String.class && ResourceKey.class.isAssignableFrom(getterType)) {
            return ValueType.STRING;
        }
        return null;
    }

    private static boolean isPublicStatic(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
    }

    private static String decapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }

    private static Section sectionFor(String name) {
        if (isMiscSetting(name)) {
            return Section.MISC;
        }
        if (isToolSetting(name)) {
            return Section.TOOLS;
        }
        return Section.MOBS;
    }

    private static boolean isMiscSetting(String name) {
        return name.equals("disableInfinityBookPortalCreation")
                || name.equals("rainbowAntsLeadToInfinityDimensions")
                || name.equals("permanentPortalsEnabled")
                || name.equals("permanentPortalsFlintAndSteelEnabled")
                || name.equals("elythiaPortalEnabled")
                || name.equals("thoraxisPortalEnabled")
                || name.equals("cavarynPortalEnabled")
                || name.equals("hushweedSporeLifetimeSeconds")
                || name.equals("elythiaFireflyParticlesEnabled")
                || name.equals("duplicatorTreeEnabled")
                || name.equals("glowVinesUnderLeaves")
                || name.equals("swingThroughGrassEnabled")
                || name.equals("fabricKeybindingConflictFixEnabled")
                || name.equals("experimentalSettingsPopupDisabled")
                || name.equals("unlockAllArchives")
                || name.equals("entitySpecificFireOverlayEnabled")
                || name.equals("dreamSandEnabled")
                || name.startsWith("dreamSand")
                || name.equals("ichorWitherEnabled")
                || name.startsWith("diamondMinecart")
                || name.startsWith("hoverboard");
    }

    private static boolean isToolSetting(String name) {
        return name.equals("sizeChangingRaysEnabled")
                || name.startsWith("sizeRay")
                || name.equals("shrinkingPotionDelta")
                || name.equals("growthPotionDelta")
                || name.startsWith("ultimate")
                || name.startsWith("battleAxe")
                || name.startsWith("bigBertha")
                || name.startsWith("attitudeAdjuster")
                || name.startsWith("scorpionWhip")
                || name.startsWith("bloodCrystal")
                || name.startsWith("nightmareHelmet")
                || name.startsWith("nightmareChestplate")
                || name.startsWith("nightmareLeggings")
                || name.startsWith("nightmareBoots")
                || name.startsWith("nightmareArmorDreadAura")
                || name.startsWith("primordialArmor")
                || name.startsWith("nightmareSword")
                || name.startsWith("basiliskDagger")
                || name.startsWith("squidzooka")
                || name.equals("invertProjectilesFromInvertedPlayers")
                || name.startsWith("gravityGun")
                || name.startsWith("critterCage")
                || name.startsWith("minersDream")
                || name.equals("potentNyxiteInvertedDurationSeconds")
                || name.equals("corneaEarNightVisionSeconds")
                || name.startsWith("american")
                || name.startsWith("mogglesVision")
                || name.equals("ductTapeRepairPercentPerUse")
                || name.startsWith("fallenKingCrown")
                || name.startsWith("royalArmor")
                || name.startsWith("royalGuardian")
                || name.startsWith("royalAssailant")
                || name.startsWith("royalWeapon");
    }
}
