package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
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

        boolean pressingJump = mc.options.keyJump.isDown();
        if (pressingJump != wasPressingJump) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.send(new DorrieJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        boolean pressingCharge = AntarchyKeyBindings.isMountSpecialPressed();
        if (pressingCharge != wasPressingCharge) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.send(new DorrieChargeJumpPayload(pressingCharge));
            if (!pressingCharge && player.getVehicle() instanceof DorrieEntity dorrie) {
                dorrie.applyJumpImpulseClient();
            }
            wasPressingCharge = pressingCharge;
        }
    }

    private static void resetAll() {
        if (wasPressingJump) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.send(new DorrieJumpInputPayload(false));
            wasPressingJump = false;
        }
        if (wasPressingCharge) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.send(new DorrieChargeJumpPayload(false));
            wasPressingCharge = false;
        }
    }
}
