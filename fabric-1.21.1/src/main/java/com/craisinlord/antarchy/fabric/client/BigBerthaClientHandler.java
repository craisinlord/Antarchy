package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class BigBerthaClientHandler {
    private static boolean lastUseDown;

    private BigBerthaClientHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(BigBerthaClientHandler::tick);
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null) {
            lastUseDown = false;
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof BigBerthaItem)) {
            lastUseDown = false;
            return;
        }

        boolean useDown = mc.options.keyUse.isDown();
        if (useDown
                && !lastUseDown
                && mc.player.isShiftKeyDown()
                && mc.player.getCooldowns().isOnCooldown(mc.player.getMainHandItem().getItem())) {
            ClientPlayNetworking.send(new BigBerthaModeCyclePayload());
        }
        lastUseDown = useDown;
    }
}
