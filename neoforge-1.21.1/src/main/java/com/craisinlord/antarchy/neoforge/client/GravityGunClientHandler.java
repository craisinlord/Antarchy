package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class GravityGunClientHandler {
    private static final double SCROLL_STEP_DISTANCE = 0.75D;
    private static boolean lastAttackDown;

    private GravityGunClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || !AntarchySettings.gravityGunEnabled()) {
            lastAttackDown = false;
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof GravityGunItem)) {
            lastAttackDown = false;
            return;
        }

        boolean attackDown = mc.options.keyAttack.isDown();
        if (attackDown && !lastAttackDown) {
            PacketDistributor.sendToServer(new GravityGunPrimaryPayload());
        }
        lastAttackDown = attackDown;
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || !Screen.hasControlDown() || !AntarchySettings.gravityGunEnabled()) {
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof GravityGunItem) || !GravityGunItem.hasHeldTarget(mc.player.getMainHandItem())) {
            return;
        }

        double scrollAmount = Mth.clamp(event.getScrollDeltaY(), -1.0D, 1.0D);
        if (Math.abs(scrollAmount) < 1.0E-4D) {
            return;
        }

        PacketDistributor.sendToServer(new GravityGunScrollPayload(scrollAmount * SCROLL_STEP_DISTANCE));
        event.setCanceled(true);
    }
}
