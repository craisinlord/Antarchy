package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.CavarynCommand;
import com.craisinlord.antarchy.content.command.DimensionResetCommand;
import com.craisinlord.antarchy.content.command.DimensionalTearCommand;
import com.craisinlord.antarchy.content.command.ElythiaCommand;
import com.craisinlord.antarchy.content.command.PortalGunCommand;
import com.craisinlord.antarchy.content.command.QueenLocateCommand;
import com.craisinlord.antarchy.content.command.RoyalCommand;
import com.craisinlord.antarchy.content.command.ThoraxisCommand;
import com.craisinlord.antarchy.content.entity.stratoshark.SharknadoManager;
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import com.craisinlord.antarchy.content.transit.AntTransit;
import com.craisinlord.antarchy.content.worldgen.elythia.KingsTreeGridFeature;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class AntarchyNeoforgeServerEvents {
    private AntarchyNeoforgeServerEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CavarynCommand.register(event.getDispatcher());
        ElythiaCommand.register(event.getDispatcher());
        DimensionResetCommand.register(event.getDispatcher());
        ThoraxisCommand.register(event.getDispatcher());
        CaterpillarCommand.register(event.getDispatcher());
        DimensionalTearCommand.register(event.getDispatcher());
        QueenLocateCommand.register(event.getDispatcher());
        RoyalCommand.register(event.getDispatcher());
        PortalGunCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        AntTransit.clear();
        SharknadoManager.clearAll();
        KingsTreeGridFeature.clearTileCache();
        PortalGunProjectileEntity.releaseAllProjectileTickets();
    }
}
