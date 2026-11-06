package com.craisinlord.antarchy.content.throwback;

import com.craisinlord.antarchy.content.network.ThrowbackZonePayload;
import com.craisinlord.antarchy.content.network.AntarchyNetwork;
import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class ThrowbackZoneTracker {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final int TORCH_TICK_OFFSET = 5;
    private static final int RESEND_TICKS = 100;
    private static final float SEND_THRESHOLD = 0.01F;
    private static final int REMEMBRANCE_DURATION_TICKS = 100;
    private static final int REMEMBRANCE_REFRESH_BELOW_TICKS = 60;
    private static final Map<ServerPlayer, Sample> SAMPLES = new WeakHashMap<>();
    private static final Map<ServerPlayer, ThrowbackZones.Influence> PENDING = new WeakHashMap<>();

    private ThrowbackZoneTracker() {
    }

    public static boolean isTorchTick(long gameTime) {
        return gameTime % CHECK_INTERVAL_TICKS == TORCH_TICK_OFFSET;
    }

    public static void contribute(ServerPlayer player, ThrowbackZones.Influence influence) {
        PENDING.merge(player, influence, ThrowbackZones.Influence::merge);
    }

    public static void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0L) {
            return;
        }
        boolean active = AntarchySettings.throwbackZonesEnabled();
        MobEffect remembrance = AntarchyObjects.REMEMBRANCE_EFFECT.get();
        for (ServerPlayer player : level.players()) {
            ThrowbackZones.Influence influence = PENDING.remove(player);
            if (influence == null || !active || !player.isAlive() || player.isSpectator()) {
                influence = ThrowbackZones.Influence.NONE;
            }
            applyEffect(player, remembrance, influence.inside());
            sync(player, influence.strength(), gameTime);
        }
    }

    private static void applyEffect(ServerPlayer player, MobEffect remembrance, boolean inside) {
        if (!inside) {
            return;
        }
        MobEffectInstance current = player.getEffect(remembrance);
        if (current != null && (!current.isAmbient() || current.getDuration() > REMEMBRANCE_REFRESH_BELOW_TICKS)) {
            return;
        }
        player.addEffect(new MobEffectInstance(remembrance, REMEMBRANCE_DURATION_TICKS, 0, true, false, true));
    }

    private static void sync(ServerPlayer player, float strength, long gameTime) {
        Sample sample = SAMPLES.computeIfAbsent(player, ignored -> new Sample());
        boolean changed = Math.abs(strength - sample.lastSent) >= SEND_THRESHOLD
                || (strength == 0.0F) != (sample.lastSent == 0.0F);
        boolean keepAlive = strength > 0.0F && gameTime - sample.sentAt >= RESEND_TICKS;
        if (!changed && !keepAlive) {
            return;
        }
        AntarchyNetwork.sendToPlayer(player, new ThrowbackZonePayload(strength));
        sample.lastSent = strength;
        sample.sentAt = gameTime;
    }

    private static final class Sample {
        private float lastSent;
        private long sentAt;
    }
}
