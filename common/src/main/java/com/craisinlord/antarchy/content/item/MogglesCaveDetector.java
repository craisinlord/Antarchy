package com.craisinlord.antarchy.content.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Client-side cave finder for Moggles.
 *
 * <p>Every {@link #SCAN_INTERVAL} ticks, BFS-maps the player's current connected
 * air space, then scans outward to find the nearest underground air pocket that
 * is NOT part of that space. The result is stored in {@link #caveTarget} and
 * read each tick by the particle renderer.
 */
public final class MogglesCaveDetector {

    private static final int SCAN_INTERVAL = 40;
    /** How far to flood-fill the player's own air pocket before stopping. */
    private static final int PLAYER_SPACE_LIMIT = 8;
    /** Search radius for the external cave scan. */
    private static final int SCAN_RADIUS = 20;
    /** Minimum number of air neighbors a candidate block must have (filters tiny cracks). */
    private static final int MIN_AIR_NEIGHBORS = 2;

    private static volatile BlockPos caveTarget = null;
    private static int cooldown = 0;

    private MogglesCaveDetector() {}

    public static BlockPos getCaveTarget() {
        return caveTarget;
    }

    /** Call once per client tick while Moggles are equipped. */
    public static void tick(Player player, Level level) {
        if (--cooldown > 0) return;
        cooldown = SCAN_INTERVAL;
        caveTarget = scan(player, level);
    }

    public static void clear() {
        caveTarget = null;
        cooldown = 0;
    }

    private static BlockPos scan(Player player, Level level) {
        BlockPos origin = player.blockPosition();

        // BFS through air from the player's feet to map their current open space.
        Set<Long> playerSpace = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        BlockPos eyePos = origin.above(); // also include the eye block
        for (BlockPos seed : new BlockPos[]{origin, eyePos}) {
            if (level.isLoaded(seed) && level.getBlockState(seed).isAir()) {
                long key = seed.asLong();
                if (playerSpace.add(key)) queue.add(seed);
            }
        }

        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (origin.distManhattan(next) > PLAYER_SPACE_LIMIT) continue;
                long key = next.asLong();
                if (playerSpace.contains(key)) continue;
                if (!level.isLoaded(next)) continue;
                if (!level.getBlockState(next).isAir()) continue;
                playerSpace.add(key);
                queue.add(next);
            }
        }

        // Scan the wider radius for underground air NOT in the player's space.
        BlockPos nearest = null;
        double nearestDistSq = Double.MAX_VALUE;
        int r = SCAN_RADIUS;

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy * dy + dz * dz > r * r) continue;
                    BlockPos pos = origin.offset(dx, dy, dz);
                    long key = pos.asLong();
                    if (playerSpace.contains(key)) continue;
                    if (!level.isLoaded(pos)) continue;
                    if (!level.getBlockState(pos).isAir()) continue;
                    if (level.canSeeSky(pos)) continue; // ignore surface air

                    int airNeighbors = 0;
                    for (Direction dir : Direction.values()) {
                        BlockPos n = pos.relative(dir);
                        if (level.isLoaded(n) && level.getBlockState(n).isAir()) airNeighbors++;
                    }
                    if (airNeighbors < MIN_AIR_NEIGHBORS) continue;

                    double distSq = origin.distSqr(pos);
                    if (distSq < nearestDistSq) {
                        nearestDistSq = distSq;
                        nearest = pos.immutable();
                    }
                }
            }
        }

        return nearest;
    }
}
