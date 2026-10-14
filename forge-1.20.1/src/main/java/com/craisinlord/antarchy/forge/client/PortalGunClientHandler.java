package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class PortalGunClientHandler {
    private static boolean lastAttackDown;

    private PortalGunClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            com.craisinlord.antarchy.content.client.PortalGunResetClientState.clear();
        }
        com.craisinlord.antarchy.content.client.PortalGunIndicatorClientState.setRequestSender(com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore::sendToServer);
        com.craisinlord.antarchy.content.client.MoonSkyTracker.setSender(com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore::sendToServer);
        com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.setClientReporter(com.craisinlord.antarchy.content.client.MoonSkyTracker::report);
        boolean hasPortalGun = mc.player != null && (mc.player.getMainHandItem().getItem() instanceof PortalGunItem || mc.player.getOffhandItem().getItem() instanceof PortalGunItem);
        boolean gameplayInput = mc.screen == null;
        boolean held = hasPortalGun && gameplayInput;
        boolean zoomPressed = AntarchyKeyBindings.consumePortalGunZoomPressed();
        PortalGunZoomClientState.tick(mc, hasPortalGun, zoomPressed && gameplayInput);
        boolean resetActive = com.craisinlord.antarchy.content.client.PortalGunResetClientState.tick(
                mc,
                held,
                AntarchyKeyBindings.isPortalGunResetDown(),
                com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore::sendToServer
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
            AntarchyForgeNetworkCore.sendToServer(new PortalGunPrimaryPayload(offhand));
        }
        lastAttackDown = attackDown;
    }
}
