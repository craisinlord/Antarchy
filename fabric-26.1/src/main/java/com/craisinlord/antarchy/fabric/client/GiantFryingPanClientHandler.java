package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.network.OpenGiantFryingPanPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class GiantFryingPanClientHandler {
    private static boolean wasUse;

    private GiantFryingPanClientHandler() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            wasUse = false;
            return;
        }
        boolean use = mc.options.keyUse.isDown();
        if (use && !wasUse && player.isShiftKeyDown()
                && (player.getMainHandItem().getItem() instanceof GiantFryingPanItem
                || player.getOffhandItem().getItem() instanceof GiantFryingPanItem)) {
            ClientPlayNetworking.send(OpenGiantFryingPanPayload.INSTANCE);
        }
        wasUse = use;
    }
}
