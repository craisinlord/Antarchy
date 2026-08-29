package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.craisinlord.antarchy.content.network.RoyalMountActionPayload;
import com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class RoyalMountClientHandler {
    private static boolean wasAttack;
    private static boolean wasSpecial;
    private static boolean wasFlightToggle;
    private static boolean wasAscend;
    private static boolean wasDescend;

    private RoyalMountClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null || !(player.getVehicle() instanceof RoyalMountEntity)) {
            reset();
            return;
        }

        boolean attack = mc.options.keyAttack.isDown();
        if (attack && !wasAttack) {
            PacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.BITE));
        }
        wasAttack = attack;

        boolean special = AntarchyKeyBindings.isMountSpecialPressed();
        if (special && !wasSpecial) {
            PacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.SPIT));
        }
        wasSpecial = special;

        boolean flightToggle = AntarchyKeyBindings.isMountFlightTogglePressed();
        if (flightToggle && !wasFlightToggle) {
            PacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.FLIGHT_TOGGLE));
        }
        wasFlightToggle = flightToggle;

        boolean ascend = mc.options.keyJump.isDown();
        boolean descend = mc.options.keyShift.isDown();
        if (ascend != wasAscend || descend != wasDescend) {
            PacketDistributor.sendToServer(new RoyalMountVerticalPayload(ascend, descend));
            wasAscend = ascend;
            wasDescend = descend;
        }
    }

    private static void reset() {
        if (wasAscend || wasDescend) {
            PacketDistributor.sendToServer(new RoyalMountVerticalPayload(false, false));
        }
        wasAttack = false;
        wasSpecial = false;
        wasFlightToggle = false;
        wasAscend = false;
        wasDescend = false;
    }
}
