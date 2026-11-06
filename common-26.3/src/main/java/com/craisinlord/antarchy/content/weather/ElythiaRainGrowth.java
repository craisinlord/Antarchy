package com.craisinlord.antarchy.content.weather;

import com.craisinlord.antarchy.config.AntarchySettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ElythiaRainGrowth {
    private static final int INTERVAL_TICKS = 5;
    private static final int RADIUS = 48;
    private static final int COLUMNS_PER_PLAYER = 35;
    private static final int LUMEN_RAIN_MULTIPLIER = 2;

    private ElythiaRainGrowth() {
    }

    public static void tick(ServerLevel level) {
        if (!AntarchySettings.elythiaRainGrowth() || !ElythiaEvents.isElythia(level)
                || level.getGameTime() % INTERVAL_TICKS != 0L || !level.isRaining()) {
            return;
        }
        int columns = COLUMNS_PER_PLAYER * (ElythiaEvents.isActive(level, ElythiaEvent.LUMEN_RAIN) ? LUMEN_RAIN_MULTIPLIER : 1);
        RandomSource random = level.getRandom();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            BlockPos origin = player.blockPosition();
            for (int i = 0; i < columns; i++) {
                cursor.set(origin.getX() + random.nextInt(RADIUS * 2 + 1) - RADIUS, 0, origin.getZ() + random.nextInt(RADIUS * 2 + 1) - RADIUS);
                if (!level.hasChunkAt(cursor)) {
                    continue;
                }
                BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cursor);
                BlockState state = level.getBlockState(pos);
                if (isRainFed(state) && level.isRainingAt(pos)) {
                    state.randomTick(level, pos, random);
                }
            }
        }
    }

    private static boolean isRainFed(BlockState state) {
        if (!state.isRandomlyTicking()) {
            return false;
        }
        return state.is(BlockTags.CROPS) || state.is(BlockTags.SAPLINGS) || state.getBlock() instanceof BonemealableBlock;
    }
}
