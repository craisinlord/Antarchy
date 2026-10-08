package com.craisinlord.antarchy.content.portalgun;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PortalGunCollisionHelper {
    private static final double SEARCH_PADDING = 1.5D;
    private static final double PLANE_TOLERANCE = 0.06D;
    private static final double EDGE_PADDING = 0.2D;
    private static final double DEPTH_PADDING = 0.45D;
    private static final double BORDER_TOLERANCE = 0.02D;
    private static final double BORDER_THICKNESS = 0.0125D;

    private PortalGunCollisionHelper() {
    }

    public static Vec3 resolveCollision(Entity entity, AABB startBox, Vec3 movement, Vec3 collided) {
        if (movement.lengthSqr() <= 1.0E-7D) {
            return collided;
        }
        AABB pathBox = startBox.expandTowards(movement).inflate(SEARCH_PADDING);
        List<PortalGunPortalEntity> nearby = PortalGunPortalEntity.findPortalsNearBounds(entity.level(), pathBox);
        if (nearby.isEmpty()) {
            return collided;
        }
        Vec3 adjusted = collided;
        for (PortalGunPortalEntity portal : nearby) {
            Vec3 candidateAdjusted = resolveCollisionAgainstPortal(entity, startBox, movement, adjusted, portal);
            if (candidateAdjusted.distanceToSqr(movement) < adjusted.distanceToSqr(movement)) {
                adjusted = candidateAdjusted;
            }
        }
        return adjusted;
    }

    private static Vec3 resolveCollisionAgainstPortal(Entity entity, AABB startBox, Vec3 movement, Vec3 collided, PortalGunPortalEntity portal) {
        PortalGunPortalEntity linked = portal.getLinkedPortal();
        if (linked == null || !linked.isAlive() || portal.isTeleportCoolingDown(entity)) {
            return collided;
        }
        PortalGunWorldPortalShape shape = portal.getWorldPortalShape();
        if (canWalkIntoFloorPortal(startBox, movement, portal, shape)) {
            return movement;
        }
        Vec3 normal = portal.getNormalVec().normalize();
        double desiredNormal = movement.dot(normal);
        double collidedNormal = collided.dot(normal);
        if (desiredNormal >= -1.0E-5D || desiredNormal >= collidedNormal - 1.0E-5D) {
            return collided;
        }
        if (!canUsePortalPassThrough(entity, startBox, movement, portal, shape)) {
            return collided;
        }
        double restoreAmount = desiredNormal - collidedNormal;
        Vec3 adjusted = collided.add(normal.scale(restoreAmount));
        return applyBorderCollision(entity, startBox, adjusted, portal);
    }

    private static boolean canWalkIntoFloorPortal(AABB startBox, Vec3 movement, PortalGunPortalEntity portal, PortalGunWorldPortalShape shape) {
        Vec3 normal = portal.getNormalVec().normalize();
        if (Math.abs(normal.y) < 0.9D) {
            return false;
        }
        AABB endBox = startBox.move(movement);
        return overlapsPortalOpeningFromSurface(startBox, shape) || overlapsPortalOpeningFromSurface(endBox, shape);
    }

    private static boolean overlapsPortalOpeningFromSurface(AABB box, PortalGunWorldPortalShape shape) {
        if (!shape.intersectsPortalColumn(box, EDGE_PADDING, EDGE_PADDING, PLANE_TOLERANCE)) {
            return false;
        }
        double minDepth = Double.POSITIVE_INFINITY;
        double maxDepth = Double.NEGATIVE_INFINITY;
        for (Vec3 corner : corners(box)) {
            double depth = shape.localCoords(corner).depth();
            minDepth = Math.min(minDepth, depth);
            maxDepth = Math.max(maxDepth, depth);
        }
        return maxDepth > 0.0D && minDepth >= -PLANE_TOLERANCE;
    }

    private static boolean canUsePortalPassThrough(Entity entity, AABB startBox, Vec3 movement, PortalGunPortalEntity portal, PortalGunWorldPortalShape shape) {
        AABB endBox = startBox.move(movement);
        if (!portal.getPortalInsides(entity).intersects(startBox)
                && !portal.getPortalInsides(entity).intersects(endBox)
                && !intersectsPortalWindow(startBox, shape)
                && !intersectsPortalWindow(endBox, shape)
                && !segmentIntersectsPortalWindow(entity, portal, startBox, endBox)) {
            return false;
        }
        Vec3 startProbe = portal.teleportProbePosition(entity, startBox);
        Vec3 endProbe = portal.teleportProbePosition(entity, endBox);
        if (portal.crossesPortal(startProbe, endProbe)) {
            return true;
        }
        PortalGunWorldPortalShape.PortalLocalCoords startCoords = shape.localCoords(startProbe);
        PortalGunWorldPortalShape.PortalLocalCoords endCoords = shape.localCoords(endProbe);
        return startCoords.depth() > 0.0D
                && endCoords.depth() >= -PLANE_TOLERANCE
                && Math.abs(endCoords.horizontal()) <= shape.halfWidth() + EDGE_PADDING
                && Math.abs(endCoords.vertical()) <= shape.halfHeight() + EDGE_PADDING
                && projectedWindowOverlap(shape, startProbe, endProbe);
    }

    private static boolean intersectsPortalWindow(AABB box, PortalGunWorldPortalShape shape) {
        for (Vec3 corner : corners(box)) {
            PortalGunWorldPortalShape.PortalLocalCoords coords = shape.localCoords(corner);
            if (Math.abs(coords.horizontal()) <= shape.halfWidth() + EDGE_PADDING
                    && Math.abs(coords.vertical()) <= shape.halfHeight() + EDGE_PADDING
                    && coords.depth() <= DEPTH_PADDING
                    && coords.depth() >= -PLANE_TOLERANCE) {
                return true;
            }
        }
        return false;
    }

    private static boolean segmentIntersectsPortalWindow(Entity entity, PortalGunPortalEntity portal, AABB startBox, AABB endBox) {
        return portal.crossesPortal(portal.teleportProbePosition(entity, startBox), portal.teleportProbePosition(entity, endBox));
    }

    private static boolean projectedWindowOverlap(PortalGunWorldPortalShape shape, Vec3 startProbe, Vec3 endProbe) {
        PortalGunWorldPortalShape.PortalLocalCoords start = shape.localCoords(startProbe);
        PortalGunWorldPortalShape.PortalLocalCoords end = shape.localCoords(endProbe);
        return Math.abs(start.horizontal()) <= shape.halfWidth() + BORDER_TOLERANCE
                && Math.abs(start.vertical()) <= shape.halfHeight() + BORDER_TOLERANCE
                && Math.abs(end.horizontal()) <= shape.halfWidth() + BORDER_TOLERANCE
                && Math.abs(end.vertical()) <= shape.halfHeight() + BORDER_TOLERANCE;
    }

    private static Vec3 applyBorderCollision(Entity entity, AABB startBox, Vec3 movement, PortalGunPortalEntity portal) {
        if (movement.lengthSqr() <= 1.0E-7D) {
            return movement;
        }
        AABB endBox = startBox.move(movement);
        if (!portal.getPortalInsides(entity).intersects(startBox)
                && !portal.getPortalInsides(entity).intersects(endBox)
                && !intersectsPortalWindow(startBox, portal.getWorldPortalShape())
                && !intersectsPortalWindow(endBox, portal.getWorldPortalShape())) {
            return movement;
        }
        List<VoxelShape> shapes = portal.getWorldPortalShape().getCollisionBoundaries(BORDER_THICKNESS).stream()
                .map(Shapes::create)
                .toList();
        Vec3 collided = Entity.collideBoundingBox(entity, movement, startBox, entity.level(), shapes);
        return collided.distanceToSqr(movement) < 1.0E-7D ? movement : collided;
    }

    private static Vec3[] corners(AABB box) {
        return new Vec3[] {
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.maxZ)
        };
    }
}
