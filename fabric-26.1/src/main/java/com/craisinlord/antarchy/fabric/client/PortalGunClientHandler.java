package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class PortalGunClientHandler {
    private static boolean lastAttackDown;

    private PortalGunClientHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(PortalGunClientHandler::tickMouse);
    }

    private static void tickMouse(Minecraft mc) {
        if (mc.player == null) {
            com.craisinlord.antarchy.content.client.PortalGunResetClientState.clear();
        }
        boolean hasPortalGun = mc.player != null && (mc.player.getMainHandItem().getItem() instanceof PortalGunItem || mc.player.getOffhandItem().getItem() instanceof PortalGunItem);
        boolean gameplayInput = mc.screen == null;
        boolean held = hasPortalGun && gameplayInput;
        boolean zoomPressed = AntarchyKeyBindings.consumePortalGunZoomPressed();
        PortalGunZoomClientState.tick(mc, held, zoomPressed && gameplayInput);
        boolean resetActive = com.craisinlord.antarchy.content.client.PortalGunResetClientState.tick(
                mc,
                held,
                AntarchyKeyBindings.isPortalGunResetDown(),
                ClientPlayNetworking::send
        );
        if (mc.player == null || mc.level == null || mc.screen != null) {
            lastAttackDown = false;
            return;
        }

        boolean offhand = !(mc.player.getMainHandItem().getItem() instanceof PortalGunItem)
                && mc.player.getOffhandItem().getItem() instanceof PortalGunItem;
        if (!offhand && !(mc.player.getMainHandItem().getItem() instanceof PortalGunItem)) {
            lastAttackDown = false;
            return;
        }

        boolean attackDown = mc.options.keyAttack.isDown();
        if (attackDown && !lastAttackDown && !resetActive) {
            com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.reportFromClient();
            ClientPlayNetworking.send(new PortalGunPrimaryPayload(offhand));
        }
        lastAttackDown = attackDown;
    }
}
