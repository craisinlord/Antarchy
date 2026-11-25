package com.craisinlord.antarchy.compat.alexscaves;

import com.craisinlord.antarchy.Antarchy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;

public final class AlexsCavesIntegration {
    public static final String MOD_ID = "alexscaves";
    private static final String CORE = "alexs_caves";
    private static final String NATIVE = "alexs_caves_native";
    private static final String CREATURE = "alexs_caves_creature";
    private static final String STRUCTURES = "alexs_caves_structures";
    private static volatile boolean enabled;

    private AlexsCavesIntegration() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static List<String> setup(boolean loaded, boolean configured, Predicate<String> hasResource, BooleanSupplier structureHooks) {
        List<String> packs = loaded && configured ? choosePacks(hasResource, structureHooks) : List.of();
        enabled = !packs.isEmpty();
        return packs;
    }

    private static List<String> choosePacks(Predicate<String> hasResource, BooleanSupplier structureHooks) {
        List<String> missing = missing(CORE, hasResource);
        if (!missing.isEmpty()) {
            Antarchy.LOGGER.warn("The installed Alex's Caves is missing content Antarchy needs, so its biomes won't generate in Antarchy's dimensions: {}", missing);
            return List.of();
        }
        List<String> packs = new ArrayList<>();
        packs.add(CORE);
        if (missing(NATIVE, hasResource).isEmpty()) {
            packs.add(NATIVE);
        } else {
            missing = missing(CREATURE, hasResource);
            if (!missing.isEmpty()) {
                Antarchy.LOGGER.warn("The installed Alex's Caves is missing mobs Antarchy needs, so its biomes won't generate in Antarchy's dimensions: {}", missing);
                return List.of();
            }
            packs.add(CREATURE);
        }
        boolean structures = missing(STRUCTURES, hasResource).isEmpty() && structureHooks.getAsBoolean();
        if (structures) {
            packs.add(STRUCTURES);
        }
        Antarchy.LOGGER.info("Alex's Caves biomes will generate in Antarchy's dimensions, {} its cave structures", structures ? "with" : "without");
        return List.copyOf(packs);
    }

    private static List<String> missing(String pack, Predicate<String> hasResource) {
        return readRequirements("/antarchy/" + pack + ".txt").stream().filter(line -> !isMet(line, hasResource)).toList();
    }

    private static List<String> readRequirements(String file) {
        try (InputStream stream = AlexsCavesIntegration.class.getResourceAsStream(file)) {
            if (stream == null) {
                return List.of("file " + file);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().map(String::trim).filter(line -> !line.isEmpty()).toList();
            }
        } catch (IOException exception) {
            return List.of("file " + file);
        }
    }

    private static boolean isMet(String line, Predicate<String> hasResource) {
        int split = line.indexOf(' ');
        if (split < 0) {
            return false;
        }
        String value = line.substring(split + 1);
        return switch (line.substring(0, split)) {
            case "data" -> hasResource.test(value);
            case "block" -> BuiltInRegistries.BLOCK.containsKey(new ResourceLocation(value));
            case "entity_type" -> BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(value));
            case "sound_event" -> BuiltInRegistries.SOUND_EVENT.containsKey(new ResourceLocation(value));
            case "particle_type" -> BuiltInRegistries.PARTICLE_TYPE.containsKey(new ResourceLocation(value));
            case "structure_type" -> BuiltInRegistries.STRUCTURE_TYPE.containsKey(new ResourceLocation(value));
            case "mob_category" -> Arrays.stream(MobCategory.values()).anyMatch(category -> category.getSerializedName().equals(value));
            default -> false;
        };
    }
}
