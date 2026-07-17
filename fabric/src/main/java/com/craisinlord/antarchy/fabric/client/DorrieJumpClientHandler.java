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
            ClientPlayNetworking.send(new DorrieJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        // Left ctrl → charge jump.
        boolean pressingCharge = AntarchyKeyBindings.isDorrieChargeJumpPressed();
        if (pressingCharge != wasPressingCharge) {
            ClientPlayNetworking.send(new DorrieChargeJumpPayload(pressingCharge));
            if (!pressingCharge && player.getVehicle() instanceof DorrieEntity dorrie) {
                dorrie.applyJumpImpulseClient();
            }
            wasPressingCharge = pressingCharge;
        }
    }

    private static void resetAll() {
        if (wasPressingJump) {
            ClientPlayNetworking.send(new DorrieJumpInputPayload(false));
            wasPressingJump = false;
        }
        if (wasPressingCharge) {
            ClientPlayNetworking.send(new DorrieChargeJumpPayload(false));
            wasPressingCharge = false;
        }
    }
}
