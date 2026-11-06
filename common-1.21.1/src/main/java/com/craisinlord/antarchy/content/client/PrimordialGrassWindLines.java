package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.client.renderer.PrimordialGrassRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class PrimordialGrassWindLines {
    private static final ResourceLocation PARTICLE = ResourceLocation.fromNamespaceAndPath("antarchy", "wind_line");
    private static final double MIN_GUST = 0.25D;
    private static final double SEARCH_RADIUS = 24.0D;
    private static final int SAMPLES_PER_TICK = 4;
    private static final int MAX_SPAWNS_PER_TICK = 2;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final double[] TUFT = new double[3];

    private PrimordialGrassWindLines() {
    }

    public static void tick(Minecraft minecraft, ClientLevel level, Vec3 center) {
        if (!AntarchySettings.elythiaGrassWindEnabled() || !AntarchySettings.elythiaWindLinesEnabled()) return;
        ParticleStatus status = minecraft.options.particles().get();
        if (status == ParticleStatus.MINIMAL) return;
        if (status == ParticleStatus.DECREASED && RANDOM.nextBoolean()) return;
        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.get(PARTICLE);
        if (!(type instanceof ParticleOptions options)) return;
        double ticks = level.getGameTime();
        double angle = ElythiaWind.angle(ticks);
        double dx = Math.cos(angle);
        double dz = Math.sin(angle);
        int spawned = 0;
        for (int i = 0; i < SAMPLES_PER_TICK && spawned < MAX_SPAWNS_PER_TICK; i++) {
            if (!PrimordialGrassRenderer.sampleExposedTuft(RANDOM, center, SEARCH_RADIUS, TUFT)) continue;
            double gust = PrimordialGrassWind.gust(ticks, TUFT[0], TUFT[2], dx, dz);
            if (gust < MIN_GUST || RANDOM.nextDouble() > gust * gust) continue;
            double speed = 0.6D + gust * 0.5D;
            double x = TUFT[0] + (RANDOM.nextDouble() - 0.5D) * 3.0D;
            double y = TUFT[1] + 0.6D + RANDOM.nextDouble() * 1.4D;
            double z = TUFT[2] + (RANDOM.nextDouble() - 0.5D) * 3.0D;
            level.addParticle(options, x, y, z, dx * speed, gust, dz * speed);
            spawned++;
        }
    }
}
