package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public final class PortalGunRollClientState {
    private static final int TELEPORT_COOLDOWN_TICKS = 15;
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
            if (portal.resolveCrossingProbe(cameraEntity) == null) {
                continue;
            }
            applyPredictedTeleport(cameraEntity, portal, linkedPortal, gameTime);
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

    private static void applyPredictedTeleport(Entity entity, PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, long gameTime) {
        Quaternionf transform = PortalGunTransformUtil.createTransform(sourcePortal, destinationPortal);
        Vec3 transformedLook = PortalGunTransformUtil.transform(entity.getLookAngle(), transform).normalize();
        Vec3 transformedUp = PortalGunTransformUtil.transform(PortalGunTransformUtil.upVectorFromLookAndRoll(entity.getLookAngle(), rollDegrees), transform).normalize();
        rollDegrees = PortalGunTransformUtil.rollFromOrientation(transformedLook, transformedUp);
        TELEPORT_COOLDOWNS.put(entity.getUUID(), gameTime + TELEPORT_COOLDOWN_TICKS);
    }
}
