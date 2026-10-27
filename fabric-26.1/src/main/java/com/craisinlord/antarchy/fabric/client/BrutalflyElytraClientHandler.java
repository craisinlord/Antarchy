package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.network.BrutalflyElytraFlapPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class BrutalflyElytraClientHandler {
    private static boolean lastJumpDown;
    private static int chargeTicks;

    static {
        BrutalflyElytraItem.FLAP_KEY_NAME = () -> AntarchyKeyBindings.BRUTALFLY_FLAP.getTranslatedKeyMessage();
    }

    private BrutalflyElytraClientHandler() {
    }

    public static void tick() {
        BrutalflyElytraClientState.tick();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            reset();
            return;
        }

        if (!BrutalflyElytraItem.isWearingBrutalflyElytra(minecraft.player)) {
            reset();
            return;
        }

        if (!minecraft.player.isFallFlying()) {
            reset();
            return;
        }

        boolean sneakDown = AntarchyKeyBindings.isBrutalflyFlapPressed();
        boolean wasSneakDown = lastJumpDown;
        lastJumpDown = sneakDown;

        if (sneakDown) {
            chargeTicks = Math.min(chargeTicks + 1, BrutalflyElytraFlightHelper.FLAP_CHARGE_TICKS_MAX);
            return;
        }

        if (wasSneakDown && chargeTicks > 0) {
            ClientPlayNetworking.send(new BrutalflyElytraFlapPayload(chargeTicks));
        }
        chargeTicks = 0;
    }

    private static void reset() {
        lastJumpDown = false;
        chargeTicks = 0;
    }

    public static boolean isCharging() {
        return chargeTicks > 0 && lastJumpDown;
    }

    public static int getChargeTicks() {
        return chargeTicks;
    }
}
