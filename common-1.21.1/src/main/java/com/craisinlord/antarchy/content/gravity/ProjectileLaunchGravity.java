package com.craisinlord.antarchy.content.gravity;

import net.minecraft.world.phys.Vec3;

/** Converts a projectile launch vector from its shooter's gravity frame to world space. */
public final class ProjectileLaunchGravity {
    private ProjectileLaunchGravity() {
    }

    public static Vec3 toWorld(Vec3 launchDirection, AntarchyGravityDirection gravityDirection) {
        return AntarchyGravityRotationUtil.vecPlayerToWorld(launchDirection, gravityDirection);
    }
}
