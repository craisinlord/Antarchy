package com.craisinlord.antarchy.content.weather;

import com.craisinlord.antarchy.Antarchy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class ThoraxisEventMath {
    public static final ResourceKey<Level> THORAXIS = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "thoraxis"));
    public static final float WEIGHTLESS_GRAVITY = 0.35F;
    public static final float RAIN_GRAVITY = 0.55F;

    private static final float RAMP_TICKS = 100.0F;
    private static final int BLINK_PERIOD_TICKS = 260;
    private static final int BLINK_TICKS = 40;

    private static volatile float serverGravityScale = 1.0F;
    private static volatile float clientGravityScale = 1.0F;
    private static volatile float clientLocalPlayerGravityScale = 1.0F;

    private ThoraxisEventMath() {
    }

    public static float intensity(ThoraxisEvent.Phase phase, long start, long end, double time) {
        if (phase != ThoraxisEvent.Phase.ACTIVE) {
            return 0.0F;
        }
        float in = (float) ((time - start) / RAMP_TICKS);
        float out = (float) ((end - time) / RAMP_TICKS);
        return smooth(Math.min(in, out));
    }

    public static float omen(ThoraxisEvent.Phase phase, long start, long end, double time) {
        return switch (phase) {
            case WARNING -> smooth((float) ((time - start) / Math.max(1.0D, end - start)));
            case ACTIVE -> smooth((float) ((end - time) / RAMP_TICKS));
            case NONE -> 0.0F;
        };
    }

    public static float eyeOpenness(long seed, double time) {
        double cycle = Math.floorMod((long) Math.floor(time) + (seed & 0xFFFFL), BLINK_PERIOD_TICKS) + (time - Math.floor(time));
        if (cycle >= BLINK_TICKS) {
            return 1.0F;
        }
        float t = (float) (cycle / BLINK_TICKS);
        return 1.0F - Mth.sin(t * Mth.PI);
    }

    public static float smooth(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    public static boolean isThoraxis(Level level) {
        return level.dimension().equals(THORAXIS);
    }

    public static double scaleGravity(Entity entity, double gravity) {
        if (gravity == 0.0D) {
            return gravity;
        }
        Level level = entity.level();
        float scale;
        if (!level.isClientSide) {
            scale = serverGravityScale;
        } else {
            scale = clientGravityScale;
            if (entity instanceof Player player && player.isLocalPlayer()) {
                scale *= clientLocalPlayerGravityScale;
            }
        }
        if (scale == 1.0F || !isThoraxis(level)) {
            return gravity;
        }
        return gravity * scale;
    }

    public static void setServerGravityScale(float scale) {
        serverGravityScale = scale;
    }

    public static void setClientGravityScale(float scale, float localPlayerScale) {
        clientGravityScale = scale;
        clientLocalPlayerGravityScale = localPlayerScale;
    }
}
