package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.network.BrutalflyElytraFlapPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BrutalflyElytraClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Antarchy/BrutalflyElytra");
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
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[BrutalflyElytra] Sending flap payload chargeTicks={} fallFlying={} deltaMovement={}", chargeTicks, minecraft.player.isFallFlying(), minecraft.player.getDeltaMovement());
            }
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
