package com.craisinlord.antarchy.content.gravity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class AntarchyGravityApi {
    public interface GravitySyncDispatcher {
        void onGravityStateChanged(Entity entity);
    }

    private static GravitySyncDispatcher syncDispatcher = entity -> {
    };

    private AntarchyGravityApi() {
    }

    public static AntarchyGravityDirection getGravityDirection(Entity entity) {
        return access(entity).antarchy$getGravityDirection();
    }

    public static AntarchyGravityDirection getPrevGravityDirection(Entity entity) {
        return access(entity).antarchy$getPrevGravityDirection();
    }

    public static boolean isGravityForced(Entity entity) {
        return access(entity).antarchy$isGravityForced();
    }

    public static boolean isGravityInverted(Entity entity) {
        return getGravityDirection(entity).isInverted();
    }

    public static int getTransitionDuration(Entity entity) {
        return access(entity).antarchy$getGravityTransitionDuration();
    }

    public static int getTransitionRemaining(Entity entity) {
        return access(entity).antarchy$getGravityTransitionRemaining();
    }

    public static float getGravityFlipProgress(Entity entity, float partialTick) {
        AntarchyGravityAccess access = access(entity);
        AntarchyGravityDirection current = access.antarchy$getGravityDirection();
        AntarchyGravityDirection previous = access.antarchy$getPrevGravityDirection();
        int duration = access.antarchy$getGravityTransitionDuration();
        int remaining = access.antarchy$getGravityTransitionRemaining();
        if (duration <= 0 || current == previous) {
            return current.isInverted() ? 1.0F : 0.0F;
        }

        float elapsed = duration - remaining + partialTick;
        float fraction = Math.max(0.0F, Math.min(1.0F, elapsed / duration));
        return previous.isInverted() ? 1.0F - fraction : fraction;
    }

    public static void setGravityDirection(Entity entity, AntarchyGravityDirection direction) {
        setGravityDirection(entity, direction, false, AntarchyGravityTransition.INSTANT);
    }

    public static void setGravityDirection(Entity entity, AntarchyGravityDirection direction, AntarchyGravityTransition transition) {
        setGravityDirection(entity, direction, false, transition);
    }

    public static void setGravityDirection(Entity entity, AntarchyGravityDirection direction, boolean forced) {
        setGravityDirection(entity, direction, forced, AntarchyGravityTransition.INSTANT);
    }

    public static void setGravityDirection(Entity entity, AntarchyGravityDirection direction, boolean forced, AntarchyGravityTransition transition) {
        access(entity).antarchy$setGravityState(direction, forced, transition);
    }

    public static void setForcedGravityDirection(Entity entity, AntarchyGravityDirection direction) {
        setForcedGravityDirection(entity, direction, AntarchyGravityTransition.INSTANT);
    }

    public static void setForcedGravityDirection(Entity entity, AntarchyGravityDirection direction, AntarchyGravityTransition transition) {
        access(entity).antarchy$setGravityState(direction, true, transition);
    }

    public static void clearForcedGravity(Entity entity) {
        access(entity).antarchy$setGravityState(AntarchyGravityDirection.DOWN, false, AntarchyGravityTransition.INSTANT);
    }

    public static void applySyncedState(
            Entity entity,
            AntarchyGravityDirection direction,
            AntarchyGravityDirection previousDirection,
            boolean forced,
            int transitionDuration,
            int transitionRemaining
    ) {
        access(entity).antarchy$applySyncedGravityState(direction, previousDirection, forced, transitionDuration, transitionRemaining);
    }

    public static void setSyncDispatcher(GravitySyncDispatcher dispatcher) {
        syncDispatcher = dispatcher;
    }

    public static void notifyGravityStateChanged(Entity entity) {
        syncDispatcher.onGravityStateChanged(entity);
    }

    public static Vec3 getWorldVelocity(Entity entity) {
        return AntarchyGravityRotationUtil.vecPlayerToWorld(entity.getDeltaMovement(), getGravityDirection(entity));
    }

    public static void setWorldVelocity(Entity entity, Vec3 worldVelocity) {
        entity.setDeltaMovement(AntarchyGravityRotationUtil.vecWorldToPlayer(worldVelocity, getGravityDirection(entity)));
    }

    public static Vec3 getEyeOffset(Entity entity) {
        return AntarchyGravityRotationUtil.getEyeOffset(entity, entity.getEyeHeight());
    }

    public static double eyeX(Entity entity) {
        return isGravityInverted(entity) ? entity.getEyePosition().x : entity.getX();
    }

    public static double eyeY(Entity entity) {
        return isGravityInverted(entity) ? entity.getEyePosition().y : entity.getY();
    }

    public static double eyeZ(Entity entity) {
        return isGravityInverted(entity) ? entity.getEyePosition().z : entity.getZ();
    }

    public static Vec3 deltaMovement(net.minecraft.world.entity.LivingEntity target) {
        return isGravityInverted(target) ? getWorldVelocity(target) : target.getDeltaMovement();
    }

    public static double projectileSpawnX(net.minecraft.world.entity.LivingEntity shooter) {
        return projectileSpawnVec(shooter).x;
    }

    public static double projectileSpawnY(net.minecraft.world.entity.LivingEntity shooter) {
        return projectileSpawnVec(shooter).y;
    }

    public static double projectileSpawnZ(net.minecraft.world.entity.LivingEntity shooter) {
        return projectileSpawnVec(shooter).z;
    }

    public static double rangedBodyTargetX(net.minecraft.world.entity.LivingEntity target) {
        if (!isGravityInverted(target)) {
            return target.getX();
        }
        return target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D,
                target.getBbHeight() * 0.3333333333333333D,
                0.0D,
                getGravityDirection(target)
        )).x;
    }

    public static double rangedBodyTargetY(net.minecraft.world.entity.LivingEntity target, double heightScale) {
        if (!isGravityInverted(target)) {
            return target.getY(heightScale);
        }
        return target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D,
                target.getBbHeight() * 0.3333333333333333D,
                0.0D,
                getGravityDirection(target)
        )).y;
    }

    public static double rangedBodyTargetZ(net.minecraft.world.entity.LivingEntity target) {
        if (!isGravityInverted(target)) {
            return target.getZ();
        }
        return target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D,
                target.getBbHeight() * 0.3333333333333333D,
                0.0D,
                getGravityDirection(target)
        )).z;
    }

    public static double rangedEyeTargetX(net.minecraft.world.entity.LivingEntity target) {
        if (!isGravityInverted(target)) {
            return target.getX();
        }
        return target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D,
                target.getEyeHeight() - 1.100000023841858D,
                0.0D,
                getGravityDirection(target)
        )).x;
    }

    public static double rangedEyeTargetY(net.minecraft.world.entity.LivingEntity target) {
        if (!isGravityInverted(target)) {
            return target.getEyeY();
        }
        return target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D,
                target.getEyeHeight() - 1.100000023841858D,
                0.0D,
                getGravityDirection(target)
        )).y + 1.100000023841858D;
    }

    public static double rangedEyeTargetZ(net.minecraft.world.entity.LivingEntity target) {
        if (!isGravityInverted(target)) {
            return target.getZ();
        }
        return target.position().add(AntarchyGravityRotationUtil.vecPlayerToWorld(
                0.0D,
                target.getEyeHeight() - 1.100000023841858D,
                0.0D,
                getGravityDirection(target)
        )).z;
    }

    public static double rangedSqrt(double value, net.minecraft.world.entity.LivingEntity target) {
        return isGravityInverted(target) ? Math.sqrt(Math.sqrt(value)) : Math.sqrt(value);
    }

    private static AntarchyGravityAccess access(Entity entity) {
        if (entity instanceof AntarchyGravityAccess access) {
            return access;
        }

        throw new IllegalStateException("Entity " + entity + " is missing Antarchy gravity state");
    }

    private static Vec3 projectileSpawnVec(net.minecraft.world.entity.LivingEntity shooter) {
        AntarchyGravityDirection direction = getGravityDirection(shooter);
        if (!direction.isInverted()) {
            return new Vec3(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ());
        }
        return shooter.getEyePosition().subtract(AntarchyGravityRotationUtil.vecPlayerToWorld(0.0D, 0.1D, 0.0D, direction));
    }
}
