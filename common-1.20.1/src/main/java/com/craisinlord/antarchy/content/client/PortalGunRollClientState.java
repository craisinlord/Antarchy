package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PortalGunRollClientState {
    private static final int TELEPORT_COOLDOWN_TICKS = 40;
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
        for (Entity renderEntity : minecraft.level.entitiesForRendering()) {
            if (renderEntity instanceof PortalGunPortalEntity portal && portal.isAlive() && portal.intersectsEntityBounds(entity)) {
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
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (!(entity instanceof PortalGunPortalEntity portal) || !portal.isAlive()) {
                continue;
            }
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
            Vec3 crossingProbe = portal.resolveCrossingProbe(cameraEntity);
            if (crossingProbe == null) {
                continue;
            }
            applyPredictedTeleport(cameraEntity, portal, linkedPortal, crossingProbe, gameTime);
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

    private static void applyPredictedTeleport(Entity entity, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, Vec3 currentProbe, long gameTime) {
        Vec3 transformedLook = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, entity.getLookAngle()).normalize();
        Vec3 transformedUp = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, PortalGunTransformUtil.upVectorFromLookAndRoll(entity.getLookAngle(), rollDegrees)).normalize();
        Vec3 relativeProbe = currentProbe.subtract(sourcePortal.position());
        Vec3 transformedProbe = PortalGunTransformUtil.transformPosition(sourcePortal, destinationPortal, relativeProbe);
        Vec3 transformedEntityOffset = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, entity.position().subtract(currentProbe));
        Vec3 destinationNormal = destinationPortal.getNormalVec().normalize();
        double exitClearance = Math.max(0.35D, entity.getBbWidth() * 0.5D + 0.16D);
        Vec3 exitProbe = destinationPortal.position().add(transformedProbe).add(destinationNormal.scale(exitClearance - transformedProbe.dot(destinationNormal)));
        Vec3 exitPos = exitProbe.add(transformedEntityOffset);
        Vec3 transformedMotion = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, entity.getDeltaMovement());
        double outward = transformedMotion.dot(destinationNormal);
        if (outward < 0.42D) {
            transformedMotion = transformedMotion.add(destinationNormal.scale(0.42D - outward));
        }
        rollDegrees = PortalGunTransformUtil.rollFromOrientation(transformedLook, transformedUp);
        entity.teleportTo(exitPos.x, exitPos.y, exitPos.z);
        entity.setDeltaMovement(transformedMotion);
        entity.setYRot(PortalGunTransformUtil.yawFromLook(transformedLook));
        entity.setXRot(PortalGunTransformUtil.pitchFromLook(transformedLook));
        TELEPORT_COOLDOWNS.put(entity.getUUID(), gameTime + TELEPORT_COOLDOWN_TICKS);
    }
}
