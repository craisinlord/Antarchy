package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.client.TigerEyeCamouflageClientState;
import com.craisinlord.antarchy.content.client.renderer.TigerEyeCamouflageTextureResolver;
import com.craisinlord.antarchy.content.network.ToggleTigerEyeCamouflagePayload;
import com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking;
import net.minecraft.client.Minecraft;

public final class TigerEyeCamouflageClientHandler {
    private TigerEyeCamouflageClientHandler() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            TigerEyeCamouflageClientState.clear();
            return;
        }

        while (AntarchyKeyBindings.consumeTigerEyeCamouflagePressed()) {
            AntarchyFabricClientNetworking.sendToServer(
                    new ToggleTigerEyeCamouflagePayload(),
                    ToggleTigerEyeCamouflagePayload.STREAM_CODEC,
                    ToggleTigerEyeCamouflagePayload.TYPE
            );
        }
    }

    public static void clearClientCaches() {
        TigerEyeCamouflageClientState.clear();
        TigerEyeCamouflageTextureResolver.clearCache();
    }
}
