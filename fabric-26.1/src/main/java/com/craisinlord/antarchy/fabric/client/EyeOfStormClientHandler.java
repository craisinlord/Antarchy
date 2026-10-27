package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.network.EyeOfStormPrimaryPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class EyeOfStormClientHandler {
    private static boolean lastAttackDown;

    private EyeOfStormClientHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(EyeOfStormClientHandler::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null || !AntarchySettings.eyeOfTheStormEnabled()) {
            lastAttackDown = false;
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof EyeOfTheStormItem)) {
            lastAttackDown = false;
            return;
        }

        boolean attackDown = mc.options.keyAttack.isDown();
        if (attackDown && !lastAttackDown) {
            ClientPlayNetworking.send(new EyeOfStormPrimaryPayload());
        }
        lastAttackDown = attackDown;
    }
}
