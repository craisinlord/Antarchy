package com.craisinlord.antarchy.content.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class MinersDreamTorchPlacer {
    private MinersDreamTorchPlacer() {
    }

    public static void tryPlaceTorch(ServerLevel level, BlockPos center, RandomSource random) {
        BlockPos floorSpot = findFloorSpot(level, center);
        boolean floorFirst = random.nextBoolean();

        if (floorFirst) {
            if (floorSpot != null && placeFloorTorch(level, floorSpot)) {
                return;
            }
            placeWallTorch(level, center, random);
            return;
        }

        if (placeWallTorch(level, center, random)) {
            return;
        }
        if (floorSpot != null) {
            placeFloorTorch(level, floorSpot);
        }
    }

    private static BlockPos findFloorSpot(ServerLevel level, BlockPos center) {
        for (int dy = -2; dy <= 1; dy++) {
            BlockPos candidate = center.offset(0, dy, 0);
            if (!isOpenForTorch(level, candidate)) {
                continue;
            }
            BlockState below = level.getBlockState(candidate.below());
            if (below.isFaceSturdy(level, candidate.below(), Direction.UP)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean placeFloorTorch(ServerLevel level, BlockPos pos) {
        if (!isOpenForTorch(level, pos)) {
            return false;
        }
        BlockState state = Blocks.TORCH.defaultBlockState();
        if (!state.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, state, 3);
        level.playSound(null, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    private static boolean placeWallTorch(ServerLevel level, BlockPos center, RandomSource random) {
        List<Direction> order = Direction.Plane.HORIZONTAL.shuffledCopy(random);
        for (int dy = -1; dy <= 1; dy++) {
            BlockPos candidate = center.offset(0, dy, 0);
            if (!isOpenForTorch(level, candidate)) {
                continue;
            }
            for (Direction wallSide : order) {
                Direction facing = wallSide.getOpposite();
                BlockState state = Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
                if (!state.canSurvive(level, candidate)) {
                    continue;
                }
                level.setBlock(candidate, state, 3);
                level.playSound(null, candidate, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }

    private static boolean isOpenForTorch(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return (state.isAir() || state.canBeReplaced()) && level.getFluidState(pos).isEmpty();
    }
}
