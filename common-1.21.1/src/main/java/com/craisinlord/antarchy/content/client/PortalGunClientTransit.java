package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.PortalGunTransitPayload;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.portalgun.PortalGunPortalEntity;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransformUtil;
import com.craisinlord.antarchy.content.portalgun.PortalGunTransitMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PortalGunClientTransit {
    private static long cooldownUntil;

    private PortalGunClientTransit() {
    }

    public static void afterLocalMove(LocalPlayer player, AABB startBox, Vec3 velocity) {
        if (player.level() == null || !player.isAlive() || player.isPassenger() || player.isSpectator()) {
            return;
        }
        long gameTime = player.level().getGameTime();
        if (gameTime < cooldownUntil && cooldownUntil - gameTime <= PortalGunPortalEntity.TELEPORT_COOLDOWN_TICKS) {
            return;
        }
        AABB currentBox = player.getBoundingBox();
        if (velocity.lengthSqr() <= 1.0E-12D && startBox.getCenter().distanceToSqr(currentBox.getCenter()) <= 1.0E-10D) {
            return;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return;
        }
        Vec3 crossingVelocity = AntarchyGravityApi.isGravityInverted(player)
                ? PortalGunTransitMath.entityVelocityToWorld(player, velocity)
                : velocity;
        for (PortalGunPortalEntity portal : PortalGunPortalEntity.findPortalsNearBounds(player.level(), startBox.minmax(currentBox).expandTowards(crossingVelocity).inflate(1.0D))) {
            PortalGunPortalEntity linked = portal.getLinkedPortal();
            if (linked == null || !linked.isAlive()) {
                continue;
            }
            if (portal.resolveCrossing(player, startBox, currentBox, crossingVelocity) == null) {
                continue;
            }
            Vec3 look = player.getLookAngle();
            connection.send(new ServerboundCustomPayloadPacket(new PortalGunTransitPayload(
                    portal.getId(), PortalGunTransitMath.entityPosition(startBox, player), player.position(), velocity)));
            PortalGunPortalEntity.PortalTransit transit = portal.computeTransit(player, linked, currentBox, velocity, look);
            Vec3 rollLook = AntarchyGravityApi.isGravityInverted(player)
                    ? PortalGunTransitMath.entityVelocityToWorld(player, look)
                    : look;
            PortalGunRollClientState.onTransit(portal, linked, rollLook);
            apply(player, transit);
            cooldownUntil = gameTime + PortalGunPortalEntity.TELEPORT_COOLDOWN_TICKS;
            return;
        }
    }

    private static void apply(LocalPlayer player, PortalGunPortalEntity.PortalTransit transit) {
        Vec3 position = transit.position();
        float yaw = PortalGunTransformUtil.yawFromLook(transit.look());
        float pitch = PortalGunTransformUtil.pitchFromLook(transit.look());
        player.setPos(position.x, position.y, position.z);
        player.xo = position.x;
        player.yo = position.y;
        player.zo = position.z;
        player.xOld = position.x;
        player.yOld = position.y;
        player.zOld = position.z;
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        player.setYHeadRot(yaw);
        player.yHeadRotO = yaw;
        player.setYBodyRot(yaw);
        player.yBodyRotO = yaw;
        player.setDeltaMovement(transit.velocity());
        Vec3 fallVelocity = PortalGunTransitMath.entityVelocityToWorld(player, transit.velocity());
        player.fallDistance = PortalGunTransitMath.exitFallDistance(player.fallDistance, fallVelocity, player);
        player.setOnGround(false);
        player.horizontalCollision = false;
        player.verticalCollision = false;
        player.verticalCollisionBelow = false;
        player.setPortalCooldown(PortalGunPortalEntity.TELEPORT_COOLDOWN_TICKS);
    }
}
