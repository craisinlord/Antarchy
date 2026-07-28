package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.DorrieEntity;
import com.craisinlord.antarchy.content.network.DorrieChargeJumpPayload;
import com.craisinlord.antarchy.content.network.DorrieJumpInputPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class DorrieJumpClientHandler {
    private static boolean wasPressingJump = false;
    private static boolean wasPressingCharge = false;

    private DorrieJumpClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
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
            PacketDistributor.sendToServer(new DorrieJumpInputPayload(pressingJump));
            wasPressingJump = pressingJump;
        }

        boolean pressingCharge = AntarchyKeyBindings.DORRIE_CHARGE_JUMP.isDown();
        if (pressingCharge != wasPressingCharge) {
            PacketDistributor.sendToServer(new DorrieChargeJumpPayload(pressingCharge));
            if (!pressingCharge && player.getVehicle() instanceof DorrieEntity dorrie) {
                dorrie.applyJumpImpulseClient();
            }
            wasPressingCharge = pressingCharge;
        }
    }

    private static void resetAll() {
        if (wasPressingJump) {
            PacketDistributor.sendToServer(new DorrieJumpInputPayload(false));
            wasPressingJump = false;
        }
        if (wasPressingCharge) {
            PacketDistributor.sendToServer(new DorrieChargeJumpPayload(false));
            wasPressingCharge = false;
        }
    }
}
