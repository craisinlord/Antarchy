package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CranberryGroveFeature extends Feature<NoneFeatureConfiguration> {
    private static final TagKey<Biome> BIOMES = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("antarchy", "cranberry_grove_biomes"));
    private static final int[][] TREE_OFFSETS = {{-10, -7}, {10, -7}, {0, 12}};

    public CranberryGroveFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        int chunkX = context.origin().getX() >> 4;
        int chunkZ = context.origin().getZ() >> 4;
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
                placed |= CranberryTreeFeature.placeTree(level, pos, context.random());
            }
        }
        return placed;
    }
}
