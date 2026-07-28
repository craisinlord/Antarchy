package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public final class BluestonePlacementHelper {
    private BluestonePlacementHelper() {
    }

    public static boolean hasCeilingSupport(LevelReader level, BlockPos pos) {
        BlockPos supportPos = pos.above();
        return Block.canSupportCenter(level, supportPos, Direction.DOWN);
    }

    public static boolean canPlaceOnCeiling(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.DOWN && hasCeilingSupport(context.getLevel(), context.getClickedPos());
    }
}
