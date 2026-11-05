package com.craisinlord.antarchy.content.throwback;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.network.ThrowbackZoneSync;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public final class ThrowbackZoneTracker {
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static final int FULL_RECHECK_TICKS = 100;
    private static final int RESEND_TICKS = 100;
    private static final int MOVE_RECHECK_DISTANCE = 2;
    private static final float SEND_THRESHOLD = 0.01F;
    private static final int REMEMBRANCE_DURATION_TICKS = 100;
    private static final Map<ServerPlayer, Sample> SAMPLES = new WeakHashMap<>();

    private ThrowbackZoneTracker() {
    }

    public static void tick(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0L) {
            return;
        }
        boolean active = level.dimension().equals(ThrowbackZones.ELYTHIA) && AntarchySettings.throwbackZonesEnabled();
        Holder<MobEffect> remembrance = AntarchyObjects.REMEMBRANCE_EFFECT.get();
        for (ServerPlayer player : level.players()) {
            ThrowbackZones.Influence influence = active && player.isAlive() && !player.isSpectator()
                    ? sample(level, player, gameTime)
                    : ThrowbackZones.Influence.NONE;
            applyEffect(player, remembrance, influence.inside());
            sync(player, influence.strength(), gameTime);
        }
    }

    private static ThrowbackZones.Influence sample(ServerLevel level, ServerPlayer player, long gameTime) {
        BlockPos pos = player.blockPosition();
        Sample sample = SAMPLES.get(player);
        if (sample != null
                && sample.level == level
                && sample.influence != null
                && gameTime - sample.sampledAt < FULL_RECHECK_TICKS
                && sample.pos.distManhattan(pos) < MOVE_RECHECK_DISTANCE) {
            return sample.influence;
        }
        ThrowbackZones.Influence influence = ThrowbackZones.influenceAt(level, pos);
        Sample updated = sample == null ? new Sample() : sample;
        updated.level = level;
        updated.pos = pos;
        updated.sampledAt = gameTime;
        updated.influence = influence;
        SAMPLES.put(player, updated);
        return influence;
    }

    private static void applyEffect(ServerPlayer player, Holder<MobEffect> remembrance, boolean inside) {
        if (!inside) {
            return;
        }
        MobEffectInstance current = player.getEffect(remembrance);
        if (current != null && !current.isAmbient()) {
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
        ThrowbackZoneSync.send(player, strength);
        sample.lastSent = strength;
        sample.sentAt = gameTime;
    }

    private static final class Sample {
        private ServerLevel level;
        private BlockPos pos;
        private long sampledAt;
        private ThrowbackZones.Influence influence;
        private float lastSent;
        private long sentAt;
    }
}
