package com.craisinlord.antarchy.content.portalgun;

import java.util.Set;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public record PortalGunPlacement(
        Vec3 center,
        Direction facing,
        Direction upAxis,
        Direction widthAxis,
        float yaw,
        BlockPos supportOrigin,
        BlockPos masterPos,
        BlockPos basePos,
        BlockPos[] portalSpots,
        Set<BlockPos> compensatedSpots,
        int width,
        int height
) {
    public static PortalGunPlacement fromStored(Direction facing, Direction upAxis, BlockPos masterPos, BlockPos basePos, BlockPos[] portalSpots, Set<BlockPos> compensatedSpots, int width, int height) {
        Direction widthAxis = widthAxis(facing, upAxis);
        BlockPos supportOrigin = masterPos.relative(facing.getOpposite());
        Vec3 center = Vec3.ZERO;
        for (BlockPos portalSpot : portalSpots) {
            center = center.add(portalSpot.getCenter());
        }
        center = center.scale(1.0D / portalSpots.length)
                .subtract(Vec3.atLowerCornerOf(facing.getNormal()).scale(0.5D));
        return new PortalGunPlacement(
                center,
                facing,
                upAxis,
                widthAxis,
                yawFor(facing, upAxis),
                supportOrigin,
                masterPos,
                basePos,
                portalSpots,
                compensatedSpots,
                width,
                height
        );
    }

    public static Direction widthAxis(Direction facing, Direction upAxis) {
        Vec3 normal = Vec3.atLowerCornerOf(facing.getNormal());
        Vec3 up = Vec3.atLowerCornerOf(upAxis.getNormal());
        Vec3 width = normal.cross(up);
        return Direction.getNearest(width.x, width.y, width.z);
    }

    public static float yawFor(Direction facing, Direction upAxis) {
        return facing.getAxis() == Direction.Axis.Y ? upAxis.toYRot() : facing.toYRot();
    }

    public static boolean isRectangularFootprint(BlockPos[] portalSpots, Direction facing, Direction upAxis, int width, int height) {
        if (portalSpots == null || portalSpots.length != width * height || portalSpots.length == 0) {
            return false;
        }
        Direction widthAxis = widthAxis(facing, upAxis);
        BlockPos origin = portalSpots[0];
        Set<Long> cells = new HashSet<>(portalSpots.length);
        int minWidth = Integer.MAX_VALUE;
        int maxWidth = Integer.MIN_VALUE;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        for (BlockPos spot : portalSpots) {
            if (spot == null) {
                return false;
            }
            int dx = spot.getX() - origin.getX();
            int dy = spot.getY() - origin.getY();
            int dz = spot.getZ() - origin.getZ();
            int depth = dx * facing.getStepX() + dy * facing.getStepY() + dz * facing.getStepZ();
            int widthCell = dx * widthAxis.getStepX() + dy * widthAxis.getStepY() + dz * widthAxis.getStepZ();
            int heightCell = dx * upAxis.getStepX() + dy * upAxis.getStepY() + dz * upAxis.getStepZ();
            if (depth != 0 || !cells.add(((long) widthCell << 32) ^ (heightCell & 0xffffffffL))) {
                return false;
            }
            minWidth = Math.min(minWidth, widthCell);
            maxWidth = Math.max(maxWidth, widthCell);
            minHeight = Math.min(minHeight, heightCell);
            maxHeight = Math.max(maxHeight, heightCell);
        }
        if (maxWidth - minWidth != width - 1 || maxHeight - minHeight != height - 1
                || (minWidth != 0 && maxWidth != 0) || (minHeight != 0 && maxHeight != 0)) {
            return false;
        }
        for (int widthCell = minWidth; widthCell <= maxWidth; widthCell++) {
            for (int heightCell = minHeight; heightCell <= maxHeight; heightCell++) {
                if (!cells.contains(((long) widthCell << 32) ^ (heightCell & 0xffffffffL))) {
                    return false;
                }
            }
        }
        return true;
    }
}
