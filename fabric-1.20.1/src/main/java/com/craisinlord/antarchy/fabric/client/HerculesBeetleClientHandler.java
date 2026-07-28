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
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new HerculesBeetleJumpInputPayload(pressingJump), HerculesBeetleJumpInputPayload.STREAM_CODEC, HerculesBeetleJumpInputPayload.TYPE);
            wasPressingJump = pressingJump;
        }

        boolean pressingAttack = mc.options.keyAttack.isDown();
        if (pressingAttack && !wasPressingAttack) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new HerculesBeetleMountedAttackPayload(), HerculesBeetleMountedAttackPayload.STREAM_CODEC, HerculesBeetleMountedAttackPayload.TYPE);
        }
        wasPressingAttack = pressingAttack;

        boolean charging = AntarchyKeyBindings.isHerculesBeetleChargePressed();
        if (charging != wasCharging) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new HerculesBeetleMountedChargePayload(charging), HerculesBeetleMountedChargePayload.STREAM_CODEC, HerculesBeetleMountedChargePayload.TYPE);
        }
        wasCharging = charging;

        boolean pressingFlightToggle = AntarchyKeyBindings.isHerculesBeetleFlightTogglePressed();
        if (pressingFlightToggle && !wasPressingFlightToggle) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new HerculesBeetleFlightTogglePayload(), HerculesBeetleFlightTogglePayload.STREAM_CODEC, HerculesBeetleFlightTogglePayload.TYPE);
        }
        wasPressingFlightToggle = pressingFlightToggle;
    }

    private static void resetAll() {
        if (wasPressingJump) {
            com.craisinlord.antarchy.fabric.network.AntarchyFabricClientNetworking.sendToServer(new HerculesBeetleJumpInputPayload(false), HerculesBeetleJumpInputPayload.STREAM_CODEC, HerculesBeetleJumpInputPayload.TYPE);
            wasPressingJump = false;
        }
        wasPressingAttack = false;
        wasCharging = false;
        wasPressingFlightToggle = false;
    }
}
