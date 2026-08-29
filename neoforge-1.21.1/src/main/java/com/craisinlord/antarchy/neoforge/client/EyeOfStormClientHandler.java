package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.EyeOfTheStormItem;
import com.craisinlord.antarchy.content.network.EyeOfStormPrimaryPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class EyeOfStormClientHandler {
    private static boolean lastAttackDown;

    private EyeOfStormClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
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
            PacketDistributor.sendToServer(new EyeOfStormPrimaryPayload());
        }
        lastAttackDown = attackDown;
    }
}
