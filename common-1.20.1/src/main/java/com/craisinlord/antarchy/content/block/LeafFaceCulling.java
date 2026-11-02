package com.craisinlord.antarchy.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class LeafFaceCulling {
    private static final int BURIAL_RADIUS = 2;
    private static final int[] OFFSETS = buildOffsets();

    private LeafFaceCulling() {
    }

    public static boolean appliesTo(BlockState state) {
        return state.getBlock() instanceof OuranwoodLeavesBlock || state.getBlock() instanceof RoyalLeavesBlock;
    }

    public static boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState neighborState) {
        if (!(neighborState.getBlock() instanceof LeavesBlock)) {
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i < OFFSETS.length; i += 3) {
            cursor.setWithOffset(pos, OFFSETS[i], OFFSETS[i + 1], OFFSETS[i + 2]);
            if (!(level.getBlockState(cursor).getBlock() instanceof LeavesBlock)) {
                return false;
            }
        }
        return true;
    }

    private static int[] buildOffsets() {
        int count = 0;
        int[] offsets = new int[125 * 3];
        for (int distance = 1; distance <= BURIAL_RADIUS; distance++) {
            for (int x = -distance; x <= distance; x++) {
                for (int y = -distance; y <= distance; y++) {
                    for (int z = -distance; z <= distance; z++) {
                        if (Math.abs(x) + Math.abs(y) + Math.abs(z) != distance) {
                            continue;
                        }
                        offsets[count++] = x;
                        offsets[count++] = y;
                        offsets[count++] = z;
                    }
                }
            }
        }
        int[] result = new int[count];
        System.arraycopy(offsets, 0, result, 0, count);
        return result;
    }
}
