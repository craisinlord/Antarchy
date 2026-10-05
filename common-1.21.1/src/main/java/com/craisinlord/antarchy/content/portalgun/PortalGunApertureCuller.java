package com.craisinlord.antarchy.content.portalgun;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Conservative side planes through the destination opening and virtual eye.
 * The exit plane itself is handled separately so bounds crossing it remain visible.
 */
public final class PortalGunApertureCuller {
    private static final double MIN_EYE_DISTANCE = 0.05D;
    private static final double PLANE_PADDING = 0.5D;

    private final Vec3 eye;
    private final Vec3[] inwardNormals;

    private PortalGunApertureCuller(Vec3 eye, Vec3[] inwardNormals) {
        this.eye = eye;
        this.inwardNormals = inwardNormals;
    }

    public static PortalGunApertureCuller create(PortalGunWorldPortalShape aperture, Vec3 eye) {
        double eyeDepth = aperture.localCoords(eye).depth();
        if (eyeDepth >= -MIN_EYE_DISTANCE) {
            return new PortalGunApertureCuller(eye, null);
        }
        Vec3[] corners = aperture.getCorners(0.0D);
        Vec3 centerRay = aperture.center().subtract(eye);
        Vec3[] normals = new Vec3[corners.length];
        for (int index = 0; index < corners.length; index++) {
            Vec3 first = corners[index].subtract(eye);
            Vec3 second = corners[(index + 1) % corners.length].subtract(eye);
            Vec3 normal = first.cross(second);
            if (normal.lengthSqr() < 1.0E-8D) {
                return new PortalGunApertureCuller(eye, null);
            }
            normal = normal.normalize();
            if (normal.dot(centerRay) < 0.0D) {
                normal = normal.scale(-1.0D);
            }
            normals[index] = normal;
        }
        return new PortalGunApertureCuller(eye, normals);
    }

    public boolean intersects(AABB bounds) {
        if (inwardNormals == null) {
            return true;
        }
        for (Vec3 normal : inwardNormals) {
            double supportX = normal.x >= 0.0D ? bounds.maxX : bounds.minX;
            double supportY = normal.y >= 0.0D ? bounds.maxY : bounds.minY;
            double supportZ = normal.z >= 0.0D ? bounds.maxZ : bounds.minZ;
            double maxDistance = (supportX - eye.x) * normal.x
                    + (supportY - eye.y) * normal.y
                    + (supportZ - eye.z) * normal.z;
            if (maxDistance < -PLANE_PADDING) {
                return false;
            }
        }
        return true;
    }
}
