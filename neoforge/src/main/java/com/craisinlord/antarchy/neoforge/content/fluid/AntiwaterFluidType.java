package com.craisinlord.antarchy.neoforge.content.fluid;

import com.craisinlord.antarchy.content.gravity.AntarchyGravityApi;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityDirection;
import com.craisinlord.antarchy.content.gravity.AntarchyGravityRotationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;

public class AntiwaterFluidType extends FluidType {

    public AntiwaterFluidType(Properties properties) {
        super(properties);
    }

    
    @Override
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        double prevY = entity.getY();
        float swimSpeed = entity.isSprinting() ? 0.026F : 0.02F;

        entity.moveRelative(swimSpeed, movementVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        Vec3 motion = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.onClimbable()) {
            motion = new Vec3(motion.x, 0.2, motion.z);
        }

        AntarchyGravityDirection gravityDirection = AntarchyGravityApi.getGravityDirection(entity);
        Vec3 worldMotion = gravityDirection.isInverted()
                ? AntarchyGravityRotationUtil.vecPlayerToWorld(motion, gravityDirection)
                : motion;

        double drag = entity.isSwimming() ? 0.9 : 0.8;
        worldMotion = worldMotion.scale(drag);
        boolean isFlying = entity instanceof Player p && p.getAbilities().flying;
        if (!isFlying) {
            worldMotion = worldMotion.add(0.0D, 0.005D, 0.0D);
        }

        entity.setDeltaMovement(
                gravityDirection.isInverted()
                        ? AntarchyGravityRotationUtil.vecWorldToPlayer(worldMotion, gravityDirection)
                        : worldMotion
        );
        if (entity.horizontalCollision && entity.isFree(
                worldMotion.x,
                worldMotion.y + 0.6 - entity.getY() + prevY,
                worldMotion.z)) {
            Vec3 boostedWorldMotion = new Vec3(worldMotion.x, 0.3D, worldMotion.z);
            entity.setDeltaMovement(
                    gravityDirection.isInverted()
                            ? AntarchyGravityRotationUtil.vecWorldToPlayer(boostedWorldMotion, gravityDirection)
                            : boostedWorldMotion
            );
        }

        return true;
    }

    
    @Override
    public void setItemMovement(ItemEntity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        entity.setDeltaMovement(
                vec3.x * 0.99,
                vec3.y + (vec3.y < 0.06 ? 5.0E-4 : 0.0),
                vec3.z * 0.99
        );
    }
}
