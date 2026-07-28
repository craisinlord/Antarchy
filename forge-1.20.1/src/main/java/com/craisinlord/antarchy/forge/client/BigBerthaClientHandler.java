package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class BigBerthaClientHandler {
    private static boolean lastUseDown;

    private BigBerthaClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
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
            AntarchyForgeNetworkCore.sendToServer(new BigBerthaModeCyclePayload());
        }
        lastUseDown = useDown;
    }
}
