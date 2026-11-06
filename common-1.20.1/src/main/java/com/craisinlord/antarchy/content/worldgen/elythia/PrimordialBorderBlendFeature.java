package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public final class PrimordialBorderBlendFeature extends Feature<NoneFeatureConfiguration> {
    private static final TagKey<Biome> PRIMORDIAL_SURFACE_BIOMES = TagKey.create(Registries.BIOME,
            new ResourceLocation("antarchy", "primordial_surface_biomes"));
    private static final TagKey<Block> PRIMORDIAL_GROUND = TagKey.create(Registries.BLOCK,
            new ResourceLocation("antarchy", "primordial_ground"));
    private static final PerlinSimplexNoise CLUMP_NOISE = new PerlinSimplexNoise(
            new WorldgenRandom(new LegacyRandomSource(7041L)), List.of(0));
    private static final int[] SEARCH_RADII = {2, 4, 6, 8};
    private static final int[][] SEARCH_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int MAX_HEIGHT_DIFFERENCE = 6;
    private static final float FALLOFF_RANGE = 9.0F;
    private static final double CLUMP_SCALE = 0.18D;
    private static final float CLUMP_WEIGHT = 0.35F;
    private static final float SCATTER_WEIGHT = 0.25F;

    public PrimordialBorderBlendFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockState loam = BuiltInRegistries.BLOCK
                .get(new ResourceLocation("antarchy", "primordial_loam"))
                .defaultBlockState();
        int minX = context.origin().getX() & ~15;
        int minZ = context.origin().getZ() & ~15;
        boolean placed = false;
        for (int x = minX; x < minX + 16; x++) {
            for (int z = minZ; z < minZ + 16; z++) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                BlockPos pos = new BlockPos(x, y, z);
                BlockState top = level.getBlockState(pos);
                if ((!top.is(Blocks.GRASS_BLOCK) && !top.is(Blocks.MOSS_BLOCK)) || isPrimordialBiome(level, x, y, z)) {
                    continue;
                }
                int distance = distanceToPrimordialGround(level, x, y, z);
                if (distance > 0 && shouldBlend(x, z, distance, random)) {
                    level.setBlock(pos, loam, 2);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean shouldBlend(int x, int z, int distance, RandomSource random) {
        float strength = 1.0F - distance / FALLOFF_RANGE;
        float clump = (float) CLUMP_NOISE.getValue(x * CLUMP_SCALE, z * CLUMP_SCALE, false);
        float roll = 0.5F + clump * CLUMP_WEIGHT + (random.nextFloat() - 0.5F) * SCATTER_WEIGHT;
        return roll < strength;
    }

    private static int distanceToPrimordialGround(WorldGenLevel level, int x, int y, int z) {
        for (int radius : SEARCH_RADII) {
            for (int[] direction : SEARCH_DIRECTIONS) {
                if (isPrimordialGround(level, x + direction[0] * radius, y, z + direction[1] * radius)) {
                    return radius;
                }
            }
        }
        return -1;
    }

    private static boolean isPrimordialGround(WorldGenLevel level, int x, int y, int z) {
        int sampleY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        return Math.abs(sampleY - y) <= MAX_HEIGHT_DIFFERENCE
                && isPrimordialBiome(level, x, sampleY, z)
                && level.getBlockState(new BlockPos(x, sampleY, z)).is(PRIMORDIAL_GROUND);
    }

    private static boolean isPrimordialBiome(WorldGenLevel level, int x, int y, int z) {
        return level.getNoiseBiome(x >> 2, y >> 2, z >> 2).is(PRIMORDIAL_SURFACE_BIOMES);
    }
}
