package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.entity.royal.RoyalMountEntity;
import com.craisinlord.antarchy.content.network.RoyalMountActionPayload;
import com.craisinlord.antarchy.content.network.RoyalMountVerticalPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;

public final class RoyalMountClientHandler {
    private static boolean wasAttack;
    private static boolean wasSpecial;
    private static boolean wasFlightToggle;
    private static boolean wasAscend;
    private static boolean wasDescend;
    private static boolean wasUse;

    private RoyalMountClientHandler() {
    }

    public static void tick() {
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
                ClientPlayNetworking.send(new RoyalMountActionPayload(RoyalMountActionPayload.MOUNT));
            }
            wasUse = use;
            return;
        }
        wasUse = mc.options.keyUse.isDown();

        boolean attack = mc.options.keyAttack.isDown();
        if (attack && !wasAttack) {
            ClientPlayNetworking.send(new RoyalMountActionPayload(RoyalMountActionPayload.BITE));
        }
        wasAttack = attack;

        boolean special = AntarchyKeyBindings.isMountSpecialPressed();
        if (special && !wasSpecial) {
            ClientPlayNetworking.send(new RoyalMountActionPayload(RoyalMountActionPayload.SPIT));
        }
        wasSpecial = special;

        boolean flightToggle = AntarchyKeyBindings.isMountFlightTogglePressed();
        if (flightToggle && !wasFlightToggle) {
            ClientPlayNetworking.send(new RoyalMountActionPayload(RoyalMountActionPayload.FLIGHT_TOGGLE));
        }
        wasFlightToggle = flightToggle;

        boolean ascend = mc.options.keyJump.isDown();
        boolean descend = mc.options.keyShift.isDown();
        if (ascend != wasAscend || descend != wasDescend) {
            ClientPlayNetworking.send(new RoyalMountVerticalPayload(ascend, descend));
            wasAscend = ascend;
            wasDescend = descend;
        }
    }

    private static void reset() {
        if (wasAscend || wasDescend) {
            ClientPlayNetworking.send(new RoyalMountVerticalPayload(false, false));
        }
        wasAttack = false;
        wasSpecial = false;
        wasFlightToggle = false;
        wasAscend = false;
        wasDescend = false;
    }
}
