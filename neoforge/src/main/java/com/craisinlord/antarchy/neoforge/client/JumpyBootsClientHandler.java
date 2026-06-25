package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.JumpyBootsHelper;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.content.network.JumpyBootsLaunchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class JumpyBootsClientHandler {
    private static int chargeTicks = 0;
    private static boolean wasCharging = false;
    private static int primeWindowTicks = 0;
    private static int storedChargeTicks = 0;

    private JumpyBootsClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.level == null || mc.screen != null) {
            reset();
            return;
        }

        if (!JumpyBootsItem.isWearingJumpyBoots(player)) {
            reset();
            return;
        }

        boolean onGround = player.onGround();
        boolean sneaking = player.input.shiftKeyDown;
        boolean jumpPressed = mc.options.keyJump.consumeClick();

        if (primeWindowTicks > 0) {
            primeWindowTicks--;
            if (jumpPressed) {
                PacketDistributor.sendToServer(new JumpyBootsLaunchPayload(storedChargeTicks, player.isSprinting()));
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

        if (sneaking) {
            chargeTicks = Math.min(chargeTicks + 1, JumpyBootsHelper.CHARGE_TICKS_MAX);
            wasCharging = true;
        } else if (wasCharging && chargeTicks > 0) {
            storedChargeTicks = chargeTicks;
            primeWindowTicks = JumpyBootsHelper.PRIME_WINDOW_TICKS;
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
