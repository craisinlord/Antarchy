package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.item.PortalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.world.item.ItemStack;

public final class PortalGunZoomClientState {
    private static final int MAX_PROGRESS = 5;
    private static int progress;
    private static boolean zooming;
    private static Double originalSensitivity;

    private PortalGunZoomClientState() {
    }

    public static void tick(Minecraft minecraft, boolean held, boolean togglePressed) {
        if (minecraft.player == null || minecraft.level == null) {
            zooming = false;
            progress = 0;
            restoreSensitivity(minecraft.options);
            return;
        }

        ItemStack main = minecraft.player.getMainHandItem();
        ItemStack off = minecraft.player.getOffhandItem();
        boolean holdingPortalGun = held && (main.getItem() instanceof PortalGunItem || off.getItem() instanceof PortalGunItem);
        if (togglePressed && holdingPortalGun) {
            zooming = !zooming;
            if (zooming && originalSensitivity == null) {
                originalSensitivity = minecraft.options.sensitivity().get();
            }
        }
        if (!holdingPortalGun) {
            zooming = false;
        }

        progress = Math.max(0, Math.min(MAX_PROGRESS, progress + (zooming ? 1 : -1)));
        if (originalSensitivity != null) {
            if (progress == 0) {
                restoreSensitivity(minecraft.options);
            }
        }
    }

    public static double applyFov(double fov, float partialTick, Options options) {
        if (progress == 0 && !zooming) {
            restoreSensitivity(options);
            return fov;
        }
        double tickProgress = Math.max(0.0D, Math.min(1.0D, partialTick));
        double easedProgress = progress == MAX_PROGRESS
                ? MAX_PROGRESS
                : zooming
                        ? progress - 1.0D + tickProgress
                        : progress + 1.0D - tickProgress;
        easedProgress = Math.max(0.0D, Math.min(MAX_PROGRESS, easedProgress));
        double zoomFactor = zoomFactor(easedProgress);
        if (originalSensitivity != null) {
            options.sensitivity().set(originalSensitivity * zoomFactor);
        }
        return fov * zoomFactor;
    }

    private static double zoomFactor(double progress) {
        double normalizedProgress = Math.max(0.0D, Math.min(1.0D, progress / MAX_PROGRESS));
        return 0.1D + 0.9D * (1.0D - Math.sin(Math.toRadians(90.0D * normalizedProgress)));
    }

    private static double zoomFactor(int progress) {
        return zoomFactor((double) progress);
    }

    private static void restoreSensitivity(Options options) {
        if (originalSensitivity != null) {
            options.sensitivity().set(originalSensitivity);
            originalSensitivity = null;
        }
    }
}
