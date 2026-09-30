package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = Antarchy.MODID, value = Dist.CLIENT)
public final class AntarchyKeyBindings {
    public static final String CATEGORY = "key.categories.antarchy";

    public static final KeyMapping BRUTALFLY_FLAP = new KeyMapping(
            "key.antarchy.brutalfly_flap",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT,
            CATEGORY
    );

    public static final KeyMapping MOUNT_SPECIAL = new KeyMapping(
            "key.antarchy.mount_special",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL,
            CATEGORY
    );

    public static final KeyMapping MOUNT_FLIGHT_TOGGLE = new KeyMapping(
            "key.antarchy.mount_flight_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            CATEGORY
    );
    public static final KeyMapping TIGERS_EYE_CAMOUFLAGE = new KeyMapping(
            "key.antarchy.tigers_eye_camouflage",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            CATEGORY
    );

    public static final KeyMapping ROYAL_INVERSION_TOGGLE = new KeyMapping(
            "key.antarchy.royal_inversion_toggle",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            CATEGORY
    );
    public static final KeyMapping ROYAL_BOUNDARY = new KeyMapping(
            "key.antarchy.royal_boundary",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
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
    public static final KeyMapping PORTAL_GUN_GRAB = new KeyMapping(
            "key.antarchy.portal_gun_grab",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            CATEGORY
    );
    public static final KeyMapping PORTAL_GUN_RESET = new KeyMapping(
            "key.antarchy.portal_gun_reset",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            CATEGORY
    );

    private AntarchyKeyBindings() {}

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BRUTALFLY_FLAP);
        event.register(MOUNT_SPECIAL);
        event.register(MOUNT_FLIGHT_TOGGLE);
        event.register(TIGERS_EYE_CAMOUFLAGE);
        event.register(ROYAL_INVERSION_TOGGLE);
        event.register(ROYAL_BOUNDARY);
        event.register(PORTAL_GUN_ZOOM);
        event.register(PORTAL_GUN_GRAB);
        event.register(PORTAL_GUN_RESET);
    }

    public static boolean isMountFlightTogglePressed() {
        return MOUNT_FLIGHT_TOGGLE.isDown();
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

    public static boolean consumePortalGunGrabPressed() {
        return PORTAL_GUN_GRAB.consumeClick();
    }

    public static boolean consumePortalGunResetPressed() {
        return PORTAL_GUN_RESET.consumeClick();
    }

    public static boolean isPortalGunResetDown() {
        return PORTAL_GUN_RESET.isDown() || PORTAL_GUN_RESET.consumeClick();
    }
}
