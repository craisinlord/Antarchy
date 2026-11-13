package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class CranberryGroveFeature implements Feature {
    public static final MapCodec<CranberryGroveFeature> CODEC = MapCodec.unit(CranberryGroveFeature::new);

    @Override
    public MapCodec<CranberryGroveFeature> codec() {
        return CODEC;
    }
    private static final TagKey<Biome> BIOMES = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("antarchy", "cranberry_grove_biomes"));
    private static final int[][] TREE_OFFSETS = {{-10, -7}, {10, -7}, {0, 12}};

    public CranberryGroveFeature() { }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        int cellX = Math.floorDiv(chunkX, 4);
        int cellZ = Math.floorDiv(chunkZ, 4);
        RandomSource cellRandom = RandomSource.create(level.getSeed() ^ (cellX * 341873128712L) ^ (cellZ * 132897987541L) ^ 0x4352414E42455252L);
        int centerX = cellX * 64 + 24 + cellRandom.nextInt(17);
        int centerZ = cellZ * 64 + 24 + cellRandom.nextInt(17);
        boolean placed = false;
        for (int[] offset : TREE_OFFSETS) {
            int x = centerX + offset[0];
            int z = centerZ + offset[1];
            if ((x >> 4) != chunkX || (z >> 4) != chunkZ) {
                continue;
            }
            BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), z);
            if (level.getBiome(pos).is(BIOMES)) {
                placed |= CranberryTreeFeature.placeTree(level, pos, random);
            }
        }
        return placed;
    }
}
