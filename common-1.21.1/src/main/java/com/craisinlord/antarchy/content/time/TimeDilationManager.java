package com.craisinlord.antarchy.content.time;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public final class TimeDilationManager {
    private static final Set<ResourceKey<Level>> DILATED_LEVELS = new HashSet<>();

    private TimeDilationManager() {
    }

    public static void tickServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            List<TimeDilationFieldEntity> fields = collectFields(level);
            syncFieldSnapshots(level, fields);
            if (fields.isEmpty()) {
                if (DILATED_LEVELS.remove(level.dimension())) {
                    resetRates(level);
                }
                continue;
            }
            DILATED_LEVELS.add(level.dimension());
            updateEntities(level, fields);
            TimeDilationParticles.spawnFieldBorders(level, fields);
        }
    }

    private static void syncFieldSnapshots(ServerLevel level, List<TimeDilationFieldEntity> fields) {
        List<TimeDilationFieldSnapshot> snapshots = fields.stream()
                .map(field -> new TimeDilationFieldSnapshot(
                        field.getX(), field.getY(), field.getZ(), field.fieldRadius(), field.fieldRate(),
                        field.fieldAge(), field.fieldDurationTicks()))
                .limit(128)
                .toList();
        for (ServerPlayer player : level.players()) {
            TimeDilationApi.syncFields(player, snapshots);
        }
    }

    private static List<TimeDilationFieldEntity> collectFields(ServerLevel level) {
        List<TimeDilationFieldEntity> fields = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof TimeDilationFieldEntity field && field.isAlive()) {
                fields.add(field);
            }
        }
        return fields;
    }

    private static void updateEntities(ServerLevel level, List<TimeDilationFieldEntity> fields) {
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof TimeDilationEntityAccess access) || entity instanceof TimeDilationFieldEntity) {
                continue;
            }
            if (entity.getType().is(com.craisinlord.antarchy.content.AntarchyTags.Entities.TIME_DILATION_IMMUNE)) {
                if (access.antarchy$getTimeDilationRate() < TimeDilationMath.NORMAL_RATE) {
                    access.antarchy$setTimeDilationRate(TimeDilationMath.NORMAL_RATE);
                    TimeDilationApi.syncEntityRate(entity, TimeDilationMath.NORMAL_RATE);
                }
                continue;
            }
            double previousRate = access.antarchy$getTimeDilationRate();
            double rate = TimeDilationFieldSampler.sample(fields, entity.position());
            access.antarchy$setTimeDilationRate(rate);
            if (Math.abs(previousRate - rate) > 0.001D) {
                TimeDilationApi.syncEntityRate(entity, rate);
            }
        }
    }

    private static void resetRates(ServerLevel level) {
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof TimeDilationEntityAccess access) || entity instanceof TimeDilationFieldEntity) {
                continue;
            }
            if (access.antarchy$getTimeDilationRate() >= TimeDilationMath.NORMAL_RATE) {
                continue;
            }
            access.antarchy$setTimeDilationRate(TimeDilationMath.NORMAL_RATE);
            TimeDilationApi.syncEntityRate(entity, TimeDilationMath.NORMAL_RATE);
        }
    }
}
