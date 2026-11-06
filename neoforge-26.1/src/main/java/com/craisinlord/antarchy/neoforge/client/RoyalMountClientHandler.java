package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.craisinlord.antarchy.content.network.RoyalMountActionPayload;
import com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class RoyalMountClientHandler {
    private static boolean wasAttack;
    private static boolean wasSpecial;
    private static boolean wasFlightToggle;
    private static boolean wasBeam;
    private static boolean wasSignature;
    private static boolean wasAscend;
    private static boolean wasDescend;
    private static boolean wasUse;

    private RoyalMountClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.screen != null) {
            reset();
            wasUse = false;
            return;
        }
        if (!(player.getVehicle() instanceof RoyalMountEntity)) {
            reset();
            boolean use = mc.options.keyUse.isDown();
            if (use && !wasUse && !(mc.hitResult instanceof EntityHitResult)
                    && RoyalMountEntity.findAssistedMountTarget(player, 1.0D) != null) {
                ClientPacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.MOUNT));
            }
            wasUse = use;
            return;
        }
        wasUse = mc.options.keyUse.isDown();

        boolean attack = mc.options.keyAttack.isDown();
        if (attack && !wasAttack) {
            ClientPacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.BITE));
        }
        wasAttack = attack;

        boolean special = AntarchyKeyBindings.isMountSpecialPressed();
        if (special && !wasSpecial) {
            ClientPacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.SPIT));
        }
        wasSpecial = special;

        boolean flightToggle = AntarchyKeyBindings.isMountFlightTogglePressed();
        if (flightToggle && !wasFlightToggle) {
            ClientPacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.FLIGHT_TOGGLE));
        }
        wasFlightToggle = flightToggle;

        boolean beam = AntarchyKeyBindings.isMountBeamPressed();
        if (beam && !wasBeam) {
            ClientPacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.BEAM));
        }
        wasBeam = beam;

        boolean signature = AntarchyKeyBindings.isMountSignaturePressed();
        if (signature && !wasSignature) {
            ClientPacketDistributor.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.SIGNATURE));
        }
        wasSignature = signature;

        boolean ascend = mc.options.keyJump.isDown();
        boolean descend = mc.options.keyShift.isDown();
        if (ascend != wasAscend || descend != wasDescend) {
            ClientPacketDistributor.sendToServer(new RoyalMountVerticalPayload(ascend, descend));
            wasAscend = ascend;
            wasDescend = descend;
        }
    }

    private static void reset() {
        if (wasAscend || wasDescend) {
            ClientPacketDistributor.sendToServer(new RoyalMountVerticalPayload(false, false));
        }
        wasAttack = false;
        wasSpecial = false;
        wasFlightToggle = false;
        wasBeam = false;
        wasSignature = false;
        wasAscend = false;
        wasDescend = false;
    }
}
