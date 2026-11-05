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

    public static float angle(ItemStack stack, ClientLevel level, LivingEntity holder, int seed) {
        Entity entity = holder == null ? stack.getEntityRepresentation() : holder;
        if (level == null && entity != null && entity.level() instanceof ClientLevel clientLevel) {
            level = clientLevel;
        }
        if (level == null || entity == null) {
            return 0.0F;
        }
        BlockPos target = entity instanceof LivingEntity ? SnoutCompassItem.target(stack, level) : null;
        double time = level.getGameTime();
        if (target == null || entity.distanceToSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5) > 4225.0) {
            return (float) Mth.positiveModulo(time * 0.013 + seed * 0.17 + Math.sin(time * 0.045) * 0.13, 1.0);
        }
        double dx = target.getX() + 0.5 - entity.getX();
        double dz = target.getZ() + 0.5 - entity.getZ();
        double angle = 0.75 - entity.getYRot() / 360.0 + Math.atan2(dz, dx) / (Math.PI * 2.0);
        if (dx * dx + dz * dz < 4.0) {
            angle += Math.sin(time * 0.2) * 0.025;
        }
        return (float) Mth.positiveModulo(angle, 1.0);
    }
}
