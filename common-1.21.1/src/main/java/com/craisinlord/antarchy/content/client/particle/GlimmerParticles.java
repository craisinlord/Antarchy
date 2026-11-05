package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class GlimmerParticles {
    private static final int AMBIENT_INTERVAL_TICKS = 6;
    private static final int TAME_BURST_COUNT = 24;
    private static final int ABILITY_BURST_COUNT = 16;
    private static final int READY_BURST_COUNT = 8;
    private static final int RING_POINTS_PER_BLOCK = 5;
    private static final DustParticleOptions AMBIENT = new DustParticleOptions(new Vector3f(0.55F, 0.85F, 1.0F), 0.9F);
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.6F, 0.9F, 1.0F), 1.1F);
    private static final DustParticleOptions RING = new DustParticleOptions(new Vector3f(0.67F, 0.99F, 0.93F), 1.4F);

    private GlimmerParticles() {
    }

    public static void tickAmbient(LivingEntity entity, boolean running) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!entity.isAlive() || entity.tickCount % AMBIENT_INTERVAL_TICKS != 0) {
            return;
        }

        int count = running ? 2 : 1;
        serverLevel.sendParticles(
                AMBIENT,
                entity.getRandomX(0.6D),
                entity.getY(0.3D) + entity.getBbHeight() * 0.6D,
                entity.getRandomZ(0.6D),
                count,
                0.15D,
                0.2D,
                0.15D,
                0.0D
        );

        Vec3 motion = entity.getDeltaMovement();
        if (running && motion.horizontalDistanceSqr() > 1.0E-4D) {
            Vec3 direction = new Vec3(motion.x, 0.0D, motion.z).normalize();
            Vec3 behind = entity.position().subtract(direction.scale(0.4D)).add(0.0D, 0.15D, 0.0D);
            serverLevel.sendParticles(TRAIL, behind.x, behind.y, behind.z, 1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }

    public static void tickBurst(LivingEntity entity) {
        burst(entity, TAME_BURST_COUNT);
    }

    public static void abilityBurst(LivingEntity entity) {
        burst(entity, ABILITY_BURST_COUNT);
    }

    public static void readyPulse(LivingEntity entity) {
        burst(entity, READY_BURST_COUNT);
    }

    public static void ring(LivingEntity center, double radius) {
        if (!(center.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        int points = Math.max(8, Mth.ceil(radius * RING_POINTS_PER_BLOCK));
        double y = center.getY() + 0.3D;
        for (int i = 0; i < points; i++) {
            double angle = Math.PI * 2.0D * i / points;
            serverLevel.sendParticles(RING, center.getX() + Math.cos(angle) * radius, y, center.getZ() + Math.sin(angle) * radius,
                    1, 0.05D, 0.1D, 0.05D, 0.0D);
        }
    }

    private static void burst(LivingEntity entity, int count) {
        if (!(entity.level() instanceof ServerLevel serverLevel) || !entity.isAlive()) {
            return;
        }

        double spreadX = entity.getBbWidth() * 0.5D + 0.1D;
        double spreadY = entity.getBbHeight() * 0.5D + 0.1D;
        serverLevel.sendParticles(
                TRAIL,
                entity.getX(),
                entity.getY(0.5D),
                entity.getZ(),
                count,
                spreadX,
                spreadY,
                spreadX,
                0.0D
        );
    }
}
