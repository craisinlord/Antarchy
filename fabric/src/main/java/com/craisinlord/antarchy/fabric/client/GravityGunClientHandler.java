package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.item.GravityGunItem;
import com.craisinlord.antarchy.content.network.GravityGunPrimaryPayload;
import com.craisinlord.antarchy.content.network.GravityGunScrollPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public final class GravityGunClientHandler {
    private static final double SCROLL_STEP_DISTANCE = 0.75D;
    private static boolean lastAttackDown;
    private static double scrollAccumulator;

    private GravityGunClientHandler() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tickMouse(client));
    }

    private static void tickMouse(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null || !AntarchySettings.gravityGunEnabled()) {
            lastAttackDown = false;
            return;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof GravityGunItem)) {
            lastAttackDown = false;
            return;
        }

        long window = mc.getWindow().getWindow();
        boolean attackDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (attackDown && !lastAttackDown) {
            ClientPlayNetworking.send(new GravityGunPrimaryPayload());
        }
        lastAttackDown = attackDown;
    }

    public static boolean onScroll(double verticalAmount) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || !Screen.hasControlDown() || !AntarchySettings.gravityGunEnabled()) {
            return false;
        }

        if (!(mc.player.getMainHandItem().getItem() instanceof GravityGunItem) || !GravityGunItem.hasHeldTarget(mc.player.getMainHandItem())) {
            return false;
        }

        double scrollAmount = Mth.clamp(verticalAmount, -1.0D, 1.0D);
        if (Math.abs(scrollAmount) < 1.0E-4D) {
            return false;
        }

        ClientPlayNetworking.send(new GravityGunScrollPayload(scrollAmount * SCROLL_STEP_DISTANCE));
        return true;
    }
}
