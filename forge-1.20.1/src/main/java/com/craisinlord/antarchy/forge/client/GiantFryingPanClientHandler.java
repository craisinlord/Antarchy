package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.GiantFryingPanItem;
import com.craisinlord.antarchy.content.network.OpenGiantFryingPanPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class GiantFryingPanClientHandler {
    private static boolean wasUse;

    private GiantFryingPanClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            AntarchyForgeNetworkCore.sendToServer(OpenGiantFryingPanPayload.INSTANCE);
        }
        wasUse = use;
    }
}
