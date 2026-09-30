package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.PortalGunGrabPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class PortalGunClientHandler {
    private static boolean lastAttackDown;

    private PortalGunClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            com.craisinlord.antarchy.content.client.PortalGunGrabClientState.clear();
            com.craisinlord.antarchy.content.client.PortalGunResetClientState.clear();
        }
        com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.setRequestSender(net.neoforged.neoforge.network.PacketDistributor::sendToServer);
        boolean hasPortalGun = mc.player != null && (mc.player.getMainHandItem().getItem() instanceof PortalGunItem || mc.player.getOffhandItem().getItem() instanceof PortalGunItem);
        boolean gameplayInput = mc.screen == null;
        boolean held = hasPortalGun && gameplayInput;
        boolean zoomPressed = AntarchyKeyBindings.consumePortalGunZoomPressed();
        PortalGunZoomClientState.tick(mc, hasPortalGun, zoomPressed && gameplayInput);
        boolean resetActive = com.craisinlord.antarchy.content.client.PortalGunResetClientState.tick(
                mc,
                held,
                AntarchyKeyBindings.isPortalGunResetDown(),
                net.neoforged.neoforge.network.PacketDistributor::sendToServer
        );
        if (held && AntarchyKeyBindings.consumePortalGunGrabPressed()) {
            PacketDistributor.sendToServer(new PortalGunGrabPayload());
        }
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
            PacketDistributor.sendToServer(new PortalGunPrimaryPayload(offhand));
        }
        lastAttackDown = attackDown;
    }
}
