package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class GravityGunClientHandler {
    private static final double SCROLL_STEP_DISTANCE = 0.75D;

    private GravityGunClientHandler() {
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || !AntarchySettings.gravityGunEnabled()) {
            return;
        }

        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof GravityGunItem)) {
            return;
        }

        PacketDistributor.sendToServer(new GravityGunPrimaryPayload());
        event.setCanceled(true);
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
