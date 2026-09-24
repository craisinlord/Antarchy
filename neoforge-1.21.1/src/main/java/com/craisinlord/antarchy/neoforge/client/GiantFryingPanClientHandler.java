package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.network.OpenGiantFryingPanPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class GiantFryingPanClientHandler {
    private static boolean wasUse;

    private GiantFryingPanClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
            PacketDistributor.sendToServer(OpenGiantFryingPanPayload.INSTANCE);
        }
        wasUse = use;
    }
}
