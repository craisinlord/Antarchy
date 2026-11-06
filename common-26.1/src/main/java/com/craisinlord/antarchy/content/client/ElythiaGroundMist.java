package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.config.AntarchySettings;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ElythiaGroundMist {
    private static final double SPAWN_RADIUS = 40.0D;
    private static final double MAX_HEIGHT_DIFFERENCE = 24.0D;
    private static final float SPAWNS_PER_TICK = 1.6F;
    private static final double WIND_SPEED = 0.012D;

    private ElythiaGroundMist() {
    }

    public static void tick(Minecraft minecraft, ClientLevel level, ParticleOptions particle) {
        Player player = minecraft.player;
        if (player == null || !AntarchySettings.elythiaGroundMistEnabled()) {
            return;
        }
        ParticleStatus status = minecraft.options.particles().get();
        if (status == ParticleStatus.MINIMAL) {
            return;
        }

        long dayTime = level.getOverworldClockTime();
        long morningIndex = Math.floorDiv(dayTime + 2000L, 24000L);
        float strength = morningStrength(Math.floorMod(dayTime, 24000L)) * (0.55F + 0.45F * hash01(morningIndex, 0xD1L));
        if (strength <= 0.01F) {
            return;
        }

        RandomSource random = level.getRandom();
        float rate = SPAWNS_PER_TICK * strength * (status == ParticleStatus.DECREASED ? 0.5F : 1.0F);
        int count = Mth.floor(rate) + (random.nextFloat() < rate - Mth.floor(rate) ? 1 : 0);
        if (count == 0) {
            return;
        }

        double windAngle = ElythiaWind.angle(level.getGameTime());
        double windX = Math.cos(windAngle) * WIND_SPEED;
        double windZ = Math.sin(windAngle) * WIND_SPEED;
        double peakAlpha = 0.18D + 0.2D * strength;

        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() * 2.0D - 1.0D) * SPAWN_RADIUS;
            double z = player.getZ() + (random.nextDouble() * 2.0D - 1.0D) * SPAWN_RADIUS;
            int blockX = Mth.floor(x);
            int blockZ = Mth.floor(z);
            if (!level.hasChunk(blockX >> 4, blockZ >> 4)) {
                continue;
            }
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
            if (Math.abs(surface - player.getY()) > MAX_HEIGHT_DIFFERENCE) {
                continue;
            }
            BlockPos pos = new BlockPos(blockX, surface, blockZ);
            if (!level.getBiome(pos).is(AntarchyTags.Biomes.ELYTHIA_GROUND_MIST_BIOMES)) {
                continue;
            }
            double y = surface + 0.2D + random.nextDouble() * 1.2D;
            level.addParticle(particle, x, y, z, windX, peakAlpha, windZ);
        }
    }

    private static float morningStrength(long timeOfDay) {
        float t = timeOfDay >= 20000L ? timeOfDay - 24000.0F : timeOfDay;
        if (t < -2000.0F || t > 3500.0F) {
            return 0.0F;
        }
        if (t < -500.0F) {
            return smooth((t + 2000.0F) / 1500.0F);
        }
        if (t < 1000.0F) {
            return 1.0F;
        }
        return smooth(1.0F - (t - 1000.0F) / 2500.0F);
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static float hash01(long value, long salt) {
        long h = value * 0x9E3779B97F4A7C15L ^ salt * 0xC2B2AE3D27D4EB4FL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h ^= h >>> 31;
        return (h >>> 40) * 0x1.0p-24F;
    }
}
