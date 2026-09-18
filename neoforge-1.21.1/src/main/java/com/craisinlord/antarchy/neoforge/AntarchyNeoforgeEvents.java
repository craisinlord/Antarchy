package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.command.CavarynCommand;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.DimensionalTearCommand;
import com.craisinlord.antarchy.content.command.GravityCommand;
import com.craisinlord.antarchy.content.command.QueenLocateCommand;
import com.craisinlord.antarchy.content.command.RoyalCommand;
import com.craisinlord.antarchy.content.time.TimeDilationCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class AntarchyNeoforgeEvents {
    private AntarchyNeoforgeEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CavarynCommand.register(event.getDispatcher());
        GravityCommand.register(event.getDispatcher());
        CaterpillarCommand.register(event.getDispatcher());
        DimensionalTearCommand.register(event.getDispatcher());
        QueenLocateCommand.register(event.getDispatcher());
        TimeDilationCommand.register(event.getDispatcher());
        RoyalCommand.register(event.getDispatcher());
    }
}
