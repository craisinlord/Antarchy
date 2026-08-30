package com.craisinlord.antarchy.content.time;

import java.util.List;
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
            combinedRate *= TimeDilationMath.localFieldRate(field.fieldRate(), falloff);
        }
        return TimeDilationMath.clampRate(combinedRate);
    }
}
