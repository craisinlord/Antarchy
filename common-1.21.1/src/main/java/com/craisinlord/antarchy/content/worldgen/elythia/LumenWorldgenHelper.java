package com.craisinlord.antarchy.content.worldgen.elythia;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

final class LumenWorldgenHelper {
    private LumenWorldgenHelper() {
    }

    static boolean isNaturalFloor(BlockState state) {
        return state.is(net.minecraft.tags.BlockTags.DIRT)
                || state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK)
                || state.is(net.minecraft.world.level.block.Blocks.ROOTED_DIRT)
                || state.is(AntarchyObjects.SHELLSTONE.get())
                || state.is(net.minecraft.world.level.block.Blocks.TUFF);
    }

    static boolean isSolidSupport(BlockState state) {
        return !state.isAir()
                && state.blocksMotion()
                && state.getFluidState().isEmpty()
                && !state.is(BlockTags.LOGS)
                && !state.is(BlockTags.LEAVES);
    }

    static BlockPos findNaturalFloor(WorldGenLevel level, int x, int z, int topY, int minY) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = topY; y >= minY; y--) {
            mutable.set(x, y, z);
            BlockState state = level.getBlockState(mutable);
            if (isNaturalFloor(state)) {
                return mutable.immutable();
            }
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                continue;
            }
            if (!state.isAir() && state.blocksMotion()) {
                break;
            }
        }
        return null;
    }

    static BlockState lumenState(BlockState lumenBlockState, boolean source) {
        if (source || !lumenBlockState.hasProperty(LiquidBlock.LEVEL)) {
            return source ? lumenBlockState.setValue(LiquidBlock.LEVEL, 0) : lumenBlockState;
        }

        return lumenBlockState.setValue(LiquidBlock.LEVEL, 7);
    }

    static BlockState lumenState(BlockState lumenBlockState, int level) {
        if (!lumenBlockState.hasProperty(LiquidBlock.LEVEL)) {
            return lumenBlockState;
        }
        return lumenBlockState.setValue(LiquidBlock.LEVEL, net.minecraft.util.Mth.clamp(level, 0, 15));
    }

    static void scheduleFluidTick(WorldGenLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }
}
