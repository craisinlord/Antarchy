package com.craisinlord.antarchy.content.worldgen.ants;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class RedAntNestFeature extends BaseAntNestFeature {
    public RedAntNestFeature(Codec<SimpleBlockConfiguration> codec) {
        super(codec);
    }

    @Override
    protected BlockPos resolveNestPos(WorldGenLevel level, BlockPos origin) {
        int startY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()) - 1;
        int minY = level.getMinBuildHeight() + 1;

        for (int y = startY; y >= minY; y--) {
            BlockPos candidate = new BlockPos(origin.getX(), y, origin.getZ());
            BlockState surfaceState = level.getBlockState(candidate);
            BlockState aboveState = level.getBlockState(candidate.above());
            BlockState supportState = level.getBlockState(candidate.below());

            if (surfaceState.getFluidState().isEmpty()
                    && this.isValidNestGround(surfaceState)
                    && aboveState.canBeReplaced()
                    && aboveState.getFluidState().isEmpty()
                    && supportState.isFaceSturdy(level, candidate.below(), Direction.UP)
                    && supportState.getFluidState().isEmpty()) {
                return candidate;
            }
        }

        return new BlockPos(origin.getX(), startY, origin.getZ());
    }

    @Override
    protected boolean isValidNestGround(BlockState nestGroundState) {
        return nestGroundState.is(BlockTags.BASE_STONE_NETHER)
                || nestGroundState.is(Blocks.BLACKSTONE)
                || nestGroundState.is(Blocks.SOUL_SAND)
                || nestGroundState.is(Blocks.SOUL_SOIL)
                || nestGroundState.is(Blocks.GRAVEL)
                || nestGroundState.is(Blocks.MAGMA_BLOCK)
                || nestGroundState.is(AntarchyObjects.NYXITE.get());
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
                if (targetState.is(BlockTags.BASE_STONE_NETHER)
                        || targetState.is(Blocks.BLACKSTONE)
                        || targetState.is(Blocks.SOUL_SAND)
                        || targetState.is(Blocks.SOUL_SOIL)
                        || targetState.is(Blocks.GRAVEL)
                        || targetState.is(AntarchyObjects.NYXITE.get())) {
                    level.setBlock(targetPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
                }
            }
        }
    }
}
