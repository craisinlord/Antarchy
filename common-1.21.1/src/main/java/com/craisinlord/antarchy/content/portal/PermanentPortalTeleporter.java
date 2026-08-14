package com.craisinlord.antarchy.content.portal;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PermanentPortalTeleporter {
    private static final int SEARCH_RADIUS = 24;
    private static final int VERTICAL_SEARCH = 24;
    private static final int EXISTING_PORTAL_SEARCH_RADIUS = 48;

    private PermanentPortalTeleporter() {
    }

    public static void teleport(Entity entity, PermanentPortalType type) {
        if (!(entity.level() instanceof ServerLevel sourceLevel) || !entity.isAlive()) {
            return;
        }

        DimensionTransition transition = createTransition(sourceLevel, entity, entity.blockPosition(), type);
        Entity movedEntity = moveEntity(entity, transition);
        if (movedEntity != null) {
            movedEntity.setPortalCooldown();
        }
    }

    public static DimensionTransition createTransition(ServerLevel sourceLevel, Entity entity, BlockPos portalPos, PermanentPortalType type) {
        ServerLevel destination = resolveDestination(sourceLevel, type);
        if (destination == null) {
            return new DimensionTransition(sourceLevel, entity, DimensionTransition.DO_NOTHING);
        }

        Vec3 arrival = findArrivalPosition(entity, destination, portalPos, type);
        return new DimensionTransition(destination, arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
    }

    @Nullable
    private static ServerLevel resolveDestination(ServerLevel sourceLevel, PermanentPortalType type) {
        if (sourceLevel.dimension() == type.primaryDimension()) {
            return sourceLevel.getServer().overworld();
        }
        return sourceLevel.getServer().getLevel(type.primaryDimension());
    }

    private static Vec3 findArrivalPosition(Entity entity, ServerLevel destination, BlockPos preferredPos, PermanentPortalType type) {
        PermanentPortalShape active = PermanentPortalShape.findActiveNear(destination, preferredPos, type);
        if (active != null) {
            return active.center();
        }

        Vec3 safe = findSafeArrivalPosition(entity, destination, preferredPos);
        if (safe != null) {
            PermanentPortalShape nearbyActive = findActiveNearby(destination, BlockPos.containing(safe), type);
            if (nearbyActive != null) {
                return nearbyActive.center();
            }

            PermanentPortalShape created = createReturnPortal(destination, safe, type);
            if (created != null) {
                return created.center();
            }
            return createFallbackPortal(destination, BlockPos.containing(safe), type);
        }

        return createFallbackPortal(destination, preferredPos, type);
    }

    @Nullable
    private static PermanentPortalShape findActiveNearby(ServerLevel level, BlockPos center, PermanentPortalType type) {
        for (int radius = 0; radius <= EXISTING_PORTAL_SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int yOff = -radius; yOff <= radius; yOff++) {
                    for (int zOff = -radius; zOff <= radius; zOff++) {
                        if (radius > 0 && Math.abs(xOff) != radius && Math.abs(yOff) != radius && Math.abs(zOff) != radius) {
                            continue;
                        }

                        BlockPos candidate = center.offset(xOff, yOff, zOff);
                        PermanentPortalShape xShape = PermanentPortalShape.findActive(level, candidate, type, Direction.Axis.X);
                        if (xShape != null) {
                            return xShape;
                        }
                        PermanentPortalShape zShape = PermanentPortalShape.findActive(level, candidate, type, Direction.Axis.Z);
                        if (zShape != null) {
                            return zShape;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private static Vec3 findSafeArrivalPosition(Entity entity, ServerLevel destination, BlockPos preferredPos) {
        int[] yRange = getDimensionYRange(destination);
        if (yRange != null) {
            return findSafeArrivalPositionInYRange(entity, destination, preferredPos, yRange[0], yRange[1]);
        }

        Set<BlockPos> candidates = new LinkedHashSet<>();
        addCandidate(candidates, preferredPos);

        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                        continue;
                    }

                    BlockPos horizontalPos = preferredPos.offset(xOff, 0, zOff);
                    if (destination.hasChunkAt(horizontalPos)) {
                        addCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontalPos));
                        addCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, horizontalPos));
                    }
                    for (int yOff = 0; yOff <= VERTICAL_SEARCH; yOff++) {
                        addCandidate(candidates, horizontalPos.above(yOff));
                        if (yOff > 0) {
                            addCandidate(candidates, horizontalPos.below(yOff));
                        }
                    }
                }
            }
        }

        for (BlockPos candidate : candidates) {
            Vec3 safe = tryFindSafePosition(entity, destination, candidate);
            if (safe != null) {
                return safe;
            }
        }

        return null;
    }

    @Nullable
    private static Vec3 findSafeArrivalPositionInYRange(Entity entity, ServerLevel destination, BlockPos preferredPos, int minY, int maxY) {
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                        continue;
                    }

                    int x = preferredPos.getX() + xOff;
                    int z = preferredPos.getZ() + zOff;
                    for (int y = maxY; y >= minY + 1; y--) {
                        Vec3 safePos = tryFindSafePosition(entity, destination, new BlockPos(x, y, z));
                        if (safePos != null && safePos.y >= minY && safePos.y <= maxY) {
                            return safePos;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void addCandidate(Set<BlockPos> candidates, @Nullable BlockPos pos) {
        if (pos == null) {
            return;
        }

        candidates.add(pos);
        candidates.add(pos.above());
        if (pos.getY() > Integer.MIN_VALUE) {
            candidates.add(pos.below());
        }
    }

    @Nullable
    private static Vec3 tryFindSafePosition(Entity entity, ServerLevel destination, BlockPos candidate) {
        if (!isSafeDismountAreaLoaded(destination, candidate)) {
            return null;
        }

        Vec3 safe = DismountHelper.findSafeDismountLocation(entity.getType(), destination, candidate, true);
        if (safe != null && isValidArrivalPosition(destination, safe)) {
            return safe;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos offsetCandidate = candidate.relative(direction);
            if (!isSafeDismountAreaLoaded(destination, offsetCandidate)) {
                continue;
            }

            safe = DismountHelper.findSafeDismountLocation(entity.getType(), destination, offsetCandidate, true);
            if (safe != null && isValidArrivalPosition(destination, safe)) {
                return safe;
            }
        }

        return null;
    }

    private static Vec3 createFallbackPortal(ServerLevel destination, BlockPos preferredPos, PermanentPortalType type) {
        int minY = destination.getMinBuildHeight() + 2;
        int maxY = destination.getMaxBuildHeight() - 4;
        BlockPos center = new BlockPos(preferredPos.getX(), Mth.clamp(preferredPos.getY(), minY, maxY), preferredPos.getZ());
        BlockState platformState = type.platformBlock().defaultBlockState();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                destination.setBlock(center.offset(dx, -1, dz), platformState, Block.UPDATE_ALL);
                for (int dy = 0; dy <= 4; dy++) {
                    destination.removeBlock(center.offset(dx, dy, dz), false);
                }
            }
        }

        PermanentPortalShape xShape = PermanentPortalShape.create(destination, center, type, Direction.Axis.X);
        if (xShape != null) {
            return xShape.center();
        }

        PermanentPortalShape zShape = PermanentPortalShape.create(destination, center, type, Direction.Axis.Z);
        if (zShape != null) {
            return zShape.center();
        }

        return Vec3.atBottomCenterOf(center);
    }

    @Nullable
    private static PermanentPortalShape createReturnPortal(ServerLevel destination, Vec3 safePos, PermanentPortalType type) {
        BlockPos preferred = BlockPos.containing(safePos);
        int minY = destination.getMinBuildHeight() + 2;
        int maxY = destination.getMaxBuildHeight() - 4;
        int startY = Mth.clamp(preferred.getY(), minY, maxY);

        for (int yOff = 0; yOff <= 6; yOff++) {
            for (int y : new int[]{startY + yOff, startY - yOff}) {
                if (y < minY || y > maxY) {
                    continue;
                }

                for (int radius = 0; radius <= 4; radius++) {
                    for (int xOff = -radius; xOff <= radius; xOff++) {
                        for (int zOff = -radius; zOff <= radius; zOff++) {
                            if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                                continue;
                            }

                            BlockPos anchor = new BlockPos(preferred.getX() + xOff, y, preferred.getZ() + zOff);
                            PermanentPortalShape xShape = PermanentPortalShape.create(destination, anchor, type, Direction.Axis.X);
                            if (xShape != null) {
                                return xShape;
                            }
                            PermanentPortalShape zShape = PermanentPortalShape.create(destination, anchor, type, Direction.Axis.Z);
                            if (zShape != null) {
                                return zShape;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    private static int[] getDimensionYRange(ServerLevel destination) {
        ResourceKey<Level> dim = destination.dimension();
        if (dim == AntarchySettings.termiteDestinationDimension()) {
            return new int[]{100, 200};
        }
        if (dim == AntarchySettings.brownAntDestinationDimension()) {
            return new int[]{75, 120};
        }
        return null;
    }

    private static boolean isValidArrivalPosition(ServerLevel destination, Vec3 safePos) {
        BlockPos standPos = BlockPos.containing(safePos);
        if (destination.dimension() == AntarchySettings.termiteDestinationDimension()
                || destination.dimension() == AntarchySettings.redAntDestinationDimension()) {
            if (!destination.hasChunkAt(standPos) || !destination.hasChunkAt(standPos.below())) {
                return false;
            }

            return !destination.getBlockState(standPos.below()).is(net.minecraft.world.level.block.Blocks.BEDROCK);
        }
        return true;
    }

    private static boolean isSafeDismountAreaLoaded(ServerLevel destination, BlockPos center) {
        if (!destination.hasChunkAt(center) || !destination.hasChunkAt(center.above()) || !destination.hasChunkAt(center.below())) {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos offset = center.relative(direction);
            if (!destination.hasChunkAt(offset) || !destination.hasChunkAt(offset.above()) || !destination.hasChunkAt(offset.below())) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    private static Entity moveEntity(Entity entity, DimensionTransition transition) {
        ServerLevel destination = transition.newLevel();
        Vec3 destinationPos = transition.pos();
        if (entity instanceof ServerPlayer player) {
            player.teleportTo(destination, destinationPos.x, destinationPos.y, destinationPos.z, player.getYRot(), player.getXRot());
            return player;
        }

        if (entity.level() == destination) {
            entity.teleportTo(destinationPos.x, destinationPos.y, destinationPos.z);
            return entity;
        }

        return entity.changeDimension(transition);
    }
}
