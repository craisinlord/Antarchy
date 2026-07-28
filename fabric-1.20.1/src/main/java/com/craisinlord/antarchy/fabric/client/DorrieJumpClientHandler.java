package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class DorrieJumpClientHandler {
    private static boolean wasPressingJump = false;
    private static boolean wasPressingCharge = false;

    private DorrieJumpClientHandler() {}

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.screen != null) {
            resetAll();
            return;
        }

        if (!(player.getVehicle() instanceof DorrieEntity)) {
            resetAll();
            return;
        }

        // Space bar → upward movement.
        boolean pressingJump = mc.options.keyJump.isDown();
        if (pressingJump != wasPressingJump) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new DorrieJumpInputPayload(pressingJump), DorrieJumpInputPayload.STREAM_CODEC, DorrieJumpInputPayload.TYPE);
            wasPressingJump = pressingJump;
        }

        // Left ctrl → charge jump.
        boolean pressingCharge = AntarchyKeyBindings.isDorrieChargeJumpPressed();
        if (pressingCharge != wasPressingCharge) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new DorrieChargeJumpPayload(pressingCharge), DorrieChargeJumpPayload.STREAM_CODEC, DorrieChargeJumpPayload.TYPE);
            if (!pressingCharge && player.getVehicle() instanceof DorrieEntity dorrie) {
                dorrie.applyJumpImpulseClient();
            }
            wasPressingCharge = pressingCharge;
        }
    }

    private static void resetAll() {
        if (wasPressingJump) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new DorrieJumpInputPayload(false), DorrieJumpInputPayload.STREAM_CODEC, DorrieJumpInputPayload.TYPE);
            wasPressingJump = false;
        }
        if (wasPressingCharge) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new DorrieChargeJumpPayload(false), DorrieChargeJumpPayload.STREAM_CODEC, DorrieChargeJumpPayload.TYPE);
            wasPressingCharge = false;
        }
    }
}
