package com.craisinlord.antarchy.content.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CavarynBurrowingMobBehavior {
    private static final int MAX_UPWARD_SEARCH = 8;

    private CavarynBurrowingMobBehavior() {
    }

    public static void moveOutOfBlocks(Mob mob) {
        if (!(mob.level() instanceof ServerLevel level) || !mob.isAlive()) {
            return;
        }
        AABB box = mob.getBoundingBox();
        if (level.noCollision(mob, box)) {
            return;
        }

        double startY = mob.getY();
        for (int offset = 1; offset <= MAX_UPWARD_SEARCH; offset++) {
            double candidateY = Math.floor(startY) + offset;
            AABB candidateBox = box.move(0.0D, candidateY - startY, 0.0D);
            if (!level.noCollision(mob, candidateBox)) {
                continue;
            }

            mob.setPos(mob.getX(), candidateY, mob.getZ());
            Vec3 motion = mob.getDeltaMovement();
            mob.setDeltaMovement(motion.x, Math.max(0.0D, motion.y), motion.z);
            mob.fallDistance = 0.0F;
            mob.getNavigation().stop();
            return;
        }
    }
}
