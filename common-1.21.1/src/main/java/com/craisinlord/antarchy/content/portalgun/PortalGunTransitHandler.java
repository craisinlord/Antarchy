package com.craisinlord.antarchy.content.portalgun;

import com.craisinlord.antarchy.content.network.PortalGunTransitPayload;
import net.minecraft.server.level.ServerPlayer;

public final class PortalGunTransitHandler {
    private PortalGunTransitHandler() {
    }

    public static void handle(ServerPlayer player, PortalGunTransitPayload payload) {
        if (player == null) {
            return;
        }
        if (!PortalGunPortalEntity.handleClientTransit(player, payload.portalId(), payload.start(), payload.end(), payload.velocity())) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        }
    }
}
