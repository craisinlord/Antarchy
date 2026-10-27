package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import com.craisinlord.antarchy.content.block.GiantLilyPadBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class GiantLilyPadPatchFeature implements Feature {
    public static final MapCodec<GiantLilyPadPatchFeature> CODEC = MapCodec.unit(() -> new GiantLilyPadPatchFeature(false));
    public static final MapCodec<GiantLilyPadPatchFeature> PEACH_FOREST_CODEC = MapCodec.unit(() -> new GiantLilyPadPatchFeature(true));
    private static final float PEACH_FOREST_LOTUS_CHANCE = 0.12F;

    private final boolean peachForest;
    private final float lotusChance;

    private GiantLilyPadPatchFeature(boolean peachForest) {
        this.peachForest = peachForest;
        this.lotusChance = peachForest ? PEACH_FOREST_LOTUS_CHANCE : 1.0F;
    }

    @Override
    public MapCodec<GiantLilyPadPatchFeature> codec() {
        return this.peachForest ? PEACH_FOREST_CODEC : CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        boolean placedAny = false;

        for (int i = 0; i < 12; i++) {
            int x = origin.getX() + random.nextInt(17) - 8;
            int z = origin.getZ() + random.nextInt(17) - 8;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            BlockPos fluidPos = new BlockPos(x, surfaceY, z);
            if (!level.getFluidState(fluidPos).is(AntarchyTags.Fluids.GIANT_LILY_PAD_SUPPORTING_FLUIDS) || !level.getFluidState(fluidPos).isSource()) {
                continue;
            }

            BlockPos padPos = fluidPos.above();
            if (!GiantLilyPadBlock.canPlaceStructure(level, padPos)) {
                continue;
            }

            GiantLilyPadBlock.placeStructure(level, padPos, GiantLilyPadBlock.PadRotation.values()[random.nextInt(4)]);
            placedAny = true;

            if (random.nextFloat() < this.lotusChance) {
                placeLotus(level, padPos);
            }
        }

        return placedAny;
    }

    private static void placeLotus(WorldGenLevel level, BlockPos pos) {
        BlockState padState = level.getBlockState(pos);
        if (!padState.is(AntarchyObjects.GIANT_LILY_PAD.get()) || padState.getValue(GiantLilyPadBlock.HAS_LOTUS)) {
            return;
        }

        level.setBlock(pos, padState.setValue(GiantLilyPadBlock.HAS_LOTUS, true), Block.UPDATE_CLIENTS);
    }
}
