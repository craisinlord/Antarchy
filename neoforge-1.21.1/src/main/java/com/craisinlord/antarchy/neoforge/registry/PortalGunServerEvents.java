package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class PortalGunServerEvents {
    private PortalGunServerEvents() {
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.clear(event.getServer());
        com.craisinlord.antarchy.content.entity.ant.AntArrivalScheduler.clear();
        com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            com.craisinlord.antarchy.content.portalgun.PortalGunSavedData.clearMissingGuns(player);
        }
    }
}
