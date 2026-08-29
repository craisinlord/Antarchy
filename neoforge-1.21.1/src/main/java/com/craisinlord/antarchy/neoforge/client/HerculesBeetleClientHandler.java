package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.CameraShakeClientState;
import com.craisinlord.antarchy.content.client.HerculesBeetleImpactShakeClientState;
import com.craisinlord.antarchy.content.client.HordeClientState;
import com.craisinlord.antarchy.content.entity.HerculesBeetleEntity;
import com.craisinlord.antarchy.content.network.HerculesBeetleFlightTogglePayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleJumpInputPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedAttackPayload;
import com.craisinlord.antarchy.content.network.HerculesBeetleMountedChargePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class HerculesBeetleClientHandler {
    private static boolean wasPressingJump;
    private static boolean wasPressingAttack;
    private static boolean wasCharging;
    private static boolean wasPressingFlightToggle;

    private HerculesBeetleClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        CameraShakeClientState.tick();
        HerculesBeetleImpactShakeClientState.tick();
        HordeClientState.tick();

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
            PacketDistributor.sendToServer(new HerculesBeetleJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        boolean pressingAttack = mc.options.keyAttack.isDown();
        if (pressingAttack && !wasPressingAttack) {
            PacketDistributor.sendToServer(new HerculesBeetleMountedAttackPayload());
        }
        wasPressingAttack = pressingAttack;

        boolean charging = AntarchyKeyBindings.MOUNT_SPECIAL.isDown();
        if (charging != wasCharging) {
            PacketDistributor.sendToServer(new HerculesBeetleMountedChargePayload(charging));
        }
        wasCharging = charging;

        boolean pressingFlightToggle = AntarchyKeyBindings.MOUNT_FLIGHT_TOGGLE.isDown();
        if (pressingFlightToggle && !wasPressingFlightToggle) {
            PacketDistributor.sendToServer(new HerculesBeetleFlightTogglePayload());
        }
        wasPressingFlightToggle = pressingFlightToggle;
    }

    private static void resetAll() {
        if (wasPressingJump) {
            PacketDistributor.sendToServer(new HerculesBeetleJumpInputPayload(false));
            wasPressingJump = false;
        }
        wasPressingAttack = false;
        wasCharging = false;
        wasPressingFlightToggle = false;
    }
}
