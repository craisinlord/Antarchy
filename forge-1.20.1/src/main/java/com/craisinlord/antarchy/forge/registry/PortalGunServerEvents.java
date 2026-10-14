package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Mod.EventBusSubscriber(modid = Antarchy.MODID)
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
