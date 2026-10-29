package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.network.AntTransitPayload;
import com.craisinlord.antarchy.content.network.TransitStyle;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class AntTransitClientState {
    public static final float SINK_TICKS = 40.0F;
    public static final float DISC_LIFETIME_TICKS = 60.0F;
    private static final float CANCEL_TICKS = 20.0F;
    private static final float WAKE_TICKS = 80.0F;
    private static final int MAX_SINKING_TICKS = 520;
    private static final int MAX_LOADING_TICKS = 600;
    private static final double POOF_MIN_SPEED = 0.3D;
    private static final double POOF_LEAD = 1.5D;
    private static final double POOF_MARGIN = 2.0D;
    private static final double POOF_SPEED = 0.05D;
    private static final double POOF_DRAG = 0.4D;
    private static final int POOF_SCAN = 12;
    private static final int MAX_GLIDE_TICKS = 1200;
    private static final float[] BLACK = {0.0F, 0.0F, 0.0F};
    private static final float[] WHITE = {1.0F, 0.97F, 0.92F};
    private static final float[] NIGHTMARE = {0.28F, 0.0F, 0.07F};
    private static final float[] EARTH = {0.08F, 0.05F, 0.03F};

    private static final Map<Integer, Disc> DISCS = new HashMap<>();
    private static Phase phase = Phase.NONE;
    private static TransitStyle style = TransitStyle.ELYTHIA_IN;
    private static int ticks;
    private static int loadingTicks;
    private static float cancelFrom;
    private static boolean gliding;
    private static int glideTicks;

    private AntTransitClientState() {
    }

    public static void handle(AntTransitPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean self = minecraft.player != null && minecraft.player.getId() == payload.entityId();
        switch (payload.phase()) {
            case AntTransitPayload.PHASE_START -> {
                DISCS.put(payload.entityId(), new Disc(new Vec3(payload.x(), payload.y(), payload.z()), payload.style()));
                if (self) {
                    phase = Phase.SINKING;
                    style = payload.style();
                    ticks = 0;
                    gliding = false;
                }
            }
            case AntTransitPayload.PHASE_ARRIVED -> {
                DISCS.remove(payload.entityId());
                if (self) {
                    phase = Phase.WAKING;
                    style = payload.style();
                    ticks = 0;
                    loadingTicks = 0;
                    gliding = payload.glide();
                    glideTicks = 0;
                }
            }
            case AntTransitPayload.PHASE_CANCEL -> {
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
        if (phase == Phase.SINKING && (player.isDeadOrDying() || ticks > MAX_SINKING_TICKS)) {
            cancelFrom = Mth.clamp(ticks / SINK_TICKS, 0.0F, 1.0F);
            phase = Phase.CANCELING;
            ticks = 0;
        }
        if (phase == Phase.WAKING && ++loadingTicks < MAX_LOADING_TICKS && !loaded(minecraft, player)) {
            return;
        }
        if (phase != Phase.NONE) {
            ticks++;
            if ((phase == Phase.WAKING && ticks > WAKE_TICKS) || (phase == Phase.CANCELING && ticks > CANCEL_TICKS)) {
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
        if (motion.y > -POOF_MIN_SPEED) {
            return;
        }
        double ground = groundDistance(player);
        if (ground > Math.abs(motion.y) * POOF_LEAD + POOF_MARGIN) {
            return;
        }
        player.setDeltaMovement(motion.x * POOF_DRAG, -POOF_SPEED, motion.z * POOF_DRAG);
        poof(minecraft, player, ground);
        gliding = false;
    }

    private static boolean loaded(Minecraft minecraft, LocalPlayer player) {
        if (minecraft.screen instanceof ReceivingLevelScreen) {
            return false;
        }
        BlockPos pos = player.blockPosition();
        return minecraft.level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static double groundDistance(LocalPlayer player) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(player.getX(), player.getY(), player.getZ()).mutable();
        double feet = player.getY();
        for (int depth = 0; depth <= POOF_SCAN; depth++) {
            cursor.setY(Mth.floor(feet) - depth);
            if (cursor.getY() < player.level().getMinBuildHeight()) {
                return depth;
            }
            BlockState state = player.level().getBlockState(cursor);
            if (!state.getCollisionShape(player.level(), cursor).isEmpty() || !state.getFluidState().isEmpty()) {
                return feet - (cursor.getY() + 1.0D);
            }
        }
        return POOF_SCAN + 1.0D;
    }

    private static void poof(Minecraft minecraft, LocalPlayer player, double ground) {
        double y = player.getY() - Math.max(0.0D, ground) + 0.2D;
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2.0D / 24.0D;
            minecraft.level.addParticle(ParticleTypes.CLOUD, player.getX() + Math.cos(angle) * 0.6D, y, player.getZ() + Math.sin(angle) * 0.6D,
                    Math.cos(angle) * 0.25D, 0.02D, Math.sin(angle) * 0.25D);
        }
        minecraft.level.addParticle(ParticleTypes.GUST, player.getX(), y + 0.3D, player.getZ(), 0.0D, 0.0D, 0.0D);
        minecraft.level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_BURST.value(),
                SoundSource.PLAYERS, 0.8F, 0.8F, false);
    }

    public static boolean frozen() {
        return phase == Phase.SINKING;
    }

    public static float sink(float partialTick) {
        float depth = switch (style) {
            case ELYTHIA_IN -> 1.4F;
            case THORAXIS_IN, THORAXIS_OUT -> 1.6F;
            case CAVARYN_IN -> 1.5F;
            case CAVARYN_OUT -> -1.5F;
            default -> 0.0F;
        };
        return switch (phase) {
            case SINKING -> depth * ease(progress(partialTick, SINK_TICKS));
            case CANCELING -> depth * ease(cancelFrom) * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float roll(float partialTick) {
        if (!style.thoraxis()) {
            return 0.0F;
        }
        return switch (phase) {
            case SINKING -> 180.0F * ease(progress(partialTick, SINK_TICKS));
            case WAKING -> 180.0F * (1.0F - ease(progress(partialTick, 40.0F)));
            case CANCELING -> 180.0F * ease(cancelFrom) * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float shake(float partialTick) {
        if (!style.cavaryn() || phase != Phase.SINKING) {
            return 0.0F;
        }
        return 1.6F * progress(partialTick, SINK_TICKS);
    }

    public static float time(float partialTick) {
        return ticks + partialTick;
    }

    public static float vignette(float partialTick) {
        if (style == TransitStyle.ELYTHIA_HOME || style.prism()) {
            return 0.0F;
        }
        return switch (phase) {
            case SINKING -> progress(partialTick, SINK_TICKS);
            case WAKING -> 1.0F - progress(partialTick, 30.0F);
            case CANCELING -> cancelFrom * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float[] vignetteColor() {
        if (style.thoraxis()) {
            return NIGHTMARE;
        }
        return style.cavaryn() ? EARTH : BLACK;
    }

    public static float fill(float partialTick) {
        boolean bright = style == TransitStyle.ELYTHIA_HOME || style.prism();
        return switch (phase) {
            case SINKING -> bright
                    ? (float) Math.pow(progress(partialTick, SINK_TICKS), 1.5D)
                    : Mth.clamp((ticks + partialTick - 28.0F) / 12.0F, 0.0F, 1.0F);
            case WAKING -> 1.0F - progress(partialTick, wakeFadeTicks());
            case CANCELING -> Mth.clamp((cancelFrom - 0.7F) / 0.3F, 0.0F, 1.0F) * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static float[] fillColor() {
        if (style == TransitStyle.ELYTHIA_HOME || style.prism()) {
            return WHITE;
        }
        if (style == TransitStyle.CAVARYN_OUT && phase == Phase.WAKING) {
            return WHITE;
        }
        return BLACK;
    }

    public static float saturation(float partialTick) {
        return switch (phase) {
            case SINKING -> switch (style) {
                case ELYTHIA_IN -> 1.0F - 0.7F * progress(partialTick, SINK_TICKS);
                case THORAXIS_IN, THORAXIS_OUT -> 1.0F - 0.5F * progress(partialTick, SINK_TICKS);
                case CAVARYN_IN -> 1.0F - 0.8F * progress(partialTick, SINK_TICKS);
                case PRISM_IN, PRISM_OUT -> 1.0F + 0.8F * progress(partialTick, SINK_TICKS);
                default -> 1.0F;
            };
            case WAKING -> switch (style) {
                case ELYTHIA_IN -> Mth.lerp(Mth.clamp((ticks + partialTick - 20.0F) / 40.0F, 0.0F, 1.0F), 0.05F, 1.0F);
                case ELYTHIA_HOME -> Mth.lerp(progress(partialTick, 40.0F), 0.4F, 1.0F);
                case THORAXIS_IN, THORAXIS_OUT -> Mth.lerp(progress(partialTick, 40.0F), 0.5F, 1.0F);
                case CAVARYN_IN -> Mth.lerp(progress(partialTick, 50.0F), 0.2F, 1.0F);
                case CAVARYN_OUT -> Mth.lerp(progress(partialTick, 50.0F), 1.7F, 1.0F);
                case PRISM_IN, PRISM_OUT -> Mth.lerp(progress(partialTick, 40.0F), 1.8F, 1.0F);
            };
            case CANCELING -> Mth.lerp(progress(partialTick, CANCEL_TICKS), 0.5F, 1.0F);
            default -> 1.0F;
        };
    }

    public static float prism(float partialTick) {
        if (!style.prism()) {
            return 0.0F;
        }
        return switch (phase) {
            case SINKING -> progress(partialTick, SINK_TICKS);
            case WAKING -> 1.0F - progress(partialTick, 30.0F);
            case CANCELING -> cancelFrom * (1.0F - progress(partialTick, CANCEL_TICKS));
            default -> 0.0F;
        };
    }

    public static Collection<Disc> discs() {
        return DISCS.values();
    }

    private static float wakeFadeTicks() {
        return switch (style) {
            case ELYTHIA_IN, CAVARYN_IN -> 20.0F;
            case CAVARYN_OUT -> 40.0F;
            case PRISM_IN, PRISM_OUT -> 25.0F;
            default -> 30.0F;
        };
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
        private final TransitStyle style;
        private float age;

        private Disc(Vec3 center, TransitStyle style) {
            this.center = center;
            this.style = style;
        }

        public Vec3 center() {
            return this.center;
        }

        public TransitStyle style() {
            return this.style;
        }

        public float age() {
            return this.age;
        }
    }
}
