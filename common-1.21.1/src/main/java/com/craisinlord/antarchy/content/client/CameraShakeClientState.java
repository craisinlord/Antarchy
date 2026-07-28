package com.craisinlord.antarchy.content.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class CameraShakeClientState {
    public static final double TORETERROR_RANGE = 48.0D;
    public static final float TORETERROR_STRENGTH = 1.0F;
    public static final double NIGHTMARE_RANGE = 32.0D;
    public static final float NIGHTMARE_STRENGTH = 1.35F;

    private record Source(double range, float strength, BooleanSupplier active) {
    }

    private record Impact(Vec3 position, float intensity, int remainingTicks, int totalTicks, float radius) {
        private Impact ticked() {
            return new Impact(position, intensity, remainingTicks - 1, totalTicks, radius);
        }
    }

    private static final Map<LivingEntity, Source> SOURCES = new HashMap<>();
    private static final java.util.List<Impact> IMPACTS = new java.util.ArrayList<>();

    private CameraShakeClientState() {
    }

    public static void register(LivingEntity entity, double range, float strength, BooleanSupplier active) {
        SOURCES.putIfAbsent(entity, new Source(range, strength, active));
    }

    public static float getStrength(Vec3 cameraPos) {
        float total = 0.0F;
        Iterator<Map.Entry<LivingEntity, Source>> iterator = SOURCES.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<LivingEntity, Source> entry = iterator.next();
            LivingEntity entity = entry.getKey();
            Source source = entry.getValue();
            if (entity.isRemoved() || !entity.isAlive() || !source.active().getAsBoolean()) {
                iterator.remove();
                continue;
            }

            double distance = Math.sqrt(cameraPos.distanceToSqr(entity.position().add(0.0D, entity.getBbHeight() * 0.5D, 0.0D)));
            if (distance > source.range()) {
                continue;
            }

            total += (float) ((1.0D - distance / source.range()) * source.strength());
        }

        Iterator<Impact> impactIterator = IMPACTS.iterator();
        while (impactIterator.hasNext()) {
            Impact impact = impactIterator.next();
            if (impact.remainingTicks() <= 0) {
                impactIterator.remove();
                continue;
            }
            double distance = Math.sqrt(cameraPos.distanceToSqr(impact.position()));
            if (distance > impact.radius()) {
                continue;
            }
            total += (float) ((1.0D - distance / impact.radius()) * impact.intensity() * (impact.remainingTicks() / (float) Math.max(1, impact.totalTicks())));
        }

        return total;
    }

    public static void triggerImpact(Vec3 position, float intensity, int durationTicks, float radius) {
        int ticks = Math.max(1, durationTicks);
        IMPACTS.add(new Impact(position, intensity, ticks, ticks, Math.max(1.0F, radius)));
    }

    public static void tick() {
        for (int i = IMPACTS.size() - 1; i >= 0; i--) {
            Impact impact = IMPACTS.get(i);
            if (impact.remainingTicks() <= 1) {
                IMPACTS.remove(i);
            } else {
                IMPACTS.set(i, impact.ticked());
            }
        }
    }

    public static void clear() {
        SOURCES.clear();
        IMPACTS.clear();
    }
}
