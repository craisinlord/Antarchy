package com.craisinlord.antarchy.content.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PrimordialGrassTrample {
    public static final int MAX_POINTS = PrimordialGrassTrampleField.MAX_POINTS;

    private static final double RANGE = PrimordialGrassTrampleField.RANGE;
    private static final double RANGE_SQR = RANGE * RANGE;
    private static final double GROUNDED_VERTICAL_SPEED = 0.1D;
    private static final PrimordialGrassTrampleField FIELD = new PrimordialGrassTrampleField();

    private PrimordialGrassTrample() {
    }

    public static void reset() {
        FIELD.clear();
    }

    public static void tick(ClientLevel level, Vec3 center) {
        FIELD.advance();
        AABB area = new AABB(
                center.x - RANGE, center.y - RANGE, center.z - RANGE,
                center.x + RANGE, center.y + RANGE, center.z + RANGE);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> entity.isAlive()
                        && !entity.isSpectator()
                        && isNearGround(entity)
                        && entity.distanceToSqr(center) <= RANGE_SQR);
        if (candidates.size() > MAX_POINTS) {
            candidates = new ArrayList<>(candidates);
            candidates.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
        }
        int stamped = Math.min(candidates.size(), MAX_POINTS);
        for (int i = 0; i < stamped; i++) {
            LivingEntity entity = candidates.get(i);
            float radius = Math.max(1.1F, entity.getBbWidth() * 0.5F + 0.85F);
            float moveX = (float) (entity.getX() - entity.xo);
            float moveZ = (float) (entity.getZ() - entity.zo);
            float speed = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
            float headingX = speed > 0.01F ? moveX / speed : 0.0F;
            float headingZ = speed > 0.01F ? moveZ / speed : 0.0F;
            FIELD.stamp(entity.getId(), entity.getX(), entity.getY(), entity.getZ(), radius, headingX, headingZ);
        }
    }

    public static int write(double camX, double camY, double camZ, float partialTick, float[] positions, float[] motions) {
        return FIELD.write(camX, camY, camZ, partialTick, positions, motions);
    }

    private static boolean isNearGround(LivingEntity entity) {
        if (entity.onGround()) {
            return true;
        }
        boolean flying = entity.isFallFlying() || entity instanceof Player player && player.getAbilities().flying;
        return !flying && Math.abs(entity.getDeltaMovement().y) < GROUNDED_VERTICAL_SPEED;
    }
}
