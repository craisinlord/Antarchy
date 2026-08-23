package com.craisinlord.antarchy.content.portalgun;

import java.util.Set;
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
        Set<BlockPos> compensatedSpots
) {
    public static PortalGunPlacement fromStored(Direction facing, Direction upAxis, BlockPos masterPos, BlockPos basePos, Set<BlockPos> compensatedSpots) {
        Direction widthAxis = widthAxis(facing, upAxis);
        BlockPos supportOrigin = masterPos.relative(facing.getOpposite());
        Vec3 center = masterPos.getCenter()
                .add(Vec3.atLowerCornerOf(upAxis.getNormal()).scale(0.5D))
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
                new BlockPos[] {masterPos, basePos},
                compensatedSpots
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
}
