package com.craisinlord.antarchy.fabric.util;

import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public final class CustomFluidPhysicsChecks {
    private CustomFluidPhysicsChecks() {
    }

    public static boolean isTouchingAntiwater(Entity entity) {
        return isTouching(entity, AntarchyFluidChecks::isAntiwater);
    }

    public static boolean isTouchingWaterlikeFluid(Entity entity) {
        return isTouching(entity, AntarchyFluidChecks::usesWaterLikePhysics);
    }

    public static Vec3 getAntiwaterFlow(Entity entity) {
        return getAverageFlow(entity, AntarchyFluidChecks::isAntiwater);
    }

    public static Vec3 getWaterlikeFlow(Entity entity) {
        return getAverageFlow(entity, AntarchyFluidChecks::usesWaterLikePhysics);
    }

    private static boolean isTouching(Entity entity, Predicate<FluidState> matches) {
        AABB box = entity.getBoundingBox().deflate(0.001D);
        Level level = entity.level();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX); x < Mth.ceil(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y < Mth.ceil(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z < Mth.ceil(box.maxZ); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = level.getFluidState(cursor);
                    if (matches.test(fluidState) && y + fluidState.getHeight(level, cursor) >= box.minY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Vec3 getAverageFlow(Entity entity, Predicate<FluidState> matches) {
        AABB box = entity.getBoundingBox().deflate(0.001D);
        Level level = entity.level();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Vec3 totalFlow = Vec3.ZERO;
        int count = 0;
        for (int x = Mth.floor(box.minX); x < Mth.ceil(box.maxX); x++) {
            for (int y = Mth.floor(box.minY); y < Mth.ceil(box.maxY); y++) {
                for (int z = Mth.floor(box.minZ); z < Mth.ceil(box.maxZ); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = level.getFluidState(cursor);
                    if (!matches.test(fluidState) || y + fluidState.getHeight(level, cursor) < box.minY) {
                        continue;
                    }

                    totalFlow = totalFlow.add(fluidState.getFlow(level, cursor));
                    count++;
                }
            }
        }

        if (count == 0 || totalFlow.lengthSqr() < 1.0E-6D) {
            return Vec3.ZERO;
        }

        return totalFlow.scale(1.0D / count);
    }
}
