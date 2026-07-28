package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class DorrieJumpClientHandler {
    private static boolean wasPressingJump = false;
    private static boolean wasPressingCharge = false;

    private DorrieJumpClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
            AntarchyForgeNetworkCore.sendToServer(new DorrieJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        boolean pressingCharge = AntarchyKeyBindings.DORRIE_CHARGE_JUMP.isDown();
        if (pressingCharge != wasPressingCharge) {
            AntarchyForgeNetworkCore.sendToServer(new DorrieChargeJumpPayload(pressingCharge));
            if (!pressingCharge && player.getVehicle() instanceof DorrieEntity dorrie) {
                dorrie.applyJumpImpulseClient();
            }
            wasPressingCharge = pressingCharge;
        }
    }

    private static void resetAll() {
        if (wasPressingJump) {
            AntarchyForgeNetworkCore.sendToServer(new DorrieJumpInputPayload(false));
            wasPressingJump = false;
        }
        if (wasPressingCharge) {
            AntarchyForgeNetworkCore.sendToServer(new DorrieChargeJumpPayload(false));
            wasPressingCharge = false;
        }
    }
}
