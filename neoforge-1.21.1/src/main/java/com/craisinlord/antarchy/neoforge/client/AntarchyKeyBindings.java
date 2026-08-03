package com.craisinlord.antarchy.neoforge.client;

import com.craisinlord.antarchy.Antarchy;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

@EventBusSubscriber(modid = Antarchy.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class AntarchyKeyBindings {
    public static final String CATEGORY = "key.categories.antarchy";

    public static final KeyMapping BRUTALFLY_FLAP = new KeyMapping(
            "key.antarchy.brutalfly_flap",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT,
            CATEGORY
    );

    public static final KeyMapping DORRIE_CHARGE_JUMP = new KeyMapping(
            "key.antarchy.dorrie_charge_jump",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL,
            CATEGORY
    );

    public static final KeyMapping HERCULES_BEETLE_CHARGE = new KeyMapping(
            "key.antarchy.hercules_beetle_charge",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LCONTROL,
            CATEGORY
    );

    public static final KeyMapping HERCULES_BEETLE_FLIGHT_TOGGLE = new KeyMapping(
            "key.antarchy.hercules_beetle_flight_toggle",
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

    private AntarchyKeyBindings() {}

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BRUTALFLY_FLAP);
        event.register(DORRIE_CHARGE_JUMP);
        event.register(HERCULES_BEETLE_CHARGE);
        event.register(HERCULES_BEETLE_FLIGHT_TOGGLE);
        event.register(TIGERS_EYE_CAMOUFLAGE);
    }

    public static boolean isHerculesBeetleFlightTogglePressed() {
        return HERCULES_BEETLE_FLIGHT_TOGGLE.isDown();
    }

    public static boolean consumeTigerEyeCamouflagePressed() {
        return TIGERS_EYE_CAMOUFLAGE.consumeClick();
    }
}
