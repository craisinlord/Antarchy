package com.craisinlord.antarchy.content.worldgen.elythia;

import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public final class ElythiaTidePoolFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("antarchy", "zebra_scarps"));
    private static final PerlinSimplexNoise POOL_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(0x5A1D3E71L)), List.of(0));
    private static final double BROAD_SCALE = 0.045D;
    private static final double BROAD_WEIGHT = 0.8D;
    private static final double DETAIL_SCALE = 0.19D;
    private static final double DETAIL_WEIGHT = 0.25D;
    private static final double DEEPEST_FIELD = 0.5D;
    private static final double TALL_RIB_FIELD = -0.25D;
    private static final int MAX_POOL_DEPTH = 3;
    private static final int MAX_CUT = 6;
    private static final int MAX_FILL = 5;

    public ElythiaTidePoolFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int startX = origin.getX() & ~15;
        int startZ = origin.getZ() & ~15;
        ChunkAccess chunk = level.getChunk(startX >> 4, startZ >> 4);
        Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        int offsetX = (int) (level.getSeed() & 0xFFFFL);
        int offsetZ = (int) (level.getSeed() >>> 16 & 0xFFFFL);
        int waterY = chunkGenerator.getSeaLevel() - 1;
        BlockState zebraStone = state("zebra_stone");
        BlockState rockite = state("rockite");
        boolean placed = false;
        for (int x = startX; x < startX + 16; x++) {
            for (int z = startZ; z < startZ + 16; z++) {
                int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
                BlockPos topPos = new BlockPos(x, top, z);
                if (top < waterY - MAX_FILL || top > waterY + MAX_CUT
                        || !level.getBiome(topPos).is(BIOME)
                        || !ElythiaStoneTorFeature.isGround(level.getBlockState(topPos))) continue;
                double field = field(x + offsetX, z + offsetZ);
                placed |= field > 0.0D
                        ? placePool(level, x, top, z, waterY, field, floor, surface)
                        : placeRib(level, x, top, z, waterY, field, zebraStone, rockite, floor, surface);
            }
        }
        return placed;
    }

    public static double field(int x, int z) {
        return POOL_NOISE.getValue(x * BROAD_SCALE, z * BROAD_SCALE, false) * BROAD_WEIGHT
                + POOL_NOISE.getValue(x * DETAIL_SCALE + 73.0D, z * DETAIL_SCALE + 73.0D, false) * DETAIL_WEIGHT;
    }

    public static int poolDepth(double field) {
        return 1 + Math.min(MAX_POOL_DEPTH - 1, (int) (field / DEEPEST_FIELD * MAX_POOL_DEPTH));
    }

    private static boolean placePool(WorldGenLevel level, int x, int top, int z, int waterY, double field, Heightmap floor, Heightmap surface) {
        int floorY = waterY - poolDepth(field);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        if (top <= floorY || !level.getBlockState(cursor.set(x, floorY - 1, z)).isSolid() || opensToAir(level, x, waterY, z)) return false;
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState gravel = Blocks.GRAVEL.defaultBlockState();
        for (int y = top; y > waterY; y--) write(level, cursor.set(x, y, z), air, floor, surface);
        for (int y = Math.min(top, waterY); y > floorY; y--) write(level, cursor.set(x, y, z), water, floor, surface);
        write(level, cursor.set(x, floorY, z), gravel, floor, surface);
        return true;
    }

    private static boolean placeRib(WorldGenLevel level, int x, int top, int z, int waterY, double field, BlockState zebraStone,
                                    BlockState rockite, Heightmap floor, Heightmap surface) {
        int ribTop = field < TALL_RIB_FIELD ? waterY + 2 : waterY + 1;
        if (top >= ribTop) return false;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = top + 1; y <= ribTop; y++) write(level, cursor.set(x, y, z), y == ribTop ? zebraStone : rockite, floor, surface);
        return true;
    }

    private static boolean opensToAir(WorldGenLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        for (Direction direction : Direction.Plane.HORIZONTAL) if (level.getBlockState(pos.relative(direction)).isAir()) return true;
        return false;
    }

    private static void write(WorldGenLevel level, BlockPos pos, BlockState state, Heightmap floor, Heightmap surface) {
        level.setBlock(pos, state, 2);
        floor.update(pos.getX() & 15, pos.getY(), pos.getZ() & 15, state);
        surface.update(pos.getX() & 15, pos.getY(), pos.getZ() & 15, state);
    }

    private static BlockState state(String path) {
        return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("antarchy", path)).defaultBlockState();
    }
}
