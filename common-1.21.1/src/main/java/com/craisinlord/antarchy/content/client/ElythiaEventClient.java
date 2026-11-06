package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.ElythiaEventPayload;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class ElythiaEventClient {
    private static final float FADE_TICKS = 200.0F;

    private static ClientLevel trackedLevel;
    private static ElythiaEvent active = ElythiaEvent.NONE;
    private static long activeSeed;
    private static ElythiaEvent shown = ElythiaEvent.NONE;
    private static long shownSeed;
    private static float strength;
    private static float previousStrength;

    private ElythiaEventClient() {
    }

    public static void handle(ElythiaEventPayload payload) {
        active = ElythiaEvent.byOrdinal(payload.event());
        activeSeed = payload.seed();
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != trackedLevel) {
            trackedLevel = level;
            active = ElythiaEvent.NONE;
            shown = ElythiaEvent.NONE;
            strength = 0.0F;
            previousStrength = 0.0F;
        }
        if (level == null || minecraft.isPaused()) {
            return;
        }
        if (!level.dimension().equals(ThrowbackZones.ELYTHIA)) {
            active = ElythiaEvent.NONE;
        }

        previousStrength = strength;
        boolean showingActive = shown == active && shownSeed == activeSeed;
        if (showingActive && active != ElythiaEvent.NONE) {
            strength = Math.min(1.0F, strength + 1.0F / FADE_TICKS);
        } else {
            strength = Math.max(0.0F, strength - 1.0F / FADE_TICKS);
            if (strength <= 0.0F) {
                shown = active;
                shownSeed = activeSeed;
                previousStrength = 0.0F;
            }
        }
    }

    public static ElythiaEvent shown() {
        return shown;
    }

    public static long seed() {
        return shownSeed;
    }

    public static float strength(float partialTick) {
        float t = Mth.lerp(partialTick, previousStrength, strength);
        return t * t * (3.0F - 2.0F * t);
    }

    public static float strength(ElythiaEvent event, float partialTick) {
        return shown == event ? strength(partialTick) : 0.0F;
    }

    public static boolean isShown(ElythiaEvent event) {
        return shown == event && strength > 0.0F;
    }
}
