package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.item.SpringyBootsHelper;
import com.craisinlord.antarchy.content.item.SpringyBootsItem;
import com.craisinlord.antarchy.content.network.SpringyBootsLaunchPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class SpringyBootsClientHandler {
    private static int chargeTicks = 0;
    private static boolean wasCharging = false;
    private static int primeWindowTicks = 0;
    private static int storedChargeTicks = 0;

    private SpringyBootsClientHandler() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.screen != null) {
            reset();
            return;
        }

        if (!SpringyBootsItem.isWearingSpringyBoots(player)) {
            reset();
            return;
        }

        boolean onGround = player.onGround();
        boolean sneaking = player.input.shiftKeyDown;
        boolean jumpPressed = mc.options.keyJump.consumeClick();

        if (primeWindowTicks > 0) {
            primeWindowTicks--;
            if (jumpPressed) {
                ClientPlayNetworking.send(new SpringyBootsLaunchPayload(storedChargeTicks, player.isSprinting()));
                reset();
                return;
            }
            if (primeWindowTicks <= 0) {
                reset();
            }
            return;
        }

        if (!onGround) {
            chargeTicks = 0;
            wasCharging = false;
            return;
        }

        if (jumpPressed && wasCharging && chargeTicks > 0) {
            ClientPlayNetworking.send(new SpringyBootsLaunchPayload(chargeTicks, player.isSprinting()));
            reset();
            return;
        }

        if (sneaking) {
            chargeTicks = Math.min(chargeTicks + 1, SpringyBootsHelper.CHARGE_TICKS_MAX);
            wasCharging = true;
        } else if (wasCharging && chargeTicks > 0) {
            storedChargeTicks = chargeTicks;
            if (jumpPressed) {
                ClientPlayNetworking.send(new SpringyBootsLaunchPayload(storedChargeTicks, player.isSprinting()));
                reset();
                return;
            }
            primeWindowTicks = SpringyBootsHelper.PRIME_WINDOW_TICKS;
            chargeTicks = 0;
            wasCharging = false;
        } else {
            chargeTicks = 0;
            wasCharging = false;
        }
    }

    private static void reset() {
        chargeTicks = 0;
        wasCharging = false;
        primeWindowTicks = 0;
        storedChargeTicks = 0;
    }

    public static boolean isCharging() {
        return chargeTicks > 0;
    }

    public static int getChargeTicks() {
        return chargeTicks;
    }
}
