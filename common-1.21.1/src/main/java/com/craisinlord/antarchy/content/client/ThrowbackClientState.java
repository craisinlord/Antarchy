package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.throwback.ThrowbackZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

public final class ThrowbackClientState {
    private static final float BASELINE_RATE = 0.05F;
    private static final float ZONE_RATE = 0.06F;
    private static final int STALE_SYNC_TICKS = 20 * 15;
    private static final float HUNGER_FADE_RATE = 0.1F;

    private static float baseline;
    private static float previousBaseline;
    private static float zone;
    private static float previousZone;
    private static float targetZone;
    private static int ticksSinceSync;
    private static float hungerHidden;
    private static float previousHungerHidden;

    private ThrowbackClientState() {
    }

    public static void update(float strength) {
        targetZone = Mth.clamp(strength, 0.0F, 1.0F);
        ticksSinceSync = 0;
    }

    public static void tick() {
        previousBaseline = baseline;
        previousZone = zone;
        previousHungerHidden = hungerHidden;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        boolean inElythia = minecraft.level != null
                && player != null
                && player.isAlive()
                && minecraft.level.dimension().equals(ThrowbackZones.ELYTHIA);
        if (!inElythia) {
            baseline = 0.0F;
            zone = 0.0F;
            previousBaseline = 0.0F;
            previousZone = 0.0F;
            targetZone = 0.0F;
            hungerHidden = 0.0F;
            previousHungerHidden = 0.0F;
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        if (++ticksSinceSync >= STALE_SYNC_TICKS) {
            targetZone = 0.0F;
        }
        float target = targetZone;
        MobEffectInstance remembrance = player.getEffect(AntarchyObjects.REMEMBRANCE_EFFECT.get());
        float hungerTarget = remembrance != null ? 1.0F : 0.0F;
        hungerHidden = Mth.clamp(hungerHidden + Mth.clamp(hungerTarget - hungerHidden, -HUNGER_FADE_RATE, HUNGER_FADE_RATE), 0.0F, 1.0F);
        if (remembrance != null && !remembrance.isAmbient()) {
            target = 1.0F;
        }
        baseline += (1.0F - baseline) * BASELINE_RATE;
        zone += (target - zone) * ZONE_RATE;
        if (zone < 0.001F && target == 0.0F) {
            zone = 0.0F;
        }
    }

    public static float baseline(float partialTick) {
        return Mth.lerp(partialTick, previousBaseline, baseline);
    }

    public static float hungerHidden(float partialTick) {
        return Mth.lerp(partialTick, previousHungerHidden, hungerHidden);
    }

    public static float zone(float partialTick) {
        return Mth.lerp(partialTick, previousZone, zone);
    }
}
