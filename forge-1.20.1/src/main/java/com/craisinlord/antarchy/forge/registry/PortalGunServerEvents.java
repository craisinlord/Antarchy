package com.craisinlord.antarchy.forge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ant.AntArrivalScheduler;
import com.craisinlord.antarchy.content.migration.DimensionResetManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync;
import com.craisinlord.antarchy.content.portalgun.PortalGunResetManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.craisinlord.antarchy.content.transit.AntTransit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID)
public final class PortalGunServerEvents {
    private PortalGunServerEvents() {
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PortalGunResetManager.clear(event.getServer());
        AntArrivalScheduler.clear();
        AntTransit.clear();
        PortalGunMoonAimSync.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PortalGunSavedData.clearMissingGuns(player);
            DimensionResetManager.onPlayerJoin(player);
        }
    }
}
