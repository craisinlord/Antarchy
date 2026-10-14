package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.network.ToggleRoyalInversionPayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class RoyalInversionClientHandler {
    private RoyalInversionClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        while (AntarchyKeyBindings.consumeRoyalInversionTogglePressed()) {
            AntarchyForgeNetworkCore.sendToServer(new ToggleRoyalInversionPayload());
        }
    }
}
