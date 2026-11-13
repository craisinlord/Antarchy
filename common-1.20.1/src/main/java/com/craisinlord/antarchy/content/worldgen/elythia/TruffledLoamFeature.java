package com.craisinlord.antarchy.content.worldgen.elythia;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
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

public final class TruffledLoamFeature extends Feature<NoneFeatureConfiguration> {
    private static final TagKey<Biome> TRUFFLE_BIOMES = TagKey.create(Registries.BIOME,
            new ResourceLocation("antarchy", "truffle_biomes"));

    public TruffledLoamFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        Block loam = BuiltInRegistries.BLOCK.get(new ResourceLocation("antarchy", "primordial_loam"));
        Block truffled = BuiltInRegistries.BLOCK.get(new ResourceLocation("antarchy", "truffled_loam"));
        int x = (context.origin().getX() & ~15) + 3 + context.random().nextInt(10);
        int z = (context.origin().getZ() & ~15) + 3 + context.random().nextInt(10);
        int minX = context.origin().getX() & ~15;
        int minZ = context.origin().getZ() & ~15;
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockPos start = new BlockPos(x, surface - 1 - context.random().nextInt(3), z);
        List<BlockPos> cluster = new ArrayList<>();
        if (!eligible(level, start, loam)) {
            return false;
        }
        cluster.add(start);
        int size = 2 + context.random().nextInt(4);
        for (int attempt = 0; attempt < 24 && cluster.size() < size; attempt++) {
            BlockPos next = cluster.get(context.random().nextInt(cluster.size()))
                    .relative(Direction.values()[context.random().nextInt(6)]);
            if (next.getX() > minX && next.getX() < minX + 15
                    && next.getZ() > minZ && next.getZ() < minZ + 15
                    && !cluster.contains(next) && eligible(level, next, loam)) cluster.add(next);
        }
        if (cluster.size() < 2) {
            return false;
        }
        for (BlockPos pos : cluster) level.setBlock(pos, truffled.defaultBlockState(), 2);
        return true;
    }

    private static boolean eligible(WorldGenLevel level, BlockPos pos, Block loam) {
        if (pos.getY() < level.getMinBuildHeight() + 1 || !soil(level.getBlockState(pos), loam)) {
            return false;
        }
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()) - 1;
        int depth = surface - pos.getY();
        BlockPos surfacePos = new BlockPos(pos.getX(), surface, pos.getZ());
        if (depth < 1 || depth > 3 || !level.getBiome(surfacePos).is(TRUFFLE_BIOMES)
                || !level.getBlockState(surfacePos).is(loam)) return false;
        for (Direction direction : Direction.values()) {
            BlockPos adjacent = pos.relative(direction);
            BlockState state = level.getBlockState(adjacent);
            if (!state.getFluidState().isEmpty() || !state.isFaceSturdy(level, adjacent, direction.getOpposite())) {
                return false;
            }
        }
        return true;
    }

    private static boolean soil(BlockState state, Block loam) {
        return state.is(loam)
                || state.is(BuiltInRegistries.BLOCK.get(new ResourceLocation("antarchy", "loam")))
                || state.is(Blocks.DIRT) || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.MOSS_BLOCK);
    }
}
