package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.SizeRaySoundHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class SizeRaySoundHandlerNeoForge {
    private SizeRaySoundHandlerNeoForge() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        SizeRaySoundHandler.tick(Minecraft.getInstance());
    }
}
