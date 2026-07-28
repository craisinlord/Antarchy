package com.craisinlord.antarchy.content.worldgen.cavaryn;

import com.craisinlord.antarchy.content.block.HangingCreeprootsBlock;
import com.craisinlord.antarchy.content.block.MoltingVinesBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bile always wins on placement, but once a vine/creeproots/molting-vine column already exists,
 * bile carving and oozing must not eat the block it's anchored to — otherwise a later-generated
 * neighboring chunk's bile cyst/vein can undermine foliage placed earlier and leave it floating
 * or pop it as a dropped item.
 */
public final class CavarynFoliageGuard {
    private CavarynFoliageGuard() {
    }

    public static boolean isFoliageNearby(LevelAccessor level, BlockPos center) {
        if (isFoliage(level.getBlockState(center))) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (isFoliage(level.getBlockState(center.relative(direction)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isFoliage(BlockState state) {
        return state.getBlock() instanceof VineBlock
                || state.getBlock() instanceof HangingCreeprootsBlock
                || state.getBlock() instanceof MoltingVinesBlock;
    }
}
