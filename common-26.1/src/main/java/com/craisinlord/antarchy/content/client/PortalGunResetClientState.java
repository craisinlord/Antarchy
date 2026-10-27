package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.PortalGunResetPayload;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;

public final class PortalGunResetClientState {
    private static boolean resetDown;
    private static boolean attackDown;
    private static boolean useDown;
    private static boolean selectedSide;

    private PortalGunResetClientState() {
    }

    public static boolean tick(Minecraft minecraft, boolean enabled, boolean resetPressed, Consumer<PortalGunResetPayload> sender) {
        boolean canInput = enabled && minecraft.player != null && minecraft.level != null && minecraft.screen == null;
        boolean active = canInput && resetPressed;
        boolean attack = canInput && minecraft.options.keyAttack.isDown();
        boolean use = canInput && minecraft.options.keyUse.isDown();
        if (active && !resetDown) {
            sender.accept(new PortalGunResetPayload(com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.ARM));
            selectedSide = false;
        }
        if (active && attack && !attackDown) {
            sender.accept(new PortalGunResetPayload(com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.RESET_BLUE));
            selectedSide = true;
        }
        if (active && use && !useDown) {
            selectedSide = true;
        }
        if (!active && resetDown) {
            int action = !canInput || selectedSide
                    ? com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.RELEASE
                    : com.craisinlord.antarchy.content.portalgun.PortalGunResetManager.RESET_ALL;
            sender.accept(new PortalGunResetPayload(action));
            selectedSide = false;
        }
        resetDown = active;
        attackDown = attack;
        useDown = use;
        return active;
    }

    public static void clear() {
        resetDown = false;
        attackDown = false;
        useDown = false;
        selectedSide = false;
    }
}
