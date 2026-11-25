package com.craisinlord.antarchy.fabric.compat;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesIntegration;
import com.craisinlord.antarchy.compat.alexscaves.AlexsCavesStructures;
import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;

public final class AlexsCavesPack {
    private static final String PACK_NAMESPACE = "mod_antarchy";
    private static final Set<ResourceKey<Biome>> CAVES = Set.of(biome("primordial_caves"), biome("candy_cavity"));
    private static final List<String> ORES = List.of("ore_coal_upper", "ore_coal_lower", "ore_iron_upper", "ore_iron_middle", "ore_iron_small",
            "ore_gold", "ore_gold_lower", "ore_redstone", "ore_redstone_lower", "ore_diamond", "ore_diamond_medium", "ore_diamond_large",
            "ore_diamond_buried", "ore_lapis", "ore_lapis_buried", "ore_copper", "uranium_ore", "titanium_ore");

    private AlexsCavesPack() {
    }

    public static void register() {
        Optional<ModContainer> alexsCaves = FabricLoader.getInstance().getModContainer(AlexsCavesIntegration.MOD_ID);
        Optional<ModContainer> antarchy = FabricLoader.getInstance().getModContainer(Antarchy.MODID);
        if (alexsCaves.isEmpty() || antarchy.isEmpty()) {
            return;
        }
        List<String> packs = AlexsCavesIntegration.setup(true, AntarchySettings.alexsCavesBiomes(),
                path -> alexsCaves.get().findPath(path).isPresent(), AlexsCavesStructures::hooksApplied);
        for (String pack : packs) {
            ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(PACK_NAMESPACE, pack), antarchy.get(),
                    Component.literal("Antarchy: Alex's Caves"), PackActivationType.ALWAYS_ENABLED);
        }
        if (packs.isEmpty()) {
            return;
        }
        for (String ore : ORES) {
            BiomeModifications.addFeature(context -> CAVES.contains(context.getBiomeKey()), GenerationStep.Decoration.UNDERGROUND_DECORATION,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia_" + ore)));
        }
    }

    private static ResourceKey<Biome> biome(String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(AlexsCavesIntegration.MOD_ID, path));
    }
}
