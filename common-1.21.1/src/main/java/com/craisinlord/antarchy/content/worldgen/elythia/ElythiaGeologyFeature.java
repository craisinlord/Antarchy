package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ElythiaGeologyFeature extends Feature<NoneFeatureConfiguration> {
    public static final TagKey<Block> NATURAL_STONE = TagKey.create(Registries.BLOCK, id("elythia_natural_stone"));
    private static final TagKey<Biome> ARKROSE_BIOMES = TagKey.create(Registries.BIOME, id("arkrose_biomes"));
    private static final TagKey<Biome> CONGLOMERATE_BIOMES = TagKey.create(Registries.BIOME, id("conglomerate_biomes"));
    private static final int LENS_CELL = 32;
    private static final int LENS_HEIGHT = 24;
    private static final int MAX_RADIUS = 11;

    public ElythiaGeologyFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int startX = context.origin().getX() & ~15;
        int startZ = context.origin().getZ() & ~15;
        BlockState rockite = state("rockite");
        boolean placed = placeLenses(level, startX, startZ, rockite, state("zebra_stone"), 96, 256, 0x6D1C7B3AL, false);
        placed |= placeLenses(level, startX, startZ, rockite, state("arkrose"), 40, level.getMaxBuildHeight() - 1, 0x473A2D91L, true);
        placed |= placeBeds(level, startX, startZ, rockite, state("tidestone"), state("conglomerate"), state("shellstone"), state("loam"));
        return placed;
    }

    private static boolean placeLenses(WorldGenLevel level, int startX, int startZ, BlockState rockite,
                                       BlockState replacement, int lowerY, int upperY, long salt, boolean peachOnly) {
        int minY = Math.max(level.getMinBuildHeight(), lowerY);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, upperY);
        if (minY > maxY) {
            return false;
        }
        long seed = level.getSeed() ^ salt;
        boolean placed = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int cellX = Math.floorDiv(startX - MAX_RADIUS, LENS_CELL); cellX <= Math.floorDiv(startX + 15 + MAX_RADIUS, LENS_CELL); cellX++) {
            for (int cellZ = Math.floorDiv(startZ - MAX_RADIUS, LENS_CELL); cellZ <= Math.floorDiv(startZ + 15 + MAX_RADIUS, LENS_CELL); cellZ++) {
                for (int cellY = Math.floorDiv(minY - 4, LENS_HEIGHT); cellY <= Math.floorDiv(maxY + 4, LENS_HEIGHT); cellY++) {
                    long key = hash(seed, cellX, cellY, cellZ);
                    if (unit(key) >= 0.55D) {
                        continue;
                    }
                    int centerX = cellX * LENS_CELL + (int) (unit(mix(key + 1)) * LENS_CELL);
                    int centerY = cellY * LENS_HEIGHT + (int) (unit(mix(key + 2)) * LENS_HEIGHT);
                    int centerZ = cellZ * LENS_CELL + (int) (unit(mix(key + 3)) * LENS_CELL);
                    int radiusX = 6 + (int) (unit(mix(key + 4)) * 6);
                    int radiusY = 2 + (int) (unit(mix(key + 5)) * 3);
                    int radiusZ = 6 + (int) (unit(mix(key + 6)) * 6);
                    for (int x = Math.max(startX, centerX - radiusX); x <= Math.min(startX + 15, centerX + radiusX); x++) {
                        double dx = (x - centerX) / (double) radiusX;
                        for (int z = Math.max(startZ, centerZ - radiusZ); z <= Math.min(startZ + 15, centerZ + radiusZ); z++) {
                            double dz = (z - centerZ) / (double) radiusZ;
                            double horizontal = dx * dx + dz * dz;
                            if (horizontal > 1.1D) {
                                continue;
                            }
                            for (int y = Math.max(minY, centerY - radiusY); y <= Math.min(maxY, centerY + radiusY); y++) {
                                double dy = (y - centerY) / (double) radiusY;
                                if (horizontal + dy * dy > 0.92D + unit(hash(key, x, y, z)) * 0.16D) {
                                    continue;
                                }
                                cursor.set(x, y, z);
                                if (peachOnly && !level.getBiome(cursor).is(ARKROSE_BIOMES)) {
                                    continue;
                                }
                                if (level.getBlockState(cursor).is(rockite.getBlock())) {
                                    level.setBlock(cursor, replacement, 2);
                                    placed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return placed;
    }

    private static boolean placeBeds(WorldGenLevel level, int startX, int startZ, BlockState rockite,
                                     BlockState tidestone, BlockState conglomerate, BlockState shellstone, BlockState loam) {
        boolean placed = false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long seed = level.getSeed();
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int floorY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                if (floorY <= level.getMinBuildHeight() || floorY >= level.getMaxBuildHeight() - 1) {
                    continue;
                }
                cursor.set(x, floorY + 1, z);
                if (level.getFluidState(cursor).is(FluidTags.WATER)) {
                    placed |= placeTidestoneColumn(level, x, floorY, z, rockite, tidestone, shellstone, cursor);
                }
                cursor.set(x, floorY, z);
                if (!level.getBiome(cursor).is(CONGLOMERATE_BIOMES)) {
                    continue;
                }
                int bandStart = -1;
                int thickness = 2 + (int) (unit(hash(seed ^ 0x38A1BC5DL, x, 0, z)) * 4);
                for (int depth = 1; depth <= 24; depth++) {
                    int y = floorY - depth;
                    if (y < level.getMinBuildHeight()) {
                        break;
                    }
                    cursor.set(x, y, z);
                    BlockState current = level.getBlockState(cursor);
                    boolean substrate = current.is(rockite.getBlock()) || current.is(loam.getBlock()) || current.is(Blocks.DIRT)
                            || current.is(Blocks.COARSE_DIRT) || current.is(Blocks.ROOTED_DIRT);
                    if (bandStart < 0 && substrate) {
                        bandStart = depth;
                    }
                    if (bandStart >= 0 && depth >= bandStart + thickness) {
                        break;
                    }
                    if (substrate) {
                        level.setBlock(cursor, conglomerate, 2);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    public static void placePondBed(WorldGenLevel level, BlockPos floor) {
        if (floor.getY() <= level.getMinBuildHeight() || floor.getY() >= level.getMaxBuildHeight() - 1
                || !level.getFluidState(floor.above()).is(FluidTags.WATER)) {
            return;
        }
        placeTidestoneColumn(level, floor.getX(), floor.getY(), floor.getZ(), state("rockite"), state("tidestone"),
                state("shellstone"), new BlockPos.MutableBlockPos());
    }

    private static boolean placeTidestoneColumn(WorldGenLevel level, int x, int floorY, int z, BlockState rockite,
                                                BlockState tidestone, BlockState shellstone, BlockPos.MutableBlockPos cursor) {
        double patch = patchDistance(level.getSeed() ^ 0x7C4E21B9L, x, z);
        if (patch >= 1.0D) {
            return false;
        }
        boolean placed = false;
        int halfDepth = Math.max(1, (int) (5 * Math.sqrt(1.0D - patch)));
        for (int depth = 7 - halfDepth; depth <= 7 + halfDepth; depth++) {
            int y = floorY - depth;
            if (y < level.getMinBuildHeight()) {
                break;
            }
            cursor.set(x, y, z);
            BlockState current = level.getBlockState(cursor);
            if (current.is(rockite.getBlock()) || current.is(shellstone.getBlock())) {
                level.setBlock(cursor, tidestone, 2);
                placed = true;
            }
        }
        return placed;
    }

    private static double patchDistance(long seed, int x, int z) {
        double best = Double.POSITIVE_INFINITY;
        int cellX = Math.floorDiv(x, LENS_CELL);
        int cellZ = Math.floorDiv(z, LENS_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long key = hash(seed, cellX + dx, 0, cellZ + dz);
                if (unit(key) > 0.7D) {
                    continue;
                }
                double centerX = (cellX + dx) * LENS_CELL + unit(mix(key + 1)) * LENS_CELL;
                double centerZ = (cellZ + dz) * LENS_CELL + unit(mix(key + 2)) * LENS_CELL;
                double radius = 8 + unit(mix(key + 3)) * 6;
                double distanceX = (x - centerX) / radius;
                double distanceZ = (z - centerZ) / radius;
                best = Math.min(best, distanceX * distanceX + distanceZ * distanceZ);
            }
        }
        return best;
    }

    private static long hash(long seed, int x, int y, int z) {
        return mix(seed ^ x * 0x632BE59BD9B4E019L ^ y * 0x9E3779B97F4A7C15L ^ z * 0x85157AF5D66D3EABL);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unit(long value) {
        return (value >>> 11) * 0x1.0p-53;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", path);
    }

    private static BlockState state(String path) {
        return BuiltInRegistries.BLOCK.getOptional(id(path))
                .orElseThrow(() -> new IllegalStateException("Missing Elythia geology block: " + path)).defaultBlockState();
    }
}
