package com.craisinlord.antarchy.content.portal;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class CrossDimensionTravelApi {
    private static final int HORIZONTAL_SEARCH_RADIUS = 8;
    private static final int VERTICAL_SEARCH_RADIUS = 8;

    private CrossDimensionTravelApi() {
    }

    public static boolean transferToSafePosition(Entity entity, ServerLevel destination, BlockPos preferred) {
        if (!entity.isAlive() || !(entity.level() instanceof ServerLevel source)) {
            return false;
        }
        Vec3 arrival = findSafePosition(entity, destination, preferred);
        if (arrival == null) {
            return false;
        }
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(destination, arrival.x, arrival.y, arrival.z, player.getYRot(), player.getXRot());
            player.setPortalCooldown();
            return true;
        }
        if (source == destination) {
            entity.teleportTo(arrival.x, arrival.y, arrival.z);
            entity.setPortalCooldown();
            return true;
        }
        Entity moved = entity.changeDimension(new DimensionTransition(
                destination,
                arrival,
                Vec3.ZERO,
                entity.getYRot(),
                entity.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND
        ));
        if (moved != null) {
            moved.setPortalCooldown();
            return true;
        }
        return false;
    }

    @Nullable
    public static Vec3 findSafePosition(Entity entity, ServerLevel destination, BlockPos preferred) {
        Set<BlockPos> candidates = new LinkedHashSet<>();
        candidates.add(preferred);
        for (int radius = 0; radius <= HORIZONTAL_SEARCH_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (radius > 0 && Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }
                    BlockPos column = preferred.offset(x, 0, z);
                    if (destination.hasChunkAt(column)) {
                        BlockPos surface = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
                        candidates.add(surface);
                        candidates.add(surface.above());
                    }
                    for (int y = 0; y <= VERTICAL_SEARCH_RADIUS; y++) {
                        candidates.add(column.above(y));
                        if (y > 0) {
                            candidates.add(column.below(y));
                        }
                    }
                }
            }
        }
        for (BlockPos candidate : candidates) {
            if (!isAreaLoaded(destination, candidate)) {
                continue;
            }
            Vec3 safe = DismountHelper.findSafeDismountLocation(entity.getType(), destination, candidate, true);
            if (safe != null && isValidPosition(destination, safe)) {
                return safe;
            }
        }
        return null;
    }

    private static boolean isAreaLoaded(ServerLevel level, BlockPos center) {
        if (center.getY() <= level.getMinBuildHeight() || center.getY() >= level.getMaxBuildHeight() - 1
                || !level.getWorldBorder().isWithinBounds(center)) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (!level.hasChunkAt(center.relative(direction))) {
                return false;
            }
        }
        return level.hasChunkAt(center) && level.hasChunkAt(center.above());
    }

    private static boolean isValidPosition(ServerLevel level, Vec3 position) {
        BlockPos feet = BlockPos.containing(position);
        BlockPos head = feet.above();
        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(head);
        return feetState.getFluidState().isEmpty() && headState.getFluidState().isEmpty()
                && feetState.getCollisionShape(level, feet).isEmpty()
                && headState.getCollisionShape(level, head).isEmpty();
    }
}
