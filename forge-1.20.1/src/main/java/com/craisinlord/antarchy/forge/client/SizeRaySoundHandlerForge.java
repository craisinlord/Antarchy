package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.SizeRaySoundHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class SizeRaySoundHandlerForge {
    private SizeRaySoundHandlerForge() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        SizeRaySoundHandler.tick(Minecraft.getInstance());
    }
}
