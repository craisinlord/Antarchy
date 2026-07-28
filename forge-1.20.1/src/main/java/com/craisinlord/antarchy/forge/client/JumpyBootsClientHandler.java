package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.item.JumpyBootsHelper;
import com.craisinlord.antarchy.content.item.JumpyBootsItem;
import com.craisinlord.antarchy.content.network.JumpyBootsLaunchPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class JumpyBootsClientHandler {
    private static int chargeTicks = 0;
    private static boolean wasCharging = false;
    private static int primeWindowTicks = 0;
    private static int storedChargeTicks = 0;

    private JumpyBootsClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
                AntarchyForgeNetworkCore.sendToServer(new JumpyBootsLaunchPayload(storedChargeTicks, player.isSprinting()));
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
            AntarchyForgeNetworkCore.sendToServer(new JumpyBootsLaunchPayload(chargeTicks, player.isSprinting()));
            reset();
            return;
        }

        if (sneaking) {
            chargeTicks = Math.min(chargeTicks + 1, JumpyBootsHelper.CHARGE_TICKS_MAX);
            wasCharging = true;
        } else if (wasCharging && chargeTicks > 0) {
            storedChargeTicks = chargeTicks;
            if (jumpPressed) {
                AntarchyForgeNetworkCore.sendToServer(new JumpyBootsLaunchPayload(storedChargeTicks, player.isSprinting()));
                reset();
                return;
            }
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

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !JumpyBootsItem.isWearingJumpyBoots(mc.player) || !isCharging()) {
            return;
        }
        if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            event.setCanceled(true);
        }
    }

    public static boolean isCharging() {
        return chargeTicks > 0;
    }

    public static int getChargeTicks() {
        return chargeTicks;
    }
}
