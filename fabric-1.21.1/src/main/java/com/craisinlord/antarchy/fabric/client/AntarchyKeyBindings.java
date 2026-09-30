package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class AntarchyKeyBindings {
    public static final String CATEGORY = "key.categories.antarchy";

    public static final KeyMapping BRUTALFLY_FLAP = new KeyMapping(
            "key.antarchy.brutalfly_flap",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping MOUNT_SPECIAL = new KeyMapping(
            "key.antarchy.mount_special",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
            CATEGORY
    );

    public static final KeyMapping MOUNT_FLIGHT_TOGGLE = new KeyMapping(
            "key.antarchy.mount_flight_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );
    public static final KeyMapping TIGERS_EYE_CAMOUFLAGE = new KeyMapping(
            "key.antarchy.tigers_eye_camouflage",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping ROYAL_INVERSION_TOGGLE = new KeyMapping(
            "key.antarchy.royal_inversion_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    public static final KeyMapping ROYAL_BOUNDARY = new KeyMapping(
            "key.antarchy.royal_boundary",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_ZOOM = new KeyMapping(
            "key.antarchy.portal_gun_zoom",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_GRAB = new KeyMapping(
            "key.antarchy.portal_gun_grab",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_RESET = new KeyMapping(
            "key.antarchy.portal_gun_reset",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    private AntarchyKeyBindings() {}

    public static void register() {
        KeyBindingHelper.registerKeyBinding(BRUTALFLY_FLAP);
        KeyBindingHelper.registerKeyBinding(MOUNT_SPECIAL);
        KeyBindingHelper.registerKeyBinding(MOUNT_FLIGHT_TOGGLE);
        KeyBindingHelper.registerKeyBinding(TIGERS_EYE_CAMOUFLAGE);
        KeyBindingHelper.registerKeyBinding(ROYAL_INVERSION_TOGGLE);
        KeyBindingHelper.registerKeyBinding(ROYAL_BOUNDARY);
        KeyBindingHelper.registerKeyBinding(PORTAL_GUN_ZOOM);
        KeyBindingHelper.registerKeyBinding(PORTAL_GUN_GRAB);
        KeyBindingHelper.registerKeyBinding(PORTAL_GUN_RESET);
    }

    public static boolean isBrutalflyFlapPressed() {
        return Minecraft.getInstance().screen == null && BRUTALFLY_FLAP.isDown();
    }

    public static boolean isMountSpecialPressed() {
        return Minecraft.getInstance().screen == null && MOUNT_SPECIAL.isDown();
    }

    public static boolean isMountFlightTogglePressed() {
        return Minecraft.getInstance().screen == null && MOUNT_FLIGHT_TOGGLE.isDown();
    }

    public static boolean consumeTigerEyeCamouflagePressed() {
        return Minecraft.getInstance().screen == null && TIGERS_EYE_CAMOUFLAGE.consumeClick();
    }

    public static boolean consumeRoyalInversionTogglePressed() {
        return Minecraft.getInstance().screen == null && ROYAL_INVERSION_TOGGLE.consumeClick();
    }

    public static boolean consumeRoyalBoundaryPressed() {
        return Minecraft.getInstance().screen == null && ROYAL_BOUNDARY.consumeClick();
    }

    public static boolean consumePortalGunZoomPressed() {
        return Minecraft.getInstance().screen == null && PORTAL_GUN_ZOOM.consumeClick();
    }

    public static boolean consumePortalGunGrabPressed() {
        return Minecraft.getInstance().screen == null && PORTAL_GUN_GRAB.consumeClick();
    }

    public static boolean consumePortalGunResetPressed() {
        return Minecraft.getInstance().screen == null && PORTAL_GUN_RESET.consumeClick();
    }

    public static boolean isPortalGunResetDown() {
        return Minecraft.getInstance().screen == null && (PORTAL_GUN_RESET.isDown() || PORTAL_GUN_RESET.consumeClick());
    }
}
