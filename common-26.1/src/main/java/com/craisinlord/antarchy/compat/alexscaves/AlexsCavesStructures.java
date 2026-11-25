package com.craisinlord.antarchy.compat.alexscaves;

import com.craisinlord.antarchy.Antarchy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class AlexsCavesStructures {
    public static final ResourceKey<Biome> FORLORN_HOLLOWS = key(AlexsCavesIntegration.MOD_ID, "forlorn_hollows");
    private static final Map<ResourceKey<Biome>, ResourceKey<Biome>> VARIANTS = Map.of(
            key(AlexsCavesIntegration.MOD_ID, "toxic_caves"), key(Antarchy.MODID, "toxic_caves"),
            FORLORN_HOLLOWS, key(Antarchy.MODID, "forlorn_hollows"),
            key(AlexsCavesIntegration.MOD_ID, "magnetic_caves"), key(Antarchy.MODID, "magnetic_caves"));
    private static final int SCAN_STEP = 4;
    private static final int EDGE = 8;

    private AlexsCavesStructures() {
    }

    public static boolean hooksApplied() {
        try {
            Method[] methods = Class.forName("com.github.alexmodguy.alexscaves.server.level.structure.AbstractCaveGenerationStructure").getDeclaredMethods();
            return Arrays.stream(methods).anyMatch(method -> method.getName().contains("antarchy$variantY"))
                    && Arrays.stream(methods).noneMatch(method -> method.getName().endsWith("ForVoronoi"));
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    private static ResourceKey<Biome> key(String namespace, String path) {
        return ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(namespace, path));
    }

    public static boolean matches(Holder<Biome> biome, ResourceKey<Biome> original) {
        ResourceKey<Biome> variant = VARIANTS.get(original);
        return variant != null && biome.is(variant);
    }

    public static int structureY(int originalY, Structure.GenerationContext context, ResourceKey<Biome> original) {
        ResourceKey<Biome> variant = VARIANTS.get(original);
        if (variant == null) {
            return originalY;
        }
        BiomeSource source = context.biomeSource();
        if (source.possibleBiomes().stream().noneMatch(biome -> biome.is(variant))) {
            return originalY;
        }
        Climate.Sampler sampler = context.randomState().sampler();
        ChunkPos chunk = context.chunkPos();
        int x = QuartPos.fromBlock(chunk.getMiddleBlockX());
        int z = QuartPos.fromBlock(chunk.getMiddleBlockZ());
        int minY = context.heightAccessor().getMinY() + EDGE;
        int maxY = context.heightAccessor().getMaxY() - EDGE;
        int bestStart = 0;
        int bestLength = 0;
        int runStart = 0;
        int runLength = 0;
        for (int y = minY; y <= maxY; y += SCAN_STEP) {
            if (source.getNoiseBiome(x, QuartPos.fromBlock(y), z, sampler).is(variant)) {
                if (runLength == 0) {
                    runStart = y;
                }
                runLength += SCAN_STEP;
                if (runLength > bestLength) {
                    bestStart = runStart;
                    bestLength = runLength;
                }
            } else {
                runLength = 0;
            }
        }
        return bestLength == 0 ? originalY : bestStart + bestLength / 2;
    }

    public static ResourceKey<Biome> replacementBiome(WorldGenLevel level, ResourceKey<Biome> original) {
        return level.getLevel().dimension() == Level.OVERWORLD ? original : VARIANTS.getOrDefault(original, original);
    }
}
