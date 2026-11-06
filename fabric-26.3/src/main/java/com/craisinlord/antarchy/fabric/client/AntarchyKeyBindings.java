package com.craisinlord.antarchy.fabric.client;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public final class AntarchyKeyBindings {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Antarchy.MODID, Antarchy.MODID));

    public static final KeyMapping BRUTALFLY_FLAP = new KeyMapping(
            "key.antarchy.brutalfly_flap",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_G,
            CATEGORY
    );

    public static final KeyMapping MOUNT_SPECIAL = new KeyMapping(
            "key.antarchy.mount_special",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_LCONTROL,
            CATEGORY
    );

    public static final KeyMapping MOUNT_FLIGHT_TOGGLE = new KeyMapping(
            "key.antarchy.mount_flight_toggle",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            CATEGORY
    );
    public static final KeyMapping MOUNT_BEAM = new KeyMapping(
            "key.antarchy.mount_beam",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_H,
            CATEGORY
    );

    public static final KeyMapping MOUNT_SIGNATURE = new KeyMapping(
            "key.antarchy.mount_signature",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_J,
            CATEGORY
    );
    public static final KeyMapping TIGERS_EYE_CAMOUFLAGE = new KeyMapping(
            "key.antarchy.tigers_eye_camouflage",
            InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping ROYAL_INVERSION_TOGGLE = new KeyMapping(
            "key.antarchy.royal_inversion_toggle",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_V,
            CATEGORY
    );
    public static final KeyMapping ROYAL_BOUNDARY = new KeyMapping(
            "key.antarchy.royal_boundary",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_B,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_ZOOM = new KeyMapping(
            "key.antarchy.portal_gun_zoom",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_Z,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_RESET = new KeyMapping(
            "key.antarchy.portal_gun_reset",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            CATEGORY
    );

    private AntarchyKeyBindings() {}

    public static void register() {
        KeyMappingHelper.registerKeyMapping(BRUTALFLY_FLAP);
        KeyMappingHelper.registerKeyMapping(MOUNT_SPECIAL);
        KeyMappingHelper.registerKeyMapping(MOUNT_FLIGHT_TOGGLE);
        KeyMappingHelper.registerKeyMapping(MOUNT_BEAM);
        KeyMappingHelper.registerKeyMapping(MOUNT_SIGNATURE);
        KeyMappingHelper.registerKeyMapping(TIGERS_EYE_CAMOUFLAGE);
        KeyMappingHelper.registerKeyMapping(ROYAL_INVERSION_TOGGLE);
        KeyMappingHelper.registerKeyMapping(ROYAL_BOUNDARY);
        KeyMappingHelper.registerKeyMapping(PORTAL_GUN_ZOOM);
        KeyMappingHelper.registerKeyMapping(PORTAL_GUN_RESET);
    }

    public static boolean isBrutalflyFlapPressed() {
        return Minecraft.getInstance().gui.screen() == null && BRUTALFLY_FLAP.isDown();
    }

    public static boolean isMountSpecialPressed() {
        return Minecraft.getInstance().gui.screen() == null && MOUNT_SPECIAL.isDown();
    }

    public static boolean isMountFlightTogglePressed() {
        return Minecraft.getInstance().gui.screen() == null && MOUNT_FLIGHT_TOGGLE.isDown();
    }

    public static boolean isMountBeamPressed() {
        return Minecraft.getInstance().gui.screen() == null && MOUNT_BEAM.isDown();
    }

    public static boolean isMountSignaturePressed() {
        return Minecraft.getInstance().gui.screen() == null && MOUNT_SIGNATURE.isDown();
    }

    public static boolean consumeTigerEyeCamouflagePressed() {
        return Minecraft.getInstance().gui.screen() == null && TIGERS_EYE_CAMOUFLAGE.consumeClick();
    }

    public static boolean consumeRoyalInversionTogglePressed() {
        return Minecraft.getInstance().gui.screen() == null && ROYAL_INVERSION_TOGGLE.consumeClick();
    }

    public static boolean consumeRoyalBoundaryPressed() {
        return Minecraft.getInstance().gui.screen() == null && ROYAL_BOUNDARY.consumeClick();
    }

    public static boolean consumePortalGunZoomPressed() {
        return Minecraft.getInstance().gui.screen() == null && PORTAL_GUN_ZOOM.consumeClick();
    }

    public static boolean consumePortalGunResetPressed() {
        return Minecraft.getInstance().gui.screen() == null && PORTAL_GUN_RESET.consumeClick();
    }

    public static boolean isPortalGunResetDown() {
        return Minecraft.getInstance().gui.screen() == null && (PORTAL_GUN_RESET.isDown() || PORTAL_GUN_RESET.consumeClick());
    }
}
