package com.craisinlord.antarchy.content.entity;

import com.craisinlord.antarchy.content.AntarchyObjects;
import com.craisinlord.antarchy.content.weather.ElythiaEvent;
import com.craisinlord.antarchy.content.weather.ElythiaEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class OuranwoodDeerMigration {
    private static final double CROWDED_RADIUS = 64.0D;
    private static final int CROWDED_COUNT = 6;
    private static final double HERD_BEHIND_DISTANCE = 40.0D;
    private static final double HERD_SIDE_SPREAD = 24.0D;
    private static final int HERD_ANCHOR_ATTEMPTS = 6;
    private static final int MEMBER_SPREAD = 4;
    private static final int MEMBER_ATTEMPTS = 4;
    private static final int MIN_ADULTS = 3;
    private static final int MAX_ADULTS = 6;
    private static final int MAX_FAWNS = 2;

    private OuranwoodDeerMigration() {
    }

    @Nullable
    public static Vec3 heading(ServerLevel level) {
        if (!ElythiaEvents.isActive(level, ElythiaEvent.DEER_MIGRATION)) {
            return null;
        }
        return ElythiaEvents.get(level).heading();
    }

    public static void begin(ServerLevel level, Vec3 heading) {
        if (!level.getGameRules().get(GameRules.SPAWN_MOBS)) {
            return;
        }
        EntityType<OuranwoodDeerEntity> type = AntarchyObjects.OURANWOOD_DEER.get();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            AABB area = player.getBoundingBox().inflate(CROWDED_RADIUS);
            if (level.getEntities(type, area, deer -> true).size() >= CROWDED_COUNT) {
                continue;
            }
            BlockPos anchor = findAnchor(level, player, heading, type);
            if (anchor != null) {
                spawnHerd(level, anchor, type);
            }
        }
    }

    @Nullable
    private static BlockPos findAnchor(ServerLevel level, ServerPlayer player, Vec3 heading, EntityType<OuranwoodDeerEntity> type) {
        RandomSource random = level.getRandom();
        Vec3 side = new Vec3(-heading.z, 0.0D, heading.x);
        for (int attempt = 0; attempt < HERD_ANCHOR_ATTEMPTS; attempt++) {
            double lateral = (random.nextDouble() * 2.0D - 1.0D) * HERD_SIDE_SPREAD;
            Vec3 point = player.position().subtract(heading.scale(HERD_BEHIND_DISTANCE)).add(side.scale(lateral));
            BlockPos pos = surface(level, Mth.floor(point.x), Mth.floor(point.z));
            if (pos != null && canStand(level, pos, type)) {
                return pos;
            }
        }
        return null;
    }

    private static void spawnHerd(ServerLevel level, BlockPos anchor, EntityType<OuranwoodDeerEntity> type) {
        RandomSource random = level.getRandom();
        int adults = Mth.nextInt(random, MIN_ADULTS, MAX_ADULTS);
        int fawns = random.nextInt(MAX_FAWNS + 1);
        for (int i = 0; i < adults + fawns; i++) {
            for (int attempt = 0; attempt < MEMBER_ATTEMPTS; attempt++) {
                int x = anchor.getX() + random.nextInt(MEMBER_SPREAD * 2 + 1) - MEMBER_SPREAD;
                int z = anchor.getZ() + random.nextInt(MEMBER_SPREAD * 2 + 1) - MEMBER_SPREAD;
                BlockPos pos = surface(level, x, z);
                if (pos == null || !canStand(level, pos, type)) {
                    continue;
                }
                OuranwoodDeerEntity deer = type.create(level, EntitySpawnReason.NATURAL);
                if (deer == null) {
                    return;
                }
                deer.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, random.nextFloat() * 360.0F, 0.0F);
                deer.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.NATURAL, null);
                if (i >= adults) {
                    deer.setAge(-24000);
                }
                level.addFreshEntity(deer);
                break;
            }
        }
    }

    @Nullable
    private static BlockPos surface(ServerLevel level, int x, int z) {
        BlockPos column = new BlockPos(x, 0, z);
        if (!level.hasChunkAt(column)) {
            return null;
        }
        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
    }

    private static boolean canStand(ServerLevel level, BlockPos pos, EntityType<OuranwoodDeerEntity> type) {
        return level.getFluidState(pos.below()).isEmpty()
                && Animal.checkAnimalSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, level.getRandom())
                && level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D));
    }
}
