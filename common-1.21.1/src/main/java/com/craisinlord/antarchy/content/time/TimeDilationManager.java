package com.craisinlord.antarchy.content.time;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import com.craisinlord.antarchy.content.effect.DilatedMobEffect;
import com.craisinlord.antarchy.content.effect.RoyalEffectEligibility;
import com.craisinlord.antarchy.content.effect.RoyalEffectHooks;

public final class TimeDilationManager {
    private TimeDilationManager() {
    }

    public static void tickServer(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            List<TimeDilationFieldEntity> fields = collectFields(level);
            syncFieldSnapshots(level, fields);
            updateEntities(level, fields);
            if (!fields.isEmpty()) {
                TimeDilationParticles.spawnFieldBorders(level, fields);
            }
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
            if (entity.getType().is(com.craisinlord.antarchy.content.AntarchyTags.Entities.TIME_DILATION_IMMUNE)
                    || (entity instanceof net.minecraft.world.entity.LivingEntity living && !RoyalEffectEligibility.canApplyDilated(living))) {
                if (RoyalEffectHooks.dilatedHolder() != null && entity instanceof net.minecraft.world.entity.LivingEntity living) {
                    living.removeEffect(RoyalEffectHooks.dilatedHolder());
                }
                TimeDilationApi.clearRate(entity);
                continue;
            }
            double previousRate = access.antarchy$getTimeDilationRate();
            double fieldRate = TimeDilationFieldSampler.sample(fields, entity.position());
            double effectRate = RoyalEffectHooks.dilatedHolder() != null
                    && entity instanceof net.minecraft.world.entity.LivingEntity living
                    && living.hasEffect(RoyalEffectHooks.dilatedHolder())
                    ? DilatedMobEffect.RATE : TimeDilationMath.NORMAL_RATE;
            double rate = Math.min(fieldRate, effectRate);
            if (rate >= TimeDilationMath.NORMAL_RATE) {
                TimeDilationApi.clearRate(entity);
            } else {
                access.antarchy$setTimeDilationRate(rate);
            }
            if (rate < TimeDilationMath.NORMAL_RATE && Math.abs(previousRate - rate) > 0.001D) {
                TimeDilationApi.syncEntityRate(entity, rate);
            }
        }
    }

}
