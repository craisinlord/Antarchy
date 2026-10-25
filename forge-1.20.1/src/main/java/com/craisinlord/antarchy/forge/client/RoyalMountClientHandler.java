package com.craisinlord.antarchy.forge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.craisinlord.antarchy.content.network.RoyalMountActionPayload;
import com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
                AntarchyForgeNetworkCore.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.MOUNT));
            }
            wasUse = use;
            return;
        }
        wasUse = mc.options.keyUse.isDown();

        boolean attack = mc.options.keyAttack.isDown();
        if (attack && !wasAttack) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.BITE));
        }
        wasAttack = attack;

        boolean special = AntarchyKeyBindings.isMountSpecialPressed();
        if (special && !wasSpecial) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.SPIT));
        }
        wasSpecial = special;

        boolean flightToggle = AntarchyKeyBindings.isMountFlightTogglePressed();
        if (flightToggle && !wasFlightToggle) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.FLIGHT_TOGGLE));
        }
        wasFlightToggle = flightToggle;

        boolean beam = AntarchyKeyBindings.isMountBeamPressed();
        if (beam && !wasBeam) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.BEAM));
        }
        wasBeam = beam;

        boolean signature = AntarchyKeyBindings.isMountSignaturePressed();
        if (signature && !wasSignature) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountActionPayload(RoyalMountActionPayload.SIGNATURE));
        }
        wasSignature = signature;

        boolean ascend = mc.options.keyJump.isDown();
        boolean descend = mc.options.keyShift.isDown();
        if (ascend != wasAscend || descend != wasDescend) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountVerticalPayload(ascend, descend));
            wasAscend = ascend;
            wasDescend = descend;
        }
    }

    private static void reset() {
        if (wasAscend || wasDescend) {
            AntarchyForgeNetworkCore.sendToServer(new RoyalMountVerticalPayload(false, false));
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
