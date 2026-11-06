package com.craisinlord.antarchy.content.portalgun;

import net.minecraft.world.phys.Vec3;

public final class PortalGunMoonMath {
    public static final double AIM_TOLERANCE_DEGREES = 12.0D;
    private static final double AIM_COSINE = Math.cos(Math.toRadians(AIM_TOLERANCE_DEGREES));
    private static final double MIN_MOON_HEIGHT = 0.05D;

    private PortalGunMoonMath() {
    }

    public static Vec3 moonDirection(double timeOfDay) {
        double angle = timeOfDay * Math.PI * 2.0D;
        return new Vec3(Math.sin(angle), -Math.cos(angle), 0.0D);
    }

    public static boolean isAimedAtMoon(Vec3 look, double timeOfDay) {
        Vec3 moon = moonDirection(timeOfDay);
        return moon.y > MIN_MOON_HEIGHT && look.normalize().dot(moon) >= AIM_COSINE;
    }
}
