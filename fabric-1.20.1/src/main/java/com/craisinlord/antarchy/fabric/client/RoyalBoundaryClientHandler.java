package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.network.ActivateRoyalBoundaryPayload;
import net.minecraft.client.Minecraft;

public final class RoyalBoundaryClientHandler {
    private RoyalBoundaryClientHandler() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;
        while (AntarchyKeyBindings.consumeRoyalBoundaryPressed()) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.send(new ActivateRoyalBoundaryPayload());
        }
    }
}
