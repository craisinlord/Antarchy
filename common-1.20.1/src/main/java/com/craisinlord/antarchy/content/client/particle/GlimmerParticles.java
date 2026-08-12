package com.craisinlord.antarchy.content.client.particle;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class GlimmerParticles {
    private static final int AMBIENT_INTERVAL_TICKS = 6;
    private static final DustParticleOptions AMBIENT = new DustParticleOptions(new Vector3f(0.55F, 0.85F, 1.0F), 0.9F);
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.6F, 0.9F, 1.0F), 1.1F);

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
        if (!(entity.level() instanceof ServerLevel serverLevel) || !entity.isAlive()) {
            return;
        }

        serverLevel.sendParticles(
                TRAIL,
                entity.getRandomX(0.4D),
                entity.getY(0.1D) + entity.getBbHeight() * 0.4D,
                entity.getRandomZ(0.4D),
                3,
                0.12D,
                0.12D,
                0.12D,
                0.0D
        );
    }
}
