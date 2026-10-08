package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PortalGunTransitMath {
    public static final double EXIT_CLEARANCE = 0.02D;
    public static final double LEADING_FACE_TOLERANCE = 0.05D;
    private static final double UPWARD_EXIT_NORMAL_Y = 0.5D;

    private PortalGunTransitMath() {
    }

    public static double extentAlong(AABB box, Vec3 axis) {
        return (box.maxX - box.minX) * 0.5D * Math.abs(axis.x)
                + (box.maxY - box.minY) * 0.5D * Math.abs(axis.y)
                + (box.maxZ - box.minZ) * 0.5D * Math.abs(axis.z);
    }

    public static boolean fitsOpening(PortalGunWorldPortalShape shape, AABB box, double tolerance) {
        PortalGunWorldPortalShape.PortalLocalCoords center = shape.localCoords(box.getCenter());
        return Math.abs(center.horizontal()) + extentAlong(box, shape.right()) <= shape.halfWidth() + tolerance
                && Math.abs(center.vertical()) + extentAlong(box, shape.up()) <= shape.halfHeight() + tolerance;
    }

    public static boolean centredOverOpening(PortalGunWorldPortalShape shape, AABB box) {
        PortalGunWorldPortalShape.PortalLocalCoords center = shape.localCoords(box.getCenter());
        return Math.abs(center.horizontal()) <= shape.halfWidth() && Math.abs(center.vertical()) <= shape.halfHeight();
    }

    public static boolean crossesWithBody(PortalGunWorldPortalShape shape, AABB previousBox, AABB currentBox, Vec3 attemptedMovement) {
        double previousDepth = shape.localCoords(previousBox.getCenter()).depth();
        double currentDepth = shape.localCoords(currentBox.getCenter()).depth();
        if (previousDepth <= 0.0D) {
            return false;
        }
        boolean movingInward = attemptedMovement.dot(shape.normal()) < -1.0E-6D || currentDepth < previousDepth - 1.0E-6D;
        if (!movingInward) {
            return false;
        }
        if (currentDepth - extentAlong(currentBox, shape.normal()) > LEADING_FACE_TOLERANCE) {
            return false;
        }
        return centredOverOpening(shape, currentBox);
    }

    public static Vec3 transformVector(PortalGunWorldPortalShape source, PortalGunWorldPortalShape destination, Vec3 vector) {
        return PortalGunTransformUtil.transformVector(source, destination, vector);
    }

    public static AABB exitBox(PortalGunWorldPortalShape source, PortalGunWorldPortalShape destination, AABB box) {
        PortalGunWorldPortalShape.PortalLocalCoords local = source.localCoords(box.getCenter());
        double horizontal = clampLateral(-local.horizontal(), destination.halfWidth(), extentAlong(box, destination.right()));
        double vertical = clampLateral(local.vertical(), destination.halfHeight(), extentAlong(box, destination.up()));
        double depth = extentAlong(box, destination.normal()) + EXIT_CLEARANCE;
        Vec3 center = destination.center()
                .add(destination.right().scale(horizontal))
                .add(destination.up().scale(vertical))
                .add(destination.normal().scale(depth));
        Vec3 offset = center.subtract(box.getCenter());
        return box.move(offset);
    }

    public static Vec3 feetPosition(AABB box) {
        return new Vec3((box.minX + box.maxX) * 0.5D, box.minY, (box.minZ + box.maxZ) * 0.5D);
    }

    public static Vec3 entityPosition(AABB box, Entity entity) {
        return entityPosition(box, AntarchyGravityApi.isGravityInverted(entity));
    }

    public static Vec3 entityPosition(AABB box, boolean inverted) {
        if (!inverted) {
            return feetPosition(box);
        }
        return new Vec3((box.minX + box.maxX) * 0.5D, box.maxY, (box.minZ + box.maxZ) * 0.5D);
    }

    public static Vec3 entityVelocityToWorld(Entity entity, Vec3 velocity) {
        return entityVelocityToWorld(velocity, AntarchyGravityApi.isGravityInverted(entity));
    }

    public static Vec3 entityVelocityToWorld(Vec3 velocity, boolean inverted) {
        if (!inverted) {
            return velocity;
        }
        return AntarchyGravityRotationUtil.vecPlayerToWorld(velocity, AntarchyGravityDirection.UP);
    }

    public static Vec3 entityVelocityFromWorld(Entity entity, Vec3 velocity) {
        return entityVelocityFromWorld(velocity, AntarchyGravityApi.isGravityInverted(entity));
    }

    public static Vec3 entityVelocityFromWorld(Vec3 velocity, boolean inverted) {
        if (!inverted) {
            return velocity;
        }
        return AntarchyGravityRotationUtil.vecWorldToPlayer(velocity, AntarchyGravityDirection.UP);
    }

    public static Vec3 exitVelocity(PortalGunWorldPortalShape source, PortalGunWorldPortalShape destination, Vec3 velocity, double minUpwardExitSpeed, double maxExitSpeed) {
        Vec3 exit = transformVector(source, destination, velocity);
        Vec3 normal = destination.normal();
        if (minUpwardExitSpeed > 0.0D && normal.y > UPWARD_EXIT_NORMAL_Y) {
            double outward = exit.dot(normal);
            if (outward < minUpwardExitSpeed) {
                exit = exit.add(normal.scale(minUpwardExitSpeed - outward));
            }
        }
        if (maxExitSpeed > 0.0D && exit.lengthSqr() > maxExitSpeed * maxExitSpeed) {
            exit = exit.normalize().scale(maxExitSpeed);
        }
        return exit;
    }

    public static float exitFallDistance(float fallDistance, Vec3 exitVelocity) {
        return exitVelocity.y < 0.0D ? fallDistance : 0.0F;
    }

    public static float exitFallDistance(float fallDistance, Vec3 exitVelocity, Entity entity) {
        if (!AntarchyGravityApi.isGravityInverted(entity)) {
            return exitFallDistance(fallDistance, exitVelocity);
        }
        return exitVelocity.y > 0.0D ? fallDistance : 0.0F;
    }

    private static double clampLateral(double coordinate, double halfSize, double extent) {
        double limit = halfSize - extent;
        if (limit <= 0.0D) {
            return 0.0D;
        }
        return Math.max(-limit, Math.min(limit, coordinate));
    }
}
