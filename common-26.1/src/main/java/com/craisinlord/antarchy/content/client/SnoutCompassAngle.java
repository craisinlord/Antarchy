package com.craisinlord.antarchy.content.client;

import com.craisinlord.antarchy.content.item.SnoutCompassItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class SnoutCompassAngle {
    private SnoutCompassAngle() {
    }

    public static float angle(ItemStack stack, ClientLevel level, Entity entity, int seed) {
        if (level == null && entity != null && entity.level() instanceof ClientLevel clientLevel) {
            level = clientLevel;
        }
        if (level == null || entity == null) {
            return 0.0F;
        }
        BlockPos target = entity instanceof LivingEntity ? SnoutCompassItem.target(stack, level) : null;
        double time = level.getGameTime();
        if (target == null || entity.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D) > 4225.0D) {
            return (float) Mth.positiveModulo(time * 0.013D + seed * 0.17D + Math.sin(time * 0.045D) * 0.13D, 1.0D);
        }
        double dx = target.getX() + 0.5D - entity.getX();
        double dz = target.getZ() + 0.5D - entity.getZ();
        double angle = 0.25D - entity.getYRot() / 360.0D + Math.atan2(dz, dx) / (Math.PI * 2.0D);
        if (dx * dx + dz * dz < 4.0D) {
            angle += Math.sin(time * 0.2D) * 0.025D;
        }
        return (float) Mth.positiveModulo(angle, 1.0D);
    }
}
