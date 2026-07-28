package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.client.BrutalflyElytraClientState;
import com.craisinlord.antarchy.content.item.BrutalflyElytraFlightHelper;
import com.craisinlord.antarchy.content.item.BrutalflyElytraItem;
import com.craisinlord.antarchy.content.network.BrutalflyElytraFlapPayload;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
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
    public static void onClientTick(ClientTickEvent.Post event) {
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
            PacketDistributor.sendToServer(new BrutalflyElytraFlapPayload(chargeTicks));
        }
        chargeTicks = 0;
    }

    private static void reset() {
        lastJumpDown = false;
        chargeTicks = 0;
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !BrutalflyElytraItem.isWearingBrutalflyElytra(minecraft.player) || !minecraft.player.isFallFlying()) {
            return;
        }
        if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR) || event.getName().equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
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
