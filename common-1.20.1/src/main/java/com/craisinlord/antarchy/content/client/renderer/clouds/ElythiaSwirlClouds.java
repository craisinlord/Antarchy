package com.craisinlord.antarchy.content.client.renderer.clouds;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;

final class ElythiaSwirlClouds {
    static final double REGION = 768.0D;
    static final double RANGE = 800.0D;
    private static final double CHANCE = 0.18D;
    private static final long SALT = 0x5F1A2C3B7D9E4F61L;
    private static final double FALLBACK_GROUND = 120.0D;
    private static final double MIN_GROUND = 100.0D;
    private static final double GROUND_LIFT = 55.0D;
    private static final double LIFT_SPREAD = 35.0D;

    interface SiteConsumer {
        void accept(long key, long seed, double x, double z);
    }

    private ElythiaSwirlClouds() {
    }

    static void forEachSite(double camX, double camZ, SiteConsumer consumer) {
        int minX = Mth.floor((camX - RANGE) / REGION);
        int maxX = Mth.floor((camX + RANGE) / REGION);
        int minZ = Mth.floor((camZ - RANGE) / REGION);
        int maxZ = Mth.floor((camZ + RANGE) / REGION);
        for (int rx = minX; rx <= maxX; rx++) {
            for (int rz = minZ; rz <= maxZ; rz++) {
                long key = ((long) rx << 32) ^ (rz & 0xFFFFFFFFL);
                long hash = mix(key ^ SALT);
                if (unit(hash) >= CHANCE) {
                    continue;
                }
                double x = (rx + 0.2D + 0.6D * unit(mix(hash ^ 0x1L))) * REGION;
                double z = (rz + 0.2D + 0.6D * unit(mix(hash ^ 0x2L))) * REGION;
                double dx = x - camX;
                double dz = z - camZ;
                if (dx * dx + dz * dz > RANGE * RANGE) {
                    continue;
                }
                consumer.accept(key, mix(hash ^ 0x3L), x, z);
            }
        }
    }

    static boolean outOfRange(double x, double z, double camX, double camZ) {
        double dx = x - camX;
        double dz = z - camZ;
        double limit = RANGE + REGION * 0.5D;
        return dx * dx + dz * dz > limit * limit;
    }

    static double baseY(ClientLevel level, double x, double z, long seed) {
        double lift = GROUND_LIFT + unit(mix(seed ^ 0x4L)) * LIFT_SPREAD;
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        if (!level.hasChunk(SectionPos.blockToSectionCoord(blockX), SectionPos.blockToSectionCoord(blockZ))) {
            return FALLBACK_GROUND + lift;
        }
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
        return Math.max(MIN_GROUND, ground) + lift;
    }

    private static long mix(long value) {
        long h = value * 0x9E3779B97F4A7C15L;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }

    private static double unit(long hash) {
        return (hash >>> 11) * 0x1.0p-53;
    }
}
