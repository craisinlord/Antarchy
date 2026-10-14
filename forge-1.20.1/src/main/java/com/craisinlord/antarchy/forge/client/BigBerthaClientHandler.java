package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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

        boolean bigBertha = mc.player.getMainHandItem().getItem() instanceof BigBerthaItem;
        boolean royalGuardianSword = mc.player.getMainHandItem().getItem() instanceof RoyalGuardianSwordItem;
        if (!bigBertha && !royalGuardianSword) {
            lastUseDown = false;
            return;
        }

        boolean useDown = mc.options.keyUse.isDown();
        if (useDown
                && !lastUseDown
                && mc.player.isShiftKeyDown()
                ) {
            if (bigBertha) AntarchyForgeNetworkCore.sendToServer(new BigBerthaModeCyclePayload());
            else AntarchyForgeNetworkCore.sendToServer(new RoyalGuardianSwordModeCyclePayload());
        }
        lastUseDown = useDown;
    }
}
