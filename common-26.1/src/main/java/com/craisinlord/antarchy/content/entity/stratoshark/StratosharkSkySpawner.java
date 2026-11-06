package com.craisinlord.antarchy.content.entity.stratoshark;

import com.craisinlord.antarchy.Antarchy;
import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.AntarchyTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;

public final class StratosharkSkySpawner {
    private static final ResourceKey<Level> ELYTHIA = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(Antarchy.MODID, "elythia")
    );

    private static final int CHECK_INTERVAL_TICKS = 20 * 20;
    private static final float SPAWN_CHANCE = 0.3F;
    private static final double NEARBY_RADIUS = 96.0D;
    private static final int MAX_NEARBY = 2;
    private static final double MIN_SPAWN_DISTANCE = 40.0D;
    private static final double MAX_SPAWN_DISTANCE = 80.0D;
    private static final int MIN_HEIGHT_ABOVE_CANOPY = 40;
    private static final int MAX_HEIGHT_ABOVE_CANOPY = 70;
    private static final int COLUMN_ATTEMPTS = 8;

    private StratosharkSkySpawner() {
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(ELYTHIA)) {
            return;
        }
        if (level.getGameTime() % CHECK_INTERVAL_TICKS != 0L) {
            return;
        }
        if (!level.getGameRules().get(GameRules.SPAWN_MOBS) || !level.getGameRules().get(GameRules.SPAWN_MONSTERS)) {
            return;
        }

        EntityType<StratosharkEntity> type = AntarchyObjects.STRATOSHARK.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (level.getRandom().nextFloat() >= SPAWN_CHANCE) {
                continue;
            }
            if (level.getEntities(type, player.getBoundingBox().inflate(NEARBY_RADIUS), shark -> shark.isAlive()).size() >= MAX_NEARBY) {
                continue;
            }
            attemptSpawnNear(level, player, type);
        }
    }

    private static void attemptSpawnNear(ServerLevel level, ServerPlayer player, EntityType<StratosharkEntity> type) {
        RandomSource random = level.getRandom();
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < COLUMN_ATTEMPTS; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = MIN_SPAWN_DISTANCE + random.nextDouble() * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);
            if (!level.hasChunkAt(new BlockPos(x, origin.getY(), z))) {
                continue;
            }
            BlockPos canopy = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(x, 0, z));
            if (!level.getBiome(canopy).is(AntarchyTags.Biomes.STRATOSHARK_SKY_SPAWN_BIOMES)) {
                continue;
            }
            int y = canopy.getY() + Mth.nextInt(random, MIN_HEIGHT_ABOVE_CANOPY, MAX_HEIGHT_ABOVE_CANOPY);
            if (y > level.getMaxY() + 1 - 8) {
                continue;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (!SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, random)) {
                continue;
            }
            if (!level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))) {
                continue;
            }
            StratosharkEntity shark = type.create(level, EntitySpawnReason.NATURAL);
            if (shark == null) {
                return;
            }
            shark.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
            shark.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
            level.addFreshEntity(shark);
            return;
        }
    }
}
