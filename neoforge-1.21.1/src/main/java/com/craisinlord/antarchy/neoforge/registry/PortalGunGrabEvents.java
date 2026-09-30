package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.portalgun.PortalGunGrabManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class PortalGunGrabEvents {
    private PortalGunGrabEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        PortalGunGrabManager.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PortalGunGrabManager.clear(event.getServer());
        com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.clear(event.getServer());
    }
}
