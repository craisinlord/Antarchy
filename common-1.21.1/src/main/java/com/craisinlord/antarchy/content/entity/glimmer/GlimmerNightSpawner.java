package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import com.craisinlord.antarchy.content.weather.ElythiaEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

public final class GlimmerNightSpawner {
    private static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );

    private static final int CHECK_INTERVAL_TICKS = 20 * 10;
    private static final float SPAWN_CHANCE = 0.35F;
    private static final double NEARBY_RADIUS = 64.0D;
    private static final int MAX_NEARBY_WILD = 2;
    private static final double MIN_SPAWN_DISTANCE = 24.0D;
    private static final double MAX_SPAWN_DISTANCE = 56.0D;
    private static final int COLUMN_ATTEMPTS = 8;
    private static final int LUMEN_RAIN_INTERVAL_TICKS = 20 * 3;
    private static final float LUMEN_RAIN_SPAWN_CHANCE = 0.7F;
    private static final int LUMEN_RAIN_MAX_NEARBY_WILD = 12;
    private static final double LUMEN_RAIN_MIN_SPAWN_DISTANCE = 12.0D;
    private static final double LUMEN_RAIN_MAX_SPAWN_DISTANCE = 40.0D;

    private GlimmerNightSpawner() {
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(ELYTHIA)) {
            return;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }
        boolean lumenRain = ElythiaEvents.isActive(level, ElythiaEvent.LUMEN_RAIN);
        if (lumenRain) {
            if (level.getGameTime() % LUMEN_RAIN_INTERVAL_TICKS == 0L) {
                spawnForPlayers(level, LUMEN_RAIN_SPAWN_CHANCE, LUMEN_RAIN_MAX_NEARBY_WILD, true);
            }
            return;
        }
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0L || !level.isNight()) {
            return;
        }
        spawnForPlayers(level, SPAWN_CHANCE, MAX_NEARBY_WILD, false);
    }

    private static void spawnForPlayers(ServerLevel level, float chance, int maxNearby, boolean lumenRain) {
        EntityType<GlimmerEntity> type = AntarchyObjects.GLIMMER.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (level.random.nextFloat() >= chance) {
                continue;
            }
            if (countNearbyWild(level, player, type) >= maxNearby) {
                continue;
            }
            attemptSpawnNear(level, player, type, lumenRain);
        }
    }

    private static int countNearbyWild(ServerLevel level, ServerPlayer player, EntityType<GlimmerEntity> type) {
        AABB box = player.getBoundingBox().inflate(NEARBY_RADIUS);
        return level.getEntities(type, box, glimmer -> !glimmer.isTame()).size();
    }

    private static void attemptSpawnNear(ServerLevel level, ServerPlayer player, EntityType<GlimmerEntity> type, boolean lumenRain) {
        RandomSource random = level.random;
        BlockPos origin = player.blockPosition();
        double minDistance = lumenRain ? LUMEN_RAIN_MIN_SPAWN_DISTANCE : MIN_SPAWN_DISTANCE;
        double maxDistance = lumenRain ? LUMEN_RAIN_MAX_SPAWN_DISTANCE : MAX_SPAWN_DISTANCE;
        for (int attempt = 0; attempt < COLUMN_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);
            if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                continue;
            }
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (lumenRain) {
                if (!Animal.checkAnimalSpawnRules(type, level, MobSpawnType.NATURAL, pos, random)) {
                    continue;
                }
            } else if (!biomeSpawnsGlimmers(level.getBiome(pos), type)
                    || !SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, random)) {
                continue;
            }
            if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
                continue;
            }
            GlimmerEntity glimmer = type.create(level);
            if (glimmer == null) {
                return;
            }
            glimmer.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
            glimmer.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.NATURAL, null);
            level.addFreshEntity(glimmer);
            return;
        }
    }

    private static boolean biomeSpawnsGlimmers(Holder<Biome> biome, EntityType<GlimmerEntity> type) {
        return biome.value().getMobSettings().getMobs(MobCategory.CREATURE).unwrap().stream()
                .anyMatch(data -> data.type == type);
    }
}
