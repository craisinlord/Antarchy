package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class HerculesBeetleClientHandler {
    private static boolean wasPressingJump;
    private static boolean wasPressingAttack;
    private static boolean wasCharging;
    private static boolean wasPressingFlightToggle;

    private HerculesBeetleClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            AntarchyForgeNetworkCore.sendToServer(new HerculesBeetleJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        boolean pressingAttack = mc.options.keyAttack.isDown();
        if (pressingAttack && !wasPressingAttack) {
            AntarchyForgeNetworkCore.sendToServer(new HerculesBeetleMountedAttackPayload());
        }
        wasPressingAttack = pressingAttack;

        boolean charging = AntarchyKeyBindings.HERCULES_BEETLE_CHARGE.isDown();
        if (charging != wasCharging) {
            AntarchyForgeNetworkCore.sendToServer(new HerculesBeetleMountedChargePayload(charging));
        }
        wasCharging = charging;

        boolean pressingFlightToggle = AntarchyKeyBindings.HERCULES_BEETLE_FLIGHT_TOGGLE.isDown();
        if (pressingFlightToggle && !wasPressingFlightToggle) {
            AntarchyForgeNetworkCore.sendToServer(new HerculesBeetleFlightTogglePayload());
        }
        wasPressingFlightToggle = pressingFlightToggle;
    }

    private static void resetAll() {
        if (wasPressingJump) {
            AntarchyForgeNetworkCore.sendToServer(new HerculesBeetleJumpInputPayload(false));
            wasPressingJump = false;
        }
        wasPressingAttack = false;
        wasCharging = false;
        wasPressingFlightToggle = false;
    }
}
