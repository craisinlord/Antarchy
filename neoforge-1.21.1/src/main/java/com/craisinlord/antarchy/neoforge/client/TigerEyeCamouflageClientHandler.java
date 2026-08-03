package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.client.renderer.TigerEyeCamouflageTextureResolver;
import com.craisinlord.antarchy.content.network.ToggleTigerEyeCamouflagePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class TigerEyeCamouflageClientHandler {
    private TigerEyeCamouflageClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            TigerEyeCamouflageClientState.clear();
            return;
        }

        while (AntarchyKeyBindings.consumeTigerEyeCamouflagePressed()) {
            PacketDistributor.sendToServer(new ToggleTigerEyeCamouflagePayload());
        }
    }

    public static void clearClientCaches() {
        TigerEyeCamouflageClientState.clear();
        TigerEyeCamouflageTextureResolver.clearCache();
    }
}
