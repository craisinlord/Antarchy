package com.craisinlord.antarchy.forge.client;

import net.minecraftforge.fml.common.Mod;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.network.BrutalflyElytraFlapPayload;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import com.craisinlord.antarchy.forge.network.AntarchyForgeNetworkCore;

@Mod.EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class BrutalflyElytraClientHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("Antarchy/BrutalflyElytra");
    private static boolean lastJumpDown;
    private static int chargeTicks;

    static {
        BrutalflyElytraItem.FLAP_KEY_NAME = () -> AntarchyKeyBindings.BRUTALFLY_FLAP.getTranslatedKeyMessage();
    }

    private BrutalflyElytraClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        BrutalflyElytraClientState.tick();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            reset();
            return;
        }

        if (!BrutalflyElytraItem.isWearingBrutalflyElytra(minecraft.player) || !minecraft.player.isFallFlying()) {
            reset();
            return;
        }

        boolean sneakDown = AntarchyKeyBindings.BRUTALFLY_FLAP.isDown();
        boolean wasSneakDown = lastJumpDown;
        lastJumpDown = sneakDown;

        if (sneakDown) {
            chargeTicks = Math.min(chargeTicks + 1, BrutalflyElytraFlightHelper.FLAP_CHARGE_TICKS_MAX);
            return;
        }

        if (wasSneakDown && chargeTicks > 0) {
            AntarchyForgeNetworkCore.sendToServer(new BrutalflyElytraFlapPayload(chargeTicks));
        }
        chargeTicks = 0;
    }

    private static void reset() {
        lastJumpDown = false;
        chargeTicks = 0;
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !BrutalflyElytraItem.isWearingBrutalflyElytra(minecraft.player) || !minecraft.player.isFallFlying()) {
            return;
        }
        if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            event.setCanceled(true);
        }
    }

    public static boolean isCharging() {
        return chargeTicks > 0 && lastJumpDown;
    }

    public static int getChargeTicks() {
        return chargeTicks;
    }
}
