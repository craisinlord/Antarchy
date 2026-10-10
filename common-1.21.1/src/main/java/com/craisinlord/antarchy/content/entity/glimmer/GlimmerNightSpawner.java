package com.craisinlord.antarchy.content.entity.glimmer;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
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

    private GlimmerNightSpawner() {
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(ELYTHIA)) {
            return;
        }
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0L || !level.isNight()) {
            return;
        }
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }

        EntityType<GlimmerEntity> type = AntarchyObjects.GLIMMER.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (level.random.nextFloat() >= SPAWN_CHANCE) {
                continue;
            }
            if (countNearbyWild(level, player, type) >= MAX_NEARBY_WILD) {
                continue;
            }
            attemptSpawnNear(level, player, type);
        }
    }

    private static int countNearbyWild(ServerLevel level, ServerPlayer player, EntityType<GlimmerEntity> type) {
        AABB box = player.getBoundingBox().inflate(NEARBY_RADIUS);
        return level.getEntities(type, box, glimmer -> !glimmer.isTame()).size();
    }

    private static void attemptSpawnNear(ServerLevel level, ServerPlayer player, EntityType<GlimmerEntity> type) {
        RandomSource random = level.random;
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < COLUMN_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);
            if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                continue;
            }
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
            if (!biomeSpawnsGlimmers(level.getBiome(pos), type)) {
                continue;
            }
            if (!SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, random)) {
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
