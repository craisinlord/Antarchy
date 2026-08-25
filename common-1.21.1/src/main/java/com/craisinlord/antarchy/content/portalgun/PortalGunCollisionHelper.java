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
        List<Entity> nearby = entity.level().getEntities(entity, pathBox, candidate -> candidate instanceof PortalGunPortalEntity && candidate.isAlive());
        if (nearby.isEmpty()) {
            return collided;
        }
        Vec3 adjusted = collided;
        for (Entity candidate : nearby) {
            PortalGunPortalEntity portal = (PortalGunPortalEntity) candidate;
            Vec3 candidateAdjusted = resolveCollisionAgainstPortal(entity, startBox, movement, adjusted, portal);
            if (candidateAdjusted.distanceToSqr(movement) < adjusted.distanceToSqr(movement)) {
                adjusted = candidateAdjusted;
            }
        }
        return adjusted;
    }

    private static Vec3 resolveCollisionAgainstPortal(Entity entity, AABB startBox, Vec3 movement, Vec3 collided, PortalGunPortalEntity portal) {
        PortalGunWorldPortalShape shape = portal.getWorldPortalShape();
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

    private static boolean canUsePortalPassThrough(Entity entity, AABB startBox, Vec3 movement, PortalGunPortalEntity portal, PortalGunWorldPortalShape shape) {
        AABB endBox = startBox.move(movement);
        if (!portal.getPortalInsides(entity).intersects(startBox)
                && !portal.getPortalInsides(entity).intersects(endBox)
                && !intersectsPortalWindow(startBox, shape)
                && !intersectsPortalWindow(endBox, shape)
                && !segmentIntersectsPortalWindow(shape, startBox, endBox)) {
            return false;
        }
        if (crossesPortal(shape, entity, startBox, movement)) {
            return true;
        }
        Vec3 startCenter = startBox.getCenter();
        Vec3 endCenter = endBox.getCenter();
        PortalGunWorldPortalShape.PortalLocalCoords startCoords = shape.localCoords(startCenter);
        PortalGunWorldPortalShape.PortalLocalCoords endCoords = shape.localCoords(endCenter);
        return startCoords.depth() > 0.0D
                && endCoords.depth() >= -PLANE_TOLERANCE
                && Math.abs(endCoords.horizontal()) <= shape.halfWidth() + EDGE_PADDING
                && Math.abs(endCoords.vertical()) <= shape.halfHeight() + EDGE_PADDING
                && projectedWindowOverlap(shape, startBox, endBox);
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

    private static boolean segmentIntersectsPortalWindow(PortalGunWorldPortalShape shape, AABB startBox, AABB endBox) {
        for (Vec3 startCorner : corners(startBox)) {
            Vec3 endCorner = matchSample(startCorner, startBox, endBox);
            if (shape.crosses(startCorner, endCorner)) {
                return true;
            }
        }
        return false;
    }

    private static boolean projectedWindowOverlap(PortalGunWorldPortalShape shape, AABB startBox, AABB endBox) {
        double minHorizontal = Double.POSITIVE_INFINITY;
        double maxHorizontal = Double.NEGATIVE_INFINITY;
        double minVertical = Double.POSITIVE_INFINITY;
        double maxVertical = Double.NEGATIVE_INFINITY;
        for (Vec3 corner : corners(startBox)) {
            PortalGunWorldPortalShape.PortalLocalCoords coords = shape.localCoords(corner);
            minHorizontal = Math.min(minHorizontal, coords.horizontal());
            maxHorizontal = Math.max(maxHorizontal, coords.horizontal());
            minVertical = Math.min(minVertical, coords.vertical());
            maxVertical = Math.max(maxVertical, coords.vertical());
        }
        for (Vec3 corner : corners(endBox)) {
            PortalGunWorldPortalShape.PortalLocalCoords coords = shape.localCoords(corner);
            minHorizontal = Math.min(minHorizontal, coords.horizontal());
            maxHorizontal = Math.max(maxHorizontal, coords.horizontal());
            minVertical = Math.min(minVertical, coords.vertical());
            maxVertical = Math.max(maxVertical, coords.vertical());
        }
        return maxHorizontal >= -shape.halfWidth() - BORDER_TOLERANCE
                && minHorizontal <= shape.halfWidth() + BORDER_TOLERANCE
                && maxVertical >= -shape.halfHeight() - BORDER_TOLERANCE
                && minVertical <= shape.halfHeight() + BORDER_TOLERANCE;
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

    private static boolean crossesPortal(PortalGunWorldPortalShape shape, Entity entity, AABB startBox, Vec3 movement) {
        AABB endBox = startBox.move(movement);
        for (Vec3 startSample : portalSamples(entity, startBox, true)) {
            Vec3 endSample = matchSample(startSample, startBox, endBox);
            if (shape.crosses(startSample, endSample)) {
                return true;
            }
        }
        return false;
    }

    private static List<Vec3> portalSamples(Entity entity, AABB box, boolean includeEye) {
        Vec3 center = new Vec3((box.minX + box.maxX) * 0.5D, (box.minY + box.maxY) * 0.5D, (box.minZ + box.maxZ) * 0.5D);
        Vec3 topCenter = new Vec3(center.x, box.maxY, center.z);
        Vec3 bottomCenter = new Vec3(center.x, box.minY, center.z);
        if (includeEye) {
            Vec3 eye = new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ());
            return List.of(center, eye, topCenter, bottomCenter);
        }
        return List.of(center, topCenter, bottomCenter);
    }

    private static Vec3 matchSample(Vec3 startSample, AABB startBox, AABB endBox) {
        double xLerp = axisRatio(startSample.x, startBox.minX, startBox.maxX);
        double yLerp = axisRatio(startSample.y, startBox.minY, startBox.maxY);
        double zLerp = axisRatio(startSample.z, startBox.minZ, startBox.maxZ);
        return new Vec3(
                lerp(endBox.minX, endBox.maxX, xLerp),
                lerp(endBox.minY, endBox.maxY, yLerp),
                lerp(endBox.minZ, endBox.maxZ, zLerp)
        );
    }

    private static double axisRatio(double value, double min, double max) {
        double size = max - min;
        if (Math.abs(size) < 1.0E-6D) {
            return 0.5D;
        }
        return (value - min) / size;
    }

    private static double lerp(double min, double max, double delta) {
        return min + (max - min) * delta;
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
