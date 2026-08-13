package com.craisinlord.antarchy.content.gravity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class AntarchyMountedGravityHelper {
    private static final AntarchyGravityTransition STACK_TRANSITION = new AntarchyGravityTransition(12);

    private AntarchyMountedGravityHelper() {
    }

    public static void syncConnectedStack(Entity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        syncRootStack(entity.getRootVehicle(), null);
    }

    public static void syncConnectedStackIgnoring(Entity entity, LivingEntity ignoredEffectSource) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        syncRootStack(entity.getRootVehicle(), ignoredEffectSource);
    }

    public static void syncSeparatedStacks(Entity previousVehicle, Entity detachedEntity) {
        if (previousVehicle != null && !previousVehicle.level().isClientSide()) {
            syncRootStack(previousVehicle.getRootVehicle(), null);
        }
        if (detachedEntity != null && !detachedEntity.level().isClientSide()) {
            syncRootStack(detachedEntity.getRootVehicle(), null);
        }
    }

    private static void syncRootStack(Entity root, LivingEntity ignoredEffectSource) {
        boolean shouldInvert = stackHasInvertedEffect(root, ignoredEffectSource);
        applyGravityState(root, shouldInvert);
        for (Entity passenger : root.getIndirectPassengers()) {
            applyGravityState(passenger, shouldInvert);
        }
    }

    private static boolean stackHasInvertedEffect(Entity root, LivingEntity ignoredEffectSource) {
        if (hasInvertedEffect(root, ignoredEffectSource)) {
            return true;
        }
        for (Entity passenger : root.getIndirectPassengers()) {
            if (hasInvertedEffect(passenger, ignoredEffectSource)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasInvertedEffect(Entity entity, LivingEntity ignoredEffectSource) {
        return entity instanceof LivingEntity living
                && living != ignoredEffectSource
                && living.hasEffect(AntarchyObjects.INVERTED_EFFECT.get());
    }

    private static void applyGravityState(Entity entity, boolean shouldInvert) {
        if (shouldInvert) {
            if (AntarchyGravityApi.getGravityDirection(entity) != AntarchyGravityDirection.UP) {
                AntarchyGravityApi.setForcedGravityDirection(entity, AntarchyGravityDirection.UP, STACK_TRANSITION);
            }
            return;
        }

        if (AntarchyGravityApi.isGravityForced(entity)) {
            AntarchyGravityApi.setGravityDirection(entity, AntarchyGravityDirection.DOWN, false, STACK_TRANSITION);
        }
    }
}
