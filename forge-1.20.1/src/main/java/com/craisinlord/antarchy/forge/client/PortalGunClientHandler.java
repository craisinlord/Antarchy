package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.MoonSkyTracker;
import com.craisinlord.antarchy.content.client.PortalGunResetClientState;
import com.craisinlord.antarchy.content.client.PortalGunZoomClientState;
import com.craisinlord.antarchy.content.item.PortalGunItem;
import com.craisinlord.antarchy.content.network.PortalGunPrimaryPayload;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class PortalGunClientHandler {
    private static boolean lastAttackDown;

    private PortalGunClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            PortalGunResetClientState.clear();
        }
        PortalGunMoonAimSync.setClientReporter(MoonSkyTracker::report);
        boolean hasPortalGun = mc.player != null && (mc.player.getMainHandItem().getItem() instanceof PortalGunItem || mc.player.getOffhandItem().getItem() instanceof PortalGunItem);
        boolean gameplayInput = mc.screen == null;
        boolean held = hasPortalGun && gameplayInput;
        boolean zoomPressed = AntarchyKeyBindings.consumePortalGunZoomPressed();
        PortalGunZoomClientState.tick(mc, hasPortalGun, zoomPressed && gameplayInput);
        boolean resetActive = PortalGunResetClientState.tick(
                mc,
                held,
                AntarchyKeyBindings.isPortalGunResetDown(),
                AntarchyForgeNetworkCore::sendToServer
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
            PortalGunMoonAimSync.reportFromClient();
            AntarchyForgeNetworkCore.sendToServer(new PortalGunPrimaryPayload(offhand));
        }
        lastAttackDown = attackDown;
    }
}
