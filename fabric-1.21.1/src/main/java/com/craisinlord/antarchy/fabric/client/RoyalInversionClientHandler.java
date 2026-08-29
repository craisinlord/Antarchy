package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.network.ToggleRoyalInversionPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class RoyalInversionClientHandler {
    private RoyalInversionClientHandler() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        while (AntarchyKeyBindings.consumeRoyalInversionTogglePressed()) {
            ClientPlayNetworking.send(new ToggleRoyalInversionPayload());
        }
    }
}
