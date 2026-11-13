package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class ElythiaMountainScreeFeature implements Feature {
    public static final MapCodec<ElythiaMountainScreeFeature> CODEC = MapCodec.unit(ElythiaMountainScreeFeature::new);

    private static final TagKey<Biome> BIOMES = TagKey.create(Registries.BIOME, id("mountain_scree_biomes"));
    private static final int ROCK_ABOVE = 3;

    @Override
    public MapCodec<ElythiaMountainScreeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {

        var columns = ElythiaSlopeField.chunk(level.getSeed(), origin.getX() >> 4, origin.getZ() >> 4,
                (x, z) -> level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z));
        BlockState rockite = state("rockite");
        BlockState zebraStone = state("zebra_stone");
        BlockState rockiteSlab = state("rockite_slab").setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState zebraStoneSlab = state("zebra_stone_slab").setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (ElythiaSlopeField.Column column : columns) {
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, column.x(), column.z()) - 1;
            if (Math.abs(y + 1 - column.height()) > 2 || y <= level.getMinY() || y + 3 >= level.getMaxY() + 1) {
                continue;
            }
            cursor.set(column.x(), y, column.z());
            BlockPos above = cursor.above();
            Holder<Biome> biome = level.getBiome(cursor);
            if (!biome.is(BIOMES) || !level.getFluidState(cursor).isEmpty()
                    || !level.getFluidState(above).isEmpty() || !level.canSeeSky(above) || !level.isEmptyBlock(above)) {
                continue;
            }
            BlockState surface = level.getBlockState(cursor);
            if (!surface.is(ElythiaGeologyFeature.NATURAL_STONE) && !surface.is(BlockTags.DIRT)
                    && !surface.is(Blocks.GRASS_BLOCK) && !surface.is(Blocks.MOSS_BLOCK)) {
                continue;
            }
            boolean zebra = matchesZebra(level.getSeed(), biome, column.x(), y + ROCK_ABOVE, column.z());
            BlockState block = zebra ? zebraStone : rockite;
            BlockState slab = zebra ? zebraStoneSlab : rockiteSlab;
            if (column.outcrop()) {
                level.setBlock(cursor, block, 2);
                level.setBlock(above, block, 2);
                if (column.rise() == 2 && level.isEmptyBlock(above.above()) && level.getFluidState(above.above()).isEmpty()) {
                    level.setBlock(above.above(), slab, 2);
                }
            } else {
                level.setBlock(above, slab, 2);
            }
            placed = true;
        }
        return placed;
    }

    private static boolean matchesZebra(long seed, Holder<Biome> biome, int x, int y, int z) {
        return biome.unwrapKey().flatMap(key -> ElythiaStrataProfile.forBiome(key.identifier().toString()))
                .map(profile -> ElythiaStrata.layerAt(seed, profile, y, ElythiaStrata.bedOffset(seed, profile, x, z)) == ElythiaStrata.Layer.ZEBRA)
                .orElse(false);
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("antarchy", path);
    }

    private static BlockState state(String path) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id(path))
                .orElseThrow(() -> new IllegalStateException("Missing mountain palette block: " + path));
        return block.defaultBlockState();
    }
}
