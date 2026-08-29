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

    public static final KeyMapping DORRIE_CHARGE_JUMP = new KeyMapping(
            "key.antarchy.dorrie_charge_jump",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_CONTROL,
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

    private AntarchyKeyBindings() {}

    public static void register() {
        KeyBindingHelper.registerKeyBinding(BRUTALFLY_FLAP);
        KeyBindingHelper.registerKeyBinding(DORRIE_CHARGE_JUMP);
        KeyBindingHelper.registerKeyBinding(MOUNT_SPECIAL);
        KeyBindingHelper.registerKeyBinding(MOUNT_FLIGHT_TOGGLE);
        KeyBindingHelper.registerKeyBinding(TIGERS_EYE_CAMOUFLAGE);
        KeyBindingHelper.registerKeyBinding(ROYAL_INVERSION_TOGGLE);
    }

    public static boolean isBrutalflyFlapPressed() {
        return Minecraft.getInstance().screen == null && BRUTALFLY_FLAP.isDown();
    }

    public static boolean isDorrieChargeJumpPressed() {
        return Minecraft.getInstance().screen == null && DORRIE_CHARGE_JUMP.isDown();
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
}
