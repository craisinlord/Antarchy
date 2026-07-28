package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class DiamondMinecartClientHandler {

    private DiamondMinecartClientHandler() {
    }

    
    private static byte lastFlags = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (!AntarchySettings.diamondMinecartEnabled()) return;
        if (mc.player == null || mc.level == null) return;
        if (!(mc.player.getVehicle() instanceof DiamondMinecartEntity)) return;
        if (mc.screen != null) {
            if (lastFlags != 0) {
                PacketDistributor.sendToServer(new DiamondMinecartInputPayload((byte) 0));
                lastFlags = 0;
            }
            return;
        }

        byte flags = 0;
        if (mc.options.keyUp.isDown())    flags |= DiamondMinecartInputPayload.FLAG_FORWARD;
        if (mc.options.keyDown.isDown())  flags |= DiamondMinecartInputPayload.FLAG_BACK;
        if (mc.options.keyLeft.isDown())  flags |= DiamondMinecartInputPayload.FLAG_LEFT;
        if (mc.options.keyRight.isDown()) flags |= DiamondMinecartInputPayload.FLAG_RIGHT;

        if (flags != lastFlags) {
            PacketDistributor.sendToServer(new DiamondMinecartInputPayload(flags));
            lastFlags = flags;
        }
    }
}
