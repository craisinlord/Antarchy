package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class PortalGunRollClientState {
    private static final double PORTAL_QUERY_RADIUS = 12.0D;
    private static final float ROLL_DECAY = 0.85F;
    private static final float ROLL_EPSILON = 0.05F;
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
        for (PortalGunPortalEntity portal : com.craisinlord.antarchy.content.portalgun.PortalGunPortalRegistry.near(minecraft.level, entity.getBoundingBox().inflate(PORTAL_QUERY_RADIUS))) {
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
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }
        previousRollDegrees = rollDegrees;
        rollDegrees *= ROLL_DECAY;
        if (Math.abs(rollDegrees) < ROLL_EPSILON) {
            rollDegrees = 0.0F;
        }
    }

    public static void onTransit(PortalGunPortalEntity sourcePortal, PortalGunPortalEntity destinationPortal, Vec3 look) {
        Vec3 transformedLook = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, look).normalize();
        Vec3 transformedUp = PortalGunTransformUtil.transformVector(sourcePortal, destinationPortal, PortalGunTransformUtil.upVectorFromLookAndRoll(look, rollDegrees)).normalize();
        rollDegrees = PortalGunTransformUtil.rollFromOrientation(transformedLook, transformedUp);
        previousRollDegrees = rollDegrees;
    }
}
