package com.craisinlord.antarchy.content.worldgen.thoraxis;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

public record ThoraxisIchorLakeFeature() implements Feature {
    public static final MapCodec<ThoraxisIchorLakeFeature> CODEC = MapCodec.unit(ThoraxisIchorLakeFeature::new);
    private static final int LAKE_FLOOR_Y = 1;
    private static final int LAKE_SURFACE_Y = 8;
    private static final Identifier ICHOR_BLOCK_ID =
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "ichor");

    @Override
    public MapCodec<ThoraxisIchorLakeFeature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        BlockState ichorState = BuiltInRegistries.BLOCK.getValue(ICHOR_BLOCK_ID).defaultBlockState();
        if (ichorState.isAir() || ichorState.getFluidState().isEmpty()) {
            return false;
        }

        int chunkMinX = origin.getX() >> 4 << 4;
        int chunkMinZ = origin.getZ() >> 4 << 4;
        boolean placedAny = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = chunkMinX; x < chunkMinX + 16; x++) {
            for (int z = chunkMinZ; z < chunkMinZ + 16; z++) {
                for (int y = LAKE_FLOOR_Y; y <= LAKE_SURFACE_Y; y++) {
                    mutable.set(x, y, z);
                    BlockState current = level.getBlockState(mutable);
                    if (!isOpenForLake(current)) {
                        continue;
                    }

                    level.setBlock(mutable, ichorState, 2);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }

    private static boolean isOpenForLake(BlockState state) {
        return state.isAir() || state.canBeReplaced() || !blocksMotion(state);
    }

    private static boolean blocksMotion(BlockState state) {
        Block block = state.getBlock();
        return block != Blocks.COBWEB && block != Blocks.BAMBOO_SAPLING && state.isSolid();
    }
}
