package com.craisinlord.antarchy.content.worldgen.ants;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class BrownAntNestFeature extends SurfaceAntNestFeature {
    public static final MapCodec<BrownAntNestFeature> CODEC = createCodec(BrownAntNestFeature::new);

    public BrownAntNestFeature(Holder<BlockStateProvider> toPlace, boolean scheduleTick) {
        super(toPlace, scheduleTick);
    }

    @Override
    public MapCodec<BrownAntNestFeature> codec() {
        return CODEC;
    }

    @Override
    protected boolean isValidNestGround(BlockState nestGroundState) {
        return nestGroundState.is(BlockTags.DIRT)
                || nestGroundState.is(Blocks.GRASS_BLOCK)
                || nestGroundState.is(Blocks.MOSS_BLOCK)
                || nestGroundState.is(Blocks.COARSE_DIRT);
    }

    @Override
    protected void decorateAroundNest(WorldGenLevel level, BlockPos center, RandomSource random) {
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                if (xOffset == 0 && zOffset == 0) {
                    continue;
                }

                if (Math.abs(xOffset) == 1 && Math.abs(zOffset) == 1 && random.nextBoolean()) {
                    continue;
                }

                BlockPos targetPos = center.offset(xOffset, 0, zOffset);
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(BlockTags.DIRT) || targetState.is(Blocks.GRASS_BLOCK) || targetState.is(Blocks.MOSS_BLOCK)) {
                    level.setBlock(targetPos, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                }
            }
        }
    }
}
