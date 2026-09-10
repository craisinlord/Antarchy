package com.craisinlord.antarchy.content.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/** Converts a full Antiwater cell next to a vanilla bubble-column source. */
public final class AntiwaterBubbleColumnUtil {
    private AntiwaterBubbleColumnUtil() {
    }

    public static void update(LevelAccessor level, BlockPos pos, FluidState fluidState) {
        if (!AntarchyFluidChecks.isAntiwater(fluidState)
                || (!fluidState.isSource() && fluidState.getAmount() < 7)) {
            return;
        }

        BlockState below = level.getBlockState(pos.below());
        if (isBubbleSource(below)) {
            replaceColumn(level, pos, Direction.UP, below.is(Blocks.MAGMA_BLOCK));
            return;
        }

        BlockState above = level.getBlockState(pos.above());
        if (isBubbleSource(above)) {
            replaceColumn(level, pos, Direction.DOWN, above.is(Blocks.MAGMA_BLOCK));
        }
    }

    private static boolean isBubbleSource(BlockState state) {
        return state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.SOUL_SAND);
    }

    private static void replaceColumn(LevelAccessor level, BlockPos start, Direction direction, boolean dragDown) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        BlockState columnState = Blocks.BUBBLE_COLUMN.defaultBlockState()
                .setValue(BubbleColumnBlock.DRAG_DOWN, dragDown);

        while (true) {
            BlockState state = level.getBlockState(cursor);
            FluidState fluidState = state.getFluidState();
            if (!AntarchyFluidChecks.isAntiwater(fluidState)
                    && !state.is(Blocks.BUBBLE_COLUMN)) {
                return;
            }

            level.setBlock(cursor, columnState, 3);
            cursor.move(direction);
        }
    }
}
