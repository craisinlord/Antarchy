package com.craisinlord.antarchy.content.portalgun;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/** Applies the destination portal plane as the near plane of a camera projection. */
public final class PortalGunProjectionUtil {
    public static final double MIN_EYE_PLANE_DISTANCE = 0.01D;

    private PortalGunProjectionUtil() {
    }

    public static Matrix4f clipAtExit(Matrix4f projection, Vec3 eye, Vec3 look, Vec3 up,
                                      Vec3 planePoint, Vec3 planeNormal) {
        Vector4f clipPlane = planeInCameraSpace(eye, look, up, planePoint, planeNormal);
        if (clipPlane.w() > -MIN_EYE_PLANE_DISTANCE) {
            return projection;
        }
        Vector4f q = new Vector4f(signNonZero(clipPlane.x()), signNonZero(clipPlane.y()), 1.0F, 1.0F)
                .mul(new Matrix4f(projection).invert());
        float planeCornerDot = clipPlane.dot(q);
        if (!Float.isFinite(planeCornerDot) || Math.abs(planeCornerDot) <= 1.0E-6F) {
            return projection;
        }
        float scale = 2.0F / planeCornerDot;
        Vector4f c = clipPlane.mul(scale, new Vector4f());
        Matrix4f clipped = new Matrix4f(projection);
        clipped.m02(c.x());
        clipped.m12(c.y());
        clipped.m22(c.z() + 1.0F);
        clipped.m32(c.w());
        return clipped;
    }

    private static Vector4f planeInCameraSpace(Vec3 eye, Vec3 look, Vec3 up, Vec3 planePoint, Vec3 planeNormal) {
        Vec3 forward = look.normalize();
        Vec3 correctedUp = up.subtract(forward.scale(up.dot(forward))).normalize();
        Vec3 right = forward.cross(correctedUp).normalize();
        Vec3 relativePoint = planePoint.subtract(eye);
        Vector4f point = new Vector4f(
                (float) relativePoint.dot(right),
                (float) relativePoint.dot(correctedUp),
                (float) -relativePoint.dot(forward),
                1.0F
        );
        Vec3 unitNormal = planeNormal.normalize();
        Vector4f normal = new Vector4f(
                (float) unitNormal.dot(right),
                (float) unitNormal.dot(correctedUp),
                (float) -unitNormal.dot(forward),
                0.0F
        );
        return new Vector4f(normal.x(), normal.y(), normal.z(), -normal.dot(point));
    }

    private static float signNonZero(float value) {
        return value >= 0.0F ? 1.0F : -1.0F;
    }
}
