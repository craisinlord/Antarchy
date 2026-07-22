package com.craisinlord.antarchy.fabric.util;

import com.craisinlord.antarchy.content.fluid.AntarchyFluidChecks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class CustomFluidPhysicsChecks {
    private CustomFluidPhysicsChecks() {
    }

    public static boolean isTouchingAntiwater(Entity entity) {
        AABB box = entity.getBoundingBox().inflate(0.05D);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    if (AntarchyFluidChecks.isAntiwater(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isTouchingWaterlikeFluid(Entity entity) {
        AABB box = entity.getBoundingBox().inflate(0.05D);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    if (AntarchyFluidChecks.usesWaterLikePhysics(fluidState)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Vec3 getAntiwaterFlow(Entity entity) {
        return getAverageFlow(entity, true);
    }

    public static Vec3 getWaterlikeFlow(Entity entity) {
        return getAverageFlow(entity, false);
    }

    private static Vec3 getAverageFlow(Entity entity, boolean antiwater) {
        AABB box = entity.getBoundingBox().inflate(0.05D);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Vec3 totalFlow = Vec3.ZERO;
        int matches = 0;
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    cursor.set(x, y, z);
                    FluidState fluidState = entity.level().getFluidState(cursor);
                    boolean matchesFluid = antiwater
                            ? AntarchyFluidChecks.isAntiwater(fluidState)
                            : AntarchyFluidChecks.usesWaterLikePhysics(fluidState);
                    if (!matchesFluid) {
                        continue;
                    }

                    totalFlow = totalFlow.add(fluidState.getFlow(entity.level(), cursor));
                    matches++;
                }
            }
        }

        if (matches == 0 || totalFlow.lengthSqr() < 1.0E-6D) {
            return Vec3.ZERO;
        }

        return totalFlow.scale(1.0D / matches);
    }
}
