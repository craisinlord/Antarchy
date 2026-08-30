package com.craisinlord.antarchy.content.time;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class TimeDilationParticles {
    private static final int PARTICLE_INTERVAL_TICKS = 4;
    private static final int MAX_RING_POINTS = 96;

    private TimeDilationParticles() {
    }

    public static void spawnFieldBorders(ServerLevel level, List<TimeDilationFieldEntity> fields) {
        if (level.getGameTime() % PARTICLE_INTERVAL_TICKS != 0L) {
            return;
        }
        for (TimeDilationFieldEntity field : fields) {
            spawnBorder(level, field.position(), field.fieldRadius());
        }
    }

    private static void spawnBorder(ServerLevel level, Vec3 center, double radius) {
        int points = Mth.clamp((int) Math.ceil(radius * 10.0D), 24, MAX_RING_POINTS);
        double phase = (level.getGameTime() % 80L) * 0.07853981633974483D;

        for (int i = 0; i < points; i++) {
            double angle = phase + (Math.PI * 2.0D * i / points);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            spawnParticle(level, x, center.y, z);

            if (i % 4 == 0) {
                spawnParticle(level, x, center.y + radius * 0.5D, z);
                spawnParticle(level, x, center.y - radius * 0.5D, z);
            }
            if (i % 8 == 0) {
                spawnParticle(level, center.x + Math.cos(angle) * radius * 0.7D, center.y + Math.sin(angle) * radius, center.z);
                spawnParticle(level, center.x, center.y + Math.sin(angle) * radius, center.z + Math.cos(angle) * radius * 0.7D);
            }
        }
    }

    private static void spawnParticle(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
    }
}
