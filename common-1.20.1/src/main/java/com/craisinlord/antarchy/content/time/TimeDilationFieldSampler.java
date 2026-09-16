package com.craisinlord.antarchy.content.time;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class TimeDilationFieldSampler {
    private TimeDilationFieldSampler() {
    }

    public static double sample(List<TimeDilationFieldEntity> fields, Vec3 position) {
        return sample(fields, position.x, position.y, position.z);
    }

    public static double sample(List<TimeDilationFieldEntity> fields, double x, double y, double z) {
        double combinedRate = TimeDilationMath.NORMAL_RATE;
        for (TimeDilationFieldEntity field : fields) {
            Vec3 center = field.position();
            double dx = center.x - x;
            double dy = center.y - y;
            double dz = center.z - z;
            double distanceSqr = dx * dx + dy * dy + dz * dz;
            if (distanceSqr >= field.fieldRadiusSqr()) {
                continue;
            }
            double falloff = TimeDilationMath.gaussianFalloff(Math.sqrt(distanceSqr), field.fieldRadius());
            combinedRate *= TimeDilationMath.localFieldRate(field.effectiveFieldRate(), falloff);
        }
        return TimeDilationMath.clampRate(combinedRate);
    }

    public static double sample(List<TimeDilationFieldEntity> fields, Entity entity) {
        double combinedRate = TimeDilationMath.NORMAL_RATE;
        for (TimeDilationFieldEntity field : fields) {
            if (!field.affects(entity)) {
                continue;
            }
            Vec3 center = field.position();
            double dx = center.x - entity.getX();
            double dy = center.y - entity.getY();
            double dz = center.z - entity.getZ();
            double distanceSqr = dx * dx + dy * dy + dz * dz;
            double distance = Math.sqrt(distanceSqr);
            double radius = field.fieldRadius();
            double falloff;
            if (field.isChronosphere()) {
                if (distance >= radius * 2.0D) continue;
                falloff = distance <= radius ? 1.0D : TimeDilationMath.gaussianFalloff(distance - radius, radius);
            } else if (distanceSqr < field.fieldRadiusSqr()) {
                falloff = TimeDilationMath.gaussianFalloff(distance, radius);
            } else {
                continue;
            }
            if (falloff > 0.0D) {
                combinedRate *= TimeDilationMath.localFieldRate(field.effectiveFieldRate(), falloff);
            }
        }
        return TimeDilationMath.clampRate(combinedRate);
    }
}
