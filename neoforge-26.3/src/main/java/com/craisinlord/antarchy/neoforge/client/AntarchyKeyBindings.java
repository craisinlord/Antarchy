package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class AntarchyKeyBindings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(Antarchy.MODID, "antarchy"));

    public static final KeyMapping BRUTALFLY_FLAP = new KeyMapping(
            "key.antarchy.brutalfly_flap",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_LSHIFT,
            CATEGORY
    );

    public static final KeyMapping MOUNT_SPECIAL = new KeyMapping(
            "key.antarchy.mount_special",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_LCONTROL,
            CATEGORY
    );

    public static final KeyMapping MOUNT_FLIGHT_TOGGLE = new KeyMapping(
            "key.antarchy.mount_flight_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            CATEGORY
    );
    public static final KeyMapping MOUNT_BEAM = new KeyMapping(
            "key.antarchy.mount_beam",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_H,
            CATEGORY
    );

    public static final KeyMapping MOUNT_SIGNATURE = new KeyMapping(
            "key.antarchy.mount_signature",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_J,
            CATEGORY
    );
    public static final KeyMapping TIGERS_EYE_CAMOUFLAGE = new KeyMapping(
            "key.antarchy.tigers_eye_camouflage",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping ROYAL_INVERSION_TOGGLE = new KeyMapping(
            "key.antarchy.royal_inversion_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_V,
            CATEGORY
    );
    public static final KeyMapping ROYAL_BOUNDARY = new KeyMapping(
            "key.antarchy.royal_boundary",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_B,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_ZOOM = new KeyMapping(
            "key.antarchy.portal_gun_zoom",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_MIDDLE,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_RESET = new KeyMapping(
            "key.antarchy.portal_gun_reset",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            CATEGORY
    );

    private AntarchyKeyBindings() {}

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(BRUTALFLY_FLAP);
        event.register(MOUNT_SPECIAL);
        event.register(MOUNT_FLIGHT_TOGGLE);
        event.register(MOUNT_BEAM);
        event.register(MOUNT_SIGNATURE);
        event.register(TIGERS_EYE_CAMOUFLAGE);
        event.register(ROYAL_INVERSION_TOGGLE);
        event.register(ROYAL_BOUNDARY);
        event.register(PORTAL_GUN_ZOOM);
        event.register(PORTAL_GUN_RESET);
    }

    public static boolean isMountFlightTogglePressed() {
        return MOUNT_FLIGHT_TOGGLE.isDown();
    }

    public static boolean isMountBeamPressed() {
        return MOUNT_BEAM.isDown();
    }

    public static boolean isMountSignaturePressed() {
        return MOUNT_SIGNATURE.isDown();
    }

    public static boolean isMountSpecialPressed() {
        return MOUNT_SPECIAL.isDown();
    }

    public static boolean consumeTigerEyeCamouflagePressed() {
        return TIGERS_EYE_CAMOUFLAGE.consumeClick();
    }

    public static boolean consumeRoyalInversionTogglePressed() {
        return ROYAL_INVERSION_TOGGLE.consumeClick();
    }

    public static boolean consumeRoyalBoundaryPressed() {
        return ROYAL_BOUNDARY.consumeClick();
    }

    public static boolean consumePortalGunZoomPressed() {
        return PORTAL_GUN_ZOOM.consumeClick();
    }

    public static boolean consumePortalGunResetPressed() {
        return PORTAL_GUN_RESET.consumeClick();
    }

    public static boolean isPortalGunResetDown() {
        return PORTAL_GUN_RESET.isDown() || PORTAL_GUN_RESET.consumeClick();
    }
}
