package com.craisinlord.antarchy.content.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ElythiaFireflies {
    private static final int SPAWN_INTERVAL = 2;
    private static final int SPAWN_RADIUS_H = 15;
    private static final int MIN_ATTEMPTS = 2;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_VERTICAL_OFFSET = 12;
    private static final double MIN_HOVER = 0.2D;
    private static final double MAX_HOVER = 2.5D;

    private ElythiaFireflies() {
    }

    public static void spawn(ClientLevel level, Player player, ParticleOptions particle) {
        if (level.getGameTime() % SPAWN_INTERVAL != 0) {
            return;
        }
        RandomSource random = level.random;
        int px = player.getBlockX();
        int py = player.getBlockY();
        int pz = player.getBlockZ();
        int attempts = MIN_ATTEMPTS + random.nextInt(MAX_ATTEMPTS - MIN_ATTEMPTS + 1);
        for (int i = 0; i < attempts; i++) {
            int x = px + random.nextInt(SPAWN_RADIUS_H * 2 + 1) - SPAWN_RADIUS_H;
            int z = pz + random.nextInt(SPAWN_RADIUS_H * 2 + 1) - SPAWN_RADIUS_H;
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            if (Math.abs(surface - py) > MAX_VERTICAL_OFFSET) {
                continue;
            }
            BlockPos ground = new BlockPos(x, surface - 1, z);
            BlockPos above = ground.above();
            if (!isGrassy(level.getBlockState(ground), level.getBlockState(above))) {
                continue;
            }
            double hover = MIN_HOVER + random.nextDouble() * random.nextDouble() * (MAX_HOVER - MIN_HOVER);
            level.addParticle(
                    particle,
                    x + random.nextDouble(),
                    surface + hover,
                    z + random.nextDouble(),
                    0.0D, 0.0D, 0.0D
            );
        }
    }

    private static boolean isGrassy(BlockState ground, BlockState above) {
        if (!above.isAir() && !above.canBeReplaced()) {
            return false;
        }
        return ground.is(BlockTags.DIRT) || ground.is(BlockTags.REPLACEABLE_BY_TREES) || ground.is(BlockTags.FLOWERS);
    }
}
