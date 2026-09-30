package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PortalGunRollClientState {
    private static final double PORTAL_QUERY_RADIUS = 12.0D;
    private static final int TELEPORT_COOLDOWN_TICKS = 3;
    private static final float ROLL_DECAY = 0.85F;
    private static final float ROLL_EPSILON = 0.05F;
    private static final Map<UUID, Long> TELEPORT_COOLDOWNS = new HashMap<>();
    private static float previousRollDegrees;
    private static float rollDegrees;

    private PortalGunRollClientState() {
    }

    public static float getRollDegrees() {
        return rollDegrees;
    }

    public static float getRollDegrees(float partialTick) {
        return previousRollDegrees + (rollDegrees - previousRollDegrees) * partialTick;
    }

    public static boolean isEntityInsidePortal(Minecraft minecraft, Entity entity) {
        if (minecraft.level == null || entity == null) {
            return false;
        }
        for (PortalGunPortalEntity portal : minecraft.level.getEntitiesOfClass(
                PortalGunPortalEntity.class,
                entity.getBoundingBox().inflate(PORTAL_QUERY_RADIUS),
                Entity::isAlive)) {
            if (portal.intersectsEntityBounds(entity)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCameraInsidePortal(Minecraft minecraft) {
        Entity cameraEntity = minecraft.getCameraEntity();
        return cameraEntity != null && isEntityInsidePortal(minecraft, cameraEntity);
    }

    public static void clear() {
        previousRollDegrees = 0.0F;
        rollDegrees = 0.0F;
        TELEPORT_COOLDOWNS.clear();
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }
        previousRollDegrees = rollDegrees;
        rollDegrees *= ROLL_DECAY;
        if (Math.abs(rollDegrees) < ROLL_EPSILON) {
            rollDegrees = 0.0F;
        }
        long gameTime = minecraft.level.getGameTime();
        TELEPORT_COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() <= gameTime);
        for (PortalGunPortalEntity portal : minecraft.level.getEntitiesOfClass(
                PortalGunPortalEntity.class,
                cameraEntity.getBoundingBox().inflate(PORTAL_QUERY_RADIUS),
                Entity::isAlive)) {
            PortalGunPortalEntity linkedPortal = portal.getLinkedPortal();
            if (linkedPortal == null || !linkedPortal.isAlive()) {
                continue;
            }
            if (!portal.shouldRenderFront(cameraEntity.position())) {
                continue;
            }
            if (!canTeleport(cameraEntity, gameTime)) {
                continue;
            }
            AABB currentBox = cameraEntity.getBoundingBox();
            AABB previousBox = currentBox.move(cameraEntity.xo - cameraEntity.getX(), cameraEntity.yo - cameraEntity.getY(), cameraEntity.zo - cameraEntity.getZ());
            PortalGunPortalEntity.PortalCrossing crossing = portal.resolveCrossing(cameraEntity, previousBox, currentBox);
            if (crossing == null) {
                continue;
            }
            applyPredictedTeleport(cameraEntity, portal, linkedPortal, crossing, gameTime);
            break;
        }
    }

    private static boolean canTeleport(Entity entity, long gameTime) {
        if (!entity.isAlive() || entity instanceof PortalGunPortalEntity || entity.isPassenger() || entity.isVehicle()) {
            return false;
        }
        long cooldownUntil = TELEPORT_COOLDOWNS.getOrDefault(entity.getUUID(), 0L);
        return cooldownUntil <= gameTime;
    }

    private static void applyPredictedTeleport(Entity entity, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, PortalGunPortalEntity.PortalCrossing crossing, long gameTime) {
        Vec3 transformedLook = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, entity.getLookAngle()).normalize();
        Vec3 transformedUp = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, PortalGunTransformUtil.upVectorFromLookAndRoll(entity.getLookAngle(), rollDegrees)).normalize();
        Vec3 resolvedMovement = entity.position().subtract(entity.xo, entity.yo, entity.zo);
        PortalGunPortalEntity.PortalTransitTransform transit = sourcePortal.previewTransit(entity, destinationPortal, crossing, resolvedMovement);
        Vec3 transformedMotion = transit.movement();
        transformedMotion = clampPortalMotion(transformedMotion);
        rollDegrees = PortalGunTransformUtil.rollFromOrientation(transformedLook, transformedUp);
        Vec3 exitPos = transit.exitPosition();
        entity.teleportTo(exitPos.x, exitPos.y, exitPos.z);
        entity.setDeltaMovement(transformedMotion);
        float verticalMotion = (float) transformedMotion.y;
        entity.fallDistance = 0.1F * (verticalMotion / -0.1F * verticalMotion / -0.1F);
        entity.setYRot(PortalGunTransformUtil.yawFromLook(transformedLook));
        entity.setXRot(PortalGunTransformUtil.pitchFromLook(transformedLook));
        entity.setPortalCooldown(TELEPORT_COOLDOWN_TICKS);
        TELEPORT_COOLDOWNS.put(entity.getUUID(), gameTime + TELEPORT_COOLDOWN_TICKS);
    }

    private static Vec3 clampPortalMotion(Vec3 motion) {
        return new Vec3(clampPortalMotionComponent(motion.x), clampPortalMotionComponent(motion.y), clampPortalMotionComponent(motion.z));
    }

    private static double clampPortalMotionComponent(double motion) {
        return Math.abs(motion) > 0.99D ? motion / (Math.abs(motion) + 0.001D) : motion;
    }
}
