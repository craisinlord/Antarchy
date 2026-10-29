package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.command.CavarynCommand;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.DimensionalTearCommand;
import com.craisinlord.antarchy.content.command.ElythiaCommand;
import com.craisinlord.antarchy.content.command.QueenLocateCommand;
import com.craisinlord.antarchy.content.command.RoyalCommand;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

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
        QueenLocateCommand.register(event.getDispatcher());
        RoyalCommand.register(event.getDispatcher());
        com.craisinlord.antarchy.content.command.PortalGunCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        com.craisinlord.antarchy.content.portalgun.PortalGunProjectileEntity.releaseAllProjectileTickets();
    }
}
