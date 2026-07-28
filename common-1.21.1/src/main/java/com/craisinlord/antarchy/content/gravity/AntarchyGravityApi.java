package com.craisinlord.antarchy.content.gravity;

import net.minecraft.world.entity.Entity;

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

    private static AntarchyGravityAccess access(Entity entity) {
        if (entity instanceof AntarchyGravityAccess access) {
            return access;
        }

        throw new IllegalStateException("Entity " + entity + " is missing Antarchy gravity state");
    }
}
