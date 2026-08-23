package com.craisinlord.antarchy.content.portalgun;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PortalGunTransformUtil {
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);
    private static final Vec3 WORLD_FORWARD_FALLBACK = new Vec3(0.0D, 0.0D, 1.0D);

    private PortalGunTransformUtil() {
    }

    public static Quaternionf createTransform(PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal) {
        return createTransform(sourcePortal.getWidthVec(), sourcePortal.getUpVec(), sourcePortal.getNormalVec().scale(-1.0D), destinationPortal.getWidthVec(), destinationPortal.getUpVec(), destinationPortal.getNormalVec());
    }

    public static Quaternionf createTransform(Vec3 sourceWidth, Vec3 sourceUp, Vec3 sourceForward, Vec3 destinationWidth, Vec3 destinationUp, Vec3 destinationForward) {
        Matrix3f sourceBasis = new Matrix3f(
                (float) sourceWidth.x, (float) sourceUp.x, (float) sourceForward.x,
                (float) sourceWidth.y, (float) sourceUp.y, (float) sourceForward.y,
                (float) sourceWidth.z, (float) sourceUp.z, (float) sourceForward.z
        );
        Matrix3f destinationBasis = new Matrix3f(
                (float) destinationWidth.x, (float) destinationUp.x, (float) destinationForward.x,
                (float) destinationWidth.y, (float) destinationUp.y, (float) destinationForward.y,
                (float) destinationWidth.z, (float) destinationUp.z, (float) destinationForward.z
        );
        Matrix3f transform = destinationBasis.mul(sourceBasis.transpose(new Matrix3f()), new Matrix3f());
        return transform.getNormalizedRotation(new Quaternionf());
    }

    public static Vec3 transform(Vec3 vector, Quaternionf quaternion) {
        Vector3f rotated = new Vector3f((float) vector.x, (float) vector.y, (float) vector.z).rotate(quaternion);
        return new Vec3(rotated.x(), rotated.y(), rotated.z());
    }

    public static Vec3 transformRelativePosition(PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, Vec3 relativePosition) {
        return transform(relativePosition, createTransform(sourcePortal, destinationPortal));
    }

    public static float yawFromLook(Vec3 look) {
        return (float) Math.toDegrees(Math.atan2(-look.x, look.z));
    }

    public static float pitchFromLook(Vec3 look) {
        return (float) Math.toDegrees(-Math.asin(Mth.clamp(look.y, -1.0D, 1.0D)));
    }

    public static Vec3 upVectorFromLookAndRoll(Vec3 look, float rollDegrees) {
        Vec3 normalizedLook = look.normalize();
        Vec3 referenceUp = Math.abs(normalizedLook.dot(WORLD_UP)) > 0.999D ? WORLD_FORWARD_FALLBACK : WORLD_UP;
        Vec3 right = referenceUp.cross(normalizedLook).normalize();
        Vec3 up = normalizedLook.cross(right).normalize();
        if (Math.abs(rollDegrees) <= 1.0E-4F) {
            return up;
        }
        Quaternionf roll = new Quaternionf().fromAxisAngleDeg((float) normalizedLook.x, (float) normalizedLook.y, (float) normalizedLook.z, rollDegrees);
        return transform(up, roll).normalize();
    }

    public static float rollFromOrientation(Vec3 look, Vec3 up) {
        Vec3 normalizedLook = look.normalize();
        Vec3 normalizedUp = up.normalize();
        Vec3 baseUp = upVectorFromLookAndRoll(normalizedLook, 0.0F);
        Vec3 projectedUp = normalizedUp.subtract(normalizedLook.scale(normalizedUp.dot(normalizedLook)));
        if (projectedUp.lengthSqr() < 1.0E-8D) {
            return 0.0F;
        }
        projectedUp = projectedUp.normalize();
        double sin = normalizedLook.dot(baseUp.cross(projectedUp));
        double cos = Mth.clamp(baseUp.dot(projectedUp), -1.0D, 1.0D);
        return normalizeDegrees((float) Math.toDegrees(Math.atan2(sin, cos)));
    }

    public static Quaternionf orientationQuaternion(Vec3 look, Vec3 up) {
        Vec3 forward = look.normalize();
        Vec3 correctedUp = up.subtract(forward.scale(up.dot(forward))).normalize();
        Vec3 right = correctedUp.cross(forward).normalize();
        Matrix3f basis = new Matrix3f(
                (float) right.x, (float) correctedUp.x, (float) (-forward.x),
                (float) right.y, (float) correctedUp.y, (float) (-forward.y),
                (float) right.z, (float) correctedUp.z, (float) (-forward.z)
        );
        return basis.getNormalizedRotation(new Quaternionf());
    }

    public static float normalizeDegrees(float degrees) {
        return Mth.wrapDegrees(degrees);
    }
}
