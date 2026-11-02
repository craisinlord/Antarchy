package com.craisinlord.antarchy.content.glimmerecho;

import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class GlimmerEchoes {
    public static final String SAVE_KEY = "GlimmerEcho";
    private static final DustParticleOptions AMBIENT = new DustParticleOptions(new Vector3f(0.55F, 0.85F, 1.0F), 1.2F);
    private static final DustParticleOptions BURST = new DustParticleOptions(new Vector3f(0.7F, 0.95F, 1.0F), 1.6F);

    private static final int RING_SEGMENTS = 8;

    private GlimmerEchoes() {
    }

    public static boolean isGlimmerEcho(@Nullable Entity entity) {
        return entity instanceof GlimmerEcho echo && echo.isGlimmerEcho();
    }

    public static ParticleOptions particle(@Nullable Entity source, ParticleOptions normal) {
        return isGlimmerEcho(source) ? BURST : normal;
    }

    public static ParticleOptions dust() {
        return AMBIENT;
    }

    public static void markSummon(Entity owner, Entity summon) {
        if (isGlimmerEcho(owner) && summon instanceof GlimmerEcho echo) {
            echo.setGlimmerEcho(true);
            echo.setGlimmerEchoOwner(owner.getUUID());
        }
    }

    public static boolean tickOwnedSummon(LivingEntity summon, @Nullable UUID owner) {
        if (owner == null || summon.tickCount % 20 != 0 || !(summon.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        Entity ownerEntity = serverLevel.getEntity(owner);
        if (ownerEntity != null && ownerEntity.isAlive()) {
            return false;
        }
        fadeQuietly(summon);
        return true;
    }

    public static float ownerDamageMultiplier(Entity summon, @Nullable UUID owner) {
        if (owner == null || !isGlimmerEcho(summon) || !(summon.level() instanceof ServerLevel serverLevel)) {
            return 1.0F;
        }
        return damageMultiplier(serverLevel.getEntity(owner));
    }

    public static float damageMultiplier(@Nullable Entity owner) {
        if (owner instanceof GlimmerEcho echo && echo.isGlimmerEcho()) {
            return echo.glimmerEchoDamageMultiplier();
        }
        return 1.0F;
    }

    public static void tickAmbient(LivingEntity entity, int interval, int count) {
        if (!(entity.level() instanceof ServerLevel serverLevel) || !entity.isAlive() || entity.tickCount % interval != 0) {
            return;
        }
        double width = entity.getBbWidth() * 0.5D;
        double height = entity.getBbHeight() * 0.5D;
        serverLevel.sendParticles(AMBIENT, entity.getX(), entity.getY() + height, entity.getZ(),
                count, width, height, width, 0.0D);
    }

    public static void burst(ServerLevel level, Vec3 center, double radius, int count) {
        level.sendParticles(BURST, center.x, center.y, center.z, count, radius, radius * 0.5D, radius, 0.0D);
    }

    public static void ring(ServerLevel level, Vec3 center, double radius, int points) {
        int segments = Math.min(points, RING_SEGMENTS);
        int perSegment = Math.max(1, points / segments);
        double spread = Math.max(0.05D, radius * Math.PI / segments * 0.3D);
        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2.0D / segments;
            level.sendParticles(BURST, center.x + Math.cos(angle) * radius, center.y + 0.2D,
                    center.z + Math.sin(angle) * radius, perSegment, spread, 0.1D, spread, 0.0D);
        }
    }

    public static void column(ServerLevel level, Vec3 base, double height, int count) {
        level.sendParticles(BURST, base.x, base.y + height * 0.5D, base.z, count, 0.15D, height * 0.35D, 0.15D, 0.0D);
    }

    public static boolean hasPlayerWithin(LivingEntity entity, double range) {
        double rangeSqr = range * range;
        for (Player player : entity.level().players()) {
            if (!player.isSpectator() && player.isAlive() && player.distanceToSqr(entity) <= rangeSqr) {
                return true;
            }
        }
        return false;
    }

    public static void fadeAway(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            burst(serverLevel, entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D),
                    entity.getBbWidth() * 0.6D, 60);
            serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.5F, 1.4F);
        }
        entity.discard();
    }

    public static void fadeQuietly(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            burst(serverLevel, entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D),
                    entity.getBbWidth() * 0.6D, 12);
        }
        entity.discard();
    }
}
