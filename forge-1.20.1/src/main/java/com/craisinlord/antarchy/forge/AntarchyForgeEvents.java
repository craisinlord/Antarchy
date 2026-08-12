package com.craisinlord.antarchy.forge;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.command.CavarynCommand;
import com.craisinlord.antarchy.content.command.CaterpillarCommand;
import com.craisinlord.antarchy.content.command.GravityCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID)
final class AntarchyForgeCommandEvents {
    private AntarchyForgeCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CavarynCommand.register(event.getDispatcher());
        GravityCommand.register(event.getDispatcher());
        CaterpillarCommand.register(event.getDispatcher());
    }
}
