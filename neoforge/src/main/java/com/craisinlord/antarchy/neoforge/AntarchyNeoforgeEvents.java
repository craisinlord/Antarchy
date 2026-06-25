package com.craisinlord.antarchy.neoforge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.GravityCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Antarchy.MODID)
public final class AntarchyNeoforgeEvents {
    private AntarchyNeoforgeEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GravityCommand.register(event.getDispatcher());
        CaterpillarCommand.register(event.getDispatcher());
    }
}
