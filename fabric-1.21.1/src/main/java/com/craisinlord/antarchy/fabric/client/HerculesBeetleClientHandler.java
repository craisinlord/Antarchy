package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.network.HerculesBeetleFlightTogglePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleJumpInputPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedAttackPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedChargePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class HerculesBeetleClientHandler {
    private static boolean wasPressingJump;
    private static boolean wasPressingAttack;
    private static boolean wasCharging;
    private static boolean wasPressingFlightToggle;

    private HerculesBeetleClientHandler() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            resetAll();
            return;
        }

        if (!(player.getVehicle() instanceof HerculesBeetleEntity)) {
            resetAll();
            return;
        }

        boolean pressingJump = mc.options.keyJump.isDown();
        if (pressingJump != wasPressingJump) {
            ClientPlayNetworking.send(new HerculesBeetleJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        boolean pressingAttack = mc.options.keyAttack.isDown();
        if (pressingAttack && !wasPressingAttack) {
            ClientPlayNetworking.send(new HerculesBeetleMountedAttackPayload());
        }
        wasPressingAttack = pressingAttack;

        boolean charging = AntarchyKeyBindings.isMountSpecialPressed();
        if (charging != wasCharging) {
            ClientPlayNetworking.send(new HerculesBeetleMountedChargePayload(charging));
        }
        wasCharging = charging;

        boolean pressingFlightToggle = AntarchyKeyBindings.isMountFlightTogglePressed();
        if (pressingFlightToggle && !wasPressingFlightToggle) {
            ClientPlayNetworking.send(new HerculesBeetleFlightTogglePayload());
        }
        wasPressingFlightToggle = pressingFlightToggle;
    }

    private static void resetAll() {
        if (wasPressingJump) {
            ClientPlayNetworking.send(new HerculesBeetleJumpInputPayload(false));
            wasPressingJump = false;
        }
        wasPressingAttack = false;
        wasCharging = false;
        wasPressingFlightToggle = false;
    }
}
