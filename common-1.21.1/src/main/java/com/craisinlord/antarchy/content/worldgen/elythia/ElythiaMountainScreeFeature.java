package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class ElythiaMountainScreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final TagKey<Biome> BIOMES = TagKey.create(Registries.BIOME, id("mountain_scree_biomes"));

    public ElythiaMountainScreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        var columns = ElythiaSlopeField.chunk(level.getSeed(), context.origin().getX() >> 4, context.origin().getZ() >> 4,
                (x, z) -> context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, randomState));
        BlockState rockite = state("rockite");
        BlockState zebraStone = state("zebra_stone");
        BlockState rockiteSlab = state("rockite_slab").setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState zebraStoneSlab = state("zebra_stone_slab").setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (ElythiaSlopeField.Column column : columns) {
            int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, column.x(), column.z()) - 1;
            if (Math.abs(y + 1 - column.height()) > 2 || y <= level.getMinBuildHeight() || y + 3 >= level.getMaxBuildHeight()) {
                continue;
            }
            cursor.set(column.x(), y, column.z());
            BlockPos above = cursor.above();
            if (!level.getBiome(cursor).is(BIOMES) || !level.getFluidState(cursor).isEmpty()
                    || !level.getFluidState(above).isEmpty() || !level.canSeeSky(above) || !level.isEmptyBlock(above)) {
                continue;
            }
            BlockState surface = level.getBlockState(cursor);
            if (!surface.is(ElythiaGeologyFeature.NATURAL_STONE) && !surface.is(BlockTags.DIRT)
                    && !surface.is(Blocks.GRASS_BLOCK) && !surface.is(Blocks.MOSS_BLOCK)) {
                continue;
            }
            BlockState block = column.zebraStone() ? zebraStone : rockite;
            BlockState slab = column.zebraStone() ? zebraStoneSlab : rockiteSlab;
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

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("antarchy", path);
    }

    private static BlockState state(String path) {
        Block block = BuiltInRegistries.BLOCK.getOptional(id(path))
                .orElseThrow(() -> new IllegalStateException("Missing mountain palette block: " + path));
        return block.defaultBlockState();
    }
}
