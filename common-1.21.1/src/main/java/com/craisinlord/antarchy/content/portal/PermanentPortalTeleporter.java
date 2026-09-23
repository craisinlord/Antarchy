package com.craisinlord.antarchy.content.portal;

import com.craisinlord.antarchy.config.AntarchySettings;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class PermanentPortalTeleporter {
    private static final int SEARCH_RADIUS = 24;
    private static final int VERTICAL_SEARCH = 24;
    private static final int EXISTING_PORTAL_SEARCH_RADIUS = 128;

    private PermanentPortalTeleporter() {
    }

    public static DimensionTransition createTransition(ServerLevel sourceLevel, Entity entity, BlockPos portalPos, PermanentPortalType type) {
        ServerLevel destination = resolveDestination(sourceLevel, type);
        if (destination == null) {
            return new DimensionTransition(sourceLevel, entity, DimensionTransition.DO_NOTHING);
        }

        Vec3 arrival = findArrivalPosition(sourceLevel, entity, destination, portalPos, type);
        if (arrival == null) {
            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.antarchy.teleport_arrival_failed"), true);
            }
            return new DimensionTransition(sourceLevel, entity, DimensionTransition.DO_NOTHING);
        }
        return new DimensionTransition(destination, arrival, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
    }

    @Nullable
    private static ServerLevel resolveDestination(ServerLevel sourceLevel, PermanentPortalType type) {
        if (sourceLevel.dimension() == type.primaryDimension()) {
            return sourceLevel.getServer().overworld();
        }
        return sourceLevel.getServer().getLevel(type.primaryDimension());
    }

    @Nullable
    private static Vec3 findArrivalPosition(ServerLevel source, Entity entity, ServerLevel destination, BlockPos preferredPos, PermanentPortalType type) {
        double scale = DimensionType.getTeleportationScale(source.dimensionType(), destination.dimensionType());
        preferredPos = BlockPos.containing(preferredPos.getX() * scale, preferredPos.getY(), preferredPos.getZ() * scale);
        PermanentPortalShape existing = findExistingPortal(destination, preferredPos, type);
        if (existing != null) {
            Vec3 safePortalPosition = tryFindSafePosition(entity, destination, BlockPos.containing(existing.center()));
            return safePortalPosition != null ? safePortalPosition : existing.center();
        }

        Vec3 safe = findSafeArrivalPosition(entity, destination, preferredPos, type);
        if (safe != null) {
            PermanentPortalShape created = createReturnPortal(destination, safe, type);
            if (created != null) {
                return created.center();
            }
            return createFallbackPortal(destination, BlockPos.containing(safe), type);
        }

        return null;
    }

    @Nullable
    private static PermanentPortalShape findExistingPortal(ServerLevel level, BlockPos center, PermanentPortalType type) {
        PoiManager poiManager = level.getPoiManager();
        poiManager.ensureLoadedAndValid(level, center, EXISTING_PORTAL_SEARCH_RADIUS);
        Block portalBlock = type.portalBlock();
        List<BlockPos> candidates = poiManager.getInSquare(
                        holder -> holder.is(type.poiKey()), center, EXISTING_PORTAL_SEARCH_RADIUS, PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(level.getWorldBorder()::isWithinBounds)
                .sorted(Comparator.<BlockPos>comparingDouble(pos -> horizontalDistanceSqr(pos, center))
                        .thenComparingInt(pos -> Math.abs(pos.getY() - center.getY())))
                .toList();
        for (BlockPos pos : candidates) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(portalBlock) || !state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
                continue;
            }
            PermanentPortalShape shape = PermanentPortalShape.findActive(level, pos, type, state.getValue(BlockStateProperties.HORIZONTAL_AXIS));
            if (shape != null) {
                return shape;
            }
        }
        return null;
    }

    private static double horizontalDistanceSqr(BlockPos a, BlockPos b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dz * dz;
    }

    @Nullable
    private static Vec3 findSafeArrivalPosition(Entity entity, ServerLevel destination, BlockPos preferredPos, PermanentPortalType type) {
        int[] yRange = getDimensionYRange(destination, type);
        if (yRange != null) {
            return findSafeArrivalPositionInYRange(entity, destination, preferredPos, yRange[0], yRange[1]);
        }

        Set<BlockPos> candidates = new LinkedHashSet<>();
        boolean elythia = destination.dimension() == PermanentPortalType.ELYTHIA.primaryDimension();
        if (!elythia) {
            addCandidate(candidates, preferredPos);
        }

        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int xOff = -radius; xOff <= radius; xOff++) {
                for (int zOff = -radius; zOff <= radius; zOff++) {
                    if (radius > 0 && Math.abs(xOff) != radius && Math.abs(zOff) != radius) {
                        continue;
                    }

                    BlockPos horizontalPos = preferredPos.offset(xOff, 0, zOff);
                    if (destination.hasChunkAt(horizontalPos)) {
                        BlockPos surface = destination.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontalPos);
                        if (elythia) {
                            candidates.add(surface);
                            candidates.add(surface.above());
                        } else {
                            addCandidate(candidates, surface);
                            addCandidate(candidates, destination.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, horizontalPos));
                        }
                    }
                    if (!elythia) {
                        for (int yOff = 0; yOff <= VERTICAL_SEARCH; yOff++) {
                            addCandidate(candidates, horizontalPos.above(yOff));
                            if (yOff > 0) {
                                addCandidate(candidates, horizontalPos.below(yOff));
                            }
                        }
                    }
                }
            }
        }

        if (elythia) {
            addCandidate(candidates, preferredPos);
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
    private static int[] getDimensionYRange(ServerLevel destination, PermanentPortalType type) {
        ResourceKey<Level> dim = destination.dimension();
        if (dim == AntarchySettings.termiteDestinationDimension()) {
            return new int[]{100, 200};
        }
        if (type != PermanentPortalType.ELYTHIA && dim == AntarchySettings.brownAntDestinationDimension()) {
            return new int[]{75, 120};
        }
        return null;
    }

    private static boolean isValidArrivalPosition(ServerLevel destination, Vec3 safePos) {
        BlockPos standPos = BlockPos.containing(safePos);
        if (destination.dimension() == PermanentPortalType.ELYTHIA.primaryDimension()) {
            BlockPos supportPos = standPos.below();
            if (!destination.hasChunkAt(standPos) || !destination.hasChunkAt(supportPos)) {
                return false;
            }
            BlockState support = destination.getBlockState(supportPos);
            if (!support.isFaceSturdy(destination, supportPos, Direction.UP)
                    || support.is(BlockTags.LEAVES)
                    || !support.getFluidState().isEmpty()) {
                return false;
            }
            int nearbySupports = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighbor = supportPos.relative(direction);
                if (!destination.hasChunkAt(neighbor)) {
                    continue;
                }
                for (int yOff = -2; yOff <= 2; yOff++) {
                    BlockPos nearby = neighbor.offset(0, yOff, 0);
                    BlockState nearbyState = destination.getBlockState(nearby);
                    if (nearbyState.isFaceSturdy(destination, nearby, Direction.UP)
                            && !nearbyState.is(BlockTags.LEAVES)
                            && nearbyState.getFluidState().isEmpty()) {
                        nearbySupports++;
                        break;
                    }
                }
            }
            return nearbySupports >= 2;
        }
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
}
