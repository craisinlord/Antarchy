package com.craisinlord.antarchy.neoforge.registry;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.ant.AntArrivalScheduler;
import com.craisinlord.antarchy.content.entity.stratoshark.SharknadoManager;
import com.craisinlord.antarchy.content.migration.DimensionResetManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunMoonAimSync;
import com.craisinlord.antarchy.content.portalgun.PortalGunResetManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunSavedData;
import com.craisinlord.antarchy.content.transit.AntTransit;
import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGridFeature;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class PortalGunServerEvents {
    private PortalGunServerEvents() {
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PortalGunResetManager.clear(event.getServer());
        AntArrivalScheduler.clear();
        AntTransit.clear();
        SharknadoManager.clearAll();
        KingsTreeGridFeature.clearTileCache();
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
