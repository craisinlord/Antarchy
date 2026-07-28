package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.entity.DiamondMinecartEntity;
import com.craisinlord.antarchy.content.network.DiamondMinecartInputPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class DiamondMinecartClientHandler {

    private DiamondMinecartClientHandler() {
    }

    
    private static byte lastFlags = 0;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();

        if (!AntarchySettings.diamondMinecartEnabled()) return;
        if (mc.player == null || mc.level == null) return;
        if (!(mc.player.getVehicle() instanceof DiamondMinecartEntity)) return;
        if (mc.screen != null) {
            if (lastFlags != 0) {
                ClientPlayNetworking.send(new DiamondMinecartInputPayload((byte) 0));
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
            ClientPlayNetworking.send(new DiamondMinecartInputPayload(flags));
            lastFlags = flags;
        }
    }
}
