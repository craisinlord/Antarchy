package com.craisinlord.antarchy.content.gravity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class AntarchyGravityRotationUtil {
    private AntarchyGravityRotationUtil() {
    }

    public static Vec3 vecWorldToPlayer(Vec3 vector, AntarchyGravityDirection direction) {
        if (!direction.isInverted()) {
            return vector;
        }

        return new Vec3(-vector.x, -vector.y, vector.z);
    }

    public static Vec3 vecWorldToPlayer(double x, double y, double z, AntarchyGravityDirection direction) {
        return vecWorldToPlayer(new Vec3(x, y, z), direction);
    }

    public static Vec3 vecPlayerToWorld(Vec3 vector, AntarchyGravityDirection direction) {
        if (!direction.isInverted()) {
            return vector;
        }

        return new Vec3(-vector.x, -vector.y, vector.z);
    }

    public static Vec3 vecPlayerToWorld(double x, double y, double z, AntarchyGravityDirection direction) {
        return vecPlayerToWorld(new Vec3(x, y, z), direction);
    }

    public static Vec3 maskPlayerToWorld(Vec3 vector, AntarchyGravityDirection direction) {
        return vector;
    }

    public static Vec3 getEyeOffset(Entity entity, double eyeHeight) {
        return vecPlayerToWorld(new Vec3(0.0D, eyeHeight, 0.0D), AntarchyGravityApi.getGravityDirection(entity));
    }

    public static Vec3 getEyeOffset(Entity entity, double eyeHeight, float partialTick) {
        AntarchyGravityDirection direction = AntarchyGravityApi.getGravityDirection(entity);
        if (!direction.isInverted() && AntarchyGravityApi.getGravityFlipProgress(entity, partialTick) <= 0.0F) {
            return new Vec3(0.0D, eyeHeight, 0.0D);
        }

        Vector3f offset = new Vector3f(0.0F, (float) eyeHeight, 0.0F);
        offset.rotate(getWorldRotationQuaternion(entity, partialTick));
        return new Vec3(offset.x(), offset.y(), offset.z());
    }

    public static AABB boxPlayerToWorld(AABB box, AntarchyGravityDirection direction) {
        if (!direction.isInverted()) {
            return box;
        }

        return new AABB(
                vecPlayerToWorld(new Vec3(box.minX, box.minY, box.minZ), direction),
                vecPlayerToWorld(new Vec3(box.maxX, box.maxY, box.maxZ), direction)
        );
    }

    public static AABB boxWorldToPlayer(AABB box, AntarchyGravityDirection direction) {
        if (!direction.isInverted()) {
            return box;
        }

        return new AABB(
                vecWorldToPlayer(new Vec3(box.minX, box.minY, box.minZ), direction),
                vecWorldToPlayer(new Vec3(box.maxX, box.maxY, box.maxZ), direction)
        );
    }

    public static Quaternionf getWorldRotationQuaternion(Entity entity, float partialTick) {
        return rotationAroundNegativeZ(180.0F * AntarchyGravityApi.getGravityFlipProgress(entity, partialTick));
    }

    public static Quaternionf getCameraRotationQuaternion(Entity entity, float partialTick) {
        return rotationAroundNegativeZ(-180.0F * AntarchyGravityApi.getGravityFlipProgress(entity, partialTick));
    }

    public static Vec2 rotWorldToPlayer(float yaw, float pitch, AntarchyGravityDirection direction) {
        Vec3 vector = vecWorldToPlayer(rotToVec(yaw, pitch), direction);
        return vecToRot(vector);
    }

    public static Vec2 rotPlayerToWorld(float yaw, float pitch, AntarchyGravityDirection direction) {
        Vec3 vector = vecPlayerToWorld(rotToVec(yaw, pitch), direction);
        return vecToRot(vector);
    }

    public static Direction getGravityDownDirection(Entity entity) {
        return AntarchyGravityApi.isGravityInverted(entity) ? Direction.UP : Direction.DOWN;
    }

    public static BlockPos getSupportingBlockPos(Entity entity, double offset) {
        Direction direction = getGravityDownDirection(entity);
        Vec3 pos = entity.position().add(
                direction.getStepX() * offset,
                direction.getStepY() * offset,
                direction.getStepZ() * offset
        );
        return BlockPos.containing(pos);
    }

    private static Quaternionf rotationAroundNegativeZ(float degrees) {
        return new Quaternionf().fromAxisAngleDeg(0.0F, 0.0F, -1.0F, degrees);
    }

    private static Vec3 rotToVec(float yaw, float pitch) {
        double radPitch = pitch * 0.017453292F;
        double radNegYaw = -yaw * 0.017453292F;
        double cosNegYaw = Math.cos(radNegYaw);
        double sinNegYaw = Math.sin(radNegYaw);
        double cosPitch = Math.cos(radPitch);
        double sinPitch = Math.sin(radPitch);
        return new Vec3(sinNegYaw * cosPitch, -sinPitch, cosNegYaw * cosPitch);
    }

    private static Vec2 vecToRot(Vec3 vector) {
        double sinPitch = -vector.y;
        double radPitch = Math.asin(sinPitch);
        double cosPitch = Math.cos(radPitch);
        double sinNegYaw = vector.x / cosPitch;
        double cosNegYaw = Math.max(-1.0D, Math.min(1.0D, vector.z / cosPitch));
        double radNegYaw = Math.acos(cosNegYaw);
        if (sinNegYaw < 0.0D) {
            radNegYaw = Math.PI * 2.0D - radNegYaw;
        }

        return new Vec2((float) (-radNegYaw / 0.017453292F), (float) (radPitch / 0.017453292F));
    }
}
