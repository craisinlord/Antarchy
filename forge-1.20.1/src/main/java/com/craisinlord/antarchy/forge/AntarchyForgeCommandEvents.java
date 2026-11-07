package com.craisinlord.antarchy.forge;

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
import com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID)
public final class AntarchyForgeCommandEvents {
    private AntarchyForgeCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CavarynCommand.register(event.getDispatcher());
        CaterpillarCommand.register(event.getDispatcher());
        DimensionalTearCommand.register(event.getDispatcher());
        ElythiaCommand.register(event.getDispatcher());
        ThoraxisCommand.register(event.getDispatcher());
        QueenLocateCommand.register(event.getDispatcher());
        RoyalCommand.register(event.getDispatcher());
        PortalGunCommand.register(event.getDispatcher());
        DimensionResetCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        PortalGunProjectileEntity.releaseAllProjectileTickets();
    }
}
