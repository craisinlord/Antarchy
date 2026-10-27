package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.AntDescentPayload;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AntDescentClientState {
    public static final float SINK_TICKS = 40.0F;
    public static final float DISC_LIFETIME_TICKS = 60.0F;
    private static final float MAX_SINK = 1.4F;
    private static final float CANCEL_TICKS = 20.0F;
    private static final float GLIDE_FALL_SPEED = 0.5F;
    private static final int MAX_GLIDE_TICKS = 1200;

    private static final Map<Integer, Disc> DISCS = new HashMap<>();
    private static Phase phase = Phase.NONE;
    private static boolean home;
    private static int ticks;
    private static float cancelFrom;
    private static boolean gliding;
    private static int glideTicks;

    private AntDescentClientState() {
    }

    public static void handle(AntDescentPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean self = minecraft.player != null && minecraft.player.getId() == payload.entityId();
        switch (payload.phase()) {
            case AntDescentPayload.PHASE_START -> {
                DISCS.put(payload.entityId(), new Disc(new Vec3(payload.x(), payload.y(), payload.z()), payload.home()));
                if (self) {
                    phase = Phase.SINKING;
                    home = payload.home();
                    ticks = 0;
                    gliding = false;
                }
            }
            case AntDescentPayload.PHASE_ARRIVED -> {
                DISCS.remove(payload.entityId());
                if (self) {
                    phase = Phase.WAKING;
                    home = payload.home();
                    ticks = 0;
                    gliding = payload.glide();
                    glideTicks = 0;
                }
            }
            case AntDescentPayload.PHASE_CANCEL -> {
                DISCS.remove(payload.entityId());
                if (self) {
                    cancelFrom = Mth.clamp(ticks / SINK_TICKS, 0.0F, 1.0F);
                    phase = Phase.CANCELING;
                    ticks = 0;
                }
            }
            default -> {
            }
        }
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.level == null) {
            reset();
            return;
        }
        if (minecraft.isPaused()) {
            return;
        }
        DISCS.values().removeIf(disc -> ++disc.age > DISC_LIFETIME_TICKS + 20.0F);
        if (phase != Phase.NONE) {
            ticks++;
            if ((phase == Phase.WAKING && ticks > 80) || (phase == Phase.CANCELING && ticks > CANCEL_TICKS)) {
                phase = Phase.NONE;
            }
        }
        if (!gliding) {
            return;
        }
        glideTicks++;
        if (glideTicks > MAX_GLIDE_TICKS || player.isInWater() || player.getAbilities().flying || player.isFallFlying()
                || (glideTicks > 5 && player.onGround())) {
            gliding = false;
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < -GLIDE_FALL_SPEED) {
            player.setDeltaMovement(motion.x, -GLIDE_FALL_SPEED, motion.z);
        }
    }

    public static boolean frozen() {
        return phase == Phase.SINKING;
    }

    public static boolean home() {
        return home;
    }

    public static float sink(float partialTick) {
        return switch (phase) {
            case SINKING -> MAX_SINK * ease(progress(partialTick, SINK_TICKS));
            case CANCELING -> MAX_SINK * ease(cancelFrom) * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float vignette(float partialTick) {
        if (home) {
            return 0.0F;
        }
        return switch (phase) {
            case SINKING -> progress(partialTick, SINK_TICKS);
            case WAKING -> 1.0F - progress(partialTick, 30.0F);
            case CANCELING -> cancelFrom * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float fill(float partialTick) {
        return switch (phase) {
            case SINKING -> home
                    ? (float) Math.pow(progress(partialTick, SINK_TICKS), 1.5D)
                    : Mth.clamp((ticks + partialTick - 28.0F) / 12.0F, 0.0F, 1.0F);
            case WAKING -> 1.0F - progress(partialTick, home ? 30.0F : 20.0F);
            case CANCELING -> Mth.clamp((cancelFrom - 0.7F) / 0.3F, 0.0F, 1.0F) * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float saturation(float partialTick) {
        return switch (phase) {
            case SINKING -> home ? 1.0F : 1.0F - 0.7F * progress(partialTick, SINK_TICKS);
            case WAKING -> home
                    ? Mth.lerp(progress(partialTick, 40.0F), 0.4F, 1.0F)
                    : Mth.lerp(Mth.clamp((ticks + partialTick - 20.0F) / 40.0F, 0.0F, 1.0F), 0.05F, 1.0F);
            case CANCELING -> Mth.lerp(progress(partialTick, CANCEL_TICKS), 0.3F, 1.0F);
            default -> 1.0F;
        };
    }

    public static Collection<Disc> discs() {
        return DISCS.values();
    }

    private static float progress(float partialTick, float duration) {
        return Mth.clamp((ticks + partialTick) / duration, 0.0F, 1.0F);
    }

    private static float ease(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private static void reset() {
        DISCS.clear();
        phase = Phase.NONE;
        gliding = false;
    }

    private enum Phase {
        NONE,
        SINKING,
        WAKING,
        CANCELING
    }

    public static final class Disc {
        private final Vec3 center;
        private final boolean home;
        private float age;

        private Disc(Vec3 center, boolean home) {
            this.center = center;
            this.home = home;
        }

        public Vec3 center() {
            return this.center;
        }

        public boolean home() {
            return this.home;
        }

        public float age() {
            return this.age;
        }
    }
}
