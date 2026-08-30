package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.time.TimeDilationEntityAccess;
import com.craisinlord.antarchy.content.time.TimeDilationFieldEntity;
import com.craisinlord.antarchy.content.time.TimeDilationFieldSampler;
import com.craisinlord.antarchy.content.time.TimeDilationMath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

public final class ClientTimeDilationTicker {
    private static boolean hadFields;
    private static volatile List<TimeDilationFieldEntity> activeFields = List.of();
    private static final Map<Entity, Double> ANIM_OFFSETS = new WeakHashMap<>();

    private ClientTimeDilationTicker() {
    }

    public static void tick(ClientLevel level) {
        List<TimeDilationFieldEntity> fields = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof TimeDilationFieldEntity field && field.isAlive()) {
                fields.add(field);
            }
        }
        activeFields = fields;

        if (fields.isEmpty()) {
            if (hadFields) {
                resetRates(level);
                hadFields = false;
            }
            return;
        }
        hadFields = true;

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof TimeDilationEntityAccess access) || entity instanceof TimeDilationFieldEntity) {
                continue;
            }
            double rate = TimeDilationFieldSampler.sample(fields, entity.position());
            access.antarchy$setTimeDilationRate(rate);
            if (rate < TimeDilationMath.NORMAL_RATE) {
                ANIM_OFFSETS.merge(entity, rate - TimeDilationMath.NORMAL_RATE, Double::sum);
            }
        }
    }

    public static float dilateAnimationTime(Entity entity, float vanillaTime) {
        Double offset = ANIM_OFFSETS.get(entity);
        return offset == null ? vanillaTime : (float) (vanillaTime + offset);
    }

    public static double rateAt(double x, double y, double z) {
        List<TimeDilationFieldEntity> fields = activeFields;
        if (fields.isEmpty()) {
            return TimeDilationMath.NORMAL_RATE;
        }
        return TimeDilationFieldSampler.sample(fields, x, y, z);
    }

    private static void resetRates(ClientLevel level) {
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof TimeDilationEntityAccess access) || entity instanceof TimeDilationFieldEntity) {
                continue;
            }
            if (access.antarchy$getTimeDilationRate() < TimeDilationMath.NORMAL_RATE) {
                access.antarchy$setTimeDilationRate(TimeDilationMath.NORMAL_RATE);
            }
        }
    }
}
