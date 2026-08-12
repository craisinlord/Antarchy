package com.craisinlord.antarchy.content.minecart;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AntimetalDismountHelper {
    private static final Vec3[] SEARCH_OFFSETS = new Vec3[]{
            new Vec3(-1, -1, 0),
            new Vec3(1, -1, 0),
            new Vec3(0, -1, -1),
            new Vec3(0, -1, 1),
            new Vec3(-1, -1, -1),
            new Vec3(-1, -1, 1),
            new Vec3(1, -1, -1),
            new Vec3(1, -1, 1),
            new Vec3(0, -1.6, 0)
    };

    private AntimetalDismountHelper() {
    }

    public static Vec3 findDismountPosition(AbstractMinecart cart, LivingEntity passenger) {
        Level level = cart.level();
        Vec3 cartPos = cart.position();
        double width = Math.max(passenger.getBbWidth(), 0.6D);
        double height = passenger.getBbHeight();

        for (Vec3 offset : SEARCH_OFFSETS) {
            Vec3 candidate = cartPos.add(offset);
            if (isClear(level, candidate, width, height)) {
                return candidate;
            }
        }
        return cartPos.add(0, -1.0D, 0);
    }

    private static boolean isClear(Level level, Vec3 pos, double width, double height) {
        AABB box = new AABB(
                pos.x - width / 2.0D, pos.y, pos.z - width / 2.0D,
                pos.x + width / 2.0D, pos.y + height, pos.z + width / 2.0D
        );
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int minX = net.minecraft.util.Mth.floor(box.minX);
        int maxX = net.minecraft.util.Mth.floor(box.maxX);
        int minY = net.minecraft.util.Mth.floor(box.minY);
        int maxY = net.minecraft.util.Mth.floor(box.maxY);
        int minZ = net.minecraft.util.Mth.floor(box.minZ);
        int maxZ = net.minecraft.util.Mth.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutable.set(x, y, z);
                    if (!level.getBlockState(mutable).getCollisionShape(level, mutable).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
