package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.BigBerthaItem;
import com.craisinlord.antarchy.content.item.RoyalGuardianSwordItem;
import com.craisinlord.antarchy.content.network.BigBerthaModeCyclePayload;
import com.craisinlord.antarchy.content.network.RoyalGuardianSwordModeCyclePayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class BigBerthaClientHandler {
    private static boolean lastUseDown;

    private BigBerthaClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
            if (bigBertha) PacketDistributor.sendToServer(new BigBerthaModeCyclePayload());
            else PacketDistributor.sendToServer(new RoyalGuardianSwordModeCyclePayload());
        }
        lastUseDown = useDown;
    }
}
